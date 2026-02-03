package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.data.local.entity.Product
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : SuspendUseCase<Long, Product?> {

    override suspend fun invoke(params: Long): Product? {
        return productRepository.getProductByIdOneShot(params)
    }
}