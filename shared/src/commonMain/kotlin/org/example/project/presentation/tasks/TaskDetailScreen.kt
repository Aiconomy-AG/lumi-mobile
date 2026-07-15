package org.example.project.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.accounts.User
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus
import org.example.project.presentation.components.AppBackButton
import org.example.project.presentation.localization.LocalAppStrings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import org.example.project.domain.tasktimeentry.TaskTimeEntry
import org.example.project.presentation.theme.AppColorPalette


@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onSubtaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    var assigneePickerOpen by remember { mutableStateOf(false) }
    var assigneeQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(assigneePickerOpen) {
                detectTapGestures(onTap = {
                    if (assigneePickerOpen) {
                        assigneePickerOpen = false
                        assigneeQuery = ""
                    }
                })
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to tasks",
                    tint = colors.onBackground,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Tasks",
                    color = colors.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = strings.text("Edit"),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onEditClick)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = task.title,
            color = colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = uiState.project?.let { "${it.name} · ${strings.text("Due")} ${task.dueDate.take(10)}" } ?: "${strings.text("Due")} ${task.dueDate}",
            color = colors.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatusTabs(currentStatus = task.status)

        Spacer(modifier = Modifier.height(12.dp))

        TimeTrackingCard(
            elapsedSeconds = uiState.elapsedSeconds,
            taskTotalSeconds = uiState.taskTotalSeconds,
            isRunning = uiState.isTimerRunning,
            isAssigned = uiState.isCurrentUserAssigned,
            onToggle = viewModel::toggleTimer,
        )

        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = colors.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        DetailSectionCard(title = strings.text("Description")) {
            Text(
                text = task.description.ifBlank { strings.text("No description.") },
                color = colors.onBackground,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AssigneesSection(
            assignees = uiState.assignees,
            allUsers = uiState.allUsers,
            onAssign = viewModel::assignUser,
            onUnassign = viewModel::unassignUser,
            pickerOpen = assigneePickerOpen,
            onSetPickerOpen = { assigneePickerOpen = it },
            query = assigneeQuery,
            onQueryChange = { assigneeQuery = it },
        )

        if (uiState.isRootTask) {
            Spacer(modifier = Modifier.height(14.dp))
            SubtasksSection(
                subtasks = uiState.subtasks,
                expanded = uiState.subtasksExpanded,
                isLoading = uiState.isSubtasksLoading,
                isCreating = uiState.isCreatingSubtask,
                parentDueDate = task.dueDate.take(10),
                onToggleExpanded = viewModel::toggleSubtasksExpanded,
                onSubtaskClick = onSubtaskClick,
                onCreateSubtask = { title, description, dueDate, status, onSuccess ->
                    viewModel.createSubtask(title, description, dueDate, status, onSuccess)
                },
            )
        }
        TimeEntriesSection(
            entries = uiState.timeEntries,
            users = uiState.allUsers,
            totalSeconds = uiState.taskTotalSeconds,
        )
    }
}

