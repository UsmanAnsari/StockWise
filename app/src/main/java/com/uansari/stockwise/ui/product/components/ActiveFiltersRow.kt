package com.uansari.stockwise.ui.product.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.util.StockFilter

@Composable
fun ActiveFiltersRow(
    selectedCategory: Category?,
    stockFilter: StockFilter,
    onClearCategory: () -> Unit,
    onClearStockFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Category chip
        if (selectedCategory != null) {
            item {
                InputChip(
                    selected = true,
                    onClick = onClearCategory,
                    label = { Text(selectedCategory.name) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
        
        // Stock filter chip
        if (stockFilter != StockFilter.ALL) {
            item {
                val label = when (stockFilter) {
                    StockFilter.IN_STOCK -> "In Stock"
                    StockFilter.LOW_STOCK -> "Low Stock"
                    StockFilter.OUT_OF_STOCK -> "Out of Stock"
                    else -> ""
                }
                
                InputChip(
                    selected = true,
                    onClick = onClearStockFilter,
                    label = { Text(label) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}