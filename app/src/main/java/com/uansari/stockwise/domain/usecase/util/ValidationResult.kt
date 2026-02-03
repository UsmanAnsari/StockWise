package com.uansari.stockwise.domain.usecase.util

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(errors: Map<String, String>) = ValidationResult(false, errors)
    }
}
