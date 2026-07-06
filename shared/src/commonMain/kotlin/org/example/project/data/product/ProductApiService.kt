package org.example.project.data.product

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.project.domain.product.Product
import org.example.project.domain.product.ProductApi
import org.example.project.domain.product.ProductVariant

class ProductApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : ProductApi {

    override suspend fun getProducts(): List<Product> =
        client.get("$baseUrl/products").body()

    override suspend fun getProductVariants(): List<ProductVariant> =
        client.get("$baseUrl/product-variants").body()
}
