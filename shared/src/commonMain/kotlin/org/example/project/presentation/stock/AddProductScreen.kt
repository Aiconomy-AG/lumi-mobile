package feature.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    var weight by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }

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
            value = weight,
            onValueChange = { weight = it },
            label = "Weight"
        )

        ProductInput(
            value = weightUnit,
            onValueChange = { weightUnit = it },
            label = "Weight unit"
        )

        ProductInput(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity"
        )

        ProductInput(
            value = categoryId,
            onValueChange = { categoryId = it },
            label = "Category"
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        Button(
            onClick = {
                val priceValue = price.toDoubleOrNull()
                val stockValue = stockQuantity.toIntOrNull()
                val categoryIdValue = categoryId.toIntOrNull()

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
