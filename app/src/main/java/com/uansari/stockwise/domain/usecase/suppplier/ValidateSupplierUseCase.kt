package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.domain.repository.SupplierRepository
import javax.inject.Inject

class ValidateSupplierUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) {
    
    suspend operator fun invoke(params: ValidationParams): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Name validation
        when {
            params.name.isBlank() -> {
                errors[FIELD_NAME] = "Supplier name is required"
            }
            params.name.length < 2 -> {
                errors[FIELD_NAME] = "Name must be at least 2 characters"
            }
            params.name.length > 100 -> {
                errors[FIELD_NAME] = "Name must be less than 100 characters"
            }
            else -> {
                // Check uniqueness
                val existingSupplier = supplierRepository.getSupplierByName(params.name.trim())
                if (existingSupplier != null && existingSupplier.id != params.excludeSupplierId) {
                    errors[FIELD_NAME] = "Supplier name already exists"
                }
            }
        }
        
        // Email validation (optional but must be valid if provided)
        if (!params.email.isNullOrBlank()) {
            if (!params.email.matches(EMAIL_PATTERN)) {
                errors[FIELD_EMAIL] = "Invalid email format"
            }
        }
        
        // Phone validation (optional but basic format check if provided)
        if (!params.phone.isNullOrBlank()) {
            val digitsOnly = params.phone.filter { it.isDigit() }
            if (digitsOnly.length < 7) {
                errors[FIELD_PHONE] = "Phone number seems too short"
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    data class ValidationParams(
        val name: String,
        val email: String?,
        val phone: String?,
        val excludeSupplierId: Long? = null
    )
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String>
    )
    
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_EMAIL = "email"
        const val FIELD_PHONE = "phone"
        
        private val EMAIL_PATTERN = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}
