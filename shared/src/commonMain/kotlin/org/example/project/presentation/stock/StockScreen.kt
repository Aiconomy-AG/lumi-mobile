package org.example.project.presentation.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    onAddProductClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var selectedProductId by remember {
        mutableStateOf<Int?>(null)
    }

    var currentPage by remember {
        mutableStateOf(0)
    }
    val pageSize = 7

    val filteredProducts = state.filteredProducts

    val totalPages = maxOf(
        1,
        (filteredProducts.size + pageSize - 1) / pageSize
    )

    val pagedProducts = filteredProducts
        .drop(currentPage * pageSize)
        .take(pageSize)

    LaunchedEffect(filteredProducts.size) {
        if (currentPage > totalPages - 1) {
            currentPage = totalPages - 1
        }
    }


    val selectedProduct = selectedProductId?.let { id ->
        state.products.firstOrNull { product ->
            product.id == id
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .padding(AppDimensions.ScreenPadding)
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
            BoxWithConstraints(
                modifier = Modifier.weight(1f)
            ) {
                val tableHeaderHeight = 36.dp
                val rowHeight = 54.dp
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
                    (filteredProducts.size + pageSize - 1) / pageSize
                )

                val pagedProducts = filteredProducts
                    .drop(currentPage * pageSize)
                    .take(pageSize)

                LaunchedEffect(filteredProducts.size, pageSize) {
                    if (currentPage > totalPages - 1) {
                        currentPage = totalPages - 1
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProductTable(
                        products = pagedProducts,
                        onProductClick = { product ->
                            selectedProductId = product.id
                        }
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                    StockPagination(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPreviousClick = {
                            if (currentPage > 0) {
                                currentPage--
                            }
                        },
                        onNextClick = {
                            if (currentPage < totalPages - 1) {
                                currentPage++
                            }
                        }
                    )
                }
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