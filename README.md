<div align="center">
  
# 📦 StockWise

### Modern Android Inventory Management App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-29+-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.12.3-blue.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

**A feature-rich inventory management application built with Clean Architecture, MVI pattern, and modern Android development practices.**

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Screenshots](#-screenshots) • [Getting Started](#-getting-started)

</div>

---

## 📱 Features

### Core Functionality
- **📊 Dashboard** - Real-time inventory overview with key metrics, low stock indicator, and recent activity
- **📦 Product Management** - Full CRUD operations with search, filtering, and sorting capabilities
- **📁 Categories** - Organize products with color-coded categories and delete protection
- **🚚 Suppliers** - Manage supplier information with contact details
- **📈 Stock Tracking** - Record stock movements (in/out/adjustments) with complete history

### User Experience
- **🔍 Advanced Search** - Search products by name or SKU with real-time results
- **🎛️ Smart Filtering** - Filter by category, stock status (in stock, low stock, out of stock)
- **📊 Sorting Options** - Sort by name, price, stock level, or last updated

---

## 🏗️ Architecture

StockWise follows **Clean Architecture** principles with clear separation of concerns across three layers:
```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Screens   │  │  ViewModels │  │  Contracts (MVI)    │  │
│  │  (Compose)  │  │   (State)   │  │ State/Event/Effect  │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Use Cases  │  │   Models    │  │Repository Interfaces│  │
│  │             │  │  (Domain)   │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                       DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │Repositories │  │    DAOs     │  │      Entities       │  │
│  │   (Impl)    │  │   (Room)    │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### MVI Pattern (Model-View-Intent)

Each screen follows a unidirectional data flow:
```
┌──────────┐     ┌───────────┐     ┌──────────┐     ┌──────────┐
│   User   │────▶│   Event   │────▶│ ViewModel│────▶│  State   │
│  Action  │     │ (Intent)  │     │          │     │          │
└──────────┘     └───────────┘     └────┬─────┘     └────┬─────┘
                                        │                │
                                        ▼                ▼
                                  ┌──────────┐     ┌──────────┐
                                  │  Effect  │     │    UI    │
                                  │(One-time)│     │ (Screen) │
                                  └──────────┘     └──────────┘
```

---

## 🛠️ Tech Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Kotlin 2.2.21 |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Architecture** | Clean Architecture + MVI |
| **Dependency Injection** | Hilt |
| **Database** | Room (SQLite) |
| **Async** | Kotlin Coroutines & Flow |
| **Navigation** | Jetpack Navigation Compose |
| **Testing** | JUnit, MockK, Turbine, Truth |

---

## 📸 Screenshots

<div align="center">

| Dashboard | Products | Product Detail |
|:---------:|:--------:|:--------------:|
| ![Dashboard](docs/screenshots/dashboard.png) | ![Products](docs/screenshots/products-list.png) | ![Detail](docs/screenshots/product-detail.png) |

| Add Product | Categories | Stock Movement |
|:-----------:|:----------:|:--------------:|
| ![Add](docs/screenshots/add-product.png) | ![Categories](docs/screenshots/categories.png) | ![Stock](docs/screenshots/stock-movement.png) |

</div>

---

## 🚀 Getting Started

### Installation

1. **Clone the repository**

2. **Open in Android Studio**

3. **Build and Run**

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

---

## 📁 Project Structure
```
app/src/main/java/com/yourpackage/stockwise/
│
├── 📂 data/                      # Data Layer
│   ├── local/
│   │   ├── dao/                  # Room DAOs
│   │   ├── entity/               # Room Entities
│   │   └── StockWiseDatabase.kt  # Room Database
│   └── repository/               # Repository Implementations
│
├── 📂 domain/                    # Domain Layer
│   ├── model/                    # Domain Models
│   ├── repository/               # Repository Interfaces
│   └── usecase/                  # Use Cases (32 total)
│       ├── dashboard/            # 4 use cases
│       ├── product/              # 8 use cases
│       ├── category/             # 7 use cases
│       ├── supplier/             # 7 use cases
│       └── stock/                # 6 use cases
│
├── 📂 presentation/              # Presentation Layer
│   ├── base/                     # Base MVI classes
│   ├── components/               # Shared UI components
│   ├── navigation/               # Navigation setup
│   ├── dashboard/                # Dashboard screen
│   ├── products/                 # Products screens
│   ├── categories/               # Categories screen
│   ├── suppliers/                # Suppliers screen
│   └── stock/                    # Stock movement screen
│
├── 📂 di/                        # Dependency Injection
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
└── 📂 util/                      # Utilities
    └── DateTimeUtils.kt
```

---

## 🧪 Testing

StockWise includes comprehensive test coverage:

### Test Statistics
- **~95 tests** across unit and instrumented tests
- **60-75% coverage** of critical business logic

### Test Categories

| Type | Location | Coverage |
|------|----------|----------|
| **Use Case Tests** | `test/.../domain/usecase/` | Business logic validation |
| **ViewModel Tests** | `test/.../ui/` | State transitions & effects |
| **DAO Tests** | `androidTest/.../data/local/dao/` | Database queries |

### Testing Approach
```kotlin
// Example: ViewModel test with Turbine
@Test
fun `search query filters products correctly`() = runTest {
    // Arrange
    viewModel = createViewModel()
    advanceUntilIdle()
    
    // Act
    viewModel.onEvent(Event.OnSearchQueryChanged("iPhone"))
    advanceUntilIdle()
    
    // Assert
    val state = viewModel.uiState.value
    assertThat(state.filteredProducts.all { 
        it.product.name.contains("iPhone", ignoreCase = true)
    }).isTrue()
}
```

---

## 📊 Use Cases Overview

StockWise implements **32 Use Cases** following the Single Responsibility Principle:

<details>
<summary><b>Dashboard Use Cases (4)</b></summary>

- `GetInventoryStatsUseCase` - Fetch total products, stock value, categories count
- `GetDailySalesSummaryUseCase` - Daily sales metrics
- `GetLowStockProductsUseCase` - Products below threshold
- `GetRecentSalesUseCase` - Recent sales activity

</details>

<details>
<summary><b>Product Use Cases (8)</b></summary>

- `GetProductsWithDetailsUseCase` - List products with category/supplier
- `GetProductWithDetailsUseCase` - Single product details
- `GetProductByIdUseCase` - Fetch product by ID
- `FilterProductsUseCase` - Filter and sort products
- `ValidateProductUseCase` - Form validation
- `CreateProductUseCase` - Create new product
- `UpdateProductUseCase` - Update existing product
- `DeleteProductUseCase` - Soft delete product

</details>

<details>
<summary><b>Category Use Cases (7)</b></summary>

- `GetCategoriesUseCase` - List all categories
- `GetCategoriesWithProductCountUseCase` - Categories with product counts
- `GetCategoryByIdUseCase` - Single category
- `ValidateCategoryUseCase` - Form validation
- `CreateCategoryUseCase` - Create category
- `UpdateCategoryUseCase` - Update category
- `DeleteCategoryUseCase` - Delete (with protection)

</details>

<details>
<summary><b>Supplier Use Cases (7)</b></summary>

- `GetSuppliersUseCase` - List all suppliers
- `GetSuppliersWithProductCountUseCase` - Suppliers with product counts
- `GetSupplierByIdUseCase` - Single supplier
- `ValidateSupplierUseCase` - Form validation
- `CreateSupplierUseCase` - Create supplier
- `UpdateSupplierUseCase` - Update supplier
- `DeleteSupplierUseCase` - Delete (with protection)

</details>

<details>
<summary><b>Stock Use Cases (6)</b></summary>

- `GetStockMovementsForProductUseCase` - Movement history
- `GetStockMovementsSummaryUseCase` - Calculate totals
- `FilterStockMovementsUseCase` - Filter by type/date
- `AddStockUseCase` - Add stock
- `RemoveStockUseCase` - Remove stock
- `AdjustStockUseCase` - Set stock level

</details>

---

## 🎯 Key Implementation Highlights

### 1. MVI Contract Pattern
```kotlin
ProductsContract.kt
 
    data class ProductsState(
        val isLoading: Boolean = true,
        val products: List<ProductWithDetails> = emptyList(),
        val searchQuery: String = ""
        // ... more state
    ) : UiState {
        // Computed properties
        val isEmpty: Boolean get() = products.isEmpty() && !isLoading
    }
    
    sealed interface ProductsEvent : UiEvent {
        data object LoadProducts : ProductsEvent
        data class OnSearchQueryChanged(val query: String) : ProductsEvent
        // ... more events
    }
    
    sealed interface ProductsEffect : UiEffect {
        data class NavigateToDetail(val productId: Long) : ProductsEffect
        data class ShowSnackbar(val message: String) : ProductsEffect
    }
}
```

### 2. Use Case with Validation
```kotlin
class ValidateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(params: ValidationParams): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Name validation
        if (params.name.isBlank()) {
            errors[FIELD_NAME] = "Product name is required"
        }
        
        // SKU uniqueness check
        val existingProduct = productRepository.getProductBySku(params.sku)
        if (existingProduct != null && existingProduct.id != params.excludeProductId) {
            errors[FIELD_SKU] = "SKU already exists"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}
```

### 3. Delete Protection
```kotlin
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : SuspendUseCase<Long, Result<Unit>> {
    
    override suspend fun invoke(params: Long): Result<Unit> {
        val productCount = productRepository.getProductCountByCategory(params)
        
        if (productCount > 0) {
            return Result.failure(
                CategoryHasProductsException(
                    "Cannot delete category with $productCount products"
                )
            )
        }
        
        return categoryRepository.deleteCategory(params)
    }
}
```

---

## 🗺️ Roadmap

- [x] Phase 1: Database & Foundation
- [x] Phase 2: Clean Architecture + MVI Refactor
- [x] Phase 3: Testing
- [ ] Phase 4: Sales Module (POS)
- [ ] Phase 5: Reports & Analytics
- [ ] Phase 6: Barcode Scanning

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 👤 Author

**Your Name**

- GitHub: [@UsmanAnsari](https://github.com/UsmanAnsari)
- LinkedIn: [usman1ansari](https://www.linkedin.com/in/usman1ansari)
- Email: usman10ansari@gmail.com

---

<div align="center">

⭐ **Star this repo if you find it helpful!** ⭐

</div>
