package org.example.project.data.auth

import org.example.project.domain.auth.UserRole

data class UserSession(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole,
    val token: String
)