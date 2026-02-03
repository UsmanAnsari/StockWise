package com.uansari.stockwise.domain.repository

import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {

    // Read operations
    fun getAllSuppliers(): Flow<List<Supplier>>
    fun getSupplierById(supplierId: Long): Flow<Supplier?>
    suspend fun getSupplierByIdOneShot(supplierId: Long): Supplier?
    fun getSuppliersWithProductCount(): Flow<List<SupplierWithProductCount>>
    fun searchSuppliers(query: String): Flow<List<Supplier>>
    suspend fun getSupplierByName(name: String): Supplier?

    // Write operations
    suspend fun insertSupplier(supplier: Supplier): Result<Long>
    suspend fun updateSupplier(supplier: Supplier): Result<Unit>
    suspend fun deleteSupplier(supplierId: Long): Result<Unit>

    // Validation
    suspend fun isSupplierNameExists(name: String, excludeId: Long? = null): Boolean
    suspend fun getProductCountForSupplier(supplierId: Long): Int
}