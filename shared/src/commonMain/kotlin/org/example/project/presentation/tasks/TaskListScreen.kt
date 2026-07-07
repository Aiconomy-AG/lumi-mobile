package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus

@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.tasks.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = "Eroare: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> {
                TaskList(tasks = uiState.tasks)
            }
        }
    }
}

@Composable
private fun TaskList(tasks: List<Task>) {
    Column(modifier = Modifier.fillMaxSize()) {
        TaskListHeader()
        HorizontalDivider()
        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                TaskRow(task)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun TaskListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "Task", modifier = Modifier.weight(2f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Status", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Due", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TaskRow(task: Task) {
    val isDone = task.status == TaskStatus.DONE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        StatusBadge(status = task.status, modifier = Modifier.weight(1f))
        Text(
            text = task.dueDate,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val (label, background, textColor) = when (status) {
        TaskStatus.TODO -> Triple("To do", Color(0xFF3A3A3A), Color(0xFFE0E0E0))
        TaskStatus.IN_PROGRESS -> Triple("In progress", Color(0xFF4A2E00), Color(0xFFFFA726))
        TaskStatus.DONE -> Triple("Done", Color(0xFF0F3D2E), Color(0xFF4CAF50))
    }
    Box(
        modifier = modifier.background(color = background, shape = RoundedCornerShape(6.dp)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
