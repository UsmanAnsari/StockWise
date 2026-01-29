package com.uansari.stockwise.domain.repository

import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.relations.DailySalesSummary
import com.uansari.stockwise.data.local.entity.relations.SaleWithItems
import com.uansari.stockwise.data.local.entity.relations.SalesSummary
import com.uansari.stockwise.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface SaleRepository {

    fun getAllSales(): Flow<List<Sale>>
    fun getSalesWithItems(): Flow<List<SaleWithItems>>
    fun getSaleWithItems(saleId: Long): Flow<SaleWithItems?>
    fun getRecentSalesWithItems(limit: Int = 10): Flow<List<SaleWithItems>>
    fun getSalesInRange(startTime: Long, endTime: Long): Flow<List<Sale>>
    fun getTodaySales(startOfDay: Long): Flow<List<Sale>>

    fun getDailySummary(startOfDay: Long): Flow<DailySalesSummary>
    fun getSalesSummary(startTime: Long, endTime: Long): Flow<SalesSummary>

    suspend fun completeSale(
        cartItems: List<CartItem>, notes: String? = null
    ): Result<Sale>

    suspend fun deleteSale(saleId: Long): Result<Unit>

    suspend fun generateSaleNumber(): String
}