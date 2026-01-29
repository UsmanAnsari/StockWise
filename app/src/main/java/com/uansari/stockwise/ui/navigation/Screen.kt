package com.uansari.stockwise.ui.navigation

sealed class Screen(val route: String) {

    data object Dashboard : Screen("dashboard")
    data object Products : Screen("products")
    data object NewSale : Screen("new_sale")
    data object Sales : Screen("sales")
    data object More : Screen("more")


    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Long) = "product/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }

    data object AddEditProduct : Screen("product/edit?productId={productId}") {
        fun createRoute(productId: Long? = null): String {
            return if (productId != null) {
                "product/edit?productId=$productId"
            } else {
                "product/edit"
            }
        }

        const val ARG_PRODUCT_ID = "productId"
    }


    data object StockMovement : Screen("stock/{productId}") {
        fun createRoute(productId: Long) = "stock/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }


    data object SaleDetail : Screen("sale/{saleId}") {
        fun createRoute(saleId: Long) = "sale/$saleId"
        const val ARG_SALE_ID = "saleId"
    }


    data object Categories : Screen("categories")
    data object AddEditCategory : Screen("category/edit?categoryId={categoryId}") {
        fun createRoute(categoryId: Long? = null): String {
            return if (categoryId != null) {
                "category/edit?categoryId=$categoryId"
            } else {
                "category/edit"
            }
        }

        const val ARG_CATEGORY_ID = "categoryId"
    }

    data object Suppliers : Screen("suppliers")
    data object AddEditSupplier : Screen("supplier/edit?supplierId={supplierId}") {
        fun createRoute(supplierId: Long? = null): String {
            return if (supplierId != null) {
                "supplier/edit?supplierId=$supplierId"
            } else {
                "supplier/edit"
            }
        }

        const val ARG_SUPPLIER_ID = "supplierId"
    }

    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}