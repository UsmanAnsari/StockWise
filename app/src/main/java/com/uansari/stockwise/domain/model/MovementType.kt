package com.uansari.stockwise.domain.model

/**
 * Represents the type of stock movement.
 *
 * IN         - Stock received (purchase, return from customer)
 *
 * OUT        - Stock removed (damage, loss, theft, expired)
 *
 * ADJUSTMENT - Manual correction (inventory count mismatch)
 *
 * SALE       - Stock sold to customer (auto-created during sale)
 */
enum class MovementType {
    IN,
    OUT,
    ADJUSTMENT,
    SALE
}