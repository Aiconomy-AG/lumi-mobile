package org.example.project.data.product

import kotlinx.coroutines.delay
import org.example.project.domain.product.Product
import org.example.project.domain.product.ProductApi
import org.example.project.domain.product.ProductVariant

class ProductMockApiService : ProductApi {

    override suspend fun getProducts(): List<Product> {
        delay(500)
        return listOf(
            Product(id = 1, name = "Ceai verde bio", description = "Ceai verde organic, 50g", price = 24.99, imageUrl = "https://example.com/ceai-verde.jpg"),
            Product(id = 2, name = "Miere de salcâm", description = "Miere naturală, 500g", price = 32.5, imageUrl = "https://example.com/miere.jpg"),
            Product(id = 3, name = "Ulei de măsline extravirgin", description = "Presat la rece, 750ml", price = 45.0, imageUrl = "https://example.com/ulei-masline.jpg"),
            Product(id = 4, name = "Făină de migdale", description = "Făină fără gluten, 400g", price = 28.0, imageUrl = "https://example.com/faina-migdale.jpg"),
            Product(id = 5, name = "Cafea boabe origine", description = "Prăjită artizanal, 250g", price = 39.9, imageUrl = "https://example.com/cafea-boabe.jpg"),
            Product(id = 6, name = "Nuci mixte", description = "Amestec de nuci crude, 300g", price = 22.5, imageUrl = "https://example.com/nuci-mixte.jpg"),
        )
    }

    override suspend fun getProductVariants(): List<ProductVariant> {
        delay(500)
        return listOf(
            ProductVariant(id = 1, productId = 1, sku = "CEAI-VERDE-50G", price = 24.99, weight = 50.0, weightUnit = "g", stockQuantity = 120),
            ProductVariant(id = 2, productId = 2, sku = "MIERE-SALCAM-500G", price = 32.5, weight = 500.0, weightUnit = "g", stockQuantity = 80),
            ProductVariant(id = 3, productId = 3, sku = "ULEI-MASLINE-750ML", price = 45.0, weight = 750.0, weightUnit = "ml", stockQuantity = 60),
            ProductVariant(id = 4, productId = 4, sku = "FAINA-MIGDALE-400G", price = 28.0, weight = 400.0, weightUnit = "g", stockQuantity = 45),
            ProductVariant(id = 5, productId = 5, sku = "CAFEA-BOABE-250G", price = 39.9, weight = 250.0, weightUnit = "g", stockQuantity = 100),
            ProductVariant(id = 6, productId = 6, sku = "NUCI-MIXTE-300G", price = 22.5, weight = 300.0, weightUnit = "g", stockQuantity = 70),
        )
    }
}
