package feature.stock.data

import org.example.project.domain.stock.ProductVariant
import org.example.project.domain.stock.Product

class MockStockRepository {

    private val products = mutableListOf(
        Product(
            id = 1,
            name = "Dell XPS 15 Laptop",
            imageUrl = "",
            description = "Premium laptop for productivity.",
            variants = listOf(
                ProductVariant(
                    id = 1,
                    productId = 1,
                    sku = "DEL-XPS-001",
                    price = 5499.99,
                    weight = 1.8,
                    weightUnit = "kg",
                    stockQuantity = 12
                )
            )
        ),
        Product(
            id = 2,
            name = "LG 27” 4K Monitor",
            imageUrl = "",
            description = "4K monitor for office work.",
            variants = listOf(
                ProductVariant(
                    id = 2,
                    productId = 2,
                    sku = "LG-MON-027",
                    price = 1899.99,
                    weight = 4.5,
                    weightUnit = "kg",
                    stockQuantity = 8
                )
            )
        ),
        Product(
            id = 3,
            name = "Keychron K2 Keyboard",
            imageUrl = "",
            description = "Mechanical keyboard.",
            variants = listOf(
                ProductVariant(
                    id = 3,
                    productId = 3,
                    sku = "KEY-K2-001",
                    price = 449.99,
                    weight = 0.9,
                    weightUnit = "kg",
                    stockQuantity = 25
                )
            )
        ),
        Product(
            id = 4,
            name = "Logitech MX Master 3",
            imageUrl = "",
            description = "Wireless productivity mouse.",
            variants = listOf(
                ProductVariant(
                    id = 4,
                    productId = 4,
                    sku = "LOG-MX3-001",
                    price = 349.99,
                    weight = 0.14,
                    weightUnit = "kg",
                    stockQuantity = 3
                )
            )
        ),
        Product(
            id = 5,
            name = "Logitech C920 Webcam",
            imageUrl = "",
            description = "Full HD webcam.",
            variants = listOf(
                ProductVariant(
                    id = 5,
                    productId = 5,
                    sku = "LOG-C920-001",
                    price = 599.99,
                    weight = 0.2,
                    weightUnit = "kg",
                    stockQuantity = 0
                )
            )
        )
    )

    suspend fun getProducts(): List<Product> {
        return products.toList()
    }

    suspend fun addProduct(product: Product) {
        products.add(product)
    }

    suspend fun deleteProduct(productId: Int) {
        products.removeAll { it.id == productId }
    }

    suspend fun updateStockQuantity(
        productId: Int,
        variantId: Int,
        newQuantity: Int
    ) {
        val productIndex = products.indexOfFirst { it.id == productId }

        if (productIndex == -1) return

        val product = products[productIndex]

        val updatedVariants = product.variants.map { variant ->
            if (variant.id == variantId) {
                variant.copy(stockQuantity = newQuantity)
            } else {
                variant
            }
        }

        products[productIndex] = product.copy(variants = updatedVariants)
    }
}