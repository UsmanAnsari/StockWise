package com.uansari.stockwise.ui.product_add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.domain.usecase.category.GetCategoriesUseCase
import com.uansari.stockwise.domain.usecase.product.CreateProductUseCase
import com.uansari.stockwise.domain.usecase.product.GetProductByIdUseCase
import com.uansari.stockwise.domain.usecase.product.UpdateProductUseCase
import com.uansari.stockwise.domain.usecase.product.ValidateProductUseCase
import com.uansari.stockwise.domain.usecase.suppplier.GetSuppliersUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import com.uansari.stockwise.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductAddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val validateProductUseCase: ValidateProductUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : BaseViewModel<ProductAddEditState, ProductAddEditEvent, ProductAddEditEffect>(
    ProductAddEditState()
) {

    private val productId: Long =
        savedStateHandle.get<Long>(Screen.ProductAddEdit.ARG_PRODUCT_ID) ?: -1L
    private val isEditMode: Boolean = productId != -1L

    init {
        updateState {
            copy(
                isEditMode = isEditMode, productId = if (isEditMode) productId else null
            )
        }
        onEvent(ProductAddEditEvent.LoadData)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: ProductAddEditEvent) {
        when (event) {
            // Lifecycle
            is ProductAddEditEvent.LoadData -> loadData()

            // Form field changes
            is ProductAddEditEvent.OnNameChanged -> onNameChanged(event.value)
            is ProductAddEditEvent.OnSkuChanged -> onSkuChanged(event.value)
            is ProductAddEditEvent.OnDescriptionChanged -> updateState { copy(description = event.value) }
            is ProductAddEditEvent.OnCategorySelected -> onCategorySelected(event.categoryId)
            is ProductAddEditEvent.OnSupplierSelected -> onSupplierSelected(event.supplierId)
            is ProductAddEditEvent.OnBuyPriceChanged -> onBuyPriceChanged(event.value)
            is ProductAddEditEvent.OnSellPriceChanged -> onSellPriceChanged(event.value)
            is ProductAddEditEvent.OnInitialStockChanged -> onInitialStockChanged(event.value)
            is ProductAddEditEvent.OnLowStockThresholdChanged -> onLowStockThresholdChanged(event.value)
            is ProductAddEditEvent.OnUnitChanged -> updateState { copy(unit = event.value) }

            // Dropdown toggle
            is ProductAddEditEvent.OnCategoryDropdownToggle -> {
                updateState { copy(isCategoryDropdownExpanded = event.expanded) }
            }

            is ProductAddEditEvent.OnSupplierDropdownToggle -> {
                updateState { copy(isSupplierDropdownExpanded = event.expanded) }
            }

            // Actions
            is ProductAddEditEvent.SaveProduct -> saveProduct()
            is ProductAddEditEvent.NavigateBack -> sendEffect(ProductAddEditEffect.NavigateBack)

            // Error
            is ProductAddEditEvent.ClearError -> updateState { copy(error = null) }
            is ProductAddEditEvent.OnProductUnitTypeDropdownToggle -> {
                updateState { copy(isProductUnitTypeDropdownExpanded = event.expanded) }
            }
        }
    }

    // ==================== LOAD DATA ====================

    private fun loadData() {
        loadCategories()
        loadSuppliers()

        if (isEditMode) {
            loadProduct()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().catch { /* Non-critical */ }.collect { categories ->
                updateState { copy(categories = categories) }
            }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            getSuppliersUseCase().catch { /* Non-critical */ }.collect { suppliers ->
                updateState { copy(suppliers = suppliers) }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            updateState { copy(isLoadingProduct = true) }

            try {
                val product = getProductByIdUseCase(productId)

                if (product != null) {
                    updateState {
                        copy(
                            isLoadingProduct = false,
                            name = product.name,
                            sku = product.sku,
                            description = product.description ?: "",
                            categoryId = product.categoryId,
                            supplierId = product.supplierId,
                            buyPrice = product.buyPrice.toString(),
                            sellPrice = product.sellPrice.toString(),
                            lowStockThreshold = product.lowStockThreshold.toString(),
                            unit = product.unit
                        )
                    }
                } else {
                    updateState { copy(isLoadingProduct = false) }
                    sendEffect(ProductAddEditEffect.ShowSnackbar("Product not found"))
                    sendEffect(ProductAddEditEffect.NavigateBack)
                }
            } catch (e: Exception) {
                updateState { copy(isLoadingProduct = false) }
                sendEffect(ProductAddEditEffect.ShowSnackbar(e.message ?: "Failed to load product"))
                sendEffect(ProductAddEditEffect.NavigateBack)
            }
        }
    }

    // ==================== FIELD CHANGES ====================

    private fun onNameChanged(value: String) {
        updateState {
            copy(
                name = value, nameError = null // Clear error on change
            )
        }
    }

    private fun onSkuChanged(value: String) {
        // Auto-uppercase and remove spaces
        val formatted = value.uppercase().replace(" ", "-")
        updateState {
            copy(
                sku = formatted, skuError = null
            )
        }
    }

    private fun onCategorySelected(categoryId: Long?) {
        updateState {
            copy(
                categoryId = categoryId, categoryError = null, isCategoryDropdownExpanded = false
            )
        }
    }

    private fun onSupplierSelected(supplierId: Long?) {
        updateState {
            copy(
                supplierId = supplierId, isSupplierDropdownExpanded = false
            )
        }
    }

    private fun onBuyPriceChanged(value: String) {
        // Allow only valid decimal input
        val filtered = filterDecimalInput(value)
        updateState {
            copy(
                buyPrice = filtered,
                buyPriceError = null,
                sellPriceError = null // Clear sell price error too (margin validation)
            )
        }
    }

    private fun onSellPriceChanged(value: String) {
        val filtered = filterDecimalInput(value)
        updateState {
            copy(
                sellPrice = filtered, sellPriceError = null
            )
        }
    }

    private fun onInitialStockChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        updateState {
            copy(
                initialStock = filtered, initialStockError = null
            )
        }
    }

    private fun onLowStockThresholdChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        updateState {
            copy(
                lowStockThreshold = filtered, lowStockThresholdError = null
            )
        }
    }

    // ==================== SAVE ====================

    private fun saveProduct() {
        viewModelScope.launch {
            // Validate
            val validationResult = validateProductUseCase(
                ValidateProductUseCase.ValidationParams(
                    name = currentState.name,
                    sku = currentState.sku,
                    categoryId = currentState.categoryId,
                    buyPrice = currentState.buyPriceValue,
                    sellPrice = currentState.sellPriceValue,
                    initialStock = currentState.initialStock.toIntOrNull(),
                    lowStockThreshold = currentState.lowStockThreshold.toIntOrNull(),
                    isEditMode = isEditMode,
                    excludeProductId = if (isEditMode) productId else null
                )
            )

            if (!validationResult.isValid) {
                applyValidationErrors(validationResult.errors)
                return@launch
            }

            // Save
            updateState { copy(isSaving = true) }

            val result = if (isEditMode) {
                updateProductUseCase(
                    UpdateProductUseCase.Params(
                        productId = productId,
                        name = currentState.name,
                        sku = currentState.sku,
                        description = currentState.description.takeIf { it.isNotBlank() },
                        categoryId = currentState.categoryId!!,
                        supplierId = currentState.supplierId,
                        buyPrice = currentState.buyPriceValue!!,
                        sellPrice = currentState.sellPriceValue!!,
                        lowStockThreshold = currentState.lowStockThreshold.toInt(),
                        unit = currentState.unit
                    )
                ).map { productId }
            } else {
                createProductUseCase(
                    CreateProductUseCase.Params(
                        name = currentState.name,
                        sku = currentState.sku,
                        description = currentState.description.takeIf { it.isNotBlank() },
                        categoryId = currentState.categoryId!!,
                        supplierId = currentState.supplierId,
                        buyPrice = currentState.buyPriceValue!!,
                        sellPrice = currentState.sellPriceValue!!,
                        initialStock = currentState.initialStock.toIntOrNull() ?: 0,
                        lowStockThreshold = currentState.lowStockThreshold.toInt(),
                        unit = currentState.unit
                    )
                )
            }

            result.onSuccess { savedProductId ->
                updateState { copy(isSaving = false) }
                sendEffect(
                    ProductAddEditEffect.ProductSaved(
                        savedProductId, isNew = !isEditMode
                    )
                )
                sendEffect(ProductAddEditEffect.NavigateBack)
            }.onFailure { error ->
                updateState { copy(isSaving = false) }
                sendEffect(
                    ProductAddEditEffect.ShowSnackbar(
                        error.message ?: "Failed to save product"
                    )
                )
            }
        }
    }

    private fun applyValidationErrors(errors: Map<String, String>) {
        updateState {
            copy(
                nameError = errors[ValidateProductUseCase.FIELD_NAME],
                skuError = errors[ValidateProductUseCase.FIELD_SKU],
                categoryError = errors[ValidateProductUseCase.FIELD_CATEGORY],
                buyPriceError = errors[ValidateProductUseCase.FIELD_BUY_PRICE],
                sellPriceError = errors[ValidateProductUseCase.FIELD_SELL_PRICE],
                initialStockError = errors[ValidateProductUseCase.FIELD_INITIAL_STOCK],
                lowStockThresholdError = errors[ValidateProductUseCase.FIELD_LOW_STOCK_THRESHOLD]
            )
        }
    }

    // ==================== HELPERS ====================

    private fun filterDecimalInput(value: String): String {
        // Allow only digits and one decimal point
        var hasDecimal = false
        return value.filter { char ->
            when {
                char.isDigit() -> true
                char == '.' && !hasDecimal -> {
                    hasDecimal = true
                    true
                }

                else -> false
            }
        }
    }
}