# Technology Stack

## Language & Runtime
- **Java**: Core application language
- **JDK**: Standard Java Development Kit (no external frameworks)

## Build System
- Standard Java compilation using `javac`
- No build automation tools (Maven/Gradle) detected

## Data Persistence
- **File-based storage**: Plain text files for data persistence
  - `Accounts.txt`: User authentication data (email,password,userType)
  - `Sellers.txt`: Seller and product data in CSV format
  - Individual customer files: Named by email for shopping carts and purchase history

## Key Libraries
- `java.util.*`: Collections (ArrayList, Scanner)
- `java.io.*`: File I/O operations (BufferedReader, BufferedWriter, FileReader, FileWriter)

## Common Commands

### Compilation
```bash
javac *.java
```

### Running the Application
```bash
# Main entry points (choose one):
java Login        # Original login implementation
java Marketplace  # Updated marketplace implementation
```

### File Structure
The application creates and manages these files at runtime:
- `Accounts.txt` - User accounts
- `Sellers.txt` - Seller/product data
- `[email]` - Individual customer files for cart/history

## Development Notes
- Console-based application using Scanner for user input
- No external dependencies or frameworks
- File I/O operations use try-with-resources for proper resource management