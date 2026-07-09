package org.example.project

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import features.main.MainScreen
import org.example.project.data.ApiConfig
import org.example.project.data.auth.AuthApiService
import org.example.project.data.auth.UserSession
import org.example.project.data.createHttpClient
import org.example.project.presentation.auth.LoginScreen
import org.example.project.presentation.auth.LoginViewModel
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.AppLocalizationProvider
import org.example.project.presentation.theme.AppTheme

@Composable
@Preview
fun App() {
    var currentUser by remember { mutableStateOf<UserSession?>(null) }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.EN) }

    AppTheme {
        AppLocalizationProvider(language = selectedLanguage) {
            if (currentUser == null) {
                val httpClient = remember { createHttpClient() }

                val viewModel = remember {
                    LoginViewModel(
                        authRepository = AuthApiService(
                            client = httpClient,
                            baseUrl = ApiConfig.BASE_URL
                        ),
                        onLoginSuccess = { user ->
                            selectedLanguage = AppLanguage.fromCode(user.languageFlag)
                            currentUser = user
                        }
                    )
                }

                LoginScreen(viewModel)
            } else {
                MainScreen(
                    user = currentUser!!,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { language ->
                        selectedLanguage = language
                    },
                    onLogout = {
                        currentUser = null
                    }
                )
            }
        }
    }
}
