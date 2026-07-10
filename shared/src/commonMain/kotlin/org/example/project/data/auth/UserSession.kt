package org.example.project.data.auth

import kotlinx.serialization.Serializable
import org.example.project.domain.auth.UserRole

@Serializable
data class UserSession(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole,
    val phoneNumber: String,
    val status: String,
    val languageFlag: String,
    val token: String
)
