package org.example.project.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.orders.Order
import org.example.project.domain.orders.OrderItem
import org.example.project.presentation.components.AppDetailField
import org.example.project.presentation.components.AppDetailGridRows
import org.example.project.presentation.components.AppDetailOverlay
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun OrderDetailsOverlay(
    order: Order,
    onBackClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    AppDetailOverlay(
        title = strings.format("Order #{id}", "id" to order.id.toString()),
        onBackClick = onBackClick,
        trailingContent = {
            OrderStatusBadge(status = order.status)
        },
    ) {
        AppDetailGridRows(
            rows = listOf(
                listOf(
                    AppDetailField(
                        label = strings.text("Customer"),
                        value = order.customerLabel(),
                        valueFontWeight = FontWeight.SemiBold,
                    ),
                    AppDetailField(
                        label = strings.text("Created"),
                        value = formatOrderDate(order.createdAt),
                        valueAlpha = 0.85f,
                    ),
                ),
                listOf(
                    AppDetailField(strings.text("Shipping address"), order.shippingAddress),
                ),
                listOf(
                    AppDetailField(strings.text("Subtotal"), formatOrderPrice(order.subtotal)),
                    AppDetailField(strings.text("Shipping cost"), formatOrderPrice(order.shippingCost)),
                    AppDetailField(strings.text("Total"), formatOrderPrice(order.totalAmount)),
                ),
                listOf(
                    AppDetailField(strings.text("Payment method"), order.paymentMethod),
                    AppDetailField(strings.text("Payment status"), order.paymentStatus),
                ),
            ),
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        Text(
            text = strings.text("Items"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.Emphasis,
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        if (order.items.isEmpty()) {
            Text(
                text = strings.text("No items."),
                color = AppColorPalette.TextSecondary,
            )
        } else {
            Column {
                order.items.forEach { item ->
                    OrderItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                color = AppColorPalette.SurfaceVariant,
                shape = RoundedCornerShape(AppDimensions.ControlCornerRadius),
            )
            .padding(AppDimensions.SmallSpacing),
    ) {
        Text(
            text = item.productId?.let {
                strings.format("Product #{id}", "id" to it.toString())
            } ?: strings.text("Product"),
            modifier = Modifier.weight(1f),
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "x${item.quantity}",
            color = AppColorPalette.TextSecondary,
        )
    }
}
