package com.uansari.stockwise.domain.usecase.stock

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.repository.StockRepository
import com.uansari.stockwise.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockMovementsForProductUseCase @Inject constructor(
    private val stockRepository: StockRepository
) : FlowUseCase<Long, List<StockMovement>> {

    override fun invoke(params: Long): Flow<List<StockMovement>> {
        return stockRepository.getMovementsForProduct(params)
    }
}
