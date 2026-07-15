package org.example.project.notifications

object PendingNotificationIntent {
    private var pending: Map<String, String>? = null

    fun enqueue(data: Map<String, String>) {
        if (data.isEmpty()) return
        pending = data
    }

    fun consume(): Map<String, String>? = pending.also { pending = null }
}
