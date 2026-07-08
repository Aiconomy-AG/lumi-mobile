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
import org.example.project.data.employee.EmployeeMockApiService
import org.example.project.data.task.TaskMockApiService
import org.example.project.data.tasktimeentry.TaskTimeEntryMockApiService
import org.example.project.domain.task.Task
import org.example.project.presentation.tasks.ActiveTimerViewModel
import org.example.project.presentation.tasks.AddTaskScreen
import org.example.project.presentation.tasks.EditTaskScreen
import org.example.project.presentation.tasks.TaskDetailScreen
import org.example.project.presentation.tasks.TaskDetailViewModel
import feature.stock.data.MockStockRepository
import feature.stock.presentation.AddProductScreen
import org.example.project.data.accounts.MockUserRepository
import org.example.project.presentation.accounts.AddUserScreen
import org.example.project.presentation.accounts.AdminScreen
import org.example.project.presentation.accounts.AdminViewModel
import org.example.project.presentation.stock.StockScreen
import org.example.project.presentation.stock.StockViewModel
import org.example.project.presentation.tasks.TaskListScreen
import org.example.project.presentation.tasks.TaskListViewModel
import org.example.project.presentation.project.ProjectListScreen
import org.example.project.presentation.project.ProjectListViewModel
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.chat.ChatScreen
import org.example.project.presentation.chat.ChatViewModel

@Composable
fun MainScreen(
    user: UserSession,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val taskApi = remember { TaskMockApiService() }
    val employeeApi = remember { EmployeeMockApiService() }
    val taskListViewModel = remember { TaskListViewModel(api = taskApi, employeeApi = employeeApi, currentUserId = user.id) }
    val projectListViewModel = remember { ProjectListViewModel() }
    val stockViewModel = remember { StockViewModel(MockStockRepository()) }
    var showAddProductScreen by remember { mutableStateOf(false) }

    val adminViewModel = remember { AdminViewModel(MockUserRepository()) }
    var showAddUserScreen by remember { mutableStateOf(false) }
    var showAddTaskScreen by remember { mutableStateOf(false) }
    var showEditTaskScreen by remember { mutableStateOf(false) }
    val taskTimeEntryApi = remember(user.id) { TaskTimeEntryMockApiService(employeeId = user.id) }
    val activeTimerViewModel = remember { ActiveTimerViewModel(timeEntryApi = taskTimeEntryApi) }
    val chatViewModel = remember(user.id) { ChatViewModel(currentEmployeeId = user.id) }

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
                    showAddTaskScreen = false
                    showEditTaskScreen = false
                    showAddProductScreen = false
                    showAddUserScreen = false
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = AppColorPalette.Background,
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
                        showEditTaskScreen = false
                        showAddTaskScreen = false
                        showAddProductScreen = false
                        showAddUserScreen = false
                    },
                )
            },
            bottomBar = {
                AppBottomBar(
                    sections = availableSections,
                    selectedSection = selectedSection,
                    onSectionSelected = { section ->
                        selectedSection = section
                        selectedTask = null
                        showAddTaskScreen = false
                        showEditTaskScreen = false
                        showAddProductScreen = false
                        showAddUserScreen = false
                    }
                )
            }
        ) { paddingValues ->
            when (selectedSection) {
                AppSection.TASKS -> {
                    val task = selectedTask

                    if (task == null) {
                        if (showAddTaskScreen) {
                            AddTaskScreen(
                                viewModel = taskListViewModel,
                                onTaskAdded = {
                                    showAddTaskScreen = false
                                },
                                onBackClick = {
                                    showAddTaskScreen = false
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        } else {
                            TaskListScreen(
                                viewModel = taskListViewModel,
                                onTaskClick = {
                                    selectedTask = it
                                },
                                onAddTaskClick = {
                                    showAddTaskScreen = true
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    } else {
                        val taskDetailViewModel = remember(task.id) {
                            TaskDetailViewModel(
                                task = task,
                                employeeId = user.id,
                                activeTimerViewModel = activeTimerViewModel,
                                taskApi = taskApi,
                                timeEntryApi = taskTimeEntryApi,
                                employeeApi = employeeApi,
                            )
                        }

                        if (showEditTaskScreen) {
                            EditTaskScreen(
                                viewModel = taskDetailViewModel,
                                onTaskUpdated = { showEditTaskScreen = false },
                                onBackClick = { showEditTaskScreen = false },
                                modifier = Modifier.padding(paddingValues),
                            )
                        } else {
                            TaskDetailScreen(
                                viewModel = taskDetailViewModel,
                                onBack = {
                                    selectedTask = null
                                    taskListViewModel.loadTasks()
                                },
                                onEditClick = { showEditTaskScreen = true },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                    }
                }

                AppSection.PROJECTS -> {
                    ProjectListScreen(
                        viewModel = projectListViewModel,
                        modifier = Modifier.padding(paddingValues),
                    )
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

                AppSection.CHAT -> {
                    ChatScreen(
                        viewModel = chatViewModel,
                        currentEmployeeId = user.id,
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.ADMIN -> {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        if (showAddUserScreen) {
                            AddUserScreen(
                                viewModel = adminViewModel,
                                onUserAdded = {
                                    showAddUserScreen = false
                                },
                                onBackClick = {
                                    showAddUserScreen = false
                                }
                            )
                        } else {
                            AdminScreen(
                                viewModel = adminViewModel,
                                onAddUserClick = {
                                    showAddUserScreen = true
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
                            .background(AppColorPalette.Background)
                    )
                }
            }
        }
    }
}