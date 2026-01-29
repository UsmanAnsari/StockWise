package com.uansari.stockwise.di

import com.uansari.stockwise.data.local.dao.CategoryDao
import com.uansari.stockwise.data.local.dao.ProductDao
import com.uansari.stockwise.data.local.dao.SaleDao
import com.uansari.stockwise.data.local.dao.SaleItemDao
import com.uansari.stockwise.data.local.dao.StockMovementDao
import com.uansari.stockwise.data.local.dao.SupplierDao
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.SaleItem
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.model.ProductUnitType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

suspend fun seedCategories(categoryDao: CategoryDao) {
    val categories = listOf(
        Category(
            id = 1,
            name = "Electronics",
            description = "Electronic devices and accessories",
            color = "#2196F3"  // Blue
        ),
        Category(
            id = 2,
            name = "Clothing",
            description = "Apparel and fashion items",
            color = "#E91E63"  // Pink
        ),
        Category(
            id = 3,
            name = "Accessories",
            description = "Phone cases, bags, jewelry",
            color = "#FF9800"  // Orange
        ),
        Category(
            id = 4,
            name = "Food & Beverage",
            description = "Snacks, drinks, groceries",
            color = "#4CAF50"  // Green
        ),
        Category(
            id = 5,
            name = "Home & Office",
            description = "Home decor and office supplies",
            color = "#9C27B0"  // Purple
        )
    )
    categoryDao.insertAll(categories)
}

// ==================== SUPPLIERS ====================

suspend fun seedSuppliers(supplierDao: SupplierDao) {
    val suppliers = listOf(
        Supplier(
            id = 1,
            name = "TechWorld Distributors",
            contactPerson = "John Smith",
            phone = "+44 20 7123 4567",
            email = "orders@techworld.com",
            address = "123 Tech Lane, London, EC1A 1BB",
            notes = "Main electronics supplier. 30-day payment terms."
        ),
        Supplier(
            id = 2,
            name = "Fashion Hub Wholesale",
            contactPerson = "Sarah Johnson",
            phone = "+44 20 7234 5678",
            email = "wholesale@fashionhub.com",
            address = "456 Style Street, Manchester, M1 1AA",
            notes = "Clothing and accessories. Minimum order £500."
        ),
        Supplier(
            id = 3,
            name = "General Supplies Co",
            contactPerson = "Mike Brown",
            phone = "+44 20 7345 6789",
            email = "sales@generalsupplies.com",
            address = "789 Trade Road, Birmingham, B1 1BB",
            notes = "Mixed inventory supplier. Quick delivery."
        )
    )
    supplierDao.insertAll(suppliers)
}

// ==================== PRODUCTS ====================

