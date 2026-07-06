package org.example.project.domain.employee

interface EmployeeApi {
    suspend fun getEmployees(): List<Employee>
}
