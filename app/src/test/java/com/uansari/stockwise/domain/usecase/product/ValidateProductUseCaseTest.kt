package com.uansari.stockwise.domain.usecase.product

import com.google.common.truth.Truth.assertThat
import com.uansari.stockwise.util.FakeProductRepository
import com.uansari.stockwise.util.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ValidateProductUseCaseTest {

    private lateinit var fakeRepository: FakeProductRepository
    private lateinit var useCase: ValidateProductUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeProductRepository()
        useCase = ValidateProductUseCase(fakeRepository)
    }

    // ==================== NAME VALIDATION ====================

    @Test
    fun `validate returns error when name is blank`() = runTest {
        // Arrange
        val params = createValidParams(name = "")

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsKey(ValidateProductUseCase.FIELD_NAME)
        assertThat(result.errors[ValidateProductUseCase.FIELD_NAME]).contains("required")
    }

    @Test
    fun `validate returns error when name is too short`() = runTest {
        // Arrange
        val params = createValidParams(name = "A")

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_NAME]).contains("at least 2 characters")
    }

    @Test
    fun `validate returns error when name is too long`() = runTest {
        // Arrange
        val longName = "A".repeat(101)
        val params = createValidParams(name = longName)

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_NAME]).contains("less than 100")
    }

    // ==================== SKU VALIDATION ====================

    @Test
    fun `validate returns error when SKU is blank`() = runTest {
        // Arrange
        val params = createValidParams(sku = "")

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsKey(ValidateProductUseCase.FIELD_SKU)
    }

    @Test
    fun `validate returns error when SKU contains invalid characters`() = runTest {
        // Arrange
        val params = createValidParams(sku = "SKU@123!")

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_SKU]).contains("letters, numbers, and hyphens")
    }

    @Test
    fun `validate returns error when SKU already exists`() = runTest {
        // Arrange
        val existingProduct = TestFixtures.createProduct(sku = "EXISTING-SKU")
        fakeRepository.setProducts(listOf(existingProduct))

        val params = createValidParams(sku = "EXISTING-SKU")

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_SKU]).contains("already exists")
    }

    @Test
    fun `validate allows same SKU when editing same product`() = runTest {
        // Arrange
        val existingProduct = TestFixtures.createProduct(id = 1L, sku = "EXISTING-SKU")
        fakeRepository.setProducts(listOf(existingProduct))

        val params = createValidParams(
            sku = "EXISTING-SKU", isEditMode = true, excludeProductId = 1L // Same product ID
        )

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.errors).doesNotContainKey(ValidateProductUseCase.FIELD_SKU)
    }

    // ==================== CATEGORY VALIDATION ====================

    @Test
    fun `validate returns error when category is null`() = runTest {
        // Arrange
        val params = createValidParams(categoryId = null)

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsKey(ValidateProductUseCase.FIELD_CATEGORY)
    }

    // ==================== PRICE VALIDATION ====================

    @Test
    fun `validate returns error when buy price is null`() = runTest {
        // Arrange
        val params = createValidParams(buyPrice = null)

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsKey(ValidateProductUseCase.FIELD_BUY_PRICE)
    }

    @Test
    fun `validate returns error when buy price is negative`() = runTest {
        // Arrange
        val params = createValidParams(buyPrice = -10.0)

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_BUY_PRICE]).contains("negative")
    }

    @Test
    fun `validate returns error when sell price is less than buy price`() = runTest {
        // Arrange
        val params = createValidParams(buyPrice = 100.0, sellPrice = 50.0)

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors[ValidateProductUseCase.FIELD_SELL_PRICE]).contains("higher than cost")
    }

    // ==================== STOCK VALIDATION ====================

    @Test
    fun `validate returns error when initial stock is null for new product`() = runTest {
        // Arrange
        val params = createValidParams(
            initialStock = null, isEditMode = false
        )

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsKey(ValidateProductUseCase.FIELD_INITIAL_STOCK)
    }

    @Test
    fun `validate does not require initial stock in edit mode`() = runTest {
        // Arrange
        val params = createValidParams(
            initialStock = null, isEditMode = true
        )

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.errors).doesNotContainKey(ValidateProductUseCase.FIELD_INITIAL_STOCK)
    }

    // ==================== SUCCESS CASE ====================

    @Test
    fun `validate returns valid result for correct data`() = runTest {
        // Arrange
        val params = createValidParams()

        // Act
        val result = useCase(params)

        // Assert
        assertThat(result.isValid).isTrue()
        assertThat(result.errors).isEmpty()
    }

    // ==================== HELPER ====================

    private fun createValidParams(
        name: String = "Valid Product",
        sku: String = "VALID-SKU",
        categoryId: Long? = 1L,
        buyPrice: Double? = 10.0,
        sellPrice: Double? = 20.0,
        initialStock: Int? = 100,
        lowStockThreshold: Int? = 10,
        isEditMode: Boolean = false,
        excludeProductId: Long? = null
    ) = ValidateProductUseCase.ValidationParams(
        name = name,
        sku = sku,
        categoryId = categoryId,
        buyPrice = buyPrice,
        sellPrice = sellPrice,
        initialStock = initialStock,
        lowStockThreshold = lowStockThreshold,
        isEditMode = isEditMode,
        excludeProductId = excludeProductId
    )
}
