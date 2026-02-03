package com.uansari.stockwise.ui.categories

import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.domain.usecase.category.CreateCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.DeleteCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.GetCategoriesWithProductCountUseCase
import com.uansari.stockwise.domain.usecase.category.UpdateCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.ValidateCategoryUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesWithProductCountUseCase: GetCategoriesWithProductCountUseCase,
    private val validateCategoryUseCase: ValidateCategoryUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : BaseViewModel<CategoryState, CategoryEvent, CategoryEffect>(CategoryState()) {

    init {
        onEvent(CategoryEvent.LoadCategories)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: CategoryEvent) {
        when (event) {
            // Lifecycle
            is CategoryEvent.LoadCategories -> loadCategories()

            // Dialog management
            is CategoryEvent.ShowAddDialog -> showAddDialog()
            is CategoryEvent.ShowEditDialog -> showEditDialog(event.category)
            is CategoryEvent.HideAddEditDialog -> hideAddEditDialog()
            is CategoryEvent.ShowDeleteDialog -> showDeleteDialog(event.category)
            is CategoryEvent.HideDeleteDialog -> hideDeleteDialog()

            // Form field changes
            is CategoryEvent.OnNameChanged -> onNameChanged(event.value)
            is CategoryEvent.OnDescriptionChanged -> updateState { copy(formDescription = event.value) }
            is CategoryEvent.OnColorSelected -> updateState {
                copy(
                    formColorHex = event.colorHex, isColorPickerVisible = false
                )
            }

            // Color picker
            is CategoryEvent.ShowColorPicker -> updateState { copy(isColorPickerVisible = true) }
            is CategoryEvent.HideColorPicker -> updateState { copy(isColorPickerVisible = false) }

            // Actions
            is CategoryEvent.SaveCategory -> saveCategory()
            is CategoryEvent.ConfirmDelete -> deleteCategory()

            // Navigation
            is CategoryEvent.NavigateBack -> sendEffect(CategoryEffect.NavigateBack)

            // Error
            is CategoryEvent.ClearError -> updateState { copy(error = null) }
        }
    }

    // ==================== LOAD DATA ====================

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesWithProductCountUseCase().catch { e ->
                    updateState {
                        copy(
                            isLoading = false, error = e.message ?: "Failed to load categories"
                        )
                    }
                }.collect { categories ->
                    updateState {
                        copy(
                            categories = categories, isLoading = false
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
                editingCategory = null,
                formName = "",
                formDescription = "",
                formColorHex = null,
                nameError = null,
                colorError = null
            )
        }
    }

    private fun showEditDialog(category: CategoryWithProductCount) {
        updateState {
            copy(
                isAddEditDialogVisible = true,
                editingCategory = category,
                formName = category.name,
                formDescription = category.description ?: "",
                formColorHex = category.color,
                nameError = null,
                colorError = null
            )
        }
    }

    private fun hideAddEditDialog() {
        updateState {
            copy(
                isAddEditDialogVisible = false,
                editingCategory = null,
                formName = "",
                formDescription = "",
                formColorHex = null,
                nameError = null,
                colorError = null,
                isColorPickerVisible = false
            )
        }
    }

    private fun showDeleteDialog(category: CategoryWithProductCount) {
        updateState {
            copy(
                isDeleteDialogVisible = true, categoryToDelete = category
            )
        }
    }

    private fun hideDeleteDialog() {
        updateState {
            copy(
                isDeleteDialogVisible = false, categoryToDelete = null
            )
        }
    }

    // ==================== FORM ====================

    private fun onNameChanged(value: String) {
        updateState {
            copy(
                formName = value, nameError = null
            )
        }
    }

    // ==================== SAVE ====================

    private fun saveCategory() {
        viewModelScope.launch {
            // Validate
            val validationResult = validateCategoryUseCase(
                ValidateCategoryUseCase.ValidationParams(
                    name = currentState.formName,
                    colorHex = currentState.formColorHex,
                    excludeCategoryId = currentState.editingCategory?.id
                )
            )

            if (!validationResult.isValid) {
                updateState {
                    copy(
                        nameError = validationResult.errors[ValidateCategoryUseCase.FIELD_NAME],
                        colorError = validationResult.errors[ValidateCategoryUseCase.FIELD_COLOR]
                    )
                }
                return@launch
            }

            updateState { copy(isSaving = true) }

            val result = if (currentState.isEditMode) {
                updateCategoryUseCase(
                    UpdateCategoryUseCase.Params(
                        categoryId = currentState.editingCategory!!.id,
                        name = currentState.formName,
                        description = currentState.formDescription.takeIf { it.isNotBlank() },
                        color = currentState.formColorHex ?: ""
                    )
                )
            } else {
                createCategoryUseCase(
                    CreateCategoryUseCase.Params(
                        name = currentState.formName,
                        description = currentState.formDescription.takeIf { it.isNotBlank() },
                        color = currentState.formColorHex ?: ""
                    )
                ).map { Unit }
            }

            result.onSuccess {
                    updateState { copy(isSaving = false) }
                    hideAddEditDialog()
                    sendEffect(CategoryEffect.CategorySaved)

                    val message =
                        if (currentState.isEditMode) "Category updated" else "Category created"
                    sendEffect(CategoryEffect.ShowSnackbar(message))
                }.onFailure { error ->
                    updateState { copy(isSaving = false) }
                    sendEffect(
                        CategoryEffect.ShowSnackbar(
                            error.message ?: "Failed to save category"
                        )
                    )
                }
        }
    }

    // ==================== DELETE ====================

    private fun deleteCategory() {
        val category = currentState.categoryToDelete ?: return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            deleteCategoryUseCase(category.id).onSuccess {
                    updateState { copy(isSaving = false) }
                    hideDeleteDialog()
                    sendEffect(CategoryEffect.CategoryDeleted)
                    sendEffect(CategoryEffect.ShowSnackbar("Category deleted"))
                }.onFailure { error ->
                    updateState { copy(isSaving = false) }
                    hideDeleteDialog()
                    sendEffect(
                        CategoryEffect.ShowSnackbar(
                            error.message ?: "Failed to delete category"
                        )
                    )
                }
        }
    }
}