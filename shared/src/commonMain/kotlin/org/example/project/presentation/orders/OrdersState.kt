package org.example.project.presentation.orders

import org.example.project.domain.orders.Order

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredOrders: List<Order>
        get() {
            val query = searchQuery.trim()

            if (query.isBlank()) {
                return orders
            }

            return orders.filter { order ->
                order.id.toString().contains(query, ignoreCase = true) ||
                        order.status.contains(query, ignoreCase = true) ||
                        order.customer?.email?.contains(query, ignoreCase = true) == true ||
                        order.customer?.username?.contains(query, ignoreCase = true) == true
            }
        }

    val orderCount: Int
        get() = orders.size
}
