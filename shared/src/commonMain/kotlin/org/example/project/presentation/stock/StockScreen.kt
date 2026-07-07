package org.example.project.presentation.stock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.stock.Product
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    onAddProductClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val state by viewModel.state.collectAsState()
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 5
    val filteredProducts = state.filteredProducts
    val totalPages = maxOf(1, (filteredProducts.size + pageSize - 1) / pageSize)
    val pagedProducts = filteredProducts
        .drop(currentPage * pageSize)
        .take(pageSize)

    LaunchedEffect(filteredProducts.size) {
        if (currentPage > totalPages - 1) {
            currentPage = totalPages - 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        StockHeader(
            productCount = state.products.size,
            lowStockCount = state.lowStockCount,
            outOfStockCount = state.outOfStockCount,
            searchQuery = state.searchQuery,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onAddProductClick = onAddProductClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        StockTable(
            products = pagedProducts,
            onDeleteProduct = viewModel::deleteProduct,
            onUpdateQuantity = viewModel::updateStockQuantity
        )

        Spacer(modifier = Modifier.height(12.dp))

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

@Composable
private fun StockHeader(
    productCount: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAddProductClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            text = "Stock",
            color = colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$productCount products",
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$lowStockCount low stock",
                color = colors.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$outOfStockCount out of stock",
                color = colors.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text("Search products...", color = colors.onSurfaceVariant)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                cursorColor = colors.primary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddProductClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text("+ Add product")
        }
    }
}

@Composable
private fun StockTable(
    products: List<Product>,
    onDeleteProduct: (Int) -> Unit,
    onUpdateQuantity: (Int, Int, Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 385.dp)
            .border(
                width = 1.dp,
                color = colors.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(12.dp)
                .width(760.dp)
        ) {
            StockTableHeader()

            products.forEach { product ->
                val variant = product.variants.firstOrNull()

                if (variant != null) {
                    StockTableRow(
                        product = product,
                        sku = variant.sku,
                        stockQuantity = variant.stockQuantity,
                        price = variant.price,
                        onDeleteProduct = {
                            onDeleteProduct(product.id)
                        },
                        onUpdateQuantity = { newQuantity ->
                            onUpdateQuantity(
                                product.id,
                                variant.id,
                                newQuantity
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StockTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TableHeaderCell("Product", 220)
        TableHeaderCell("SKU", 150)
        TableHeaderCell("Stock", 120)
        TableHeaderCell("Price", 120)
        TableHeaderCell("Actions", 150)
    }
}

@Composable
private fun StockTableRow(
    product: Product,
    sku: String,
    stockQuantity: Int,
    price: Double,
    onDeleteProduct: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(product.name, 220, colors.onBackground)
        TableCell(sku, 150, colors.onSurfaceVariant)

        TableCell(
            text = if (stockQuantity == 0) "Out of stock" else stockQuantity.toString(),
            width = 120,
            color = when {
                stockQuantity == 0 -> colors.error
                stockQuantity <= 5 -> colors.primary
                else -> AppColorPalette.StatusDone.content
            }
        )

        TableCell("${price} lei", 120, colors.onBackground)

        Row(
            modifier = Modifier.width(150.dp)
        ) {
            Button(
                modifier = Modifier.width(62.dp),
                onClick = {
                    showEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text("Edit")
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                modifier = Modifier.width(84.dp),
                onClick = onDeleteProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text("Delete")
            }
        }
    }

    if (showEditDialog) {
        EditQuantityDialog(
            currentQuantity = stockQuantity,
            onDismiss = {
                showEditDialog = false
            },
            onSave = { newQuantity ->
                onUpdateQuantity(newQuantity)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun StockPagination(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousClick,
            enabled = currentPage > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.outline,
                disabledContentColor = colors.onSurfaceVariant
            )
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.outline,
                disabledContentColor = colors.onSurfaceVariant
            )
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun EditQuantityDialog(
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var quantityText by remember {
        mutableStateOf(currentQuantity.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.onBackground,
        title = {
            Text("Edit")
        },
        text = {
            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    quantityText = it
                },
                label = {
                    Text("Quantity")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onBackground,
                    unfocusedTextColor = colors.onBackground,
                    cursorColor = colors.primary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityText.toIntOrNull()

                    if (quantity != null && quantity >= 0) {
                        onSave(quantity)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: Int
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width.dp)
    )
}

@Composable
private fun TableCell(
    text: String,
    width: Int,
    color: Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width.dp)
    )
}
