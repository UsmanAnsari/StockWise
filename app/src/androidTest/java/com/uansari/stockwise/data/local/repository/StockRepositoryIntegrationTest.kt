package com.uansari.stockwise.data.local.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.repository.StockRepositoryImpl
import com.uansari.stockwise.domain.model.MovementType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class StockRepositoryIntegrationTest {

    private lateinit var database: StockWiseDatabase
    private lateinit var stockRepository: StockRepositoryImpl

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), StockWiseDatabase::class.java
        ).allowMainThreadQueries().build()

        stockRepository = StockRepositoryImpl(
            database = database,
            stockMovementDao = database.stockMovementDao(),
            productDao = database.productDao()
        )

        // Setup test data
        runTest {
            database.categoryDao().insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
            database.productDao().insert(
                Product(
                    id = 1,
                    name = "Phone",
                    sku = "P1",
                    categoryId = 1,
                    buyPrice = 100.0,
                    sellPrice = 150.0,
                    currentStock = 50
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== ADD STOCK TESTS ====================

    @Test
    fun addStock_updatesProductAndCreatesMovement() = runTest {
        val result = stockRepository.addStock(
            productId = 1,
            quantity = 10,
            unitCost = 100.0,
            reference = "Restock",
            notes = "From supplier"
        )

        assertThat(result.isSuccess).isTrue()

        // Verify product stock updated
        val product = database.productDao().getProductByIdOneShot(1)
        assertThat(product?.currentStock).isEqualTo(60)  // 50 + 10

        // Verify movement created
        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements).hasSize(1)
        assertThat(movements[0].type).isEqualTo(MovementType.IN)
        assertThat(movements[0].quantity).isEqualTo(10)
        assertThat(movements[0].previousStock).isEqualTo(50)
        assertThat(movements[0].newStock).isEqualTo(60)
    }

    @Test
    fun addStock_invalidProductId_returnsFailure() = runTest {
        val result = stockRepository.addStock(productId = 999, quantity = 10)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Product not found")
    }

    @Test
    fun addStock_negativeQuantity_throwsException() = runTest {
        try {
            stockRepository.addStock(productId = 1, quantity = -5)
            assert(false) { "Should have thrown IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("positive")
        }
    }

    // ==================== REMOVE STOCK TESTS ====================

    @Test
    fun removeStock_updatesProductAndCreatesMovement() = runTest {
        val result = stockRepository.removeStock(
            productId = 1, quantity = 5, reference = "Damaged", notes = "Broken in storage"
        )

        assertThat(result.isSuccess).isTrue()

        val product = database.productDao().getProductByIdOneShot(1)
        assertThat(product?.currentStock).isEqualTo(45)  // 50 - 5

        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements[0].type).isEqualTo(MovementType.OUT)
        assertThat(movements[0].quantity).isEqualTo(-5)  // Negative for OUT
    }

    @Test
    fun removeStock_insufficientStock_returnsFailure() = runTest {
        val result = stockRepository.removeStock(productId = 1, quantity = 100)  // Only have 50

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Insufficient stock")

        // Verify no changes made (atomic rollback)
        val product = database.productDao().getProductByIdOneShot(1)
        assertThat(product?.currentStock).isEqualTo(50)  // Unchanged

        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements).isEmpty()  // No movement created
    }

    // ==================== ADJUST STOCK TESTS ====================

    @Test
    fun adjustStock_increase_createsPositiveAdjustment() = runTest {
        val result = stockRepository.adjustStock(
            productId = 1, newStockLevel = 60,  // From 50
            notes = "Found extra stock"
        )

        assertThat(result.isSuccess).isTrue()

        val product = database.productDao().getProductByIdOneShot(1)
        assertThat(product?.currentStock).isEqualTo(60)

        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements[0].type).isEqualTo(MovementType.ADJUSTMENT)
        assertThat(movements[0].quantity).isEqualTo(10)  // Positive adjustment
    }

    @Test
    fun adjustStock_decrease_createsNegativeAdjustment() = runTest {
        val result = stockRepository.adjustStock(
            productId = 1, newStockLevel = 45,  // From 50
            notes = "Inventory count correction"
        )

        assertThat(result.isSuccess).isTrue()

        val product = database.productDao().getProductByIdOneShot(1)
        assertThat(product?.currentStock).isEqualTo(45)

        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements[0].quantity).isEqualTo(-5)  // Negative adjustment
    }

    @Test
    fun adjustStock_sameLevel_returnsFailure() = runTest {
        val result =
            stockRepository.adjustStock(productId = 1, newStockLevel = 50)  // Same as current

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("same as current")
    }

    @Test
    fun adjustStock_negativeLevel_throwsException() = runTest {
        try {
            stockRepository.adjustStock(productId = 1, newStockLevel = -10)
            assert(false) { "Should have thrown" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("negative")
        }
    }
}