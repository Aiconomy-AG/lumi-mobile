package org.example.project.notifications

import android.content.Intent

object AndroidNotificationIntents {
    fun handle(intent: Intent?) {
        if (intent == null) return

        val extras = intent.extras ?: return
        val data = buildMap {
            for (key in extras.keySet()) {
                val value = extras.getString(key) ?: continue
                put(key, value)
            }
        }

        val link = NotificationRouter.parse(data) ?: return
        NotificationRouter.emit(link)
    }
}
