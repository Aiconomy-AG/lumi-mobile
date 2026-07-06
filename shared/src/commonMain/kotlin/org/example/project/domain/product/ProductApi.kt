package org.example.project.domain.product

interface ProductApi {
    suspend fun getProducts(): List<Product>
    suspend fun getProductVariants(): List<ProductVariant>
}
