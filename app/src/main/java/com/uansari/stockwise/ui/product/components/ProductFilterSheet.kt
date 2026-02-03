package com.uansari.stockwise.ui.product.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.util.ProductSortOption
import com.uansari.stockwise.util.StockFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterSheet(
    categories: List<Category>,
    selectedCategoryId: Long?,
    stockFilter: StockFilter,
    sortOption: ProductSortOption,
    onCategoryChange: (Long?) -> Unit,
    onStockFilterChange: (StockFilter) -> Unit,
    onSortOptionChange: (ProductSortOption) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
                Button(
                    onClick = onDismiss,
                ) {
                    Text("Apply Filters")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Filter
            FilterSectionHeader(title = "Category")

            Spacer(modifier = Modifier.height(8.dp))

            CategoryFilterChips(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategoryChange = onCategoryChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stock Filter
            FilterSectionHeader(title = "Stock Status")

            Spacer(modifier = Modifier.height(8.dp))

            StockFilterChips(
                stockFilter = stockFilter, onStockFilterChange = onStockFilterChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sort Options
            FilterSectionHeader(title = "Sort By")

            Spacer(modifier = Modifier.height(8.dp))

            SortOptionsList(
                sortOption = sortOption, onSortOptionChange = onSortOptionChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apply Button
            Button(
                onClick = onDismiss, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(
    title: String, modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategoryChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategoryChange(null) },
                label = { Text("All") },
                leadingIcon = if (selectedCategoryId == null) {
                    {
                        Icon(
                            Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)
                        )
                    }
                } else null)
        }

        items(categories) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategoryChange(category.id) },
                label = { Text(category.name) },
                leadingIcon = if (selectedCategoryId == category.id) {
                    {
                        Icon(
                            Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)
                        )
                    }
                } else null)
        }
    }
}

@Composable
private fun StockFilterChips(
    stockFilter: StockFilter,
    onStockFilterChange: (StockFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val stockFilters = listOf(
        StockFilter.ALL to "All",
        StockFilter.IN_STOCK to "In Stock",
        StockFilter.LOW_STOCK to "Low Stock",
        StockFilter.OUT_OF_STOCK to "Out of Stock"
    )

    LazyRow(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stockFilters) { (filter, label) ->
            FilterChip(
                selected = stockFilter == filter,
                onClick = { onStockFilterChange(filter) },
                label = { Text(label) },
                leadingIcon = if (stockFilter == filter) {
                    {
                        Icon(
                            Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)
                        )
                    }
                } else null)
        }
    }
}

@Composable
private fun SortOptionsList(
    sortOption: ProductSortOption,
    onSortOptionChange: (ProductSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortOptions = listOf(
        ProductSortOption.NAME_ASC to "Name (A-Z)",
        ProductSortOption.NAME_DESC to "Name (Z-A)",
        ProductSortOption.STOCK_LOW_HIGH to "Stock (Low to High)",
        ProductSortOption.STOCK_HIGH_LOW to "Stock (High to Low)",
        ProductSortOption.PRICE_LOW_HIGH to "Price (Low to High)",
        ProductSortOption.PRICE_HIGH_LOW to "Price (High to Low)",
        ProductSortOption.RECENTLY_UPDATED to "Recently Updated"
    )

    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        sortOptions.forEach { (option, label) ->
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = sortOption == option, onClick = { onSortOptionChange(option) })

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
