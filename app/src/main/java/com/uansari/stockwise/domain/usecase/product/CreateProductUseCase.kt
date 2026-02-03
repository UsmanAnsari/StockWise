package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.domain.model.ProductUnitType
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.repository.StockRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class CreateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository, private val stockRepository: StockRepository
) : SuspendUseCase<CreateProductUseCase.Params, Result<Long>> {

    override suspend fun invoke(params: Params): Result<Long> {
        return try {
            // Create product with initial stock set to 0
            // We'll add stock separately via StockRepository
            val product = Product(
                name = params.name.trim(),
                sku = params.sku.trim().uppercase(),
                description = params.description?.trim()?.takeIf { it.isNotBlank() },
                categoryId = params.categoryId,
                supplierId = params.supplierId,
                buyPrice = params.buyPrice,
                sellPrice = params.sellPrice,
                currentStock = 0, // Will be updated by addStock
                lowStockThreshold = params.lowStockThreshold,
                unit = params.unit
            )

            // Insert product
            val productId = productRepository.insertProduct(product).getOrThrow()

            // Add initial stock if > 0
            if (params.initialStock > 0) {
                stockRepository.addStock(
                    productId = productId, quantity = params.initialStock, notes = "Initial stock"
                ).getOrThrow()
            }

            Result.success(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class Params(
        val name: String,
        val sku: String,
        val description: String?,
        val categoryId: Long,
        val supplierId: Long?,
        val buyPrice: Double,
        val sellPrice: Double,
        val initialStock: Int,
        val lowStockThreshold: Int,
        val unit: ProductUnitType = ProductUnitType.PCS
    )
}
