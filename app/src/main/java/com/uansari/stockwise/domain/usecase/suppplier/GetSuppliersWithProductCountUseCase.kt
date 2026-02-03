package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.data.local.entity.relations.SupplierWithProductCount
import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSuppliersWithProductCountUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) : NoParamFlowUseCase<List<SupplierWithProductCount>> {
    
    override fun invoke(): Flow<List<SupplierWithProductCount>> {
        return supplierRepository.getSuppliersWithProductCount()
    }
}