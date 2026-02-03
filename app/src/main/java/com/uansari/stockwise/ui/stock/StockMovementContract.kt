package com.uansari.stockwise.ui.stock

import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.QuickStockAction
import com.uansari.stockwise.domain.model.StockMovementsSummary
import com.uansari.stockwise.domain.usecase.stock.MovementTypeFilter
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState
import java.time.LocalDate

data class StockMovementState(
    // Loading states
    val isLoading: Boolean = true,
    val isPerformingAction: Boolean = false,

    // Product info
    val product: Product? = null,

    // Movements
    val allMovements: List<StockMovement> = emptyList(),
    val filteredMovements: List<StockMovement> = emptyList(),
    val summary: StockMovementsSummary = StockMovementsSummary(),

    // Filters
    val typeFilter: MovementTypeFilter = MovementTypeFilter.ALL,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    // UI State
    val isFilterExpanded: Boolean = false,
    val isQuickStockDialogVisible: Boolean = false,
    val quickStockAction: QuickStockAction? = null,
    val isDatePickerVisible: Boolean = false,
    val datePickerTarget: DatePickerTarget? = null,

    // Error
    val error: String? = null
) : UiState {

    val isProductLoaded: Boolean
        get() = product != null

    val productName: String
        get() = product?.name ?: ""

    val currentStock: Int
        get() = product?.currentStock ?: 0

    val hasFilters: Boolean
        get() = typeFilter != MovementTypeFilter.ALL || startDate != null || endDate != null

    val isEmpty: Boolean
        get() = filteredMovements.isEmpty() && !isLoading

    val hasNoMovements: Boolean
        get() = allMovements.isEmpty() && !isLoading

    val dateRangeText: String?
        get() {
            return when {
                startDate != null && endDate != null -> "$startDate - $endDate"
                startDate != null -> "From $startDate"
                endDate != null -> "Until $endDate"
                else -> null
            }
        }
}

/**
 * Target for date picker (start or end date).
 */
enum class DatePickerTarget {
    START_DATE, END_DATE
}

// ==================== EVENTS (Intents) ====================

sealed interface StockMovementEvent : UiEvent {
    // Lifecycle
    data object LoadData : StockMovementEvent
    data object Refresh : StockMovementEvent

    // Filters
    data class OnTypeFilterChanged(val filter: MovementTypeFilter) : StockMovementEvent
    data class OnStartDateChanged(val date: LocalDate?) : StockMovementEvent
    data class OnEndDateChanged(val date: LocalDate?) : StockMovementEvent
    data object ClearFilters : StockMovementEvent
    data object ToggleFilterExpanded : StockMovementEvent

    // Date Picker
    data class ShowDatePicker(val target: DatePickerTarget) : StockMovementEvent
    data object HideDatePicker : StockMovementEvent
    data class OnDateSelected(val date: LocalDate) : StockMovementEvent

    // Quick Stock Actions
    data class ShowQuickStockDialog(val action: QuickStockAction) : StockMovementEvent
    data object HideQuickStockDialog : StockMovementEvent
    data class PerformQuickStock(val quantity: Int, val notes: String?) : StockMovementEvent

    // Navigation
    data object NavigateBack : StockMovementEvent

    // Error
    data object ClearError : StockMovementEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface StockMovementEffect : UiEffect {
    data object NavigateBack : StockMovementEffect
    data class ShowSnackbar(val message: String) : StockMovementEffect
}