suspend fun seedProducts(productDao: ProductDao) {
    val products = listOf(
        // Electronics (Category 1)
        Product(
            id = 1,
            name = "iPhone 15 Case - Clear",
            sku = "ELEC-001",
            description = "Crystal clear protective case for iPhone 15",
            categoryId = 1,
            supplierId = 1,
            buyPrice = 3.50,
            sellPrice = 12.99,
            currentStock = 45,
            lowStockThreshold = 10,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 2,
            name = "USB-C Cable 2m",
            sku = "ELEC-002",
            description = "Fast charging USB-C to USB-C cable, 2 meters",
            categoryId = 1,
            supplierId = 1,
            buyPrice = 2.00,
            sellPrice = 8.99,
            currentStock = 120,
            lowStockThreshold = 20,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 3,
            name = "Wireless Earbuds",
            sku = "ELEC-003",
            description = "Bluetooth 5.0 wireless earbuds with charging case",
            categoryId = 1,
            supplierId = 1,
            buyPrice = 12.00,
            sellPrice = 34.99,
            currentStock = 8,  // Low stock!
            lowStockThreshold = 15,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 4,
            name = "Screen Protector - Universal",
            sku = "ELEC-004",
            description = "Tempered glass screen protector, fits most phones",
            categoryId = 1,
            supplierId = 1,
            buyPrice = 0.80,
            sellPrice = 4.99,
            currentStock = 200,
            lowStockThreshold = 30,
            unit = ProductUnitType.PCS
        ),

        // Clothing (Category 2)
        Product(
            id = 5,
            name = "Basic T-Shirt - White",
            sku = "CLTH-001",
            description = "100% cotton basic t-shirt, white, various sizes",
            categoryId = 2,
            supplierId = 2,
            buyPrice = 4.00,
            sellPrice = 14.99,
            currentStock = 75,
            lowStockThreshold = 20,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 6,
            name = "Basic T-Shirt - Black",
            sku = "CLTH-002",
            description = "100% cotton basic t-shirt, black, various sizes",
            categoryId = 2,
            supplierId = 2,
            buyPrice = 4.00,
            sellPrice = 14.99,
            currentStock = 60,
            lowStockThreshold = 20,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 7,
            name = "Denim Jeans - Classic",
            sku = "CLTH-003",
            description = "Classic fit denim jeans, various sizes",
            categoryId = 2,
            supplierId = 2,
            buyPrice = 15.00,
            sellPrice = 44.99,
            currentStock = 5,  // Low stock!
            lowStockThreshold = 10,
            unit = ProductUnitType.PCS
        ),

        // Accessories (Category 3)
        Product(
            id = 8,
            name = "Canvas Tote Bag",
            sku = "ACCS-001",
            description = "Eco-friendly canvas tote bag",
            categoryId = 3,
            supplierId = 2,
            buyPrice = 3.00,
            sellPrice = 9.99,
            currentStock = 40,
            lowStockThreshold = 15,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 9,
            name = "Leather Wallet - Brown",
            sku = "ACCS-002",
            description = "Genuine leather bifold wallet",
            categoryId = 3,
            supplierId = 3,
            buyPrice = 8.00,
            sellPrice = 24.99,
            currentStock = 25,
            lowStockThreshold = 10,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 10,
            name = "Sunglasses - Classic",
            sku = "ACCS-003",
            description = "UV400 protection sunglasses",
            categoryId = 3,
            supplierId = 3,
            buyPrice = 5.00,
            sellPrice = 19.99,
            currentStock = 0,  // Out of stock!
            lowStockThreshold = 10,
            unit = ProductUnitType.PCS
        ),

        // Food & Beverage (Category 4)
        Product(
            id = 11,
            name = "Energy Drink - 250ml",
            sku = "FOOD-001",
            description = "Energy drink, 250ml can",
            categoryId = 4,
            supplierId = 3,
            buyPrice = 0.60,
            sellPrice = 1.99,
            currentStock = 150,
            lowStockThreshold = 50,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 12,
            name = "Protein Bar - Chocolate",
            sku = "FOOD-002",
            description = "High protein snack bar, chocolate flavor",
            categoryId = 4,
            supplierId = 3,
            buyPrice = 0.80,
            sellPrice = 2.49,
            currentStock = 80,
            lowStockThreshold = 30,
            unit = ProductUnitType.PCS
        ),

        // Home & Office (Category 5)
        Product(
            id = 13,
            name = "Notebook - A5 Lined",
            sku = "HOME-001",
            description = "A5 lined notebook, 100 pages",
            categoryId = 5,
            supplierId = 3,
            buyPrice = 1.20,
            sellPrice = 4.99,
            currentStock = 100,
            lowStockThreshold = 25,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 14,
            name = "Desk Organizer",
            sku = "HOME-002",
            description = "Wooden desk organizer with compartments",
            categoryId = 5,
            supplierId = 3,
            buyPrice = 6.00,
            sellPrice = 18.99,
            currentStock = 15,
            lowStockThreshold = 5,
            unit = ProductUnitType.PCS
        ),
        Product(
            id = 15,
            name = "LED Desk Lamp",
            sku = "HOME-003",
            description = "Adjustable LED desk lamp with USB port",
            categoryId = 5,
            supplierId = 1,
            buyPrice = 10.00,
            sellPrice = 29.99,
            currentStock = 3,  // Low stock!
            lowStockThreshold = 5,
            unit = ProductUnitType.PCS
        )
    )
    productDao.insertAllProducts(products)
}

// ==================== STOCK MOVEMENTS ====================

