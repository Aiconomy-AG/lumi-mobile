package org.example.project.presentation.accounts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.accounts.MockUserRepository
import org.example.project.data.accounts.User
import org.example.project.domain.accounts.AccountRole

class AdminViewModel(
    private val repository: MockUserRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val users = repository.getUsers()

            _state.value = _state.value.copy(
                users = users,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            loadUsers()
        }
    }

    fun addUser(
        fullName: String,
        email: String,
        password: String,
        team: String,
        role: AccountRole
    ) {
        viewModelScope.launch {
            val currentUsers = repository.getUsers()
            val newId = (currentUsers.maxOfOrNull { it.id } ?: 0) + 1

            val newUser = User(
                id = newId,
                fullName = fullName,
                email = email,
                password = password,
                team = team,
                role = role,
                isOnline = false
            )

            repository.addUser(newUser)
            loadUsers()
        }
    }
}
