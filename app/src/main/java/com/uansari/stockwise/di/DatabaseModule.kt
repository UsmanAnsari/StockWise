package com.uansari.stockwise.di

import android.content.Context
import androidx.room.Room
import com.uansari.stockwise.data.local.dao.CategoryDao
import com.uansari.stockwise.data.local.dao.ProductDao
import com.uansari.stockwise.data.local.dao.SaleDao
import com.uansari.stockwise.data.local.dao.SaleItemDao
import com.uansari.stockwise.data.local.dao.StockMovementDao
import com.uansari.stockwise.data.local.dao.SupplierDao
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: SeedDatabaseCallback
    ): StockWiseDatabase {
        return Room.databaseBuilder(
            context,
            StockWiseDatabase::class.java,
            "stockwise_database"
        )
            .addCallback(callback)  // For seeding initial data
            .fallbackToDestructiveMigration()  // OK for development
            .build()
    }

    // ==================== DAO PROVIDERS ====================

    @Provides
    @Singleton
    fun provideCategoryDao(database: StockWiseDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideSupplierDao(database: StockWiseDatabase): SupplierDao {
        return database.supplierDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: StockWiseDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideStockMovementDao(database: StockWiseDatabase): StockMovementDao {
        return database.stockMovementDao()
    }

    @Provides
    @Singleton
    fun provideSaleDao(database: StockWiseDatabase): SaleDao {
        return database.saleDao()
    }

    @Provides
    @Singleton
    fun provideSaleItemDao(database: StockWiseDatabase): SaleItemDao {
        return database.saleItemDao()
    }
}