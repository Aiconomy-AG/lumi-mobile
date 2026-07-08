package org.example.project.domain.stock

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariant(
    val id: Int,
    val product_id: Int,
    val sku: String,
    val name: String? = null,
    val price: Double? = null,
    val weight: Double? = null,
    val weight_unit: String? = null,
    val colour: String? = null,
    val stock_quantity: Int? = null,
    val options: List<String>? = null
)