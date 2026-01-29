package com.uansari.stockwise.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.SaleItem
/**
 * Sale Detail
 * */
data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "sale_id"
    ) val saleItem: List<SaleItem>
)