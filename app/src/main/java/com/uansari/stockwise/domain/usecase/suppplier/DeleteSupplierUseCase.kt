package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class DeleteSupplierUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) : SuspendUseCase<Long, Result<Unit>> {
    
    override suspend fun invoke(params: Long): Result<Unit> {
        return try {
            // Check if supplier has products
            val productCount = productRepository.getProductCountBySupplier(params)
            
            if (productCount > 0) {
                return Result.failure(
                    SupplierHasProductsException(
                        "Cannot delete supplier with $productCount product(s). " +
                        "Please reassign the products to another supplier first."
                    )
                )
            }
            
            supplierRepository.deleteSupplier(params)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Exception thrown when attempting to delete a supplier that has products.
 */
class SupplierHasProductsException(message: String) : Exception(message)
