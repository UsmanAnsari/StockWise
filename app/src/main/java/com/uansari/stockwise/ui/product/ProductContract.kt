package com.uansari.stockwise.ui.product

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState
import com.uansari.stockwise.util.ProductSortOption
import com.uansari.stockwise.util.StockFilter

data class ProductState(
    // Loading states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,

    // Data
    val allProducts: List<ProductWithDetails> = emptyList(),
    val filteredProducts: List<ProductWithDetails> = emptyList(),
    val categories: List<Category> = emptyList(),

    // Search & Filters
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val stockFilter: StockFilter = StockFilter.ALL,
    val sortOption: ProductSortOption = ProductSortOption.NAME_ASC,

    // UI state
    val isSearchActive: Boolean = false,
    val isFilterSheetVisible: Boolean = false,

    // Error
    val error: String? = null
) : UiState {

    /**
     * Count of active filters (excluding default values).
     */
    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedCategoryId != null) count++
            if (stockFilter != StockFilter.ALL) count++
            if (sortOption != ProductSortOption.NAME_ASC) count++
            return count
        }

    /**
     * True if any filter is applied (including search).
     */
    val isFiltered: Boolean
        get() = searchQuery.isNotBlank() ||
                selectedCategoryId != null ||
                stockFilter != StockFilter.ALL

    /**
     * True if filtered products list is empty and not loading.
     */
    val isEmpty: Boolean
        get() = filteredProducts.isEmpty() && !isLoading

    /**
     * True if there are no products at all (not just filtered).
     */
    val hasNoProducts: Boolean
        get() = allProducts.isEmpty() && !isLoading

    /**
     * Get selected category object.
     */
    val selectedCategory: Category?
        get() = categories.find { it.id == selectedCategoryId }
}

// ==================== EVENTS (Intents) ====================

sealed interface ProductEvent : UiEvent {
    // Lifecycle
    data object LoadProducts : ProductEvent
    data object Refresh : ProductEvent

    // Search
    data class OnSearchQueryChanged(val query: String) : ProductEvent
    data class OnSearchActiveChanged(val isActive: Boolean) : ProductEvent

    // Filters
    data class OnCategorySelected(val categoryId: Long?) : ProductEvent
    data class OnStockFilterSelected(val filter: StockFilter) : ProductEvent
    data class OnSortOptionSelected(val option: ProductSortOption) : ProductEvent
    data object ClearAllFilters : ProductEvent

    // Filter Sheet
    data object ShowFilterSheet : ProductEvent
    data object HideFilterSheet : ProductEvent

    // Navigation
    data class OnProductClicked(val productId: Long) : ProductEvent
    data class OnEditProductClicked(val productId: Long) : ProductEvent
    data class OnStockAdjustClicked(val productId: Long) : ProductEvent
    data object OnAddProductClicked : ProductEvent

    // Error
    data object ClearError : ProductEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface ProductEffect : UiEffect {
    data class NavigateToProductDetail(val productId: Long) : ProductEffect
    data class NavigateToEditProduct(val productId: Long) : ProductEffect
    data class NavigateToStockMovement(val productId: Long) : ProductEffect
    data object NavigateToAddProduct : ProductEffect
    data class ShowError(val message: String) : ProductEffect
}