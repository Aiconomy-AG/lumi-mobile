package org.example.project.presentation.accounts

import org.example.project.data.accounts.User

data class AdminState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredUsers: List<User>
        get() {
            if (searchQuery.isBlank()) return users

            return users.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        user.status.contains(searchQuery, ignoreCase = true) ||
                        user.role.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
}