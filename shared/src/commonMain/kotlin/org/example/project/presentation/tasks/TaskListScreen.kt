package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
                TaskList(tasks = uiState.tasks, onTaskClick = onTaskClick)
            }
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
        Text(text = "Task", modifier = Modifier.weight(2f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = "Status", modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = "Due", modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val isDone = task.status == TaskStatus.DONE

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
            modifier = Modifier.weight(2f),
            style = if (isDone) {
                MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (isDone) colors.onSurfaceVariant else colors.onBackground,
        )
        StatusBadge(status = task.status, modifier = Modifier.weight(1f))
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
        TaskStatus.TODO -> "To do" to AppColorPalette.StatusTodo
        TaskStatus.IN_PROGRESS -> "In progress" to AppColorPalette.StatusInProgress
        TaskStatus.DONE -> "Done" to AppColorPalette.StatusDone
    }
    Box(
        modifier = modifier
            .padding(0.dp, 0.dp, 10.dp, 0.dp)
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
