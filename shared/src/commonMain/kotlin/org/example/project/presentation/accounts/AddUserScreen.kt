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
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var team by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AccountRole.EMPLOYEE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.ScreenPadding)
    ) {
        Text(
            text = "Add user",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        UserInput(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Full name"
        )

        UserInput(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        UserInput(
            value = password,
            onValueChange = { password = it },
            label = "Password"
        )

        UserInput(
            value = team,
            onValueChange = { team = it },
            label = "Team"
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Text(
            text = "Role",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.Emphasis
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        RoleOption(
            text = "Admin",
            selected = selectedRole == AccountRole.ADMIN,
            onClick = {
                selectedRole = AccountRole.ADMIN
            }
        )

        RoleOption(
            text = "Employee",
            selected = selectedRole == AccountRole.EMPLOYEE,
            onClick = {
                selectedRole = AccountRole.EMPLOYEE
            }
        )

        Spacer(modifier = Modifier.height(AppDimensions.LargeSpacing))

        Button(
            onClick = {
                if (
                    fullName.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    team.isNotBlank()
                ) {
                    viewModel.addUser(
                        fullName = fullName,
                        email = email,
                        password = password,
                        team = team,
                        role = selectedRole
                    )

                    onUserAdded()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("Save user")
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("Cancel")
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
