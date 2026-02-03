package com.uansari.stockwise.ui.product_detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.data.local.entity.StockMovement
import kotlin.collections.forEachIndexed
import kotlin.collections.lastIndex

@Composable
fun StockMovementList(
    movements: List<StockMovement>,
    modifier: Modifier = Modifier,
    showDividers: Boolean = true
) {
    Column(modifier = modifier) {
        movements.forEachIndexed { index, movement ->
            StockMovementItem(
                movement = movement,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            if (showDividers && index < movements.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}