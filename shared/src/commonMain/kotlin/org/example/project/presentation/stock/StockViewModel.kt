package org.example.project.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.stock.CreateProductInput
import org.example.project.domain.stock.CreateProductVariantInput
import org.example.project.domain.stock.StockApi
import org.example.project.domain.stock.UpdateProductInput
import org.example.project.domain.stock.UpdateProductVariantInput

class StockViewModel(
    private val repository: StockApi
) : ViewModel() {

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.getProducts()

            result
                .onSuccess { products ->
                    _state.value = _state.value.copy(
                        products = products,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load products."
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
        _state.value = _state.value.copy(
            searchQuery = query
        )
    }

    fun addProduct(
        name: String,
        description: String,
        imageUrl: String,
        sku: String,
        price: Double,
        stockQuantity: Int,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val input = CreateProductInput(
                name = name,
                price = price,
                description = description.ifBlank { null },
                imageUrl = imageUrl.ifBlank { null },
                sku = sku.ifBlank { null },
                stockQuantity = stockQuantity,
                categoryId = categoryId
            )

            val result = repository.addProduct(input)

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not create product."
                    )
                }
        }
    }

    fun updateProduct(
        productId: Int,
        name: String,
        description: String,
        imageUrl: String,
        sku: String,
        price: Double,
        stockQuantity: Int,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val input = UpdateProductInput(
                name = name,
                price = price,
                description = description.ifBlank { null },
                imageUrl = imageUrl.ifBlank { null },
                sku = sku.ifBlank { null },
                stockQuantity = stockQuantity,
                categoryId = categoryId
            )

            val result = repository.updateProduct(
                productId = productId,
                input = input
            )

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not update product."
                    )
                }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val result = repository.deleteProduct(productId)

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not delete product."
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
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val input = CreateProductVariantInput(
                sku = sku,
                name = name.ifBlank { null },
                price = price,
                weight = weight,
                weightUnit = weightUnit.ifBlank { null },
                colour = colour.ifBlank { null },
                stockQuantity = stockQuantity
            )

            val result = repository.addProductVariant(
                productId = productId,
                input = input
            )

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not create variant."
                    )
                }
        }
    }

    fun updateProductVariant(
        productId: Int,
        variantId: Int,
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val input = UpdateProductVariantInput(
                sku = sku,
                name = name.ifBlank { null },
                price = price,
                weight = weight,
                weightUnit = weightUnit.ifBlank { null },
                colour = colour.ifBlank { null },
                stockQuantity = stockQuantity
            )

            val result = repository.updateProductVariant(
                productId = productId,
                variantId = variantId,
                input = input
            )

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not update variant."
                    )
                }
        }
    }

    fun deleteProductVariant(
        productId: Int,
        variantId: Int
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val result = repository.deleteProductVariant(
                productId = productId,
                variantId = variantId
            )

            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false
                    )
                    loadProducts()
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Could not delete variant."
                    )
                }
        }
    }
}