package com.uansari.stockwise.ui.suppliers

import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import com.uansari.stockwise.domain.usecase.suppplier.CreateSupplierUseCase
import com.uansari.stockwise.domain.usecase.suppplier.DeleteSupplierUseCase
import com.uansari.stockwise.domain.usecase.suppplier.GetSuppliersWithProductCountUseCase
import com.uansari.stockwise.domain.usecase.suppplier.UpdateSupplierUseCase
import com.uansari.stockwise.domain.usecase.suppplier.ValidateSupplierUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val getSuppliersWithProductCountUseCase: GetSuppliersWithProductCountUseCase,
    private val validateSupplierUseCase: ValidateSupplierUseCase,
    private val createSupplierUseCase: CreateSupplierUseCase,
    private val updateSupplierUseCase: UpdateSupplierUseCase,
    private val deleteSupplierUseCase: DeleteSupplierUseCase
) : BaseViewModel<SupplierState, SupplierEvent, SupplierEffect>(SupplierState()) {
    
    init {
        onEvent(SupplierEvent.LoadSuppliers)
    }
    
    // ==================== SINGLE ENTRY POINT ====================
    
    override fun onEvent(event: SupplierEvent) {
        when (event) {
            // Lifecycle
            is SupplierEvent.LoadSuppliers -> loadSuppliers()
            
            // Dialog management
            is SupplierEvent.ShowAddDialog -> showAddDialog()
            is SupplierEvent.ShowEditDialog -> showEditDialog(event.supplier)
            is SupplierEvent.HideAddEditDialog -> hideAddEditDialog()
            is SupplierEvent.ShowDeleteDialog -> showDeleteDialog(event.supplier)
            is SupplierEvent.HideDeleteDialog -> hideDeleteDialog()
            
            // Form field changes
            is SupplierEvent.OnNameChanged -> onNameChanged(event.value)
            is SupplierEvent.OnContactPersonChanged -> updateState { copy(formContactPerson = event.value) }
            is SupplierEvent.OnPhoneChanged -> onPhoneChanged(event.value)
            is SupplierEvent.OnEmailChanged -> onEmailChanged(event.value)
            is SupplierEvent.OnAddressChanged -> updateState { copy(formAddress = event.value) }
            
            // Actions
            is SupplierEvent.SaveSupplier -> saveSupplier()
            is SupplierEvent.ConfirmDelete -> deleteSupplier()
            
            // Navigation
            is SupplierEvent.NavigateBack -> sendEffect(SupplierEffect.NavigateBack)
            
            // Error
            is SupplierEvent.ClearError -> updateState { copy(error = null) }
        }
    }
    
    // ==================== LOAD DATA ====================
    
    private fun loadSuppliers() {
        viewModelScope.launch {
            getSuppliersWithProductCountUseCase()
                .catch { e ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load suppliers"
                        )
                    }
                }
                .collect { suppliers ->
                    updateState {
                        copy(
                            suppliers = suppliers,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    // ==================== DIALOG MANAGEMENT ====================
    
    private fun showAddDialog() {
        updateState {
            copy(
                isAddEditDialogVisible = true,
                editingSupplier = null,
                formName = "",
                formContactPerson = "",
                formPhone = "",
                formEmail = "",
                formAddress = "",
                nameError = null,
                emailError = null,
                phoneError = null
            )
        }
    }
    
    private fun showEditDialog(supplier: SupplierWithProductCount) {
        updateState {
            copy(
                isAddEditDialogVisible = true,
                editingSupplier = supplier,
                formName = supplier.name,
                formContactPerson = supplier.contactPerson ?: "",
                formPhone = supplier.phone ?: "",
                formEmail = supplier.email ?: "",
                formAddress = supplier.address ?: "",
                nameError = null,
                emailError = null,
                phoneError = null
            )
        }
    }
    
    private fun hideAddEditDialog() {
        updateState {
            copy(
                isAddEditDialogVisible = false,
                editingSupplier = null,
                formName = "",
                formContactPerson = "",
                formPhone = "",
                formEmail = "",
                formAddress = "",
                nameError = null,
                emailError = null,
                phoneError = null
            )
        }
    }
    
    private fun showDeleteDialog(supplier: SupplierWithProductCount) {
        updateState {
            copy(
                isDeleteDialogVisible = true,
                supplierToDelete = supplier
            )
        }
    }
    
    private fun hideDeleteDialog() {
        updateState {
            copy(
                isDeleteDialogVisible = false,
                supplierToDelete = null
            )
        }
    }
    
    // ==================== FORM ====================
    
    private fun onNameChanged(value: String) {
        updateState {
            copy(
                formName = value,
                nameError = null
            )
        }
    }
    
    private fun onPhoneChanged(value: String) {
        updateState {
            copy(
                formPhone = value,
                phoneError = null
            )
        }
    }
    
    private fun onEmailChanged(value: String) {
        updateState {
            copy(
                formEmail = value,
                emailError = null
            )
        }
    }
    
    // ==================== SAVE ====================
    
    private fun saveSupplier() {
        viewModelScope.launch {
            // Validate
            val validationResult = validateSupplierUseCase(
                ValidateSupplierUseCase.ValidationParams(
                    name = currentState.formName,
                    email = currentState.formEmail.takeIf { it.isNotBlank() },
                    phone = currentState.formPhone.takeIf { it.isNotBlank() },
                    excludeSupplierId = currentState.editingSupplier?.id
                )
            )
            
            if (!validationResult.isValid) {
                updateState {
                    copy(
                        nameError = validationResult.errors[ValidateSupplierUseCase.FIELD_NAME],
                        emailError = validationResult.errors[ValidateSupplierUseCase.FIELD_EMAIL],
                        phoneError = validationResult.errors[ValidateSupplierUseCase.FIELD_PHONE]
                    )
                }
                return@launch
            }
            
            updateState { copy(isSaving = true) }
            
            val result = if (currentState.isEditMode) {
                updateSupplierUseCase(
                    UpdateSupplierUseCase.Params(
                        supplierId = currentState.editingSupplier!!.id,
                        name = currentState.formName,
                        contactPerson = currentState.formContactPerson.takeIf { it.isNotBlank() },
                        phone = currentState.formPhone.takeIf { it.isNotBlank() },
                        email = currentState.formEmail.takeIf { it.isNotBlank() },
                        address = currentState.formAddress.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                createSupplierUseCase(
                    CreateSupplierUseCase.Params(
                        name = currentState.formName,
                        contactPerson = currentState.formContactPerson.takeIf { it.isNotBlank() },
                        phone = currentState.formPhone.takeIf { it.isNotBlank() },
                        email = currentState.formEmail.takeIf { it.isNotBlank() },
                        address = currentState.formAddress.takeIf { it.isNotBlank() }
                    )
                ).map { Unit }
            }
            
            result
                .onSuccess {
                    updateState { copy(isSaving = false) }
                    hideAddEditDialog()
                    sendEffect(SupplierEffect.SupplierSaved)
                    
                    val message = if (currentState.isEditMode) "Supplier updated" else "Supplier created"
                    sendEffect(SupplierEffect.ShowSnackbar(message))
                }
                .onFailure { error ->
                    updateState { copy(isSaving = false) }
                    sendEffect(SupplierEffect.ShowSnackbar(error.message ?: "Failed to save supplier"))
                }
        }
    }
    
    // ==================== DELETE ====================
    
    private fun deleteSupplier() {
        val supplier = currentState.supplierToDelete ?: return
        
        viewModelScope.launch {
            updateState { copy(isSaving = true) }
            
            deleteSupplierUseCase(supplier.id)
                .onSuccess {
                    updateState { copy(isSaving = false) }
                    hideDeleteDialog()
                    sendEffect(SupplierEffect.SupplierDeleted)
                    sendEffect(SupplierEffect.ShowSnackbar("Supplier deleted"))
                }
                .onFailure { error ->
                    updateState { copy(isSaving = false) }
                    hideDeleteDialog()
                    sendEffect(SupplierEffect.ShowSnackbar(error.message ?: "Failed to delete supplier"))
                }
        }
    }
}