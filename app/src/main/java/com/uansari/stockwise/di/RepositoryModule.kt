package com.uansari.stockwise.di

import com.uansari.stockwise.data.repository.CategoryRepositoryImpl
import com.uansari.stockwise.data.repository.ProductRepositoryImpl
import com.uansari.stockwise.data.repository.SaleRepositoryImpl
import com.uansari.stockwise.data.repository.StockRepositoryImpl
import com.uansari.stockwise.data.repository.SupplierRepositoryImpl
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.repository.SaleRepository
import com.uansari.stockwise.domain.repository.StockRepository
import com.uansari.stockwise.domain.repository.SupplierRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(
        impl: SupplierRepositoryImpl
    ): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: StockRepositoryImpl
    ): StockRepository

    @Binds
    @Singleton
    abstract fun bindSaleRepository(
        impl: SaleRepositoryImpl
    ): SaleRepository
}