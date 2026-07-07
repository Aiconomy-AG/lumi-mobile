package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.data.auth.MockAuthRepository
import org.example.project.data.auth.UserSession
import org.example.project.presentation.auth.LoginScreen
import org.example.project.presentation.auth.LoginViewModel
import org.example.project.presentation.tasks.TaskListScreen
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
    }
    else{
        MaterialTheme{
            val viewModel = viewModel{ TaskListViewModel() }
            TaskListScreen(viewModel = viewModel)
        }
    }
}
