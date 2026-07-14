package org.example.project.domain.calls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual object CallPermissions {
    private var appContext: Context? = null
    private var requestHandler: (suspend (Array<String>) -> Boolean)? = null
    private val mutex = Mutex()

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        appContext = context.applicationContext
    }

    fun bindRequestHandler(handler: suspend (Array<String>) -> Boolean) {
        requestHandler = handler
    }

    fun unbindRequestHandler() {
        requestHandler = null
    }

    actual fun hasAudio(): Boolean = isGranted(Manifest.permission.RECORD_AUDIO)

    actual fun hasCamera(): Boolean = isGranted(Manifest.permission.CAMERA)

    actual suspend fun ensureForCall(type: String): Boolean {
        val needed = permissionsFor(type).filterNot { isGranted(it) }
        if (needed.isEmpty()) return true
        val handler = requestHandler ?: return false
        return mutex.withLock {
            handler(needed.toTypedArray())
        }
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
