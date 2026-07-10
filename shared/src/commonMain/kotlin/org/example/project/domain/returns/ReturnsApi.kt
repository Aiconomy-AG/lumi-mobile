package org.example.project.domain.returns

interface ReturnsApi {
    suspend fun getReturns(): Result<List<ReturnRequest>>

    suspend fun getReturn(id: Int): Result<ReturnRequest>

    suspend fun updateReturn(
        id: Int,
        status: ReturnStatus,
        notes: String?,
    ): Result<ReturnRequest>
}
