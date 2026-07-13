package org.example.project.presentation.stock

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant
import org.example.project.presentation.components.AppDetailOverlay
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun ProductDetailsOverlay(
    product: Product,
    categories: List<Category>,
    isSaving: Boolean,
    onBackClick: () -> Unit,
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
    ) -> Unit,
    onAddVariant: (
        productId: Int,
        sku: String,
        name: String,
        colour: String,
        weight: Double?,
        weightUnit: String,
        price: Double,
        stockQuantity: Int
    ) -> Unit,
    onDeleteVariant: (
        productId: Int,
        variantId: Int
    ) -> Unit,
    onDeleteProduct: (productId: Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var isEditingProduct by remember(product.id) { mutableStateOf(false) }
    var editingVariantId by remember(product.id) { mutableStateOf<Int?>(null) }
    var isAddingVariant by remember(product.id) { mutableStateOf(false) }
    var variantToDelete by remember(product.id) { mutableStateOf<ProductVariant?>(null) }
    var showDeleteProductConfirmation by remember(product.id) { mutableStateOf(false) }

    AppDetailOverlay(
        title = product.name,
        onBackClick = onBackClick,
    ) {
        if (isEditingProduct) {
            ProductEditSection(
                product = product,
                categories = categories,
                isSaving = isSaving,
                onCancel = { isEditingProduct = false },
                onSave = { name, description, imageUrl, sku, price, stockQuantity, categoryId ->
                    onUpdateProduct(
                        product.id,
                        name,
                        description,
                        imageUrl,
                        sku,
                        price,
                        stockQuantity,
                        categoryId,
                    )
                    isEditingProduct = false
                },
            )
        } else {
            ProductReadOnlySection(
                product = product,
                onEditClick = { isEditingProduct = true },
                onDeleteClick = { showDeleteProductConfirmation = true },
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        if (isAddingVariant) {
            AddVariantCard(
                defaultPrice = product.price,
                isSaving = isSaving,
                onCancel = { isAddingVariant = false },
                onSave = { sku, name, colour, weight, weightUnit, price, stockQuantity ->
                    onAddVariant(
                        product.id,
                        sku,
                        name,
                        colour,
                        weight,
                        weightUnit,
                        price,
                        stockQuantity,
                    )
                    isAddingVariant = false
                },
            )
        } else {
            Button(
                onClick = {
                    editingVariantId = null
                    isAddingVariant = true
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = AppComponentDefaults.primaryButtonColors(),
            ) {
                Text(strings.text("Add variant"))
            }

            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
        }

        Text(
            text = strings.text("Variants"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.TableHeader,
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        if (product.variants.isEmpty()) {
            Text(
                text = strings.text("No variants."),
                color = AppColorPalette.TextSecondary,
            )
        } else {
            product.variants.forEach { variant ->
                if (editingVariantId == variant.id) {
                    VariantEditCard(
                        productId = product.id,
                        variant = variant,
                        isSaving = isSaving,
                        onCancel = { editingVariantId = null },
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
                                stockQuantity,
                            )
                            editingVariantId = null
                        },
                    )
                } else {
                    VariantInfoCard(
                        variant = variant,
                        onEditClick = {
                            isAddingVariant = false
                            editingVariantId = variant.id
                        },
                        onDeleteClick = { variantToDelete = variant },
                    )
                }
            }
        }
    }

    variantToDelete?.let { variant ->
        DeleteVariantConfirmationDialog(
            variant = variant,
            isSaving = isSaving,
            onConfirm = {
                onDeleteVariant(product.id, variant.id)
                if (editingVariantId == variant.id) {
                    editingVariantId = null
                }
                variantToDelete = null
            },
            onDismiss = { variantToDelete = null },
        )
    }

    if (showDeleteProductConfirmation) {
        DeleteProductConfirmationDialog(
            product = product,
            isSaving = isSaving,
            onConfirm = {
                onDeleteProduct(product.id)
                showDeleteProductConfirmation = false
            },
            onDismiss = { showDeleteProductConfirmation = false },
        )
    }
}

@Composable
private fun DeleteVariantConfirmationDialog(
    variant: ProductVariant,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        title = {
            Text(
                text = strings.text("Delete variant?"),
                color = AppColorPalette.TextPrimary,
            )
        },
        text = {
            Text(
                text = strings.format(
                    "Are you sure you want to delete variant {sku}?",
                    "sku" to variant.sku,
                ),
                color = AppColorPalette.TextSecondary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                colors = AppComponentDefaults.primaryButtonColors(),
            ) {
                Text(strings.text("Delete"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.text("Cancel"),
                    color = AppColorPalette.Primary,
                )
            }
        },
    )
}

@Composable
private fun DeleteProductConfirmationDialog(
    product: Product,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        title = {
            Text(
                text = strings.text("Delete product?"),
                color = AppColorPalette.TextPrimary,
            )
        },
        text = {
            Text(
                text = strings.format(
                    "Are you sure you want to delete {name}? This will also delete its variants.",
                    "name" to product.name,
                ),
                color = AppColorPalette.TextSecondary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                colors = AppComponentDefaults.primaryButtonColors(),
            ) {
                Text(strings.text("Delete"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.text("Cancel"),
                    color = AppColorPalette.Primary,
                )
            }
        },
    )
}
