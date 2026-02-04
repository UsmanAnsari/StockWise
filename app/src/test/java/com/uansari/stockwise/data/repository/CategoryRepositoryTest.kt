package com.uansari.stockwise.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.dao.CategoryDao
import com.uansari.stockwise.data.local.entity.Category
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        categoryDao = mockk()
        repository = CategoryRepositoryImpl(categoryDao)
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun `insertCategory success returns id`() = runTest {
        val category = Category(name = "Electronics", color = "#2196F3")
        coEvery { categoryDao.insert(category) } returns 1L

        val result = repository.insertCategory(category)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)
    }

    @Test
    fun `insertCategory duplicate name returns failure`() = runTest {
        val category = Category(name = "Electronics", color = "#2196F3")
        coEvery { categoryDao.insert(category) } throws SQLiteConstraintException()

        val result = repository.insertCategory(category)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("already exists")
    }

    // ==================== UPDATE TESTS ====================

    @Test
    fun `updateCategory with unique name succeeds`() = runTest {
        val category = Category(id = 1, name = "Updated Name", color = "#2196F3")
        coEvery { categoryDao.getCategoryByName("Updated Name") } returns null
        coEvery { categoryDao.updateCategory(category) } just runs

        val result = repository.updateCategory(category)

        assertThat(result.isSuccess).isTrue()
        coVerify { categoryDao.updateCategory(category) }
    }

    @Test
    fun `updateCategory with conflicting name fails`() = runTest {
        val category = Category(id = 1, name = "Electronics", color = "#2196F3")
        val existingCategory =
            Category(id = 2, name = "Electronics", color = "#2196F3")  // Different ID
        coEvery { categoryDao.getCategoryByName("Electronics") } returns existingCategory

        val result = repository.updateCategory(category)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("already exists")
        coVerify(exactly = 0) { categoryDao.updateCategory(any()) }
    }

    @Test
    fun `updateCategory with same name same id succeeds`() = runTest {
        val category = Category(id = 1, name = "Electronics", color = "#2196F3")
        val existingCategory = Category(id = 1, name = "Electronics", color = "#2196F3")  // Same ID
        coEvery { categoryDao.getCategoryByName("Electronics") } returns existingCategory
        coEvery { categoryDao.updateCategory(category) } just runs

        val result = repository.updateCategory(category)

        assertThat(result.isSuccess).isTrue()
    }


    // ==================== VALIDATION TESTS ====================

    @Test
    fun `isCategoryNameExists returns true when exists`() = runTest {
        coEvery { categoryDao.getCategoryByName("Electronics") } returns Category(
            id = 1,
            name = "Electronics",
            color = "#2196F3"
        )

        val exists = repository.isCategoryNameExists("Electronics")

        assertThat(exists).isTrue()
    }

    @Test
    fun `isCategoryNameExists with excludeId returns false for same id`() = runTest {
        coEvery { categoryDao.getCategoryByName("Electronics") } returns Category(
            id = 1,
            name = "Electronics",
            color = "#2196F3"
        )

        val exists = repository.isCategoryNameExists("Electronics", excludeId = 1)

        assertThat(exists).isFalse()  // Same ID, so not a conflict
    }

    @Test
    fun `canDeleteCategory returns true when no products`() = runTest {
        coEvery { categoryDao.getProductCountForCategory(1) } returns 0

        val canDelete = repository.canDeleteCategory(1)

        assertThat(canDelete).isTrue()
    }

    @Test
    fun `canDeleteCategory returns false when has products`() = runTest {
        coEvery { categoryDao.getProductCountForCategory(1) } returns 3

        val canDelete = repository.canDeleteCategory(1)

        assertThat(canDelete).isFalse()
    }
}
