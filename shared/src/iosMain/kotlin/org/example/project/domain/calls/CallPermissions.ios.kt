package org.example.project.domain.calls

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual object CallPermissions {
    private const val KEY_REQUESTED_ONCE = "call_permissions_requested_once"
    private const val KEY_LAUNCH_PROMPT_SHOWN = "call_permissions_launch_prompt_shown"

    private val mutex = Mutex()
    private var pendingResult: CompletableDeferred<Boolean>? = null
    private val _state = MutableStateFlow(CallPermissionState.DENIED)
    actual val state: StateFlow<CallPermissionState> = _state.asStateFlow()

    actual fun initialize(platformContext: Any?) {
        refresh()
    }

    actual fun hasAudio(): Boolean {
        return AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
    }

    actual fun hasCamera(): Boolean {
        return AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
            AVAuthorizationStatusAuthorized
    }

    actual suspend fun ensureForCall(type: String): Boolean {
        val needsCamera = type == "video" || type == "camera"
        val needsAudio = type != "camera"

        if ((!needsAudio || hasAudio()) && (!needsCamera || hasCamera())) {
            refresh()
            return true
        }

        refresh()
        if (_state.value == CallPermissionState.PERMANENTLY_DENIED) return false

        return mutex.withLock {
            withContext(Dispatchers.Main) {
                preferences().setBool(true, KEY_REQUESTED_ONCE)
                _state.value = CallPermissionState.REQUESTING
                val deferred = CompletableDeferred<Boolean>()
                pendingResult = deferred

                requestMissingPermissions(needsAudio = needsAudio, needsCamera = needsCamera) { granted ->
                    refresh()
                    pendingResult?.complete(granted)
                    pendingResult = null
                }

                deferred.await().also { refresh() }
            }
        }
    }

    actual suspend fun requestLaunchPermissionsIfNeeded(): Boolean {
        if (preferences().boolForKey(KEY_LAUNCH_PROMPT_SHOWN)) {
            refresh()
            return _state.value == CallPermissionState.GRANTED
        }
        preferences().setBool(true, KEY_LAUNCH_PROMPT_SHOWN)
        return ensureForCall("video")
    }

    actual fun refresh() {
        if (hasAudio() && hasCamera()) {
            _state.value = CallPermissionState.GRANTED
            return
        }
        if (_state.value == CallPermissionState.REQUESTING) return
        _state.value = deniedState()
    }

    actual fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    private fun requestMissingPermissions(
        needsAudio: Boolean,
        needsCamera: Boolean,
        onComplete: (Boolean) -> Unit,
    ) {
        fun finish() {
            onComplete(hasAudio() && hasCamera())
        }

        when {
            needsAudio && !hasAudio() -> requestAudio {
                when {
                    needsCamera && !hasCamera() -> requestCamera { finish() }
                    else -> finish()
                }
            }
            needsCamera && !hasCamera() -> requestCamera { finish() }
            else -> finish()
        }
    }

    private fun requestAudio(onComplete: () -> Unit) {
        val session = AVAudioSession.sharedInstance()
        when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> onComplete()
            AVAudioSessionRecordPermissionUndetermined -> {
                session.requestRecordPermission { _ ->
                    onComplete()
                }
            }
            else -> onComplete()
        }
    }

    private fun requestCamera(onComplete: () -> Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> onComplete()
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { _ ->
                    onComplete()
                }
            }
            else -> onComplete()
        }
    }

    private fun preferences() = NSUserDefaults.standardUserDefaults

    private fun deniedState(): CallPermissionState {
        val requestedOnce = preferences().boolForKey(KEY_REQUESTED_ONCE)
        val micDenied = AVAudioSession.sharedInstance().recordPermission ==
            AVAudioSessionRecordPermissionDenied
        val cameraDenied = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
            AVAuthorizationStatusDenied

        return if (requestedOnce && (micDenied || cameraDenied)) {
            CallPermissionState.PERMANENTLY_DENIED
        } else {
            CallPermissionState.DENIED
        }
    }
}
