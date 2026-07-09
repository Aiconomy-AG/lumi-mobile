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
            label = "Product name"
        )

        ProductInput(
            value = description,
            onValueChange = { description = it },
            label = "Description"
        )

        ProductInput(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "Image URL"
        )

        ProductInput(
            value = sku,
            onValueChange = { sku = it },
            label = "SKU"
        )

        ProductInput(
            value = price,
            onValueChange = { price = it },
            label = "Price"
        )

        ProductInput(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity"
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
                val priceValue = price.toDoubleOrNull()
                val stockValue = stockQuantity.toIntOrNull()
                val categoryIdValue = selectedCategoryId

                if (
                    name.isNotBlank() &&
                    sku.isNotBlank() &&
                    priceValue != null &&
                    stockValue != null &&
                    categoryIdValue != null
                ) {
                    viewModel.addProduct(
                        name = name,
                        description = description,
                        imageUrl = imageUrl,
                        sku = sku,
                        price = priceValue,
                        stockQuantity = stockValue,
                        categoryId = categoryIdValue
                    )

                    onProductAdded()
                }
            },
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
    label: String
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
        singleLine = true,
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
