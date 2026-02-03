package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.domain.model.ProductUnitType
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : SuspendUseCase<UpdateProductUseCase.Params, Result<Unit>> {

    override suspend fun invoke(params: Params): Result<Unit> {
        return try {
            // Get existing product
            val existingProduct =
                productRepository.getProductByIdOneShot(params.productId) ?: return Result.failure(
                    Exception("Product not found")
                )

            // Update product (preserve currentStock and createdAt)
            val updatedProduct = existingProduct.copy(
                name = params.name.trim(),
                sku = params.sku.trim().uppercase(),
                description = params.description?.trim()?.takeIf { it.isNotBlank() },
                categoryId = params.categoryId,
                supplierId = params.supplierId,
                buyPrice = params.buyPrice,
                sellPrice = params.sellPrice,
                lowStockThreshold = params.lowStockThreshold,
                unit = params.unit,
                updatedAt = System.currentTimeMillis()
            )

            productRepository.updateProduct(updatedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class Params(
        val productId: Long,
        val name: String,
        val sku: String,
        val description: String?,
        val categoryId: Long,
        val supplierId: Long?,
        val buyPrice: Double,
        val sellPrice: Double,
        val lowStockThreshold: Int,
        val unit: ProductUnitType = ProductUnitType.PCS
    )
}
