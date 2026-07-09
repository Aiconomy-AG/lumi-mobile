package org.example.project.data.accounts

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.domain.accounts.AccountRole

class UserApiService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: String
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = client.get("$baseUrl/admin/users") {
                bearerAuth()
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = usersJson.decodeFromString<UserListResponse>(responseText)

            Result.success(body.data.map { it.toUser() })
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not load users."))
        }
    }

    override suspend fun addUser(
        name: String,
        email: String,
        password: String,
        role: AccountRole,
        phoneNumber: String,
        languageFlag: String,
        status: String,
        isActive: Boolean
    ): Result<User> {
        return try {
            val response = client.post("$baseUrl/admin/users") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(
                    CreateUserRequest(
                        name = name,
                        email = email,
                        password = password,
                        role = role.toApiValue(),
                        phoneNumber = phoneNumber,
                        languageFlag = languageFlag,
                        status = status,
                        isActive = isActive
                    )
                )
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.Created) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = usersJson.decodeFromString<UserResponse>(responseText)

            Result.success(body.data.toUser())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not create user."))
        }
    }

    override suspend fun setUserActive(userId: Int, isActive: Boolean): Result<User> {
        return try {
            val response = client.put("$baseUrl/admin/users/$userId") {
                bearerAuth()
                contentType(ContentType.Application.Json)
                setBody(UpdateUserActiveRequest(isActive = isActive))
            }

            val responseText = response.bodyAsText()

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(parseErrorMessage(responseText)))
            }

            val body = usersJson.decodeFromString<UserResponse>(responseText)

            Result.success(body.data.toUser())
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Could not update user."))
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerAuth() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Accept, "application/json")
    }

    private fun parseErrorMessage(responseText: String): String {
        return try {
            usersJson.decodeFromString<ApiErrorResponse>(responseText).message
        } catch (exception: Exception) {
            "Request failed."
        }
    }
}

@Serializable
private data class UserListResponse(
    val data: List<UserDto>
)

@Serializable
private data class UserResponse(
    val data: UserDto
)

@Serializable
private data class UpdateUserActiveRequest(
    @SerialName("is_active")
    val isActive: Boolean
)

@Serializable
private data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("language_flag")
    val languageFlag: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
) {
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            role = role.toAccountRole(),
            status = status ?: "offline",
            phoneNumber = phoneNumber ?: "",
            languageFlag = languageFlag ?: "en",
            isActive = isActive
        )
    }
}

@Serializable
private data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val status: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("language_flag")
    val languageFlag: String,
    @SerialName("is_active")
    val isActive: Boolean
)

@Serializable
private data class ApiErrorResponse(
    val message: String = "Request failed."
)

private fun String.toAccountRole(): AccountRole {
    return when (lowercase()) {
        "admin" -> AccountRole.ADMIN
        else -> AccountRole.EMPLOYEE
    }
}

private fun AccountRole.toApiValue(): String {
    return when (this) {
        AccountRole.ADMIN -> "admin"
        AccountRole.EMPLOYEE -> "employee"
    }
}

private val usersJson = Json {
    ignoreUnknownKeys = true
}
