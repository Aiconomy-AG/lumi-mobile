package org.example.project.notifications

import android.app.Activity
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual object PushNotifications {
    private const val TAG = "PushNotifications"

    private var appContext: Context? = null

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        appContext = context.applicationContext
        activity = context as? Activity
        NotificationChannels.create(context.applicationContext)
    }

    actual suspend fun requestPermission(): Boolean {
        val context = appContext ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationChannels.areNotificationsEnabled(context)
        }

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun needsRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to get FCM token", exception)
            null
        }
    }

    private var activity: Activity? = null

    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                val exception = task.exception ?: IllegalStateException("Task failed")
                continuation.resumeWithException(exception)
            }
        }
    }
}
