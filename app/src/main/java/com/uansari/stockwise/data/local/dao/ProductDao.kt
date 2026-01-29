package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(product: Product): Long

    @Insert
    suspend fun insertAllProducts(products: List<Product>): List<Long>

    // ==================== READ - Basic ====================

    /**
     * Get all products including inactive.
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Get all active products ordered by name.
     */
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    /**
     * Get a single product by ID. (Flow)
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Long): Flow<Product?>

    /**
     * Get a single product by ID (one-shot not Flow).
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdOneShot(productId: Long): Product?

    /**
     * Get product by SKU.
     */
    @Query("SELECT * FROM products WHERE sku = :productSku LIMIT 1")
    suspend fun getProductBySku(productSku: String): Product?

    // ==================== READ - With Relations ====================

    /**
     * Get all active products with their category and supplier details.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getProductsWithDetails(): Flow<List<ProductWithDetails>>

    /**
     * Get a single product with details.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductWithDetails(productId: Long): Flow<ProductWithDetails?>

    /**
     * Get products by category with details.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE category_id = :categoryId AND is_active = 1 ORDER BY name ASC")
    fun getProductsByCategoryWithDetails(categoryId: Long): Flow<List<ProductWithDetails>>

    /**
     * Get products by supplier with details.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE supplier_id = :supplierId AND is_active = 1 ORDER BY name ASC")
    fun getProductsBySupplierWithDetails(supplierId: Long): Flow<List<ProductWithDetails>>

    // ==================== READ - Search & Filter ====================

    /**
     * Search products by name or SKU.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM products 
        WHERE is_active = 1 
          AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')
        ORDER BY name ASC
    """
    )
    fun searchProducts(query: String): Flow<List<ProductWithDetails>>

    /**
     * Get low stock products (stock <= threshold).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM products 
        WHERE is_active = 1 AND current_stock <= low_stock_threshold
        ORDER BY current_stock ASC
    """
    )
    fun getLowStockProducts(): Flow<List<ProductWithDetails>>

    /**
     * Get out of stock products.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 AND current_stock = 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<ProductWithDetails>>

    /**
     * Get products filtered by category.
     */
    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 AND category_id = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductWithDetails>>

    // ==================== READ - Aggregations ====================

    /**
     * Get inventory statistics for dashboard.
     */
    @Query(
        """
        SELECT 
            COALESCE(SUM(current_stock * buy_price), 0.0) as totalValue,
            COUNT(*) as totalProducts,
            SUM(CASE WHEN current_stock <= low_stock_threshold THEN 1 ELSE 0 END) as lowStockCount
        FROM products 
        WHERE is_active = 1
    """
    )
    fun getInventoryStats(): Flow<InventoryStats>

    /**
     * Get total inventory value.
     */
    @Query("SELECT COALESCE(SUM(current_stock * buy_price), 0.0) FROM products WHERE is_active = 1")
    fun getTotalInventoryValue(): Flow<Double>

    /**
     * Get count of active products.
     */
    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    suspend fun getActiveProductCount(): Int

    /**
     * Get count of low stock products.
     */
    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1 AND current_stock <= low_stock_threshold")
    suspend fun getLowStockCount(): Int

    // ==================== UPDATE ====================

    @Update
    suspend fun update(product: Product)

    /**
     * Update stock level directly.
     */
    @Query("UPDATE products SET current_stock = :newStock, updated_at = :updatedAt WHERE id = :productId")
    suspend fun updateStock(
        productId: Long,
        newStock: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    // ==================== DELETE ====================

    /**
     * Soft delete - set is_active to false.
     */
    @Query("UPDATE products SET is_active = 0, updated_at = :updatedAt WHERE id = :productId")
    suspend fun softDelete(productId: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * Restore soft-deleted product.
     */
    @Query("UPDATE products SET is_active = 1, updated_at = :updatedAt WHERE id = :productId")
    suspend fun restore(productId: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * Hard delete - only use if no stock movements or sales reference this product.
     * Generally prefer softDelete().
     */
    @Delete
    suspend fun hardDelete(product: Product)

}