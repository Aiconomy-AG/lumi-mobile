package org.example.project.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.domain.orders.Order
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun OrderTable(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit
) {
    Box(
        modifier = Modifier
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
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            orders.forEachIndexed { index, order ->
                OrderTableRow(
                    order = order,
                    onClick = {
                        onOrderClick(order)
                    }
                )
                if (index != orders.lastIndex) {
                    HorizontalDivider(color = AppColorPalette.Border)
                }
            }

            if (orders.isEmpty()) {
                EmptyOrderTableRow()
            }
        }
    }
}

@Composable
private fun OrderTableRow(
    order: Order,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${order.id}",
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.Emphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            OrderStatusBadge(status = order.status)
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Text(
            text = order.customerLabel(),
            color = AppColorPalette.TextPrimary.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = "${order.items.size} ${LocalAppStrings.current.text("Items")} · ${formatOrderPrice(order.totalAmount)}",
            color = AppColorPalette.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = formatOrderDate(order.createdAt),
            color = AppColorPalette.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val statusColor = orderStatusColor(status)

    Box(
        modifier = Modifier
            .background(
                color = statusColor.background,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = statusColor.content
        )
    }
}

@Composable
private fun EmptyOrderTableRow() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.text("No orders found."),
            color = AppColorPalette.TextSecondary
        )
    }
}

private fun formatOrderDate(createdAt: String): String {
    return createdAt.substringBefore("T").ifBlank { createdAt }
}

private fun Order.customerLabel(): String {
    return customer?.username?.takeIf { it.isNotBlank() }
        ?: customer?.email?.takeIf { it.isNotBlank() }
        ?: "—"
}
