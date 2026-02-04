package com.uansari.stockwise.domain.usecase.stock

import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.util.TestFixtures
import org.junit.Before
import org.junit.Test

class FilterStockMovementsUseCaseTest {
    
    private lateinit var useCase: FilterStockMovementsUseCase
    private val sampleMovements = TestFixtures.sampleStockMovements
    
    @Before
    fun setUp() {
        useCase = FilterStockMovementsUseCase()
    }
    
    @Test
    fun `filter by ALL returns all movements`() {
        // Arrange
        val params = FilterStockMovementsUseCase.FilterParams(
            movements = sampleMovements,
            typeFilter = MovementTypeFilter.ALL
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result).hasSize(sampleMovements.size)
    }
    
    @Test
    fun `filter by STOCK_IN returns only IN movements`() {
        // Arrange
        val params = FilterStockMovementsUseCase.FilterParams(
            movements = sampleMovements,
            typeFilter = MovementTypeFilter.STOCK_IN
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { it.type == MovementType.IN }).isTrue()
    }
    
    @Test
    fun `filter by SALE returns only SALE movements`() {
        // Arrange
        val params = FilterStockMovementsUseCase.FilterParams(
            movements = sampleMovements,
            typeFilter = MovementTypeFilter.SALE
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { it.type == MovementType.SALE }).isTrue()
    }
    
    @Test
    fun `filter by date range returns movements within range`() {
        // Arrange
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600000
        
        val params = FilterStockMovementsUseCase.FilterParams(
            movements = sampleMovements,
            startDate = oneHourAgo,
            endDate = now
        )
        
        // Act
        val result = useCase(params)
        
        // Assert
        assertThat(result.all { it.createdAt in oneHourAgo..now }).isTrue()
    }
}
