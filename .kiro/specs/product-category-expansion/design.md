# Design Document

## Overview

The product category expansion transforms the Happy Feet marketplace from a shoe-only platform to a multi-category marketplace. The design maintains backward compatibility with existing shoe data while introducing a flexible category system that can accommodate diverse product types.

## Architecture

### Current System Analysis
- **Product Model**: `Shoe.java` class with fields: name, quantity, price, description, storeName
- **Data Format**: CSV-based storage with format: `name,quantity,price,description,storeName`
- **File Structure**: `Sellers.txt` contains seller data with embedded product information
- **Search System**: Hard-coded search by shoe-specific attributes

### Proposed Architecture Changes

#### 1. Product Model Transformation
- Rename `Shoe.java` to `Product.java` to reflect generic nature
- Add `category` field to the Product class
- Maintain all existing fields for backward compatibility
- Update toString() method to include category: `name,quantity,price,description,storeName,category`

#### 2. Category Management System
```java
public enum ProductCategory {
    SHOES("Shoes"),
    CLOTHING("Clothing"), 
    ACCESSORIES("Accessories"),
    ELECTRONICS("Electronics"),
    HOME_GARDEN("Home & Garden"),
    SPORTS_OUTDOORS("Sports & Outdoors"),
    BOOKS_MEDIA("Books & Media");
    
    private final String displayName;
    // Constructor and getter methods
}
```

#### 3. Data Migration Strategy
- Implement automatic migration for existing shoe data
- Add category field with default value "Shoes" for existing products
- Support both old and new data formats during transition period
- Preserve all existing functionality during migration

## Components and Interfaces

### 1. Product Class (formerly Shoe)
```java
public class Product {
    private String name;
    private String storeName;
    private String description;
    private int quantity;
    private double price;
    private ProductCategory category; // New field
    private ArrayList<String> review;
    
    // Constructors for backward compatibility
    public Product(String name, int quantity, double price, String description, String storeName) {
        // Default to SHOES category for backward compatibility
    }
    
    public Product(String name, int quantity, double price, String description, String storeName, ProductCategory category) {
        // New constructor with category
    }
}
```

### 2. Store Class Updates
```java
public class Store {
    private ArrayList<Product> products; // Renamed from shoes
    
    // Updated method names for generic products
    public void addProduct(Product product);
    public void removeProduct(Product product);
    public ArrayList<Product> getProductsByCategory(ProductCategory category);
}
```

### 3. Search and Filter System
```java
public class ProductSearchService {
    public static ArrayList<Product> searchByName(String name);
    public static ArrayList<Product> searchByCategory(ProductCategory category);
    public static ArrayList<Product> searchByNameAndCategory(String name, ProductCategory category);
    public static ArrayList<Product> filterByCategory(ArrayList<Product> products, ProductCategory category);
}
```

### 4. Data Migration Service
```java
public class DataMigrationService {
    public static void migrateExistingData();
    public static Product parseProductFromLegacyFormat(String csvLine);
    public static Product parseProductFromNewFormat(String csvLine);
}
```

## Data Models

### File Format Evolution

#### Current Format (Sellers.txt):
```
seller@email.com,StoreName,ProductName,Quantity,Price,Description,StoreName
```

#### New Format (Sellers.txt):
```
seller@email.com,StoreName,ProductName,Quantity,Price,Description,StoreName,Category
```

### Migration Handling
- Support reading both old and new formats
- Automatically assign "Shoes" category to products without category field
- Write all new data in the new format
- Maintain file structure compatibility

### Category Data Structure
```java
// Categories stored as enum for type safety and validation
public enum ProductCategory {
    SHOES, CLOTHING, ACCESSORIES, ELECTRONICS, 
    HOME_GARDEN, SPORTS_OUTDOORS, BOOKS_MEDIA
}
```

## Error Handling

### Category Validation
- Validate category selection during product creation
- Provide user-friendly error messages for invalid categories
- Default to appropriate category if validation fails during migration

### Data Migration Error Handling
- Handle malformed data gracefully during migration
- Log migration issues without stopping the process
- Provide fallback values for missing or invalid data

### File I/O Error Handling
- Maintain existing error handling patterns
- Add specific handling for category-related data corruption
- Ensure system remains functional if category data is missing

## Testing Strategy

### Unit Testing Focus Areas
1. **Product Class Testing**
   - Constructor validation with and without category
   - toString() method format verification
   - Backward compatibility with existing shoe data

2. **Category System Testing**
   - Category enum validation
   - Category display name formatting
   - Invalid category handling

3. **Data Migration Testing**
   - Legacy data format parsing
   - New data format parsing
   - Mixed format handling during transition

4. **Search and Filter Testing**
   - Category-based search functionality
   - Combined name and category searches
   - Filter performance with large datasets

### Integration Testing
1. **End-to-End User Flows**
   - Customer browsing by category
   - Seller adding products with categories
   - Shopping cart with mixed category products

2. **Data Persistence Testing**
   - File format consistency after operations
   - Data integrity during migration
   - Backward compatibility verification

### Migration Testing
1. **Data Integrity**
   - Verify all existing shoes are preserved
   - Confirm automatic category assignment
   - Validate file format consistency

2. **Performance Testing**
   - Migration time for large datasets
   - Search performance with categories
   - Memory usage during migration

## Implementation Phases

### Phase 1: Core Infrastructure
- Create ProductCategory enum
- Rename Shoe to Product with category field
- Update constructors for backward compatibility
- Implement data migration utilities

### Phase 2: Data Layer Updates
- Update Store class to use generic products
- Implement category-aware search methods
- Update file I/O to handle new format
- Implement automatic data migration

### Phase 3: User Interface Updates
- Update seller menus to include category selection
- Update customer search interface with category filters
- Update display methods to show categories
- Add category-based analytics for sellers

### Phase 4: Testing and Validation
- Comprehensive testing of all components
- Migration testing with sample data
- Performance validation
- User acceptance testing simulation