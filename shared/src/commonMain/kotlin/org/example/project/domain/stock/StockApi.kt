package org.example.project.domain.stock

interface StockApi {
    suspend fun getProducts(): Result<List<Product>>

    suspend fun getCategories(): Result<List<Category>>

    suspend fun addProduct(
        input: CreateProductInput
    ): Result<Product>

    suspend fun updateProduct(
        productId: Int,
        input: UpdateProductInput
    ): Result<Product>

    suspend fun deleteProduct(
        productId: Int
    ): Result<Unit>

    suspend fun addProductVariant(
        productId: Int,
        input: CreateProductVariantInput
    ): Result<Product>

    suspend fun updateProductVariant(
        productId: Int,
        variantId: Int,
        input: UpdateProductVariantInput
    ): Result<Product>

    suspend fun deleteProductVariant(
        productId: Int,
        variantId: Int
    ): Result<Unit>
}