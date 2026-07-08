package org.example.project.data.accounts

import org.example.project.domain.accounts.AccountRole

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>

    suspend fun addUser(
        name: String,
        email: String,
        password: String,
        role: AccountRole,
        phoneNumber: String,
        languageFlag: String,
        status: String,
        isActive: Boolean
    ): Result<User>

    suspend fun setUserActive(userId: Int, isActive: Boolean): Result<User>
}
