package org.example.project.presentation.stock

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
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    onAddProductClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var selectedProductId by remember {
        mutableStateOf<Int?>(null)
    }

    val filteredProducts = state.filteredProducts

    val selectedProduct = selectedProductId?.let { id ->
        state.products.firstOrNull { product ->
            product.id == id
        }
    }


    DismissKeyboardOnTapOutside(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding),
        ) {
        StockHeader(
            productCount = state.productCount,
            variantCount = state.variantCount,
            lowStockCount = state.lowStockCount,
            outOfStockCount = state.outOfStockCount,
            searchQuery = state.searchQuery,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onAddProductClick = onAddProductClick
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        if (state.isLoading) {
            LoadingStockContent()
        } else {
            ProductTable(
                products = filteredProducts,
                onProductClick = { product ->
                    selectedProductId = product.id
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
    }

    if (selectedProduct != null) {
        ProductDetailsDialog(
            product = selectedProduct,
            categories = state.categories,
            isSaving = state.isSaving,
            onDismiss = {
                selectedProductId = null
            },
            onUpdateProduct = { productId, name, description, imageUrl, sku, price, stockQuantity, categoryId ->
                viewModel.updateProduct(
                    productId = productId,
                    name = name,
                    description = description,
                    imageUrl = imageUrl,
                    sku = sku,
                    price = price,
                    stockQuantity = stockQuantity,
                    categoryId = categoryId
                )
            },
            onUpdateVariant = { productId, variantId, sku, name, colour, weight, weightUnit, price, stockQuantity ->
                viewModel.updateProductVariant(
                    productId = productId,
                    variantId = variantId,
                    sku = sku,
                    name = name,
                    colour = colour,
                    weight = weight,
                    weightUnit = weightUnit,
                    price = price,
                    stockQuantity = stockQuantity
                )
            },
            onAddVariant = { productId, sku, name, colour, weight, weightUnit, price, stockQuantity ->
                viewModel.addProductVariant(
                    productId = productId,
                    sku = sku,
                    name = name,
                    colour = colour,
                    weight = weight,
                    weightUnit = weightUnit,
                    price = price,
                    stockQuantity = stockQuantity
                )
            },
            onDeleteVariant = { productId, variantId ->
                viewModel.deleteProductVariant(
                    productId = productId,
                    variantId = variantId
                )
            },
            onDeleteProduct = { productId ->
                viewModel.deleteProduct(productId)
                selectedProductId = null
            }
        )
    }
}


@Composable
private fun LoadingStockContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppColorPalette.Primary
        )
    }
}