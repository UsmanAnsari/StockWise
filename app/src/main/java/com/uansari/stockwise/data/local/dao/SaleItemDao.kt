package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.uansari.stockwise.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(saleItem: SaleItem): Long

    @Insert
    suspend fun insertAll(saleItems: List<SaleItem>): List<Long>

    // ==================== READ ====================

    /**
     * Get all items for a sale. (Flow)
     */
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId ORDER BY id ASC")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItem>>

    /**
     * Get all items for a sale (one-shot not Flow).
     */
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId ORDER BY id ASC")
    suspend fun getItemsForSaleOneShot(saleId: Long): List<SaleItem>

    /**
     * Get sales history for a product (which sales included this product).
     */
    @Query("SELECT * FROM sale_items WHERE product_id = :productId ORDER BY id DESC")
    fun getSalesForProduct(productId: Long): Flow<List<SaleItem>>

    /**
     * Get total quantity sold for a product.
     */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sale_items WHERE product_id = :productId")
    suspend fun getTotalQuantitySold(productId: Long): Int

    /**
     * Get total revenue for a product.
     */
    @Query("SELECT COALESCE(SUM(subtotal), 0.0) FROM sale_items WHERE product_id = :productId")
    suspend fun getTotalRevenueForProduct(productId: Long): Double

    /**
     * Get total profit for a product.
     */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sale_items WHERE product_id = :productId")
    suspend fun getTotalProfitForProduct(productId: Long): Double

    // ==================== DELETE ====================

    /**
     * Delete items for a sale (handled by CASCADE).
     */
    @Query("DELETE FROM sale_items WHERE sale_id = :saleId")
    suspend fun deleteItemsForSale(saleId: Long)

    @Delete
    suspend fun delete(saleItem: SaleItem)
}