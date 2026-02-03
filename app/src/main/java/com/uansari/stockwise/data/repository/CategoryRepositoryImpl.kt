package com.uansari.stockwise.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.uansari.stockwise.data.local.dao.CategoryDao
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategory()


    override fun getCategoryById(categoryId: Long): Flow<Category?> =
        categoryDao.getCategoryById(categoryId)

    override suspend fun getCategoryByIdOneShot(categoryId: Long): Category? {
        return categoryDao.getCategoryByIdOneShot(categoryId)
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }

    override fun getCategoriesWithProductCount(): Flow<List<CategoryWithProductCount>> =
        categoryDao.getCategoriesWithProductCount().map { entities -> entities.map { it } }

    override suspend fun insertCategory(category: Category): Result<Long> {
        return try {
            val id = categoryDao.insert(category)
            Result.success(id)
        } catch (e: SQLiteConstraintException) {
            Result.failure(Exception("Category name already exists"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            val existing = categoryDao.getCategoryByName(category.name)
            if (existing != null && existing.id != category.id) {
                return Result.failure(Exception("Category name already exists"))
            }
            categoryDao.updateCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        return try {
            categoryDao.deleteById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isCategoryNameExists(
        name: String, excludeId: Long?
    ): Boolean {
        val existing = categoryDao.getCategoryByName(name)
        return existing != null && existing.id != excludeId
    }

    override suspend fun canDeleteCategory(categoryId: Long): Boolean {
        return categoryDao.getProductCountForCategory(categoryId) == 0
    }
}