package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.data.local.entity.Supplier
import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class CreateSupplierUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) : SuspendUseCase<CreateSupplierUseCase.Params, Result<Long>> {
    
    override suspend fun invoke(params: Params): Result<Long> {
        return try {
            val supplier = Supplier(
                name = params.name.trim(),
                contactPerson = params.contactPerson?.trim()?.takeIf { it.isNotBlank() },
                phone = params.phone?.trim()?.takeIf { it.isNotBlank() },
                email = params.email?.trim()?.lowercase()?.takeIf { it.isNotBlank() },
                address = params.address?.trim()?.takeIf { it.isNotBlank() }
            )
            
            supplierRepository.insertSupplier(supplier)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    data class Params(
        val name: String,
        val contactPerson: String?,
        val phone: String?,
        val email: String?,
        val address: String?
    )
}
