package com.uansari.stockwise.domain.repository

import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // Read operations
    fun getAllActiveProducts(): Flow<List<Product>>
    fun getProductById(productId: Long): Flow<Product?>
    fun getProductsWithDetails(): Flow<List<ProductWithDetails>>
    fun getProductWithDetails(productId: Long): Flow<ProductWithDetails?>
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductWithDetails>>
    fun getProductsBySupplier(supplierId: Long): Flow<List<ProductWithDetails>>
    fun searchProducts(query: String): Flow<List<ProductWithDetails>>
    fun getLowStockProducts(): Flow<List<ProductWithDetails>>
    fun getOutOfStockProducts(): Flow<List<ProductWithDetails>>
    suspend fun getProductCountByCategory(categoryId: Long): Int
    suspend fun getProductCountBySupplier(supplierId: Long): Int

    // Stats
    fun getInventoryStats(): Flow<InventoryStats>
    fun getTotalInventoryValue(): Flow<Double>

    // Write operations
    suspend fun insertProduct(product: Product): Result<Long>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun softDeleteProduct(productId: Long): Result<Unit>
    suspend fun restoreProduct(productId: Long): Result<Unit>

    // Validation
    suspend fun isSkuExists(sku: String, excludeId: Long? = null): Boolean
    suspend fun getProductBySku(sku: String): Product?
    suspend fun getProductByIdOneShot(productId: Long): Product?
}