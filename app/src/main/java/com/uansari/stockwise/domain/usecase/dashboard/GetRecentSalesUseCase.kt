package com.uansari.stockwise.domain.usecase.dashboard

import com.uansari.stockwise.data.local.entity.Sale
import com.uansari.stockwise.domain.repository.SaleRepository
import com.uansari.stockwise.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRecentSalesUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) : FlowUseCase<Int, List<Sale>> {

    override fun invoke(params: Int): Flow<List<Sale>> {
        return saleRepository.getAllSales().map { sales -> sales.take(params) }
    }

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
