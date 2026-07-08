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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.project.data.accounts.User
import org.example.project.domain.accounts.AccountRole
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .padding(AppDimensions.ScreenPadding)
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

        AdminPagination(
            currentPage = currentPage,
            totalPages = totalPages,
            onPreviousClick = {
                if (currentPage > 0) {
                    currentPage--
                }
            },
            onNextClick = {
                if (currentPage < totalPages - 1) {
                    currentPage++
                }
            }
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
    Column {
        Text(
            text = "Admin",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Text(
            text = "$userCount users",
            color = AppColorPalette.TextSecondary,
            style = AppTextStyles.Emphasis
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text("Search users...", color = AppColorPalette.TextSecondary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onAddUserClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("+ Add user")
        }
    }
}

@Composable
private fun UsersTable(
    users: List<User>,
    onSetUserActive: (Int, Boolean) -> Unit
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

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
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 26.dp)
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

        AdminHorizontalScrollBar(
            scrollValue = horizontalScrollState.value,
            maxScrollValue = horizontalScrollState.maxValue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun UsersTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.TinySpacing)
    ) {
        TableHeaderCell("User", 260)
        TableHeaderCell("Email", 260)
        TableHeaderCell("Phone", 160)
        TableHeaderCell("Role", 140)
        TableHeaderCell("Status", 120)
        TableHeaderCell("Active", 120)
        TableHeaderCell("Actions", 120)
    }
}

@Composable
private fun UserTableRow(
    user: User,
    onSetUserActive: () -> Unit
) {
    var showActiveDialog by remember { mutableStateOf(false) }

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

        Button(
            onClick = {
                showActiveDialog = true
            },
            modifier = Modifier.width(120.dp),
            colors = AppComponentDefaults.primaryButtonColors(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(if (user.isActive) "Deactivate" else "Reactivate")
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
                Text(if (user.isActive) "Deactivate account?" else "Reactivate account?")
            },
            text = {
                Text(
                    if (user.isActive) {
                        "Are you sure you want to deactivate ${user.name}?"
                    } else {
                        "Are you sure you want to reactivate ${user.name}?"
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
                        text = if (user.isActive) "Deactivate" else "Reactivate",
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
                        text = "Cancel",
                        color = AppColorPalette.TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun AdminPagination(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousClick,
            enabled = currentPage > 0,
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            color = AppColorPalette.TextSecondary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun AdminHorizontalScrollBar(
    scrollValue: Int,
    maxScrollValue: Int,
    modifier: Modifier = Modifier
) {
    if (maxScrollValue <= 0) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimensions.ScrollBarHeight)
            .background(
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.ScrollBarHeight)
            )
    ) {
        val density = LocalDensity.current
        val trackWidth = maxWidth
        val trackWidthPx = with(density) { trackWidth.toPx() }
        val contentWidthPx = trackWidthPx + maxScrollValue
        val thumbWidth = (trackWidth * (trackWidthPx / contentWidthPx)).coerceAtLeast(40.dp)
        val maxThumbOffset = trackWidth - thumbWidth
        val scrollProgress = scrollValue.toFloat() / maxScrollValue.toFloat()
        val thumbOffset = maxThumbOffset * scrollProgress

        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(thumbWidth)
                .fillMaxHeight()
                .background(
                    color = AppColorPalette.Primary,
                    shape = RoundedCornerShape(AppDimensions.ScrollBarHeight)
                )
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
            color = AppColorPalette.Primary,
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
                text = role.displayName,
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
        text = status.replaceFirstChar { it.uppercase() },
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
        text = if (isActive) "Active" else "Inactive",
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
