package com.uansari.stockwise.domain.repository

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.MovementType
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>
    fun getRecentMovements(limit: Int = 10): Flow<List<StockMovement>>
    fun getMovementsByType(type: MovementType): Flow<List<StockMovement>>
    fun getMovementsInRange(startTime: Long, endTime: Long): Flow<List<StockMovement>>

    // Stock operations (these are atomic)
    suspend fun addStock(
        productId: Long,
        quantity: Int,
        unitCost: Double? = null,
        reference: String? = null,
        notes: String? = null
    ): Result<StockMovement>

    suspend fun removeStock(
        productId: Long,
        quantity: Int,
        reference: String? = null,
        notes: String? = null
    ): Result<StockMovement>

    suspend fun adjustStock(
        productId: Long,
        newStockLevel: Int,
        notes: String? = null
    ): Result<StockMovement>

    // For internal use by SaleRepository
    suspend fun recordSaleMovement(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        reference: String
    ): StockMovement
}