package org.example.project.domain.stock

data class Product(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val variants: List<ProductVariant>
)