package com.uansari.stockwise.domain.usecase.stock

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.repository.StockRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class AdjustStockUseCase @Inject constructor(
    private val stockRepository: StockRepository
) : SuspendUseCase<AdjustStockUseCase.Params, Result<StockMovement>> {

    override suspend fun invoke(params: Params): Result<StockMovement> {
        return stockRepository.adjustStock(
            productId = params.productId, newStockLevel = params.newStockLevel, notes = params.notes
        )
    }

    data class Params(
        val productId: Long, val newStockLevel: Int, val notes: String? = null
    )
}
