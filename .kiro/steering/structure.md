# Project Structure

## File Organization
The project follows a flat structure with all Java classes in the root directory:

```
├── .git/                    # Git repository
├── .kiro/                   # Kiro IDE configuration
├── .vscode/                 # VS Code settings
├── *.java                   # Java source files
├── README.md                # Project documentation
└── [documentation files]   # Project reports and notes
```

## Core Classes

### Main Entry Points
- **`Login.java`** - Original main class with authentication and user flows
- **`Marketplace.java`** - Updated main class (appears to be the current version)

### Domain Models
- **`Shoe.java`** - Product entity with name, price, quantity, description, store
- **`Store.java`** - Store entity managing products and sales
- **`Seller.java`** - Seller entity managing multiple stores
- **`Customers.java`** - Customer entity with shopping cart functionality

### Class Relationships
```
Marketplace (main)
├── Seller
│   └── Store (1:many)
│       └── Shoe (1:many)
└── Customer
    └── Shoe (shopping cart, many:many)
```

## Naming Conventions
- **Classes**: PascalCase (e.g., `Shoe`, `Store`, `Customer`)
- **Methods**: camelCase (e.g., `addToCart`, `processPurchase`)
- **Fields**: camelCase with private visibility
- **Constants**: UPPER_SNAKE_CASE (e.g., `WELCOME_PROMPT`)

## File I/O Patterns
- **Data files**: Created in project root directory
- **File naming**: 
  - System files: `Accounts.txt`, `Sellers.txt`
  - User files: Email address as filename
- **Format**: Comma-separated values (CSV) for structured data

## Code Organization Notes
- All classes in default package (no package declarations)
- Heavy use of ArrayList for collections
- File operations use try-with-resources pattern
- Console I/O handled through Scanner class
- String manipulation for data parsing (split by comma)