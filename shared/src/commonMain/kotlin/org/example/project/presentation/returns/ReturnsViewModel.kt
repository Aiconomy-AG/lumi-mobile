package org.example.project.presentation.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.returns.ReturnStatus
import org.example.project.domain.returns.ReturnsApi

class ReturnsViewModel(
    private val api: ReturnsApi,
) : ViewModel() {

    private val _state = MutableStateFlow(ReturnsState())
    val state: StateFlow<ReturnsState> = _state.asStateFlow()

    init {
        loadReturns()
    }

    fun loadReturns() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
            )

            api.getReturns()
                .onSuccess { returns ->
                    _state.value = _state.value.copy(
                        returns = returns,
                        isLoading = false,
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load returns.",
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun openReturn(returnId: Int) {
        viewModelScope.launch {
            val currentReturn = _state.value.returns.firstOrNull { it.id == returnId }
            _state.value = _state.value.copy(
                selectedReturn = currentReturn,
                isLoading = currentReturn == null,
                errorMessage = null,
                dialogErrorMessage = null,
            )

            api.getReturn(returnId)
                .onSuccess { returnRequest ->
                    _state.value = _state.value.copy(
                        selectedReturn = returnRequest,
                        returns = _state.value.returns.map { existing ->
                            if (existing.id == returnRequest.id) returnRequest else existing
                        },
                        isLoading = false,
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load return.",
                    )
                }
        }
    }

    fun closeReturn() {
        _state.value = _state.value.copy(
            selectedReturn = null,
            dialogErrorMessage = null,
        )
    }

    fun updateReturn(
        returnId: Int,
        status: ReturnStatus,
        notes: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                dialogErrorMessage = null,
            )

            api.updateReturn(
                id = returnId,
                status = status,
                notes = notes.ifBlank { null },
            )
                .onSuccess { updatedReturn ->
                    _state.value = _state.value.copy(
                        returns = _state.value.returns.map { existing ->
                            if (existing.id == updatedReturn.id) updatedReturn else existing
                        },
                        selectedReturn = null,
                        isSaving = false,
                        dialogErrorMessage = null,
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        dialogErrorMessage = exception.message ?: "Could not update return.",
                    )
                }
        }
    }
}
