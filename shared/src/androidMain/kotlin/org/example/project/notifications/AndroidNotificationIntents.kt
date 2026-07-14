package org.example.project.notifications

import android.content.Intent

object AndroidNotificationIntents {
    fun handle(intent: Intent?) {
        if (intent == null) return

        val extras = intent.extras ?: return
        val data = buildMap {
            for (key in extras.keySet()) {
                @Suppress("DEPRECATION")
                val value = extras.get(key) as? String ?: continue
                put(key, value)
            }
        }

        val link = NotificationRouter.parse(data) ?: return
        NotificationRouter.emit(link)
    }
}
