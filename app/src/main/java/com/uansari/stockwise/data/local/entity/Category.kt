package com.uansari.stockwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories", indices = [Index("name", unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String = "#2196F3",
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
)
