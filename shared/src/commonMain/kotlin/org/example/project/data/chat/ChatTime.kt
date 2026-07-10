package org.example.project.data.chat

expect fun currentTimeLabel(): String

expect fun chatClockTimeLabel(value: String): String

expect fun chatActivitySortKey(value: String): Long

expect fun currentActivitySortKey(): Long
