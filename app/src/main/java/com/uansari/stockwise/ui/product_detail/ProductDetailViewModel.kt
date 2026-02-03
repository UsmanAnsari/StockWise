package com.uansari.stockwise.ui.product_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.uansari.stockwise.domain.usecase.product.DeleteProductUseCase
import com.uansari.stockwise.domain.usecase.product.GetProductWithDetailsUseCase
import com.uansari.stockwise.domain.usecase.stock.AddStockUseCase
import com.uansari.stockwise.domain.usecase.stock.AdjustStockUseCase
import com.uansari.stockwise.domain.usecase.stock.GetStockMovementsForProductUseCase
import com.uansari.stockwise.domain.usecase.stock.RemoveStockUseCase
import com.uansari.stockwise.ui.base.BaseViewModel
import com.uansari.stockwise.ui.navigation.Screen
import com.uansari.stockwise.domain.model.QuickStockAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductWithDetailsUseCase: GetProductWithDetailsUseCase,
    private val getStockMovementsForProductUseCase: GetStockMovementsForProductUseCase,
    private val addStockUseCase: AddStockUseCase,
    private val removeStockUseCase: RemoveStockUseCase,
    private val adjustStockUseCase: AdjustStockUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : BaseViewModel<ProductDetailState, ProductDetailEvent, ProductDetailEffect>(ProductDetailState()) {

    private val productId: Long = savedStateHandle.get<Long>(Screen.ProductDetail.ARG_PRODUCT_ID)
        ?: throw IllegalArgumentException("Product ID is required")

    init {
        onEvent(ProductDetailEvent.LoadProduct)
    }

    // ==================== SINGLE ENTRY POINT ====================

    override fun onEvent(event: ProductDetailEvent) {
        when (event) {
            // Lifecycle
            is ProductDetailEvent.LoadProduct -> loadProduct()
            is ProductDetailEvent.Refresh -> refresh()

            // Quick Stock Actions
            is ProductDetailEvent.ShowQuickStockDialog -> showQuickStockDialog(event.action)
            is ProductDetailEvent.HideQuickStockDialog -> hideQuickStockDialog()
            is ProductDetailEvent.PerformQuickStock -> performQuickStock(
                event.quantity, event.notes
            )

            // Delete
            is ProductDetailEvent.ShowDeleteDialog -> updateState { copy(isDeleteDialogVisible = true) }
            is ProductDetailEvent.HideDeleteDialog -> updateState { copy(isDeleteDialogVisible = false) }
            is ProductDetailEvent.ConfirmDelete -> deleteProduct()

            // Navigation
            is ProductDetailEvent.NavigateBack -> sendEffect(ProductDetailEffect.NavigateBack)
            is ProductDetailEvent.NavigateToEdit -> sendEffect(
                ProductDetailEffect.NavigateToEdit(
                    productId
                )
            )

            is ProductDetailEvent.NavigateToStockHistory -> sendEffect(
                ProductDetailEffect.NavigateToStockHistory(
                    productId
                )
            )

            // Error
            is ProductDetailEvent.ClearError -> updateState { copy(error = null) }
        }
    }

    // ==================== LOAD DATA ====================

    private fun loadProduct() {
        observeProductDetails()
        observeStockMovements()
    }

    private fun refresh() {
        updateState { copy(isLoading = true, error = null) }
        loadProduct()
    }

    private fun observeProductDetails() {
        viewModelScope.launch {
            getProductWithDetailsUseCase(productId).catch { e ->
                handleError(e.message ?: "Failed to load product")
            }.collect { productWithDetails ->
                updateState {
                    copy(
                        productWithDetails = productWithDetails,
                        isLoading = false,
                        error = if (productWithDetails == null) "Product not found" else null
                    )
                }
            }
        }
    }

    private fun observeStockMovements() {
        viewModelScope.launch {
            getStockMovementsForProductUseCase(productId).catch { e ->
                // Non-critical - just log or ignore
                updateState { copy(isLoadingMovements = false) }
            }.collect { movements ->
                updateState {
                    copy(
                        stockMovements = movements, isLoadingMovements = false
                    )
                }
            }
        }
    }

    // ==================== QUICK STOCK ====================

    private fun showQuickStockDialog(action: QuickStockAction) {
        updateState {
            copy(
                isQuickStockDialogVisible = true, quickStockAction = action
            )
        }
    }

    private fun hideQuickStockDialog() {
        updateState {
            copy(
                isQuickStockDialogVisible = false, quickStockAction = null
            )
        }
    }

    private fun performQuickStock(quantity: Int, notes: String?) {
        val action = currentState.quickStockAction ?: return

        viewModelScope.launch {
            updateState { copy(isPerformingAction = true) }

            val result = when (action) {
                QuickStockAction.ADD_STOCK -> addStockUseCase(
                    AddStockUseCase.Params(
                        productId = productId, quantity = quantity, notes = notes
                    )
                )

                QuickStockAction.REMOVE_STOCK -> removeStockUseCase(
                    RemoveStockUseCase.Params(
                        productId = productId, quantity = quantity, notes = notes
                    )
                )

                QuickStockAction.ADJUST_STOCK -> adjustStockUseCase(
                    AdjustStockUseCase.Params(
                        productId = productId, newStockLevel = quantity, notes = notes
                    )
                )
            }

            result.onSuccess {
                updateState {
                    copy(
                        isPerformingAction = false,
                        isQuickStockDialogVisible = false,
                        quickStockAction = null
                    )
                }
                sendEffect(ProductDetailEffect.ShowSnackbar("Stock updated successfully"))
            }.onFailure { error ->
                updateState { copy(isPerformingAction = false) }
                sendEffect(
                    ProductDetailEffect.ShowSnackbar(
                        error.message ?: "Failed to update stock"
                    )
                )
            }
        }
    }

    // ==================== DELETE ====================

    private fun deleteProduct() {
        viewModelScope.launch {
            updateState { copy(isPerformingAction = true) }

            deleteProductUseCase(productId).onSuccess {
                updateState {
                    copy(
                        isPerformingAction = false, isDeleteDialogVisible = false
                    )
                }
                sendEffect(ProductDetailEffect.ProductDeleted)
                sendEffect(ProductDetailEffect.NavigateBack)
            }.onFailure { error ->
                updateState {
                    copy(
                        isPerformingAction = false, isDeleteDialogVisible = false
                    )
                }
                sendEffect(
                    ProductDetailEffect.ShowSnackbar(
                        error.message ?: "Failed to delete product"
                    )
                )
            }
        }
    }

    // ==================== ERROR HANDLING ====================

    private fun handleError(message: String) {
        updateState {
            copy(
                error = message, isLoading = false, isLoadingMovements = false
            )
        }
    }
}
