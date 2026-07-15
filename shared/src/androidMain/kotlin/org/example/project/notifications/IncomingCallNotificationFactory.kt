package org.example.project.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import org.example.project.domain.calls.IncomingCallActivity

internal object IncomingCallNotificationFactory {
    fun build(context: Context, payload: IncomingCallPayload): Notification {
        val answerIntent = pendingIntent(context, payload, "answer")
        val declineIntent = pendingIntent(context, payload, "decline")
        val viewIntent = pendingIntent(context, payload, "")
        val caller = Person.Builder()
            .setName(payload.callerName)
            .setImportant(true)
            .build()

        return NotificationCompat.Builder(context, NotificationChannels.CALL_CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(viewIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent))
            .setFullScreenIntent(viewIntent, true)
            .build()
    }

    private fun pendingIntent(
        context: Context,
        payload: IncomingCallPayload,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            payload.putInto(this)
            if (action.isNotEmpty()) putExtra("call_action", action)
        }
        return PendingIntent.getActivity(
            context,
            (payload.callId + action).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
