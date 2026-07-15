package org.example.project.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import org.example.project.domain.accounts.AccountRole
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun AddUserScreen(
    viewModel: AdminViewModel,
    onUserAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AccountRole.EMPLOYEE) }
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.ScreenPadding)
    ) {
        Text(
            text = strings.text("Add user"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        UserInput(
            value = email,
            onValueChange = { email = it },
            label = strings.text("Email")
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Text(
            text = strings.text("Role"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.Emphasis
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        RoleOption(
            text = strings.text("Admin"),
            selected = selectedRole == AccountRole.ADMIN,
            onClick = {
                selectedRole = AccountRole.ADMIN
            }
        )

        RoleOption(
            text = strings.text("Employee"),
            selected = selectedRole == AccountRole.EMPLOYEE,
            onClick = {
                selectedRole = AccountRole.EMPLOYEE
            }
        )

        Spacer(modifier = Modifier.height(AppDimensions.LargeSpacing))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    viewModel.addUser(
                        email = email,
                        role = selectedRole,
                        onSuccess = onUserAdded
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text(strings.text("Save user"))
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text(strings.text("Cancel"))
        }
    }
}

@Composable
private fun UserInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.SmallSpacing),
        singleLine = true,
        colors = AppComponentDefaults.appTextFieldColors()
    )
}

@Composable
private fun RoleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.ControlCornerRadius)
            )
            .padding(AppDimensions.SmallSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = AppComponentDefaults.appRadioButtonColors()
        )

        Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

        Text(
            text = text,
            color = AppColorPalette.TextPrimary
        )
    }

    Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))
}
