package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.data.local.entity.Category
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : NoParamFlowUseCase<List<Category>> {

    override fun invoke(): Flow<List<Category>> {
        return categoryRepository.getAllCategories()
    }
}