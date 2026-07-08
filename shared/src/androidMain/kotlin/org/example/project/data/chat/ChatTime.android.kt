package org.example.project.data.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeLabel(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
