package org.example.project.presentation.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun ProductReadOnlySection(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.primaryButtonColors()
            ) {
                Text("Edit product")
            }

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Delete",
                    color = AppColorPalette.Error
                )
            }
        }
    }
}

@Composable
fun ProductEditSection(
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
fun ProductEditField(
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
fun CategoryDropdownField(
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
fun ReadMoreText(
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