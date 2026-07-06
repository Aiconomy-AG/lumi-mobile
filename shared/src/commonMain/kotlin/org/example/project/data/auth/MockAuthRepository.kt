package org.example.project.data.auth

import org.example.project.domain.auth.UserRole
import kotlin.Result

class MockAuthRepository {
    private val users = listOf(
        MockUser(
            id = 1,
            name = "Ana Popescu",
            email = "admin@test.com",
            password = "admin123",
            role = UserRole.ADMIN
        ),
        MockUser(
            id = 2,
            name = "Mihai Ionescu",
            email = "employee@test.com",
            password = "employee123",
            role = UserRole.EMPLOYEE
        )
    )

    suspend fun login(email: String, password: String): Result<UserSession> {
        val user = users.firstOrNull {
            it.email == email && it.password == password
        }

        return if (user != null) {
            Result.success(
                UserSession(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    role = user.role
                )
            )
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }
}

private data class MockUser(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole
)