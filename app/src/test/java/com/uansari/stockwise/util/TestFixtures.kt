package com.uansari.stockwise.util

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.model.ProductUnitType

object TestFixtures {

    // ==================== CATEGORIES ====================

    fun createCategory(
        id: Long = 1L,
        name: String = "Electronics",
        description: String? = "Electronic devices",
        color: String = "#2196F3"
    ) = Category(
        id = id,
        name = name,
        description = description,
        color = color,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun createCategoryWithProductCount(
        id: Long = 1L, name: String = "Electronics", productCount: Int = 5
    ) = CategoryWithProductCount(
        id = id,
        name = name,
        description = "Test category",
        color = "#2196F3",
        productCount = productCount,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    val sampleCategories = listOf(
        createCategory(1L, "Electronics"),
        createCategory(2L, "Clothing"),
        createCategory(3L, "Food & Beverages")
    )

    // ==================== SUPPLIERS ====================

    fun createSupplier(
        id: Long = 1L,
        name: String = "Acme Supplies",
        contactPerson: String? = "John Doe",
        phone: String? = "+44 123 456 7890",
        email: String? = "john@acme.com"
    ) = Supplier(
        id = id,
        name = name,
        contactPerson = contactPerson,
        phone = phone,
        email = email,
        address = "123 Business St",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun createSupplierWithProductCount(
        id: Long = 1L, name: String = "Acme Supplies", productCount: Int = 3
    ) = SupplierWithProductCount(
        id = id,
        name = name,
        contactPerson = "John Doe",
        phone = "+44 123 456 7890",
        email = "john@acme.com",
        address = "123 Business St",
        productCount = productCount,
        updatedAt = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis(),
        notes = "Sample Note"
    )

    val sampleSuppliers = listOf(
        createSupplier(1L, "Acme Supplies"),
        createSupplier(2L, "Global Trading"),
        createSupplier(3L, "Local Vendor")
    )

    // ==================== PRODUCTS ====================

    fun createProduct(
        id: Long = 1L,
        name: String = "iPhone 15",
        sku: String = "IPHONE-15",
        categoryId: Long = 1L,
        supplierId: Long? = 1L,
        buyPrice: Double = 800.0,
        sellPrice: Double = 999.0,
        currentStock: Int = 50,
        lowStockThreshold: Int = 10,
        isActive: Boolean = true
    ) = Product(
        id = id,
        name = name,
        sku = sku,
        description = "Test product description",
        categoryId = categoryId,
        supplierId = supplierId,
        buyPrice = buyPrice,
        sellPrice = sellPrice,
        currentStock = currentStock,
        lowStockThreshold = lowStockThreshold,
        unit = ProductUnitType.PCS,
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun createProductWithDetails(
        product: Product = createProduct(),
        category: Category = createCategory(),
        supplier: Supplier? = createSupplier()
    ) = ProductWithDetails(
        product = product, category = category, supplier = supplier
    )

    val sampleProducts = listOf(
        createProduct(1L, "iPhone 15", "IPHONE-15", currentStock = 50),
        createProduct(2L, "Samsung Galaxy", "SAMSUNG-S24", currentStock = 30),
        createProduct(3L, "MacBook Pro", "MACBOOK-PRO", currentStock = 5, lowStockThreshold = 10),
        createProduct(4L, "iPad Air", "IPAD-AIR", currentStock = 0),
        createProduct(5L, "AirPods Pro", "AIRPODS-PRO", currentStock = 100)
    )

    val sampleProductsWithDetails = sampleProducts.map { product ->
        createProductWithDetails(
            product = product,
            category = sampleCategories.first(),
            supplier = sampleSuppliers.first()
        )
    }

    // ==================== STOCK MOVEMENTS ====================

    fun createStockMovement(
        id: Long = 1L,
        productId: Long = 1L,
        type: MovementType = MovementType.IN,
        quantity: Int = 10,
        previousStock: Int = 40,
        newStock: Int = 50,
        reference: String? = "PO-001",
        notes: String? = null
    ) = StockMovement(
        id = id,
        productId = productId,
        type = type,
        quantity = quantity,
        previousStock = previousStock,
        newStock = newStock,
        reference = reference,
        notes = notes,
        createdAt = System.currentTimeMillis()
    )

    val sampleStockMovements = listOf(
        createStockMovement(
            1L, type = MovementType.IN, quantity = 50, previousStock = 0, newStock = 50
        ), createStockMovement(
            2L, type = MovementType.SALE, quantity = -5, previousStock = 50, newStock = 45
        ), createStockMovement(
            3L, type = MovementType.ADJUSTMENT, quantity = 5, previousStock = 45, newStock = 50
        ), createStockMovement(
            4L, type = MovementType.OUT, quantity = -10, previousStock = 50, newStock = 40
        )
    )
}