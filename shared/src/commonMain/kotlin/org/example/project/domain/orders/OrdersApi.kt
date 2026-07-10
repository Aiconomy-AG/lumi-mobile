package org.example.project.domain.orders

interface OrdersApi {
    suspend fun getOrders(): Result<List<Order>>
}
