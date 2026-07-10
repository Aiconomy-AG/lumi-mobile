package org.example.project.domain.returns

data class ReturnRequest(
    val id: Int,
    val orderId: Int?,
    val customerId: Int?,
    val shopDomain: String?,
    val shopifyCustomerId: String?,
    val shopifyOrderId: String?,
    val shopifyOrderName: String?,
    val email: String?,
    val items: List<ReturnDisplayItem>,
    val reason: String,
    val notes: String?,
    val status: ReturnStatus,
    val refundAmount: Double?,
    val receivedAt: String?,
    val refundedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class ReturnDisplayItem(
    val title: String,
    val quantity: Int?,
    val details: String?,
)
