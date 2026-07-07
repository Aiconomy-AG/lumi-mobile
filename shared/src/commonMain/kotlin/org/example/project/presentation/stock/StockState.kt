package org.example.project.presentation.stock

import org.example.project.domain.stock.Product

data class StockState (
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
) {
    val filteredProducts: List<Product>
        get() {
            if (searchQuery.isBlank())
                return products

            return products.filter { product -> product.name.contains(searchQuery, ignoreCase = true) ||
                product.variants.any {it.sku.contains(searchQuery, ignoreCase = true)}}
        }

    val lowStockCount: Int
        get() = products.count { product ->
            product.variants.any {it.stockQuantity in 1..5 }
        }

    val outOfStockCount: Int
        get() = products.count {product ->
            product.variants.any {it.stockQuantity == 0}
        }
}