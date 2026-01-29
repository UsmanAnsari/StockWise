package com.uansari.stockwise.data.local.database

import androidx.room.TypeConverter
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.model.ProductUnitType

class Converters {
    @TypeConverter
    fun fromMovementType(type: MovementType): String = type.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromProductUnitType(type: ProductUnitType): String = type.name

    @TypeConverter
    fun toProductUnitType(value: String): ProductUnitType = ProductUnitType.valueOf(value)
}