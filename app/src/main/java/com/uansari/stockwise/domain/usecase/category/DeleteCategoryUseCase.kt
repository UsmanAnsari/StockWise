package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : SuspendUseCase<Long, Result<Unit>> {
    
    override suspend fun invoke(params: Long): Result<Unit> {
        return try {
            // Check if category has products
            val productCount = productRepository.getProductCountByCategory(params)
            
            if (productCount > 0) {
                return Result.failure(
                    CategoryHasProductsException(
                        "Cannot delete category with $productCount product(s). " +
                        "Please reassign or delete the products first."
                    )
                )
            }
            
            categoryRepository.deleteCategory(params)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CategoryHasProductsException(message: String) : Exception(message)

