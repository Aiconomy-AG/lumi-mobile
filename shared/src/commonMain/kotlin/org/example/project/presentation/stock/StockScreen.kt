package org.example.project.presentation.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
            onUpdateVariant = { productId, variantId, variantSku, variantName, colour, weight, weightUnit, variantPrice, variantStock ->
                viewModel.updateProductVariant(
                    productId = productId,
                    variantId = variantId,
                    sku = variantSku,
                    name = variantName,
                    colour = colour,
                    weight = weight,
                    weightUnit = weightUnit,
                    price = variantPrice,
                    stockQuantity = variantStock
                )
            }
        )
    }
}

@Composable
private fun StockHeader(
    productCount: Int,
    variantCount: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    searchQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onAddProductClick: () -> Unit
) {
    Column {
        Text(
            text = "Stock",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$productCount products",
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = "$variantCount variants",
                color = AppColorPalette.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$lowStockCount low stock",
                color = AppColorPalette.Primary
            )

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Text(
                text = "$outOfStockCount out of stock",
                color = AppColorPalette.Error
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

            Text(
                text = errorMessage,
                color = AppColorPalette.Error
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Search products...",
                    color = AppColorPalette.TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onAddProductClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("+ Add product")
        }
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

@Composable
private fun ProductTable(
    products: List<Product>,
    onProductClick: (Product) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val rowHeight = 54.dp
    val headerHeight = 36.dp
    val verticalPadding = 24.dp
    val tableHeight = headerHeight + rowHeight * 7 + verticalPadding
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tableHeight)
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
                .horizontalScroll(horizontalScrollState)
                .padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                )
                .width(790.dp)
        ) {
            ProductTableHeader()

            products.forEach { product ->
                ProductTableRow(
                    product = product,
                    onClick = {
                        onProductClick(product)
                    }
                )
            }

            val emptyRows = 7 - products.size

            repeat(emptyRows.coerceAtLeast(0)) {
                EmptyFixedTableRow()
            }

            if (products.isEmpty()) {
                EmptyProductTableRow()
            }
        }
    }
}

@Composable
private fun EmptyFixedTableRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // rând gol, doar păstrează înălțimea tabelului
    }
}

@Composable
private fun ProductTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TableHeaderCell("Product", 220)
        TableHeaderCell("SKU", 150)
        TableHeaderCell("Category", 150)
        TableHeaderCell("Stock", 120)
        TableHeaderCell("Price", 150)
    }
}

@Composable
private fun ProductTableRow(
    product: Product,
    onClick: () -> Unit
) {
    val totalStock = productTotalStock(product)
    val displayPrice = productDisplayPrice(product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(
            text = product.name,
            width = 220,
            color = AppColorPalette.TextPrimary
        )

        TableCell(
            text = product.sku ?: "-",
            width = 150,
            color = AppColorPalette.TextSecondary
        )

        TableCell(
            text = product.categoryName ?: "-",
            width = 150,
            color = AppColorPalette.TextSecondary
        )

        TableCell(
            text = if (totalStock == 0) "Out of stock" else totalStock.toString(),
            width = 120,
            color = when {
                totalStock == 0 -> AppColorPalette.Error
                totalStock <= 5 -> AppColorPalette.Primary
                else -> AppColorPalette.Success
            }
        )

        TableCell(
            text = displayPrice,
            width = 150,
            color = AppColorPalette.TextPrimary
        )
    }
}

@Composable
private fun EmptyProductTableRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "No products found.",
            color = AppColorPalette.TextSecondary
        )
    }
}

