package org.example.project.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val state by viewModel.state.collectAsState()

    val background = Color(0xFF0B0B0B)
    val cardBackground = Color(0xFF121212)
    val borderColor = Color(0xFF2A2A2A)
    val textWhite = Color(0xFFF5F5F5)
    val textGray = Color(0xFF9A9A9A)
    val yellow = Color(0xFFFFB31A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(horizontal = 20.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(
                    color = cardBackground,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 44.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                text = "Welcome back",
                color = textWhite,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginInput(
                label = "Email",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Enter your email",
            )

            LoginInput(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Enter your password",
                isPassword = true
            )

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF5C5C),
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = viewModel::login,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = yellow,
                    contentColor = Color.Black,
                    disabledContainerColor = yellow.copy(alpha = 0.5f),
                    disabledContentColor = Color.Black.copy(alpha = 0.6f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = borderColor)

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = textGray)) {
                        append("Mock users: ")
                    }
                    withStyle(SpanStyle(color = yellow)) {
                        append("admin@test.com")
                    }
                    withStyle(SpanStyle(color = textGray)) {
                        append(" / ")
                    }
                    withStyle(SpanStyle(color = yellow)) {
                        append("admin123")
                    }
                    withStyle(SpanStyle(color = textGray)) {
                        append(" or\n")
                    }
                    withStyle(SpanStyle(color = yellow)) {
                        append("employee@test.com")
                    }
                    withStyle(SpanStyle(color = textGray)) {
                        append(" / ")
                    }
                    withStyle(SpanStyle(color = yellow)) {
                        append("employee123")
                    }
                },
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun LoginInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    val borderColor = Color(0xFF303030)
    val focusedBorderColor = Color(0xFFFFB31A)
    val textWhite = Color(0xFFF5F5F5)
    val textGray = Color(0xFF9A9A9A)
    val fieldBackground = Color(0xFF111111)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            color = textWhite,
            fontSize = 18.sp
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = textGray,
                    fontSize = 16.sp
                )
            },
            singleLine = true,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textWhite,
                unfocusedTextColor = textWhite,
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = fieldBackground,
                unfocusedContainerColor = fieldBackground,
                cursorColor = focusedBorderColor
            )
        )
    }
}
