package com.uansari.stockwise.domain.usecase.dashboard

import com.uansari.stockwise.data.local.entity.relations.InventoryStats
import com.uansari.stockwise.domain.repository.ProductRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInventoryStatsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : NoParamFlowUseCase<InventoryStats> {
    
    override fun invoke(): Flow<InventoryStats> {
        return productRepository.getInventoryStats()
    }
}