package org.example.project.presentation.orders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.orders.Order
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun OrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        title = {
            Text(
                text = "Order #${order.id}",
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.PageTitle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OrderStatusBadge(status = order.status)

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                DetailRow("Customer", order.customer?.email ?: order.customer?.username ?: "-")
                DetailRow("Payment method", order.paymentMethod ?: "-")
                DetailRow("Payment status", order.paymentStatus ?: "-")
                DetailRow("Shipping address", order.shippingAddress ?: "-")
                DetailRow("Subtotal", formatOrderPrice(order.subtotal))
                DetailRow("Shipping cost", formatOrderPrice(order.shippingCost))
                DetailRow("Total", formatOrderPrice(order.totalAmount))

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                Text(
                    text = "Items",
                    color = AppColorPalette.TextPrimary,
                    style = AppTextStyles.Emphasis
                )

                Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

                if (order.items.isEmpty()) {
                    Text(
                        text = "No items.",
                        color = AppColorPalette.TextSecondary
                    )
                } else {
                    order.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = item.productId?.let { "Product #$it" } ?: "Product",
                                color = AppColorPalette.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "x${item.quantity}",
                                color = AppColorPalette.TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppColorPalette.Primary)
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = AppColorPalette.TextSecondary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = AppColorPalette.TextPrimary
        )
    }
}
