package org.example.project.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.orders.Order
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    var selectedOrderId by remember {
        mutableStateOf<Int?>(null)
    }

    var currentPage by remember {
        mutableStateOf(0)
    }

    val filteredOrders = state.filteredOrders

    val selectedOrder = selectedOrderId?.let { id ->
        state.orders.firstOrNull { order ->
            order.id == id
        }
    }

    DismissKeyboardOnTapOutside(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding),
        ) {
        OrdersHeader(
            orderCount = state.orderCount,
            searchQuery = state.searchQuery,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onSearchQueryChanged = viewModel::onSearchQueryChanged
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        if (state.isLoading) {
            LoadingOrdersContent()
        } else {
            BoxWithConstraints(
                modifier = Modifier.weight(1f)
            ) {
                val tableHeaderHeight = 0.dp
                val rowHeight = 92.dp
                val paginationHeight = 56.dp
                val spacing = AppDimensions.SmallSpacing

                val availableForRows = maxHeight -
                        tableHeaderHeight -
                        paginationHeight -
                        spacing

                val pageSize = maxOf(
                    1,
                    (availableForRows.value / rowHeight.value).toInt()
                )

                val totalPages = maxOf(
                    1,
                    (filteredOrders.size + pageSize - 1) / pageSize
                )

                val pagedOrders = filteredOrders
                    .drop(currentPage * pageSize)
                    .take(pageSize)

                LaunchedEffect(filteredOrders.size, pageSize) {
                    if (currentPage > totalPages - 1) {
                        currentPage = totalPages - 1
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OrderTable(
                        orders = pagedOrders,
                        onOrderClick = { order: Order ->
                            selectedOrderId = order.id
                        }
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

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

    if (selectedOrder != null) {
        OrderDetailsDialog(
            order = selectedOrder,
            onDismiss = {
                selectedOrderId = null
            }
        )
    }
}

@Composable
private fun LoadingOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppColorPalette.Primary
        )
    }
}
