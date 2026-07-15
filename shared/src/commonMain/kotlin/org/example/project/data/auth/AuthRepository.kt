package org.example.project.data.auth

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun validateSession(session: UserSession): Result<UserSession>
    suspend fun updatePhoneNumber(token: String, phoneNumber: String): Result<String>
    suspend fun updateStatus(session: UserSession, status: String): Result<UserSession>
    suspend fun updateProfilePhoto(session: UserSession, bytes: ByteArray, fileName: String, mimeType: String): Result<UserSession>
}
