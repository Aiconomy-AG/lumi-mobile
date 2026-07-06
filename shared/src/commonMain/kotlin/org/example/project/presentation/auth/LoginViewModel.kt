package org.example.project.presentation.auth

import features.login.presentation.LoginState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.data.auth.MockAuthRepository
import org.example.project.data.auth.UserSession
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: MockAuthRepository,
    private val onLoginSuccess: (UserSession) -> Unit
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val currentState = _state.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Please enter email or password")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)

            val result = authRepository.login(
                email = currentState.email.trim(),
                password = currentState.password
            )

            result
                .onSuccess { user ->
                    _state.value = _state.value.copy(isLoading = false)
                    onLoginSuccess(user)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Login failed"
                    )
                }
        }
    }
}