suspend fun seedStockMovements(
    stockMovementDao: StockMovementDao,
    productDao: ProductDao
) {
    // Create initial "IN" movements for all products
    // This represents the initial stock received

    val now = System.currentTimeMillis()
    val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)

    val movements = mutableListOf<StockMovement>()

    // Initial stock for each product (happened a week ago)
    val initialStockData = listOf(
        Triple(1L, 50, 3.50),   // iPhone Case: received 50
        Triple(2L, 130, 2.00),  // USB Cable: received 130
        Triple(3L, 25, 12.00),  // Wireless Earbuds: received 25
        Triple(4L, 220, 0.80),  // Screen Protector: received 220
        Triple(5L, 80, 4.00),   // T-Shirt White: received 80
        Triple(6L, 70, 4.00),   // T-Shirt Black: received 70
        Triple(7L, 20, 15.00),  // Denim Jeans: received 20
        Triple(8L, 50, 3.00),   // Tote Bag: received 50
        Triple(9L, 30, 8.00),   // Wallet: received 30
        Triple(10L, 15, 5.00),  // Sunglasses: received 15 (now 0)
        Triple(11L, 200, 0.60), // Energy Drink: received 200
        Triple(12L, 100, 0.80), // Protein Bar: received 100
        Triple(13L, 120, 1.20), // Notebook: received 120
        Triple(14L, 20, 6.00),  // Desk Organizer: received 20
        Triple(15L, 10, 10.00)  // LED Lamp: received 10
    )

    initialStockData.forEachIndexed { index, (productId, quantity, cost) ->
        movements.add(
            StockMovement(
                id = (index + 1).toLong(),
                productId = productId,
                type = MovementType.IN,
                quantity = quantity,
                previousStock = 0,
                newStock = quantity,
                unitCost = cost,
                reference = "Initial Stock",
                notes = "Opening inventory",
                createdAt = oneWeekAgo + (index * 60000) // Spread out by 1 minute each
            )
        )
    }

    // Add some OUT movements (damage/loss) - happened 3 days ago
    val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000L)

    movements.add(
        StockMovement(
            id = 16,
            productId = 4,  // Screen Protector
            type = MovementType.OUT,
            quantity = -20,
            previousStock = 220,
            newStock = 200,
            unitCost = 0.80,
            reference = "Damaged",
            notes = "Damaged in storage - water leak",
            createdAt = threeDaysAgo
        )
    )

    movements.add(
        StockMovement(
            id = 17,
            productId = 10,  // Sunglasses
            type = MovementType.OUT,
            quantity = -15,
            previousStock = 15,
            newStock = 0,
            unitCost = 5.00,
            reference = "Stolen",
            notes = "Theft incident - police report filed",
            createdAt = threeDaysAgo + 3600000
        )
    )

    // Add an ADJUSTMENT - happened 2 days ago
    val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)

    movements.add(
        StockMovement(
            id = 18,
            productId = 5,  // T-Shirt White
            type = MovementType.ADJUSTMENT,
            quantity = -5,
            previousStock = 80,
            newStock = 75,
            unitCost = 4.00,
            reference = "Inventory Count",
            notes = "Physical count showed 75, adjusted from 80",
            createdAt = twoDaysAgo
        )
    )

    stockMovementDao.insertAll(movements)
}

// ==================== SALES ====================

