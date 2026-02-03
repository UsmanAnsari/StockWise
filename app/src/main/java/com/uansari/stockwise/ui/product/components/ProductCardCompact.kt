package com.uansari.stockwise.ui.product.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.ui.components.StockIndicator
import com.uansari.stockwise.util.CurrencyFormatter.formatAsCurrency

@Composable
fun ProductCardCompact(
    productWithDetails: ProductWithDetails, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val product = productWithDetails.product
    val category = productWithDetails.category

    Card(
        modifier = modifier.fillMaxWidth(), onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category color indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            category.color.let {
                                try {
                                    Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.2f)
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            }), contentAlignment = Alignment.Center) {
                    Text(
                        text = product.name.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = category.color.let {
                            try {
                                Color(android.graphics.Color.parseColor(it))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        })
                }

                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.sku,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.sellPrice.formatAsCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                StockIndicator(
                    currentStock = product.currentStock,
                    lowStockThreshold = product.lowStockThreshold,
                    unit = product.unit
                )
            }
        }
    }
}