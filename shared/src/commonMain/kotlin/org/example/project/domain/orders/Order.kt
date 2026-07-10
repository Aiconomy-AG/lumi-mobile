package org.example.project.domain.orders

import asDouble
import asInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data class Order(
    val id: Int,
    val customerId: Int,
    val status: String,
    val subtotal: Double,
    val shippingCost: Double,
    val totalAmount: Double,
    val shippingAddress: String?,
    val paymentMethod: String?,
    val paymentStatus: String?,
    val createdAt: String,
    val customer: OrderCustomer?,
    val items: List<OrderItem> = emptyList()
)

data class OrderCustomer(
    val id: Int,
    val username: String?,
    val email: String?
)

data class OrderItem(
    val orderId: Int,
    val productId: Int?,
    val quantity: Int
)

@Serializable
data class OrderDto(
    val id: Int,

    @SerialName("customer_id")
    val customerId: Int,

    val status: String,
    val subtotal: JsonElement? = null,

    @SerialName("shipping_cost")
    val shippingCost: JsonElement? = null,

    @SerialName("total_amount")
    val totalAmount: JsonElement? = null,

    @SerialName("shipping_address")
    val shippingAddress: String? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null,

    @SerialName("payment_status")
    val paymentStatus: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    val customer: OrderCustomerDto? = null,

    val items: List<OrderItemDto>? = null
) {
    fun toOrder(): Order {
        return Order(
            id = id,
            customerId = customerId,
            status = status,
            subtotal = subtotal.asDouble(),
            shippingCost = shippingCost.asDouble(),
            totalAmount = totalAmount.asDouble(),
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            createdAt = createdAt,
            customer = customer?.toOrderCustomer(),
            items = items.orEmpty().map { it.toOrderItem() }
        )
    }
}

@Serializable
data class OrderCustomerDto(
    val id: Int,
    val username: String? = null,
    val email: String? = null
) {
    fun toOrderCustomer(): OrderCustomer {
        return OrderCustomer(
            id = id,
            username = username,
            email = email
        )
    }
}

@Serializable
data class OrderItemDto(
    @SerialName("order_id")
    val orderId: Int,

    @SerialName("product_id")
    val productId: Int? = null,

    val quantity: JsonElement? = null
) {
    fun toOrderItem(): OrderItem {
        return OrderItem(
            orderId = orderId,
            productId = productId,
            quantity = quantity.asInt()
        )
    }
}
