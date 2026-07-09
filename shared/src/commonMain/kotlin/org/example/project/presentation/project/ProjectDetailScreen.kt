package org.example.project.presentation.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.project.Project
import org.example.project.domain.task.Task
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.tasks.TaskList

@Composable
fun ProjectDetailScreen(
    viewModel: ProjectDetailViewModel,
    project: Project,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = strings.text("Projects"),
                color = colors.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onBack),
            )
            Text(text = " / ", color = colors.onSurfaceVariant)
            Text(
                text = project.name,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = project.name,
            color = colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${strings.text("Deadline")} ${project.deadline.take(10)}",
            color = colors.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProjectStatusBadge(status = project.status)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = strings.text("Description"), color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = project.description, color = colors.onBackground)

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = strings.text("Tasks"), color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(
                text = strings.text("+ Add task"),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAddTaskClick),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(text = strings.format("Error: {message}", "message" to (uiState.error ?: "")), color = colors.error)
            }
            uiState.tasks.isEmpty() -> {
                Text(text = strings.text("No tasks in this project yet."), color = colors.onSurfaceVariant)
            }
            else -> {
                TaskList(tasks = uiState.tasks, onTaskClick = onTaskClick)
            }
        }
    }
}
