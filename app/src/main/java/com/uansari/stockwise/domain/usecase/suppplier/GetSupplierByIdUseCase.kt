package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class GetSupplierByIdUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) : SuspendUseCase<Long, Supplier?> {
    
    override suspend fun invoke(params: Long): Supplier? {
        return supplierRepository.getSupplierByIdOneShot(params)
    }
}