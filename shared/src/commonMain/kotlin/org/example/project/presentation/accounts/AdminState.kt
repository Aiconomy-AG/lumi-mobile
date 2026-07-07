package org.example.project.presentation.accounts

import org.example.project.data.accounts.User

data class AdminState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
) {
    val filteredUsers: List<User>
        get() {
            if (searchQuery.isBlank()) return users

            return users.filter { user ->
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        user.team.contains(searchQuery, ignoreCase = true) ||
                        user.role.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
}