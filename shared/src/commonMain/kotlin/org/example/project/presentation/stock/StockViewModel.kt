package org.example.project.presentation.stock

import feature.stock.data.MockStockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant

class StockViewModel (private val repository: MockStockRepository) {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val products = repository.getProducts()

            _state.value = _state.value.copy(
                products = products,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            loadProducts()
        }
    }

    fun updateStockQuantity(
        productId: Int,
        variantId: Int,
        newQuantity: Int
    ) {
        viewModelScope.launch {
            repository.updateStockQuantity(
                productId = productId,
                variantId = variantId,
                newQuantity = newQuantity
            )
            loadProducts()
        }
    }

    fun addProduct(
        name: String,
        description: String,
        imageUrl: String,
        sku: String,
        price: Double,
        weight: Double,
        weightUnit: String,
        stockQuantity: Int
    ) {
        viewModelScope.launch {
            val currentProducts = repository.getProducts()

            val newProductId = (currentProducts.maxOfOrNull { it.id } ?: 0) + 1

            val product = Product(
                id = newProductId,
                name = name,
                description = description,
                imageUrl = imageUrl,
                variants = listOf(
                    ProductVariant(
                        id = newProductId,
                        productId = newProductId,
                        sku = sku,
                        price = price,
                        weight = weight,
                        weightUnit = weightUnit,
                        stockQuantity = stockQuantity
                    )
                )
            )

            repository.addProduct(product)
            loadProducts()
        }
    }
}