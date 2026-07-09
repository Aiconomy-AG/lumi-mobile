package org.example.project.data.chat

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun currentTimeLabel(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "HH:mm"
    return formatter.stringFromDate(NSDate())
}

actual fun chatClockTimeLabel(value: String): String {
    val trimmed = value.trim()
    if (trimmed.length >= 5 && trimmed[2] == ':') return trimmed.take(5)

    val normalized = normalizeFractionalSeconds(trimmed)
    val parsedDate = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXXXX",
        "yyyy-MM-dd HH:mm:ss",
    ).firstNotNullOfOrNull { pattern ->
        NSDateFormatter().apply {
            dateFormat = pattern
        }.dateFromString(normalized)
    } ?: return trimmed.toClockFallback()

    val outputFormatter = NSDateFormatter()
    outputFormatter.dateFormat = "HH:mm"
    return outputFormatter.stringFromDate(parsedDate)
}

private fun normalizeFractionalSeconds(value: String): String {
    return value.replace(Regex("""\.(\d{3})\d+(Z|[+-]\d{2}:?\d{2})$""")) {
        ".${it.groupValues[1]}${it.groupValues[2]}"
    }
}

private fun String.toClockFallback(): String {
    val timeStart = indexOf('T').takeIf { it >= 0 }
        ?: indexOf(' ').takeIf { it >= 0 }
        ?: return this

    return drop(timeStart + 1)
        .take(5)
        .takeIf { it.length == 5 && it[2] == ':' }
        ?: this
}
