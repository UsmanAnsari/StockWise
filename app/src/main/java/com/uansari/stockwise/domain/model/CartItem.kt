package com.uansari.stockwise.domain.model

data class CartItem(
    val productId: Long,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,   // Selling price
    val unitCost: Double,    // Cost price (for profit calculation)
    val availableStock: Int  // For validation
) {
    val subtotal: Double
        get() = quantity * unitPrice

    val profit: Double
        get() = (unitPrice - unitCost) * quantity

    val isValid: Boolean
        get() = quantity in 1..availableStock
}
