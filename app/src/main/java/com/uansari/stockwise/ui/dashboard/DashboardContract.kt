package com.uansari.stockwise.ui.dashboard

import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.relations.DailySalesSummary
import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState


// ==================== STATE ====================

data class DashboardState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,

    // Data
    val inventoryStats: InventoryStats = InventoryStats(
        totalValue = 0.0, totalProducts = 0, lowStockCount = 0
    ),
    val dailySummary: DailySalesSummary = DailySalesSummary(
        totalSales = 0.0, totalProfit = 0.0, saleCount = 0
    ),
    val lowStockProducts: List<ProductWithDetails> = emptyList(),
    val recentSales: List<Sale> = emptyList(),

    // Error
    val error: String? = null
) : UiState {

    val hasLowStockAlert: Boolean
        get() = lowStockProducts.isNotEmpty()

    val profitMargin: Double
        get() = if (dailySummary.totalSales > 0) {
            (dailySummary.totalProfit / dailySummary.totalSales) * 100
        } else 0.0
}

// ==================== EVENTS (Intents) ====================

sealed interface DashboardEvent : UiEvent {
    data object LoadDashboard : DashboardEvent
    data object Refresh : DashboardEvent
    data object ClearError : DashboardEvent

    // Navigation events (handled by ViewModel, trigger side effects)
    data object NavigateToProducts : DashboardEvent
    data object NavigateToNewSale : DashboardEvent
    data object NavigateToLowStock : DashboardEvent
    data class NavigateToSaleDetail(val saleId: Long) : DashboardEvent
    data class NavigateToProductDetail(val productId: Long) : DashboardEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface DashboardEffect : UiEffect {
    data object NavigateToProducts : DashboardEffect
    data object NavigateToNewSale : DashboardEffect
    data object NavigateToLowStock : DashboardEffect
    data class NavigateToSaleDetail(val saleId: Long) : DashboardEffect
    data class NavigateToProductDetail(val productId: Long) : DashboardEffect
    data class ShowError(val message: String) : DashboardEffect
}
