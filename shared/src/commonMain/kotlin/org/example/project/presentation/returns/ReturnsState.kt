package org.example.project.presentation.returns

import org.example.project.domain.returns.ReturnRequest
import org.example.project.domain.returns.ReturnStatus

data class ReturnsState(
    val returns: List<ReturnRequest> = emptyList(),
    val selectedReturn: ReturnRequest? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val filteredReturns: List<ReturnRequest>
        get() {
            val query = searchQuery.trim()

            if (query.isBlank()) {
                return returns
            }

            return returns.filter { returnRequest ->
                returnRequest.id.toString().contains(query, ignoreCase = true) ||
                        returnRequest.orderId?.toString()?.contains(query, ignoreCase = true) == true ||
                        returnRequest.shopifyOrderId?.contains(query, ignoreCase = true) == true ||
                        returnRequest.shopifyOrderName?.contains(query, ignoreCase = true) == true ||
                        returnRequest.email?.contains(query, ignoreCase = true) == true ||
                        returnRequest.reason.contains(query, ignoreCase = true) ||
                        returnRequest.status.value.contains(query, ignoreCase = true)
            }
        }

    val requestedCount: Int
        get() = returns.count { it.status == ReturnStatus.REQUESTED }

    val activeCount: Int
        get() = returns.count {
            it.status == ReturnStatus.REQUESTED ||
                    it.status == ReturnStatus.APPROVED ||
                    it.status == ReturnStatus.RECEIVED
        }
}
