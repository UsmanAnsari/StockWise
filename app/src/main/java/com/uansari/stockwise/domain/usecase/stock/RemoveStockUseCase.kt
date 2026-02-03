package com.uansari.stockwise.domain.usecase.stock

import com.uansari.stockwise.data.local.entity.StockMovement
import com.uansari.stockwise.domain.repository.StockRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class RemoveStockUseCase @Inject constructor(
    private val stockRepository: StockRepository
) : SuspendUseCase<RemoveStockUseCase.Params, Result<StockMovement>> {

    override suspend fun invoke(params: Params): Result<StockMovement> {
        return stockRepository.removeStock(
            productId = params.productId, quantity = params.quantity, notes = params.notes
        )
    }

    data class Params(
        val productId: Long, val quantity: Int, val notes: String? = null
    )
}