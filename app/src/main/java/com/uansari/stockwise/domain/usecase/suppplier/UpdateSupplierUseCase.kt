package com.uansari.stockwise.domain.usecase.suppplier

import com.uansari.stockwise.domain.repository.SupplierRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class UpdateSupplierUseCase @Inject constructor(
    private val supplierRepository: SupplierRepository
) : SuspendUseCase<UpdateSupplierUseCase.Params, Result<Unit>> {

    override suspend fun invoke(params: Params): Result<Unit> {
        return try {
            val existingSupplier = supplierRepository.getSupplierByIdOneShot(params.supplierId)
                ?: return Result.failure(Exception("Supplier not found"))

            val updatedSupplier = existingSupplier.copy(
                name = params.name.trim(),
                contactPerson = params.contactPerson?.trim()?.takeIf { it.isNotBlank() },
                phone = params.phone?.trim()?.takeIf { it.isNotBlank() },
                email = params.email?.trim()?.lowercase()?.takeIf { it.isNotBlank() },
                address = params.address?.trim()?.takeIf { it.isNotBlank() },
                updatedAt = System.currentTimeMillis()
            )

            supplierRepository.updateSupplier(updatedSupplier)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class Params(
        val supplierId: Long,
        val name: String,
        val contactPerson: String?,
        val phone: String?,
        val email: String?,
        val address: String?
    )
}
