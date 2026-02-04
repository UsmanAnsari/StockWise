package com.uansari.stockwise.ui.product_add_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.domain.model.ProductUnitType
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.product_add_edit.compnents.DropdownField
import com.uansari.stockwise.ui.product_add_edit.compnents.NumberInputField
import com.uansari.stockwise.ui.product_add_edit.compnents.PriceInputField
import com.uansari.stockwise.ui.product_add_edit.compnents.ProfitPreviewCard
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAddEditScreen(
    onNavigateBack: () -> Unit,
    onProductSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductAddEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ProductAddEditEffect.NavigateBack -> onNavigateBack()
                is ProductAddEditEffect.ProductSaved -> {
                    val message = if (effect.isNew) "Product created" else "Product updated"
                    snackbarHostState.showSnackbar(message)
                    onProductSaved(effect.productId)
                }

                is ProductAddEditEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(state.screenTitle) }, navigationIcon = {
            IconButton(
                onClick = { viewModel.onEvent(ProductAddEditEvent.NavigateBack) },
                enabled = !state.isSaving
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }, actions = {
            TextButton(
                onClick = { viewModel.onEvent(ProductAddEditEvent.SaveProduct) },
                enabled = state.canSave
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->

        if (state.isLoadingProduct) {
            LoadingScreen(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()))
        } else {
            ProductAddEditForm(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = modifier.padding(top = paddingValues.calculateTopPadding())
            )
        }
    }
}

@Composable
private fun ProductAddEditForm(
    state: ProductAddEditState,
    onEvent: (ProductAddEditEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === BASIC INFO SECTION ===
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Product Name
        OutlinedTextField(
            value = state.name,
            onValueChange = { onEvent(ProductAddEditEvent.OnNameChanged(it)) },
            label = { Text("Product Name *") },
            placeholder = { Text("e.g. iPhone 17 Pro") },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // SKU
        OutlinedTextField(
            value = state.sku,
            onValueChange = { onEvent(ProductAddEditEvent.OnSkuChanged(it)) },
            label = { Text("SKU *") },
            placeholder = { Text("e.g. IPHONE-17-PRO") },
            singleLine = true,
            isError = state.skuError != null,
            supportingText = state.skuError?.let { { Text(it) } }
                ?: { Text("Unique product identifier") },
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(ProductAddEditEvent.OnDescriptionChanged(it)) },
            label = { Text("Description") },
            placeholder = { Text("Optional product description") },
            minLines = 2,
            maxLines = 4,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // === CATEGORY & SUPPLIER SECTION ===
        Text(
            text = "Category & Supplier",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Category Dropdown
        DropdownField(
            label = "Category *",
            selectedItem = state.selectedCategory,
            items = state.categories,
            onItemSelected = { category ->
                onEvent(ProductAddEditEvent.OnCategorySelected(category?.id))
            },
            itemText = { it.name },
            expanded = state.isCategoryDropdownExpanded,
            onExpandedChange = { onEvent(ProductAddEditEvent.OnCategoryDropdownToggle(it)) },
            isError = state.categoryError != null,
            errorMessage = state.categoryError,
            enabled = !state.isSaving
        )

        // Supplier Dropdown (optional)
        DropdownField(
            label = "Supplier",
            selectedItem = state.selectedSupplier,
            items = state.suppliers,
            onItemSelected = { supplier ->
                onEvent(ProductAddEditEvent.OnSupplierSelected(supplier?.id))
            },
            itemText = { it.name },
            expanded = state.isSupplierDropdownExpanded,
            onExpandedChange = { onEvent(ProductAddEditEvent.OnSupplierDropdownToggle(it)) },
            allowClear = true,
            enabled = !state.isSaving
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // === PRICING SECTION ===
        Text(
            text = "Pricing",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceInputField(
                label = "Cost Price *",
                value = state.buyPrice,
                onValueChange = { onEvent(ProductAddEditEvent.OnBuyPriceChanged(it)) },
                isError = state.buyPriceError != null,
                errorMessage = state.buyPriceError,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            )

            PriceInputField(
                label = "Sell Price *",
                value = state.sellPrice,
                onValueChange = { onEvent(ProductAddEditEvent.OnSellPriceChanged(it)) },
                isError = state.sellPriceError != null,
                errorMessage = state.sellPriceError,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            )
        }

        // Profit Preview
        if (state.hasValidPrices) {
            ProfitPreviewCard(
                buyPrice = state.buyPriceValue, sellPrice = state.sellPriceValue
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // === STOCK SECTION ===
        Text(
            text = "Stock",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Initial Stock (only for new products)
            if (!state.isEditMode) {
                NumberInputField(
                    label = "Initial Stock *",
                    value = state.initialStock,
                    onValueChange = { onEvent(ProductAddEditEvent.OnInitialStockChanged(it)) },
                    isError = state.initialStockError != null,
                    errorMessage = state.initialStockError,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            // Low Stock Threshold
            NumberInputField(
                label = "Low Stock Alert",
                value = state.lowStockThreshold,
                onValueChange = { onEvent(ProductAddEditEvent.OnLowStockThresholdChanged(it)) },
                isError = state.lowStockThresholdError != null,
                errorMessage = state.lowStockThresholdError,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            )
        }

        // Unit

        DropdownField(
            label = "Unit",
            selectedItem = state.unit,
            items = ProductUnitType.entries,
            onItemSelected = {
                onEvent(ProductAddEditEvent.OnUnitChanged(it ?: ProductUnitType.PCS))
            },
            itemText = { it.name },
            expanded = state.isProductUnitTypeDropdownExpanded,
            onExpandedChange = { onEvent(ProductAddEditEvent.OnProductUnitTypeDropdownToggle(it)) },
            isError = state.productUnitTypeError != null,
            errorMessage = state.productUnitTypeError,
            enabled = !state.isSaving
        )

        if (state.isEditMode) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "💡 To adjust stock quantity, use the Stock adjustment feature on the product detail screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Save Button (bottom)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onEvent(ProductAddEditEvent.SaveProduct) },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(state.saveButtonText)
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(8.dp))
    }
}