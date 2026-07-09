package org.example.project.domain.stock

import kotlinx.serialization.Serializable

data class Category(
    val id: Int,
    val name: String
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String
) {
    fun toCategory(): Category {
        return Category(
            id = id,
            name = name
        )
    }
}
