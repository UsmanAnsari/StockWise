package com.uansari.stockwise.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.data.local.entity.Supplier

/**
 * Product with its related Category and Supplier.
 */
data class ProductWithDetails(
    @Embedded val product: Product,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: Category,
    @Relation(parentColumn = "supplier_id", entityColumn = "id")
    val supplier: Supplier?
)
