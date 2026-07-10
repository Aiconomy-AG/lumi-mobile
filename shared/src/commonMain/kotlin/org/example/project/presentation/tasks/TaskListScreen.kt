package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.AppStatusBadge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.theme.AppColorPalette

private const val TASK_LIST_REFRESH_INTERVAL_MS = 5_000L
private const val TASK_LIST_PAGE_SIZE = 5

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Task) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()

    var currentPage by remember { mutableStateOf(0) }
    val pageSize = TASK_LIST_PAGE_SIZE
    val totalPages = maxOf(1, (uiState.filteredTasks.size + pageSize - 1) / pageSize)
    val pagedTasks = uiState.filteredTasks.drop(currentPage * pageSize).take(pageSize)

    LaunchedEffect(uiState.filteredTasks.size) {
        if (currentPage > totalPages - 1) currentPage = totalPages - 1
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadTasks()
            delay(TASK_LIST_REFRESH_INTERVAL_MS)
        }
    }

    DismissKeyboardOnTapOutside(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
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
                    text = strings.format("Error: {message}", "message" to (uiState.error ?: "")),
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.error,
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    TaskListToolbar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        statusFilter = uiState.statusFilter,
                        onStatusFilterChanged = viewModel::onStatusFilterChanged,
                        onlyMine = uiState.onlyMine,
                        onToggleOnlyMine = viewModel::onToggleOnlyMine,
                        onAddTaskClick = onAddTaskClick,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TaskList(tasks = pagedTasks, onTaskClick = onTaskClick)

                    Spacer(modifier = Modifier.height(12.dp))

                    AppPaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPreviousClick = { if (currentPage > 0) currentPage-- },
                        onNextClick = { if (currentPage < totalPages - 1) currentPage++ },
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun TaskListToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    statusFilter: TaskStatus?,
    onStatusFilterChanged: (TaskStatus?) -> Unit,
    onlyMine: Boolean,
    onToggleOnlyMine: () -> Unit,
    onAddTaskClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    Column {
        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search tasks..."),
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusFilterBar(selected = statusFilter, onSelect = onStatusFilterChanged)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyTasksToggle(
                active = onlyMine,
                onToggle = onToggleOnlyMine,
            )

            AppButton(
                onClick = onAddTaskClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(strings.text("+ Add task"))
            }
        }
    }
}

@Composable
private fun StatusFilterBar(
    selected: TaskStatus?,
    onSelect: (TaskStatus?) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    val options: List<Pair<String, TaskStatus?>> = listOf(
        strings.text("All") to null,
        strings.taskStatus(TaskStatus.TO_DO) to TaskStatus.TO_DO,
        strings.taskStatus(TaskStatus.IN_PROGRESS) to TaskStatus.IN_PROGRESS,
        strings.taskStatus(TaskStatus.COMPLETE) to TaskStatus.COMPLETE,
        strings.taskStatus(TaskStatus.BLOCKED) to TaskStatus.BLOCKED,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(12.dp))
            .background(color = colors.surface, shape = RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (label, status) ->
            val isSelected = selected == status
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) colors.primary else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelect(status) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun MyTasksToggle(
    active: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (active) colors.primary else colors.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .background(
                color = if (active) colors.primary else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = strings.text("My tasks"),
            color = if (active) colors.onPrimary else colors.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun TaskList(tasks: List<Task>, onTaskClick: (Task) -> Unit = {}) {
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
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = strings.text("Task"), modifier = Modifier.weight(1.7f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = strings.text("Status"), modifier = Modifier.weight(1.3f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = strings.text("Due"), modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
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
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDone) colors.onSurfaceVariant else colors.onBackground,
        )
        Box(
            modifier = Modifier.weight(1.3f),
            contentAlignment = Alignment.CenterStart,
        ) {
            StatusBadge(status = task.status)
        }
        Text(
            text = task.dueDate.take(10),
            modifier = Modifier.weight(1f),
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val (label, statusColor) = when (status) {
        TaskStatus.TO_DO -> strings.taskStatus(status) to AppColorPalette.StatusToDo
        TaskStatus.IN_PROGRESS -> strings.taskStatus(status) to AppColorPalette.StatusInProgress
        TaskStatus.COMPLETE -> strings.taskStatus(status) to AppColorPalette.StatusComplete
        TaskStatus.BLOCKED -> strings.taskStatus(status) to AppColorPalette.StatusBlocked
    }
    AppStatusBadge(
        label = label,
        statusColor = statusColor,
        modifier = modifier,
    )
}
