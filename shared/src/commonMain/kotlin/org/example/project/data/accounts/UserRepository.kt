package org.example.project.data.accounts

import org.example.project.domain.accounts.AccountRole

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>

    suspend fun addUser(
        email: String,
        role: AccountRole,
    ): Result<User>

    suspend fun setUserActive(userId: Int, isActive: Boolean): Result<User>
}
