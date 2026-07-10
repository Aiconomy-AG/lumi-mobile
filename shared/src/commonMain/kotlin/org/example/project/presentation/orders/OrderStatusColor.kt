package org.example.project.presentation.orders

import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.StatusColor

fun orderStatusColor(status: String): StatusColor {
    return when (status.trim().lowercase()) {
        "completed", "delivered", "paid" -> AppColorPalette.StatusComplete
        "processing", "pending", "shipped" -> AppColorPalette.StatusInProgress
        "cancelled", "canceled", "failed", "refunded" -> AppColorPalette.StatusBlocked
        else -> AppColorPalette.StatusToDo
    }
}
