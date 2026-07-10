package org.example.project.domain.auth

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    EMPLOYEE
}