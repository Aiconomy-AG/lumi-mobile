package org.example.project.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val DEFAULT_CHANNEL_ID = "lumi_default"
    const val CALL_CHANNEL_ID = "lumi_calls"
    private const val DEFAULT_CHANNEL_NAME = "General"
    private const val DEFAULT_CHANNEL_DESCRIPTION = "Task and app notifications"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            DEFAULT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = DEFAULT_CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
        notificationManager?.createNotificationChannel(
            NotificationChannel(CALL_CHANNEL_ID, "Lumi calls", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Incoming and active Lumi audio calls"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI, null)
            }
        )
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
