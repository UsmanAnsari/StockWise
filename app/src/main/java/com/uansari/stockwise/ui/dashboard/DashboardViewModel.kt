package com.uansari.stockwise.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.domain.usecase.dashboard.GetDailySalesSummaryUseCase
import com.uansari.stockwise.domain.usecase.dashboard.GetInventoryStatsUseCase
import com.uansari.stockwise.domain.usecase.dashboard.GetLowStockProductsUseCase
import com.uansari.stockwise.domain.usecase.dashboard.GetRecentSalesUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getInventoryStatsUseCase: GetInventoryStatsUseCase,
    private val getDailySalesSummaryUseCase: GetDailySalesSummaryUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    private val getRecentSalesUseCase: GetRecentSalesUseCase
) : BaseViewModel<DashboardState, DashboardEvent, DashboardEffect>(
    DashboardState()
) {

    init {
        onEvent(DashboardEvent.LoadDashboard)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadDashboard -> loadDashboard()
            is DashboardEvent.Refresh -> refresh()
            is DashboardEvent.ClearError -> clearError()

            // Navigation events → trigger side effects
            is DashboardEvent.NavigateToProducts -> sendEffect(DashboardEffect.NavigateToProducts)
            is DashboardEvent.NavigateToNewSale -> sendEffect(DashboardEffect.NavigateToNewSale)
            is DashboardEvent.NavigateToLowStock -> sendEffect(DashboardEffect.NavigateToLowStock)
            is DashboardEvent.NavigateToSaleDetail -> sendEffect(
                DashboardEffect.NavigateToSaleDetail(
                    event.saleId
                )
            )

            is DashboardEvent.NavigateToProductDetail -> sendEffect(
                DashboardEffect.NavigateToProductDetail(
                    event.productId
                )
            )
        }
    }

    // ==================== PRIVATE METHODS ====================

    private fun loadDashboard() {
        observeInventoryStats()
        observeDailySummary()
        observeLowStockProducts()
        observeRecentSales()
    }

    private fun refresh() {
        updateState { copy(isRefreshing = true, error = null) }
        loadDashboard()
    }

    private fun clearError() {
        updateState { copy(error = null) }
    }

    // ==================== USE CASE OBSERVERS ====================

    private fun observeInventoryStats() {
        viewModelScope.launch {
            getInventoryStatsUseCase().catch { e ->
                handleError(e.message ?: "Failed to load inventory stats")
            }.collect { stats ->
                updateState {
                    copy(
                        inventoryStats = stats, isLoading = false, isRefreshing = false
                    )
                }
            }
        }
    }

    private fun observeDailySummary() {
        viewModelScope.launch {
            getDailySalesSummaryUseCase.forToday().catch { e ->
                handleError(e.message ?: "Failed to load daily summary")
            }.collect { summary ->
                updateState { copy(dailySummary = summary) }
            }
        }
    }

    private fun observeLowStockProducts() {
        viewModelScope.launch {
            getLowStockProductsUseCase().catch { e ->
                handleError(e.message ?: "Failed to load low stock products")
            }.collect { products ->
                updateState { copy(lowStockProducts = products) }
            }
        }
    }

    private fun observeRecentSales() {
        viewModelScope.launch {
            getRecentSalesUseCase(GetRecentSalesUseCase.DEFAULT_LIMIT).catch { e ->
                handleError(e.message ?: "Failed to load recent sales")
            }.collect { sales ->
                updateState {
                    copy(
                        recentSales = sales, isLoading = false, isRefreshing = false
                    )
                }
            }
        }
    }

    private fun handleError(message: String) {
        updateState {
            copy(
                error = message, isLoading = false, isRefreshing = false
            )
        }
        sendEffect(DashboardEffect.ShowError(message))
    }
}