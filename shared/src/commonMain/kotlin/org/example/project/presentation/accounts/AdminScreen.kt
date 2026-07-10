package org.example.project.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.example.project.data.accounts.User
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppDetailField
import org.example.project.presentation.components.AppDetailGrid
import org.example.project.presentation.components.AppDetailOverlay
import org.example.project.presentation.components.AppListContainer
import org.example.project.presentation.components.AppListRow
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.AppStatusBadge
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.components.PlatformBackHandler
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.StatusColor

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onAddUserClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    val filteredUsers = state.filteredUsers
    val selectedUser = selectedUserId?.let { id ->
        state.users.firstOrNull { it.id == id }
    }

    PlatformBackHandler(
        enabled = selectedUserId != null,
        onBack = { selectedUserId = null },
    )

    DismissKeyboardOnTapOutside(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding),
        ) {
            AdminHeader(
                userCount = state.users.size,
                searchQuery = state.searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onAddUserClick = onAddUserClick
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
                Text(
                    text = message,
                    color = AppColorPalette.Error,
                    style = AppTextStyles.Emphasis
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

            UsersList(
                users = filteredUsers,
                onUserClick = { selectedUserId = it.id },
                onSetUserActive = viewModel::setUserActive,
                modifier = Modifier.weight(1f),
            )
        }
    }

    selectedUser?.let { user ->
        AdminUserOverlay(
            user = user,
            onBackClick = { selectedUserId = null },
        )
    }
}

@Composable
private fun AdminHeader(
    userCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAddUserClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column {
        Text(
            text = strings.text("Admin"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Text(
            text = strings.format("{count} users", "count" to userCount.toString()),
            color = AppColorPalette.TextSecondary,
            style = AppTextStyles.Emphasis
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search users..."),
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AppButton(
            onClick = onAddUserClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(strings.text("+ Add user"))
        }
    }
}

@Composable
private fun UsersList(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onSetUserActive: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListContainer(
        items = users,
        emptyMessage = LocalAppStrings.current.text("No users found"),
        modifier = modifier,
        key = { it.id },
    ) { user ->
        UserListRow(
            user = user,
            onClick = { onUserClick(user) },
            onSetUserActive = { onSetUserActive(user.id, !user.isActive) },
        )
    }
}

@Composable
private fun UserListRow(
    user: User,
    onClick: () -> Unit,
    onSetUserActive: () -> Unit,
) {
    var showActiveDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    AppListRow(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = user.name,
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.Emphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

            ActiveBadge(isActive = user.isActive)
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        AppButton(
            onClick = { showActiveDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (user.isActive) strings.text("Deactivate") else strings.text("Reactivate"))
        }
    }

    if (showActiveDialog) {
        ActiveConfirmationDialog(
            user = user,
            onConfirm = {
                onSetUserActive()
                showActiveDialog = false
            },
            onDismiss = { showActiveDialog = false },
        )
    }
}

@Composable
private fun AdminUserOverlay(
    user: User,
    onBackClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    AppDetailOverlay(
        title = user.name,
        onBackClick = onBackClick,
        trailingContent = {
            ActiveBadge(isActive = user.isActive)
        },
    ) {
        AppDetailGrid(
            fields = listOf(
                AppDetailField(strings.text("User"), user.name),
                AppDetailField(strings.text("Email"), user.email),
                AppDetailField(strings.text("Phone"), user.phoneNumber),
                AppDetailField(strings.text("Role"), strings.accountRole(user.role)),
                AppDetailField(strings.text("Status"), strings.accountStatus(user.status)),
                AppDetailField(strings.text("Active"), if (user.isActive) strings.text("Active") else strings.text("Inactive")),
                AppDetailField("ID", user.id.toString()),
                AppDetailField(strings.text("Language"), user.languageFlag),
            ),
        )
    }
}

@Composable
private fun ActiveConfirmationDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        titleContentColor = AppColorPalette.TextPrimary,
        textContentColor = AppColorPalette.TextPrimary,
        title = {
            Text(if (user.isActive) strings.text("Deactivate account?") else strings.text("Reactivate account?"))
        },
        text = {
            Text(
                if (user.isActive) {
                    strings.format("Are you sure you want to deactivate {name}?", "name" to user.name)
                } else {
                    strings.format("Are you sure you want to reactivate {name}?", "name" to user.name)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (user.isActive) strings.text("Deactivate") else strings.text("Reactivate"),
                    color = AppColorPalette.Primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.text("Cancel"),
                    color = AppColorPalette.TextSecondary
                )
            }
        }
    )
}

@Composable
private fun ActiveBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    AppStatusBadge(
        label = if (isActive) LocalAppStrings.current.text("Active") else LocalAppStrings.current.text("Inactive"),
        statusColor = activeStatusColor(isActive),
        modifier = modifier,
    )
}

private fun activeStatusColor(isActive: Boolean): StatusColor {
    return if (isActive) {
        AppColorPalette.StatusComplete
    } else {
        AppColorPalette.StatusToDo
    }
}
