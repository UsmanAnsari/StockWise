package com.uansari.stockwise.domain.usecase.stock

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.MovementType
import javax.inject.Inject

class FilterStockMovementsUseCase @Inject constructor() {

    operator fun invoke(params: FilterParams): List<StockMovement> {
        return params.movements
            .applyTypeFilter(params.typeFilter)
            .applyDateFilter(params.startDate, params.endDate)
            .sortedByDescending { it.createdAt }
    }

    private fun List<StockMovement>.applyTypeFilter(
        typeFilter: MovementTypeFilter
    ): List<StockMovement> {
        return when (typeFilter) {
            MovementTypeFilter.ALL -> this
            MovementTypeFilter.STOCK_IN -> filter { it.type == MovementType.IN }
            MovementTypeFilter.STOCK_OUT -> filter { it.type == MovementType.OUT }
            MovementTypeFilter.ADJUSTMENT -> filter { it.type == MovementType.ADJUSTMENT }
            MovementTypeFilter.SALE -> filter { it.type == MovementType.SALE }
        }
    }

    private fun List<StockMovement>.applyDateFilter(
        startDate: Long?,
        endDate: Long?
    ): List<StockMovement> {
        return filter { movement ->
            val afterStart = startDate == null || movement.createdAt >= startDate
            val beforeEnd = endDate == null || movement.createdAt <= endDate
            afterStart && beforeEnd
        }
    }

    data class FilterParams(
        val movements: List<StockMovement>,
        val typeFilter: MovementTypeFilter = MovementTypeFilter.ALL,
        val startDate: Long? = null,
        val endDate: Long? = null
    )
}

/**
 * Filter options for movement types.
 */
enum class MovementTypeFilter {
    ALL,
    STOCK_IN,
    STOCK_OUT,
    ADJUSTMENT,
    SALE;

    val displayName: String
        get() = when (this) {
            ALL -> "All"
            STOCK_IN -> "Stock In"
            STOCK_OUT -> "Stock Out"
            ADJUSTMENT -> "Adjustments"
            SALE -> "Sales"
        }
}