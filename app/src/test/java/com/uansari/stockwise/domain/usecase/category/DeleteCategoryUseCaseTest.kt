package com.uansari.stockwise.domain.usecase.category

import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.util.FakeCategoryRepository
import com.uansari.stockwise.util.FakeProductRepository
import com.uansari.stockwise.util.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {
    
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeProductRepository: FakeProductRepository
    private lateinit var useCase: DeleteCategoryUseCase
    
    @Before
    fun setUp() {
        fakeCategoryRepository = FakeCategoryRepository()
        fakeProductRepository = FakeProductRepository()
        useCase = DeleteCategoryUseCase(fakeCategoryRepository, fakeProductRepository)
    }
    
    @Test
    fun `delete category succeeds when no products assigned`() = runTest {
        // Arrange
        val category = TestFixtures.createCategory(id = 1L)
        fakeCategoryRepository.setCategories(listOf(category))
        fakeProductRepository.setProducts(emptyList()) // No products
        
        // Act
        val result = useCase(1L)
        
        // Assert
        assertThat(result.isSuccess).isTrue()
    }
    
    @Test
    fun `delete category fails when products are assigned`() = runTest {
        // Arrange
        val category = TestFixtures.createCategory(id = 1L)
        val product = TestFixtures.createProduct(categoryId = 1L)
        
        fakeCategoryRepository.setCategories(listOf(category))
        fakeProductRepository.setProducts(listOf(product))
        
        // Act
        val result = useCase(1L)
        
        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(CategoryHasProductsException::class.java)
    }
    
    @Test
    fun `delete category error message includes product count`() = runTest {
        // Arrange
        val category = TestFixtures.createCategory(id = 1L)
        val products = listOf(
            TestFixtures.createProduct(id = 1L, categoryId = 1L),
            TestFixtures.createProduct(id = 2L, categoryId = 1L),
            TestFixtures.createProduct(id = 3L, categoryId = 1L)
        )
        
        fakeCategoryRepository.setCategories(listOf(category))
        fakeProductRepository.setProducts(products)
        
        // Act
        val result = useCase(1L)
        
        // Assert
        assertThat(result.exceptionOrNull()?.message).contains("3 product")
    }
    
    @Test
    fun `delete category only counts active products`() = runTest {
        // Arrange
        val category = TestFixtures.createCategory(id = 1L)
        val products = listOf(
            TestFixtures.createProduct(id = 1L, categoryId = 1L, isActive = false), // Inactive
            TestFixtures.createProduct(id = 2L, categoryId = 1L, isActive = false)  // Inactive
        )
        
        fakeCategoryRepository.setCategories(listOf(category))
        fakeProductRepository.setProducts(products)
        
        // Act
        val result = useCase(1L)
        
        // Assert
        assertThat(result.isSuccess).isTrue() // Should succeed because inactive products don't count
    }
    
    @Test
    fun `delete category returns failure when repository fails`() = runTest {
        // Arrange
        fakeCategoryRepository.shouldReturnError = true
        fakeProductRepository.setProducts(emptyList())
        
        // Act
        val result = useCase(1L)
        
        // Assert
        assertThat(result.isFailure).isTrue()
    }
}
