package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.domain.repository.ProductRepository
import javax.inject.Inject

class ValidateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    
    suspend operator fun invoke(params: ValidationParams): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Name validation
        when {
            params.name.isBlank() -> {
                errors[FIELD_NAME] = "Product name is required"
            }
            params.name.length < 2 -> {
                errors[FIELD_NAME] = "Name must be at least 2 characters"
            }
            params.name.length > 100 -> {
                errors[FIELD_NAME] = "Name must be less than 100 characters"
            }
        }
        
        // SKU validation
        when {
            params.sku.isBlank() -> {
                errors[FIELD_SKU] = "SKU is required"
            }
            params.sku.length < 3 -> {
                errors[FIELD_SKU] = "SKU must be at least 3 characters"
            }
            params.sku.length > 50 -> {
                errors[FIELD_SKU] = "SKU must be less than 50 characters"
            }
            !params.sku.matches(SKU_PATTERN) -> {
                errors[FIELD_SKU] = "SKU can only contain letters, numbers, and hyphens"
            }
            else -> {
                // Check uniqueness (excluding current product in edit mode)
                val existingProduct = productRepository.getProductBySku(params.sku)
                if (existingProduct != null && existingProduct.id != params.excludeProductId) {
                    errors[FIELD_SKU] = "SKU already exists"
                }
            }
        }
        
        // Category validation
        if (params.categoryId == null) {
            errors[FIELD_CATEGORY] = "Please select a category"
        }
        
        // Buy price validation
        when {
            params.buyPrice == null -> {
                errors[FIELD_BUY_PRICE] = "Cost price is required"
            }
            params.buyPrice < 0 -> {
                errors[FIELD_BUY_PRICE] = "Cost price cannot be negative"
            }
        }
        
        // Sell price validation
        when {
            params.sellPrice == null -> {
                errors[FIELD_SELL_PRICE] = "Sell price is required"
            }
            params.sellPrice < 0 -> {
                errors[FIELD_SELL_PRICE] = "Sell price cannot be negative"
            }
            params.buyPrice != null && params.sellPrice < params.buyPrice -> {
                errors[FIELD_SELL_PRICE] = "Sell price should be higher than cost price"
            }
        }
        
        // Initial stock validation (only for new products)
        if (!params.isEditMode) {
            when {
                params.initialStock == null -> {
                    errors[FIELD_INITIAL_STOCK] = "Initial stock is required"
                }
                params.initialStock < 0 -> {
                    errors[FIELD_INITIAL_STOCK] = "Initial stock cannot be negative"
                }
            }
        }
        
        // Low stock threshold validation
        when {
            params.lowStockThreshold == null -> {
                errors[FIELD_LOW_STOCK_THRESHOLD] = "Low stock threshold is required"
            }
            params.lowStockThreshold < 0 -> {
                errors[FIELD_LOW_STOCK_THRESHOLD] = "Threshold cannot be negative"
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    data class ValidationParams(
        val name: String,
        val sku: String,
        val categoryId: Long?,
        val buyPrice: Double?,
        val sellPrice: Double?,
        val initialStock: Int?,
        val lowStockThreshold: Int?,
        val isEditMode: Boolean = false,
        val excludeProductId: Long? = null
    )
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String>
    )
    
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_SKU = "sku"
        const val FIELD_CATEGORY = "category"
        const val FIELD_BUY_PRICE = "buyPrice"
        const val FIELD_SELL_PRICE = "sellPrice"
        const val FIELD_INITIAL_STOCK = "initialStock"
        const val FIELD_LOW_STOCK_THRESHOLD = "lowStockThreshold"
        
        private val SKU_PATTERN = Regex("^[a-zA-Z0-9-]+$")
    }
}