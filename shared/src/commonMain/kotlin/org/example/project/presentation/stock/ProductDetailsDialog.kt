package org.example.project.presentation.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import org.example.project.domain.stock.Category
import org.example.project.domain.stock.Product
import org.example.project.domain.stock.ProductVariant
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun ProductDetailsDialog(
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

    var isAddingVariant by remember(product.id) {
        mutableStateOf(false)
    }

    var variantToDelete by remember(product.id) {
        mutableStateOf<ProductVariant?>(null)
    }

    var showDeleteProductConfirmation by remember(product.id) {
        mutableStateOf(false)
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
                        },
                        onDeleteClick = {
                            showDeleteProductConfirmation = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

                if (isAddingVariant) {
                    AddVariantCard(
                        defaultPrice = product.price,
                        isSaving = isSaving,
                        onCancel = {
                            isAddingVariant = false
                        },
                        onSave = { sku, name, colour, weight, weightUnit, price, stockQuantity ->
                            onAddVariant(
                                product.id,
                                sku,
                                name,
                                colour,
                                weight,
                                weightUnit,
                                price,
                                stockQuantity
                            )

                            isAddingVariant = false
                        }
                    )
                } else {
                    Button(
                        onClick = {
                            editingVariantId = null
                            isAddingVariant = true
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppComponentDefaults.primaryButtonColors()
                    ) {
                        Text("+ Add variant")
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
                }

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
                                    isAddingVariant = false
                                    editingVariantId = variant.id
                                },
                                onDeleteClick = {
                                    variantToDelete = variant
                                }
                            )
                        }
                    }
                }
                val selectedVariantToDelete = variantToDelete

                if (selectedVariantToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            variantToDelete = null
                        },
                        containerColor = AppColorPalette.Surface,
                        title = {
                            Text(
                                text = "Delete variant?",
                                color = AppColorPalette.TextPrimary
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to delete variant ${selectedVariantToDelete.sku}?",
                                color = AppColorPalette.TextSecondary
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onDeleteVariant(
                                        product.id,
                                        selectedVariantToDelete.id
                                    )

                                    if (editingVariantId == selectedVariantToDelete.id) {
                                        editingVariantId = null
                                    }

                                    variantToDelete = null
                                },
                                enabled = !isSaving,
                                colors = AppComponentDefaults.primaryButtonColors()
                            ) {
                                Text(
                                    text = if (isSaving) "Deleting..." else "Delete"
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    variantToDelete = null
                                }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = AppColorPalette.Primary
                                )
                            }
                        }
                    )
                }
                if (showDeleteProductConfirmation) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteProductConfirmation = false
                        },
                        containerColor = AppColorPalette.Surface,
                        title = {
                            Text(
                                text = "Delete product?",
                                color = AppColorPalette.TextPrimary
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to delete ${product.name}? This will also delete its variants.",
                                color = AppColorPalette.TextSecondary
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onDeleteProduct(product.id)
                                    showDeleteProductConfirmation = false
                                },
                                enabled = !isSaving,
                                colors = AppComponentDefaults.primaryButtonColors()
                            ) {
                                Text(
                                    text = if (isSaving) "Deleting..." else "Delete"
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteProductConfirmation = false
                                }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = AppColorPalette.Primary
                                )
                            }
                        }
                    )
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