package com.uansari.stockwise.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.Supplier
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProductDaoTest : BaseDaoTest() {

    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var supplierDao: SupplierDao

    @Before
    override fun setUp() {
        super.setUp()
        productDao = database.productDao()
        categoryDao = database.categoryDao()
        supplierDao = database.supplierDao()

        // Insert required category for FK constraint
        runTest {
            categoryDao.insert(Category(id = 1, name = "Electronics", color = "#2196F3"))
            categoryDao.insert(Category(id = 2, name = "Clothing", color = "#2196F3"))
            supplierDao.insert(Supplier(id = 1, name = "TechWorld"))
        }
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertProduct_returnsId() = runTest {
        val product = createProduct(name = "Phone", sku = "P1")

        val id = productDao.insert(product)

        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun insertProduct_duplicateSku_throwsException() = runTest {
        productDao.insert(createProduct(name = "Phone", sku = "P1"))

        try {
            productDao.insert(createProduct(name = "Laptop", sku = "P1"))  // Same SKU
            assert(false) { "Should have thrown SQLiteConstraintException" }
        } catch (e: SQLiteConstraintException) {
            // Expected
        }
    }

    @Test
    fun insertProduct_invalidCategoryId_throwsException() = runTest {
        val product = createProduct(name = "Phone", sku = "P1", categoryId = 999)  // Invalid

        try {
            productDao.insert(product)
            assert(false) { "Should have thrown SQLiteConstraintException" }
        } catch (e: SQLiteConstraintException) {
            // Expected - FK constraint
        }
    }

    // ==================== READ WITH RELATIONS ====================

    @Test
    fun getProductsWithDetails_includesCategoryAndSupplier() = runTest {
        productDao.insert(
            createProduct(
                name = "Phone", sku = "P1", categoryId = 1, supplierId = 1
            )
        )

        productDao.getProductsWithDetails().test {
            val products = awaitItem()

            assertThat(products).hasSize(1)
            assertThat(products[0].product.name).isEqualTo("Phone")
            assertThat(products[0].category.name).isEqualTo("Electronics")
            assertThat(products[0].supplier?.name).isEqualTo("TechWorld")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getProductsWithDetails_nullSupplier_handledCorrectly() = runTest {
        productDao.insert(
            createProduct(
                name = "Phone", sku = "P1", categoryId = 1, supplierId = null  // No supplier
            )
        )

        productDao.getProductsWithDetails().test {
            val products = awaitItem()

            assertThat(products[0].supplier).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getProductsByCategory_returnsOnlyMatchingProducts() = runTest {
        productDao.insert(createProduct(name = "Phone", sku = "P1", categoryId = 1))
        productDao.insert(createProduct(name = "Laptop", sku = "P2", categoryId = 1))
        productDao.insert(createProduct(name = "Shirt", sku = "P3", categoryId = 2))

        productDao.getProductsByCategoryWithDetails(1).test {
            val products = awaitItem()

            assertThat(products).hasSize(2)
            assertThat(products.map { it.product.name }).containsExactly("Laptop", "Phone")

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== SEARCH TESTS ====================

    @Test
    fun searchProducts_byName_returnsMatches() = runTest {
        productDao.insert(createProduct(name = "iPhone 15 Case", sku = "P1"))
        productDao.insert(createProduct(name = "Samsung Case", sku = "P2"))
        productDao.insert(createProduct(name = "USB Cable", sku = "P3"))

        productDao.searchProducts("Case").test {
            val products = awaitItem()

            assertThat(products).hasSize(2)
            assertThat(products.map { it.product.name }).containsExactly(
                "iPhone 15 Case", "Samsung Case"
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchProducts_bySku_returnsMatches() = runTest {
        productDao.insert(createProduct(name = "Phone", sku = "ELEC-001"))
        productDao.insert(createProduct(name = "Laptop", sku = "ELEC-002"))
        productDao.insert(createProduct(name = "Shirt", sku = "CLTH-001"))

        productDao.searchProducts("ELEC").test {
            val products = awaitItem()

            assertThat(products).hasSize(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchProducts_caseInsensitive() = runTest {
        productDao.insert(createProduct(name = "iPhone Case", sku = "P1"))

        productDao.searchProducts("iphone").test {
            val products = awaitItem()
            assertThat(products).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== LOW STOCK TESTS ====================

    @Test
    fun getLowStockProducts_returnsBelowThreshold() = runTest {
        productDao.insert(
            createProduct(
                name = "Low Stock Item", sku = "P1", currentStock = 5, lowStockThreshold = 10
            )
        )
        productDao.insert(
            createProduct(
                name = "OK Stock Item", sku = "P2", currentStock = 50, lowStockThreshold = 10
            )
        )
        productDao.insert(
            createProduct(
                name = "At Threshold",
                sku = "P3",
                currentStock = 10,
                lowStockThreshold = 10  // Equal counts as low
            )
        )

        productDao.getLowStockProducts().test {
            val products = awaitItem()

            assertThat(products).hasSize(2)
            assertThat(products.map { it.product.name }).containsExactly(
                "At Threshold",
                "Low Stock Item"
            )  // Sorted by stock ASC

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getOutOfStockProducts_returnsZeroStock() = runTest {
        productDao.insert(createProduct(name = "Out", sku = "P1", currentStock = 0))
        productDao.insert(createProduct(name = "Has Stock", sku = "P2", currentStock = 10))

        productDao.getOutOfStockProducts().test {
            val products = awaitItem()

            assertThat(products).hasSize(1)
            assertThat(products[0].product.name).isEqualTo("Out")

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== AGGREGATION TESTS ====================

    @Test
    fun getInventoryStats_calculatesCorrectly() = runTest {
        productDao.insert(
            createProduct(
                name = "P1", sku = "P1", currentStock = 10, buyPrice = 5.0, lowStockThreshold = 5
            )
        )
        productDao.insert(
            createProduct(
                name = "P2",
                sku = "P2",
                currentStock = 20,
                buyPrice = 10.0,
                lowStockThreshold = 25  // Low stock
            )
        )
        productDao.insert(
            createProduct(
                name = "P3",
                sku = "P3",
                currentStock = 5,
                buyPrice = 20.0,
                lowStockThreshold = 10  // Low stock
            )
        )

        productDao.getInventoryStats().test {
            val stats = awaitItem()

            // Total value: (10*5) + (20*10) + (5*20) = 50 + 200 + 100 = 350
            assertThat(stats.totalValue).isEqualTo(350.0)
            assertThat(stats.totalProducts).isEqualTo(3)
            assertThat(stats.lowStockCount).isEqualTo(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getInventoryStats_emptyDatabase_returnsZeros() = runTest {
        productDao.getInventoryStats().test {
            val stats = awaitItem()

            assertThat(stats.totalValue).isEqualTo(0.0)
            assertThat(stats.totalProducts).isEqualTo(0)
            assertThat(stats.lowStockCount).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== SOFT DELETE TESTS ====================

    @Test
    fun softDelete_setsIsActiveFalse() = runTest {
        val id = productDao.insert(createProduct(name = "Phone", sku = "P1"))

        productDao.softDelete(id)

        val product = productDao.getProductByIdOneShot(id)
        assertThat(product?.isActive).isFalse()
    }

    @Test
    fun getAllActiveProducts_excludesSoftDeleted() = runTest {
        productDao.insert(createProduct(name = "Active", sku = "P1"))
        val id2 = productDao.insert(createProduct(name = "Deleted", sku = "P2"))

        productDao.softDelete(id2)

        productDao.getAllActiveProducts().test {
            val products = awaitItem()

            assertThat(products).hasSize(1)
            assertThat(products[0].name).isEqualTo("Active")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun restore_setsIsActiveTrue() = runTest {
        val id = productDao.insert(createProduct(name = "Phone", sku = "P1", isActive = false))

        productDao.restore(id)

        val product = productDao.getProductByIdOneShot(id)
        assertThat(product?.isActive).isTrue()
    }

    // ==================== UPDATE STOCK ====================

    @Test
    fun updateStock_updatesCorrectly() = runTest {
        val id = productDao.insert(createProduct(name = "Phone", sku = "P1", currentStock = 50))

        productDao.updateStock(id, 45)

        val product = productDao.getProductByIdOneShot(id)
        assertThat(product?.currentStock).isEqualTo(45)
    }

    // ==================== FOREIGN KEY BEHAVIOR ====================

    @Test
    fun deleteSupplier_setsProductSupplierToNull() = runTest {
        productDao.insert(createProduct(name = "Phone", sku = "P1", supplierId = 1))

        val supplier = supplierDao.getSupplierByIdOneShot(1)!!
        supplierDao.delete(supplier.id)

        val product = productDao.getProductByIdOneShot(1)
        assertThat(product?.supplierId).isNull()  // SET_NULL behavior
    }

    // ==================== HELPER ====================

    private fun createProduct(
        name: String,
        sku: String,
        categoryId: Long = 1,
        supplierId: Long? = null,
        buyPrice: Double = 10.0,
        sellPrice: Double = 20.0,
        currentStock: Int = 100,
        lowStockThreshold: Int = 10,
        isActive: Boolean = true
    ) = Product(
        name = name,
        sku = sku,
        categoryId = categoryId,
        supplierId = supplierId,
        buyPrice = buyPrice,
        sellPrice = sellPrice,
        currentStock = currentStock,
        lowStockThreshold = lowStockThreshold,
        isActive = isActive
    )
}