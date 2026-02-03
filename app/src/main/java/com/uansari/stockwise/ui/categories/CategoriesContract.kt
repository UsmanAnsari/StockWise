package com.uansari.stockwise.ui.categories

import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState

data class CategoryState(
    // Loading states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Data
    val categories: List<CategoryWithProductCount> = emptyList(),

    // Dialog states
    val isAddEditDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val editingCategory: CategoryWithProductCount? = null,
    val categoryToDelete: CategoryWithProductCount? = null,

    // Form fields
    val formName: String = "",
    val formDescription: String = "",
    val formColorHex: String? = null,

    // Validation errors
    val nameError: String? = null,
    val colorError: String? = null,

    // Color picker
    val isColorPickerVisible: Boolean = false,

    // Error
    val error: String? = null
) : UiState {

    val isEditMode: Boolean
        get() = editingCategory != null

    val dialogTitle: String
        get() = if (isEditMode) "Edit Category" else "New Category"

    val saveButtonText: String
        get() = if (isEditMode) "Update" else "Create"

    val isEmpty: Boolean
        get() = categories.isEmpty() && !isLoading

    val canSave: Boolean
        get() = formName.isNotBlank() && !isSaving && nameError == null

    val totalProducts: Int
        get() = categories.sumOf { it.productCount }
}

// ==================== EVENTS (Intents) ====================

sealed interface CategoryEvent : UiEvent {
    // Lifecycle
    data object LoadCategories : CategoryEvent

    // Dialog management
    data object ShowAddDialog : CategoryEvent
    data class ShowEditDialog(val category: CategoryWithProductCount) : CategoryEvent
    data object HideAddEditDialog : CategoryEvent
    data class ShowDeleteDialog(val category: CategoryWithProductCount) : CategoryEvent
    data object HideDeleteDialog : CategoryEvent

    // Form field changes
    data class OnNameChanged(val value: String) : CategoryEvent
    data class OnDescriptionChanged(val value: String) : CategoryEvent
    data class OnColorSelected(val colorHex: String?) : CategoryEvent

    // Color picker
    data object ShowColorPicker : CategoryEvent
    data object HideColorPicker : CategoryEvent

    // Actions
    data object SaveCategory : CategoryEvent
    data object ConfirmDelete : CategoryEvent

    // Navigation
    data object NavigateBack : CategoryEvent

    // Error
    data object ClearError : CategoryEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface CategoryEffect : UiEffect {
    data object NavigateBack : CategoryEffect
    data class ShowSnackbar(val message: String) : CategoryEffect
    data object CategorySaved : CategoryEffect
    data object CategoryDeleted : CategoryEffect
}