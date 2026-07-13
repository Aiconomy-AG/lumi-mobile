package org.example.project.presentation.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import org.example.project.domain.stock.Product
import org.example.project.presentation.components.AppListContainer
import org.example.project.presentation.components.AppListRow
import org.example.project.presentation.components.AppStatusBadge
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
    AppListContainer(
        items = products,
        emptyMessage = LocalAppStrings.current.text("No products found."),
        modifier = modifier,
        key = { it.id },
    ) { product ->
        ProductListRow(
            product = product,
            onClick = { onProductClick(product) },
        )
    }
}

@Composable
private fun ProductListRow(
    product: Product,
    onClick: () -> Unit,
) {
    val totalStock = productTotalStock(product)

    AppListRow(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductThumbnail(product = product)

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = product.name,
                        modifier = Modifier.weight(1f),
                        color = AppColorPalette.TextPrimary,
                        style = AppTextStyles.Emphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

                    Text(
                        text = productDisplayPrice(product),
                        color = AppColorPalette.TextPrimary,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = product.sku ?: "—",
                        modifier = Modifier.weight(1f),
                        color = AppColorPalette.TextPrimary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

                    ProductStockBadge(totalStock = totalStock)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.categoryName ?: "—",
                    color = AppColorPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProductThumbnail(product: Product) {
    val modifier = Modifier
        .size(72.dp)
        .clip(RoundedCornerShape(AppDimensions.ControlCornerRadius))
        .background(AppColorPalette.SurfaceVariant)

    if (product.imageUrl.isNullOrBlank()) {
        ProductImagePlaceholder(modifier = modifier)
    } else {
        SubcomposeAsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = AppColorPalette.Primary,
                        strokeWidth = 2.dp,
                    )
                }
            },
            error = {
                ProductImagePlaceholder(modifier = Modifier.fillMaxSize())
            },
        )
    }
}

@Composable
private fun ProductImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(AppColorPalette.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "—",
            color = AppColorPalette.TextSecondary,
            style = AppTextStyles.Emphasis,
        )
    }
}

@Composable
private fun ProductStockBadge(totalStock: Int) {
    AppStatusBadge(
        label = if (totalStock == 0) {
            LocalAppStrings.current.text("Out of stock")
        } else {
            totalStock.toString()
        },
        statusColor = when {
            totalStock == 0 -> AppColorPalette.StatusBlocked
            totalStock <= 5 -> AppColorPalette.StatusInProgress
            else -> AppColorPalette.StatusComplete
        },
    )
}

private fun productTotalStock(product: Product): Int {
    return if (product.variants.isNotEmpty()) {
        product.variants.sumOf { it.stockQuantity }
    } else {
        product.stockQuantity
    }
}

private fun productDisplayPrice(product: Product): String {
    if (product.variants.isEmpty()) {
        return formatChf(product.price)
    }

    val prices = product.variants.map { it.price }
    val min = prices.minOrNull() ?: product.price
    val max = prices.maxOrNull() ?: product.price

    return formatChfRange(min, max)
}
