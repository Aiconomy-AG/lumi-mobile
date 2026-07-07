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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.stock.Product

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    onAddProductClick: () -> Unit
) {
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
            .background(Color(0xFF0B0B0B))
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
    Column {
        Text(
            text = "Stock",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$productCount products",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$lowStockCount low stock",
                color = Color(0xFFF5B11B)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$outOfStockCount out of stock",
                color = Color(0xFFFF5C5C)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text("Search products...", color = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFF5B11B),
                focusedBorderColor = Color(0xFFF5B11B),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color(0xFFF5B11B),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddProductClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5B11B),
                contentColor = Color(0xFF0B0B0B)
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
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 390.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color(0xFF121212),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 26.dp)
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

        HorizontalScrollBar(
            scrollValue = horizontalScrollState.value,
            maxScrollValue = horizontalScrollState.maxValue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HorizontalScrollBar(
    scrollValue: Int,
    maxScrollValue: Int,
    modifier: Modifier = Modifier
) {
    if (maxScrollValue <= 0) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        val density = LocalDensity.current
        val trackWidth = maxWidth
        val trackWidthPx = with(density) { trackWidth.toPx() }
        val contentWidthPx = trackWidthPx + maxScrollValue
        val thumbWidth = (trackWidth * (trackWidthPx / contentWidthPx)).coerceAtLeast(40.dp)
        val maxThumbOffset = trackWidth - thumbWidth
        val scrollProgress = scrollValue.toFloat() / maxScrollValue.toFloat()
        val thumbOffset = maxThumbOffset * scrollProgress

        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(thumbWidth)
                .fillMaxHeight()
                .background(
                    color = Color(0xFFF5B11B),
                    shape = RoundedCornerShape(6.dp)
                )
        )
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
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(product.name, 220, Color.White)
        TableCell(sku, 150, Color.Gray)

        TableCell(
            text = if (stockQuantity == 0) "Out of stock" else stockQuantity.toString(),
            width = 120,
            color = when {
                stockQuantity == 0 -> Color(0xFFFF5C5C)
                stockQuantity <= 5 -> Color(0xFFF5B11B)
                else -> Color(0xFF00D084)
            }
        )

        TableCell("${price} lei", 120, Color.White)

        Row(
            modifier = Modifier.width(150.dp)
        ) {
            Button(
                modifier = Modifier.width(62.dp),
                onClick = {
                    showEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5B11B),
                    contentColor = Color(0xFF0B0B0B)
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
                    containerColor = Color(0xFFF5B11B),
                    contentColor = Color(0xFF0B0B0B)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousClick,
            enabled = currentPage > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5B11B),
                contentColor = Color(0xFF0B0B0B),
                disabledContainerColor = Color(0xFF2A2A2A),
                disabledContentColor = Color.Gray
            )
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5B11B),
                contentColor = Color(0xFF0B0B0B),
                disabledContainerColor = Color(0xFF2A2A2A),
                disabledContentColor = Color.Gray
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
    var quantityText by remember {
        mutableStateOf(currentQuantity.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        titleContentColor = Color.White,
        textContentColor = Color.White,
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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFF5B11B),
                    focusedBorderColor = Color(0xFFF5B11B),
                    unfocusedBorderColor = Color(0xFF2A2A2A),
                    focusedLabelColor = Color(0xFFF5B11B),
                    unfocusedLabelColor = Color.Gray
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
                    contentColor = Color(0xFFF5B11B)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
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
        color = Color.Gray,
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