@Composable
private fun ProductDetailsDialog(
    product: Product,
    categories: List<Category>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onUpdateProduct: (
        productId: Int,
        name: String,
        description: String,
        imageUrl: String,
        sku: String,
        price: Double,
        stockQuantity: Int,
        categoryId: Int?
    ) -> Unit,
    onUpdateVariant: (
        productId: Int,
        variantId: Int,
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) -> Unit
) {
    var name by remember(product.id, product.name) {
        mutableStateOf(product.name)
    }

    var description by remember(product.id, product.description) {
        mutableStateOf(product.description ?: "")
    }

    var imageUrl by remember(product.id, product.imageUrl) {
        mutableStateOf(product.imageUrl ?: "")
    }

    var sku by remember(product.id, product.sku) {
        mutableStateOf(product.sku ?: "")
    }

    var price by remember(product.id, product.price) {
        mutableStateOf(product.price.toString())
    }

    var stockQuantity by remember(product.id, product.stockQuantity) {
        mutableStateOf(product.stockQuantity.toString())
    }

    var selectedCategoryId by remember(product.id, product.categoryId) {
        mutableStateOf(product.categoryId)
    }

    var isEditingProduct by remember(product.id) {
        mutableStateOf(false)
    }

    var editingVariantId by remember(product.id) {
        mutableStateOf<Int?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        titleContentColor = AppColorPalette.TextPrimary,
        textContentColor = AppColorPalette.TextPrimary,
        title = {
            Text("Product details")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isEditingProduct) {
                    ProductEditSection(
                        product = product,
                        categories = categories,
                        isSaving = isSaving,
                        onCancel = {
                            isEditingProduct = false
                        },
                        onSave = { name, description, imageUrl, sku, price, stockQuantity, categoryId ->
                            onUpdateProduct(
                                product.id,
                                name,
                                description,
                                imageUrl,
                                sku,
                                price,
                                stockQuantity,
                                categoryId
                            )

                            isEditingProduct = false
                        }
                    )
                } else {
                    ProductReadOnlySection(
                        product = product,
                        onEditClick = {
                            isEditingProduct = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

                Text(
                    text = "Variants",
                    color = AppColorPalette.TextPrimary,
                    style = AppTextStyles.TableHeader
                )

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                if (product.variants.isEmpty()) {
                    Text(
                        text = "No variants.",
                        color = AppColorPalette.TextSecondary
                    )
                } else {
                    product.variants.forEach { variant ->
                        if (editingVariantId == variant.id) {
                            VariantEditCard(
                                productId = product.id,
                                variant = variant,
                                isSaving = isSaving,
                                onCancel = {
                                    editingVariantId = null
                                },
                                onUpdateVariant = { productId, variantId, sku, name, colour, weight, weightUnit, price, stockQuantity ->
                                    onUpdateVariant(
                                        productId,
                                        variantId,
                                        sku,
                                        name,
                                        colour,
                                        weight,
                                        weightUnit,
                                        price,
                                        stockQuantity
                                    )

                                    editingVariantId = null
                                }
                            )
                        } else {
                            VariantInfoCard(
                                variant = variant,
                                onEditClick = {
                                    editingVariantId = variant.id
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = AppColorPalette.Primary
                )
            }
        }
    )
}
@Composable
private fun ProductEditSection(
    product: Product,
    categories: List<Category>,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: (
        name: String,
        description: String,
        imageUrl: String,
        sku: String,
        price: Double,
        stockQuantity: Int,
        categoryId: Int?
    ) -> Unit
) {
    var name by remember(product.id, product.name) {
        mutableStateOf(product.name)
    }

    var description by remember(product.id, product.description) {
        mutableStateOf(product.description ?: "")
    }

    var imageUrl by remember(product.id, product.imageUrl) {
        mutableStateOf(product.imageUrl ?: "")
    }

    var sku by remember(product.id, product.sku) {
        mutableStateOf(product.sku ?: "")
    }

    var price by remember(product.id, product.price) {
        mutableStateOf(product.price.toString())
    }

    var stockQuantity by remember(product.id, product.stockQuantity) {
        mutableStateOf(product.stockQuantity.toString())
    }

    var selectedCategoryId by remember(product.id, product.categoryId) {
        mutableStateOf(product.categoryId)
    }

    Column {
        Text(
            text = "Edit product",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        ProductEditField(
            value = name,
            onValueChange = { name = it },
            label = "Name"
        )

        ProductEditField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            singleLine = false
        )

        ProductEditField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "Image URL"
        )

        ProductEditField(
            value = sku,
            onValueChange = { sku = it },
            label = "SKU"
        )

        CategoryDropdownField(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { category ->
                selectedCategoryId = category.id
            }
        )

        ProductEditField(
            value = price,
            onValueChange = { price = it },
            label = "Price"
        )

        ProductEditField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity"
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.paginationButtonColors()
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Button(
                onClick = {
                    val priceValue = price.trim().toDoubleOrNull()
                    val stockValue = stockQuantity.trim().toIntOrNull()

                    if (
                        name.isNotBlank() &&
                        priceValue != null &&
                        stockValue != null &&
                        stockValue >= 0
                    ) {
                        onSave(
                            name,
                            description,
                            imageUrl,
                            sku,
                            priceValue,
                            stockValue,
                            selectedCategoryId
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.primaryButtonColors()
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun ProductReadOnlySection(
    product: Product,
    onEditClick: () -> Unit
) {
    Column {
        Text(
            text = product.name,
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        ProductInfoLine("SKU", product.sku ?: "-")
        ProductInfoLine("Category", product.categoryName ?: "-")
        ProductInfoLine("Price", "${product.price} lei")
        ProductInfoLine("Stock", product.stockQuantity.toString())

        if (!product.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

            Text(
                text = "Description:",
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

            ReadMoreText(
                text = product.description,
                maxCharacters = 140
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("Edit product")
        }
    }
}

@Composable
private fun VariantEditCard(
    productId: Int,
    variant: ProductVariant,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onUpdateVariant: (
        productId: Int,
        variantId: Int,
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) -> Unit
) {
    var sku by remember(variant.id, variant.sku) {
        mutableStateOf(variant.sku)
    }

    var name by remember(variant.id, variant.name) {
        mutableStateOf(variant.name ?: "")
    }

    var colour by remember(variant.id, variant.colour) {
        mutableStateOf(variant.colour ?: "")
    }

    var weight by remember(variant.id, variant.weight) {
        mutableStateOf(variant.weight?.toString() ?: "")
    }

    var weightUnit by remember(variant.id, variant.weightUnit) {
        mutableStateOf(variant.weightUnit ?: "")
    }

    var price by remember(variant.id, variant.price) {
        mutableStateOf(variant.price.toString())
    }

    var stockQuantity by remember(variant.id, variant.stockQuantity) {
        mutableStateOf(variant.stockQuantity.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.SmallSpacing)
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
            .padding(AppDimensions.SmallSpacing)
    ) {
        Text(
            text = variantLabel(variant),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        ProductEditField(
            value = sku,
            onValueChange = { sku = it },
            label = "Variant SKU"
        )

        ProductEditField(
            value = name,
            onValueChange = { name = it },
            label = "Variant name"
        )

        ProductEditField(
            value = colour,
            onValueChange = { colour = it },
            label = "Colour"
        )

        ProductEditField(
            value = weight,
            onValueChange = { weight = it },
            label = "Weight"
        )

        ProductEditField(
            value = weightUnit,
            onValueChange = { weightUnit = it },
            label = "Weight unit"
        )

        ProductEditField(
            value = price,
            onValueChange = { price = it },
            label = "Price"
        )

        ProductEditField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity"
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.paginationButtonColors()
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            Button(
                onClick = {
                    val priceValue = price.trim().toDoubleOrNull()
                    val stockValue = stockQuantity.trim().toIntOrNull()
                    val weightValue = weight.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()

                    if (
                        sku.isNotBlank() &&
                        priceValue != null &&
                        stockValue != null &&
                        stockValue >= 0
                    ) {
                        onUpdateVariant(
                            productId,
                            variant.id,
                            sku,
                            name,
                            colour,
                            weightValue,
                            weightUnit,
                            priceValue,
                            stockValue
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.primaryButtonColors()
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun ProductEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.SmallSpacing),
        singleLine = singleLine,
        colors = AppComponentDefaults.appTextFieldColors()
    )
}

@Composable
private fun CategoryDropdownField(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedCategory = categories.firstOrNull { category ->
        category.id == selectedCategoryId
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.SmallSpacing)
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Category")
            },
            placeholder = {
                Text("Select category")
            },
            trailingIcon = {
                Text(
                    text = if (expanded) "▲" else "▼",
                    color = AppColorPalette.TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    expanded = true
                }
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            categories.forEach { category ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(category.name)
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductInfoLine(
    label: String,
    value: String
) {
    Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

    Row {
        Text(
            text = "$label: ",
            color = AppColorPalette.TextSecondary
        )

        Text(
            text = value,
            color = AppColorPalette.TextPrimary
        )
    }
}

@Composable
private fun VariantInfoCard(
    variant: ProductVariant,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimensions.SmallSpacing)
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius)
            )
            .padding(AppDimensions.SmallSpacing)
    ) {
        Text(
            text = variantLabel(variant),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Text(
            text = "SKU: ${variant.sku}",
            color = AppColorPalette.TextSecondary
        )

        Text(
            text = "Stock: ${variant.stockQuantity}",
            color = AppColorPalette.TextSecondary
        )

        Text(
            text = "Price: ${variant.price} lei",
            color = AppColorPalette.TextSecondary
        )

        if (!variant.colour.isNullOrBlank()) {
            Text(
                text = "Colour: ${variant.colour}",
                color = AppColorPalette.TextSecondary
            )
        }

        if (variant.weight != null && variant.weight > 0) {
            Text(
                text = "Weight: ${variant.weight}${variant.weightUnit.orEmpty()}",
                color = AppColorPalette.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Edit variant")
        }
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
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            color = AppColorPalette.TextSecondary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1,
            colors = AppComponentDefaults.paginationButtonColors()
        ) {
            Text("Next")
        }
    }
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
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width.dp)
    )
}

private fun productTotalStock(product: Product): Int {
    return if (product.variants.isNotEmpty()) {
        product.variants.sumOf { variant ->
            variant.stockQuantity
        }
    } else {
        product.stockQuantity
    }
}

private fun productDisplayPrice(product: Product): String {
    if (product.variants.isEmpty()) {
        return "${product.price} lei"
    }

    val prices = product.variants.map { variant ->
        variant.price
    }

    val min = prices.minOrNull() ?: product.price
    val max = prices.maxOrNull() ?: product.price

    return if (min == max) {
        "$min lei"
    } else {
        "$min - $max lei"
    }
}

private fun variantLabel(variant: ProductVariant): String {
    val parts = mutableListOf<String>()

    if (!variant.colour.isNullOrBlank()) {
        parts.add(variant.colour)
    }

    if (variant.weight != null && variant.weight > 0) {
        val unit = variant.weightUnit.orEmpty()
        parts.add("${variant.weight}${unit}")
    }

    if (parts.isNotEmpty()) {
        return parts.joinToString(" / ")
    }

    if (!variant.name.isNullOrBlank()) {
        return variant.name
    }

    return "Default variant"
}

@Composable
private fun ReadMoreText(
    text: String,
    maxCharacters: Int = 120
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val shouldShowReadMore = text.length > maxCharacters

    val displayedText = if (!shouldShowReadMore || expanded) {
        text
    } else {
        text.take(maxCharacters).trimEnd() + "..."
    }

    Column {
        Text(
            text = displayedText,
            color = AppColorPalette.TextPrimary
        )

        if (shouldShowReadMore) {
            TextButton(
                onClick = {
                    expanded = !expanded
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (expanded) "Show less" else "Read more",
                    color = AppColorPalette.Primary
                )
            }
        }
    }
}