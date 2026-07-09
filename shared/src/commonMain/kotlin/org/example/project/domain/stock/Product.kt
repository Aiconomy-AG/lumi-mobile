package org.example.project.domain.stock

import asDouble
import asInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String?,
    val imageUrl: String?,
    val sku: String?,
    val stockQuantity: Int,
    val categoryId: Int?,
    val categoryName: String?,
    val variants: List<ProductVariant> = emptyList()
)

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val price: JsonElement? = null,
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val sku: String? = null,

    @SerialName("stock_quantity")
    val stockQuantity: JsonElement? = null,

    @SerialName("category_id")
    val categoryId: Int? = null,

    @SerialName("category_name")
    val categoryName: String? = null,

    val category: CategoryDto? = null,

    val variants: List<ProductVariantDto>? = null
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            price = price.asDouble(),
            description = description,
            imageUrl = imageUrl,
            sku = sku,
            stockQuantity = stockQuantity.asInt(),
            categoryId = categoryId,
            categoryName = categoryName ?: category?.name,
            variants = variants.orEmpty().map { variantDto ->
                variantDto.toProductVariant(fallbackProductId = id)
            }
        )
    }
}