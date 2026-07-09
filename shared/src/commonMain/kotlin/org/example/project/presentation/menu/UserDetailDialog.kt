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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.project.data.auth.UserSession
import org.example.project.domain.auth.UserRole
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun UserDetailDialog(
    user: UserSession,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
) {
    val strings = LocalAppStrings.current

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
            InfoRow(label = strings.text("Phone"), value = user.phoneNumber.ifBlank { "-" })
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
