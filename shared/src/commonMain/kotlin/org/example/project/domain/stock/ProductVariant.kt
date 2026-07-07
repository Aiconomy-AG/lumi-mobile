package org.example.project.domain.stock

data class ProductVariant(
    val id: Int,
    val productId: Int,
    val sku: String,
    val price: Double,
    val weight: Double,
    val weightUnit: String,
    val stockQuantity: Int
)