package org.example.project.presentation.components

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
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onPreviousClick,
            enabled = currentPage > 0,
            colors = AppComponentDefaults.paginationButtonColors(),
        ) {
            Text(strings.text("Previous"))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = strings.format(
                "Page {page} of {total}",
                "page" to (currentPage + 1).toString(),
                "total" to totalPages.toString(),
            ),
            color = AppColorPalette.TextSecondary,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = AppComponentDefaults.paginationButtonColors(),
        ) {
            Text(strings.text("Next"))
        }
    }
}
