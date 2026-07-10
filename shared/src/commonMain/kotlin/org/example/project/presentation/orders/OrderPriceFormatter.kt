package org.example.project.presentation.orders

import org.example.project.presentation.theme.formatChf

internal fun formatOrderPrice(amount: Double): String = formatChf(amount)
