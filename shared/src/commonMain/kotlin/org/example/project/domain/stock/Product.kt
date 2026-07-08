package org.example.project.domain.stock

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val description: String? = null,
    val image_url: String? = null,
    val sku: String? = null,
    val stock_quantity: Int,
    val category_id: Int?=null,
    val variants: List<ProductVariant>? = null
)