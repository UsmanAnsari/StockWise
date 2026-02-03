package com.uansari.stockwise.ui.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.QuickStockAction
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.stock.AddStockUseCase
import com.uansari.stockwise.domain.usecase.stock.AdjustStockUseCase
import com.uansari.stockwise.domain.usecase.stock.FilterStockMovementsUseCase
import com.uansari.stockwise.domain.usecase.stock.GetStockMovementsForProductUseCase
import com.uansari.stockwise.domain.usecase.stock.GetStockMovementsSummaryUseCase
import com.uansari.stockwise.domain.usecase.stock.MovementTypeFilter
import com.uansari.stockwise.domain.usecase.stock.RemoveStockUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import com.uansari.stockwise.ui.navigation.Screen
import com.uansari.stockwise.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StockMovementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val getStockMovementsForProductUseCase: GetStockMovementsForProductUseCase,
    private val filterStockMovementsUseCase: FilterStockMovementsUseCase,
    private val getStockMovementsSummaryUseCase: GetStockMovementsSummaryUseCase,
    private val addStockUseCase: AddStockUseCase,
    private val removeStockUseCase: RemoveStockUseCase,
    private val adjustStockUseCase: AdjustStockUseCase
) : BaseViewModel<StockMovementState, StockMovementEvent, StockMovementEffect>(StockMovementState()) {

    private val productId: Long = savedStateHandle.get<Long>(Screen.StockMovement.ARG_PRODUCT_ID)
        ?: throw IllegalArgumentException("Product ID is required")

    init {
        onEvent(StockMovementEvent.LoadData)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: StockMovementEvent) {
        when (event) {
            // Lifecycle
            is StockMovementEvent.LoadData -> loadData()
            is StockMovementEvent.Refresh -> refresh()

            // Filters
            is StockMovementEvent.OnTypeFilterChanged -> onTypeFilterChanged(event.filter)
            is StockMovementEvent.OnStartDateChanged -> onStartDateChanged(event.date)
            is StockMovementEvent.OnEndDateChanged -> onEndDateChanged(event.date)
            is StockMovementEvent.ClearFilters -> clearFilters()
            is StockMovementEvent.ToggleFilterExpanded -> {
                updateState { copy(isFilterExpanded = !isFilterExpanded) }
            }

            // Date Picker
            is StockMovementEvent.ShowDatePicker -> {
                updateState {
                    copy(
                        isDatePickerVisible = true, datePickerTarget = event.target
                    )
                }
            }

            is StockMovementEvent.HideDatePicker -> {
                updateState {
                    copy(
                        isDatePickerVisible = false, datePickerTarget = null
                    )
                }
            }

            is StockMovementEvent.OnDateSelected -> onDateSelected(event.date)

            // Quick Stock Actions
            is StockMovementEvent.ShowQuickStockDialog -> {
                updateState {
                    copy(
                        isQuickStockDialogVisible = true, quickStockAction = event.action
                    )
                }
            }

            is StockMovementEvent.HideQuickStockDialog -> {
                updateState {
                    copy(
                        isQuickStockDialogVisible = false, quickStockAction = null
                    )
                }
            }

            is StockMovementEvent.PerformQuickStock -> performQuickStock(
                event.quantity, event.notes
            )

            // Navigation
            is StockMovementEvent.NavigateBack -> sendEffect(StockMovementEffect.NavigateBack)

            // Error
            is StockMovementEvent.ClearError -> updateState { copy(error = null) }
        }
    }

    // ==================== LOAD DATA ====================

    private fun loadData() {
        loadProduct()
        observeMovements()
    }

    private fun refresh() {
        updateState { copy(isLoading = true, error = null) }
        loadData()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductByIdOneShot(productId)
                updateState { copy(product = product) }
            } catch (e: Exception) {
                handleError(e.message ?: "Failed to load product")
            }
        }
    }

    private fun observeMovements() {
        viewModelScope.launch {
            getStockMovementsForProductUseCase(productId).catch { e ->
                handleError(e.message ?: "Failed to load movements")
            }.collect { movements ->
                updateState {
                    val filtered = applyFilters(movements)
                    val summary = getStockMovementsSummaryUseCase(filtered)

                    copy(
                        allMovements = movements,
                        filteredMovements = filtered,
                        summary = summary,
                        isLoading = false
                    )
                }
            }
        }
    }

    // ==================== FILTERS ====================

    private fun onTypeFilterChanged(filter: MovementTypeFilter) {
        updateState {
            val filtered = applyFilters(allMovements, typeFilter = filter)
            val summary = getStockMovementsSummaryUseCase(filtered)

            copy(
                typeFilter = filter, filteredMovements = filtered, summary = summary
            )
        }
    }

    private fun onStartDateChanged(date: LocalDate?) {
        updateState {
            val filtered = applyFilters(allMovements, startDate = date)
            val summary = getStockMovementsSummaryUseCase(filtered)

            copy(
                startDate = date, filteredMovements = filtered, summary = summary
            )
        }
    }

    private fun onEndDateChanged(date: LocalDate?) {
        updateState {
            val filtered = applyFilters(allMovements, endDate = date)
            val summary = getStockMovementsSummaryUseCase(filtered)

            copy(
                endDate = date, filteredMovements = filtered, summary = summary
            )
        }
    }

    private fun onDateSelected(date: LocalDate) {
        when (currentState.datePickerTarget) {
            DatePickerTarget.START_DATE -> onStartDateChanged(date)
            DatePickerTarget.END_DATE -> onEndDateChanged(date)
            null -> { /* Ignore */
            }
        }
        updateState {
            copy(
                isDatePickerVisible = false, datePickerTarget = null
            )
        }
    }

    private fun clearFilters() {
        updateState {
            val summary = getStockMovementsSummaryUseCase(allMovements)

            copy(
                typeFilter = MovementTypeFilter.ALL,
                startDate = null,
                endDate = null,
                filteredMovements = allMovements,
                summary = summary
            )
        }
    }

    private fun applyFilters(
        movements: List<StockMovement>,
        typeFilter: MovementTypeFilter = currentState.typeFilter,
        startDate: LocalDate? = currentState.startDate,
        endDate: LocalDate? = currentState.endDate
    ): List<StockMovement> {
        return filterStockMovementsUseCase(
            FilterStockMovementsUseCase.FilterParams(
                movements = movements,
                typeFilter = typeFilter,
                startDate = startDate?.let { DateTimeUtils.getStartOfDay(it) },
                endDate = endDate?.let { DateTimeUtils.getEndOfDay(it) })
        )
    }

    // ==================== QUICK STOCK ====================

    private fun performQuickStock(quantity: Int, notes: String?) {
        val action = currentState.quickStockAction ?: return

        viewModelScope.launch {
            updateState { copy(isPerformingAction = true) }

            val result = when (action) {
                QuickStockAction.ADD_STOCK -> addStockUseCase(
                    AddStockUseCase.Params(
                        productId = productId, quantity = quantity, notes = notes
                    )
                )

                QuickStockAction.REMOVE_STOCK -> removeStockUseCase(
                    RemoveStockUseCase.Params(
                        productId = productId, quantity = quantity, notes = notes
                    )
                )

                QuickStockAction.ADJUST_STOCK -> adjustStockUseCase(
                    AdjustStockUseCase.Params(
                        productId = productId, newStockLevel = quantity, notes = notes
                    )
                )
            }

            result.onSuccess {
                updateState {
                    copy(
                        isPerformingAction = false,
                        isQuickStockDialogVisible = false,
                        quickStockAction = null
                    )
                }
                // Reload product to get updated stock
                loadProduct()
                sendEffect(StockMovementEffect.ShowSnackbar("Stock updated successfully"))
            }.onFailure { error ->
                updateState { copy(isPerformingAction = false) }
                sendEffect(
                    StockMovementEffect.ShowSnackbar(
                        error.message ?: "Failed to update stock"
                    )
                )
            }
        }
    }

    // ==================== ERROR HANDLING ====================

    private fun handleError(message: String) {
        updateState {
            copy(
                error = message, isLoading = false
            )
        }
    }
}