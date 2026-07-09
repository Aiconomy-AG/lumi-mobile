package feature.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.example.project.domain.stock.Category
import org.example.project.presentation.stock.StockViewModel
import org.example.project.presentation.stock.parseStockDouble
import org.example.project.presentation.stock.parseStockInt
import org.example.project.presentation.stock.validateRequiredDouble
import org.example.project.presentation.stock.validateRequiredInt
import org.example.project.presentation.stock.validateRequiredText
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun AddProductScreen(
    viewModel: StockViewModel,
    onProductAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var hasTriedSubmit by remember {
        mutableStateOf(false)
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var skuError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockQuantityError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.ScreenPadding)
    ) {
        Text(
            text = "Add product",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        ProductInput(
            value = name,
            onValueChange = { name = it },
            label = "Product name",
            isError = nameError != null,
            errorMessage = nameError
        )

        ProductInput(
            value = description,
            onValueChange = { description = it },
            label = "Description",
        )

        ProductInput(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "Image URL"
        )

        ProductInput(
            value = sku,
            onValueChange = { sku = it },
            label = "SKU",
            isError = skuError != null,
            errorMessage = skuError
        )

        ProductInput(
            value = price,
            onValueChange = { price = it },
            label = "Price",
            isError = priceError != null,
            errorMessage = priceError
        )

        ProductInput(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity",
            isError = stockQuantityError != null,
            errorMessage = stockQuantityError

        )

        CategoryDropdown(
            categories = state.categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { category ->
                selectedCategoryId = category.id
            }
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        Button(
            onClick = {
                nameError = null
                skuError = null
                priceError = null
                stockQuantityError = null

                val priceValue = price.trim().replace(",", ".").toDoubleOrNull()
                val stockValue = stockQuantity.trim().toIntOrNull()

                var hasError = false

                if (name.isBlank()) {
                    nameError = "Product name is required"
                    hasError = true
                }

                if (sku.isBlank()) {
                    skuError = "SKU is required"
                    hasError = true
                }

                if (price.isBlank()) {
                    priceError = "Price is required"
                    hasError = true
                } else if (priceValue == null) {
                    priceError = "Price must be a number"
                    hasError = true
                } else if (priceValue < 0) {
                    priceError = "Price cannot be negative"
                    hasError = true
                }

                if (stockQuantity.isBlank()) {
                    stockQuantityError = "Stock quantity is required"
                    hasError = true
                } else if (stockValue == null) {
                    stockQuantityError = "Stock quantity must be a whole number"
                    hasError = true
                } else if (stockValue < 0) {
                    stockQuantityError = "Stock quantity cannot be negative"
                    hasError = true
                }

                if (!hasError && priceValue != null && stockValue != null) {
                    viewModel.addProduct(
                        name = name,
                        description = description,
                        imageUrl = imageUrl,
                        sku = sku,
                        price = priceValue,
                        stockQuantity = stockValue,
                        categoryId = selectedCategoryId
                    )

                    onProductAdded()
                }
            },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("Save product")
        }

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = AppComponentDefaults.primaryButtonColors()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun ProductInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
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
        isError = isError,
        supportingText = {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = AppColorPalette.Error
                )
            }
        },
        colors = AppComponentDefaults.appTextFieldColors()
    )
}

@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
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