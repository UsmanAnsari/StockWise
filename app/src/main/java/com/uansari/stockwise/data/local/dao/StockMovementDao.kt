package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.MovementType
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(stockMovement: StockMovement): Long

    @Insert
    suspend fun insertAll(stockMovements: List<StockMovement>): List<Long>

    // ==================== READ ====================

    /**
     * Get all movements ordered by date (newest first).
     */
    @Query("SELECT * FROM stock_movements ORDER BY created_at DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    /**
     * Get movements for a specific product.
     */
    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY created_at DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    /**
     * Get movements for a specific product (one-shot not Flow).
     */
    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY created_at DESC")
    suspend fun getMovementsForProductOneShot(productId: Long): List<StockMovement>

    /**
     * Get movements by type.
     */
    @Query("SELECT * FROM stock_movements WHERE type = :type ORDER BY created_at DESC")
    fun getMovementsByType(type: MovementType): Flow<List<StockMovement>>

    /**
     * Get movements in a date range.
     */
    @Query(
        """
        SELECT * FROM stock_movements 
        WHERE created_at >= :startTime AND created_at < :endTime
        ORDER BY created_at DESC
    """
    )
    fun getMovementsInRange(startTime: Long, endTime: Long): Flow<List<StockMovement>>

    /**
     * Get recent movements (for dashboard).
     */
    @Query("SELECT * FROM stock_movements ORDER BY created_at DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 10): Flow<List<StockMovement>>

    /**
     * Get the last movement for a product (to verify current stock).
     */
    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastMovementForProduct(productId: Long): StockMovement?

    /**
     * Get total quantity IN for a product (for verification).
     */
    @Query(
        """
        SELECT COALESCE(SUM(quantity), 0) 
        FROM stock_movements 
        WHERE product_id = :productId AND type = 'IN'
    """
    )
    suspend fun getTotalStockIn(productId: Long): Int

    /**
     * Get total quantity OUT/SALE for a product.
     */
    @Query(
        """
        SELECT COALESCE(SUM(ABS(quantity)), 0) 
        FROM stock_movements 
        WHERE product_id = :productId AND type IN ('OUT', 'SALE')
    """
    )
    suspend fun getTotalStockOut(productId: Long): Int

    // ==================== DELETE ====================

    /**
     * Delete movements for a product.
     * Typically only for data cleanup.
     */
    @Query("DELETE FROM stock_movements WHERE product_id = :productId")
    suspend fun deleteMovementsForProduct(productId: Long)

    @Delete
    suspend fun delete(movement: StockMovement)
}