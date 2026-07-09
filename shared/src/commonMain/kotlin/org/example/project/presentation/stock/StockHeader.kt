package org.example.project.presentation.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
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
    Column {
        Text(
            text = "Stock",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$productCount products",
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = "$variantCount variants",
                color = AppColorPalette.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$lowStockCount low stock",
                color = AppColorPalette.Primary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = "$outOfStockCount out of stock",
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Search products...",
                    color = AppColorPalette.TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onAddProductClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("+ Add product")
        }
    }
}