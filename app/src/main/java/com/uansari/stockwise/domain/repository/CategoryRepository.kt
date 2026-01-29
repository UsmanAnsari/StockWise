package com.uansari.stockwise.domain.repository

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(categoryId: Long): Flow<Category?>
    fun getCategoriesWithProductCount(): Flow<List<CategoryWithProductCount>>

    suspend fun insertCategory(category: Category): Result<Long>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(category: Category): Result<Unit>

    suspend fun isCategoryNameExists(name: String, excludeId: Long? = null): Boolean
    suspend fun canDeleteCategory(categoryId: Long): Boolean
}