package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.usecase.SuspendUseCase
import javax.inject.Inject

class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : SuspendUseCase<Long, Category?> {
    
    override suspend fun invoke(params: Long): Category? {
        return categoryRepository.getCategoryByIdOneShot(params)
    }
}
