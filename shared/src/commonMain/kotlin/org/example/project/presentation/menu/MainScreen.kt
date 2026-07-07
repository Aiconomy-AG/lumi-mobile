package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.example.project.data.auth.UserSession
import org.example.project.domain.auth.UserRole
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import org.example.project.data.tasktimeentry.TaskTimeEntryMockApiService
import org.example.project.domain.task.Task
import org.example.project.presentation.tasks.ActiveTimerViewModel
import org.example.project.presentation.tasks.TaskDetailScreen
import org.example.project.presentation.tasks.TaskDetailViewModel
import feature.stock.data.MockStockRepository
import feature.stock.presentation.AddProductScreen
import org.example.project.presentation.stock.StockScreen
import org.example.project.presentation.stock.StockViewModel
import org.example.project.presentation.tasks.TaskListScreen
import org.example.project.presentation.tasks.TaskListViewModel

@Composable
fun MainScreen(
    user: UserSession,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val taskListViewModel = remember { TaskListViewModel() }
    val stockViewModel = remember { StockViewModel(MockStockRepository()) }
    var showAddProductScreen by remember { mutableStateOf(false) }
    val taskTimeEntryApi = remember { TaskTimeEntryMockApiService() }
    val activeTimerViewModel = remember { ActiveTimerViewModel(timeEntryApi = taskTimeEntryApi) }
    val colors = MaterialTheme.colorScheme

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
                    selectedTask = null
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = colors.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                AppTopBar(
                    title = selectedSection.title,
                    user = user,
                    activeTimerViewModel = activeTimerViewModel,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onOpenActiveTask = { task ->
                        selectedSection = AppSection.TASKS
                        selectedTask = task
                    }
                )
            },
            bottomBar = {
                AppBottomBar(
                    sections = availableSections,
                    selectedSection = selectedSection,
                    onSectionSelected = { section ->
                        selectedSection = section
                        selectedTask = null
                    }
                )
            }
        ) { paddingValues ->
            when (selectedSection) {
                AppSection.TASKS -> {
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
                            viewModel = remember(task.id) {
                                TaskDetailViewModel(
                                    task = task,
                                    employeeId = user.id,
                                    activeTimerViewModel = activeTimerViewModel,
                                    timeEntryApi = taskTimeEntryApi,
                                )
                            },
                            onBack = { selectedTask = null },
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }

                AppSection.STOCK -> {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        if (showAddProductScreen) {
                            AddProductScreen(
                                viewModel = stockViewModel,
                                onProductAdded = {
                                    showAddProductScreen = false
                                },
                                onBackClick = {
                                    showAddProductScreen = false
                                }
                            )
                        } else {
                            StockScreen(
                                viewModel = stockViewModel,
                                onAddProductClick = {
                                    showAddProductScreen = true
                                }
                            )
                        }
                    }
                }

                else -> {
                    EmptySectionScreen(
                        title = selectedSection.title,
                        modifier = Modifier
                            .padding(paddingValues)
                            .background(colors.background)
                    )
                }
            }
        }
    }
}