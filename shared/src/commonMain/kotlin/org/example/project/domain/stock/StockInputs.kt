package org.example.project.domain.stock

data class CreateProductInput(
    val name: String,
    val price: Double,
    val description: String?,
    val imageUrl: String?,
    val sku: String?,
    val stockQuantity: Int,
    val categoryId: Int?
)

data class UpdateProductInput(
    val name: String,
    val price: Double,
    val description: String?,
    val imageUrl: String?,
    val sku: String?,
    val stockQuantity: Int,
    val categoryId: Int?
)

data class CreateProductVariantInput(
    val sku: String,
    val name: String?,
    val price: Double,
    val weight: Double?,
    val weightUnit: String?,
    val colour: String?,
    val stockQuantity: Int,
    val options: Map<String, String>? = null
)

data class UpdateProductVariantInput(
    val sku: String,
    val name: String?,
    val price: Double,
    val weight: Double?,
    val weightUnit: String?,
    val colour: String?,
    val stockQuantity: Int,
    val options: Map<String, String>? = null
)