package com.uansari.stockwise.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.ui.components.AlertCard
import com.uansari.stockwise.ui.components.LargeStatsCard
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.components.SectionHeader
import com.uansari.stockwise.ui.components.SmallStatsCard
import com.uansari.stockwise.ui.dashboard.components.LowStockItem
import com.uansari.stockwise.ui.dashboard.components.QuickActionsRow
import com.uansari.stockwise.ui.dashboard.components.RecentSaleItem
import com.uansari.stockwise.util.CurrencyFormatter.formatAsCurrency
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProducts: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToLowStock: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit,
    onNavigateToProductDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.NavigateToProducts -> onNavigateToProducts()
                is DashboardEffect.NavigateToNewSale -> onNavigateToNewSale()
                is DashboardEffect.NavigateToLowStock -> onNavigateToLowStock()
                is DashboardEffect.NavigateToSaleDetail -> onNavigateToSaleDetail(effect.saleId)
                is DashboardEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                is DashboardEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "StockWise", fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.onEvent(DashboardEvent.Refresh) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (state.isLoading && !state.isRefreshing) {
                LoadingScreen()
            } else {
                DashboardContent(
                    state = state, onEvent = viewModel::onEvent, modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState, onEvent: (DashboardEvent) -> Unit, modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Inventory Value Card
        item {
            LargeStatsCard(
                title = "Inventory Value",
                value = state.inventoryStats.totalValue.formatAsCurrency(),
                subtitle = "${state.inventoryStats.totalProducts} products in stock",
                icon = Icons.Default.Inventory
            )
        }

        // Today's Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallStatsCard(
                    title = "Today's Sales",
                    value = state.dailySummary.totalSales.formatAsCurrency(),
                    subtitle = "${state.dailySummary.saleCount} transactions",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )

                SmallStatsCard(
                    title = "Today's Profit",
                    value = state.dailySummary.totalProfit.formatAsCurrency(),
                    subtitle = if (state.profitMargin >= 0) {
                        "${state.profitMargin.toInt()}% margin"
                    } else null,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Low Stock Alert
        if (state.hasLowStockAlert) {
            item {
                AlertCard(
                    title = "Low Stock Alert",
                    count = state.lowStockProducts.size,
                    icon = Icons.Default.Warning,
                    onClick = { onEvent(DashboardEvent.NavigateToLowStock) })
            }

            // Show first 3 low stock items
            items(
                items = state.lowStockProducts.take(3),
                key = { it.product.id }) { productWithDetails ->
                LowStockItem(
                    productWithDetails = productWithDetails,
                    onClick = { onEvent(DashboardEvent.NavigateToProductDetail(productWithDetails.product.id)) })
            }
        }

        // Quick Actions
        item {
            SectionHeader(title = "Quick Actions")
        }

        item {
            QuickActionsRow(
                onNewSale = { onEvent(DashboardEvent.NavigateToNewSale) },
                onAddProduct = { onEvent(DashboardEvent.NavigateToProducts) },
                onViewProducts = { onEvent(DashboardEvent.NavigateToProducts) })
        }

        // Recent Sales
        if (state.recentSales.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Sales",
                    action = "View All",
                    onActionClick = { /* TODO: Navigate to sales */ })
            }

            items(
                items = state.recentSales, key = { it.id }) { sale ->
                RecentSaleItem(
                    sale = sale,
                    onClick = { onEvent(DashboardEvent.NavigateToSaleDetail(sale.id)) })
            }
        }
    }
}