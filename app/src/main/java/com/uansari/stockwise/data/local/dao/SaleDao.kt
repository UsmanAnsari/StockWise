package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.relations.DailySalesSummary
import com.uansari.stockwise.data.local.entity.relations.SaleWithItems
import com.uansari.stockwise.data.local.entity.relations.SalesSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(sale: Sale): Long

    // ==================== READ - Basic ====================

    /**
     * Get all sales ordered by date (newest first).
     */
    @Query("SELECT * FROM sales ORDER BY created_at DESC")
    fun getAllSales(): Flow<List<Sale>>

    /**
     * Get a single sale by ID.
     */
    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun getSaleById(saleId: Long): Flow<Sale?>

    /**
     * Get a single sale by ID (one-shot).
     */
    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleByIdOneShot(saleId: Long): Sale?

    /**
     * Get sale by sale number.
     */
    @Query("SELECT * FROM sales WHERE sale_number = :saleNumber")
    suspend fun getSaleBySaleNumber(saleNumber: String): Sale?

    // ==================== READ - With Relations ====================

    /**
     * Get all sales with their items.
     */
    @Transaction
    @Query("SELECT * FROM sales ORDER BY created_at DESC")
    fun getSalesWithItems(): Flow<List<SaleWithItems>>

    /**
     * Get a single sale with its items.
     */
    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun getSaleWithItems(saleId: Long): Flow<SaleWithItems?>

    /**
     * Get a single sale with items (one-shot).
     */
    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleWithItemsOneShot(saleId: Long): SaleWithItems?

    /**
     * Get recent sales with items.
     */
    @Transaction
    @Query("SELECT * FROM sales ORDER BY created_at DESC LIMIT :limit")
    fun getRecentSalesWithItems(limit: Int = 10): Flow<List<SaleWithItems>>

    // ==================== READ - Date Filters ====================

    /**
     * Get sales in a date range.
     */
    @Query(
        """
        SELECT * FROM sales 
        WHERE created_at >= :startTime AND created_at < :endTime
        ORDER BY created_at DESC
    """
    )
    fun getSalesInRange(startTime: Long, endTime: Long): Flow<List<Sale>>

    /**
     * Get sales in range with items.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM sales 
        WHERE created_at >= :startTime AND created_at < :endTime
        ORDER BY created_at DESC
    """
    )
    fun getSalesInRangeWithItems(startTime: Long, endTime: Long): Flow<List<SaleWithItems>>

    /**
     * Get today's sales.
     */
    @Query("SELECT * FROM sales WHERE created_at >= :startOfDay ORDER BY created_at DESC")
    fun getTodaySales(startOfDay: Long): Flow<List<Sale>>

    // ==================== READ - Aggregations ====================

    /**
     * Get daily summary (for dashboard).
     */
    @Query(
        """
        SELECT 
            COALESCE(SUM(total_amount), 0.0) as totalSales,
            COALESCE(SUM(total_Profit), 0.0) as totalProfit,
            COUNT(*) as saleCount
        FROM sales 
        WHERE created_at >= :startOfDay
    """
    )
    fun getDailySummary(startOfDay: Long): Flow<DailySalesSummary>

    /**
     * Get summary for a date range.
     */
    @Query(
        """
        SELECT 
            COALESCE(SUM(total_amount), 0.0) as totalSales,
            COALESCE(SUM(total_cost), 0.0) as totalCost,
            COALESCE(SUM(total_Profit), 0.0) as totalProfit,
            COUNT(*) as saleCount,
            COALESCE(SUM(item_count), 0) as itemsSold
        FROM sales 
        WHERE created_at >= :startTime AND created_at < :endTime
    """
    )
    fun getSalesSummary(startTime: Long, endTime: Long): Flow<SalesSummary>

    /**
     * Get total sales amount for today.
     */
    @Query("SELECT COALESCE(SUM(total_amount), 0.0) FROM sales WHERE created_at >= :startOfDay")
    fun getTodayTotalSales(startOfDay: Long): Flow<Double>

    /**
     * Get total profit for today.
     */
    @Query("SELECT COALESCE(SUM(total_Profit), 0.0) FROM sales WHERE created_at >= :startOfDay")
    fun getTodayTotalProfit(startOfDay: Long): Flow<Double>

    /**
     * Get count of sales today (for generating sale number).
     */
    @Query("SELECT COUNT(*) FROM sales WHERE created_at >= :startOfDay")
    suspend fun getTodaySaleCount(startOfDay: Long): Int

    /**
     * Get total sales count.
     */
    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getTotalSaleCount(): Int

    // ==================== DELETE ====================

    /**
     * Delete a sale.
     * SaleItems will be deleted automatically (CASCADE).
     */
    @Delete
    suspend fun delete(sale: Sale)

    /**
     * Delete sale by ID.
     */
    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteById(saleId: Long)
}