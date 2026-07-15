package org.example.project.presentation.accounts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.accounts.UserRepository
import org.example.project.domain.accounts.AccountRole

class AdminViewModel(
    private val repository: UserRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            repository.getUsers()
                .onSuccess { users ->
                    _state.value = _state.value.copy(
                        users = users,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load users."
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun setUserActive(userId: Int, isActive: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            repository.setUserActive(userId = userId, isActive = isActive)
                .onSuccess {
                    loadUsers()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not update user."
                    )
                }
        }
    }

    fun addUser(
        email: String,
        role: AccountRole,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)

            repository.addUser(email = email, role = role)
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        users = _state.value.users + user,
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Could not create user."
                    )
                }
        }
    }
}
