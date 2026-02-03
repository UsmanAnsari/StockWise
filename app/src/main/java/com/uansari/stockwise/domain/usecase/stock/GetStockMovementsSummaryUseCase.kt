package com.uansari.stockwise.domain.usecase.stock

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.model.StockMovementsSummary
import javax.inject.Inject

class GetStockMovementsSummaryUseCase @Inject constructor() {
    
    operator fun invoke(movements: List<StockMovement>): StockMovementsSummary {
        if (movements.isEmpty()) {
            return StockMovementsSummary()
        }
        
        var totalIn = 0
        var totalOut = 0
        var totalAdjustments = 0
        var totalSales = 0
        
        movements.forEach { movement ->
            when (movement.type) {
                MovementType.IN -> totalIn += movement.quantity
                MovementType.OUT -> totalOut += kotlin.math.abs(movement.quantity)
                MovementType.ADJUSTMENT -> {
                    // Adjustments can be positive or negative
                    if (movement.quantity > 0) {
                        totalAdjustments += movement.quantity
                    } else {
                        totalAdjustments += movement.quantity // negative
                    }
                }
                MovementType.SALE -> totalSales += kotlin.math.abs(movement.quantity)
            }
        }
        
        return StockMovementsSummary(
            totalMovements = movements.size,
            totalIn = totalIn,
            totalOut = totalOut,
            totalAdjustments = totalAdjustments,
            totalSales = totalSales,
            netChange = totalIn - totalOut + totalAdjustments - totalSales
        )
    }
}

