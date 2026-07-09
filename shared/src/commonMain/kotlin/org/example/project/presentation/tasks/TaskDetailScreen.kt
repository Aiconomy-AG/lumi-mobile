package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.accounts.User
import org.example.project.domain.task.TaskStatus
import org.example.project.presentation.localization.LocalAppStrings

@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
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
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = strings.text("Tasks"),
                color = colors.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onBack),
            )
            Text(text = " / ", color = colors.onSurfaceVariant)
            Text(
                text = task.title,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = strings.text("Edit"),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onEditClick),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = task.title,
            color = colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = uiState.project?.let { "${it.name} · ${strings.text("Due")} ${task.dueDate}" } ?: "${strings.text("Due")} ${task.dueDate}",
            color = colors.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatusTabs(currentStatus = task.status)

        Spacer(modifier = Modifier.height(20.dp))

        TimeTrackingCard(
            elapsedSeconds = uiState.elapsedSeconds,
            taskTotalSeconds = uiState.taskTotalSeconds,
            isRunning = uiState.isTimerRunning,
            onToggle = viewModel::toggleTimer,
        )

        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = colors.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = strings.text("Description"), color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = task.description, color = colors.onBackground)

        Spacer(modifier = Modifier.height(24.dp))

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
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskStatus.entries.forEach { status ->
            val selected = status == currentStatus
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) colors.surfaceVariant else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                    )
                    .border(width = 1.dp, color = colors.outline, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = strings.taskStatus(status),
                    color = if (selected) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimeTrackingCard(
    elapsedSeconds: Int,
    taskTotalSeconds: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface, shape = MaterialTheme.shapes.medium)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = strings.text("Time tracking"), color = colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatElapsed(elapsedSeconds),
                color = colors.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${strings.text("Total task")}: ${formatElapsed(taskTotalSeconds)}",
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color = colors.primary, shape = CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isRunning) "■" else "▶",
                color = colors.onPrimary,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
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
