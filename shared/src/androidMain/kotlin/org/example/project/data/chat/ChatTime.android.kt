package org.example.project.data.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun currentTimeLabel(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

actual fun chatClockTimeLabel(value: String): String {
    val trimmed = value.trim()
    if (trimmed.length >= 5 && trimmed[2] == ':') return trimmed.take(5)

    val normalized = normalizeFractionalSeconds(trimmed)
    val parsedDate = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd HH:mm:ss",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.contains("'T'")) {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            }.parse(normalized)
        }.getOrNull()
    } ?: return trimmed.toClockFallback()

    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(parsedDate)
}

actual fun currentActivitySortKey(): Long = System.currentTimeMillis()

actual fun chatActivitySortKey(value: String): Long {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return 0L
    if (trimmed.length >= 5 && trimmed[2] == ':') return currentActivitySortKey()

    val normalized = normalizeFractionalSeconds(trimmed)
    val parsedDate = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd HH:mm:ss",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.contains("'T'")) {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            }.parse(normalized)
        }.getOrNull()
    } ?: return 0L

    return parsedDate.time
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
