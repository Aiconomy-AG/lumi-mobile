package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import features.login.presentation.LoginScreen
import org.jetbrains.compose.resources.painterResource

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.compose_multiplatform
import org.example.project.data.auth.MockAuthRepository
import org.example.project.data.auth.UserSession
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