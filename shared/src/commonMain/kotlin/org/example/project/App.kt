package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import features.main.MainScreen
import kotlinx.coroutines.launch
import org.example.project.data.ApiConfig
import org.example.project.data.auth.AuthApiService
import org.example.project.data.auth.SessionStorage
import org.example.project.data.auth.UserSession
import org.example.project.data.createHttpClient
import org.example.project.notifications.PushNotificationCoordinator
import org.example.project.notifications.PushNotifications
import org.example.project.notifications.installTokenRefreshHandler
import org.example.project.presentation.auth.LoginScreen
import org.example.project.presentation.auth.LoginViewModel
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.AppLocalizationProvider
import org.example.project.presentation.theme.AppTheme


private enum class AppAuthState {
    Loading,
    LoggedOut,
    LoggedIn,
}

@Composable
@Preview
fun App() {
    var authState by remember { mutableStateOf(AppAuthState.Loading) }
    var currentUser by remember { mutableStateOf<UserSession?>(null) }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.EN) }
    val httpClient = remember { createHttpClient() }
    val authRepository = remember(httpClient) {
        AuthApiService(
            client = httpClient,
            baseUrl = ApiConfig.BASE_URL,
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        PushNotifications.initialize()
        PushNotificationCoordinator.configure(httpClient, ApiConfig.BASE_URL)
        installTokenRefreshHandler { fcmToken ->
            PushNotificationCoordinator.onTokenRefreshed(fcmToken)
        }
        PushNotifications.requestPermission()

        val savedSession = SessionStorage.loadSession()
        if (savedSession == null) {
            authState = AppAuthState.LoggedOut
            return@LaunchedEffect
        }

        authRepository.validateSession(savedSession)
            .onSuccess { session ->
                selectedLanguage = AppLanguage.fromCode(session.languageFlag)
                currentUser = session
                SessionStorage.saveSession(session)
                authState = AppAuthState.LoggedIn
            }
            .onFailure {
                SessionStorage.clearSession()
                authState = AppAuthState.LoggedOut
            }
    }

    LaunchedEffect(currentUser?.token) {
        val token = currentUser?.token ?: return@LaunchedEffect
        PushNotificationCoordinator.registerAfterLogin(token)
    }

    AppTheme {
        AppLocalizationProvider(language = selectedLanguage) {
            when (authState) {
                AppAuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                AppAuthState.LoggedOut -> {
                    val viewModel = remember(authRepository) {
                        LoginViewModel(
                            authRepository = authRepository,
                            onLoginSuccess = { user ->
                                SessionStorage.saveSession(user)
                                selectedLanguage = AppLanguage.fromCode(user.languageFlag)
                                currentUser = user
                                authState = AppAuthState.LoggedIn
                            }
                        )
                    }

                    LoginScreen(viewModel)
                }

                AppAuthState.LoggedIn -> {
                    val user = currentUser
                    if (user == null) {
                        authState = AppAuthState.LoggedOut
                    } else {
                        MainScreen(
                            user = user,
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { language ->
                                selectedLanguage = language
                            },
                            onPhoneNumberUpdated = { phoneNumber ->
                                val updatedUser = user.copy(phoneNumber = phoneNumber)
                                currentUser = updatedUser
                                SessionStorage.saveSession(updatedUser)
                            },
                            onLogout = {
                                val authToken = currentUser?.token
                                scope.launch {
                                    if (authToken != null) {
                                        PushNotificationCoordinator.unregisterOnLogout(authToken)
                                    }
                                    SessionStorage.clearSession()
                                    currentUser = null
                                    authState = AppAuthState.LoggedOut
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
