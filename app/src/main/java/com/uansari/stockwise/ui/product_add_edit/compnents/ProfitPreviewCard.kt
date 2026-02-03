package com.uansari.stockwise.ui.product_add_edit.compnents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.util.CurrencyFormatter.formatAsCurrency

@Composable
fun ProfitPreviewCard(
    buyPrice: Double?,
    sellPrice: Double?,
    modifier: Modifier = Modifier
) {
    val profit = if (buyPrice != null && sellPrice != null) {
        sellPrice - buyPrice
    } else null
    
    val margin = if (sellPrice != null && sellPrice > 0 && profit != null) {
        (profit / sellPrice) * 100
    } else null
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profit per unit
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Profit/Unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = profit?.formatAsCurrency() ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        profit == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        profit > 0 -> Color(0xFF4CAF50)
                        profit < 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Profit margin
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Margin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = margin?.let { "${it.toInt()}%" } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        margin == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        margin >= 50 -> Color(0xFF4CAF50)
                        margin >= 25 -> Color(0xFFFF9800)
                        margin > 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}