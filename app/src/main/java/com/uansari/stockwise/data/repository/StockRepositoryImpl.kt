package com.uansari.stockwise.data.repository

import androidx.room.withTransaction
import com.uansari.stockwise.data.local.dao.ProductDao
import com.uansari.stockwise.data.local.dao.StockMovementDao
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val database: StockWiseDatabase,
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao
) : StockRepository {


    override fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsForProduct(productId)
    }

    override fun getRecentMovements(limit: Int): Flow<List<StockMovement>> {
        return stockMovementDao.getRecentMovements(limit)
    }

    override fun getMovementsByType(type: MovementType): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsByType(type)
    }

    override fun getMovementsInRange(startTime: Long, endTime: Long): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsInRange(startTime, endTime)
    }


    /**
     * Add stock to a product (Stock IN).
     * Atomically:
     * 1. Creates StockMovement record
     * 2. Updates Product.currentStock
     */
    override suspend fun addStock(
        productId: Long,
        quantity: Int,
        unitCost: Double?,
        reference: String?,
        notes: String?
    ): Result<StockMovement> {
        require(quantity > 0) { "Quantity must be positive for stock in" }

        return try {
            val movement = database.withTransaction {
                // Get current product
                val product = productDao.getProductByIdOneShot(productId)
                    ?: throw IllegalArgumentException("Product not found")

                val previousStock = product.currentStock
                val newStock = previousStock + quantity

                // Create movement record
                val movement = StockMovement(
                    productId = productId,
                    type = MovementType.IN,
                    quantity = quantity,  // Positive for IN
                    previousStock = previousStock,
                    newStock = newStock,
                    unitCost = unitCost ?: product.buyPrice,
                    reference = reference,
                    notes = notes
                )

                val movementId = stockMovementDao.insert(movement)

                // Update product stock
                productDao.updateStock(productId, newStock)

                movement.copy(id = movementId)
            }
            Result.success(movement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove stock from a product (Stock OUT - not a sale).
     * Used for damage, loss, theft, expired goods, etc.
     */
    override suspend fun removeStock(
        productId: Long,
        quantity: Int,
        reference: String?,
        notes: String?
    ): Result<StockMovement> {
        require(quantity > 0) { "Quantity must be positive" }

        return try {
            val movement = database.withTransaction {
                val product = productDao.getProductByIdOneShot(productId)
                    ?: throw IllegalArgumentException("Product not found")

                if (product.currentStock < quantity) {
                    throw IllegalStateException(
                        "Insufficient stock. Available: ${product.currentStock}, Requested: $quantity"
                    )
                }

                val previousStock = product.currentStock
                val newStock = previousStock - quantity

                val movement = StockMovement(
                    productId = productId,
                    type = MovementType.OUT,
                    quantity = -quantity,  // Negative for OUT
                    previousStock = previousStock,
                    newStock = newStock,
                    unitCost = product.buyPrice,
                    reference = reference,
                    notes = notes
                )

                val movementId = stockMovementDao.insert(movement)
                productDao.updateStock(productId, newStock)

                movement.copy(id = movementId)
            }
            Result.success(movement)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Adjust stock to a specific level.
     * Used when physical count doesn't match system count.
     */
    override suspend fun adjustStock(
        productId: Long,
        newStockLevel: Int,
        notes: String?
    ): Result<StockMovement> {
        require(newStockLevel >= 0) { "Stock level cannot be negative" }

        return try {
            val movement = database.withTransaction {
                val product = productDao.getProductByIdOneShot(productId)
                    ?: throw IllegalArgumentException("Product not found")

                val previousStock = product.currentStock
                val difference = newStockLevel - previousStock

                if (difference == 0) {
                    throw IllegalArgumentException("New stock level is same as current")
                }

                val movement = StockMovement(
                    productId = productId,
                    type = MovementType.ADJUSTMENT,
                    quantity = difference,  // Can be positive or negative
                    previousStock = previousStock,
                    newStock = newStockLevel,
                    unitCost = product.buyPrice,
                    reference = "Stock Adjustment",
                    notes = notes ?: "Adjusted from $previousStock to $newStockLevel"
                )

                val movementId = stockMovementDao.insert(movement)
                productDao.updateStock(productId, newStockLevel)

                movement.copy(id = movementId)
            }
            Result.success(movement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record stock movement for a sale.
     * Called internally by SaleRepository during sale completion.
     * Not wrapped in Result because it's part of a larger transaction.
     */
    override suspend fun recordSaleMovement(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        reference: String
    ): StockMovement {
        val product = productDao.getProductByIdOneShot(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")

        val previousStock = product.currentStock
        val newStock = previousStock - quantity

        if (newStock < 0) {
            throw IllegalStateException(
                "Insufficient stock for ${product.name}. Available: $previousStock, Requested: $quantity"
            )
        }

        val movement = StockMovement(
            productId = productId,
            type = MovementType.SALE,
            quantity = -quantity,  // Negative for sale
            previousStock = previousStock,
            newStock = newStock,
            unitCost = unitCost,
            reference = reference,
            notes = null
        )

        val movementId = stockMovementDao.insert(movement)
        productDao.updateStock(productId, newStock)

        return movement.copy(id = movementId)
    }
}