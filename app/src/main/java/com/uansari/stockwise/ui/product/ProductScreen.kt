package com.uansari.stockwise.ui.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.ui.components.EmptyState
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.product.components.ActiveFiltersRow
import com.uansari.stockwise.ui.product.components.ProductCard
import com.uansari.stockwise.ui.product.components.ProductFilterSheet
import com.uansari.stockwise.util.StockFilter
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToStockMovement: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ProductEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                is ProductEffect.NavigateToEditProduct -> onNavigateToEditProduct(effect.productId)
                is ProductEffect.NavigateToStockMovement -> onNavigateToStockMovement(effect.productId)
                is ProductEffect.NavigateToAddProduct -> onNavigateToAddProduct()
                is ProductEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(topBar = {
        ProductsTopBar(
            state = state, onEvent = viewModel::onEvent
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { viewModel.onEvent(ProductEvent.OnAddProductClicked) }) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onEvent(ProductEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                // Active filters row
                AnimatedVisibility(
                    visible = state.isFiltered,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ActiveFiltersRow(
                        selectedCategory = state.selectedCategory,
                        stockFilter = state.stockFilter,
                        onClearCategory = {
                            viewModel.onEvent(ProductEvent.OnCategorySelected(null))
                        },
                        onClearStockFilter = {
                            viewModel.onEvent(ProductEvent.OnStockFilterSelected(StockFilter.ALL))
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Content
                ProductsContent(
                    state = state, onEvent = viewModel::onEvent, modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter Bottom Sheet
        if (state.isFilterSheetVisible) {
            ProductFilterSheet(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                stockFilter = state.stockFilter,
                sortOption = state.sortOption,
                onCategoryChange = { viewModel.onEvent(ProductEvent.OnCategorySelected(it)) },
                onStockFilterChange = { viewModel.onEvent(ProductEvent.OnStockFilterSelected(it)) },
                onSortOptionChange = { viewModel.onEvent(ProductEvent.OnSortOptionSelected(it)) },
                onClearFilters = { viewModel.onEvent(ProductEvent.ClearAllFilters) },
                onDismiss = { viewModel.onEvent(ProductEvent.HideFilterSheet) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTopBar(
    state: ProductState, onEvent: (ProductEvent) -> Unit, modifier: Modifier = Modifier
) {
    if (state.isSearchActive) {
        // Search mode
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(ProductEvent.OnSearchQueryChanged(it)) },
            onSearch = { /* Already filtering live */ },
            active = false,
            onActiveChange = { },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search products...") },
            leadingIcon = {
                IconButton(onClick = { onEvent(ProductEvent.OnSearchActiveChanged(false)) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onEvent(ProductEvent.OnSearchQueryChanged("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }) { }
    } else {
        // Normal mode
        TopAppBar(title = { Text("Products") }, actions = {
            IconButton(onClick = { onEvent(ProductEvent.OnSearchActiveChanged(true)) }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            BadgedBox(
                badge = {
                    if (state.activeFilterCount > 0) {
                        Badge { Text(state.activeFilterCount.toString()) }
                    }
                }) {
                IconButton(onClick = { onEvent(ProductEvent.ShowFilterSheet) }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        })
    }
}

@Composable
private fun ProductsContent(
    state: ProductState, onEvent: (ProductEvent) -> Unit, modifier: Modifier = Modifier
) {
    when {
        state.isLoading && !state.isRefreshing -> {
            LoadingScreen(modifier = modifier)
        }

        state.hasNoProducts -> {
            EmptyState(
                icon = Icons.Default.Inventory,
                title = "No products yet",
                message = "Add your first product to get started",
                modifier = modifier,
                action = {
                    Button(onClick = { onEvent(ProductEvent.OnAddProductClicked) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Product")
                    }
                })
        }

        state.isEmpty && state.isFiltered -> {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No products found",
                message = "Try adjusting your search or filters",
                modifier = modifier,
                action = {
                    TextButton(onClick = { onEvent(ProductEvent.ClearAllFilters) }) {
                        Text("Clear Filters")
                    }
                })
        }

        else -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Results count
                item {
                    Text(
                        text = "${state.filteredProducts.size} products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(
                    items = state.filteredProducts, key = { it.product.id }) { productWithDetails ->
                    ProductCard(productWithDetails = productWithDetails, onClick = {
                        onEvent(ProductEvent.OnProductClicked(productWithDetails.product.id))
                    }, onEditClick = {
                        onEvent(ProductEvent.OnEditProductClicked(productWithDetails.product.id))
                    }, onStockClick = {
                        onEvent(ProductEvent.OnStockAdjustClicked(productWithDetails.product.id))
                    })
                }
            }
        }
    }
}