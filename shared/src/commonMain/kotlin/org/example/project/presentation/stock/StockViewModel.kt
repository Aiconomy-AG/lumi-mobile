package org.example.project.presentation.stock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.stock.StockApiService
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant

class StockViewModel (private val repository: StockApiService) {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = repository.getProducts()

            result
                .onSuccess { products ->
                    _state.value = _state.value.copy(
                        products = products,
                        isLoading = false,

                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                    )
                }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()

            result
                .onSuccess { categories ->
                    _state.value = _state.value.copy(
                        categories = categories
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        errorMessage = exception.message ?: "Could not load categories."
                    )
                }
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
        stockQuantity: Int,
        categoryId: Int
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price,
                description = description.ifBlank { null },
                image_url = imageUrl.ifBlank { null },
                sku = sku,
                stock_quantity = stockQuantity,
                category_id = categoryId,
                variants = null
            )

            val result = repository.addProduct(product)

            result
                .onSuccess {
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        errorMessage = exception.message ?: "Could not create product."
                    )
                }
        }
    }

    fun addProductVariant(
        productId: Int,
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) {
        viewModelScope.launch {
            val result = repository.addProductVariant(
                ProductVariant(
                    product_id = productId,
                    sku = sku,
                    name = name.ifBlank { null },
                    colour = colour.ifBlank { null },
                    weight = weight,
                    weight_unit = weightUnit.ifBlank { null },
                    price = price,
                    stock_quantity = stockQuantity
                )
            )

            result
                .onSuccess {
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        errorMessage = exception.message ?: "Could not create variant."
                    )
                }
        }
    }
}