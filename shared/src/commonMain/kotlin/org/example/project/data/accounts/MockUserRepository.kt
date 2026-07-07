package org.example.project.data.accounts

class MockUserRepository {
    suspend fun getUsers(): List<User> {
        return AccountStore.getUsers()
    }

    suspend fun addUser(user: User) {
        AccountStore.addUser(user)
    }

    suspend fun deleteUser(userId: Int) {
        AccountStore.deleteUser(userId)
    }
}
