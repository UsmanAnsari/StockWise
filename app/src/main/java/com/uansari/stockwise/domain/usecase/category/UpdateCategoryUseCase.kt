package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : SuspendUseCase<UpdateCategoryUseCase.Params, Result<Unit>> {

    override suspend fun invoke(params: Params): Result<Unit> {
        return try {
            val existingCategory = categoryRepository.getCategoryByIdOneShot(params.categoryId)
                ?: return Result.failure(Exception("Category not found"))

            val updatedCategory = existingCategory.copy(
                name = params.name.trim(),
                description = params.description?.trim()?.takeIf { it.isNotBlank() },
                color = params.color,
                updatedAt = System.currentTimeMillis()
            )

            categoryRepository.updateCategory(updatedCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class Params(
        val categoryId: Long,
        val name: String,
        val description: String?,
        val color: String
    )
}
