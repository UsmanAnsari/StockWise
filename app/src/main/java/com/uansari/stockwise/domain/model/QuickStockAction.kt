package com.uansari.stockwise.domain.model

enum class QuickStockAction {
    ADD_STOCK, REMOVE_STOCK, ADJUST_STOCK;

    val title: String
        get() = when (this) {
            ADD_STOCK -> "Add Stock"
            REMOVE_STOCK -> "Remove Stock"
            ADJUST_STOCK -> "Adjust Stock"
        }

    val quantityLabel: String
        get() = when (this) {
            ADD_STOCK -> "Quantity to add"
            REMOVE_STOCK -> "Quantity to remove"
            ADJUST_STOCK -> "New stock level"
        }
}