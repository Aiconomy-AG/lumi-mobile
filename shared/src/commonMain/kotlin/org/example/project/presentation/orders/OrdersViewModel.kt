package org.example.project.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.orders.OrdersApi

class OrdersViewModel(
    private val repository: OrdersApi
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.getOrders()

            result
                .onSuccess { orders ->
                    _state.value = _state.value.copy(
                        orders = orders,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load orders."
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query
        )
    }
}
