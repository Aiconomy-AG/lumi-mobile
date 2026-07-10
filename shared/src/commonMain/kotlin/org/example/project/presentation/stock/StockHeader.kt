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
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

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
        Text(
            text = strings.text("Stock"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

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

        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search products..."),
            enabled = !isLoading,
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AppButton(
            onClick = onAddProductClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(strings.text("+ Add product"))
        }
    }
}
