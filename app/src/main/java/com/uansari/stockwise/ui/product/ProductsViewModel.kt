package com.uansari.stockwise.ui.product

import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.domain.usecase.category.GetCategoriesUseCase
import com.uansari.stockwise.domain.usecase.product.FilterProductsUseCase
import com.uansari.stockwise.domain.usecase.product.GetProductsWithDetailsUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import com.uansari.stockwise.util.ProductSortOption
import com.uansari.stockwise.util.StockFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsWithDetailsUseCase: GetProductsWithDetailsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterProductsUseCase: FilterProductsUseCase
) : BaseViewModel<ProductState, ProductEvent, ProductEffect>(ProductState()) {

    init {
        onEvent(ProductEvent.LoadProducts)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: ProductEvent) {
        when (event) {
            // Lifecycle
            is ProductEvent.LoadProducts -> loadProducts()
            is ProductEvent.Refresh -> refresh()

            // Search
            is ProductEvent.OnSearchQueryChanged -> onSearchQueryChanged(event.query)
            is ProductEvent.OnSearchActiveChanged -> onSearchActiveChanged(event.isActive)

            // Filters
            is ProductEvent.OnCategorySelected -> onCategorySelected(event.categoryId)
            is ProductEvent.OnStockFilterSelected -> onStockFilterSelected(event.filter)
            is ProductEvent.OnSortOptionSelected -> onSortOptionSelected(event.option)
            is ProductEvent.ClearAllFilters -> clearAllFilters()

            // Filter Sheet
            is ProductEvent.ShowFilterSheet -> updateState { copy(isFilterSheetVisible = true) }
            is ProductEvent.HideFilterSheet -> updateState { copy(isFilterSheetVisible = false) }

            // Navigation
            is ProductEvent.OnProductClicked -> sendEffect(ProductEffect.NavigateToProductDetail(event.productId))
            is ProductEvent.OnEditProductClicked -> sendEffect(ProductEffect.NavigateToEditProduct(event.productId))
            is ProductEvent.OnStockAdjustClicked -> sendEffect(ProductEffect.NavigateToStockMovement(event.productId))
            is ProductEvent.OnAddProductClicked -> sendEffect(ProductEffect.NavigateToAddProduct)

            // Error
            is ProductEvent.ClearError -> updateState { copy(error = null) }
        }
    }

    // ==================== LOAD DATA ====================

    private fun loadProducts() {
        observeProducts()
        observeCategories()
    }

    private fun refresh() {
        updateState { copy(isRefreshing = true, error = null) }
        loadProducts()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            getProductsWithDetailsUseCase()
                .catch { e ->
                    handleError(e.message ?: "Failed to load products")
                }
                .collect { products ->
                    updateState {
                        copy(
                            allProducts = products,
                            filteredProducts = applyFilters(products),
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { e ->
                    // Non-critical error - categories are just for filtering
                }
                .collect { categories ->
                    updateState { copy(categories = categories) }
                }
        }
    }

    // ==================== SEARCH ====================

    private fun onSearchQueryChanged(query: String) {
        updateState {
            copy(
                searchQuery = query,
                filteredProducts = applyFilters(allProducts, searchQuery = query)
            )
        }
    }

    private fun onSearchActiveChanged(isActive: Boolean) {
        updateState { copy(isSearchActive = isActive) }

        // Clear search when deactivating
        if (!isActive && currentState.searchQuery.isNotBlank()) {
            onSearchQueryChanged("")
        }
    }

    // ==================== FILTERS ====================

    private fun onCategorySelected(categoryId: Long?) {
        updateState {
            copy(
                selectedCategoryId = categoryId,
                filteredProducts = applyFilters(allProducts, categoryId = categoryId)
            )
        }
    }

    private fun onStockFilterSelected(filter: StockFilter) {
        updateState {
            copy(
                stockFilter = filter,
                filteredProducts = applyFilters(allProducts, stockFilter = filter)
            )
        }
    }

    private fun onSortOptionSelected(option: ProductSortOption) {
        updateState {
            copy(
                sortOption = option,
                filteredProducts = applyFilters(allProducts, sortOption = option)
            )
        }
    }

    private fun clearAllFilters() {
        updateState {
            copy(
                searchQuery = "",
                selectedCategoryId = null,
                stockFilter = StockFilter.ALL,
                sortOption = ProductSortOption.NAME_ASC,
                filteredProducts = applyFilters(
                    products = allProducts,
                    searchQuery = "",
                    categoryId = null,
                    stockFilter = StockFilter.ALL,
                    sortOption = ProductSortOption.NAME_ASC
                )
            )
        }
    }

    // ==================== HELPER ====================

    /**
     * Apply filters using the FilterProductsUseCase.
     * Uses current state values as defaults for unspecified parameters.
     */
    private fun applyFilters(
        products: List<ProductWithDetails>,
        searchQuery: String = currentState.searchQuery,
        categoryId: Long? = currentState.selectedCategoryId,
        stockFilter: StockFilter = currentState.stockFilter,
        sortOption: ProductSortOption = currentState.sortOption
    ): List<ProductWithDetails> {
        return filterProductsUseCase(
            FilterProductsUseCase.FilterParams(
                products = products,
                searchQuery = searchQuery,
                categoryId = categoryId,
                stockFilter = stockFilter,
                sortOption = sortOption
            )
        )
    }

    private fun handleError(message: String) {
        updateState {
            copy(
                error = message,
                isLoading = false,
                isRefreshing = false
            )
        }
        sendEffect(ProductEffect.ShowError(message))
    }
}