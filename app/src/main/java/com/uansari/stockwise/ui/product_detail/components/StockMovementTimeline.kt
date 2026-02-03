package com.uansari.stockwise.ui.product_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.data.local.entity.StockMovement
import kotlin.collections.forEachIndexed
import kotlin.collections.lastIndex

@Composable
fun StockMovementTimeline(
    movements: List<StockMovement>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        movements.forEachIndexed { index, movement ->
            StockMovementTimelineItem(
                movement = movement,
                isFirst = index == 0,
                isLast = index == movements.lastIndex
            )
        }
    }
}