package com.uansari.stockwise.ui.suppliers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uansari.stockwise.ui.components.ConfirmDialog
import com.uansari.stockwise.ui.components.EmptyState
import com.uansari.stockwise.ui.components.LoadingScreen
import com.uansari.stockwise.ui.suppliers.components.SupplierDialog
import com.uansari.stockwise.ui.suppliers.components.SupplierListItem
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SuppliersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is SupplierEffect.NavigateBack -> onNavigateBack()
                is SupplierEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is SupplierEffect.SupplierSaved -> { /* Handled by snackbar */ }
                is SupplierEffect.SupplierDeleted -> { /* Handled by snackbar */ }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suppliers") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(SupplierEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(SupplierEvent.ShowAddDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        SuppliersContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = modifier.padding(top = paddingValues.calculateTopPadding())
        )
        
        // Add/Edit Dialog
        if (state.isAddEditDialogVisible) {
            SupplierDialog(
                title = state.dialogTitle,
                name = state.formName,
                contactPerson = state.formContactPerson,
                phone = state.formPhone,
                email = state.formEmail,
                address = state.formAddress,
                nameError = state.nameError,
                phoneError = state.phoneError,
                emailError = state.emailError,
                isSaving = state.isSaving,
                onNameChanged = { viewModel.onEvent(SupplierEvent.OnNameChanged(it)) },
                onContactPersonChanged = { viewModel.onEvent(SupplierEvent.OnContactPersonChanged(it)) },
                onPhoneChanged = { viewModel.onEvent(SupplierEvent.OnPhoneChanged(it)) },
                onEmailChanged = { viewModel.onEvent(SupplierEvent.OnEmailChanged(it)) },
                onAddressChanged = { viewModel.onEvent(SupplierEvent.OnAddressChanged(it)) },
                onSave = { viewModel.onEvent(SupplierEvent.SaveSupplier) },
                onDismiss = { viewModel.onEvent(SupplierEvent.HideAddEditDialog) },
                saveButtonText = state.saveButtonText
            )
        }
        
        // Delete Confirmation Dialog
        if (state.isDeleteDialogVisible && state.supplierToDelete != null) {
            ConfirmDialog(
                title = "Delete Supplier",
                message = "Are you sure you want to delete \"${state.supplierToDelete!!.name}\"? " +
                        "This action cannot be undone.",
                confirmText = "Delete",
                dismissText = "Cancel",
                onConfirm = { viewModel.onEvent(SupplierEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(SupplierEvent.HideDeleteDialog) },
                isDestructive = true
            )
        }
    }
}

@Composable
private fun SuppliersContent(
    state: SupplierState,
    onEvent: (SupplierEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> {
            LoadingScreen(modifier = modifier)
        }
        
        state.isEmpty -> {
            EmptyState(
                icon = Icons.Default.LocalShipping,
                title = "No suppliers yet",
                message = "Add suppliers to track where your products come from",
                modifier = modifier,
                action = {
                    Button(onClick = { onEvent(SupplierEvent.ShowAddDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Supplier")
                    }
                }
            )
        }
        
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary
                item {
                    Text(
                        text = "${state.suppliers.size} suppliers • ${state.totalProducts} products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Suppliers list
                items(
                    items = state.suppliers,
                    key = { it.id }
                ) { supplier ->
                    SupplierListItem(
                        supplier = supplier,
                        onEditClick = { onEvent(SupplierEvent.ShowEditDialog(supplier)) },
                        onDeleteClick = { onEvent(SupplierEvent.ShowDeleteDialog(supplier)) }
                    )
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}