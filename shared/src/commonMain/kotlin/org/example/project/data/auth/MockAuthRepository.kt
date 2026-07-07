package org.example.project.data.auth

import org.example.project.data.accounts.AccountStore
import org.example.project.domain.accounts.AccountRole
import org.example.project.domain.auth.UserRole
import kotlin.Result

class MockAuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession> {
        val user = AccountStore.findByCredentials(email, password)

        return if (user != null) {
            Result.success(
                UserSession(
                    id = user.id,
                    name = user.fullName,
                    email = user.email,
                    role = user.role.toUserRole()
                )
            )
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }
}

private fun AccountRole.toUserRole(): UserRole {
    return when (this) {
        AccountRole.ADMIN -> UserRole.ADMIN
        AccountRole.EMPLOYEE -> UserRole.EMPLOYEE
    }
}
