package com.uansari.stockwise.ui.product_add_edit

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.domain.model.ProductUnitType
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState

data class ProductAddEditState(
    // Mode
    val isEditMode: Boolean = false,
    val productId: Long? = null,

    // Loading states
    val isLoading: Boolean = false,
    val isLoadingProduct: Boolean = false,
    val isSaving: Boolean = false,

    // Form fields
    val name: String = "",
    val sku: String = "",
    val description: String = "",
    val categoryId: Long? = null,
    val supplierId: Long? = null,
    val buyPrice: String = "",
    val sellPrice: String = "",
    val initialStock: String = "",
    val lowStockThreshold: String = "5",
    val unit: ProductUnitType = ProductUnitType.PCS,

    // Dropdown data
    val categories: List<Category> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),

    // Validation errors
    val nameError: String? = null,
    val skuError: String? = null,
    val categoryError: String? = null,
    val productUnitTypeError: String? = null,
    val buyPriceError: String? = null,
    val sellPriceError: String? = null,
    val initialStockError: String? = null,
    val lowStockThresholdError: String? = null,

    // Dropdown visibility
    val isCategoryDropdownExpanded: Boolean = false,
    val isSupplierDropdownExpanded: Boolean = false,
    val isProductUnitTypeDropdownExpanded: Boolean = false,

    // General error
    val error: String? = null
) : UiState {

    // Computed properties
    val screenTitle: String
        get() = if (isEditMode) "Edit Product" else "Add Product"

    val saveButtonText: String
        get() = if (isEditMode) "Update Product" else "Create Product"

    val selectedCategory: Category?
        get() = categories.find { it.id == categoryId }

    val selectedSupplier: Supplier?
        get() = suppliers.find { it.id == supplierId }

    val buyPriceValue: Double?
        get() = buyPrice.toDoubleOrNull()

    val sellPriceValue: Double?
        get() = sellPrice.toDoubleOrNull()

    val profitPerUnit: Double
        get() {
            val buy = buyPriceValue ?: return 0.0
            val sell = sellPriceValue ?: return 0.0
            return sell - buy
        }

    val profitMargin: Double
        get() {
            val sell = sellPriceValue ?: return 0.0
            if (sell <= 0) return 0.0
            return (profitPerUnit / sell) * 100
        }

    val hasValidPrices: Boolean
        get() = buyPriceValue != null && sellPriceValue != null && buyPriceValue!! >= 0 && sellPriceValue!! >= 0

    val hasErrors: Boolean
        get() = nameError != null || skuError != null || categoryError != null || buyPriceError != null || sellPriceError != null || initialStockError != null || lowStockThresholdError != null

    val canSave: Boolean
        get() = !isSaving && !hasErrors && name.isNotBlank() && sku.isNotBlank() && categoryId != null && buyPriceValue != null && sellPriceValue != null
}

// ==================== EVENTS (Intents) ====================

sealed interface ProductAddEditEvent : UiEvent {
    // Lifecycle
    data object LoadData : ProductAddEditEvent

    // Form field changes
    data class OnNameChanged(val value: String) : ProductAddEditEvent
    data class OnSkuChanged(val value: String) : ProductAddEditEvent
    data class OnDescriptionChanged(val value: String) : ProductAddEditEvent
    data class OnCategorySelected(val categoryId: Long?) : ProductAddEditEvent
    data class OnSupplierSelected(val supplierId: Long?) : ProductAddEditEvent
    data class OnBuyPriceChanged(val value: String) : ProductAddEditEvent
    data class OnSellPriceChanged(val value: String) : ProductAddEditEvent
    data class OnInitialStockChanged(val value: String) : ProductAddEditEvent
    data class OnLowStockThresholdChanged(val value: String) : ProductAddEditEvent
    data class OnUnitChanged(val value: ProductUnitType) : ProductAddEditEvent

    // Dropdown toggle
    data class OnCategoryDropdownToggle(val expanded: Boolean) : ProductAddEditEvent
    data class OnSupplierDropdownToggle(val expanded: Boolean) : ProductAddEditEvent

    data class OnProductUnitTypeDropdownToggle(val expanded: Boolean) : ProductAddEditEvent

    // Actions
    data object SaveProduct : ProductAddEditEvent
    data object NavigateBack : ProductAddEditEvent

    // Error
    data object ClearError : ProductAddEditEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface ProductAddEditEffect : UiEffect {
    data object NavigateBack : ProductAddEditEffect
    data class ProductSaved(val productId: Long, val isNew: Boolean) : ProductAddEditEffect
    data class ShowSnackbar(val message: String) : ProductAddEditEffect
}