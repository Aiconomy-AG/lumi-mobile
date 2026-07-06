package org.example.project

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.data.auth.MockAuthRepository
import org.example.project.data.auth.UserSession
import org.example.project.presentation.auth.LoginScreen
import org.example.project.presentation.auth.LoginViewModel

@Composable
@Preview
fun App() {
    var currentUser by remember { mutableStateOf<UserSession?>(null) }

    if (currentUser == null) {
        val viewModel = remember {
            LoginViewModel(
                authRepository = MockAuthRepository(),
                onLoginSuccess = { user ->
                    currentUser = user
                }
            )
        }

        LoginScreen(viewModel)
    }
}
