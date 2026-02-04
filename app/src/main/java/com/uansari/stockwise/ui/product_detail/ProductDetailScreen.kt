package com.uansari.stockwise.ui.product_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.ui.components.ConfirmDialog
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.components.SectionHeader
import com.uansari.stockwise.ui.product_detail.components.ProductHeaderSection
import com.uansari.stockwise.ui.product_detail.components.ProductPricingCard
import com.uansari.stockwise.ui.product_detail.components.ProductStockCard
import com.uansari.stockwise.ui.product_detail.components.ProductSupplierCard
import com.uansari.stockwise.ui.product_detail.components.QuickStockDialog
import com.uansari.stockwise.ui.product_detail.components.StockMovementList
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToStockHistory: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ProductDetailEffect.NavigateBack -> onNavigateBack()
                is ProductDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.productId)
                is ProductDetailEffect.NavigateToStockHistory -> onNavigateToStockHistory(effect.productId)
                is ProductDetailEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ProductDetailEffect.ProductDeleted -> {
                    snackbarHostState.showSnackbar("Product deleted")
                }
            }
        }
    }

    Scaffold(topBar = {
        ProductDetailTopBar(
            state = state, onEvent = viewModel::onEvent
        )
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->

        ProductDetailContent(
            state = state, onEvent = viewModel::onEvent, modifier = modifier.padding(top = paddingValues.calculateTopPadding())
        )

        // Delete Confirmation Dialog
        if (state.isDeleteDialogVisible) {
            ConfirmDialog(
                title = "Delete Product",
                message = "Are you sure you want to delete \"${state.product?.name}\"? " + "This will hide the product from your inventory. " + "Sales history will be preserved.",
                confirmText = "Delete",
                dismissText = "Cancel",
                onConfirm = { viewModel.onEvent(ProductDetailEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(ProductDetailEvent.HideDeleteDialog) },
                isDestructive = true
            )
        }

        // Quick Stock Dialog
        if (state.isQuickStockDialogVisible && state.quickStockAction != null) {
            QuickStockDialog(
                action = state.quickStockAction!!,
                currentStock = state.currentStock,
                isLoading = state.isPerformingAction,
                onConfirm = { quantity, notes ->
                    viewModel.onEvent(ProductDetailEvent.PerformQuickStock(quantity, notes))
                },
                onDismiss = { viewModel.onEvent(ProductDetailEvent.HideQuickStockDialog) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailTopBar(
    state: ProductDetailState, onEvent: (ProductDetailEvent) -> Unit
) {
    TopAppBar(title = { Text("Product Details") }, navigationIcon = {
        IconButton(onClick = { onEvent(ProductDetailEvent.NavigateBack) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }, actions = {
        if (state.isProductLoaded) {
            IconButton(onClick = { onEvent(ProductDetailEvent.NavigateToEdit) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            IconButton(onClick = { onEvent(ProductDetailEvent.ShowDeleteDialog) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    })
}

@Composable
private fun ProductDetailContent(
    state: ProductDetailState, onEvent: (ProductDetailEvent) -> Unit, modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> {
            LoadingScreen(modifier = modifier)
        }

        state.productWithDetails == null -> {
            // Product not found
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Product not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            val product = state.product!!
            val category = state.category!!
            val supplier = state.supplier

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Section
                ProductHeaderSection(
                    product = product,
                    category = category,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Stock Card
                ProductStockCard(
                    product = product,
                    stockValue = state.stockValue,
                    onQuickStockAction = { action ->
                        onEvent(ProductDetailEvent.ShowQuickStockDialog(action))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Pricing Card
                ProductPricingCard(
                    product = product,
                    profitPerUnit = state.profitPerUnit,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Supplier Card
                ProductSupplierCard(
                    supplier = supplier, modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Stock History Section
                if (state.recentMovements.isNotEmpty()) {
                    SectionHeader(
                        title = "Recent Stock Activity",
                        action = if (state.hasMoreMovements) "View All" else null,
                        onActionClick = { onEvent(ProductDetailEvent.NavigateToStockHistory) })

                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        StockMovementList(
                            movements = state.recentMovements,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                // Bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}