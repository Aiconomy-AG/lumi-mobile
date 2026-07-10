package org.example.project.presentation.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Product
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.formatChf
import org.example.project.presentation.theme.formatChfRange

@Composable
fun ProductTable(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .padding(12.dp)
                .width(790.dp)
        ) {
            ProductTableHeader()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = products,
                    key = { product -> product.id },
                ) { product ->
                    ProductTableRow(
                        product = product,
                        onClick = {
                            onProductClick(product)
                        }
                    )
                }

                if (products.isEmpty()) {
                    item {
                        EmptyProductTableRow()
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: Int
) {
    Text(
        text = text,
        color = AppColorPalette.TextSecondary,
        style = AppTextStyles.TableHeader,
        modifier = Modifier.width(width.dp)
    )
}

@Composable
private fun ProductTableHeader() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TableHeaderCell(strings.text("Product"), 220)
        TableHeaderCell(strings.text("SKU"), 150)
        TableHeaderCell(strings.text("Category"), 150)
        TableHeaderCell(strings.text("Stock"), 120)
        TableHeaderCell(strings.text("Price"), 150)
    }
}

@Composable
private fun ProductTableRow(
    product: Product,
    onClick: () -> Unit
) {
    val totalStock = productTotalStock(product)
    val displayPrice = productDisplayPrice(product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(
            text = product.name,
            width = 220,
            color = AppColorPalette.TextPrimary
        )

        TableCell(
            text = product.sku ?: "-",
            width = 150,
            color = AppColorPalette.TextSecondary
        )

        TableCell(
            text = product.categoryName ?: "-",
            width = 150,
            color = AppColorPalette.TextSecondary
        )

        TableCell(
            text = if (totalStock == 0) "Out of stock" else totalStock.toString(),
            width = 120,
            color = when {
                totalStock == 0 -> AppColorPalette.Error
                totalStock <= 5 -> AppColorPalette.Primary
                else -> AppColorPalette.Success
            }
        )

        TableCell(
            text = displayPrice,
            width = 150,
            color = AppColorPalette.TextPrimary
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width.dp)
    )
}

@Composable
private fun EmptyProductTableRow() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.text("No products found."),
            color = AppColorPalette.TextSecondary
        )
    }
}

private fun productTotalStock(product: Product): Int {
    return if (product.variants.isNotEmpty()) {
        product.variants.sumOf { variant ->
            variant.stockQuantity
        }
    } else {
        product.stockQuantity
    }
}

private fun productDisplayPrice(product: Product): String {
    if (product.variants.isEmpty()) {
        return formatChf(product.price)
    }

    val prices = product.variants.map { variant ->
        variant.price
    }

    val min = prices.minOrNull() ?: product.price
    val max = prices.maxOrNull() ?: product.price

    return formatChfRange(min, max)
}
