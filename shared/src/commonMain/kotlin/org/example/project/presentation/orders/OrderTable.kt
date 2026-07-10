package org.example.project.presentation.orders

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.orders.Order
import org.example.project.presentation.components.AppListContainer
import org.example.project.presentation.components.AppListRow
import org.example.project.presentation.components.AppStatusBadge
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun OrderTable(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListContainer(
        items = orders,
        emptyMessage = LocalAppStrings.current.text("No orders found."),
        modifier = modifier,
        key = { it.id },
    ) { order ->
        OrderTableRow(
            order = order,
            onClick = { onOrderClick(order) },
        )
    }
}

@Composable
private fun OrderTableRow(
    order: Order,
    onClick: () -> Unit
) {
    AppListRow(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${order.id}",
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.Emphasis.copy(fontSize = 32.sp),
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

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = order.itemTotalLabel(),
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

            Text(
                text = formatOrderDate(order.createdAt),
                color = AppColorPalette.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    AppStatusBadge(
        label = status,
        statusColor = orderStatusColor(status),
    )
}

fun formatOrderDate(createdAt: String): String {
    return createdAt.substringBefore("T").ifBlank { createdAt }
}

fun Order.customerLabel(): String {
    return customer?.username?.takeIf { it.isNotBlank() }
        ?: customer?.email?.takeIf { it.isNotBlank() }
        ?: "—"
}

@Composable
fun Order.itemTotalLabel(): String {
    return "${items.size} ${LocalAppStrings.current.text("Items")} · ${formatOrderPrice(totalAmount)}"
}
