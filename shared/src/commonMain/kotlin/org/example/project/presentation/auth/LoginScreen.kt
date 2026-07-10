package org.example.project.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppTextField
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current

    DismissKeyboardOnTapOutside(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 20.dp)
                    .border(
                        width = 1.dp,
                        color = AppColorPalette.Border,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        color = AppColorPalette.Surface,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 44.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Text(
                    text = strings.text("Sign in"),
                    color = AppColorPalette.TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LoginInput(
                    label = strings.text("Email"),
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = strings.text("Enter your email"),
                )

                LoginInput(
                    label = strings.text("Password"),
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = strings.text("Enter your password"),
                    visualTransformation = PasswordVisualTransformation(),
                )

                state.errorMessage?.let {
                    Text(
                        text = it,
                        color = AppColorPalette.Error,
                        fontSize = 14.sp
                    )
                }

                AppButton(
                    onClick = viewModel::login,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = AppColorPalette.OnPrimary
                        )
                    } else {
                        Text(
                            text = strings.text("Login"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            color = AppColorPalette.TextPrimary,
            fontSize = 18.sp
        )

        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            visualTransformation = visualTransformation,
        )
    }
}
