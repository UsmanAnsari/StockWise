package com.uansari.stockwise.data.local.entity.relations

/**
 * Inventory statistics for dashboard.
 */
data class InventoryStats(
    val totalValue: Double, val totalProducts: Int, val lowStockCount: Int
)

/**
 * Daily sales summary for dashboard.
 */
data class DailySalesSummary(
    val totalSales: Double,
    val totalProfit: Double,
    val saleCount: Int
)

/**
 * Sales summary for a date range (weekly/monthly).
 */
data class SalesSummary(
    val totalSales: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val saleCount: Int,
    val itemsSold: Int
)
