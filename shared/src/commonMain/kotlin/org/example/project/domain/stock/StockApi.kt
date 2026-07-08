package org.example.project.domain.stock

import org.example.project.data.stock.Category

interface StockApi {
    suspend fun getProducts(): Result<List<Product>>

    suspend fun updateStockQuantity(
        productId: Int,
        variantId: Int,
        newQuantity: Int
    ): Result<Product>

    suspend fun deleteProductVariant(
        productId: Int,
        variantId: Int
    ): Result<Unit>

    suspend fun deleteProduct(productId: Int): Result<Unit>

    suspend fun addProduct(product: Product): Result<Product>

    suspend fun addProductVariant(productVariant: ProductVariant): Result<Product>

    suspend fun getCategories(): Result<List<Category>>
}