package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.example.project.data.auth.UserSession
import org.example.project.domain.auth.UserRole
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import org.example.project.domain.task.Task
import org.example.project.presentation.tasks.TaskDetailScreen
import org.example.project.presentation.tasks.TaskDetailViewModel
import org.example.project.presentation.tasks.TaskListScreen
import org.example.project.presentation.tasks.TaskListViewModel

@Composable
fun MainScreen(
    user: UserSession,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    val taskListViewModel = remember { TaskListViewModel() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val availableSections = AppSection.entries.filter {
        !it.adminOnly || user.role == UserRole.ADMIN
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                user = user,
                sections = availableSections,
                selectedSection = selectedSection,
                onSectionSelected = { section ->
                    selectedSection = section
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF0B0B0B),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                AppTopBar(
                    title = selectedSection.title,
                    user = user,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                AppBottomBar(
                    sections = availableSections,
                    selectedSection = selectedSection,
                    onSectionSelected = { section ->
                        selectedSection = section
                    }
                )
            }
        ) { paddingValues ->
            when (selectedSection) {
                AppSection.TASKS -> {
                    var selectedTask by remember { mutableStateOf<Task?>(null) }
                    val task = selectedTask

                    if (task == null) {
                        TaskListScreen(
                            viewModel = taskListViewModel,
                            onTaskClick = { selectedTask = it },
                            modifier = Modifier.padding(paddingValues),
                        )
                    } else {
                        TaskDetailScreen(
                            task = task,
                            viewModel = remember(task.id) { TaskDetailViewModel(task = task) },
                            onBack = { selectedTask = null },
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }
                else -> {
                    EmptySectionScreen(
                        title = selectedSection.title,
                        modifier = Modifier
                            .padding(paddingValues)
                            .background(Color(0xFF0B0B0B))
                    )
                }
            }
        }
    }
}