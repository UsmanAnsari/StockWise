package com.uansari.stockwise.domain.usecase.product

import com.uansari.stockwise.data.local.entity.relations.ProductWithDetails
import com.uansari.stockwise.util.ProductSortOption
import com.uansari.stockwise.util.StockFilter
import javax.inject.Inject

class FilterProductsUseCase @Inject constructor() {
    
    operator fun invoke(params: FilterParams): List<ProductWithDetails> {
        return params.products
            .applySearchFilter(params.searchQuery)
            .applyCategoryFilter(params.categoryId)
            .applyStockFilter(params.stockFilter)
            .applySorting(params.sortOption)
    }
    
    // ==================== FILTER EXTENSIONS ====================
    
    private fun List<ProductWithDetails>.applySearchFilter(
        query: String
    ): List<ProductWithDetails> {
        if (query.isBlank()) return this
        
        val searchLower = query.lowercase()
        return filter { productWithDetails ->
            val product = productWithDetails.product
            product.name.lowercase().contains(searchLower) ||
            product.sku.lowercase().contains(searchLower) ||
            productWithDetails.category.name.lowercase().contains(searchLower)
        }
    }
    
    private fun List<ProductWithDetails>.applyCategoryFilter(
        categoryId: Long?
    ): List<ProductWithDetails> {
        if (categoryId == null) return this
        return filter { it.product.categoryId == categoryId }
    }
    
    private fun List<ProductWithDetails>.applyStockFilter(
        filter: StockFilter
    ): List<ProductWithDetails> {
        return when (filter) {
            StockFilter.ALL -> this
            StockFilter.IN_STOCK -> filter { 
                it.product.currentStock > it.product.lowStockThreshold 
            }
            StockFilter.LOW_STOCK -> filter { 
                it.product.currentStock in 1..it.product.lowStockThreshold 
            }
            StockFilter.OUT_OF_STOCK -> filter { 
                it.product.currentStock == 0 
            }
        }
    }
    
    private fun List<ProductWithDetails>.applySorting(
        sortOption: ProductSortOption
    ): List<ProductWithDetails> {
        return when (sortOption) {
            ProductSortOption.NAME_ASC -> sortedBy { it.product.name.lowercase() }
            ProductSortOption.NAME_DESC -> sortedByDescending { it.product.name.lowercase() }
            ProductSortOption.STOCK_LOW_HIGH -> sortedBy { it.product.currentStock }
            ProductSortOption.STOCK_HIGH_LOW -> sortedByDescending { it.product.currentStock }
            ProductSortOption.PRICE_LOW_HIGH -> sortedBy { it.product.sellPrice }
            ProductSortOption.PRICE_HIGH_LOW -> sortedByDescending { it.product.sellPrice }
            ProductSortOption.RECENTLY_UPDATED -> sortedByDescending { it.product.updatedAt }
        }
    }
    
    // ==================== DATA CLASSES ====================
    
    data class FilterParams(
        val products: List<ProductWithDetails>,
        val searchQuery: String = "",
        val categoryId: Long? = null,
        val stockFilter: StockFilter = StockFilter.ALL,
        val sortOption: ProductSortOption = ProductSortOption.NAME_ASC
    )
}
