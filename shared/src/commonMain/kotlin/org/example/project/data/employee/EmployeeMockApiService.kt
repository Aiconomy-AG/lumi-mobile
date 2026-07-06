package org.example.project.data.employee

import kotlinx.coroutines.delay
import org.example.project.domain.employee.Employee
import org.example.project.domain.employee.EmployeeApi
import org.example.project.domain.employee.EmployeeRole

class EmployeeMockApiService : EmployeeApi {

    override suspend fun getEmployees(): List<Employee> {
        delay(500)
        return listOf(
            Employee(id = 1, name = "Ana Pop", email = "ana.pop@lumi.com", phoneNumber = "0722111222", role = EmployeeRole.ADMIN),
            Employee(id = 2, name = "Ion Ionescu", email = "ion.ionescu@lumi.com", phoneNumber = "0733222333", role = EmployeeRole.ADMIN),
            Employee(id = 3, name = "Maria Georgescu", email = "maria.georgescu@lumi.com", phoneNumber = "0744333444", role = EmployeeRole.EMPLOYEE),
            Employee(id = 4, name = "Radu Constantin", email = "radu.constantin@lumi.com", phoneNumber = "0755444555", role = EmployeeRole.EMPLOYEE),
            Employee(id = 5, name = "Elena Dumitrescu", email = "elena.dumitrescu@lumi.com", phoneNumber = "0766555666", role = EmployeeRole.EMPLOYEE),
            Employee(id = 6, name = "Vlad Marinescu", email = "vlad.marinescu@lumi.com", phoneNumber = "0777666777", role = EmployeeRole.EMPLOYEE),
        )
    }
}
