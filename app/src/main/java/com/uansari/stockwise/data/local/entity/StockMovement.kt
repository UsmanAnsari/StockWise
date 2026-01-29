package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.RESTRICT
import androidx.room.Index
import androidx.room.PrimaryKey
import com.uansari.stockwise.domain.model.MovementType

@Entity(
    tableName = "stock_movements",
    foreignKeys = [ForeignKey(
        entity = Product::class,
        parentColumns = ["id"],
        childColumns = ["product_id"],
        onDelete = RESTRICT
    )],
    indices = [
        Index("product_id"),
        Index("created_at"),
        Index("type"),
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo("product_id") val productId: Long,
    val type: MovementType,
    val quantity: Int,
    @ColumnInfo("previous_stock") val previousStock: Int,
    @ColumnInfo("new_stock") val newStock: Int,
    @ColumnInfo("unit_cost") val unitCost: Double? = null,
    val reference: String? = null,
    val notes: String? = null,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
)
