package org.example.project.data.stock

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant
import org.example.project.domain.stock.StockApi

class StockApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String
) : StockApi {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = client.get("$baseUrl/v1/admin/products") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductListResponse>(responseText)

            Result.success(body.data.map { it.toProduct() })
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load products."))
        }
    }

    override suspend fun addProduct(product: Product): Result<Product> {
        return try {
            val response = client.post("$baseUrl/v1/admin/products") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(
                    CreateProductRequest(
                        name = product.name,
                        price = product.price,
                        description = product.description,
                        imageUrl = product.image_url,
                        sku = product.sku,
                        stockQuantity = product.stock_quantity,
                        categoryId = product.category_id,
                        variants = product.variants?.map { it.toCreateVariantRequest() }
                    )
                )
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not create product."))
        }
    }

    override suspend fun addProductVariant(productVariant: ProductVariant): Result<Product> {
        return try {
            val response = client.post("$baseUrl/v1/admin/products/${productVariant.product_id}/variants") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(productVariant.toCreateVariantRequest())
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not create product variant."))
        }
    }

    override suspend fun updateStockQuantity(
        productId: Int,
        variantId: Int,
        newQuantity: Int
    ): Result<Product> {
        return try {
            val response = client.put("$baseUrl/v1/admin/products/$productId/variants/$variantId") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(UpdateProductVariantStockRequest(newStock = newQuantity))
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not update stock quantity."))
        }
    }

    override suspend fun deleteProductVariant(
        productId: Int,
        variantId: Int
    ): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/v1/admin/products/$productId/variants/$variantId") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not delete product variant."))
        }
    }

    override suspend fun deleteProduct(productId: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/v1/admin/products/$productId") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not delete product."))
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            stockJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class ProductListResponse(
    val data: List<ProductDto>
)

@Serializable
private data class ProductResponse(
    val data: ProductDto
)

@Serializable
private data class ProductDto(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val sku: String? = null,
    @SerialName("stock_quantity")
    val stockQuantity: Int = 0,
    @SerialName("category_id")
    val categoryId: Int?=null,
    val variants: List<ProductVariantDto>? = null
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            price = price,
            description = description,
            image_url = imageUrl,
            sku = sku,
            stock_quantity = stockQuantity,
            category_id = categoryId,
            variants = variants?.map { it.toProductVariant() }
        )
    }
}

@Serializable
private data class CreateProductRequest(
    val name: String,
    val price: Double,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val sku: String? = null,
    @SerialName("stock_quantity")
    val stockQuantity: Int,
    @SerialName("category_id")
    val categoryId: Int?=null,
    val variants: List<CreateProductVariantRequest>? = null
)

@Serializable
private data class CreateProductVariantRequest(
    val sku: String,
    val name: String? = null,
    val price: Double? = null,
    val weight: Double? = null,
    @SerialName("weight_unit")
    val weightUnit: String? = null,
    val colour: String? = null,
    @SerialName("stock_quantity")
    val stockQuantity: Int? = null,
    val options: List<String>? = null
)

@Serializable
private data class UpdateProductVariantStockRequest(
    val newStock: Int
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed."
)

private val stockJson = Json {
    ignoreUnknownKeys = true
}

private fun ProductVariant.toCreateVariantRequest(): CreateProductVariantRequest {
    return CreateProductVariantRequest(
        sku = sku,
        name = name,
        price = price,
        weight = weight,
        weightUnit = weight_unit,
        colour = colour,
        stockQuantity = stock_quantity,
        options = options
    )
}

@Serializable
private data class ProductVariantDto(
    val id: Int,

    @SerialName("product_id")
    val productId: Int,

    val sku: String,

    val name: String? = null,

    val price: Double? = null,

    val weight: Double? = null,

    @SerialName("weight_unit")
    val weightUnit: String? = null,

    val colour: String? = null,

    @SerialName("stock_quantity")
    val stockQuantity: Int? = null,

    val options: List<String>? = null
) {
    fun toProductVariant(): ProductVariant {
        return ProductVariant(
            id = id,
            product_id = productId,
            sku = sku,
            name = name,
            price = price,
            weight = weight,
            weight_unit = weightUnit,
            colour = colour,
            stock_quantity = stockQuantity,
            options = options
        )
    }
}