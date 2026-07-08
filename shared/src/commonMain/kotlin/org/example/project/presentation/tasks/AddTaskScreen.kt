package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.employee.Employee
import org.example.project.domain.project.Project
import org.example.project.domain.task.TaskStatus

@Composable
fun AddTaskScreen(
    viewModel: TaskListViewModel,
    onTaskAdded: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    projectId: Int = 0,
) {
    val colors = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.TO_DO) }
    var selectedAssignees by remember { mutableStateOf(emptySet<Int>()) }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }

    val showProjectPicker = projectId == 0
    val effectiveProjectId = if (projectId != 0) projectId else selectedProjectId

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Add task",
            color = colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TaskInput(value = title, onValueChange = { title = it }, label = "Title")
        TaskInput(value = description, onValueChange = { description = it }, label = "Description")
        TaskInput(value = dueDate, onValueChange = { dueDate = it }, label = "Due date (YYYY-MM-DD)")

        if (showProjectPicker) {
            Text(text = "Project", color = colors.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            ProjectDropdown(
                projects = uiState.projects,
                selectedId = selectedProjectId,
                onSelect = { selectedProjectId = it },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Status", color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        TaskStatusPicker(selected = status, onSelected = { status = it })

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Assignees", color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        AssigneePicker(
            employees = uiState.employees,
            selectedIds = selectedAssignees,
            onToggle = { id ->
                selectedAssignees = if (id in selectedAssignees) {
                    selectedAssignees - id
                } else {
                    selectedAssignees + id
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && dueDate.isNotBlank() && effectiveProjectId != null) {
                    viewModel.addTask(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        status = status,
                        assigneeIds = selectedAssignees.toList(),
                        projectId = effectiveProjectId,
                    )
                    onTaskAdded()
                }
            },
            enabled = title.isNotBlank() && dueDate.isNotBlank() && effectiveProjectId != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text("Save task")
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

@Composable
fun ProjectDropdown(
    projects: List<Project>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val selectedName = projects.find { it.id == selectedId }?.name ?: "Select project"

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedName,
                color = if (selectedId == null) colors.onSurfaceVariant else colors.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(text = "▾", color = colors.onSurfaceVariant)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onSelect(project.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AssigneePicker(
    employees: List<Employee>,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        employees.forEach { employee ->
            val isSelected = employee.id in selectedIds
            Row(
                modifier = Modifier
                    .background(
                        color = if (isSelected) colors.surfaceVariant else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) colors.primary else colors.outline,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onToggle(employee.id) }
                    .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmployeeAvatar(employee = employee, size = 24.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = employee.name,
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    fontSize = 14.sp,
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
