package features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import feature.stock.presentation.AddProductScreen
import kotlinx.coroutines.launch
import org.example.project.data.ApiConfig
import org.example.project.data.accounts.UserApiService
import org.example.project.data.auditlogs.AuditLogApiService
import org.example.project.data.auth.AuthApiService
import org.example.project.data.auth.UserSession
import org.example.project.data.chat.ChatApiService
import org.example.project.data.chat.ReverbChatRealtimeService
import org.example.project.data.createHttpClient
import org.example.project.data.orders.OrdersApiService
import org.example.project.data.project.ProjectApiService
import org.example.project.data.returns.ReturnsApiService
import org.example.project.data.stock.StockApiService
import org.example.project.data.task.TaskApiService
import org.example.project.data.tasktimeentry.TaskTimeEntryApiService
import org.example.project.domain.auth.UserRole
import org.example.project.domain.project.Project
import org.example.project.domain.task.Task
import org.example.project.notifications.NotificationRouter
import org.example.project.presentation.accounts.AddUserScreen
import org.example.project.presentation.accounts.AdminScreen
import org.example.project.presentation.accounts.AdminViewModel
import org.example.project.presentation.auditlogs.AuditLogsScreen
import org.example.project.presentation.auditlogs.AuditLogsViewModel
import org.example.project.presentation.chat.ChatScreen
import org.example.project.presentation.chat.ChatViewModel
import org.example.project.presentation.components.PlatformBackHandler
import org.example.project.presentation.dashboard.DashboardScreen
import org.example.project.presentation.localization.AppLanguage
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.orders.OrdersScreen
import org.example.project.presentation.orders.OrdersViewModel
import org.example.project.presentation.project.AddProjectScreen
import org.example.project.presentation.project.ProjectDetailScreen
import org.example.project.presentation.project.ProjectDetailViewModel
import org.example.project.presentation.project.ProjectListScreen
import org.example.project.presentation.project.ProjectListViewModel
import org.example.project.presentation.returns.ReturnsScreen
import org.example.project.presentation.returns.ReturnsViewModel
import org.example.project.presentation.stock.StockScreen
import org.example.project.presentation.stock.StockViewModel
import org.example.project.presentation.tasks.ActiveTimerViewModel
import org.example.project.presentation.tasks.AddTaskScreen
import org.example.project.presentation.tasks.EditTaskScreen
import org.example.project.presentation.tasks.TaskDetailScreen
import org.example.project.presentation.tasks.TaskDetailViewModel
import org.example.project.presentation.tasks.TaskListScreen
import org.example.project.presentation.tasks.TaskListViewModel
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun MainScreen(
    user: UserSession,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onPhoneNumberUpdated: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var selectedSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    var subRouteStack by remember { mutableStateOf<List<MainSubRoute>>(emptyList()) }
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
    val taskListViewModel = remember {
        TaskListViewModel(
            userApi = userApi,
            api = taskApi,
            projectApi = projectApi,
            currentUserId = user.id,
        )
    }
    val projectListViewModel = remember { ProjectListViewModel(api = projectApi) }

    val adminViewModel = remember(user.token) {
        AdminViewModel(userApi)
    }

    val stockViewModel = remember(user.token) {
        StockViewModel(
            StockApiService(
                client = apiHttpClient,
                baseUrl = ApiConfig.BASE_URL,
                token = user.token,
            ),
        )
    }
    val ordersViewModel = remember(user.token) {
        OrdersViewModel(
            OrdersApiService(
                client = apiHttpClient,
                baseUrl = ApiConfig.BASE_URL,
                token = user.token,
            ),
        )
    }
    val returnsViewModel = remember(user.token) {
        ReturnsViewModel(
            ReturnsApiService(
                client = apiHttpClient,
                baseUrl = ApiConfig.BASE_URL,
                token = user.token,
            ),
        )
    }

    val auditLogsViewModel = remember(user.token) {
        AuditLogsViewModel(
            AuditLogApiService(
                client = apiHttpClient,
                baseUrl = ApiConfig.BASE_URL,
                token = user.token,
            ),
        )
    }

    var showUserDetail by remember { mutableStateOf(false) }
    val taskTimeEntryApi = remember(user.token) {
        TaskTimeEntryApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val activeTimerViewModel = remember { ActiveTimerViewModel(timeEntryApi = taskTimeEntryApi) }
    val chatApi = remember(user.token) {
        ChatApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL, token = user.token)
    }
    val chatRealtimeApi = remember(user.token) {
        ReverbChatRealtimeService(
            client = apiHttpClient,
            baseUrl = ApiConfig.BASE_URL,
            appKey = ApiConfig.REVERB_APP_KEY,
            host = ApiConfig.REVERB_HOST,
            port = ApiConfig.REVERB_PORT,
            scheme = ApiConfig.REVERB_SCHEME,
            token = user.token,
        )
    }
    val chatViewModel = remember(user.id, user.token) {
        ChatViewModel(
            currentEmployeeId = user.id,
            userApi = userApi,
            chatApi = chatApi,
            chatRealtimeApi = chatRealtimeApi,
        )
    }
    val authRepository = remember(apiHttpClient) {
        AuthApiService(client = apiHttpClient, baseUrl = ApiConfig.BASE_URL)
    }

    val strings = LocalAppStrings.current
    val pendingDeepLink by NotificationRouter.pending.collectAsState()
    val chatUiState by chatViewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun navigateToMainSection(section: AppSection) {
        selectedSection = section
        subRouteStack = emptyList()
    }

    fun pushSubRoute(route: MainSubRoute) {
        subRouteStack = subRouteStack + route
    }

    fun popSubRoute() {
        if (subRouteStack.isNotEmpty()) {
            subRouteStack = subRouteStack.dropLast(1)
        }
    }

    fun currentTaskDetail(): MainSubRoute.TaskDetail? =
        subRouteStack.filterIsInstance<MainSubRoute.TaskDetail>().lastOrNull()

    fun currentProjectDetail(): MainSubRoute.ProjectDetail? =
        subRouteStack.filterIsInstance<MainSubRoute.ProjectDetail>().lastOrNull()

    suspend fun openTaskById(taskId: Int) {
        navigateToMainSection(AppSection.TASKS)

        var task = taskListViewModel.uiState.value.tasks.find { it.id == taskId }
        if (task == null) {
            try {
                task = taskApi.getTask(taskId)
                taskListViewModel.loadTasks()
            } catch (_: Exception) {
            }
        }
        task?.let { pushSubRoute(MainSubRoute.TaskDetail(it)) }
    }

    LaunchedEffect(pendingDeepLink) {
        val link = pendingDeepLink ?: return@LaunchedEffect

        when (link.type) {
            "task_assigned",
            "task_status_changed",
            "task_unassigned" -> {
                link.taskId?.let { openTaskById(it) }
            }

            "chat_message_received" -> {
                navigateToMainSection(AppSection.CHAT)
                link.conversationId?.let { chatViewModel.openConversationById(it) }
            }
        }

        NotificationRouter.consume()
    }

    val hasChatConversation = selectedSection == AppSection.CHAT && chatUiState.selectedConversation != null

    PlatformBackHandler(
        enabled = showUserDetail || hasChatConversation || subRouteStack.isNotEmpty(),
    ) {
        when {
            showUserDetail -> showUserDetail = false
            hasChatConversation -> chatViewModel.backToConversationList()
            subRouteStack.isNotEmpty() -> popSubRoute()
        }
    }

    val availableSections = AppSection.entries.filter {
        !it.adminOnly || user.role == UserRole.ADMIN
    }
    val bottomBarSections = availableSections.filter { it.showInBottomBar }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                user = user,
                sections = availableSections,
                selectedSection = selectedSection,
                onSectionSelected = { section ->
                    navigateToMainSection(section)
                    scope.launch { drawerState.close() }
                },
            )
        },
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
                        navigateToMainSection(AppSection.TASKS)
                        pushSubRoute(MainSubRoute.TaskDetail(task))
                    },
                )
            },
            bottomBar = {
                AppBottomBar(
                    sections = bottomBarSections,
                    selectedSection = selectedSection,
                    onSectionSelected = { section ->
                        navigateToMainSection(section)
                    },
                )
            },
        ) { paddingValues ->
            when (selectedSection) {
                AppSection.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = taskListViewModel,
                        user = user,
                        onTaskClick = { task ->
                            navigateToMainSection(AppSection.TASKS)
                            pushSubRoute(MainSubRoute.TaskDetail(task))
                        },
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.TASKS -> {
                    TasksSectionContent(
                        subRouteStack = subRouteStack,
                        paddingValues = paddingValues,
                        taskListViewModel = taskListViewModel,
                        user = user,
                        taskApi = taskApi,
                        taskTimeEntryApi = taskTimeEntryApi,
                        userApi = userApi,
                        projectApi = projectApi,
                        activeTimerViewModel = activeTimerViewModel,
                        onPushSubRoute = ::pushSubRoute,
                        onPopSubRoute = ::popSubRoute,
                        currentTaskDetail = ::currentTaskDetail,
                    )
                }

                AppSection.PROJECTS -> {
                    ProjectsSectionContent(
                        subRouteStack = subRouteStack,
                        paddingValues = paddingValues,
                        taskListViewModel = taskListViewModel,
                        projectListViewModel = projectListViewModel,
                        user = user,
                        taskApi = taskApi,
                        taskTimeEntryApi = taskTimeEntryApi,
                        userApi = userApi,
                        projectApi = projectApi,
                        activeTimerViewModel = activeTimerViewModel,
                        onPushSubRoute = ::pushSubRoute,
                        onPopSubRoute = ::popSubRoute,
                        currentTaskDetail = ::currentTaskDetail,
                        currentProjectDetail = ::currentProjectDetail,
                    )
                }

                AppSection.STOCK -> {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        if (subRouteStack.lastOrNull() is MainSubRoute.AddProduct) {
                            AddProductScreen(
                                viewModel = stockViewModel,
                                onProductAdded = { popSubRoute() },
                                onBackClick = { popSubRoute() },
                            )
                        } else {
                            StockScreen(
                                viewModel = stockViewModel,
                                onAddProductClick = { pushSubRoute(MainSubRoute.AddProduct) },
                            )
                        }
                    }
                }

                AppSection.ORDERS -> {
                    OrdersScreen(
                        viewModel = ordersViewModel,
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.RETURNS -> {
                    ReturnsScreen(
                        viewModel = returnsViewModel,
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.CHAT -> {
                    ChatScreen(
                        viewModel = chatViewModel,
                        currentEmployeeId = user.id,
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.AUDIT_LOGS -> {
                    AuditLogsScreen(
                        viewModel = auditLogsViewModel,
                        modifier = Modifier.padding(paddingValues),
                    )
                }

                AppSection.ADMIN -> {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        if (subRouteStack.lastOrNull() is MainSubRoute.AddUser) {
                            AddUserScreen(
                                viewModel = adminViewModel,
                                onUserAdded = { popSubRoute() },
                                onBackClick = { popSubRoute() },
                            )
                        } else {
                            AdminScreen(
                                viewModel = adminViewModel,
                                onAddUserClick = { pushSubRoute(MainSubRoute.AddUser) },
                            )
                        }
                    }
                }
            }

            if (showUserDetail) {
                UserDetailDialog(
                    user = user,
                    selectedLanguage = selectedLanguage,
                    authRepository = authRepository,
                    onLanguageSelected = onLanguageSelected,
                    onPhoneNumberUpdated = onPhoneNumberUpdated,
                    onDismiss = { showUserDetail = false },
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun TasksSectionContent(
    subRouteStack: List<MainSubRoute>,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    taskListViewModel: TaskListViewModel,
    user: UserSession,
    taskApi: TaskApiService,
    taskTimeEntryApi: TaskTimeEntryApiService,
    userApi: UserApiService,
    projectApi: ProjectApiService,
    activeTimerViewModel: ActiveTimerViewModel,
    onPushSubRoute: (MainSubRoute) -> Unit,
    onPopSubRoute: () -> Unit,
    currentTaskDetail: () -> MainSubRoute.TaskDetail?,
) {
    when (subRouteStack.lastOrNull()) {
        is MainSubRoute.EditTask -> {
            val detail = currentTaskDetail()
            if (detail != null) {
                TaskDetailRoute(
                    task = detail.task,
                    paddingValues = paddingValues,
                    user = user,
                    taskApi = taskApi,
                    taskTimeEntryApi = taskTimeEntryApi,
                    userApi = userApi,
                    projectApi = projectApi,
                    activeTimerViewModel = activeTimerViewModel,
                    showEdit = true,
                    onBack = onPopSubRoute,
                    onEditClick = {},
                    onSubtaskClick = { subtask ->
                        onPopSubRoute()
                        onPushSubRoute(MainSubRoute.TaskDetail(subtask))
                    },
                    onTaskUpdated = onPopSubRoute,
                    onDelete = {
                        onPopSubRoute()
                        onPopSubRoute()
                        taskListViewModel.loadTasks()
                    },
                )
            }
        }

        is MainSubRoute.TaskDetail -> {
            val detail = subRouteStack.last() as MainSubRoute.TaskDetail
            TaskDetailRoute(
                task = detail.task,
                paddingValues = paddingValues,
                user = user,
                taskApi = taskApi,
                taskTimeEntryApi = taskTimeEntryApi,
                userApi = userApi,
                projectApi = projectApi,
                activeTimerViewModel = activeTimerViewModel,
                showEdit = false,
                onBack = {
                    onPopSubRoute()
                    taskListViewModel.loadTasks()
                },
                onEditClick = { onPushSubRoute(MainSubRoute.EditTask) },
                onSubtaskClick = { subtask ->
                    onPushSubRoute(MainSubRoute.TaskDetail(subtask))
                },
                onTaskUpdated = onPopSubRoute,
                onDelete = {
                    onPopSubRoute()
                    taskListViewModel.loadTasks()
                },
            )
        }

        is MainSubRoute.AddTask -> {
            AddTaskScreen(
                viewModel = taskListViewModel,
                onTaskAdded = onPopSubRoute,
                onBackClick = onPopSubRoute,
                modifier = Modifier.padding(paddingValues),
            )
        }

        else -> {
            TaskListScreen(
                viewModel = taskListViewModel,
                onTaskClick = { onPushSubRoute(MainSubRoute.TaskDetail(it)) },
                onAddTaskClick = { onPushSubRoute(MainSubRoute.AddTask) },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun ProjectsSectionContent(
    subRouteStack: List<MainSubRoute>,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    taskListViewModel: TaskListViewModel,
    projectListViewModel: ProjectListViewModel,
    user: UserSession,
    taskApi: TaskApiService,
    taskTimeEntryApi: TaskTimeEntryApiService,
    userApi: UserApiService,
    projectApi: ProjectApiService,
    activeTimerViewModel: ActiveTimerViewModel,
    onPushSubRoute: (MainSubRoute) -> Unit,
    onPopSubRoute: () -> Unit,
    currentTaskDetail: () -> MainSubRoute.TaskDetail?,
    currentProjectDetail: () -> MainSubRoute.ProjectDetail?,
) {
    when (subRouteStack.lastOrNull()) {
        is MainSubRoute.EditTask -> {
            val detail = currentTaskDetail()
            if (detail != null) {
                TaskDetailRoute(
                    task = detail.task,
                    paddingValues = paddingValues,
                    user = user,
                    taskApi = taskApi,
                    taskTimeEntryApi = taskTimeEntryApi,
                    userApi = userApi,
                    projectApi = projectApi,
                    activeTimerViewModel = activeTimerViewModel,
                    showEdit = true,
                    onBack = onPopSubRoute,
                    onEditClick = {},
                    onSubtaskClick = { subtask ->
                        onPopSubRoute()
                        onPushSubRoute(MainSubRoute.TaskDetail(subtask))
                    },
                    onTaskUpdated = onPopSubRoute,
                    onDelete = {
                        onPopSubRoute()
                        onPopSubRoute()
                    },
                )
            }
        }

        is MainSubRoute.TaskDetail -> {
            val detail = subRouteStack.last() as MainSubRoute.TaskDetail
            TaskDetailRoute(
                task = detail.task,
                paddingValues = paddingValues,
                user = user,
                taskApi = taskApi,
                taskTimeEntryApi = taskTimeEntryApi,
                userApi = userApi,
                projectApi = projectApi,
                activeTimerViewModel = activeTimerViewModel,
                showEdit = false,
                onBack = onPopSubRoute,
                onEditClick = { onPushSubRoute(MainSubRoute.EditTask) },
                onSubtaskClick = { subtask ->
                    onPushSubRoute(MainSubRoute.TaskDetail(subtask))
                },
                onTaskUpdated = onPopSubRoute,
                onDelete = onPopSubRoute,
            )
        }

        is MainSubRoute.AddTask -> {
            val projectDetail = currentProjectDetail()
            val projectDetailViewModel = projectDetail?.let { detail ->
                remember(detail.project.id) {
                    ProjectDetailViewModel(project = detail.project, taskApi = taskApi)
                }
            }
            AddTaskScreen(
                viewModel = taskListViewModel,
                projectId = projectDetail?.project?.id ?: 0,
                onTaskAdded = {
                    projectDetailViewModel?.loadTasks()
                    onPopSubRoute()
                },
                onBackClick = onPopSubRoute,
                modifier = Modifier.padding(paddingValues),
            )
        }

        is MainSubRoute.ProjectDetail -> {
            val detail = subRouteStack.last() as MainSubRoute.ProjectDetail
            val projectDetailViewModel = remember(detail.project.id) {
                ProjectDetailViewModel(project = detail.project, taskApi = taskApi)
            }
            ProjectDetailScreen(
                viewModel = projectDetailViewModel,
                project = detail.project,
                onBack = onPopSubRoute,
                onTaskClick = { onPushSubRoute(MainSubRoute.TaskDetail(it)) },
                onAddTaskClick = { onPushSubRoute(MainSubRoute.AddTask) },
                modifier = Modifier.padding(paddingValues),
            )
        }

        is MainSubRoute.AddProject -> {
            AddProjectScreen(
                viewModel = projectListViewModel,
                onProjectAdded = onPopSubRoute,
                onBackClick = onPopSubRoute,
                modifier = Modifier.padding(paddingValues),
            )
        }

        else -> {
            ProjectListScreen(
                viewModel = projectListViewModel,
                onProjectClick = { onPushSubRoute(MainSubRoute.ProjectDetail(it)) },
                onAddProjectClick = { onPushSubRoute(MainSubRoute.AddProject) },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun TaskDetailRoute(
    task: Task,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    user: UserSession,
    taskApi: TaskApiService,
    taskTimeEntryApi: TaskTimeEntryApiService,
    userApi: UserApiService,
    projectApi: ProjectApiService,
    activeTimerViewModel: ActiveTimerViewModel,
    showEdit: Boolean,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onSubtaskClick: (Task) -> Unit,
    onTaskUpdated: () -> Unit,
    onDelete: () -> Unit,
) {
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

    if (showEdit) {
        EditTaskScreen(
            viewModel = taskDetailViewModel,
            onTaskUpdated = onTaskUpdated,
            onBackClick = onBack,
            onDelete = onDelete,
            modifier = Modifier.padding(paddingValues),
        )
    } else {
        TaskDetailScreen(
            viewModel = taskDetailViewModel,
            onBack = onBack,
            onEditClick = onEditClick,
            onSubtaskClick = onSubtaskClick,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
