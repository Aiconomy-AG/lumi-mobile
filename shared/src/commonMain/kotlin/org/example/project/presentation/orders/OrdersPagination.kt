package org.example.project.presentation.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults

@Composable
fun OrdersPagination(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousClick,
            enabled = currentPage > 0,
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            color = AppColorPalette.TextSecondary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Next")
        }
    }
}
