package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Task) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
    ) {
        when {
            uiState.isLoading && uiState.tasks.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = "Eroare: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.error,
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    TaskListToolbar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onAddTaskClick = onAddTaskClick,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TaskList(tasks = uiState.filteredTasks, onTaskClick = onTaskClick)
                }
            }
        }
    }
}

@Composable
private fun TaskListToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAddTaskClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text("Search tasks...", color = colors.onSurfaceVariant)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                cursorColor = colors.primary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant,
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddTaskClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            )
        ) {
            Text("+ Add task")
        }
    }
}

@Composable
private fun TaskList(tasks: List<Task>, onTaskClick: (Task) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(16.dp))
            .background(color = colors.surface, shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TaskListHeader()
            HorizontalDivider(color = colors.outline, modifier = Modifier.padding(vertical = 8.dp))
            tasks.forEachIndexed { index, task ->
                TaskRow(task, onClick = { onTaskClick(task) })
                if (index != tasks.lastIndex) {
                    HorizontalDivider(color = colors.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun TaskListHeader() {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "Task", modifier = Modifier.weight(1.7f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = "Status", modifier = Modifier.weight(1.3f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = "Due", modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val isDone = task.status == TaskStatus.COMPLETE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = task.title,
            modifier = Modifier.weight(1.7f),
            style = if (isDone) {
                MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (isDone) colors.onSurfaceVariant else colors.onBackground,
        )
        Box(
            modifier = Modifier.weight(1.3f),
            contentAlignment = Alignment.CenterStart,
        ) {
            StatusBadge(status = task.status)
        }
        Text(
            text = task.dueDate,
            modifier = Modifier.weight(1f),
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val (label, statusColor) = when (status) {
        TaskStatus.TO_DO -> "To do" to AppColorPalette.StatusToDo
        TaskStatus.IN_PROGRESS -> "In progress" to AppColorPalette.StatusInProgress
        TaskStatus.COMPLETE -> "Complete" to AppColorPalette.StatusComplete
        TaskStatus.BLOCKED -> "Blocked" to AppColorPalette.StatusBlocked
    }
    Box(
        modifier = modifier
            .background(color = statusColor.background, shape = MaterialTheme.shapes.extraSmall),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = statusColor.content,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
