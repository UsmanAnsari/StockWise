package com.uansari.stockwise.ui.product_detail

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState
import com.uansari.stockwise.domain.model.QuickStockAction

data class ProductDetailState(
    // Loading states
    val isLoading: Boolean = true,
    val isLoadingMovements: Boolean = true,

    // Data
    val productWithDetails: ProductWithDetails? = null,
    val stockMovements: List<StockMovement> = emptyList(),

    // Dialog states
    val isDeleteDialogVisible: Boolean = false,
    val isQuickStockDialogVisible: Boolean = false,
    val quickStockAction: QuickStockAction? = null,

    // Operation states
    val isPerformingAction: Boolean = false,

    // Error
    val error: String? = null
) : UiState {

    // Convenience accessors
    val product: Product?
        get() = productWithDetails?.product

    val category: Category?
        get() = productWithDetails?.category

    val supplier: Supplier?
        get() = productWithDetails?.supplier

    val isProductLoaded: Boolean
        get() = productWithDetails != null

    val currentStock: Int
        get() = product?.currentStock ?: 0

    val recentMovements: List<StockMovement>
        get() = stockMovements.take(RECENT_MOVEMENTS_COUNT)

    val hasMoreMovements: Boolean
        get() = stockMovements.size > RECENT_MOVEMENTS_COUNT

    val stockValue: Double
        get() = product?.let { it.currentStock * it.buyPrice } ?: 0.0

    val profitPerUnit: Double
        get() = product?.let { it.sellPrice - it.buyPrice } ?: 0.0

    companion object {
        const val RECENT_MOVEMENTS_COUNT = 2
    }
}

// ==================== EVENTS (Intents) ====================

sealed interface ProductDetailEvent : UiEvent {
    // Lifecycle
    data object LoadProduct : ProductDetailEvent
    data object Refresh : ProductDetailEvent

    // Quick Stock Actions
    data class ShowQuickStockDialog(val action: QuickStockAction) : ProductDetailEvent
    data object HideQuickStockDialog : ProductDetailEvent
    data class PerformQuickStock(val quantity: Int, val notes: String?) : ProductDetailEvent

    // Delete
    data object ShowDeleteDialog : ProductDetailEvent
    data object HideDeleteDialog : ProductDetailEvent
    data object ConfirmDelete : ProductDetailEvent

    // Navigation
    data object NavigateBack : ProductDetailEvent
    data object NavigateToEdit : ProductDetailEvent
    data object NavigateToStockHistory : ProductDetailEvent

    // Error
    data object ClearError : ProductDetailEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface ProductDetailEffect : UiEffect {
    data object NavigateBack : ProductDetailEffect
    data class NavigateToEdit(val productId: Long) : ProductDetailEffect
    data class NavigateToStockHistory(val productId: Long) : ProductDetailEffect
    data class ShowSnackbar(val message: String) : ProductDetailEffect
    data object ProductDeleted : ProductDetailEffect
}