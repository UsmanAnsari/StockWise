package com.uansari.stockwise.ui.categories

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
import androidx.compose.material.icons.filled.Category
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
import com.uansari.stockwise.ui.categories.components.CategoryDialog
import com.uansari.stockwise.ui.categories.components.CategoryListItem
import com.uansari.stockwise.ui.components.ConfirmDialog
import com.uansari.stockwise.ui.components.EmptyState
import com.uansari.stockwise.ui.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is CategoryEffect.NavigateBack -> onNavigateBack()
                is CategoryEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is CategoryEffect.CategorySaved -> { /* Handled by snackbar */ }
                is CategoryEffect.CategoryDeleted -> { /* Handled by snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(CategoryEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(CategoryEvent.ShowAddDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        CategoriesContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = modifier.padding(top = paddingValues.calculateTopPadding())
        )

        // Add/Edit Dialog
        if (state.isAddEditDialogVisible) {
            CategoryDialog(
                title = state.dialogTitle,
                name = state.formName,
                description = state.formDescription,
                colorHex = state.formColorHex,
                nameError = state.nameError,
                isSaving = state.isSaving,
                onNameChanged = { viewModel.onEvent(CategoryEvent.OnNameChanged(it)) },
                onDescriptionChanged = { viewModel.onEvent(CategoryEvent.OnDescriptionChanged(it)) },
                onColorSelected = { viewModel.onEvent(CategoryEvent.OnColorSelected(it)) },
                onSave = { viewModel.onEvent(CategoryEvent.SaveCategory) },
                onDismiss = { viewModel.onEvent(CategoryEvent.HideAddEditDialog) },
                saveButtonText = state.saveButtonText
            )
        }

        // Delete Confirmation Dialog
        if (state.isDeleteDialogVisible && state.categoryToDelete != null) {
            ConfirmDialog(
                title = "Delete Category",
                message = "Are you sure you want to delete \"${state.categoryToDelete!!.name}\"? " +
                        "This action cannot be undone.",
                confirmText = "Delete",
                dismissText = "Cancel",
                onConfirm = { viewModel.onEvent(CategoryEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(CategoryEvent.HideDeleteDialog) },
                isDestructive = true
            )
        }
    }
}

@Composable
private fun CategoriesContent(
    state: CategoryState,
    onEvent: (CategoryEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> {
            LoadingScreen(modifier = modifier)
        }

        state.isEmpty -> {
            EmptyState(
                icon = Icons.Default.Category,
                title = "No categories yet",
                message = "Create categories to organize your products",
                modifier = modifier,
                action = {
                    Button(onClick = { onEvent(CategoryEvent.ShowAddDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Category")
                    }
                }
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Summary
                item {
                    Text(
                        text = "${state.categories.size} categories • ${state.totalProducts} products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Categories list
                items(
                    items = state.categories,
                    key = { it.id }
                ) { category ->
                    CategoryListItem(
                        category = category,
                        onEditClick = { onEvent(CategoryEvent.ShowEditDialog(category)) },
                        onDeleteClick = { onEvent(CategoryEvent.ShowDeleteDialog(category)) }
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