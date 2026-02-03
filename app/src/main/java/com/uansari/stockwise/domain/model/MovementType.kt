package com.uansari.stockwise.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the type of stock movement.
 *
 * IN         - Stock received (purchase, return from customer)
 *
 * OUT        - Stock removed (damage, loss, theft, expired)
 *
 * ADJUSTMENT - Manual correction (inventory count mismatch)
 *
 * SALE       - Stock sold to customer (auto-created during sale)
 */
enum class MovementType {
    IN, OUT, ADJUSTMENT, SALE;

    fun toDisplayStyle(): MovementDisplayStyle {
        return when (this) {
            IN -> MovementDisplayStyle(
                icon = Icons.Default.AddCircle, color = Color(0xFF4CAF50), label = "Stock In"
            )

            OUT -> MovementDisplayStyle(
                icon = Icons.Default.RemoveCircle, color = Color(0xFFFF9800), label = "Stock Out"
            )

            ADJUSTMENT -> MovementDisplayStyle(
                icon = Icons.Default.Tune, color = Color(0xFF2196F3), label = "Adjustment"
            )

            MovementType.SALE -> MovementDisplayStyle(
                icon = Icons.Default.ShoppingCart, color = Color(0xFF9C27B0), label = "Sale"
            )
        }
    }

}

data class MovementDisplayStyle(
    val icon: ImageVector, val color: Color, val label: String
)


