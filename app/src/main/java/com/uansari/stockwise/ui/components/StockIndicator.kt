package com.uansari.stockwise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.domain.model.ProductUnitType

@Composable
fun StockIndicator(
    currentStock: Int, lowStockThreshold: Int, unit: ProductUnitType, modifier: Modifier = Modifier
) {
    val (color, label) = when {
        currentStock == 0 -> MaterialTheme.colorScheme.error to "Out of stock"
        currentStock <= lowStockThreshold -> Color(0xFFFF9800) to "Low stock"
        else -> MaterialTheme.colorScheme.primary to "In stock"
        //Color(0xFF4CAF50)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$currentStock ${unit.name}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}