# Happy Feet - Online Shoe Marketplace

A Java-based console application for managing an online marketplace with support for multiple product categories.

## Features

- **User Authentication**: Separate login flows for customers and sellers
- **Product Management**: Support for 7 product categories (Shoes, Clothing, Accessories, Electronics, Home & Garden, Sports & Outdoors, Books & Media)
- **Shopping Cart**: Full cart functionality with purchase history
- **Search & Filter**: Category-aware search by name, price, store, and description
- **Seller Dashboard**: Multi-store management with sales analytics
- **Database Support**: H2 database integration with file-based fallback

## [Click for Installation & Run instructions](RUNNING.md)


## Project Structure

```
├── src/
│   ├── main/          # Main application entry points
│   ├── model/         # Domain models (Product, Customer, Seller, etc.)
│   ├── dao/           # Data Access Objects for database operations
│   ├── service/       # Business logic services
│   └── util/          # Utility classes (DatabaseManager, etc.)
├── tests/             # Unit and integration tests
├── data/              # Data files (Accounts.txt, Sellers.txt)
├── docs/              # Project documentation and reports
└── lib/               # External libraries (H2 database)
```




## Product Categories

The marketplace supports the following product categories:

1. **Shoes** - Footwear of all types
2. **Clothing** - Apparel and fashion items
3. **Accessories** - Fashion accessories and add-ons
4. **Electronics** - Electronic devices and gadgets
5. **Home & Garden** - Home improvement and gardening items
6. **Sports & Outdoors** - Athletic and outdoor equipment
7. **Books & Media** - Books, music, and media content

## Data Migration

The system automatically migrates legacy shoe-only data to the new multi-category format. Existing products without a category are automatically assigned to the "Shoes" category for backward compatibility.
