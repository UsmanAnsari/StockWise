package com.uansari.stockwise.ui.suppliers

import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import com.uansari.stockwise.ui.base.UiEffect
import com.uansari.stockwise.ui.base.UiEvent
import com.uansari.stockwise.ui.base.UiState

data class SupplierState(
    // Loading states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Data
    val suppliers: List<SupplierWithProductCount> = emptyList(),

    // Dialog states
    val isAddEditDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val editingSupplier: SupplierWithProductCount? = null,
    val supplierToDelete: SupplierWithProductCount? = null,

    // Form fields
    val formName: String = "",
    val formContactPerson: String = "",
    val formPhone: String = "",
    val formEmail: String = "",
    val formAddress: String = "",

    // Validation errors
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,

    // Error
    val error: String? = null
) : UiState {

    val isEditMode: Boolean
        get() = editingSupplier != null

    val dialogTitle: String
        get() = if (isEditMode) "Edit Supplier" else "New Supplier"

    val saveButtonText: String
        get() = if (isEditMode) "Update" else "Create"

    val isEmpty: Boolean
        get() = suppliers.isEmpty() && !isLoading

    val canSave: Boolean
        get() = formName.isNotBlank() && !isSaving &&
                nameError == null && emailError == null && phoneError == null

    val totalProducts: Int
        get() = suppliers.sumOf { it.productCount }
}

// ==================== EVENTS (Intents) ====================

sealed interface SupplierEvent : UiEvent {
    // Lifecycle
    data object LoadSuppliers : SupplierEvent

    // Dialog management
    data object ShowAddDialog : SupplierEvent
    data class ShowEditDialog(val supplier: SupplierWithProductCount) : SupplierEvent
    data object HideAddEditDialog : SupplierEvent
    data class ShowDeleteDialog(val supplier: SupplierWithProductCount) : SupplierEvent
    data object HideDeleteDialog : SupplierEvent

    // Form field changes
    data class OnNameChanged(val value: String) : SupplierEvent
    data class OnContactPersonChanged(val value: String) : SupplierEvent
    data class OnPhoneChanged(val value: String) : SupplierEvent
    data class OnEmailChanged(val value: String) : SupplierEvent
    data class OnAddressChanged(val value: String) : SupplierEvent

    // Actions
    data object SaveSupplier : SupplierEvent
    data object ConfirmDelete : SupplierEvent

    // Navigation
    data object NavigateBack : SupplierEvent

    // Error
    data object ClearError : SupplierEvent
}

// ==================== SIDE EFFECTS ====================

sealed interface SupplierEffect : UiEffect {
    data object NavigateBack : SupplierEffect
    data class ShowSnackbar(val message: String) : SupplierEffect
    data object SupplierSaved : SupplierEffect
    data object SupplierDeleted : SupplierEffect
}