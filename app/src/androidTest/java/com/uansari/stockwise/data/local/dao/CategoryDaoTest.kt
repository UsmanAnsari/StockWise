package com.uansari.stockwise.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CategoryDaoTest : BaseDaoTest() {

    private lateinit var categoryDao: CategoryDao
    private lateinit var productDao: ProductDao

    @Before
    override fun setUp() {
        super.setUp()
        categoryDao = database.categoryDao()
        productDao = database.productDao()
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertCategory_returnsId() = runTest {
        val category = Category(name = "Electronics", color = "#2196F3")

        val id = categoryDao.insert(category)

        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun insertCategory_withSameName_throwsException() = runTest {
        val category1 = Category(name = "Electronics", color = "#2196F3")
        val category2 = Category(name = "Electronics", color = "#2196F3")  // Duplicate name

        categoryDao.insert(category1)

        try {
            categoryDao.insert(category2)
            assert(false) { "Should have thrown SQLiteConstraintException" }
        } catch (e: SQLiteConstraintException) {
            // Expected
        }
    }

    @Test
    fun insertMultipleCategories_returnsAllIds() = runTest {
        val categories = listOf(
            Category(name = "Electronics", color = "#2196F3"),
            Category(name = "Clothing", color = "#2196F3"),
            Category(name = "Food", color = "#2196F3")
        )

        val ids = categoryDao.insertAll(categories)

        assertThat(ids).hasSize(3)
        assertThat(ids).containsExactly(1L, 2L, 3L)
    }

    // ==================== READ TESTS ====================

    @Test
    fun getAllCategories_returnsAllInOrder() = runTest {
        categoryDao.insert(Category(name = "Zebra", color = "#2196F3"))
        categoryDao.insert(Category(name = "Apple", color = "#2196F3"))
        categoryDao.insert(Category(name = "Mango", color = "#2196F3"))

        categoryDao.getAllCategory().test {
            val categories = awaitItem()

            assertThat(categories).hasSize(3)
            // Should be ordered by name ASC
            assertThat(categories[0].name).isEqualTo("Apple")
            assertThat(categories[1].name).isEqualTo("Mango")
            assertThat(categories[2].name).isEqualTo("Zebra")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryById_existingId_returnsCategory() = runTest {
        val id = categoryDao.insert(
            Category(
                name = "Electronics", description = "Tech stuff", color = "#2196F3"
            )
        )

        categoryDao.getCategoryById(id).test {
            val category = awaitItem()

            assertThat(category).isNotNull()
            assertThat(category?.name).isEqualTo("Electronics")
            assertThat(category?.description).isEqualTo("Tech stuff")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryById_nonExistingId_returnsNull() = runTest {
        categoryDao.getCategoryById(999).test {
            val category = awaitItem()
            assertThat(category).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoryByName_existingName_returnsCategory() = runTest {
        categoryDao.insert(Category(name = "Electronics", color = "#2196F3"))

        val category = categoryDao.getCategoryByName("Electronics")

        assertThat(category).isNotNull()
        assertThat(category?.name).isEqualTo("Electronics")
    }

    @Test
    fun getCategoryByName_nonExistingName_returnsNull() = runTest {
        val category = categoryDao.getCategoryByName("NonExistent")
        assertThat(category).isNull()
    }

    // ==================== CATEGORIES WITH PRODUCT COUNT ====================

    @Test
    fun getCategoriesWithProductCount_noProducts_returnsZeroCount() = runTest {
        categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
        categoryDao.insert(Category(id = 2, name = "Clothing", color = "#2196F3"))

        categoryDao.getCategoriesWithProductCount().test {
            val categories = awaitItem()

            assertThat(categories).hasSize(2)
            assertThat(categories[0].productCount).isEqualTo(0)
            assertThat(categories[1].productCount).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoriesWithProductCount_withProducts_returnsCorrectCount() = runTest {
        // Insert categories
        categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
        categoryDao.insert(Category(id = 2, name = "Clothing", color = "#2196F3"))

        // Insert products
        productDao.insert(
            Product(
                name = "Phone", sku = "P1", categoryId = 1, buyPrice = 100.0, sellPrice = 150.0
            )
        )
        productDao.insert(
            Product(
                name = "Laptop", sku = "P2", categoryId = 1, buyPrice = 500.0, sellPrice = 700.0
            )
        )
        productDao.insert(
            Product(
                name = "Shirt", sku = "P3", categoryId = 2, buyPrice = 10.0, sellPrice = 25.0
            )
        )

        categoryDao.getCategoriesWithProductCount().test {
            val categories = awaitItem()

            // Sorted by name: Clothing, Electronics
            assertThat(categories[0].name).isEqualTo("Clothing")
            assertThat(categories[0].productCount).isEqualTo(1)

            assertThat(categories[1].name).isEqualTo("Electronics")
            assertThat(categories[1].productCount).isEqualTo(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoriesWithProductCount_excludesInactiveProducts() = runTest {
        categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))

        productDao.insert(
            Product(
                name = "Phone",
                sku = "P1",
                categoryId = 1,
                buyPrice = 100.0,
                sellPrice = 150.0,
                isActive = true
            )
        )
        productDao.insert(
            Product(
                name = "Old Phone",
                sku = "P2",
                categoryId = 1,
                buyPrice = 50.0,
                sellPrice = 80.0,
                isActive = false  // Inactive
            )
        )

        categoryDao.getCategoriesWithProductCount().test {
            val categories = awaitItem()

            // Should only count active product
            assertThat(categories[0].productCount).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== UPDATE TESTS ====================

    @Test
    fun updateCategory_updatesSuccessfully() = runTest {
        val id = categoryDao.insert(Category(name = "Electronics", color = "#2196F3"))

        val category = categoryDao.getCategoryByIdOneShot(id)!!
        val updated = category.copy(name = "Tech", description = "Updated description")
        categoryDao.updateCategory(updated)

        val result = categoryDao.getCategoryByIdOneShot(id)
        assertThat(result?.name).isEqualTo("Tech")
        assertThat(result?.description).isEqualTo("Updated description")
    }

    // ==================== DELETE TESTS ====================

    @Test
    fun deleteCategory_noProducts_deletesSuccessfully() = runTest {
        val category = Category(name = "Electronics", color = "#2196F3")
        val id = categoryDao.insert(category)

        val toDelete = categoryDao.getCategoryByIdOneShot(id)!!
        categoryDao.delete(toDelete)

        val result = categoryDao.getCategoryByIdOneShot(id)
        assertThat(result).isNull()
    }

    @Test
    fun deleteCategory_withProducts_throwsException() = runTest {
        // Insert category and product
        categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
        productDao.insert(
            Product(
                name = "Phone", sku = "P1", categoryId = 1, buyPrice = 100.0, sellPrice = 150.0
            )
        )

        val category = categoryDao.getCategoryByIdOneShot(1)!!

        try {
            categoryDao.delete(category)
            assert(false) { "Should have thrown SQLiteConstraintException (FK RESTRICT)" }
        } catch (e: SQLiteConstraintException) {
            // Expected - can't delete category with products
        }
    }

    @Test
    fun getProductCountForCategory_returnsCorrectCount() = runTest {
        categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))

        productDao.insert(
            Product(
                name = "Phone", sku = "P1", categoryId = 1, buyPrice = 100.0, sellPrice = 150.0
            )
        )
        productDao.insert(
            Product(
                name = "Laptop", sku = "P2", categoryId = 1, buyPrice = 500.0, sellPrice = 700.0
            )
        )

        val count = categoryDao.getProductCountForCategory(1)

        assertThat(count).isEqualTo(2)
    }

    // ==================== FLOW REACTIVITY TEST ====================

    @Test
    fun getAllCategories_emitsOnChange() = runTest {
        categoryDao.getAllCategory().test {
            // Initial empty state
            assertThat(awaitItem()).isEmpty()

            // Insert a category
            categoryDao.insert(Category(name = "Electronics", color = "#2196F3"))

            // Flow should emit new list
            val afterInsert = awaitItem()
            assertThat(afterInsert).hasSize(1)

            // Insert another
            categoryDao.insert(Category(name = "Clothing", color = "#2196F3"))

            // Flow should emit updated list
            val afterSecondInsert = awaitItem()
            assertThat(afterSecondInsert).hasSize(2)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
