package org.example.project.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import org.example.project.data.auth.UserSession
import org.example.project.domain.employee.Employee
import org.example.project.domain.project.Project
import org.example.project.domain.task.Task
import org.example.project.domain.task.TaskStatus
import org.example.project.presentation.tasks.TaskListViewModel
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun DashboardScreen(
    viewModel: TaskListViewModel,
    user: UserSession,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayIso = remember { currentIsoDate() }
    val dueTodayTasks = remember(uiState.tasks, todayIso) {
        uiState.tasks
            .filter { it.dueDate == todayIso && it.status != TaskStatus.COMPLETE }
            .sortedWith(compareBy<Task> { taskStatusRank(it.status) }.thenBy { it.title })
    }
    val onlinePeople = remember(uiState.employees, user) {
        onlinePeopleFor(user = user, employees = uiState.employees)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        when {
            uiState.isLoading && uiState.tasks.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColorPalette.Primary,
                )
            }

            uiState.error != null && uiState.tasks.isEmpty() -> {
                Text(
                    text = "Eroare: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColorPalette.Error,
                )
            }

            else -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWide = maxWidth >= 760.dp

                    if (isWide) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(44.dp),
                        ) {
                            DashboardMainColumn(
                                userName = user.name,
                                dateLabel = dashboardDateLabel(todayIso),
                                tasks = dueTodayTasks,
                                projects = uiState.projects,
                                employees = uiState.employees,
                                onTaskClick = onTaskClick,
                                modifier = Modifier.weight(1f),
                            )

                            OnlinePeoplePanel(
                                people = onlinePeople,
                                modifier = Modifier.width(250.dp),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                        ) {
                            DashboardMainColumn(
                                userName = user.name,
                                dateLabel = dashboardDateLabel(todayIso),
                                tasks = dueTodayTasks,
                                projects = uiState.projects,
                                employees = uiState.employees,
                                onTaskClick = onTaskClick,
                            )

                            OnlinePeoplePanel(
                                people = onlinePeople,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMainColumn(
    userName: String,
    dateLabel: String,
    tasks: List<Task>,
    projects: List<Project>,
    employees: List<Employee>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = dateLabel,
            color = AppColorPalette.TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Good evening, ${userName.firstName()}.",
            color = AppColorPalette.TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
        )

        Spacer(modifier = Modifier.height(46.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Due today",
                color = AppColorPalette.TextPrimarySoft,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}",
                color = AppColorPalette.TextSecondary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        HorizontalDivider(color = AppColorPalette.SubtleBorder)

        if (tasks.isEmpty()) {
            EmptyTodayState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                tasks.forEachIndexed { index, task ->
                    DashboardTaskRow(
                        task = task,
                        project = projects.firstOrNull { it.id == task.projectId },
                        assignees = employees.filter { it.id in task.assigneeIds },
                        onClick = { onTaskClick(task) },
                    )

                    if (index != tasks.lastIndex) {
                        HorizontalDivider(color = AppColorPalette.SubtleBorder)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTaskRow(
    task: Task,
    project: Project?,
    assignees: List<Employee>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlertDot(status = task.status)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = AppColorPalette.TextPrimarySoft,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = taskMeta(project = project, assignees = assignees),
                color = AppColorPalette.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        DashboardStatusBadge(status = task.status)
    }
}

@Composable
private fun AlertDot(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.BLOCKED -> AppColorPalette.Error
        TaskStatus.IN_PROGRESS -> AppColorPalette.Primary
        TaskStatus.TO_DO -> AppColorPalette.Error
        TaskStatus.COMPLETE -> AppColorPalette.Success
    }

    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}

@Composable
private fun DashboardStatusBadge(status: TaskStatus) {
    val (label, statusColor) = when (status) {
        TaskStatus.TO_DO -> "To do" to AppColorPalette.StatusToDo
        TaskStatus.IN_PROGRESS -> "In progress" to AppColorPalette.StatusInProgress
        TaskStatus.COMPLETE -> "Complete" to AppColorPalette.StatusComplete
        TaskStatus.BLOCKED -> "Blocked" to AppColorPalette.StatusBlocked
    }

    Box(
        modifier = Modifier.background(
            color = statusColor.background,
            shape = MaterialTheme.shapes.extraSmall,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = statusColor.content,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun EmptyTodayState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp)
            .border(1.dp, AppColorPalette.SubtleBorder, MaterialTheme.shapes.medium)
            .background(AppColorPalette.Surface, MaterialTheme.shapes.medium)
            .padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No tasks due today.",
            color = AppColorPalette.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OnlinePeoplePanel(
    people: List<OnlinePerson>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Online now",
                color = AppColorPalette.TextPrimarySoft,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${people.size} ${if (people.size == 1) "person" else "people"}",
                color = AppColorPalette.TextSecondary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        people.forEach { person ->
            OnlinePersonRow(person = person)
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun OnlinePersonRow(person: OnlinePerson) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(42.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.Center)
                    .background(AppColorPalette.SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = person.initials,
                    color = AppColorPalette.TextPrimarySoft,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .background(AppColorPalette.Success, CircleShape),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = person.name,
                color = AppColorPalette.TextPrimarySoft,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Available",
                color = AppColorPalette.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
            )
        }
    }
}

private data class OnlinePerson(
    val name: String,
    val initials: String,
)

private fun onlinePeopleFor(user: UserSession, employees: List<Employee>): List<OnlinePerson> {
    val names = buildList {
        add(user.name)
        addAll(employees.filterNot { it.id == user.id }.take(2).map { it.name })
    }.distinct().take(3)

    return names.map { name ->
        OnlinePerson(
            name = name,
            initials = initialsFor(name),
        )
    }
}

private fun currentIsoDate(): String = Clock.System.now().toString().take(10)

private fun dashboardDateLabel(isoDate: String): String {
    val year = isoDate.substringOrNull(0, 4)?.toIntOrNull() ?: return "Today"
    val month = isoDate.substringOrNull(5, 7)?.toIntOrNull() ?: return "Today"
    val day = isoDate.substringOrNull(8, 10)?.toIntOrNull() ?: return "Today"
    val weekday = weekdayName(year = year, month = month, day = day)
    val monthName = monthNames.getOrNull(month - 1) ?: return "Today"

    return "$weekday, $monthName $day"
}

private fun weekdayName(year: Int, month: Int, day: Int): String {
    val adjustedMonth = if (month < 3) month + 12 else month
    val adjustedYear = if (month < 3) year - 1 else year
    val century = adjustedYear / 100
    val yearOfCentury = adjustedYear % 100
    val index = (day + (13 * (adjustedMonth + 1)) / 5 + yearOfCentury + yearOfCentury / 4 + century / 4 + 5 * century) % 7

    return weekdayNames[(index + 6) % 7]
}

private fun taskMeta(project: Project?, assignees: List<Employee>): String {
    val projectName = project?.name ?: "No project"
    val assigneeText = assignees
        .take(3)
        .joinToString(" ") { initialsFor(it.name) }
        .ifBlank { "Unassigned" }

    return "$projectName · $assigneeText"
}

private fun taskStatusRank(status: TaskStatus): Int =
    when (status) {
        TaskStatus.BLOCKED -> 0
        TaskStatus.IN_PROGRESS -> 1
        TaskStatus.TO_DO -> 2
        TaskStatus.COMPLETE -> 3
    }

private fun initialsFor(name: String): String =
    name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1) }
        .uppercase()

private fun String.firstName(): String =
    trim().substringBefore(" ").ifBlank { this }

private fun String.substringOrNull(startIndex: Int, endIndex: Int): String? =
    if (length >= endIndex) substring(startIndex, endIndex) else null

private val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
)

private val weekdayNames = listOf(
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
)
