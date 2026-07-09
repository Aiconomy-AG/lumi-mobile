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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Product
import org.example.project.presentation.components.PaginationBar
import org.example.project.presentation.localization.LocalAppStrings
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
            .background(AppColorPalette.Background)
            .padding(AppDimensions.ScreenPadding)
    ) {
        StockHeader(
            productCount = state.products.size,
            lowStockCount = state.lowStockCount,
            outOfStockCount = state.outOfStockCount,
            searchQuery = state.searchQuery,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onAddProductClick = onAddProductClick
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        StockTable(
            products = pagedProducts,
            onDeleteProduct = viewModel::deleteProduct,
            onUpdateQuantity = viewModel::updateStockQuantity
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        PaginationBar(
            currentPage = currentPage,
            totalPages = totalPages,
            onPreviousClick = { if (currentPage > 0) currentPage-- },
            onNextClick = { if (currentPage < totalPages - 1) currentPage++ },

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
    val strings = LocalAppStrings.current

    Column {
        Text(
            text = strings.text("Stock"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.format("{count} products", "count" to productCount.toString()),
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = strings.format("{count} low stock", "count" to lowStockCount.toString()),
                color = AppColorPalette.Primary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = strings.format("{count} out of stock", "count" to outOfStockCount.toString()),
                color = AppColorPalette.Error
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(strings.text("Search products..."), color = AppColorPalette.TextSecondary)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onAddProductClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text(strings.text("+ Add product"))
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
            .heightIn(max = AppDimensions.TableMaxHeight)
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 26.dp)
                .width(686.dp)
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
            .height(AppDimensions.ScrollBarHeight)
            .background(
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.ScrollBarHeight)
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
                    color = AppColorPalette.Primary,
                    shape = RoundedCornerShape(AppDimensions.ScrollBarHeight)
                )
        )
    }
}

@Composable
private fun StockTableHeader() {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TableHeaderCell(strings.text("Product"), 220)
        TableHeaderCell("SKU", 150)
        TableHeaderCell(strings.text("Stock"), 120)
        TableHeaderCell(strings.text("Price"), 120)
        TableHeaderCell(strings.text("Actions"), 76)
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
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(product.name, 220, AppColorPalette.TextPrimary)
        TableCell(sku, 150, AppColorPalette.TextSecondary)

        TableCell(
            text = if (stockQuantity == 0) strings.text("Out of stock") else stockQuantity.toString(),
            width = 120,
            color = when {
                stockQuantity == 0 -> AppColorPalette.Error
                stockQuantity <= 5 -> AppColorPalette.Primary
                else -> AppColorPalette.Success
            }
        )

        TableCell("${price} lei", 120, AppColorPalette.TextPrimary)

        Row(
            modifier = Modifier.width(76.dp)
        ) {
            Button(
                modifier = Modifier.size(AppDimensions.ActionButtonSize),
                onClick = {
                    showEditDialog = true
                },
                colors = AppComponentDefaults.primaryButtonColors(),
                contentPadding = PaddingValues(0.dp)
            ) {
                EditIcon(tint = AppColorPalette.OnPrimary)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                modifier = Modifier.size(AppDimensions.ActionButtonSize),
                onClick = onDeleteProduct,
                colors = AppComponentDefaults.primaryButtonColors(),
                contentPadding = PaddingValues(0.dp)
            ) {
                DeleteIcon(tint = AppColorPalette.OnPrimary)
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
private fun EditIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(AppDimensions.ActionIconSize)) {
        val strokeWidth = size.width * 0.1f

        drawLine(
            color = tint,
            start = Offset(size.width * 0.28f, size.height * 0.76f),
            end = Offset(size.width * 0.72f, size.height * 0.32f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.66f, size.height * 0.24f),
            end = Offset(size.width * 0.82f, size.height * 0.4f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.2f, size.height * 0.84f),
            end = Offset(size.width * 0.34f, size.height * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.72f, size.height * 0.3f),
            end = Offset(size.width * 0.78f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DeleteIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(AppDimensions.ActionIconSize)) {
        val strokeWidth = size.width * 0.1f

        drawLine(
            color = tint,
            start = Offset(size.width * 0.24f, size.height * 0.34f),
            end = Offset(size.width * 0.76f, size.height * 0.34f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.42f, size.height * 0.22f),
            end = Offset(size.width * 0.58f, size.height * 0.22f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.48f, size.height * 0.16f),
            end = Offset(size.width * 0.52f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.32f, size.height * 0.42f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.05f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.44f, size.height * 0.5f),
            end = Offset(size.width * 0.44f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.56f, size.height * 0.5f),
            end = Offset(size.width * 0.56f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.5f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
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
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        titleContentColor = AppColorPalette.TextPrimary,
        textContentColor = AppColorPalette.TextPrimary,
        title = {
            Text(strings.text("Edit"))
        },
        text = {
            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    quantityText = it
                },
                label = {
                    Text(strings.text("Quantity"))
                },
                singleLine = true,
                colors = AppComponentDefaults.appTextFieldColors()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityText.toIntOrNull()

                    if (quantity != null && quantity >= 0) {
                        onSave(quantity)
                    }
                }
            ) {
                Text(strings.text("Save"), color = AppColorPalette.Primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(strings.text("Cancel"), color = AppColorPalette.TextSecondary)
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
        color = AppColorPalette.TextSecondary,
        style = AppTextStyles.TableHeader,
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
