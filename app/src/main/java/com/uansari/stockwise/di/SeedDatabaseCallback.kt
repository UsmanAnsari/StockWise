package com.uansari.stockwise.di

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class SeedDatabaseCallback @Inject constructor(
    private val databaseProvider: Provider<StockWiseDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Seed data on background thread
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabase(databaseProvider.get())
        }
    }
    
    private suspend fun seedDatabase(database: StockWiseDatabase) {
        val categoryDao = database.categoryDao()
        val supplierDao = database.supplierDao()
        val productDao = database.productDao()
        val stockMovementDao = database.stockMovementDao()
        val saleDao = database.saleDao()
        val saleItemDao = database.saleItemDao()
        
        // Seed in order of dependencies
        seedCategories(categoryDao)
        seedSuppliers(supplierDao)
        seedProducts(productDao)
        seedStockMovements(stockMovementDao, productDao)
        seedSales(saleDao, saleItemDao, productDao, stockMovementDao)
    }
}