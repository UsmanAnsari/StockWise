package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "suppliers", indices = [Index("name", unique = true)]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo("contact_person") val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    @ColumnInfo("updated_at") val updatedAt: Long? = System.currentTimeMillis(),
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
)
