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
import org.example.project.data.tasktimeentry.TaskTimeEntryApiService
import org.example.project.domain.task.Task
import org.example.project.presentation.tasks.ActiveTimerViewModel
import org.example.project.presentation.tasks.AddTaskScreen
import org.example.project.presentation.tasks.EditTaskScreen
import org.example.project.presentation.tasks.TaskDetailScreen
import org.example.project.presentation.tasks.TaskDetailViewModel
import feature.stock.data.MockStockRepository
import feature.stock.presentation.AddProductScreen
import org.example.project.presentation.accounts.AddUserScreen
import org.example.project.presentation.accounts.AdminScreen
import org.example.project.presentation.accounts.AdminViewModel
import org.example.project.presentation.stock.StockScreen
import org.example.project.presentation.stock.StockViewModel
import org.example.project.presentation.tasks.TaskListScreen
import org.example.project.presentation.tasks.TaskListViewModel
import org.example.project.domain.project.Project
import org.example.project.presentation.project.AddProjectScreen
import org.example.project.presentation.project.ProjectDetailScreen
import org.example.project.presentation.project.ProjectDetailViewModel
import org.example.project.presentation.project.ProjectListScreen
import org.example.project.presentation.project.ProjectListViewModel
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.chat.ChatScreen
import org.example.project.presentation.chat.ChatViewModel
import org.example.project.data.ApiConfig
import org.example.project.data.accounts.UserApiService
import org.example.project.data.task.TaskApiService
import org.example.project.data.chat.ChatApiService
import org.example.project.data.project.ProjectApiService
import org.example.project.data.createHttpClient
import org.example.project.presentation.dashboard.DashboardScreen
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.LocalAppStrings

@Composable
fun MainScreen(
    user: UserSession,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val apiHttpClient = remember { createHttpClient() }
    val taskApi = remember(user.token) {
        TaskApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val userApi = remember(user.token) {
        UserApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val projectApi = remember(user.token) {
        ProjectApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val taskListViewModel = remember { TaskListViewModel(userApi = userApi, api = taskApi, projectApi = projectApi, currentUserId = user.id) }
    val projectListViewModel = remember { ProjectListViewModel(api = projectApi) }
    var showAddProjectScreen by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    val stockViewModel = remember { StockViewModel(MockStockRepository()) }
    var showAddProductScreen by remember { mutableStateOf(false) }

    val adminViewModel = remember(user.token) {
        AdminViewModel(userApi)
    }

    var showAddUserScreen by remember { mutableStateOf(false) }
    var showUserDetail by remember { mutableStateOf(false) }
    var showAddTaskScreen by remember { mutableStateOf(false) }
    var showEditTaskScreen by remember { mutableStateOf(false) }
    val taskTimeEntryApi = remember(user.token) {
        TaskTimeEntryApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val activeTimerViewModel = remember { ActiveTimerViewModel(timeEntryApi = taskTimeEntryApi) }
    val chatApi = remember(user.token) {
        ChatApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val chatViewModel = remember(user.id) { ChatViewModel(currentEmployeeId = user.id, userApi = userApi, chatApi = chatApi) }

    val strings = LocalAppStrings.current

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
                    selectedProject = null
                    showAddTaskScreen = false
                    showEditTaskScreen = false
                    showAddProjectScreen = false
                    showAddProductScreen = false
                    showAddUserScreen = false
                    scope.launch { drawerState.close() }
                },
            )
        }
    ) {
        Scaffold(
            containerColor = AppColorPalette.Background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                AppTopBar(
                    title = strings.text(selectedSection.title),
                    user = user,
                    activeTimerViewModel = activeTimerViewModel,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onProfileClick = { showUserDetail = true },
                    onOpenActiveTask = { task ->
                        selectedSection = AppSection.TASKS
                        selectedTask = task
                        selectedProject = null
                        showEditTaskScreen = false
                        showAddTaskScreen = false
                        showAddProjectScreen = false
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
                        selectedProject = null
                        showAddTaskScreen = false
                        showEditTaskScreen = false
                        showAddProjectScreen = false
                        showAddProductScreen = false
                        showAddUserScreen = false
                    }
                )
            }
        ) { paddingValues ->
            when (selectedSection) {
                AppSection.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = taskListViewModel,
                        user = user,
                        onTaskClick = { task ->
                            selectedSection = AppSection.TASKS
                            selectedTask = task
                            selectedProject = null
                            showEditTaskScreen = false
                            showAddTaskScreen = false
                            showAddProjectScreen = false
                            showAddProductScreen = false
                            showAddUserScreen = false
                        },
                        modifier = Modifier.padding(paddingValues),
                    )
                }

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
                                userApi = userApi,
                                projectApi = projectApi,
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
                    val project = selectedProject
                    val task = selectedTask

                    if (task != null) {
                        val taskDetailViewModel = remember(task.id) {
                            TaskDetailViewModel(
                                task = task,
                                employeeId = user.id,
                                activeTimerViewModel = activeTimerViewModel,
                                taskApi = taskApi,
                                timeEntryApi = taskTimeEntryApi,
                                userApi = userApi,
                                projectApi = projectApi,
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
                                onBack = { selectedTask = null },
                                onEditClick = { showEditTaskScreen = true },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                    } else if (project != null) {
                        val projectDetailViewModel = remember(project.id) {
                            ProjectDetailViewModel(project = project, taskApi = taskApi)
                        }
                        if (showAddTaskScreen) {
                            AddTaskScreen(
                                viewModel = taskListViewModel,
                                projectId = project.id,
                                onTaskAdded = {
                                    showAddTaskScreen = false
                                    projectDetailViewModel.loadTasks()
                                },
                                onBackClick = { showAddTaskScreen = false },
                                modifier = Modifier.padding(paddingValues),
                            )
                        } else {
                            ProjectDetailScreen(
                                viewModel = projectDetailViewModel,
                                project = project,
                                onBack = { selectedProject = null },
                                onTaskClick = { selectedTask = it },
                                onAddTaskClick = { showAddTaskScreen = true },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                    } else if (showAddProjectScreen) {
                        AddProjectScreen(
                            viewModel = projectListViewModel,
                            onProjectAdded = { showAddProjectScreen = false },
                            onBackClick = { showAddProjectScreen = false },
                            modifier = Modifier.padding(paddingValues),
                        )
                    } else {
                        ProjectListScreen(
                            viewModel = projectListViewModel,
                            onProjectClick = { selectedProject = it },
                            onAddProjectClick = { showAddProjectScreen = true },
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
                        title = strings.text(selectedSection.title),
                        modifier = Modifier
                            .padding(paddingValues)
                            .background(AppColorPalette.Background)
                    )
                }
            }

            if (showUserDetail) {
                UserDetailDialog(
                    user = user,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onDismiss = { showUserDetail = false },
                    onLogout = onLogout,
                )
            }
        }
    }
}
