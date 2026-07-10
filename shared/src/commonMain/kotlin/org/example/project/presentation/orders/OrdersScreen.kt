package org.example.project.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.domain.orders.Order
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
            OrderTable(
                orders = filteredOrders,
                onOrderClick = { order: Order ->
                    selectedOrderId = order.id
                },
                modifier = Modifier.weight(1f),
            )
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
