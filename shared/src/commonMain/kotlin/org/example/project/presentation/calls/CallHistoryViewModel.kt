package org.example.project.presentation.calls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.WorkspaceCall

data class CallHistoryUiState(
    val calls: List<WorkspaceCall> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 0,
    val lastPage: Int = 1,
    val error: String? = null,
)

class CallHistoryViewModel(
    private val api: CallApi,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(CallHistoryUiState())
    val state: StateFlow<CallHistoryUiState> = _state.asStateFlow()

    init {
        loadInitial()
    }

    fun loadInitial() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val page = api.history(page = 1, perPage = 20)
                _state.value = CallHistoryUiState(
                    calls = page.data,
                    currentPage = page.meta.currentPage,
                    lastPage = page.meta.lastPage,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load call history.",
                )
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || current.isLoading || current.currentPage >= current.lastPage) return
        scope.launch {
            _state.value = current.copy(isLoadingMore = true, error = null)
            try {
                val nextPage = current.currentPage + 1
                val page = api.history(page = nextPage, perPage = 20)
                _state.value = current.copy(
                    calls = current.calls + page.data,
                    currentPage = page.meta.currentPage,
                    lastPage = page.meta.lastPage,
                    isLoadingMore = false,
                )
            } catch (e: Exception) {
                _state.value = current.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Could not load more calls.",
                )
            }
        }
    }

    fun close() = scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
}
