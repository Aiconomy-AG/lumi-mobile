package org.example.project.data.accounts

import org.example.project.domain.accounts.AccountRole

object AccountStore {
    private val users = mutableListOf(
        User(
            id = 1,
            fullName = "Ana Popescu",
            email = "admin@test.com",
            password = "admin123",
            team = "Backend",
            role = AccountRole.ADMIN,
            isOnline = true
        ),
        User(
            id = 2,
            fullName = "Mihai Ionescu",
            email = "employee@test.com",
            password = "employee123",
            team = "Frontend",
            role = AccountRole.EMPLOYEE,
            isOnline = true
        ),
        User(
            id = 3,
            fullName = "Elena Dumitrescu",
            email = "elena.dumitrescu@company.ro",
            password = "employee123",
            team = "QA",
            role = AccountRole.EMPLOYEE,
            isOnline = false
        ),
        User(
            id = 4,
            fullName = "Radu Popa",
            email = "radu.popa@company.ro",
            password = "employee123",
            team = "Backend",
            role = AccountRole.EMPLOYEE,
            isOnline = true
        ),
        User(
            id = 5,
            fullName = "Cristina Marin",
            email = "cristina.marin@company.ro",
            password = "employee123",
            team = "Design",
            role = AccountRole.EMPLOYEE,
            isOnline = false
        ),
        User(
            id = 6,
            fullName = "Alexandru Stan",
            email = "alex.stan@company.ro",
            password = "employee123",
            team = "Frontend",
            role = AccountRole.EMPLOYEE,
            isOnline = false
        )
    )

    fun getUsers(): List<User> = users.toList()

    fun addUser(user: User) {
        users.add(user)
    }

    fun deleteUser(userId: Int) {
        users.removeAll { it.id == userId }
    }

    fun findByCredentials(email: String, password: String): User? {
        return users.firstOrNull {
            it.email == email && it.password == password
        }
    }
}
