package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : SuspendUseCase<CreateCategoryUseCase.Params, Result<Long>> {

    override suspend fun invoke(params: Params): Result<Long> {
        return try {
            val category = Category(
                name = params.name.trim(),
                description = params.description?.trim()?.takeIf { it.isNotBlank() },
                color = params.color
            )

            categoryRepository.insertCategory(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class Params(
        val name: String, val description: String?, val color: String
    )
}
