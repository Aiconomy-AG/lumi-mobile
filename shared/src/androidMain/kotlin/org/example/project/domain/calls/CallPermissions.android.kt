package org.example.project.domain.calls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

actual object CallPermissions {
    private var appContext: Context? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var pendingResult: CompletableDeferred<Boolean>? = null

    actual fun initialize(platformContext: Any?) {
        val activity = platformContext as? ComponentActivity ?: return
        appContext = activity.applicationContext
        if (permissionLauncher != null) return
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            val granted = results.values.all { it }
            pendingResult?.complete(granted)
            pendingResult = null
        }
    }

    actual fun hasAudio(): Boolean {
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }

    actual fun hasCamera(): Boolean {
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun ensureForCall(type: String): Boolean {
        val needed = permissionsFor(type).filterNot { isGranted(it) }
        if (needed.isEmpty()) return true
        val launcher = permissionLauncher ?: return false
        val deferred = CompletableDeferred<Boolean>()
        pendingResult = deferred
        launcher.launch(needed.toTypedArray())
        return deferred.await()
    }

    private fun permissionsFor(type: String): List<String> {
        return if (type == "video") {
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            listOf(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun isGranted(permission: String): Boolean {
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
