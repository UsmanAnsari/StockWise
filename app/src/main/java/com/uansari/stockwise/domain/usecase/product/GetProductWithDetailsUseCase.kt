package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductWithDetailsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : FlowUseCase<Long, ProductWithDetails?> {
    
    override fun invoke(params: Long): Flow<ProductWithDetails?> {
        return productRepository.getProductWithDetails(params)
    }
}