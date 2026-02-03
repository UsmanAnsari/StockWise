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
    @ColumnInfo("updated_at") val updatedAt: Long?,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("product_count") val productCount: Int
) {
    val canDelete: Boolean
        get() = productCount == 0

    val hasContactInfo: Boolean
        get() = !contactPerson.isNullOrBlank() || !phone.isNullOrBlank() || !email.isNullOrBlank()
}