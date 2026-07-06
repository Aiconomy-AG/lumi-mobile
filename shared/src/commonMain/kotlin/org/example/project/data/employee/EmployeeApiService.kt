package org.example.project.data.employee

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.project.domain.employee.Employee
import org.example.project.domain.employee.EmployeeApi

class EmployeeApiService(
    private val client: HttpClient,
    private val baseUrl: String,
) : EmployeeApi {

    override suspend fun getEmployees(): List<Employee> =
        client.get("$baseUrl/employees").body()
}
