package com.uansari.stockwise.domain.model

data class StockMovementsSummary(
    val totalMovements: Int = 0,
    val totalIn: Int = 0,
    val totalOut: Int = 0,
    val totalAdjustments: Int = 0,
    val totalSales: Int = 0,
    val netChange: Int = 0
)