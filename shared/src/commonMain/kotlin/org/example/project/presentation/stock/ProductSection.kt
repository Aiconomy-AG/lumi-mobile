package org.example.project.presentation.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.formatChf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

@Composable
fun ProductReadOnlySection(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column {
        if (!product.imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                contentDescription = "Image of ${product.name}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(AppDimensions.TableCornerRadius))
                    .background(AppColorPalette.Surface),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColorPalette.Primary)
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(AppColorPalette.Border),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", color = AppColorPalette.TextSecondary)
                    }
                }
            )
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
        }

        Text(
            text = product.name,
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        ProductInfoLine(strings.text("SKU"), product.sku ?: "-")
        ProductInfoLine(strings.text("Category"), product.categoryName ?: "-")
        ProductInfoLine(strings.text("Price"), formatChf(product.price))
        ProductInfoLine(strings.text("Stock"), product.stockQuantity.toString())

        val description = product.description?.htmlToPlainText().orEmpty()
        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

            Text(
                text = strings.text("Description:"),
                color = AppColorPalette.TextSecondary
            )

            Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

            Text(
                text = description,
                color = AppColorPalette.TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            StockEditActionButton(onClick = onEditClick)

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            StockDeleteActionButton(onClick = onDeleteClick)
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

    var hasTriedSubmit by remember {
        mutableStateOf(false)
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var skuError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockQuantityError by remember { mutableStateOf<String?>(null) }


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
            label = "Name",
            isError = nameError !=null,
            errorMessage = nameError
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
            label = "SKU",
            isError = skuError != null,
            errorMessage = skuError
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
            label = "Price",
            isError = priceError != null,
            errorMessage = priceError
        )

        ProductEditField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = "Stock quantity",
            isError = stockQuantityError != null,
            errorMessage = stockQuantityError
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
                    nameError = null
                    skuError = null
                    priceError = null
                    stockQuantityError = null

                    val priceValue = parseStockDouble(price)
                    val stockValue = parseStockInt(stockQuantity)

                    var hasError = false

                    if (name.isBlank()) {
                        nameError = "Name is required"
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

private fun String.htmlToPlainText(): String {
    return replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
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
