package com.uansari.stockwise.domain.usecase.dashboard

import com.uansari.stockwise.data.local.entity.relations.DailySalesSummary
import com.uansari.stockwise.domain.repository.SaleRepository
import com.uansari.stockwise.domain.usecase.FlowUseCase
import com.uansari.stockwise.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailySalesSummaryUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) : FlowUseCase<Long, DailySalesSummary> {
    override fun invoke(params: Long): Flow<DailySalesSummary> {
        return saleRepository.getDailySummary(params)
    }

    fun forToday(): Flow<DailySalesSummary> {
        return invoke(DateTimeUtils.getStartOfToday())
    }
}