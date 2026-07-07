package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
    val taskListViewModel = remember { TaskListViewModel() }
    val stockViewModel = remember { StockViewModel(MockStockRepository()) }
    var showAddProductScreen by remember { mutableStateOf(false) }

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
                    TaskListScreen(
                        viewModel = taskListViewModel,
                        modifier = Modifier.padding(paddingValues)
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