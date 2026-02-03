package com.uansari.stockwise.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfitBadge(
    profitMargin: Double,
    modifier: Modifier = Modifier
) {
    val color = when {
        profitMargin >= 50 -> Color(0xFF4CAF50)  // Green
        profitMargin >= 25 -> Color(0xFFFF9800)  // Orange
        else -> MaterialTheme.colorScheme.error   // Red
    }
    
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = "${profitMargin.toInt()}%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}