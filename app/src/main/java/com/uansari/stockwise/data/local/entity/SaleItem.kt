package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.RESTRICT
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["sale_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = RESTRICT
        ),
    ],
    indices = [Index("sale_id"), Index("product_id")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo("sale_id") val saleId: Long,
    @ColumnInfo("product_id") val productId: Long,
    @ColumnInfo("product_name") val productName: String,
    val quantity: Int,
    @ColumnInfo("unit_price") val unitPrice: Double,
    @ColumnInfo("unit_cost") val unitCost: Double,
    val subtotal: Double,
    val profit: Double
)
