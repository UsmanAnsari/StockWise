package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.RESTRICT
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.Index
import androidx.room.PrimaryKey
import com.uansari.stockwise.domain.model.ProductUnitType

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = RESTRICT
    ), ForeignKey(
        entity = Supplier::class,
        parentColumns = ["id"],
        childColumns = ["supplier_id"],
        onDelete = SET_NULL
    )], indices = [
        Index("category_id"),
        Index("supplier_id"),
        Index("name", unique = true),
        Index("sku", unique = true),
        Index("is_active"),
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val description: String? = null,
    @ColumnInfo("category_id") val categoryId: Long,
    @ColumnInfo("supplier_id") val supplierId: Long? = null,
    @ColumnInfo("buy_price") val buyPrice: Double,
    @ColumnInfo("sell_price") val sellPrice: Double,
    @ColumnInfo("current_stock") val currentStock: Int = 0,
    @ColumnInfo("low_stock_threshold") val lowStockThreshold: Int = 10,
    val unit: ProductUnitType = ProductUnitType.PCS,
    @ColumnInfo("is_active") val isActive: Boolean = true,
    @ColumnInfo("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
) {
    val profitMargin: Double
        get() = if (sellPrice > 0) ((sellPrice - buyPrice) / sellPrice) * 100 else 0.0

    val profitPerUnit: Double
        get() = sellPrice - buyPrice

    val isLowStock: Boolean
        get() = currentStock <= lowStockThreshold

    val stockValue: Double
        get() = currentStock * buyPrice
}
