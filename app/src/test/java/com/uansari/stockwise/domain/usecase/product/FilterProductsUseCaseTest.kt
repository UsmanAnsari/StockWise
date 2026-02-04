package com.uansari.stockwise.domain.usecase.product

import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.util.ProductSortOption
import com.uansari.stockwise.util.StockFilter
import com.uansari.stockwise.util.TestFixtures
import org.junit.Before
import org.junit.Test

class FilterProductsUseCaseTest {
    
    private lateinit var useCase: FilterProductsUseCase
    private val sampleProducts = TestFixtures.sampleProductsWithDetails
    
    @Before
    fun setUp() {
        useCase = FilterProductsUseCase()
    }
    
    // ==================== SEARCH FILTER ====================
    
    @Test
    fun `filter by search query matches product name`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            searchQuery = "iPhone"
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).hasSize(1)
        assertThat(result.first().product.name).contains("iPhone")
    }
    
    @Test
    fun `filter by search query matches SKU`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            searchQuery = "MACBOOK"
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).hasSize(1)
        assertThat(result.first().product.sku).contains("MACBOOK")
    }
    
    @Test
    fun `filter by search query is case insensitive`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            searchQuery = "iphone" // lowercase
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).isNotEmpty()
    }
    
    @Test
    fun `filter returns all products when search query is blank`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            searchQuery = "   "
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).hasSize(sampleProducts.size)
    }
    
    // ==================== CATEGORY FILTER ====================
    
    @Test
    fun `filter by category returns only matching products`() {
        // Arrange
        val categoryId = 1L
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            categoryId = categoryId
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { it.product.categoryId == categoryId }).isTrue()
    }
    
    @Test
    fun `filter returns all products when category is null`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            categoryId = null
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).hasSize(sampleProducts.size)
    }
    
    // ==================== STOCK FILTER ====================
    
    @Test
    fun `filter by IN_STOCK returns products with stock above threshold`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            stockFilter = StockFilter.IN_STOCK
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { 
            it.product.currentStock > it.product.lowStockThreshold 
        }).isTrue()
    }
    
    @Test
    fun `filter by LOW_STOCK returns products at or below threshold`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            stockFilter = StockFilter.LOW_STOCK
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { product ->
            val p = product.product
            p.currentStock in 1..p.lowStockThreshold
        }).isTrue()
    }
    
    @Test
    fun `filter by OUT_OF_STOCK returns products with zero stock`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            stockFilter = StockFilter.OUT_OF_STOCK
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { it.product.currentStock == 0 }).isTrue()
    }
    
    // ==================== SORTING ====================
    
    @Test
    fun `sort by NAME_ASC sorts alphabetically ascending`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            sortOption = ProductSortOption.NAME_ASC
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        val names = result.map { it.product.name.lowercase() }
        assertThat(names).isInOrder()
    }
    
/*
    @Test
    fun `sort by NAME_DESC sorts alphabetically descending`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            sortOption = ProductSortOption.NAME_DESC
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        val names = result.map { it.product.name.lowercase() }
        assertThat(names).isInOrder(reverseOrder())
    }
*/

    @Test
    fun `sort by STOCK_LOW_HIGH sorts by current stock ascending`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            sortOption = ProductSortOption.STOCK_LOW_HIGH
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        val stocks = result.map { it.product.currentStock }
        assertThat(stocks).isInOrder()
    }
    
/*
    @Test
    fun `sort by PRICE_HIGH_LOW sorts by sell price descending`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            sortOption = ProductSortOption.PRICE_HIGH_LOW
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        val prices = result.map { it.product.sellPrice }
        assertThat(prices).isInOrder(Comparator.reverseOrder())
    }
*/

    // ==================== COMBINED FILTERS ====================
    
    @Test
    fun `multiple filters are applied together`() {
        // Arrange
        val params = FilterProductsUseCase.FilterParams(
            products = sampleProducts,
            searchQuery = "a", // Broad search
            stockFilter = StockFilter.IN_STOCK,
            sortOption = ProductSortOption.NAME_ASC
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        // All results should match search, have stock, and be sorted
        assertThat(result.all { 
            it.product.name.lowercase().contains("a") ||
            it.product.sku.lowercase().contains("a")
        }).isTrue()
        
        assertThat(result.all { 
            it.product.currentStock > it.product.lowStockThreshold 
        }).isTrue()
    }
}
