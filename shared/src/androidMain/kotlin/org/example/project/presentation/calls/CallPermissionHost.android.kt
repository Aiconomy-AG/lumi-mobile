package org.example.project.presentation.calls

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.domain.calls.CallPermissions
import kotlin.coroutines.resume

@Composable
actual fun CallPermissionHost() {
    val pendingContinuation = remember { mutableStateOf<CancellableContinuation<Boolean>?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val granted = results.values.all { it }
        pendingContinuation.value?.takeIf { it.isActive }?.resume(granted)
        pendingContinuation.value = null
    }

    DisposableEffect(launcher) {
        CallPermissions.bindRequestHandler { permissions ->
            suspendCancellableCoroutine { continuation ->
                pendingContinuation.value = continuation
                launcher.launch(permissions)
            }
        }
        onDispose {
            CallPermissions.unbindRequestHandler()
            pendingContinuation.value?.takeIf { it.isActive }?.resume(false)
            pendingContinuation.value = null
        }
    }
}
