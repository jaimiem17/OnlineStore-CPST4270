# Happy Feet Marketplace

A console-based Java marketplace application that allows customers to browse and purchase products while enabling sellers to manage their stores and inventory.

## Features

### For Customers

- Browse products by name, price, store, description, or quantity
- Search products by category (Shoes, Clothing, Accessories)
- Add items to shopping cart
- Purchase products and view purchase history
- Leave reviews for purchased products

### For Sellers

- Create and manage multiple stores
- Add, edit, and remove products from inventory
- Set product categories and pricing
- View sales data and store information

## Product Categories

- **Shoes** - Footwear products
- **Clothing** - Apparel and garments
- **Accessories** - Fashion accessories and extras

## Getting Started

### Prerequisites

- Java Development Kit (JDK) installed
- Command line access

### Compilation

```bash
javac *.java
```

### Running the Application

Choose one of the main entry points:

**Marketplace (Recommended)**

```bash
java Marketplace
```

**Login (Legacy)**

```bash
java Login
```

## Data Storage

The application uses file-based storage:

- `Accounts.txt` - User authentication data
- `Sellers.txt` - Seller and product information
- `[email]` - Individual customer files for cart and purchase history

## Architecture

- **Product.java** - Core product entity with category support
- **Store.java** - Store management and product operations
- **Seller.java** - Seller account and multi-store management
- **Customer.java** - Customer account and shopping functionality
- **ProductCategory.java** - Product categorization system
- **ProductSearchService.java** - Advanced search and filtering
- **DataMigrationService.java** - Legacy data compatibility

## Backward Compatibility

The system automatically migrates legacy shoe-only data to the new multi-category format while preserving all existing functionality.
