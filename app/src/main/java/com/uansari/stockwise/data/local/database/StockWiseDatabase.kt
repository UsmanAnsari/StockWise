package com.uansari.stockwise.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

@Database(
    entities = [
        Category::class,
        Supplier::class,
        Product::class,
        StockMovement::class,
        Sale::class,
        SaleItem::class,
    ], version = 1, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StockWiseDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
}