package com.uansari.stockwise.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.uansari.stockwise.data.local.dao.SupplierDao
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import com.uansari.stockwise.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao
) : SupplierRepository {
    override fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()

    override fun getSupplierById(supplierId: Long): Flow<Supplier?> =
        supplierDao.getSupplierById(supplierId)

    override suspend fun getSupplierByIdOneShot(supplierId: Long): Supplier? {
        return supplierDao.getSupplierByIdOneShot(supplierId)
    }

    override fun getSuppliersWithProductCount(): Flow<List<SupplierWithProductCount>> =
        supplierDao.getSuppliersWithProductCount()

    override fun searchSuppliers(query: String): Flow<List<Supplier>> =
        supplierDao.searchSuppliers(query)

    override suspend fun getSupplierByName(name: String) = supplierDao.getSupplierByName(name)

    override suspend fun insertSupplier(supplier: Supplier): Result<Long> {
        return try {
            val id = supplierDao.insert(supplier)
            Result.success(id)
        } catch (e: SQLiteConstraintException) {
            Result.failure(Exception("Supplier name already exists"))
        } catch (e: SQLiteConstraintException) {
            Result.failure(e)
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        return try {
            val existing = supplierDao.getSupplierByName(supplier.name)
            if (existing != null && existing.id != supplier.id) {
                return Result.failure(Exception("Supplier name already exists"))
            }
            supplierDao.update(supplier)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSupplier(supplierId: Long): Result<Unit> {
        return try {
            // Products with this supplier will have supplier_id set to NULL (SET_NULL)
            // We might want to warn the user about this
            supplierDao.delete(supplierId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isSupplierNameExists(
        name: String, excludeId: Long?
    ): Boolean {
        val existing = supplierDao.getSupplierByName(name)
        return existing != null && existing.id != excludeId
    }

    override suspend fun getProductCountForSupplier(supplierId: Long): Int {
        return supplierDao.getProductCountForSupplier(supplierId)
    }
}