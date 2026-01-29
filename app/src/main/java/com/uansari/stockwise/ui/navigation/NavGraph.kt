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
import com.uansari.stockwise.ui.sale.NewSaleScreen
import com.uansari.stockwise.ui.sale.SalesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // ==================== Bottom Nav Destinations ====================

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToProducts = {
                    navController.navigate(Screen.Products.route)
                },
                onNavigateToNewSale = {
                    navController.navigate(Screen.NewSale.route)
                },
                onNavigateToLowStock = {
                    // TODO: Navigate to products filtered by low stock
                    navController.navigate(Screen.Products.route)
                },
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                }
            )
        }

        composable(route = Screen.Products.route) {
            ProductsScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddEditProduct.createRoute())
                }
            )
        }

        composable(route = Screen.NewSale.route) {
            NewSaleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaleComplete = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }

        composable(route = Screen.Sales.route) {
            SalesScreen(
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                }
            )
        }

        composable(route = Screen.More.route) {
            MoreScreen(
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToSuppliers = {
                    navController.navigate(Screen.Suppliers.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ==================== Product Detail & Edit ====================

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            // TODO: ProductDetailScreen
            PlaceholderScreen(title = "Product Detail")
        }

        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(
                navArgument(Screen.AddEditProduct.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                    defaultValue = -1L  // -1 means "add new"
                }
            )
        ) {
            // TODO: AddEditProductScreen
            PlaceholderScreen(title = "Add/Edit Product")
        }

        // ==================== Stock Movement ====================

        composable(
            route = Screen.StockMovement.route,
            arguments = listOf(
                navArgument(Screen.StockMovement.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            // TODO: StockMovementScreen
            PlaceholderScreen(title = "Stock Movement")
        }

        // ==================== Sale Detail ====================

        composable(
            route = Screen.SaleDetail.route,
            arguments = listOf(
                navArgument(Screen.SaleDetail.ARG_SALE_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            // TODO: SaleDetailScreen
            PlaceholderScreen(title = "Sale Detail")
        }

        // ==================== Categories ====================

        composable(route = Screen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddEditCategory.createRoute())
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(Screen.AddEditCategory.createRoute(categoryId))
                }
            )
        }

        composable(
            route = Screen.AddEditCategory.route,
            arguments = listOf(
                navArgument(Screen.AddEditCategory.ARG_CATEGORY_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            // TODO: AddEditCategoryScreen
            PlaceholderScreen(title = "Add/Edit Category")
        }

        // ==================== Suppliers ====================

        composable(route = Screen.Suppliers.route) {
            // TODO: SuppliersScreen
            PlaceholderScreen(title = "Suppliers")
        }

        composable(
            route = Screen.AddEditSupplier.route,
            arguments = listOf(
                navArgument(Screen.AddEditSupplier.ARG_SUPPLIER_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
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
