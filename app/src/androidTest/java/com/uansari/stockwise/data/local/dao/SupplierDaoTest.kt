package com.uansari.stockwise.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.data.local.entity.Supplier
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SupplierDaoTest : BaseDaoTest() {
    private lateinit var supplierDao: SupplierDao

    @Before
    override fun setUp() {
        super.setUp()
        supplierDao = database.supplierDao()
    }

    @Test
    fun insertSupplier_returnId() = runTest {
        val id = supplierDao.insert(
            Supplier(
                name = "APPLE"
            )
        )
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun insertSupplier_withSameName_throwsException() = runTest {
        supplierDao.insert(
            Supplier(
                name = "APPLE"
            )
        )
        try {
            supplierDao.insert(
                Supplier(
                    name = "APPLE"
                )
            )
            assertThat(false)
        } catch (e: SQLiteConstraintException) {

        }
    }
}