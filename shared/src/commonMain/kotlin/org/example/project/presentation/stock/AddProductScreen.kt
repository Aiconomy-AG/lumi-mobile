package feature.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.stock.StockViewModel

@Composable
fun AddProductScreen(
    viewModel: StockViewModel,
    onProductAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Add product",
            color = colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val priceValue = price.toDoubleOrNull()
                val weightValue = weight.toDoubleOrNull()
                val stockValue = stockQuantity.toIntOrNull()

                if (
                    name.isNotBlank() &&
                    sku.isNotBlank() &&
                    priceValue != null &&
                    weightValue != null &&
                    stockValue != null
                ) {
                    viewModel.addProduct(
                        name = name,
                        description = description,
                        imageUrl = imageUrl,
                        sku = sku,
                        price = priceValue,
                        weight = weightValue,
                        weightUnit = weightUnit,
                        stockQuantity = stockValue
                    )

                    onProductAdded()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text("Save product")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
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
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
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
}
