package com.uansari.stockwise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.uansari.stockwise.ui.categories.CategoriesScreen
import com.uansari.stockwise.ui.dashboard.DashboardScreen
import com.uansari.stockwise.ui.more.MoreScreen
import com.uansari.stockwise.ui.product.ProductsScreen
import com.uansari.stockwise.ui.product_add_edit.ProductAddEditScreen
import com.uansari.stockwise.ui.product_detail.ProductDetailScreen
import com.uansari.stockwise.ui.sale.NewSaleScreen
import com.uansari.stockwise.ui.sale.SalesScreen
import com.uansari.stockwise.ui.stock.StockMovementScreen
import com.uansari.stockwise.ui.suppliers.SuppliersScreen

@Composable
fun NavGraph(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // ==================== Bottom Nav Destinations ====================

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(onNavigateToProducts = {
                navController.navigate(Screen.Products.route)
            }, onNavigateToNewSale = {
                navController.navigate(Screen.NewSale.route)
            }, onNavigateToLowStock = {
                // Navigate to products with low stock filter
                navController.navigate(Screen.Products.route)
            }, onNavigateToSaleDetail = { saleId ->
                navController.navigate(Screen.SaleDetail.createRoute(saleId))
            }, onNavigateToProductDetail = { productId ->
                navController.navigate(Screen.ProductDetail.createRoute(productId))
            })
        }

        composable(route = Screen.Products.route) {
            ProductsScreen(onNavigateToProductDetail = { productId ->
                navController.navigate(Screen.ProductDetail.createRoute(productId))
            }, onNavigateToAddProduct = {
                navController.navigate(Screen.ProductAddEdit.createRoute())
            }, onNavigateToEditProduct = { productId ->
                navController.navigate(Screen.ProductAddEdit.createRoute(productId))
            }, onNavigateToStockMovement = { productId ->
                navController.navigate(Screen.StockMovement.createRoute(productId))
            })
        }

        composable(route = Screen.NewSale.route) {
            NewSaleScreen(onNavigateBack = {
                navController.popBackStack()
            }, onSaleComplete = { saleId ->
                navController.navigate(Screen.SaleDetail.createRoute(saleId)) {
                    popUpTo(Screen.Dashboard.route)
                }
            })
        }

        composable(route = Screen.Sales.route) {
            SalesScreen(
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                })
        }

        composable(route = Screen.More.route) {
            MoreScreen(
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToSuppliers = {
                    navController.navigate(Screen.Suppliers.route)
                },
                /*                onNavigateToReports = {
                                navController.navigate(Screen.Reports.route)
                            }, onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            }*/
            )
        }

        // ==================== Product Detail & Edit ====================

        composable(
            route = Screen.ProductDetail.route, arguments = listOf(
                navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                })
        ) {
            ProductDetailScreen(onNavigateBack = {
                navController.popBackStack()
            }, onNavigateToEdit = { productId ->
                navController.navigate(Screen.ProductAddEdit.createRoute(productId))
            }, onNavigateToStockHistory = { productId ->
                navController.navigate(Screen.StockMovement.createRoute(productId))
            })
        }
        composable(
            route = Screen.ProductAddEdit.route, arguments = listOf(
                navArgument(Screen.ProductAddEdit.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
        ) {
            ProductAddEditScreen(onNavigateBack = {
                navController.popBackStack()
            }, onProductSaved = { productId ->
                // Navigate to product detail after save
                navController.navigate(Screen.ProductDetail.createRoute(productId)) {
                    // Remove add/edit screen from back stack
                    popUpTo(Screen.Products.route)
                }
            })
        }

        // ==================== Stock Movement ====================

        composable(
            route = Screen.StockMovement.route, arguments = listOf(
                navArgument(Screen.StockMovement.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                })
        ) {
            StockMovementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                })
        }
        // ==================== Sale Detail ====================

        composable(
            route = Screen.SaleDetail.route, arguments = listOf(
                navArgument(Screen.SaleDetail.ARG_SALE_ID) {
                    type = NavType.LongType
                })
        ) {
            // TODO: SaleDetailScreen
            PlaceholderScreen(title = "Sale Detail")
        }

        // ==================== Categories ====================

        composable(route = Screen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                })
        }

        composable(
            route = Screen.AddEditCategory.route, arguments = listOf(
                navArgument(Screen.AddEditCategory.ARG_CATEGORY_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
        ) {
            // TODO: AddEditCategoryScreen
            PlaceholderScreen(title = "Add/Edit Category")
        }

        // ==================== Suppliers ====================

        composable(route = Screen.Suppliers.route) {
            SuppliersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                })
        }

        composable(
            route = Screen.AddEditSupplier.route, arguments = listOf(
                navArgument(Screen.AddEditSupplier.ARG_SUPPLIER_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
        ) {
            // TODO: AddEditSupplierScreen
            PlaceholderScreen(title = "Add/Edit Supplier")
        }

        // ==================== Reports & Settings ====================

        composable(route = Screen.Reports.route) {
            // TODO: ReportsScreen
            PlaceholderScreen(title = "Reports")
        }

        composable(route = Screen.Settings.route) {
            // TODO: SettingsScreen
            PlaceholderScreen(title = "Settings")
        }
    }
}
