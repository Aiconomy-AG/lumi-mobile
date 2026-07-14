package org.example.project.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.project.domain.calls.AndroidCallRuntime

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

        if (data["type"] == "workspace_call_incoming") {
            showIncomingCall(title, body, data)
            return
        }

        if (data["type"] == "workspace_call_updated") {
            getSystemService(NotificationManager::class.java)?.cancel(notificationIdFor(data))
            data["call_id"]?.let(AndroidCallRuntime::dismiss)
            AndroidNotificationIntents.handle(Intent().apply { data.forEach(::putExtra) })
            return
        }

        showNotification(
            title = title,
            body = body,
            data = data,
        )
    }

    private fun showIncomingCall(title: String, body: String, data: Map<String, String>) {
        NotificationChannels.create(applicationContext)
        AndroidCallRuntime.initialize(applicationContext)
        data["call_id"]?.let { callId ->
            AndroidCallRuntime.reportIncoming(
                callId,
                data["caller_user_id"] ?: callId,
                data["caller_name"] ?: title,
            )
        }
        val answerIntent = callPendingIntent(data, "answer")
        val declineIntent = callPendingIntent(data, "decline")
        val viewIntent = callPendingIntent(data, "")
        val caller = Person.Builder().setName(data["caller_name"] ?: title).setImportant(true).build()
        val notification = NotificationCompat.Builder(this, NotificationChannels.CALL_CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(viewIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent))
            .setFullScreenIntent(viewIntent, true)
            .build()
        getSystemService(NotificationManager::class.java)?.notify(notificationIdFor(data), notification)
    }

    private fun callPendingIntent(data: Map<String, String>, action: String): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)!!.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
            if (action.isNotEmpty()) putExtra("call_action", action)
        }
        return PendingIntent.getActivity(
            this,
            (notificationIdFor(data).toString() + action).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
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
        data["call_id"]?.let { return ("call" + it).hashCode() }
        val type = data["type"] ?: "unknown"
        val id = data["task_id"] ?: data["conversation_id"] ?: data["message_id"] ?: ""
        return (type + id).hashCode()
    }

    companion object {
        private const val TAG = "LumiFirebaseMessaging"
    }
}
