package com.uansari.stockwise.util

import com.uansari.stockwise.domain.model.ProductUnitType
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    // Default to GBP, can be made configurable later
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK).apply {
        currency = Currency.getInstance("GBP")
    }

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.UK).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    /**
     * Format as currency (e.g., "£12.99").
     */
    fun Double.formatAsCurrency(): String {
        return currencyFormat.format(this)
    }

    /**
     * Format as number with 2 decimal places (e.g., "12.99").
     */
    fun Double.formatAsDecimal(): String {
        return numberFormat.format(this)
    }

    /**
     * Format as percentage (e.g., "45.5%").
     */
    fun Double.formatAsPercentage(): String {
        return "${numberFormat.format(this)}%"
    }

    /**
     * Format quantity with unit (e.g., "45 pcs").
     */
    fun Int.formatWithUnit(unit: ProductUnitType): String {
        return "$this ${unit.name}"
    }
}
