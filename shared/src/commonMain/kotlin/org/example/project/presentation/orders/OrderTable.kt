package org.example.project.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val horizontalScrollState = rememberScrollState()
    val rowHeight = 54.dp
    val headerHeight = 36.dp
    val verticalPadding = 24.dp
    val tableHeight = headerHeight + rowHeight * 7 + verticalPadding

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tableHeight)
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
                .horizontalScroll(horizontalScrollState)
                .padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                )
                .width(820.dp)
        ) {
            OrderTableHeader()

            orders.forEach { order ->
                OrderTableRow(
                    order = order,
                    onClick = {
                        onOrderClick(order)
                    }
                )
            }

            val emptyRows = 7 - orders.size

            repeat(emptyRows.coerceAtLeast(0)) {
                EmptyFixedOrderRow()
            }

            if (orders.isEmpty()) {
                EmptyOrderTableRow()
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
private fun OrderTableHeader() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TableHeaderCell(strings.text("Order"), 90)
        TableHeaderCell(strings.text("Customer"), 200)
        TableHeaderCell(strings.text("Status"), 130)
        TableHeaderCell(strings.text("Items"), 90)
        TableHeaderCell(strings.text("Total"), 120)
        TableHeaderCell(strings.text("Created"), 130)
    }
}

@Composable
private fun OrderTableRow(
    order: Order,
    onClick: () -> Unit
) {
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
            text = "#${order.id}",
            width = 90,
            color = AppColorPalette.TextPrimary
        )

        TableCell(
            text = order.customer?.email ?: order.customer?.username ?: "-",
            width = 200,
            color = AppColorPalette.TextSecondary
        )

        Box(modifier = Modifier.width(130.dp)) {
            OrderStatusBadge(status = order.status)
        }

        TableCell(
            text = order.items.size.toString(),
            width = 90,
            color = AppColorPalette.TextSecondary
        )

        TableCell(
            text = formatOrderPrice(order.totalAmount),
            width = 120,
            color = AppColorPalette.TextPrimary
        )

        TableCell(
            text = formatOrderDate(order.createdAt),
            width = 130,
            color = AppColorPalette.TextSecondary
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
private fun TableCell(
    text: String,
    width: Int,
    color: Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width.dp)
    )
}

@Composable
private fun EmptyFixedOrderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
