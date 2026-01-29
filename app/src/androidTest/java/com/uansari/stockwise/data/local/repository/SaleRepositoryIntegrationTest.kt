package com.uansari.stockwise.data.local.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.repository.SaleRepositoryImpl
import com.uansari.stockwise.domain.model.CartItem
import com.uansari.stockwise.domain.model.MovementType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SaleRepositoryIntegrationTest {

    private lateinit var database: StockWiseDatabase
    private lateinit var saleRepository: SaleRepositoryImpl

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), StockWiseDatabase::class.java
        ).allowMainThreadQueries().build()

        saleRepository = SaleRepositoryImpl(
            database = database,
            saleDao = database.saleDao(),
            saleItemDao = database.saleItemDao(),
            productDao = database.productDao(),
            stockMovementDao = database.stockMovementDao()
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
            database.productDao().insert(
                Product(
                    id = 2,
                    name = "Laptop",
                    sku = "P2",
                    categoryId = 1,
                    buyPrice = 500.0,
                    sellPrice = 700.0,
                    currentStock = 20
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== COMPLETE SALE TESTS ====================

    @Test
    fun completeSale_createsAllRecordsAndUpdatesStock() = runTest {
        val cartItems = listOf(
            CartItem(
                productId = 1,
                productName = "Phone",
                sku = "P1",
                quantity = 2,
                unitPrice = 150.0,
                unitCost = 100.0,
                availableStock = 50
            ), CartItem(
                productId = 2,
                productName = "Laptop",
                sku = "P2",
                quantity = 1,
                unitPrice = 700.0,
                unitCost = 500.0,
                availableStock = 20
            )
        )

        val result = saleRepository.completeSale(cartItems, notes = "Test sale")

        assertThat(result.isSuccess).isTrue()

        val sale = result.getOrNull()!!
        assertThat(sale.totalAmount).isEqualTo(1000.0)  // 2*150 + 1*700
        assertThat(sale.totalCost).isEqualTo(700.0)     // 2*100 + 1*500
        assertThat(sale.totalProfit).isEqualTo(300.0)   // 1000 - 700
        assertThat(sale.itemCount).isEqualTo(2)

        // Verify sale items created
        val saleItems = database.saleItemDao().getItemsForSaleOneShot(sale.id)
        assertThat(saleItems).hasSize(2)

        // Verify stock updated
        val phone = database.productDao().getProductByIdOneShot(1)
        assertThat(phone?.currentStock).isEqualTo(48)  // 50 - 2

        val laptop = database.productDao().getProductByIdOneShot(2)
        assertThat(laptop?.currentStock).isEqualTo(19)  // 20 - 1

        // Verify stock movements created
        val phoneMovements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(phoneMovements).hasSize(1)
        assertThat(phoneMovements[0].type).isEqualTo(MovementType.SALE)
        assertThat(phoneMovements[0].quantity).isEqualTo(-2)

        val laptopMovements = database.stockMovementDao().getMovementsForProductOneShot(2)
        assertThat(laptopMovements).hasSize(1)
        assertThat(laptopMovements[0].type).isEqualTo(MovementType.SALE)
    }

    @Test
    fun completeSale_insufficientStock_rollsBackEverything() = runTest {
        val cartItems = listOf(
            CartItem(
                productId = 1,
                productName = "Phone",
                sku = "P1",
                quantity = 2,
                unitPrice = 150.0,
                unitCost = 100.0,
                availableStock = 50
            ), CartItem(
                productId = 2,
                productName = "Laptop",
                sku = "P2",
                quantity = 100,
                unitPrice = 700.0,
                unitCost = 500.0,
                availableStock = 20  // Not enough!
            )
        )

        val result = saleRepository.completeSale(cartItems)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Insufficient stock")

        // Verify NO changes made (atomic rollback)
        val sales = database.saleDao().getTotalSaleCount()
        assertThat(sales).isEqualTo(0)

        val phone = database.productDao().getProductByIdOneShot(1)
        assertThat(phone?.currentStock).isEqualTo(50)  // Unchanged

        val laptop = database.productDao().getProductByIdOneShot(2)
        assertThat(laptop?.currentStock).isEqualTo(20)  // Unchanged

        val movements = database.stockMovementDao().getMovementsForProductOneShot(1)
        assertThat(movements).isEmpty()  // No movements created
    }

    @Test
    fun completeSale_emptyCart_returnsFailure() = runTest {
        val result = saleRepository.completeSale(emptyList())

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("empty")
    }

    @Test
    fun completeSale_invalidProductId_rollsBack() = runTest {
        val cartItems = listOf(
            CartItem(
                productId = 999,  // Invalid
                productName = "Unknown",
                sku = "X1",
                quantity = 1,
                unitPrice = 100.0,
                unitCost = 50.0,
                availableStock = 10
            )
        )

        val result = saleRepository.completeSale(cartItems)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not found")
    }

    @Test
    fun completeSale_saleNumberGenerated() = runTest {
        val cartItems = listOf(
            CartItem(
                productId = 1,
                productName = "Phone",
                sku = "P1",
                quantity = 1,
                unitPrice = 150.0,
                unitCost = 100.0,
                availableStock = 50
            )
        )

        val result = saleRepository.completeSale(cartItems)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.saleNumber).startsWith("SALE-")
    }

    @Test
    fun completeSale_snapshotsProductData() = runTest {
        val cartItems = listOf(
            CartItem(
                productId = 1,
                productName = "Phone",
                sku = "P1",
                quantity = 1,
                unitPrice = 150.0,
                unitCost = 100.0,
                availableStock = 50
            )
        )

        val result = saleRepository.completeSale(cartItems)
        val saleItems = database.saleItemDao().getItemsForSaleOneShot(result.getOrNull()!!.id)

        // Verify snapshots stored
        assertThat(saleItems[0].productName).isEqualTo("Phone")
        assertThat(saleItems[0].unitPrice).isEqualTo(150.0)
        assertThat(saleItems[0].unitCost).isEqualTo(100.0)

        // Now change product name/price
        val product = database.productDao().getProductByIdOneShot(1)!!
        database.productDao().update(product.copy(name = "New Phone", sellPrice = 200.0))

        // Sale item should still have original values
        val saleItemsAfter = database.saleItemDao().getItemsForSaleOneShot(result.getOrNull()!!.id)
        assertThat(saleItemsAfter[0].productName).isEqualTo("Phone")  // Original
        assertThat(saleItemsAfter[0].unitPrice).isEqualTo(150.0)      // Original
    }
}