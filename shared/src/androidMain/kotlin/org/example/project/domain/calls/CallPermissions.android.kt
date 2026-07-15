package org.example.project.domain.calls

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

actual object CallPermissions {
    private const val PREFS_NAME = "call_permissions"
    private const val KEY_REQUESTED_ONCE = "requested_once"
    private const val KEY_LAUNCH_PROMPT_SHOWN = "launch_prompt_shown"

    private var appContext: Context? = null
    private var activity: ComponentActivity? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var pendingResult: CompletableDeferred<Boolean>? = null
    private val mutex = Mutex()
    private val _state = MutableStateFlow(CallPermissionState.DENIED)
    actual val state: StateFlow<CallPermissionState> = _state.asStateFlow()

    /**
     * Must be called from [ComponentActivity.onCreate] so the permission launcher
     * stays tied to a live activity instance (survives configuration changes).
     */
    fun attachActivity(activity: ComponentActivity) {
        pendingResult?.takeIf { !it.isCompleted }?.complete(false)
        pendingResult = null
        this.activity = activity
        appContext = activity.applicationContext
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            val granted = results.values.all { it }
            _state.value = if (granted && hasAudio() && hasCamera()) {
                CallPermissionState.GRANTED
            } else {
                deniedState()
            }
            pendingResult?.complete(granted)
            pendingResult = null
        }
        refresh()
    }

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        appContext = context.applicationContext
        refresh()
    }

    actual fun hasAudio(): Boolean = isGranted(Manifest.permission.RECORD_AUDIO)

    actual fun hasCamera(): Boolean = isGranted(Manifest.permission.CAMERA)

    actual suspend fun ensureForCall(type: String): Boolean {
        val needed = permissionsFor(type).filterNot { isGranted(it) }
        if (needed.isEmpty()) {
            refresh()
            return true
        }
        refresh()
        if (_state.value == CallPermissionState.PERMANENTLY_DENIED) return false
        val launcher = permissionLauncher ?: run {
            _state.value = CallPermissionState.DENIED
            return false
        }
        return mutex.withLock {
            withContext(Dispatchers.Main.immediate) {
                preferences().edit().putBoolean(KEY_REQUESTED_ONCE, true).apply()
                _state.value = CallPermissionState.REQUESTING
                val deferred = CompletableDeferred<Boolean>()
                pendingResult = deferred
                launcher.launch(needed.toTypedArray())
                deferred.await().also { refresh() }
            }
        }
    }

    suspend fun requestAtLaunchIfNeeded(): Boolean {
        val preferences = preferences()
        if (preferences.getBoolean(KEY_LAUNCH_PROMPT_SHOWN, false)) {
            refresh()
            return state.value == CallPermissionState.GRANTED
        }
        preferences.edit().putBoolean(KEY_LAUNCH_PROMPT_SHOWN, true).apply()
        return ensureForCall("video")
    }

    actual suspend fun requestLaunchPermissionsIfNeeded(): Boolean = requestAtLaunchIfNeeded()

    actual fun refresh() {
        val context = appContext ?: return
        if (hasAudio() && hasCamera()) {
            _state.value = CallPermissionState.GRANTED
            return
        }
        if (_state.value == CallPermissionState.REQUESTING) return

        _state.value = deniedState()
    }

    actual fun openAppSettings() {
        val context = appContext ?: return
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun permissionsFor(type: String): List<String> = when (type) {
        "camera" -> listOf(Manifest.permission.CAMERA)
        "video" -> listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        else -> listOf(Manifest.permission.RECORD_AUDIO)
    }

    private fun isGranted(permission: String): Boolean {
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun preferences() = checkNotNull(appContext)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun deniedState(): CallPermissionState {
        val requestedOnce = preferences().getBoolean(KEY_REQUESTED_ONCE, false)
        val currentActivity = activity
        val missing = permissionsFor("video").filterNot { isGranted(it) }
        val canExplain = currentActivity != null && missing.any {
            currentActivity.shouldShowRequestPermissionRationale(it)
        }
        return if (requestedOnce && !canExplain) {
            CallPermissionState.PERMANENTLY_DENIED
        } else {
            CallPermissionState.DENIED
        }
    }
}
