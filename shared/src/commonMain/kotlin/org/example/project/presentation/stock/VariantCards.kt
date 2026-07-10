package org.example.project.presentation.stock

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.ProductVariant
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.formatChf

@Composable
fun VariantInfoCard(
    variant: ProductVariant,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val strings = LocalAppStrings.current

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
            text = strings.format("SKU: {value}", "value" to (variant.sku ?: "-")),
            color = AppColorPalette.TextSecondary
        )

        Text(
            text = strings.format("Stock: {count}", "count" to variant.stockQuantity.toString()),
            color = AppColorPalette.TextSecondary
        )

        Text(
            text = "${strings.text("Price")}: ${formatChf(variant.price)}",
            color = AppColorPalette.TextSecondary
        )

        if (!variant.colour.isNullOrBlank()) {
            Text(
                text = strings.format("Colour: {value}", "value" to variant.colour),
                color = AppColorPalette.TextSecondary
            )
        }

        if (variant.weight != null && variant.weight > 0) {
            Text(
                text = strings.format(
                    "Weight: {value}",
                    "value" to "${variant.weight}${variant.weightUnit.orEmpty()}",
                ),
                color = AppColorPalette.TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                colors = AppComponentDefaults.paginationButtonColors()
            ) {
                Text(strings.text("Edit"))
            }

            Spacer(modifier = Modifier.width(AppDimensions.SmallSpacing))

            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = strings.text("Delete"),
                    color = AppColorPalette.Error
                )
            }
        }
    }
}

@Composable
fun VariantEditCard(
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

    var hasTriedSubmit by remember {
        mutableStateOf(false)
    }

    var skuError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockQuantityError by remember { mutableStateOf<String?>(null) }

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
            label = "Variant SKU",
            isError = skuError!=null,
            errorMessage = skuError
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
            label = "Weight",
            isError = weightError !=null,
            errorMessage = weightError
        )

        ProductEditField(
            value = weightUnit,
            onValueChange = { weightUnit = it },
            label = "Weight unit"
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
            isError = stockQuantityError!=null,
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
                    skuError = null
                    weightError = null
                    priceError = null
                    stockQuantityError = null

                    val priceValue = parseStockDouble(price)
                    val stockValue = parseStockInt(stockQuantity)

                    val weightText = weight.trim()
                    val weightValue = if (weightText.isBlank()) {
                        null
                    } else {
                        parseStockDouble(weightText)
                    }

                    var hasError = false

                    if (sku.isBlank()) {
                        skuError = "SKU is required"
                        hasError = true
                    }

                    if (weightText.isNotBlank() && weightValue == null) {
                        weightError = "Weight must be a number"
                        hasError = true
                    } else if (weightValue != null && weightValue < 0) {
                        weightError = "Weight cannot be negative"
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
fun AddVariantCard(
    defaultPrice: Double,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: (
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) -> Unit
) {
    var sku by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    var colour by remember {
        mutableStateOf("")
    }

    var weight by remember {
        mutableStateOf("")
    }

    var weightUnit by remember {
        mutableStateOf("g")
    }

    var price by remember(defaultPrice) {
        mutableStateOf(defaultPrice.toString())
    }

    var stockQuantity by remember {
        mutableStateOf("0")
    }

    var hasTriedSubmit by remember {
        mutableStateOf(false)
    }

    val priceError = if (hasTriedSubmit) {
        validateRequiredDouble(price, "Price")
    } else {
        null
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
            text = "Add variant",
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        ProductEditField(
            value = sku,
            onValueChange = { sku = it },
            label = "SKU"
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
            label = "Price",
            isError = priceError != null,
            errorMessage = priceError
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

                    val weightText = weight.trim()
                    val weightValue = if (weightText.isBlank()) {
                        null
                    } else {
                        weightText.toDoubleOrNull()
                    }

                    val isWeightValid = weightText.isBlank() || weightValue != null

                    if (
                        sku.isNotBlank() &&
                        priceValue != null &&
                        stockValue != null &&
                        stockValue >= 0 &&
                        isWeightValid
                    ) {
                        onSave(
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
                Text(
                    text = if (isSaving) "Saving..." else "Save"
                )
            }
        }
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
