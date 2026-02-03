package com.uansari.stockwise.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionsRow(
    onNewSale: () -> Unit,
    onAddProduct: () -> Unit,
    onViewProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "New Sale",
            onClick = onNewSale,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )

        QuickActionButton(
            icon = Icons.Default.AddBox,
            label = "Add Stock",
            onClick = onAddProduct,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            icon = Icons.Default.Inventory,
            label = "Products",
            onClick = onViewProducts,
            modifier = Modifier.weight(1f)
        )
    }
}