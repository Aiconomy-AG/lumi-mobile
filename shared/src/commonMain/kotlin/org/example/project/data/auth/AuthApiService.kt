package org.example.project.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
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
            val response = client.post("$baseUrl/auth/login") {
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

    override suspend fun validateSession(session: UserSession): Result<UserSession> {
        return try {
            val meResponse = client.get("$baseUrl/auth/me") {
                bearerAuth(session.token)
            }

            val meResponseText = meResponse.bodyAsText()

            if (meResponse.status == HttpStatusCode.Unauthorized ||
                meResponse.status == HttpStatusCode.Forbidden
            ) {
                return Result.failure(Exception("Session expired."))
            }

            if (meResponse.status.isSuccess()) {
                parseMeResponse(meResponseText, session)?.let { return Result.success(it) }
            }

            validateWithTasksEndpoint(session)
        } catch (exception: Exception) {
            Result.failure(
                Exception(exception.message ?: "Could not validate session.")
            )
        }
    }

    override suspend fun updatePhoneNumber(token: String, phoneNumber: String): Result<String> {
        return try {
            val response = client.put("$baseUrl/auth/phone") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(UpdatePhoneRequest(phoneNumber = phoneNumber))
            }

            val responseText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = authJson.decodeFromString<UpdatePhoneResponse>(responseText)
            Result.success(body.phoneNumber)
        } catch (exception: Exception) {
            Result.failure(
                Exception(exception.message ?: "Could not update phone number.")
            )
        }
    }

    private suspend fun validateWithTasksEndpoint(session: UserSession): Result<UserSession> {
        val response = client.get("$baseUrl/workspace/tasks") {
            bearerAuth(session.token)
        }

        if (response.status == HttpStatusCode.Unauthorized ||
            response.status == HttpStatusCode.Forbidden
        ) {
            return Result.failure(Exception("Session expired."))
        }

        return if (response.status.isSuccess()) {
            Result.success(session)
        } else {
            Result.failure(Exception("Session validation failed."))
        }
    }

    private fun parseMeResponse(responseText: String, session: UserSession): UserSession? {
        return try {
            val wrapped = authJson.decodeFromString<MeWrappedResponse>(responseText)
            wrapped.data?.toUserSession(session.token)
                ?: wrapped.user?.toUserSession(session.token)
        } catch (_: Exception) {
            try {
                authJson.decodeFromString<LoginResponse>(responseText)
                    .let { loginResponse ->
                        UserSession(
                            id = loginResponse.user.id,
                            name = loginResponse.user.name,
                            email = loginResponse.user.email,
                            role = loginResponse.user.role.toUserRole(),
                            phoneNumber = loginResponse.user.resolvedPhoneNumber(),
                            status = loginResponse.user.status ?: "",
                            languageFlag = loginResponse.user.languageFlag ?: "en",
                            token = loginResponse.token.ifBlank { session.token },
                        )
                    }
            } catch (_: Exception) {
                try {
                    authJson.decodeFromString<AuthUserResponse>(responseText)
                        .toUserSession(session.token)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
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
private data class MeWrappedResponse(
    val data: AuthUserResponse? = null,
    val user: AuthUserResponse? = null,
)

private fun AuthUserResponse.toUserSession(token: String): UserSession {
    return UserSession(
        id = id,
        name = name,
        email = email,
        role = role.toUserRole(),
        phoneNumber = resolvedPhoneNumber(),
        status = status ?: "",
        languageFlag = languageFlag ?: "en",
        token = token,
    )
}

@Serializable
private data class UpdatePhoneRequest(
    @SerialName("phone_number")
    val phoneNumber: String,
)

@Serializable
private data class UpdatePhoneResponse(
    val message: String = "",
    @SerialName("phone_number")
    val phoneNumber: String,
)

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
