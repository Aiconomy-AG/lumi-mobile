package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.task.TaskStatus

@Composable
fun EditTaskScreen(
    viewModel: TaskDetailViewModel,
    onTaskUpdated: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val initialTask = uiState.task

    var title by remember { mutableStateOf(initialTask.title) }
    var description by remember { mutableStateOf(initialTask.description) }
    var dueDate by remember { mutableStateOf(initialTask.dueDate) }
    var status by remember { mutableStateOf(initialTask.status) }
    var selectedProjectId by remember { mutableStateOf(initialTask.projectId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Edit task",
            color = colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TaskInput(value = title, onValueChange = { title = it }, label = "Title")
        TaskInput(value = description, onValueChange = { description = it }, label = "Description")
        TaskInput(value = dueDate, onValueChange = { dueDate = it }, label = "Due date (YYYY-MM-DD)")

        Text(text = "Project", color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        ProjectDropdown(
            projects = uiState.allProjects,
            selectedId = selectedProjectId,
            onSelect = { selectedProjectId = it },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Status", color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        TaskStatusPicker(selected = status, onSelected = { status = it })

        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = colors.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && dueDate.isNotBlank() && selectedProjectId != 0) {
                    viewModel.updateTask(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        status = status,
                        projectId = selectedProjectId,
                        onSuccess = onTaskUpdated,
                    )
                }
            },
            enabled = !uiState.isSaving && selectedProjectId != 0,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(if (uiState.isSaving) "Saving..." else "Save changes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun TaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onBackground,
            unfocusedTextColor = colors.onBackground,
            cursorColor = colors.primary,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurfaceVariant
        )
    )
}

@Composable
private fun TaskStatusPicker(selected: TaskStatus, onSelected: (TaskStatus) -> Unit) {
    val colors = MaterialTheme.colorScheme

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
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(8.dp))
                    .clickable { onSelected(status) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = status.label(),
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.TO_DO -> "To do"
    TaskStatus.IN_PROGRESS -> "In progress"
    TaskStatus.COMPLETE -> "Complete"
    TaskStatus.BLOCKED -> "Blocked"
}
