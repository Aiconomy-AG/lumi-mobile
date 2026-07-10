package org.example.project.data.returns

import asDouble
import asInt
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.domain.returns.ReturnDisplayItem
import org.example.project.domain.returns.ReturnRequest
import org.example.project.domain.returns.ReturnStatus
import org.example.project.domain.returns.ReturnsApi

class ReturnsApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String,
) : ReturnsApi {

    override suspend fun getReturns(): Result<List<ReturnRequest>> {
        return try {
            val allReturns = mutableListOf<ReturnRequest>()
            var page = 1
            var lastPage = 1

            do {
                val response = client.get("$baseUrl/workspace/returns") {
                    bearerAuth()
                    parameter("page", page)
                    parameter("per_page", 100)
                }

                val responseText = response.bodyAsText()

                if (response.status != HttpStatusCode.OK) {
                    return Result.failure(Exception(parseErrorMessage(responseText)))
                }

                val body = returnsJson.decodeFromString<ReturnListResponse>(responseText)
                allReturns.addAll(body.data.map { it.toReturnRequest() })
                lastPage = body.meta?.lastPage ?: page
                page++
            } while (page <= lastPage)

            Result.success(allReturns)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load returns."))
        }
    }

    override suspend fun getReturn(id: Int): Result<ReturnRequest> {
        return try {
            val response = client.get("$baseUrl/workspace/returns/$id") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = returnsJson.decodeFromString<ReturnResponse>(responseText)
            Result.success(body.data.toReturnRequest())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load return."))
        }
    }

    override suspend fun updateReturn(
        id: Int,
        status: ReturnStatus,
        notes: String?,
    ): Result<ReturnRequest> {
        return try {
            val response = client.patch("$baseUrl/workspace/returns/$id") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(UpdateReturnRequest(status = status.value, notes = notes?.ifBlank { null }))
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = returnsJson.decodeFromString<ReturnResponse>(responseText)
            Result.success(body.data.toReturnRequest())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not update return."))
        }
    }

    private fun HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            returnsJson.decodeFromString<ApiErrorResponse>(responseText).message ?: "Request failed."
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class ReturnListResponse(
    val data: List<ReturnRequestDto>,
    val meta: PaginationMeta? = null,
)

@Serializable
private data class ReturnResponse(
    val data: ReturnRequestDto,
)

@Serializable
private data class PaginationMeta(
    @SerialName("current_page")
    val currentPage: Int,

    @SerialName("last_page")
    val lastPage: Int,

    @SerialName("per_page")
    val perPage: Int,

    val total: Int,
)

@Serializable
private data class ReturnRequestDto(
    val id: Int,

    @SerialName("order_id")
    val orderId: Int? = null,

    @SerialName("customer_id")
    val customerId: Int? = null,

    @SerialName("shop_domain")
    val shopDomain: String? = null,

    @SerialName("shopify_customer_id")
    val shopifyCustomerId: String? = null,

    @SerialName("shopify_order_id")
    val shopifyOrderId: String? = null,

    @SerialName("shopify_order_name")
    val shopifyOrderName: String? = null,

    val email: String? = null,
    val items: JsonElement? = null,
    val reason: String? = null,
    val notes: String? = null,
    val status: String? = null,

    @SerialName("refund_amount")
    val refundAmount: JsonElement? = null,

    @SerialName("received_at")
    val receivedAt: String? = null,

    @SerialName("refunded_at")
    val refundedAt: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("return_items")
    val returnItems: List<ReturnItemDto>? = null,
) {
    fun toReturnRequest(): ReturnRequest {
        val displayItems = returnItems
            ?.map { it.toDisplayItem() }
            ?.ifEmpty { null }
            ?: items.toDisplayItems()

        return ReturnRequest(
            id = id,
            orderId = orderId,
            customerId = customerId,
            shopDomain = shopDomain,
            shopifyCustomerId = shopifyCustomerId,
            shopifyOrderId = shopifyOrderId,
            shopifyOrderName = shopifyOrderName,
            email = email,
            items = displayItems,
            reason = reason.orEmpty(),
            notes = notes,
            status = ReturnStatus.fromValue(status),
            refundAmount = refundAmount.asDouble(defaultValue = Double.NaN).takeUnless { it.isNaN() },
            receivedAt = receivedAt,
            refundedAt = refundedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

@Serializable
private data class ReturnItemDto(
    val id: Int? = null,

    @SerialName("return_request_id")
    val returnRequestId: Int? = null,

    @SerialName("order_item_id")
    val orderItemId: Int? = null,

    val quantity: Int? = null,
) {
    fun toDisplayItem(): ReturnDisplayItem {
        return ReturnDisplayItem(
            title = orderItemId?.let { "Order item #$it" } ?: id?.let { "Return item #$it" } ?: "Return item",
            quantity = quantity,
            details = null,
        )
    }
}

@Serializable
private data class UpdateReturnRequest(
    val status: String,
    val notes: String? = null,
)

@Serializable
private data class ApiErrorResponse(
    val message: String? = null,
)

private fun JsonElement?.toDisplayItems(): List<ReturnDisplayItem> {
    return when (this) {
        is JsonArray -> mapIndexed { index, element -> element.toDisplayItem(index + 1) }
        is JsonObject -> listOf(toDisplayItem(1))
        else -> emptyList()
    }
}

private fun JsonElement.toDisplayItem(index: Int): ReturnDisplayItem {
    val jsonObject = this as? JsonObject
    val title = jsonObject?.stringValue("title")
        ?: jsonObject?.stringValue("name")
        ?: jsonObject?.stringValue("product_title")
        ?: jsonObject?.stringValue("variant_title")
        ?: jsonObject?.stringValue("sku")
        ?: "Item $index"

    val quantity = jsonObject?.get("quantity").asInt(defaultValue = Int.MIN_VALUE).takeUnless { it == Int.MIN_VALUE }
    val details = jsonObject
        ?.entries
        ?.mapNotNull { entry ->
            val primitive = entry.value as? JsonPrimitive
            val content = primitive?.contentOrNull
            if (content.isNullOrBlank() || entry.key == "title" || entry.key == "name" || entry.key == "quantity") {
                null
            } else {
                "${entry.key.replace("_", " ")}: $content"
            }
        }
        ?.take(3)
        ?.joinToString(", ")

    return ReturnDisplayItem(
        title = title,
        quantity = quantity,
        details = details,
    )
}

private fun JsonObject.stringValue(key: String): String? {
    return get(key)?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
}

private val returnsJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
