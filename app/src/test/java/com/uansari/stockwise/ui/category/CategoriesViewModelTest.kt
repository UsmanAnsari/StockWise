package com.uansari.stockwise.ui.category

import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.domain.usecase.category.CreateCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.DeleteCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.GetCategoriesWithProductCountUseCase
import com.uansari.stockwise.domain.usecase.category.UpdateCategoryUseCase
import com.uansari.stockwise.domain.usecase.category.ValidateCategoryUseCase
import com.uansari.stockwise.ui.categories.CategoriesViewModel
import com.uansari.stockwise.ui.categories.CategoryEvent
import com.uansari.stockwise.util.MainDispatcherRule
import com.uansari.stockwise.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    // Using MockK for these tests to demonstrate both approaches
    private lateinit var getCategoriesWithProductCountUseCase: GetCategoriesWithProductCountUseCase
    private lateinit var validateCategoryUseCase: ValidateCategoryUseCase
    private lateinit var createCategoryUseCase: CreateCategoryUseCase
    private lateinit var updateCategoryUseCase: UpdateCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    
    private lateinit var viewModel: CategoriesViewModel
    
    private val sampleCategories = listOf(
        TestFixtures.createCategoryWithProductCount(1L, "Electronics", 5),
        TestFixtures.createCategoryWithProductCount(2L, "Clothing", 3),
        TestFixtures.createCategoryWithProductCount(3L, "Empty Category", 0)
    )
    
    @Before
    fun setUp() {
        getCategoriesWithProductCountUseCase = mockk()
        validateCategoryUseCase = mockk()
        createCategoryUseCase = mockk()
        updateCategoryUseCase = mockk()
        deleteCategoryUseCase = mockk()
        
        // Default mock behavior
        coEvery { getCategoriesWithProductCountUseCase() } returns flowOf(sampleCategories)
    }
    
    private fun createViewModel(): CategoriesViewModel {
        return CategoriesViewModel(
            getCategoriesWithProductCountUseCase = getCategoriesWithProductCountUseCase,
            validateCategoryUseCase = validateCategoryUseCase,
            createCategoryUseCase = createCategoryUseCase,
            updateCategoryUseCase = updateCategoryUseCase,
            deleteCategoryUseCase = deleteCategoryUseCase
        )
    }
    
    // ==================== LOADING ====================
    
    @Test
    fun `initial state loads categories`() = runTest {
        // Act
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.categories).hasSize(3)
    }
    
    // ==================== ADD DIALOG ====================
    
    @Test
    fun `show add dialog updates state correctly`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isAddEditDialogVisible).isTrue()
        assertThat(state.isEditMode).isFalse()
        assertThat(state.formName).isEmpty()
        assertThat(state.editingCategory).isNull()
    }
    
    @Test
    fun `show edit dialog populates form with category data`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        val categoryToEdit = sampleCategories.first()
        
        // Act
        viewModel.onEvent(CategoryEvent.ShowEditDialog(categoryToEdit))
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isAddEditDialogVisible).isTrue()
        assertThat(state.isEditMode).isTrue()
        assertThat(state.formName).isEqualTo(categoryToEdit.name)
        assertThat(state.editingCategory).isEqualTo(categoryToEdit)
    }
    
    @Test
    fun `hide dialog clears form state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        viewModel.onEvent(CategoryEvent.OnNameChanged("Test"))
        
        // Act
        viewModel.onEvent(CategoryEvent.HideAddEditDialog)
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isAddEditDialogVisible).isFalse()
        assertThat(state.formName).isEmpty()
    }
    
    // ==================== FORM CHANGES ====================
    
    @Test
    fun `name change updates form state and clears error`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        
        // Act
        viewModel.onEvent(CategoryEvent.OnNameChanged("New Category"))
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.formName).isEqualTo("New Category")
        assertThat(state.nameError).isNull()
    }
    
    @Test
    fun `color selection updates form state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        
        // Act
        viewModel.onEvent(CategoryEvent.OnColorSelected("#FF5722"))
        
        // Assert
        assertThat(viewModel.uiState.value.formColorHex).isEqualTo("#FF5722")
    }
    
    // ==================== CREATE CATEGORY ====================

    @Test
    fun `save category with invalid data shows errors`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        coEvery { 
            validateCategoryUseCase(any()) 
        } returns ValidateCategoryUseCase.ValidationResult(
            isValid = false,
            errors = mapOf(ValidateCategoryUseCase.FIELD_NAME to "Name already exists")
        )
        
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        viewModel.onEvent(CategoryEvent.OnNameChanged("Existing"))
        
        // Act
        viewModel.onEvent(CategoryEvent.SaveCategory)
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.nameError).isEqualTo("Name already exists")
        assertThat(state.isAddEditDialogVisible).isTrue() // Dialog stays open
    }
    
    // ==================== DELETE CATEGORY ====================
    
    @Test
    fun `show delete dialog for category with products`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        val categoryWithProducts = sampleCategories.first() // Has 5 products
        
        // Act
        viewModel.onEvent(CategoryEvent.ShowDeleteDialog(categoryWithProducts))
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isDeleteDialogVisible).isTrue()
        assertThat(state.categoryToDelete).isEqualTo(categoryWithProducts)
    }


    // ==================== COMPUTED PROPERTIES ====================
    
    @Test
    fun `totalProducts sums product counts`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Assert
        // 5 + 3 + 0 = 8
        assertThat(viewModel.uiState.value.totalProducts).isEqualTo(8)
    }
    
    @Test
    fun `canSave is true when form is valid`() = runTest {
        // Arrange
        viewModel = createViewModel()
        viewModel.onEvent(CategoryEvent.ShowAddDialog)
        
        // Initially false (name is empty)
        assertThat(viewModel.uiState.value.canSave).isFalse()
        
        // Act
        viewModel.onEvent(CategoryEvent.OnNameChanged("Valid Name"))
        
        // Assert
        assertThat(viewModel.uiState.value.canSave).isTrue()
    }
}