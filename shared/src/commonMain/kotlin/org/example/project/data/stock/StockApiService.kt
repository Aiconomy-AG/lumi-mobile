package org.example.project.data.stock

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.CategoryDto
import org.example.project.domain.stock.CreateProductInput
import org.example.project.domain.stock.CreateProductVariantInput
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductDto
import org.example.project.domain.stock.ProductVariant
import org.example.project.domain.stock.StockApi
import org.example.project.domain.stock.UpdateProductInput
import org.example.project.domain.stock.UpdateProductVariantInput

class StockApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String
) : StockApi {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val allProducts = mutableListOf<Product>()

            var page = 1
            var lastPage = 1

            do {
                val response = client.get("$baseUrl/admin/products") {
                    bearerAuth()
                    parameter("page", page)
                    parameter("per_page", 100)
                }

                val responseText = response.bodyAsText()

                if (response.status != HttpStatusCode.OK) {
                    return Result.failure(Exception(parseErrorMessage(responseText)))
                }

                val body = stockJson.decodeFromString<ProductListResponse>(responseText)

                allProducts.addAll(
                    body.data.map { productDto ->
                        productDto.toProduct()
                    }
                )

                lastPage = body.meta?.lastPage ?: page
                page++

            } while (page <= lastPage)

            Result.success(allProducts)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load products."))
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = client.get("$baseUrl/shop/categories") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val categories = try {
                stockJson.decodeFromString<CategoryListResponse>(responseText)
                    .data
                    .map { it.toCategory() }
            } catch (exception: Exception) {
                stockJson.decodeFromString<List<CategoryDto>>(responseText)
                    .map { it.toCategory() }
            }

            Result.success(categories)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load categories."))
        }
    }

    override suspend fun addProduct(
        input: CreateProductInput
    ): Result<Product> {
        return try {
            val response = client.post("$baseUrl/admin/products") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(input.toCreateProductRequest())
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not create product."))
        }
    }

    override suspend fun updateProduct(
        productId: Int,
        input: UpdateProductInput
    ): Result<Product> {
        return try {
            val response = client.put("$baseUrl/admin/products/$productId") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(input.toUpdateProductRequest())
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not update product."))
        }
    }

    override suspend fun deleteProduct(
        productId: Int
    ): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/products/$productId") {
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

    override suspend fun addProductVariant(
        productId: Int,
        input: CreateProductVariantInput
    ): Result<Product> {
        return try {
            val response = client.post("$baseUrl/admin/products/$productId/variants") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(input.toVariantRequest())
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not create product variant."))
        }
    }

    override suspend fun updateProductVariant(
        productId: Int,
        variantId: Int,
        input: UpdateProductVariantInput
    ): Result<Product> {
        return try {
            val response = client.put("$baseUrl/admin/products/$productId/variants/$variantId") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(input.toVariantRequest())
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = stockJson.decodeFromString<ProductResponse>(responseText)

            Result.success(body.data.toProduct())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not update product variant."))
        }
    }

    override suspend fun deleteProductVariant(
        productId: Int,
        variantId: Int
    ): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/products/$productId/variants/$variantId") {
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

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            stockJson.decodeFromString<ApiErrorResponse>(responseText).message ?: "Request failed."
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class ProductListResponse(
    val data: List<ProductDto>,
    val meta: PaginationMeta? = null
)

@Serializable
private data class ProductResponse(
    val data: ProductDto
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
private data class CategoryListResponse(
    val data: List<CategoryDto>
)

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
    val categoryId: Int? = null
)

@Serializable
private data class UpdateProductRequest(
    val name: String,
    val price: Double,
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val sku: String? = null,

    @SerialName("stock_quantity")
    val stockQuantity: Int,

    @SerialName("category_id")
    val categoryId: Int? = null
)

@Serializable
private data class ProductVariantRequest(
    val sku: String,
    val name: String? = null,
    val price: Double,
    val weight: Double? = null,

    @SerialName("weight_unit")
    val weightUnit: String? = null,

    val colour: String? = null,

    @SerialName("stock_quantity")
    val stockQuantity: Int,

    val options: Map<String, String>? = null
)

@Serializable
private data class ApiErrorResponse(
    val message: String? = null
)

private fun CreateProductInput.toCreateProductRequest(): CreateProductRequest {
    return CreateProductRequest(
        name = name,
        price = price,
        description = description,
        imageUrl = imageUrl,
        sku = sku,
        stockQuantity = stockQuantity,
        categoryId = categoryId
    )
}

private fun UpdateProductInput.toUpdateProductRequest(): UpdateProductRequest {
    return UpdateProductRequest(
        name = name,
        price = price,
        description = description,
        imageUrl = imageUrl,
        sku = sku,
        stockQuantity = stockQuantity,
        categoryId = categoryId
    )
}

private fun CreateProductVariantInput.toVariantRequest(): ProductVariantRequest {
    return ProductVariantRequest(
        sku = sku,
        name = name,
        price = price,
        weight = weight,
        weightUnit = weightUnit,
        colour = colour,
        stockQuantity = stockQuantity,
        options = options
    )
}

private fun UpdateProductVariantInput.toVariantRequest(): ProductVariantRequest {
    return ProductVariantRequest(
        sku = sku,
        name = name,
        price = price,
        weight = weight,
        weightUnit = weightUnit,
        colour = colour,
        stockQuantity = stockQuantity,
        options = options
    )
}

private val stockJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}