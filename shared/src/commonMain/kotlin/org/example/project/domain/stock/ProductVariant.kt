package org.example.project.domain.stock

import asDouble
import asInt
import asNullableDouble
import asStringMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data class ProductVariant(
    val id: Int,
    val productId: Int,
    val sku: String,
    val name: String?,
    val price: Double,
    val weight: Double?,
    val weightUnit: String?,
    val colour: String?,
    val stockQuantity: Int,
    val options: Map<String, String>?
)

@Serializable
data class ProductVariantDto(
    val id: Int,

    @SerialName("product_id")
    val productId: Int? = null,

    val sku: String,

    val name: String? = null,

    val price: JsonElement? = null,

    val weight: JsonElement? = null,

    @SerialName("weight_unit")
    val weightUnit: String? = null,

    val colour: String? = null,

    @SerialName("stock_quantity")
    val stockQuantity: JsonElement? = null,

    val options: JsonElement? = null
) {
    fun toProductVariant(fallbackProductId: Int): ProductVariant {
        return ProductVariant(
            id = id,
            productId = productId ?: fallbackProductId,
            sku = sku,
            name = name,
            price = price.asDouble(),
            weight = weight.asNullableDouble(),
            weightUnit = weightUnit,
            colour = colour,
            stockQuantity = stockQuantity.asInt(),
            options = options.asStringMap()
        )
    }
}