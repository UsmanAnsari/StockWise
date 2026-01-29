package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categoryList: List<Category>): List<Long>

    // ==================== READ ====================

    /**
     * Get all categories ordered by name.
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategory(): Flow<List<Category>>

    /**
     * Get a single category by ID (Flow).
     */
    @Query("SELECT * FROM categories WHERE id=:categoryId")
    fun getCategoryById(categoryId: Long): Flow<Category?>

    /**
     * Get a single category by ID (one-shot, not Flow).
     * Useful for validation.
     */
    @Query("SELECT * FROM categories WHERE id=:categoryId")
    suspend fun getCategoryByIdOneShot(categoryId: Long): Category?

    /**
     * Get category by name
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    /**
     * Get all categories with count of products in each.
     * Uses LEFT JOIN to include categories with 0 products.
     */
    @Query(
        """
        SELECT 
            c.id,
            c.name,
            c.description,
            c.color,
            c.created_at,
            COUNT(p.id) as product_count
        FROM categories c
        LEFT JOIN products p ON c.id = p.category_id AND p.is_active = 1
        GROUP BY c.id
        ORDER BY c.name ASC
    """
    )
    fun getCategoriesWithProductCount(): Flow<List<CategoryWithProductCount>>

    /**
     * Get count of categories.
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryByName(): Int

    // ==================== UPDATE ====================

    @Update
    suspend fun updateCategory(category: Category)

    // ==================== DELETE ====================

    /**
     * Delete a category unless there are not products with the respective category (FK RESTRICT).
     */
    @Delete
    suspend fun delete(category: Category)

    /**
     * Check if category can be deleted (no products using it).
     */
    @Query("SELECT COUNT(*) FROM products WHERE category_id = :categoryId AND is_active = 1")
    suspend fun getProductCountForCategory(categoryId: Long): Int
}