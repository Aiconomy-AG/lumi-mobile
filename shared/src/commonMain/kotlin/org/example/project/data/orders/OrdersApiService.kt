package org.example.project.data.orders

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.orders.Order
import org.example.project.domain.orders.OrderDto
import org.example.project.domain.orders.OrdersApi

class OrdersApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String
) : OrdersApi {

    override suspend fun getOrders(): Result<List<Order>> {
        return try {
            val allOrders = mutableListOf<Order>()

            var page = 1
            var lastPage = 1

            do {
                val response = client.get("$baseUrl/admin/orders") {
                    bearerAuth()
                    parameter("page", page)
                }

                val responseText = response.bodyAsText()

                if (response.status != HttpStatusCode.OK) {
                    return Result.failure(Exception(parseErrorMessage(responseText)))
                }

                val body = ordersJson.decodeFromString<OrderListResponse>(responseText)

                allOrders.addAll(
                    body.data.map { orderDto ->
                        orderDto.toOrder()
                    }
                )

                lastPage = body.meta?.lastPage ?: page
                page++

            } while (page <= lastPage)

            Result.success(allOrders)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load orders."))
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            ordersJson.decodeFromString<ApiErrorResponse>(responseText).message ?: "Request failed."
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class OrderListResponse(
    val data: List<OrderDto>,
    val meta: PaginationMeta? = null
)

@Serializable
private data class PaginationMeta(
    @SerialName("current_page")
    val currentPage: Int,

    @SerialName("last_page")
    val lastPage: Int,

    @SerialName("per_page")
    val perPage: Int,

    val total: Int
)

@Serializable
private data class ApiErrorResponse(
    val message: String? = null
)

private val ordersJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
