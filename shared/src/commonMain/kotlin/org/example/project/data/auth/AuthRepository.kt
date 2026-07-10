package org.example.project.data.auth

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun validateSession(session: UserSession): Result<UserSession>
}