package com.uansari.stockwise.domain.usecase.dashboard

import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : NoParamFlowUseCase<List<ProductWithDetails>> {
    
    override fun invoke(): Flow<List<ProductWithDetails>> {
        return productRepository.getLowStockProducts()
    }
}
