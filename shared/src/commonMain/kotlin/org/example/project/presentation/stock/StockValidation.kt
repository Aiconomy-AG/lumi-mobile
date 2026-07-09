package org.example.project.presentation.stock

fun parseStockDouble(value: String): Double? {
    return value
        .trim()
        .replace(",", ".")
        .toDoubleOrNull()
}

fun parseStockInt(value: String): Int? {
    return value
        .trim()
        .toIntOrNull()
}

fun validateRequiredText(
    value: String,
    fieldName: String
): String? {
    return if (value.trim().isBlank()) {
        "$fieldName is required"
    } else {
        null
    }
}

fun validateRequiredDouble(
    value: String,
    fieldName: String
): String? {
    val text = value.trim()

    if (text.isBlank()) {
        return "$fieldName is required"
    }

    val number = parseStockDouble(text)

    if (number == null) {
        return "$fieldName must be a number"
    }

    if (number < 0) {
        return "$fieldName cannot be negative"
    }

    return null
}

fun validateOptionalDouble(
    value: String,
    fieldName: String
): String? {
    val text = value.trim()

    if (text.isBlank()) {
        return null
    }

    val number = parseStockDouble(text)

    if (number == null) {
        return "$fieldName must be a number"
    }

    if (number < 0) {
        return "$fieldName cannot be negative"
    }

    return null
}

fun validateRequiredInt(
    value: String,
    fieldName: String
): String? {
    val text = value.trim()

    if (text.isBlank()) {
        return "$fieldName is required"
    }

    val number = parseStockInt(text)

    if (number == null) {
        return "$fieldName must be a whole number"
    }

    if (number < 0) {
        return "$fieldName cannot be negative"
    }

    return null
}