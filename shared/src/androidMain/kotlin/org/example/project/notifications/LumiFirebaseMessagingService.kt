package org.example.project.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LumiFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM message received from=${message.from} data=${message.data}")

        val data = message.data
        val notification = message.notification
        val title = notification?.title ?: data["title"]
        val body = notification?.body ?: data["body"]

        if (title.isNullOrBlank() || body.isNullOrBlank()) {
            return
        }

        showNotification(
            title = title,
            body = body,
            data = data,
        )
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed: $token")
        serviceScope.launch {
            TokenRefreshHandlerHolder.handler?.invoke(token)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        NotificationChannels.create(applicationContext)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationIdFor(data),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.DEFAULT_CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(notificationIdFor(data), notification)
    }

    private fun notificationIdFor(data: Map<String, String>): Int {
        val type = data["type"] ?: "unknown"
        val id = data["task_id"] ?: data["conversation_id"] ?: data["message_id"] ?: ""
        return (type + id).hashCode()
    }

    companion object {
        private const val TAG = "LumiFirebaseMessaging"
    }
}
