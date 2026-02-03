package com.uansari.stockwise.domain.usecase.category

import com.uansari.stockwise.data.local.entity.relations.CategoryWithProductCount
import com.uansari.stockwise.domain.repository.CategoryRepository
import com.uansari.stockwise.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesWithProductCountUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : NoParamFlowUseCase<List<CategoryWithProductCount>> {
    
    override fun invoke(): Flow<List<CategoryWithProductCount>> {
        return categoryRepository.getCategoriesWithProductCount()
    }
}
