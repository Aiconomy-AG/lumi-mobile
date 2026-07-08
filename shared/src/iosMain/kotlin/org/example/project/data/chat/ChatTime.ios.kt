package org.example.project.data.chat

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun currentTimeLabel(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "HH:mm"
    return formatter.stringFromDate(NSDate())
}
