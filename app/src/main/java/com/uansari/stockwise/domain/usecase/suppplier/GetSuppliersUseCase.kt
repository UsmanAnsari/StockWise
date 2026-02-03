package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSuppliersUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) : NoParamFlowUseCase<List<Supplier>> {

    override fun invoke(): Flow<List<Supplier>> {
        return supplierRepository.getAllSuppliers()
    }
}