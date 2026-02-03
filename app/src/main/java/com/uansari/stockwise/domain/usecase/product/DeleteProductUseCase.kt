package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : SuspendUseCase<Long, Result<Unit>> {
    
    override suspend fun invoke(params: Long): Result<Unit> {
        return productRepository.softDeleteProduct(params)
    }
}
