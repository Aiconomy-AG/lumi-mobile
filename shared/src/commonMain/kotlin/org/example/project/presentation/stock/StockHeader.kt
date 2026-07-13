package org.example.project.presentation.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions

@Composable
fun StockHeader(
    productCount: Int,
    variantCount: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    searchQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onAddProductClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.format("{count} products", "count" to productCount.toString()),
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = strings.format("{count} variants", "count" to variantCount.toString()),
                color = AppColorPalette.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.format("{count} low stock", "count" to lowStockCount.toString()),
                color = AppColorPalette.Primary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = strings.format("{count} out of stock", "count" to outOfStockCount.toString()),
                color = AppColorPalette.Error
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

            Text(
                text = errorMessage,
                color = AppColorPalette.Error
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = strings.text("Search products..."),
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            StockAddActionButton(
                onClick = onAddProductClick,
                enabled = !isLoading,
            )
        }
    }
}
