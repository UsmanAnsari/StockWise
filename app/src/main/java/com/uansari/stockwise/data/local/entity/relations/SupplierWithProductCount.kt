package com.uansari.stockwise.data.local.entity.relations

import androidx.room.ColumnInfo

data class SupplierWithProductCount(
    val id: Long,
    val name: String,
    @ColumnInfo("contact_person") val contactPerson: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val notes: String?,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("product_count") val productCount: Int
)