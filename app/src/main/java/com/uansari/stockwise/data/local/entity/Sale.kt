package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index("sale_number", unique = true),
        Index("created_at", unique = true),
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    @ColumnInfo("sale_number") val saleNumber: String,
    @ColumnInfo("total_amount") val totalAmount: Double,
    @ColumnInfo("total_cost") val totalCost: Double,
    @ColumnInfo("total_Profit") val totalProfit: Double,
    @ColumnInfo("item_count") val itemCount: Int,
    val notes: String? = null,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
)
