package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.example.project.data.auth.AuthRepository
import org.example.project.data.auth.UserSession
import org.example.project.domain.auth.UserRole
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun UserDetailDialog(
    user: UserSession,
    selectedLanguage: AppLanguage,
    authRepository: AuthRepository,
    onLanguageSelected: (AppLanguage) -> Unit,
    onPhoneNumberUpdated: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var displayedPhone by remember(user.phoneNumber) { mutableStateOf(user.phoneNumber) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AppColorPalette.Surface, shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = AppColorPalette.Border, shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(color = AppColorPalette.Primary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.name.take(2).uppercase(),
                    color = AppColorPalette.OnPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.name,
                color = AppColorPalette.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user.email,
                color = AppColorPalette.TextSecondary,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleBadge(role = user.role)

            Spacer(modifier = Modifier.height(24.dp))

            InfoRow(label = strings.text("Email"), value = user.email)
            EditablePhoneRow(
                phoneNumber = displayedPhone,
                authRepository = authRepository,
                token = user.token,
                onPhoneNumberUpdated = { updatedPhone ->
                    displayedPhone = updatedPhone
                    onPhoneNumberUpdated(updatedPhone)
                },
            )
            StatusRow(status = user.status)
            InfoRow(label = strings.text("Role"), value = strings.userRole(user.role))

            Spacer(modifier = Modifier.height(14.dp))

            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.LogoutDanger,
                    contentColor = AppColorPalette.TextPrimary,
                ),
            ) {
                Text(strings.text("Log out"))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.text("Close"),
                color = AppColorPalette.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier
            .background(color = AppColorPalette.SelectionOverlay, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = strings.userRole(role),
            color = AppColorPalette.Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusRow(status: String) {
    val strings = LocalAppStrings.current
    val isAvailable = status.equals("available", ignoreCase = true)
    val statusColor = if (isAvailable) AppColorPalette.Success else AppColorPalette.Error
    val label = if (status.isBlank()) "-" else strings.accountStatus(status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = strings.text("Status"), color = AppColorPalette.TextSecondary, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = statusColor, shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = strings.text("Language"),
            color = AppColorPalette.TextSecondary,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.values().forEach { language ->
                val selected = language == selectedLanguage

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) AppColorPalette.Primary else AppColorPalette.SurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { onLanguageSelected(language) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = language.label,
                        color = if (selected) AppColorPalette.OnPrimary else AppColorPalette.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = AppColorPalette.TextSecondary, fontSize = 14.sp)
        Text(text = value, color = AppColorPalette.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EditablePhoneRow(
    phoneNumber: String,
    authRepository: AuthRepository,
    token: String,
    onPhoneNumberUpdated: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var draftPhone by remember(phoneNumber) { mutableStateOf(phoneNumber) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = strings.text("Phone"), color = AppColorPalette.TextSecondary, fontSize = 14.sp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = draftPhone,
                        onValueChange = {
                            draftPhone = it
                            errorMessage = null
                        },
                        modifier = Modifier.width(160.dp),
                        singleLine = true,
                        enabled = !isSaving,
                        placeholder = { Text("+40722123456") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColorPalette.TextPrimary,
                            unfocusedTextColor = AppColorPalette.TextPrimary,
                            cursorColor = AppColorPalette.Primary,
                            focusedBorderColor = AppColorPalette.Primary,
                            unfocusedBorderColor = AppColorPalette.Border,
                        ),
                    )
                } else {
                    Text(
                        text = phoneNumber.ifBlank { "-" },
                        color = AppColorPalette.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppColorPalette.Primary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = if (isEditing) "✓" else "✎",
                        color = AppColorPalette.Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (isEditing) {
                                    val trimmed = draftPhone.trim()
                                    if (trimmed.isBlank()) {
                                        errorMessage = strings.text("Phone number is required.")
                                        return@clickable
                                    }
                                    if (trimmed.length > 20) {
                                        errorMessage = strings.text("Phone number is too long.")
                                        return@clickable
                                    }

                                    scope.launch {
                                        isSaving = true
                                        errorMessage = null
                                        authRepository.updatePhoneNumber(token, trimmed)
                                            .onSuccess { updatedPhone ->
                                                onPhoneNumberUpdated(updatedPhone)
                                                draftPhone = updatedPhone
                                                isEditing = false
                                            }
                                            .onFailure { error ->
                                                errorMessage = error.message
                                            }
                                        isSaving = false
                                    }
                                } else {
                                    draftPhone = phoneNumber
                                    errorMessage = null
                                    isEditing = true
                                }
                            }
                            .padding(4.dp),
                    )
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = AppColorPalette.Error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}
