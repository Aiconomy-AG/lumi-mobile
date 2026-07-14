package org.example.project.notifications

import android.content.Intent

object PendingNotificationIntent {
    @Volatile
    private var pending: Intent? = null

    fun enqueue(intent: Intent?) {
        if (intent == null || intent.extras == null) return
        pending = Intent().apply { intent.extras?.let(::putExtras) }
    }

    fun consume(): Intent? = pending.also { pending = null }
}
