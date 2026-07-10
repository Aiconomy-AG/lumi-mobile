package org.example.project.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.data.accounts.User
import org.example.project.domain.accounts.AccountRole
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onAddUserClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 5
    val filteredUsers = state.filteredUsers
    val totalPages = maxOf(1, (filteredUsers.size + pageSize - 1) / pageSize)
    val pagedUsers = filteredUsers
        .drop(currentPage * pageSize)
        .take(pageSize)

    LaunchedEffect(filteredUsers.size) {
        if (currentPage > totalPages - 1) {
            currentPage = totalPages - 1
        }
    }

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

        UsersTable(
            users = pagedUsers,
            onSetUserActive = viewModel::setUserActive
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AppPaginationBar(
            currentPage = currentPage,
            totalPages = totalPages,
            onPreviousClick = { if (currentPage > 0) currentPage-- },
            onNextClick = { if (currentPage < totalPages - 1) currentPage++ },
        )
        }
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
private fun UsersTable(
    users: List<User>,
    onSetUserActive: (Int, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = AppDimensions.TableMaxHeight)
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
                .width(1180.dp)
        ) {
            UsersTableHeader()

            users.forEach { user ->
                UserTableRow(
                    user = user,
                    onSetUserActive = {
                        onSetUserActive(user.id, !user.isActive)
                    }
                )
            }
        }
    }
}

@Composable
private fun UsersTableHeader() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.TinySpacing)
    ) {
        TableHeaderCell(strings.text("User"), 260)
        TableHeaderCell(strings.text("Email"), 260)
        TableHeaderCell(strings.text("Phone"), 160)
        TableHeaderCell(strings.text("Role"), 140)
        TableHeaderCell(strings.text("Status"), 120)
        TableHeaderCell(strings.text("Active"), 120)
        TableHeaderCell(strings.text("Actions"), 120)
    }
}

@Composable
private fun UserTableRow(
    user: User,
    onSetUserActive: () -> Unit
) {
    var showActiveDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(260.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user)

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = user.name,
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.Emphasis
            )
        }

        TableCell(user.email, 260, AppColorPalette.TextSecondary)
        TableCell(user.phoneNumber.ifBlank { "-" }, 160, AppColorPalette.TextSecondary)

        RoleBadge(
            role = user.role,
            modifier = Modifier.width(140.dp)
        )

        StatusCell(
            status = user.status,
            modifier = Modifier.width(120.dp)
        )

        ActiveCell(
            isActive = user.isActive,
            modifier = Modifier.width(120.dp)
        )

        AppButton(
            onClick = { showActiveDialog = true },
            modifier = Modifier.width(120.dp),
        ) {
            Text(if (user.isActive) strings.text("Deactivate") else strings.text("Reactivate"))
        }
    }

    if (showActiveDialog) {
        AlertDialog(
            onDismissRequest = {
                showActiveDialog = false
            },
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
                TextButton(
                    onClick = {
                        onSetUserActive()
                        showActiveDialog = false
                    }
                ) {
                    Text(
                        text = if (user.isActive) strings.text("Deactivate") else strings.text("Reactivate"),
                        color = AppColorPalette.Primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showActiveDialog = false
                    }
                ) {
                    Text(
                        text = strings.text("Cancel"),
                        color = AppColorPalette.TextSecondary
                    )
                }
            }
        )
    }
}


@Composable
private fun UserAvatar(
    user: User
) {
    val backgroundColor = when (user.role) {
        AccountRole.ADMIN -> AppColorPalette.AdminAvatarBackground
        AccountRole.EMPLOYEE -> AppColorPalette.EmployeeAvatarBackground
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = user.initials,
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader
        )
    }
}

@Composable
private fun RoleBadge(
    role: AccountRole,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (role) {
        AccountRole.ADMIN -> AppColorPalette.AdminAvatarBackground
        AccountRole.EMPLOYEE -> AppColorPalette.Border
    }

    val textColor = when (role) {
        AccountRole.ADMIN -> AppColorPalette.Primary
        AccountRole.EMPLOYEE -> AppColorPalette.TextPrimary
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(AppDimensions.TinySpacing)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = LocalAppStrings.current.accountRole(role),
                color = textColor,
                style = AppTextStyles.TableHeader
            )
        }
    }
}

@Composable
private fun StatusCell(
    status: String,
    modifier: Modifier = Modifier
) {
    val normalizedStatus = status.lowercase()
    val color = when (normalizedStatus) {
        "available" -> AppColorPalette.Success
        "busy" -> AppColorPalette.Error
        "away" -> AppColorPalette.Primary
        else -> AppColorPalette.TextSecondary
    }

    Text(
        text = LocalAppStrings.current.accountStatus(status),
        color = color,
        style = AppTextStyles.Emphasis,
        modifier = modifier
    )
}

@Composable
private fun ActiveCell(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (isActive) LocalAppStrings.current.text("Active") else LocalAppStrings.current.text("Inactive"),
        color = if (isActive) AppColorPalette.Success else AppColorPalette.TextSecondary,
        style = AppTextStyles.Emphasis,
        modifier = modifier
    )
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: Int
) {
    Text(
        text = text,
        color = AppColorPalette.TextSecondary,
        style = AppTextStyles.TableHeader,
        modifier = Modifier.width(width.dp)
    )
}

@Composable
private fun TableCell(
    text: String,
    width: Int,
    color: Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width.dp)
    )
}
