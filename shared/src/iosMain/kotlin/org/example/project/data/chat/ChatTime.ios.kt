package org.example.project.data.chat

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

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

actual fun currentActivitySortKey(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun chatActivitySortKey(value: String): Long {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return 0L
    if (trimmed.length >= 5 && trimmed[2] == ':') return currentActivitySortKey()

    val normalized = normalizeFractionalSeconds(trimmed)
    val parsedDate = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXXXX",
        "yyyy-MM-dd HH:mm:ss",
    ).firstNotNullOfOrNull { pattern ->
        NSDateFormatter().apply {
            dateFormat = pattern
        }.dateFromString(normalized)
    } ?: return 0L

    return (parsedDate.timeIntervalSince1970 * 1000).toLong()
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
