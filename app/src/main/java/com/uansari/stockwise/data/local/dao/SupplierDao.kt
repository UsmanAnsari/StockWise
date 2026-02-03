package com.uansari.stockwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    // ==================== CREATE ====================

    @Insert
    suspend fun insert(supplier: Supplier): Long

    @Insert
    suspend fun insertAll(suppliers: List<Supplier>): List<Long>

    // ==================== READ ====================

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id =:supplierId")
    fun getSupplierById(supplierId: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers WHERE id =:supplierId")
    suspend fun getSupplierByIdOneShot(supplierId: Long): Supplier?

    @Query("SELECT * FROM suppliers WHERE name =:name LIMIT 1")
    suspend fun getSupplierByName(name: String): Supplier?

    /**
     * Get all suppliers with count of products they supply.
     */
    @Query(
        """
        SELECT 
            s.id,
            s.name,
            s.contact_person,
            s.phone,
            s.email,
            s.address,
            s.notes,
            s.created_at,
            COUNT(p.id) as product_count
        FROM suppliers s
        LEFT JOIN products p ON s.id = p.supplier_id AND p.is_active = 1
        GROUP BY s.id
        ORDER BY s.name ASC
    """
    )
    fun getSuppliersWithProductCount(): Flow<List<SupplierWithProductCount>>


    /**
     * Search suppliers by name or contact person.
     */
    @Query(
        """
        SELECT * FROM suppliers 
        WHERE name LIKE '%' || :query || '%' 
           OR contact_person LIKE '%' || :query || '%'
        ORDER BY name ASC
    """
    )
    fun searchSuppliers(query: String): Flow<List<Supplier>>

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getSupplierCount(): Int

    // ==================== UPDATE ====================

    @Update
    suspend fun update(supplier: Supplier)

    // ==================== DELETE ====================
    /**
     * Delete a supplier.
     * Products referencing this supplier will have supplier_id set to NULL (FK SET_NULL).
     */
    @Query("DELETE FROM suppliers WHERE id = :supplierId")
    suspend fun delete(supplierId: Long)

    /**
     * Get count of products for the respective supplier (for UI warning before delete).
     */
    @Query("SELECT COUNT(*) FROM products WHERE supplier_id = :supplierId AND is_active = 1")
    suspend fun getProductCountForSupplier(supplierId: Long): Int
}
