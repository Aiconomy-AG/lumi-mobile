package org.example.project

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import features.main.MainScreen
import org.example.project.data.auth.MockAuthRepository
import org.example.project.data.auth.UserSession
import org.example.project.presentation.auth.LoginScreen
import org.example.project.presentation.auth.LoginViewModel
import org.example.project.presentation.tasks.TaskListViewModel

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
    } else {
        MainScreen(
            user = currentUser!!,
            onLogout = {
                currentUser = null
            }
        )
    }
}
