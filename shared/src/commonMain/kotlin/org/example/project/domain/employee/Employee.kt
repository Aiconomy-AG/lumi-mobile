package org.example.project.domain.employee

import kotlinx.serialization.Serializable

@Serializable
enum class EmployeeRole {
    ADMIN, EMPLOYEE
}

@Serializable
data class Employee(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: EmployeeRole,
)
