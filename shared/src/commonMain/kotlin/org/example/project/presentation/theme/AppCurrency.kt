package org.example.project.presentation.theme

import kotlin.math.abs
import kotlin.math.round

fun formatChf(amount: Double): String {
    val cents = round(amount * 100).toLong()
    val whole = cents / 100
    val fraction = abs(cents % 100).toInt()
    val numeric = if (fraction == 0) {
        whole.toString()
    } else {
        "$whole.${fraction.toString().padStart(2, '0')}"
    }
    return "$numeric CHF"
}

fun formatChfRange(min: Double, max: Double): String {
    return if (min == max) {
        formatChf(min)
    } else {
        "${formatChf(min)} - ${formatChf(max)}"
    }
}
