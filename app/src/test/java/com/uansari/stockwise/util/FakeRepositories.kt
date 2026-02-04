package com.uansari.stockwise.util

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeProductRepository : ProductRepository {

    private val products = MutableStateFlow<List<Product>>(emptyList())
    private val productsWithDetails = MutableStateFlow<List<ProductWithDetails>>(emptyList())

    // Control flags for testing error scenarios
    var shouldReturnError = false
    var errorMessage = "Test error"

    fun setProducts(productList: List<Product>) {
        products.value = productList
    }

    fun setProductsWithDetails(list: List<ProductWithDetails>) {
        productsWithDetails.value = list
    }

    override fun getProductsWithDetails(): Flow<List<ProductWithDetails>> {
        return productsWithDetails
    }

    override fun getProductWithDetails(productId: Long): Flow<ProductWithDetails?> {
        return productsWithDetails.map { list ->
            list.find { it.product.id == productId }
        }
    }

    override suspend fun getProductByIdOneShot(productId: Long): Product? {
        return products.value.find { it.id == productId }
    }

    override suspend fun getProductBySku(sku: String): Product? {
        return products.value.find { it.sku.equals(sku, ignoreCase = true) }
    }

    override suspend fun insertProduct(product: Product): Result<Long> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            val newId = (products.value.maxOfOrNull { it.id } ?: 0) + 1
            val newProduct = product.copy(id = newId)
            products.value = products.value + newProduct
            Result.success(newId)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            products.value = products.value.map {
                if (it.id == product.id) product else it
            }
            Result.success(Unit)
        }
    }

    override suspend fun softDeleteProduct(productId: Long): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            products.value = products.value.map {
                if (it.id == productId) it.copy(isActive = false) else it
            }
            Result.success(Unit)
        }
    }

    override suspend fun getProductCountByCategory(categoryId: Long): Int {
        return products.value.count { it.categoryId == categoryId && it.isActive }
    }

    override suspend fun getProductCountBySupplier(supplierId: Long): Int {
        return products.value.count { it.supplierId == supplierId && it.isActive }
    }

    override fun getLowStockProducts(): Flow<List<ProductWithDetails>> {
        return productsWithDetails.map { list ->
            list.filter { it.product.currentStock <= it.product.lowStockThreshold }
        }
    }

    override fun getAllActiveProducts(): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override fun getProductById(productId: Long): Flow<Product?> {
        TODO("Not yet implemented")
    }

    override fun getProductsByCategory(categoryId: Long): Flow<List<ProductWithDetails>> {
        TODO("Not yet implemented")
    }

    override fun getProductsBySupplier(supplierId: Long): Flow<List<ProductWithDetails>> {
        TODO("Not yet implemented")
    }

    override fun searchProducts(query: String): Flow<List<ProductWithDetails>> {
        TODO("Not yet implemented")
    }

    override fun getOutOfStockProducts(): Flow<List<ProductWithDetails>> {
        TODO("Not yet implemented")
    }

    override fun getInventoryStats(): Flow<InventoryStats> {
        TODO("Not yet implemented")
    }

    override fun getTotalInventoryValue(): Flow<Double> {
        TODO("Not yet implemented")
    }

    override suspend fun restoreProduct(productId: Long): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun isSkuExists(sku: String, excludeId: Long?): Boolean {
        TODO("Not yet implemented")
    }
}

/**
 * Fake CategoryRepository for testing.
 */
class FakeCategoryRepository : CategoryRepository {

    private val categories = MutableStateFlow<List<Category>>(emptyList())
    private val categoriesWithCount = MutableStateFlow<List<CategoryWithProductCount>>(emptyList())

    var shouldReturnError = false
    var errorMessage = "Test error"

    fun setCategories(list: List<Category>) {
        categories.value = list
    }

    fun setCategoriesWithCount(list: List<CategoryWithProductCount>) {
        categoriesWithCount.value = list
    }

    override fun getAllCategories(): Flow<List<Category>> = categories

    override fun getCategoriesWithProductCount(): Flow<List<CategoryWithProductCount>> = categoriesWithCount

    override suspend fun getCategoryByIdOneShot(categoryId: Long): Category? {
        return categories.value.find { it.id == categoryId }
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categories.value.find { it.name.equals(name, ignoreCase = true) }
    }

    override suspend fun insertCategory(category: Category): Result<Long> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            val newId = (categories.value.maxOfOrNull { it.id } ?: 0) + 1
            categories.value = categories.value + category.copy(id = newId)
            Result.success(newId)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            categories.value = categories.value.map {
                if (it.id == category.id) category else it
            }
            Result.success(Unit)
        }
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            categories.value = categories.value.filter { it.id != categoryId }
            Result.success(Unit)
        }
    }

    override fun getCategoryById(categoryId: Long): Flow<Category?> {
        TODO("Not yet implemented")
    }

    override suspend fun isCategoryNameExists(
        name: String,
        excludeId: Long?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canDeleteCategory(categoryId: Long): Boolean {
        TODO("Not yet implemented")
    }
}

/**
 * Fake StockRepository for testing.
 */
/*
class FakeStockRepository : StockRepository {

    private val movements = MutableStateFlow<List<StockMovement>>(emptyList())
    private val productStocks = mutableMapOf<Long, Int>()

    var shouldReturnError = false
    var errorMessage = "Test error"

    fun setMovements(list: List<StockMovement>) {
        movements.value = list
    }

    fun setProductStock(productId: Long, stock: Int) {
        productStocks[productId] = stock
    }

    override fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>> {
        return movements.map { list ->
            list.filter { it.productId == productId }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun addStock(
        productId: Long,
        quantity: Int,
        notes: String?
    ): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            val currentStock = productStocks[productId] ?: 0
            productStocks[productId] = currentStock + quantity

            val movement = StockMovement(
                id = movements.value.size + 1L,
                productId = productId,
                type = MovementType.IN,
                quantity = quantity,
                previousStock = currentStock,
                newStock = currentStock + quantity,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            movements.value = movements.value + movement
            Result.success(Unit)
        }
    }

    override suspend fun removeStock(
        productId: Long,
        quantity: Int,
        notes: String?
    ): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            val currentStock = productStocks[productId] ?: 0
            if (quantity > currentStock) {
                return Result.failure(Exception("Insufficient stock"))
            }

            productStocks[productId] = currentStock - quantity

            val movement = StockMovement(
                id = movements.value.size + 1L,
                productId = productId,
                type = MovementType.OUT,
                quantity = -quantity,
                previousStock = currentStock,
                newStock = currentStock - quantity,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            movements.value = movements.value + movement
            Result.success(Unit)
        }
    }

    override suspend fun adjustStock(
        productId: Long,
        newStockLevel: Int,
        notes: String?
    ): Result<Unit> {
        TODO()
        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            val currentStock = productStocks[productId] ?: 0
            productStocks[productId] = newStockLevel

            val movement = StockMovement(
                id = movements.value.size + 1L,
                productId = productId,
                type = MovementType.ADJUSTMENT,
                quantity = newStockLevel - currentStock,
                previousStock = currentStock,
                newStock = newStockLevel,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            movements.value = movements.value + movement
            Result.success(Unit)
        }
    }

}*/
