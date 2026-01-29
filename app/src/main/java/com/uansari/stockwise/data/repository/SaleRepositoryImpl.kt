package com.uansari.stockwise.data.repository

import androidx.room.withTransaction
import com.uansari.stockwise.data.local.dao.ProductDao
import com.uansari.stockwise.data.local.dao.SaleDao
import com.uansari.stockwise.data.local.dao.SaleItemDao
import com.uansari.stockwise.data.local.dao.StockMovementDao
import com.uansari.stockwise.data.local.database.StockWiseDatabase
import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.data.local.entity.SaleItem
import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.data.local.entity.relations.DailySalesSummary
import com.uansari.stockwise.data.local.entity.relations.SaleWithItems
import com.uansari.stockwise.data.local.entity.relations.SalesSummary
import com.uansari.stockwise.domain.model.CartItem
import com.uansari.stockwise.domain.model.MovementType
import com.uansari.stockwise.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepositoryImpl @Inject constructor(
    private val database: StockWiseDatabase,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) : SaleRepository {


    override fun getAllSales(): Flow<List<Sale>> {
        return saleDao.getAllSales()
    }

    override fun getSalesWithItems(): Flow<List<SaleWithItems>> {
        return saleDao.getSalesWithItems()
    }

    override fun getSaleWithItems(saleId: Long): Flow<SaleWithItems?> {
        return saleDao.getSaleWithItems(saleId)
    }

    override fun getRecentSalesWithItems(limit: Int): Flow<List<SaleWithItems>> {
        return saleDao.getRecentSalesWithItems(limit)
    }

    override fun getSalesInRange(startTime: Long, endTime: Long): Flow<List<Sale>> {
        return saleDao.getSalesInRange(startTime, endTime)
    }

    override fun getTodaySales(startOfDay: Long): Flow<List<Sale>> {
        return saleDao.getTodaySales(startOfDay)
    }


    override fun getDailySummary(startOfDay: Long): Flow<DailySalesSummary> {
        return saleDao.getDailySummary(startOfDay)
    }

    override fun getSalesSummary(startTime: Long, endTime: Long): Flow<SalesSummary> {
        return saleDao.getSalesSummary(startTime, endTime)
    }

    /**
     * Complete a sale with all cart items.
     * This is an ATOMIC operation that:
     * 1. Validates all cart items (sufficient stock)
     * 2. Creates Sale record
     * 3. Creates SaleItem for each cart item
     * 4. Creates StockMovement for each product
     * 5. Updates currentStock on each Product
     *
     * If ANY step fails, ALL changes are rolled back.
     */
    override suspend fun completeSale(
        cartItems: List<CartItem>, notes: String?
    ): Result<Sale> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        return try {
            val sale = database.withTransaction {
                // Step 1: Validate all items have sufficient stock
                for (item in cartItems) {
                    val product = productDao.getProductByIdOneShot(item.productId)
                        ?: throw IllegalArgumentException("Product not found: ${item.productName}")

                    if (product.currentStock < item.quantity) {
                        throw IllegalStateException(
                            "Insufficient stock for ${item.productName}. " + "Available: ${product.currentStock}, Requested: ${item.quantity}"
                        )
                    }
                }

                // Step 2: Calculate totals
                val totalAmount = cartItems.sumOf { it.subtotal }
                val totalCost = cartItems.sumOf { it.unitCost * it.quantity }
                val totalProfit = totalAmount - totalCost
                val itemCount = cartItems.size

                // Step 3: Generate sale number
                val saleNumber = generateSaleNumberInternal()

                // Step 4: Create Sale record
                val sale = Sale(
                    saleNumber = saleNumber,
                    totalAmount = totalAmount,
                    totalCost = totalCost,
                    totalProfit = totalProfit,
                    itemCount = itemCount,
                    notes = notes
                )

                val saleId = saleDao.insert(sale)

                // Step 5: Create SaleItems and StockMovements for each product
                for (item in cartItems) {
                    // Create SaleItem
                    val saleItem = SaleItem(
                        saleId = saleId,
                        productId = item.productId,
                        productName = item.productName,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        unitCost = item.unitCost,
                        subtotal = item.subtotal,
                        profit = item.profit
                    )
                    saleItemDao.insert(saleItem)

                    // Get current stock for movement record
                    val product = productDao.getProductByIdOneShot(item.productId)!!
                    val previousStock = product.currentStock
                    val newStock = previousStock - item.quantity

                    // Create StockMovement
                    val movement = StockMovement(
                        productId = item.productId,
                        type = MovementType.SALE,
                        quantity = -item.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        unitCost = item.unitCost,
                        reference = saleNumber,
                        notes = null
                    )
                    stockMovementDao.insert(movement)

                    // Update product stock
                    productDao.updateStock(item.productId, newStock)
                }

                // Return the created sale
                sale.copy(id = saleId)
            }

            Result.success(sale)
        } catch (e: IllegalStateException) {
            // Stock validation error
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            // Invalid input error
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to complete sale: ${e.message}"))
        }
    }

    /**
     * Delete a sale.
     * NOTE: This does NOT restore stock. In a real app, you might want to
     * either restore stock or prevent deletion entirely.
     * For MVP, we'll just delete without stock restoration.
     */
    override suspend fun deleteSale(saleId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                // SaleItems are deleted automatically via CASCADE
                // StockMovements remain for audit trail
                saleDao.deleteById(saleId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== UTILITIES ====================

    override suspend fun generateSaleNumber(): String {
        return generateSaleNumberInternal()
    }

    /**
     * Generate unique sale number in format: SALE-YYYYMMDD-XXX
     */
    private suspend fun generateSaleNumberInternal(): String {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayCount = saleDao.getTodaySaleCount(startOfDay)

        val dateStr = today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
        val sequence = (todayCount + 1).toString().padStart(3, '0')

        return "SALE-$dateStr-$sequence"
    }
}