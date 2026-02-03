package com.uansari.stockwise.data.local.entity.relations

import androidx.room.ColumnInfo

/**
 * Category with count of products in it.
 * */
data class CategoryWithProductCount(
    val id: Long,
    val name: String,
    val description: String?,
    val color: String,
    @ColumnInfo("updated_at") val updatedAt: Long?,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("product_count") val productCount: Int
) {
    val canDelete: Boolean
        get() = productCount == 0
}