package org.example.project.presentation.stock

import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product

data class StockState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredProducts: List<Product>
        get() {
            val query = searchQuery.trim()

            if (query.isBlank()) {
                return products
            }

            return products.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.sku?.contains(query, ignoreCase = true) == true ||
                        product.categoryName?.contains(query, ignoreCase = true) == true ||
                        product.variants.any { variant ->
                            variant.sku.contains(query, ignoreCase = true) ||
                                    variant.name?.contains(query, ignoreCase = true) == true ||
                                    variant.colour?.contains(query, ignoreCase = true) == true
                        }
            }
        }

    val productCount: Int
        get() = products.size

    val variantCount: Int
        get() = products.sumOf { product ->
            product.variants.size
        }

    val lowStockCount: Int
        get() = products.sumOf { product ->
            product.variants.count { variant ->
                variant.stockQuantity in 1..5
            }
        }

    val outOfStockCount: Int
        get() = products.sumOf { product ->
            product.variants.count { variant ->
                variant.stockQuantity == 0
            }
        }
}