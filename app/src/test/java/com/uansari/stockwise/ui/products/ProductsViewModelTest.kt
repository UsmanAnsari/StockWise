package com.uansari.stockwise.ui.products

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.domain.usecase.category.GetCategoriesUseCase
import com.uansari.stockwise.domain.usecase.product.FilterProductsUseCase
import com.uansari.stockwise.domain.usecase.product.GetProductsWithDetailsUseCase
import com.uansari.stockwise.ui.product.ProductEffect
import com.uansari.stockwise.ui.product.ProductEvent
import com.uansari.stockwise.ui.product.ProductViewModel
import com.uansari.stockwise.util.FakeCategoryRepository
import com.uansari.stockwise.util.FakeProductRepository
import com.uansari.stockwise.util.MainDispatcherRule
import com.uansari.stockwise.util.StockFilter
import com.uansari.stockwise.util.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.jvm.java

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var fakeProductRepository: FakeProductRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var getProductsWithDetailsUseCase: GetProductsWithDetailsUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var filterProductsUseCase: FilterProductsUseCase
    private lateinit var viewModel: ProductViewModel
    
    @Before
    fun setUp() {
        fakeProductRepository = FakeProductRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        
        getProductsWithDetailsUseCase = GetProductsWithDetailsUseCase(fakeProductRepository)
        getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository)
        filterProductsUseCase = FilterProductsUseCase()
        
        // Set up default data
        fakeProductRepository.setProductsWithDetails(TestFixtures.sampleProductsWithDetails)
        fakeCategoryRepository.setCategories(TestFixtures.sampleCategories)
    }
    
    private fun createViewModel(): ProductViewModel {
        return ProductViewModel(
            getProductsWithDetailsUseCase = getProductsWithDetailsUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            filterProductsUseCase = filterProductsUseCase
        )
    }
    
    // ==================== INITIAL STATE ====================
    
    @Test
    fun `initial state is loading`() = runTest {
        // Act
        viewModel = createViewModel()
        
        // Assert - Check initial state before data loads
        val initialState = viewModel.uiState.value
        assertThat(initialState.isLoading).isTrue()
    }
    
    @Test
    fun `state updates with products after loading`() = runTest {
        // Arrange
        viewModel = createViewModel()
        
        // Act
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.allProducts).isNotEmpty()
        assertThat(state.filteredProducts).isNotEmpty()
    }
    
    // ==================== SEARCH ====================
    
    @Test
    fun `search query filters products`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(ProductEvent.OnSearchQueryChanged("iPhone"))
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.searchQuery).isEqualTo("iPhone")
        assertThat(state.filteredProducts.all { 
            it.product.name.contains("iPhone", ignoreCase = true) ||
            it.product.sku.contains("iPhone", ignoreCase = true)
        }).isTrue()
    }
    
    @Test
    fun `clearing search shows all products`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ProductEvent.OnSearchQueryChanged("iPhone"))
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(ProductEvent.OnSearchQueryChanged(""))
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.filteredProducts.size).isEqualTo(state.allProducts.size)
    }
    
    @Test
    fun `search active state toggles correctly`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act & Assert - Activate
        viewModel.onEvent(ProductEvent.OnSearchActiveChanged(true))
        assertThat(viewModel.uiState.value.isSearchActive).isTrue()
        
        // Act & Assert - Deactivate
        viewModel.onEvent(ProductEvent.OnSearchActiveChanged(false))
        assertThat(viewModel.uiState.value.isSearchActive).isFalse()
    }
    
    // ==================== FILTERS ====================
    
    @Test
    fun `category filter updates filtered products`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(ProductEvent.OnCategorySelected(1L))
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.selectedCategoryId).isEqualTo(1L)
        assertThat(state.filteredProducts.all { it.product.categoryId == 1L }).isTrue()
    }
    
    @Test
    fun `stock filter updates filtered products`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(ProductEvent.OnStockFilterSelected(StockFilter.OUT_OF_STOCK))
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.stockFilter).isEqualTo(StockFilter.OUT_OF_STOCK)
        assertThat(state.filteredProducts.all { it.product.currentStock == 0 }).isTrue()
    }
    
    @Test
    fun `clear all filters resets to default`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ProductEvent.OnCategorySelected(1L))
        viewModel.onEvent(ProductEvent.OnStockFilterSelected(StockFilter.LOW_STOCK))
        viewModel.onEvent(ProductEvent.OnSearchQueryChanged("test"))
        advanceUntilIdle()
        
        // Act
        viewModel.onEvent(ProductEvent.ClearAllFilters)
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.selectedCategoryId).isNull()
        assertThat(state.stockFilter).isEqualTo(StockFilter.ALL)
        assertThat(state.searchQuery).isEmpty()
    }
    
    // ==================== FILTER SHEET ====================
    
    @Test
    fun `show filter sheet updates state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        
        // Act
        viewModel.onEvent(ProductEvent.ShowFilterSheet)
        
        // Assert
        assertThat(viewModel.uiState.value.isFilterSheetVisible).isTrue()
    }
    
    @Test
    fun `hide filter sheet updates state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        viewModel.onEvent(ProductEvent.ShowFilterSheet)
        
        // Act
        viewModel.onEvent(ProductEvent.HideFilterSheet)
        
        // Assert
        assertThat(viewModel.uiState.value.isFilterSheetVisible).isFalse()
    }
    
    // ==================== NAVIGATION SIDE EFFECTS ====================
    
    @Test
    fun `clicking product emits navigation effect`() = runTest {
        // Arrange
        viewModel = createViewModel()
        
        // Act & Assert
        viewModel.sideEffect.test {
            viewModel.onEvent(ProductEvent.OnProductClicked(123L))
            
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProductEffect.NavigateToProductDetail::class.java)
            assertThat((effect as ProductEffect.NavigateToProductDetail).productId).isEqualTo(123L)
        }
    }
    
    @Test
    fun `clicking add product emits navigation effect`() = runTest {
        // Arrange
        viewModel = createViewModel()
        
        // Act & Assert
        viewModel.sideEffect.test {
            viewModel.onEvent(ProductEvent.OnAddProductClicked)
            
            val effect = awaitItem()
            assertThat(effect).isEqualTo(ProductEffect.NavigateToAddProduct)
        }
    }
    
    @Test
    fun `clicking edit emits navigation effect with product id`() = runTest {
        // Arrange
        viewModel = createViewModel()
        
        // Act & Assert
        viewModel.sideEffect.test {
            viewModel.onEvent(ProductEvent.OnEditProductClicked(456L))
            
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProductEffect.NavigateToEditProduct::class.java)
            assertThat((effect as ProductEffect.NavigateToEditProduct).productId).isEqualTo(456L)
        }
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    @Test
    fun `active filter count reflects applied filters`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Initially no filters
        assertThat(viewModel.uiState.value.activeFilterCount).isEqualTo(0)
        
        // Apply category filter
        viewModel.onEvent(ProductEvent.OnCategorySelected(1L))
        assertThat(viewModel.uiState.value.activeFilterCount).isEqualTo(1)
        
        // Apply stock filter
        viewModel.onEvent(ProductEvent.OnStockFilterSelected(StockFilter.LOW_STOCK))
        assertThat(viewModel.uiState.value.activeFilterCount).isEqualTo(2)
    }
    
    @Test
    fun `isEmpty is true when no products match filter`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Act - Search for something that doesn't exist
        viewModel.onEvent(ProductEvent.OnSearchQueryChanged("NONEXISTENT_PRODUCT_XYZ"))
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isEmpty).isTrue()
        assertThat(state.isFiltered).isTrue()
    }
}