suspend fun seedSales(
    saleDao: SaleDao,
    saleItemDao: SaleItemDao,
    productDao: ProductDao,
    stockMovementDao: StockMovementDao
) {
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // Helper to create timestamps
    fun daysAgo(days: Int, hoursOffset: Int = 0): Long {
        return now - (days * 24 * 60 * 60 * 1000L) + (hoursOffset * 60 * 60 * 1000L)
    }

    // ==================== Sale 1: Yesterday ====================
    val sale1 = Sale(
        id = 1,
        saleNumber = "SALE-${today.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)}-001",
        totalAmount = 57.96,
        totalCost = 17.30,
        totalProfit = 40.66,
        itemCount = 3,
        notes = null,
        createdAt = daysAgo(1, 10)  // Yesterday 10am
    )
    saleDao.insert(sale1)

    val sale1Items = listOf(
        SaleItem(
            id = 1, saleId = 1, productId = 1, productName = "iPhone 15 Case - Clear",
            quantity = 2, unitPrice = 12.99, unitCost = 3.50, subtotal = 25.98, profit = 18.98
        ),
        SaleItem(
            id = 2, saleId = 1, productId = 2, productName = "USB-C Cable 2m",
            quantity = 1, unitPrice = 8.99, unitCost = 2.00, subtotal = 8.99, profit = 6.99
        ),
        SaleItem(
            id = 3, saleId = 1, productId = 4, productName = "Screen Protector - Universal",
            quantity = 3, unitPrice = 4.99, unitCost = 0.80, subtotal = 14.97, profit = 12.57
        )
    )
    saleItemDao.insertAll(sale1Items)

    // Stock movements for sale 1 (these reduce current stock)
    // Note: These are already reflected in seedProducts currentStock values

    // ==================== Sale 2: Yesterday (later) ====================
    val sale2 = Sale(
        id = 2,
        saleNumber = "SALE-${today.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)}-002",
        totalAmount = 79.97,
        totalCost = 23.00,
        totalProfit = 56.97,
        itemCount = 2,
        notes = "Customer requested gift wrap",
        createdAt = daysAgo(1, 14)  // Yesterday 2pm
    )
    saleDao.insert(sale2)

    val sale2Items = listOf(
        SaleItem(
            id = 4, saleId = 2, productId = 3, productName = "Wireless Earbuds",
            quantity = 2, unitPrice = 34.99, unitCost = 12.00, subtotal = 69.98, profit = 45.98
        ),
        SaleItem(
            id = 5, saleId = 2, productId = 4, productName = "Screen Protector - Universal",
            quantity = 2, unitPrice = 4.99, unitCost = 0.80, subtotal = 9.98, profit = 8.38
        )
    )
    saleItemDao.insertAll(sale2Items)

    // ==================== Sale 3: 2 days ago ====================
    val sale3 = Sale(
        id = 3,
        saleNumber = "SALE-${today.minusDays(2).format(DateTimeFormatter.BASIC_ISO_DATE)}-001",
        totalAmount = 104.95,
        totalCost = 38.00,
        totalProfit = 66.95,
        itemCount = 4,
        notes = null,
        createdAt = daysAgo(2, 11)
    )
    saleDao.insert(sale3)

    val sale3Items = listOf(
        SaleItem(
            id = 6, saleId = 3, productId = 5, productName = "Basic T-Shirt - White",
            quantity = 2, unitPrice = 14.99, unitCost = 4.00, subtotal = 29.98, profit = 21.98
        ),
        SaleItem(
            id = 7, saleId = 3, productId = 6, productName = "Basic T-Shirt - Black",
            quantity = 2, unitPrice = 14.99, unitCost = 4.00, subtotal = 29.98, profit = 21.98
        ),
        SaleItem(
            id = 8, saleId = 3, productId = 7, productName = "Denim Jeans - Classic",
            quantity = 1, unitPrice = 44.99, unitCost = 15.00, subtotal = 44.99, profit = 29.99
        )
    )
    saleItemDao.insertAll(sale3Items)

    // ==================== Sale 4: 3 days ago ====================
    val sale4 = Sale(
        id = 4,
        saleNumber = "SALE-${today.minusDays(3).format(DateTimeFormatter.BASIC_ISO_DATE)}-001",
        totalAmount = 27.96,
        totalCost = 4.80,
        totalProfit = 23.16,
        itemCount = 2,
        notes = null,
        createdAt = daysAgo(3, 9)
    )
    saleDao.insert(sale4)

    val sale4Items = listOf(
        SaleItem(
            id = 9, saleId = 4, productId = 11, productName = "Energy Drink - 250ml",
            quantity = 6, unitPrice = 1.99, unitCost = 0.60, subtotal = 11.94, profit = 8.34
        ),
        SaleItem(
            id = 10, saleId = 4, productId = 12, productName = "Protein Bar - Chocolate",
            quantity = 4, unitPrice = 2.49, unitCost = 0.80, subtotal = 9.96, profit = 6.76
        )
    )
    saleItemDao.insertAll(sale4Items)

    // ==================== Sale 5: 5 days ago ====================
    val sale5 = Sale(
        id = 5,
        saleNumber = "SALE-${today.minusDays(5).format(DateTimeFormatter.BASIC_ISO_DATE)}-001",
        totalAmount = 83.96,
        totalCost = 27.20,
        totalProfit = 56.76,
        itemCount = 3,
        notes = "Bulk order for office",
        createdAt = daysAgo(5, 15)
    )
    saleDao.insert(sale5)

    val sale5Items = listOf(
        SaleItem(
            id = 11, saleId = 5, productId = 13, productName = "Notebook - A5 Lined",
            quantity = 10, unitPrice = 4.99, unitCost = 1.20, subtotal = 49.90, profit = 37.90
        ),
        SaleItem(
            id = 12, saleId = 5, productId = 14, productName = "Desk Organizer",
            quantity = 2, unitPrice = 18.99, unitCost = 6.00, subtotal = 37.98, profit = 25.98
        )
    )
    saleItemDao.insertAll(sale5Items)

    // ==================== Sale 6: Today ====================
    val sale6 = Sale(
        id = 6,
        saleNumber = "SALE-${today.format(DateTimeFormatter.BASIC_ISO_DATE)}-001",
        totalAmount = 47.97,
        totalCost = 13.00,
        totalProfit = 34.97,
        itemCount = 2,
        notes = null,
        createdAt = startOfToday + (2 * 60 * 60 * 1000)  // Today 2am (for testing "today's sales")
    )
    saleDao.insert(sale6)

    val sale6Items = listOf(
        SaleItem(
            id = 13, saleId = 6, productId = 2, productName = "USB-C Cable 2m",
            quantity = 3, unitPrice = 8.99, unitCost = 2.00, subtotal = 26.97, profit = 20.97
        ),
        SaleItem(
            id = 14, saleId = 6, productId = 8, productName = "Canvas Tote Bag",
            quantity = 2, unitPrice = 9.99, unitCost = 3.00, subtotal = 19.98, profit = 13.98
        )
    )
    saleItemDao.insertAll(sale6Items)

    // Add SALE type stock movements for all the sales above
    // Note: In production, these would be created by SaleRepository.completeSale()
    // For seed data, we're pre-calculating the movements

    val saleMovements = listOf(
        // Sale 1 movements
        StockMovement(
            id = 19, productId = 1, type = MovementType.SALE, quantity = -2,
            previousStock = 47, newStock = 45, unitCost = 3.50, reference = sale1.saleNumber,
            createdAt = sale1.createdAt
        ),
        StockMovement(
            id = 20, productId = 2, type = MovementType.SALE, quantity = -1,
            previousStock = 124, newStock = 123, unitCost = 2.00, reference = sale1.saleNumber,
            createdAt = sale1.createdAt
        ),
        StockMovement(
            id = 21, productId = 4, type = MovementType.SALE, quantity = -3,
            previousStock = 203, newStock = 200, unitCost = 0.80, reference = sale1.saleNumber,
            createdAt = sale1.createdAt
        ),

        // Sale 2 movements
        StockMovement(
            id = 22, productId = 3, type = MovementType.SALE, quantity = -2,
            previousStock = 10, newStock = 8, unitCost = 12.00, reference = sale2.saleNumber,
            createdAt = sale2.createdAt
        ),

        // Sale 6 (today) movements
        StockMovement(
            id = 23, productId = 2, type = MovementType.SALE, quantity = -3,
            previousStock = 123, newStock = 120, unitCost = 2.00, reference = sale6.saleNumber,
            createdAt = sale6.createdAt
        ),
        StockMovement(
            id = 24, productId = 8, type = MovementType.SALE, quantity = -2,
            previousStock = 42, newStock = 40, unitCost = 3.00, reference = sale6.saleNumber,
            createdAt = sale6.createdAt
        )

        // Note: Not adding all movements for brevity - in production, stock would be tracked precisely
    )

    stockMovementDao.insertAll(saleMovements)
}