@Composable
private fun TimeEntriesSection(
    entries: List<TaskTimeEntry>,
    users: List<User>,
    totalSeconds: Int,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    DetailSectionCard(title = strings.text("Time entries")) {
        if (entries.isEmpty()) {
            Text(
                text = strings.text("No time entries yet."),
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
            )
        } else {
            entries.take(6).forEach { entry ->
                val user = users.firstOrNull { it.id == entry.employeeId }
                val displayName = user?.name ?: "Employee #${entry.employeeId}"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (user != null) {
                        UserAvatar(user = user, size = 28.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            color = colors.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = if (entry.stoppedAt == null) "Running" else "Completed",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }

                    Text(
                        text = formatElapsed(entry.durationSeconds ?: 0),
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total logged: ${formatElapsed(totalSeconds)}",
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface, shape = RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = title,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

@Composable
private fun AssigneesSection(
    assignees: List<User>,
    allUsers: List<User>,
    onAssign: (Int) -> Unit,
    onUnassign: (Int) -> Unit,
    pickerOpen: Boolean,
    onSetPickerOpen: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    val assignedIds = assignees.map { it.id }.toSet()

    Text(text = strings.text("Assigned to"), color = colors.onSurfaceVariant)
    Spacer(modifier = Modifier.height(12.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        assignees.forEach { employee ->
            AssigneeChip(employee = employee, onRemove = { onUnassign(employee.id) })
        }

        Box(
            modifier = Modifier
                .background(color = Color.Transparent, shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(20.dp))
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
                .clickable {
                    val newOpen = !pickerOpen
                    onSetPickerOpen(newOpen)
                    if (!newOpen) onQueryChange("")
                }
        ) {
            Text(text = strings.text("+ Assign"), color = colors.onSurfaceVariant, fontSize = 14.sp)
        }
    }

    if (pickerOpen) {
        val filtered = allUsers.filter { it.name.contains(query, ignoreCase = true) }

        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = colors.outline, shape = MaterialTheme.shapes.medium)
                .background(color = colors.surface, shape = MaterialTheme.shapes.medium)
                .padding(8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(strings.text("Search by name..."), color = colors.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onBackground,
                    unfocusedTextColor = colors.onBackground,
                    cursorColor = colors.primary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Text(
                    text = strings.text("No users found"),
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                )
            } else {
                filtered.forEach { employee ->
                    val isAssigned = employee.id in assignedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAssigned) onUnassign(employee.id) else onAssign(employee.id)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UserAvatar(user = employee, size =28.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = employee.name,
                            color = colors.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        if (isAssigned) {
                            Text(text = "✓", color = colors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssigneeChip(employee: User, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .background(color = colors.surface, shape = RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(20.dp))
            .padding(start = 6.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(user = employee, size =24.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = employee.name, color = colors.onBackground, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "✕",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.clickable(onClick = onRemove),
        )
    }
}

@Composable
private fun StatusTabs(currentStatus: TaskStatus) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskStatus.entries.forEach { status ->
            val selected = status == currentStatus
            val color = statusColor(status)

            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) color.copy(alpha = 0.20f) else Color.Transparent,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) color else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = LocalAppStrings.current.taskStatus(status),
                    color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun statusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.TO_DO -> Color(0xFF8A8A8A)
        TaskStatus.IN_PROGRESS -> Color(0xFFB57CFF)
        TaskStatus.COMPLETE -> Color(0xFF22C55E)
        TaskStatus.BLOCKED -> Color(0xFFFF4D4D)
    }
}

@Composable
private fun TimeTrackingCard(
    elapsedSeconds: Int,
    taskTotalSeconds: Int,
    isRunning: Boolean,
    isAssigned: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val canUseTimer = isAssigned || isRunning

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface, shape = RoundedCornerShape(18.dp))
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Current session",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatElapsed(elapsedSeconds),
                color = colors.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total logged: ${formatElapsed(taskTotalSeconds)}",
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            if (!isAssigned && !isRunning) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You are not assigned on this task!",
                    color = colors.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        IconButton(
            onClick = onToggle,
            enabled = canUseTimer,
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = if (canUseTimer) colors.primary.copy(alpha = 0.18f) else colors.surfaceVariant,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isRunning) "Pause timer" else "Start timer",
                tint = if (canUseTimer) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Int.pad(): String = toString().padStart(2, '0')

@Composable
private fun SubtasksSection(
    subtasks: List<Task>,
    expanded: Boolean,
    isLoading: Boolean,
    isCreating: Boolean,
    parentDueDate: String,
    onToggleExpanded: () -> Unit,
    onSubtaskClick: (Task) -> Unit,
    onCreateSubtask: (String, String, String, TaskStatus, () -> Unit) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    var showAddForm by remember { mutableStateOf(false) }
    var subtaskTitle by remember { mutableStateOf("") }
    var subtaskDescription by remember { mutableStateOf("") }
    var subtaskDueDate by remember { mutableStateOf(parentDueDate) }
    var subtaskStatus by remember { mutableStateOf(TaskStatus.TO_DO) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "▼" else "▶",
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = "${strings.text("Subtasks")} (${subtasks.size})",
                color = colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (isLoading) {
                Text(text = "...", color = colors.onSurfaceVariant)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                if (subtasks.isEmpty() && !isLoading) {
                    Text(
                        text = strings.text("No subtasks yet"),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                } else {
                    subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSubtaskClick(subtask) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subtask.title,
                                    color = colors.onBackground,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${strings.text("Due")} ${subtask.dueDate.take(10)}",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Text(
                                text = strings.taskStatus(subtask.status),
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }

                if (showAddForm) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subtaskTitle,
                        onValueChange = { subtaskTitle = it },
                        placeholder = { Text(strings.text("Subtask title")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subtaskDescription,
                        onValueChange = { subtaskDescription = it },
                        placeholder = { Text(strings.text("Description")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subtaskDueDate,
                        onValueChange = { subtaskDueDate = it },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SubtaskStatusPicker(
                        selected = subtaskStatus,
                        onSelected = { subtaskStatus = it },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onCreateSubtask(
                                    subtaskTitle,
                                    subtaskDescription,
                                    subtaskDueDate,
                                    subtaskStatus,
                                ) {
                                    subtaskTitle = ""
                                    subtaskDescription = ""
                                    subtaskDueDate = parentDueDate
                                    subtaskStatus = TaskStatus.TO_DO
                                    showAddForm = false
                                }
                            },
                            enabled = !isCreating && subtaskTitle.isNotBlank() && subtaskDueDate.isNotBlank(),
                        ) {
                            Text(if (isCreating) strings.text("Saving...") else strings.text("Create subtask"))
                        }
                        Button(onClick = { showAddForm = false }) {
                            Text(strings.text("Cancel"))
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.text("Add subtask"),
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showAddForm = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtaskStatusPicker(
    selected: TaskStatus,
    onSelected: (TaskStatus) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskStatus.entries.forEach { status ->
            val isSelected = status == selected
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) colors.surfaceVariant else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                    )
                    .border(width = 1.dp, color = colors.outline, shape = MaterialTheme.shapes.small)
                    .clickable { onSelected(status) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = strings.taskStatus(status),
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
    }
}
