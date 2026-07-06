package org.example.project.domain.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariant(
    val id: Int,
    val productId: Int,
    val sku: String,
    val price: Double,
    val weight: Double,
    val weightUnit: String,
    val stockQuantity: Int,
)
