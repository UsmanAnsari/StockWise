package com.uansari.stockwise.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.uansari.stockwise.data.local.dao.ProductDao
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllActiveProducts(): Flow<List<Product>> {
        return productDao.getAllActiveProducts()
    }

    override fun getProductById(productId: Long): Flow<Product?> {
        return productDao.getProductById(productId)
    }

    override fun getProductsWithDetails(): Flow<List<ProductWithDetails>> {
        return productDao.getProductsWithDetails()
    }

    override fun getProductWithDetails(productId: Long): Flow<ProductWithDetails?> {
        return productDao.getProductWithDetails(productId)
    }

    override fun getProductsByCategory(categoryId: Long): Flow<List<ProductWithDetails>> {
        return productDao.getProductsByCategoryWithDetails(categoryId)
    }

    override fun getProductsBySupplier(supplierId: Long): Flow<List<ProductWithDetails>> {
        return productDao.getProductsBySupplierWithDetails(supplierId)
    }

    override fun searchProducts(query: String): Flow<List<ProductWithDetails>> {
        return productDao.searchProducts(query)
    }

    override fun getLowStockProducts(): Flow<List<ProductWithDetails>> {
        return productDao.getLowStockProducts()
    }

    override fun getOutOfStockProducts(): Flow<List<ProductWithDetails>> {
        return productDao.getOutOfStockProducts()
    }


    override fun getInventoryStats(): Flow<InventoryStats> {
        return productDao.getInventoryStats()
    }

    override fun getTotalInventoryValue(): Flow<Double> {
        return productDao.getTotalInventoryValue()
    }


    override suspend fun insertProduct(product: Product): Result<Long> {
        return try {
            val id = productDao.insert(product)
            Result.success(id)
        } catch (e: SQLiteConstraintException) {
            // Could be duplicate SKU or invalid FK
            Result.failure(Exception("Product SKU already exists or invalid category/supplier"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            // Check SKU uniqueness (excluding current product)
            val existing = productDao.getProductBySku(product.sku)
            if (existing != null && existing.id != product.id) {
                return Result.failure(Exception("SKU already exists"))
            }

            val updatedProduct = product.copy(updatedAt = System.currentTimeMillis())
            productDao.update(updatedProduct)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun softDeleteProduct(productId: Long): Result<Unit> {
        return try {
            productDao.softDelete(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreProduct(productId: Long): Result<Unit> {
        return try {
            productDao.restore(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isSkuExists(sku: String, excludeId: Long?): Boolean {
        val existing = productDao.getProductBySku(sku)
        return existing != null && existing.id != excludeId
    }

    override suspend fun getProductBySku(sku: String): Product? {
        return productDao.getProductBySku(sku)
    }

    override suspend fun getProductCountByCategory(categoryId: Long): Int {
        return productDao.getProductCountByCategory(categoryId)
    }

    override suspend fun getProductCountBySupplier(supplierId: Long): Int {
        return productDao.getProductCountBySupplier(supplierId)
    }

    override suspend fun getProductByIdOneShot(productId: Long): Product? {
        return productDao.getProductByIdOneShot(productId)
    }
}