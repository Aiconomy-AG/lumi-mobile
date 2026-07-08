package org.example.project.presentation.stock

import org.example.project.domain.stock.Product

data class StockState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
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
                        product.variants?.any { variant ->
                            variant.sku.contains(query, ignoreCase = true)
                        } == true
            }
        }

    val lowStockCount: Int
        get() = products.count { product ->
            val variants = product.variants

            if (!variants.isNullOrEmpty()) {
                variants.any { variant ->
                    val quantity = variant.stock_quantity ?: 0
                    quantity in 1..5
                }
            } else {
                product.stock_quantity in 1..5
            }
        }

    val outOfStockCount: Int
        get() = products.count { product ->
            val variants = product.variants

            if (!variants.isNullOrEmpty()) {
                variants.any { variant ->
                    val quantity = variant.stock_quantity ?: 0
                    quantity == 0
                }
            } else {
                product.stock_quantity == 0
            }
        }
}