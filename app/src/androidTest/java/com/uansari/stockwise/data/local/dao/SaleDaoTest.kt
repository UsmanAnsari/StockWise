package com.uansari.stockwise.data.local.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.SaleItem
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SaleDaoTest : BaseDaoTest() {

    private lateinit var saleDao: SaleDao
    private lateinit var saleItemDao: SaleItemDao
    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao

    @Before
    override fun setUp() {
        super.setUp()
        saleDao = database.saleDao()
        saleItemDao = database.saleItemDao()
        productDao = database.productDao()
        categoryDao = database.categoryDao()

        // Setup required data
        runTest {
            categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
            productDao.insert(
                Product(
                    id = 1,
                    name = "Phone",
                    sku = "P1",
                    categoryId = 1,
                    buyPrice = 100.0,
                    sellPrice = 150.0
                )
            )
            productDao.insert(
                Product(
                    id = 2,
                    name = "Laptop",
                    sku = "P2",
                    categoryId = 1,
                    buyPrice = 500.0,
                    sellPrice = 700.0
                )
            )
        }
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertSale_returnsId() = runTest {
        val sale = createSale(saleNumber = "SALE-001")

        val id = saleDao.insert(sale)

        assertThat(id).isGreaterThan(0)
    }

    // ==================== SALE WITH ITEMS ====================

    @Test
    fun getSaleWithItems_returnsAllItems() = runTest {
        val saleId = saleDao.insert(createSale(saleNumber = "SALE-001"))

        saleItemDao.insertAll(
            listOf(
                createSaleItem(saleId = saleId, productId = 1, productName = "Phone"),
                createSaleItem(saleId = saleId, productId = 2, productName = "Laptop")
            )
        )

        saleDao.getSaleWithItems(saleId).test {
            val saleWithItems = awaitItem()

            assertThat(saleWithItems).isNotNull()
            assertThat(saleWithItems?.saleItem).hasSize(2)
            assertThat(saleWithItems?.saleItem?.map { it.productName }).containsExactly(
                "Phone", "Laptop"
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== CASCADE DELETE ====================

    @Test
    fun deleteSale_deletesItemsToo() = runTest {
        val saleId = saleDao.insert(createSale(saleNumber = "SALE-001"))

        saleItemDao.insertAll(
            listOf(
                createSaleItem(saleId = saleId, productId = 1, productName = "Phone"),
                createSaleItem(saleId = saleId, productId = 2, productName = "Laptop")
            )
        )

        // Verify items exist
        var items = saleItemDao.getItemsForSaleOneShot(saleId)
        assertThat(items).hasSize(2)

        // Delete sale
        saleDao.deleteById(saleId)

        // Items should be gone (CASCADE)
        items = saleItemDao.getItemsForSaleOneShot(saleId)
        assertThat(items).isEmpty()
    }

    // ==================== DATE RANGE QUERIES ====================

    @Test
    fun getSalesInRange_returnsOnlyInRange() = runTest {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfYesterday =
            today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfTwoDaysAgo =
            today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        saleDao.insert(createSale("SALE-001", createdAt = startOfToday + 1000))
        saleDao.insert(createSale("SALE-002", createdAt = startOfYesterday + 1000))
        saleDao.insert(createSale("SALE-003", createdAt = startOfTwoDaysAgo + 1000))

        // Get only yesterday's sales
        saleDao.getSalesInRange(startOfYesterday, startOfToday).test {
            val sales = awaitItem()

            assertThat(sales).hasSize(1)
            assertThat(sales[0].saleNumber).isEqualTo("SALE-002")

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== AGGREGATION TESTS ====================

    @Test
    fun getDailySummary_calculatesCorrectly() = runTest {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        saleDao.insert(
            createSale(
                saleNumber = "SALE-001",
                totalAmount = 100.0,
                totalProfit = 30.0,
                createdAt = startOfToday + 1000
            )
        )
        saleDao.insert(
            createSale(
                saleNumber = "SALE-002",
                totalAmount = 200.0,
                totalProfit = 50.0,
                createdAt = startOfToday + 2000
            )
        )

        saleDao.getDailySummary(startOfToday).test {
            val summary = awaitItem()

            assertThat(summary.totalSales).isEqualTo(300.0)
            assertThat(summary.totalProfit).isEqualTo(80.0)
            assertThat(summary.saleCount).isEqualTo(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDailySummary_noSales_returnsZeros() = runTest {
        val startOfToday =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        saleDao.getDailySummary(startOfToday).test {
            val summary = awaitItem()

            assertThat(summary.totalSales).isEqualTo(0.0)
            assertThat(summary.totalProfit).isEqualTo(0.0)
            assertThat(summary.saleCount).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTodaySaleCount_correctForSaleNumber() = runTest {
        val startOfToday =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        saleDao.insert(createSale("SALE-001", createdAt = startOfToday + 1000))
        saleDao.insert(createSale("SALE-002", createdAt = startOfToday + 2000))

        val count = saleDao.getTodaySaleCount(startOfToday)

        assertThat(count).isEqualTo(2)
    }

    // ==================== HELPERS ====================

    private fun createSale(
        saleNumber: String,
        totalAmount: Double = 100.0,
        totalCost: Double = 60.0,
        totalProfit: Double = 40.0,
        itemCount: Int = 1,
        createdAt: Long = System.currentTimeMillis()
    ) = Sale(
        saleNumber = saleNumber,
        totalAmount = totalAmount,
        totalCost = totalCost,
        totalProfit = totalProfit,
        itemCount = itemCount,
        createdAt = createdAt
    )

    private fun createSaleItem(
        saleId: Long,
        productId: Long,
        productName: String,
        quantity: Int = 1,
        unitPrice: Double = 100.0,
        unitCost: Double = 60.0
    ) = SaleItem(
        saleId = saleId,
        productId = productId,
        productName = productName,
        quantity = quantity,
        unitPrice = unitPrice,
        unitCost = unitCost,
        subtotal = quantity * unitPrice,
        profit = (unitPrice - unitCost) * quantity
    )
}