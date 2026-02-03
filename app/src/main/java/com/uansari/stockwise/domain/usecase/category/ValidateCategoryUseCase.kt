package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.domain.repository.CategoryRepository
import javax.inject.Inject

class ValidateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(params: ValidationParams): ValidationResult {
        val errors = mutableMapOf<String, String>()

        // Name validation
        when {
            params.name.isBlank() -> {
                errors[FIELD_NAME] = "Category name is required"
            }

            params.name.length < 2 -> {
                errors[FIELD_NAME] = "Name must be at least 2 characters"
            }

            params.name.length > 50 -> {
                errors[FIELD_NAME] = "Name must be less than 50 characters"
            }

            else -> {
                // Check uniqueness
                val existingCategory = categoryRepository.getCategoryByName(params.name.trim())
                if (existingCategory != null && existingCategory.id != params.excludeCategoryId) {
                    errors[FIELD_NAME] = "Category name already exists"
                }
            }
        }

        // Color validation (optional but if provided, must be valid hex)
        if (!params.colorHex.isNullOrBlank()) {
            if (!params.colorHex.matches(HEX_COLOR_PATTERN)) {
                errors[FIELD_COLOR] = "Invalid color format"
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(), errors = errors
        )
    }

    data class ValidationParams(
        val name: String, val colorHex: String?, val excludeCategoryId: Long? = null
    )

    data class ValidationResult(
        val isValid: Boolean, val errors: Map<String, String>
    )

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_COLOR = "color"

        private val HEX_COLOR_PATTERN = Regex("^#[0-9A-Fa-f]{6}$")
    }
}
