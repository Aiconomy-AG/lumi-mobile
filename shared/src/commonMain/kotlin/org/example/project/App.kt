package org.example.project

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import features.main.MainScreen
import kotlinx.coroutines.launch
import org.example.project.data.ApiConfig
import org.example.project.data.auth.AuthApiService
import org.example.project.data.auth.UserSession
import org.example.project.data.createHttpClient
import org.example.project.notifications.PushNotificationCoordinator
import org.example.project.notifications.installTokenRefreshHandler
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
    val httpClient = remember { createHttpClient() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        PushNotificationCoordinator.configure(httpClient, ApiConfig.BASE_URL)
        installTokenRefreshHandler { fcmToken ->
            PushNotificationCoordinator.onTokenRefreshed(fcmToken)
        }
    }

    LaunchedEffect(currentUser?.token) {
        val token = currentUser?.token ?: return@LaunchedEffect
        PushNotificationCoordinator.registerAfterLogin(token)
    }

    AppTheme {
        AppLocalizationProvider(language = selectedLanguage) {
            if (currentUser == null) {
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
                        val authToken = currentUser?.token
                        scope.launch {
                            if (authToken != null) {
                                PushNotificationCoordinator.unregisterOnLogout(authToken)
                            }
                            currentUser = null
                        }
                    }
                )
            }
        }
    }
}
