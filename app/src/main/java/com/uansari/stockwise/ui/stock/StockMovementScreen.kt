package com.uansari.stockwise.ui.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.domain.model.QuickStockAction
import com.uansari.stockwise.ui.components.EmptyState
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.product_detail.components.QuickStockDialog
import com.uansari.stockwise.ui.stock.components.MovementFilterSection
import com.uansari.stockwise.ui.stock.components.MovementListItem
import com.uansari.stockwise.ui.stock.components.MovementSummaryCard
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockMovementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val datePickerState = rememberDatePickerState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is StockMovementEffect.NavigateBack -> onNavigateBack()
                is StockMovementEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Column {
                Text("Stock History")
                if (state.isProductLoaded) {
                    Text(
                        text = state.productName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }, navigationIcon = {
            IconButton(onClick = { viewModel.onEvent(StockMovementEvent.NavigateBack) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }, actions = {
            // Quick Stock Actions Menu
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Stock")
            }

            DropdownMenu(
                expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Add Stock") },
                    leadingIcon = { Icon(Icons.Default.AddCircle, null) },
                    onClick = {
                        showMenu = false
                        viewModel.onEvent(
                            StockMovementEvent.ShowQuickStockDialog(
                                QuickStockAction.ADD_STOCK
                            )
                        )
                    })
                DropdownMenuItem(
                    text = { Text("Remove Stock") },
                    leadingIcon = { Icon(Icons.Default.RemoveCircle, null) },
                    onClick = {
                        showMenu = false
                        viewModel.onEvent(
                            StockMovementEvent.ShowQuickStockDialog(
                                QuickStockAction.REMOVE_STOCK
                            )
                        )
                    })
                DropdownMenuItem(
                    text = { Text("Adjust Stock") },
                    leadingIcon = { Icon(Icons.Default.Tune, null) },
                    onClick = {
                        showMenu = false
                        viewModel.onEvent(
                            StockMovementEvent.ShowQuickStockDialog(
                                QuickStockAction.ADJUST_STOCK
                            )
                        )
                    })
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->

        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            StockMovementContent(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = modifier.padding(paddingValues)
            )
        }

        // Quick Stock Dialog
        if (state.isQuickStockDialogVisible && state.quickStockAction != null) {
            QuickStockDialog(
                action = state.quickStockAction!!,
                currentStock = state.currentStock,
                isLoading = state.isPerformingAction,
                onConfirm = { quantity, notes ->
                    viewModel.onEvent(StockMovementEvent.PerformQuickStock(quantity, notes))
                },
                onDismiss = { viewModel.onEvent(StockMovementEvent.HideQuickStockDialog) })
        }

        // Date Picker Dialog
        if (state.isDatePickerVisible) {
            DatePickerDialog(
                onDismissRequest = { viewModel.onEvent(StockMovementEvent.HideDatePicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date =
                                    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                viewModel.onEvent(StockMovementEvent.OnDateSelected(date))
                            }
                        }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(StockMovementEvent.HideDatePicker) }) {
                        Text("Cancel")
                    }
                }) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun StockMovementContent(
    state: StockMovementState, onEvent: (StockMovementEvent) -> Unit, modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Summary Card
        item {
            MovementSummaryCard(
                summary = state.summary,
                currentStock = state.currentStock,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Filters
        item {
            MovementFilterSection(
                typeFilter = state.typeFilter,
                startDate = state.startDate,
                endDate = state.endDate,
                isExpanded = state.isFilterExpanded,
                onTypeFilterChanged = { onEvent(StockMovementEvent.OnTypeFilterChanged(it)) },
                onStartDateClick = { onEvent(StockMovementEvent.ShowDatePicker(DatePickerTarget.START_DATE)) },
                onEndDateClick = { onEvent(StockMovementEvent.ShowDatePicker(DatePickerTarget.END_DATE)) },
                onClearStartDate = { onEvent(StockMovementEvent.OnStartDateChanged(null)) },
                onClearEndDate = { onEvent(StockMovementEvent.OnEndDateChanged(null)) },
                onClearAllFilters = { onEvent(StockMovementEvent.ClearFilters) },
                onToggleExpanded = { onEvent(StockMovementEvent.ToggleFilterExpanded) },
                hasFilters = state.hasFilters
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Results count
        if (!state.isEmpty) {
            item {
                Text(
                    text = "${state.filteredMovements.size} movements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Content
        when {
            state.hasNoMovements -> {
                item {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No stock movements",
                        message = "Stock changes will appear here",
                        modifier = Modifier.fillParentMaxHeight(0.5f)
                    )
                }
            }

            state.isEmpty && state.hasFilters -> {
                item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No movements found",
                        message = "Try adjusting your filters",
                        modifier = Modifier.fillParentMaxHeight(0.5f),
                        action = {
                            TextButton(onClick = { onEvent(StockMovementEvent.ClearFilters) }) {
                                Text("Clear Filters")
                            }
                        })
                }
            }

            else -> {
                items(
                    items = state.filteredMovements, key = { it.id }) { movement ->
                    MovementListItem(
                        movement = movement,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}