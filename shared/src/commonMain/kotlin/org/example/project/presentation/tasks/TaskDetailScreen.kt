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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus

@Composable
fun TaskDetailScreen(
    task: Task,
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tasks",
                color = colors.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onBack),
            )
            Text(text = " / ", color = colors.onSurfaceVariant)
            Text(text = task.title, color = colors.onSurfaceVariant)
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
            text = "Due ${task.dueDate}",
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

        Text(text = "Description", color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = task.description, color = colors.onBackground)
    }
}

@Composable
private fun StatusTabs(currentStatus: TaskStatus) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    text = status.label(),
                    color = if (selected) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.TODO -> "To do"
    TaskStatus.IN_PROGRESS -> "In progress"
    TaskStatus.DONE -> "Done"
}

@Composable
private fun TimeTrackingCard(
    elapsedSeconds: Int,
    taskTotalSeconds: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface, shape = MaterialTheme.shapes.medium)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = "Time tracking", color = colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatElapsed(elapsedSeconds),
                color = colors.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total task: ${formatElapsed(taskTotalSeconds)}",
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
                fontSize = 18.sp,
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
