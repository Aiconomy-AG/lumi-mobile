package org.example.project.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.auth.UserRole

class AuthApiService(
    private val client: HttpClient,
    private val baseUrl: String
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val response = client.post("$baseUrl/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email = email, password = password))
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created) {
                return Result.failure(
                    Exception(parseErrorMessage(responseText))
                )
            }

            val loginResponse = authJson.decodeFromString<LoginResponse>(responseText)

            Result.success(
                UserSession(
                    id = loginResponse.user.id,
                    name = loginResponse.user.name,
                    email = loginResponse.user.email,
                    role = loginResponse.user.role.toUserRole(),
                    phoneNumber = loginResponse.user.resolvedPhoneNumber(),
                    status = loginResponse.user.status ?: "",
                    languageFlag = loginResponse.user.languageFlag ?: "en",
                    token = loginResponse.token
                )
            )
        } catch (exception: Exception) {
            Result.failure(
                Exception(exception.message ?: "Could not connect to backend.")
            )
        }
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            authJson.decodeFromString<AuthErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Login failed."
        }
    }
}

@Serializable
private data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
private data class LoginResponse(
    val token: String,
    val user: AuthUserResponse
)

@Serializable
private data class AuthUserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("phoneNumber")
    val phoneNumberCamel: String? = null,
    val phone: String? = null,
    val mobile: String? = null,
    val telephone: String? = null,
    @SerialName("language_flag")
    val languageFlag: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)

private fun AuthUserResponse.resolvedPhoneNumber(): String =
    listOf(phoneNumber, phoneNumberCamel, phone, mobile, telephone)
        .firstOrNull { !it.isNullOrBlank() }
        ?: ""

@Serializable
private data class AuthErrorResponse(
    val message: String = "Login failed."
)

private fun String.toUserRole(): UserRole {
    return when (lowercase()) {
        "admin" -> UserRole.ADMIN
        else -> UserRole.EMPLOYEE
    }
}

private val authJson = Json {
    ignoreUnknownKeys = true
}
