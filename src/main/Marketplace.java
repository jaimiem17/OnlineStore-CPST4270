import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Marketplace {
    public static final String WELCOME_PROMPT = "Welcome to our Marketplace!";
    public static final String ENTER_YOUR_EMAIL = "Please enter your e-mail.";
    public static final String SIGN_IN_PROMPT = "1: Sign In\n2: Create an account.";

    public static final String LOGIN_PASSWORD_PROMPT = "Please enter your password.";

    public static final String CREATE_PASSWORD_PROMPT = "Please enter a password greater than 5 characters.";

    public static final String BUYER_OR_SELLER = "1: Customer\n2: Seller";
    
    private static final int MAX_PRODUCT_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MAX_CHANGE_REASON_LENGTH = 120;

    public static ArrayList<Seller> sellers = new ArrayList<>();

    public static void loadMarket() {
        File f = new File(FileConstants.SELLERS_FILE);
        if (f.exists()) {
            try (BufferedReader bfr = new BufferedReader(new FileReader(f))) {
                String line = "";
                ArrayList<String> allLines = new ArrayList<>();
                boolean needsMigration = false;
                
                // First pass: read all lines and check if migration is needed
                while ((line = bfr.readLine()) != null) {
                    allLines.add(line);
                    if (!line.trim().isEmpty() && DataMigrationService.isLegacyFormat(line)) {
                        needsMigration = true;
                    }
                }
                
                // If migration is needed, convert legacy data to new format
                if (needsMigration) {
                    System.out.println("Migrating existing shoe data to new product format...");
                    try {
                        migrateDataFile(allLines);
                    } catch (Exception e) {
                        System.out.println("Warning: Some data migration issues occurred: " + e.getMessage());
                        System.out.println("Continuing with available data...");
                    }
                }
                
                // Process all lines (now in new format)
                for (String currentLine : allLines) {
                    if (currentLine.trim().isEmpty()) {
                        continue;
                    }
                    
                    String[] arr = currentLine.split(",");
                    
                    if (arr.length > 2) {
                        // Line contains product data
                        String sellerEmail = arr[0].replaceAll(",", "");
                        String storeName = arr[1].replaceAll(",", "");
                        
                        // Find or create seller
                        Seller seller = findOrCreateSeller(sellerEmail);
                        
                        // Find or create store (avoid duplicates)
                        Store store = findOrCreateStore(seller, storeName);
                        
                        // Parse products using migration service
                        try {
                            ArrayList<Product> products = DataMigrationService.parseProductsFromLine(currentLine);
                            for (Product product : products) {
                                // Ensure product's store name matches the Store object's name
                                // This prevents mismatches when the CSV has inconsistent store names
                                product.setStoreName(storeName);
                                store.addProduct(product);
                            }
                        } catch (Exception e) {
                            System.out.println("Warning: Could not parse products from line: " + currentLine);
                            System.out.println("Error: " + e.getMessage());
                            // Continue processing other lines
                        }
                        
                    } else if (arr.length == 2) {
                        // Line contains only seller and store info (no products)
                        String sellerEmail = arr[0].replaceAll(",", "");
                        String storeName = arr[1].replaceAll(",", "");
                        
                        // Find or create seller
                        Seller seller = findOrCreateSeller(sellerEmail);
                        
                        // Find or create empty store (avoid duplicates)
                        findOrCreateStore(seller, storeName);
                        
                    } else if (arr.length == 1 && !currentLine.trim().isEmpty()) {
                        // Line contains only seller info
                        String sellerEmail = arr[0].replaceAll(",", "");
                        findOrCreateSeller(sellerEmail);
                    }
                }
                
                // Add empty seller at the end (maintaining original behavior)
                sellers.add(new Seller(""));
                
            } catch (IOException e) {
                System.out.println("Error reading the sellers file: " + e.getMessage());
            }
        } else {
            try {
                boolean b = f.createNewFile();
            } catch (IOException e) {
                System.out.println("There was an error creating the sellers file." + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method to find an existing seller or create a new one.
     * @param email The seller's email
     * @return The existing or newly created Seller object
     */
    private static Seller findOrCreateSeller(String email) {
        for (int i = 0; i < sellers.size(); i++) {
            if (email.equals(sellers.get(i).getEmail())) {
                return sellers.get(i);
            }
        }
        
        // Seller not found, create new one
        Seller newSeller = new Seller(email);
        sellers.add(newSeller);
        return newSeller;
    }
    
    /**
     * Helper method to find an existing store for a seller or create a new one.
     * This prevents duplicate stores when the same store appears on multiple lines.
     * @param seller The seller who owns the store
     * @param storeName The name of the store
     * @return The existing or newly created Store object
     */
    private static Store findOrCreateStore(Seller seller, String storeName) {
        // Check if store already exists for this seller
        for (Store store : seller.getStores()) {
            if (store.getName().equalsIgnoreCase(storeName)) {
                return store;
            }
        }
        
        // Store not found, create new one and add to seller
        Store newStore = new Store(storeName);
        seller.addStores(newStore);
        return newStore;
    }
    
    /**
     * Migrates the data file from legacy format to new format.
     * @param lines All lines from the original file
     */
    private static void migrateDataFile(ArrayList<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileConstants.SELLERS_FILE))) {
            int migratedLines = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) {
                    writer.write(line);
                } else {
                    try {
                        String convertedLine = DataMigrationService.convertLegacyLineToNewFormat(line);
                        writer.write(convertedLine);
                        // Update the line in memory for processing
                        lines.set(i, convertedLine);
                        migratedLines++;
                    } catch (Exception e) {
                        System.out.println("Warning: Could not migrate line: " + line);
                        System.out.println("Error: " + e.getMessage());
                        // Keep original line if migration fails
                        writer.write(line);
                    }
                }
                writer.newLine();
            }
            System.out.println("Data migration completed successfully. Migrated " + migratedLines + " lines.");
        } catch (IOException e) {
            System.out.println("Error during data migration: " + e.getMessage());
            throw new RuntimeException("Failed to migrate data file", e);
        }
    }


    public static void main(String[] args) {
        
        FileConstants.bootstrapLegacyFilesIfNeeded();
// Initialize database on startup
        try {
            System.out.println("Initializing database...");
            DatabaseManager.initializeTables();
            System.out.println("Database initialized successfully.");
            System.out.println();
            
            // Check if migration is needed
            File accountsFile = new File(FileConstants.ACCOUNTS_FILE);
            File sellersFile = new File(FileConstants.SELLERS_FILE);
            
            if (accountsFile.exists() || sellersFile.exists()) {
                System.out.println("Text files detected. Would you like to migrate data to database? (yes/no)");
                Scanner migrationScanner = new Scanner(System.in);
                String migrationResponse = migrationScanner.nextLine();
                
                if (migrationResponse.equalsIgnoreCase("yes") || migrationResponse.equalsIgnoreCase("y")) {
                    System.out.println();
                    DataMigrationService.migrateAllDataToDatabase();
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Database initialization failed: " + e.getMessage());
            System.err.println("Continuing with file-based operations...");
        }
        
        String email = "";
        String userType = "";
        Scanner scanner = new Scanner(System.in);
        boolean authenticated = false;

        while (!authenticated) {
            System.out.println(WELCOME_PROMPT);
            System.out.println(SIGN_IN_PROMPT);
            String response = scanner.nextLine().trim();

            if ("1".equals(response)) {
                AccountRecord record = attemptLogin(scanner);
                if (record != null) {
                    email = record.email;
                    userType = record.role;
                    authenticated = true;
                }
            } else if ("2".equals(response)) {
                AccountRecord record = handleAccountCreation(scanner);
                if (record != null) {
                    email = record.email;
                    userType = record.role;
                    authenticated = true;
                }
            } else {
                System.out.println("Please either select 1 or 2.");
            }
        }

        loadMarket();



// Sellers
        if (userType.equals("SELLER")) {
            Seller s = new Seller(email);
            int index = 0;
            if (sellers.size() == 1) {
                sellers.set(0, s);
                index = sellers.indexOf(s);
            }

            for (int i = 0; i < sellers.size(); i++) {
                if (sellers.get(i).getEmail().equals(email)) {
                    index = i;
                    break;
                }
            }
            


            int sellerUserId = -1;
            try {
                UserDAO userDAO = new UserDAO();
                sellerUserId = userDAO.getUserId(email);
            } catch (Exception e) {
                System.err.println("Warning: Unable to load seller profile: " + e.getMessage());
            }
            
            String performActivity = "";
            do {
                /**
                 * below are the general variables we will use in your menu
                 */
                // ____________
                String storeName = "";

                // ____________
                System.out.println("Menu");
                System.out.println("1: Add a store");
                System.out.println("2: Add a product to one of your Stores");
                System.out.println("3: Remove a product from one of your Stores");
                System.out.println("4: Edit a product from one of your Stores");
                System.out.println("5: View your stores and their details");
                System.out.println("6: View a product's change history");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        System.out.println("What is the name of the store you would like to add?");
                        storeName = scanner.nextLine();
                        sellers.get(index).addStores(storeName);
                        sellers.get(index).writeToSellerFileAddStore(storeName);
                        break;
                    case 2:
                        System.out.println("What is the name of the store you would like to add a product to?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            String productName = promptLimitedText(scanner, "What is the name of your product?", MAX_PRODUCT_NAME_LENGTH, false);
                            int quantity = promptNonNegativeInt(scanner, "How many products do you want to manufacture?");
                            double price = promptPositiveDouble(scanner, "What will the price of your product be?");
                            String description = promptLimitedText(scanner, "What is the description of your product? (you may leave this blank)", MAX_DESCRIPTION_LENGTH, true);
                            if (description.isEmpty()) {
                                description = "No description provided";
                            }
                            ProductCategory category = promptForCategorySelection(scanner);
                            sellers.get(index).writeToSellerFileAddProduct(storeName, productName, quantity, price, description, category);
                            
                            if (sellerUserId > 0) {
                                try {
                                    sellers.get(index).createProductDB(storeName, productName, quantity, price, description, category, sellerUserId);
                                } catch (Exception e) {
                                    System.err.println("Warning: Unable to sync product with database: " + e.getMessage());
                                }
                            }
                            
                            System.out.println("Product added successfully.");
                        } else {
                            System.out.println("Sorry, you are not affiliated with " + storeName);
                        }
                        break;
                    case 3:
                        System.out.println("What is the name of the store you would to remove a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            String productName = promptLimitedText(scanner, "What was the name of the product?", MAX_PRODUCT_NAME_LENGTH, false);
                            try {
                                ProductDAO productDAO = new ProductDAO();
                                int productId = productDAO.getProductId(productName, storeName);
                                Product product = productId > 0 ? productDAO.getProductById(productId) : null;
                                if (product == null) {
                                    System.out.println("Unable to locate that product in " + storeName + ".");
                                } else {
                                    sellers.get(index).writeToSellerFileRemoveProduct(storeName, product.getName(), product.getQuantity(), product.getPrice(), product.getDescription(), product.getCategory());
                                    try {
                                        if (sellerUserId > 0) {
                                            sellers.get(index).removeProductDB(productId, storeName, product);
                                        } else {
                                            sellers.get(index).removeProduct(storeName, product);
                                        }
                                    } catch (Exception e) {
                                        sellers.get(index).removeProduct(storeName, product);
                                        System.err.println("Warning: Unable to remove product from database: " + e.getMessage());
                                    }
                                    System.out.println("Product removed successfully.");
                                }
                            } catch (Exception e) {
                                System.err.println("Error removing product: " + e.getMessage());
                            }
                        }
                        break;
                    case 4:
                        System.out.println("What is the name of the store you would like to edit a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            String productName = promptLimitedText(scanner, "What is the name of your product?", MAX_PRODUCT_NAME_LENGTH, false);
                            try {
                                ProductDAO productDAO = new ProductDAO();
                                int productId = productDAO.getProductId(productName, storeName);
                                Product oldProduct = productId > 0 ? productDAO.getProductById(productId) : null;
                                if (oldProduct == null) {
                                    System.out.println("Unable to find that product in " + storeName + ".");
                                    break;
                                }

                                String newProductName = promptUpdatedText(scanner, "What do you want the new name of the product to be?", oldProduct.getName(), MAX_PRODUCT_NAME_LENGTH);
                                int newQuantity = promptUpdatedInt(scanner, "What is the new quantity?", oldProduct.getQuantity());
                                double newPrice = promptUpdatedDouble(scanner, "What is the new price?", oldProduct.getPrice());
                                String newDescription = promptUpdatedText(scanner, "What is the new description?", oldProduct.getDescription(), MAX_DESCRIPTION_LENGTH);
                                ProductCategory newCategory = promptUpdatedCategory(scanner, oldProduct.getCategory());
                                String changeReason = promptChangeReason(scanner);

                                boolean updated = false;
                                try {
                                    if (sellerUserId > 0) {
                                        updated = sellers.get(index).editProductDB(productId, oldProduct, newProductName, newDescription, storeName, newQuantity, newPrice, newCategory, sellerUserId, changeReason);
                                    } else {
                                        sellers.get(index).editProduct(oldProduct, newProductName, newDescription, storeName, newQuantity, newPrice, newCategory);
                                        updated = true;
                                    }
                                } catch (Exception e) {
                                    System.err.println("Warning: Unable to update product in database: " + e.getMessage());
                                    sellers.get(index).editProduct(oldProduct, newProductName, newDescription, storeName, newQuantity, newPrice, newCategory);
                                    updated = true;
                                }

                                if (updated) {
                                    sellers.get(index).writerToSellerFileEditProduct(oldProduct.getName(), oldProduct.getQuantity(), oldProduct.getPrice(), oldProduct.getDescription(), oldProduct.getCategory(), storeName,
                                            newProductName, newQuantity, newPrice, newDescription, newCategory);
                                    System.out.println("Product updated successfully.");
                                }
                            } catch (Exception e) {
                                System.err.println("Error editing product: " + e.getMessage());
                            }
                        }
                        break;
                    case 5:
                        for (Store store : sellers.get(index).getStores()) {
                            System.out.println(store.toString());
                        }
                        break;
                    case 6:
                        System.out.println("What is the name of the store for which you want to view change history?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            String productName = promptLimitedText(scanner, "Enter the product name:", MAX_PRODUCT_NAME_LENGTH, false);
                            try {
                                ProductDAO productDAO = new ProductDAO();
                                int productId = productDAO.getProductId(productName, storeName);
                                if (productId <= 0) {
                                    System.out.println("Unable to locate that product in " + storeName + ".");
                                } else {
                                    sellers.get(index).viewProductAuditTrail(productId);
                                }
                            } catch (Exception e) {
                                System.err.println("Unable to display change history: " + e.getMessage());
                            }
                        }
                        break;
                    default:
                        System.out.println("");
                }
                System.out.println("Would you like to perform another activity?");
                performActivity = scanner.nextLine();

            } while ("Yes".equalsIgnoreCase(performActivity) || "y".equalsIgnoreCase(performActivity));
            /**
             * END OF SELLER IMPLEMENTATION
             */

        } else if (userType.equals("CUSTOMER")) {
            System.out.println("WELCOME CUSTOMER!");
            Customer customer = new Customer(email);
            String keepGoing = "";
            do {
                System.out.println("Customer Menu");
                System.out.println("1: Search the market and the various products it has to offer");
                System.out.println("2: View your shopping cart");
                System.out.println("3: View your purchase history");
                System.out.println("4: Enter a review for a product you have purchased");
                System.out.println("5: View your reward points balance");
                System.out.println("6: Redeem reward points");
                int choice3 = scanner.nextInt();
                scanner.nextLine();
                if (choice3 == 5) {
                    customer.displayRewardPoints();
                } else if (choice3 == 6) {
                    handleRewardRedemption(scanner, customer);
                } else {
                    switch (choice3) {
                    case 1:
                        System.out.println("On what basis would you like to search by?");
                        System.out.println("1. NAME");
                        System.out.println("2. PRICE");
                        System.out.println("3. STORE");
                        System.out.println("4. DESCRIPTION");
                        System.out.println("5. QUANTITY");
                        System.out.println("6. CATEGORY");
                        System.out.println("7. NO FILTERS, VIEW ENTIRE MARKETPLACE");
                        int choice4 = scanner.nextInt();
                        scanner.nextLine();
                        switch (choice4) {
                            case 1:
                                System.out.println("What is the name of the product you wish to search by:");
                                String searchName = scanner.nextLine();
                                
                                // Ask if they want to filter by category
                                System.out.println("Would you like to filter by category? (y/n)");
                                String filterByCategory = scanner.nextLine();
                                ProductCategory selectedCategory = null;
                                
                                if (filterByCategory.equalsIgnoreCase("y") || filterByCategory.equalsIgnoreCase("yes")) {
                                    System.out.println("Please select a category:");
                                    System.out.println("1. Shoes");
                                    System.out.println("2. Clothing");
                                    System.out.println("3. Accessories");
                                    System.out.println("4. Electronics");
                                    System.out.println("5. Home & Garden");
                                    System.out.println("6. Sports & Outdoors");
                                    System.out.println("7. Books & Media");
                                    
                                    int categoryChoice = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice >= 1 && categoryChoice <= 7) {
                                        selectedCategory = ProductCategory.values()[categoryChoice - 1];
                                    }
                                }
                                
                                try {
                                    ArrayList<Product> searchResults = ProductSearchService.searchByNameAsProducts(searchName);
                                    if (selectedCategory != null) {
                                        searchResults = ProductSearchService.filterByCategory(searchResults, selectedCategory);
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        System.out.println("No products found matching your criteria.");
                                    } else {
                                        System.out.println("Search Results:");
                                        for (Product product : searchResults) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error searching products: " + e.getMessage());
                                }
                                break;
                            case 2:
                                System.out.println("What is the threshold price of your search?");
                                double searchPrice = scanner.nextDouble();
                                scanner.nextLine();
                                
                                // Ask if they want to filter by category
                                System.out.println("Would you like to filter by category? (y/n)");
                                String filterByCategory2 = scanner.nextLine();
                                ProductCategory selectedCategory2 = null;
                                
                                if (filterByCategory2.equalsIgnoreCase("y") || filterByCategory2.equalsIgnoreCase("yes")) {
                                    System.out.println("Please select a category:");
                                    System.out.println("1. Shoes");
                                    System.out.println("2. Clothing");
                                    System.out.println("3. Accessories");
                                    System.out.println("4. Electronics");
                                    System.out.println("5. Home & Garden");
                                    System.out.println("6. Sports & Outdoors");
                                    System.out.println("7. Books & Media");
                                    
                                    int categoryChoice2 = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice2 >= 1 && categoryChoice2 <= 7) {
                                        selectedCategory2 = ProductCategory.values()[categoryChoice2 - 1];
                                    }
                                }
                                
                                try {
                                    ArrayList<Product> searchResults = ProductSearchService.searchByPriceAsProducts(searchPrice);
                                    if (selectedCategory2 != null) {
                                        searchResults = ProductSearchService.filterByCategory(searchResults, selectedCategory2);
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        System.out.println("No products found matching your criteria.");
                                    } else {
                                        System.out.println("Search Results:");
                                        for (Product product : searchResults) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error searching products: " + e.getMessage());
                                }
                                break;
                            case 3:
                                System.out.println("What is the name of the store you would like to search in?");
                                String searchStore = scanner.nextLine();
                                
                                // Ask if they want to filter by category
                                System.out.println("Would you like to filter by category? (y/n)");
                                String filterByCategory3 = scanner.nextLine();
                                ProductCategory selectedCategory3 = null;
                                
                                if (filterByCategory3.equalsIgnoreCase("y") || filterByCategory3.equalsIgnoreCase("yes")) {
                                    System.out.println("Please select a category:");
                                    System.out.println("1. Shoes");
                                    System.out.println("2. Clothing");
                                    System.out.println("3. Accessories");
                                    System.out.println("4. Electronics");
                                    System.out.println("5. Home & Garden");
                                    System.out.println("6. Sports & Outdoors");
                                    System.out.println("7. Books & Media");
                                    
                                    int categoryChoice3 = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice3 >= 1 && categoryChoice3 <= 7) {
                                        selectedCategory3 = ProductCategory.values()[categoryChoice3 - 1];
                                    }
                                }
                                
                                try {
                                    ArrayList<Product> searchResults = ProductSearchService.searchByStoreAsProducts(searchStore);
                                    if (selectedCategory3 != null) {
                                        searchResults = ProductSearchService.filterByCategory(searchResults, selectedCategory3);
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        System.out.println("No products found matching your criteria.");
                                    } else {
                                        System.out.println("Search Results:");
                                        for (Product product : searchResults) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error searching products: " + e.getMessage());
                                }
                                break;
                            case 4:
                                System.out.println("What is the description of product you wish to purchase?");
                                String searchDescription = scanner.nextLine();
                                
                                // Ask if they want to filter by category
                                System.out.println("Would you like to filter by category? (y/n)");
                                String filterByCategory4 = scanner.nextLine();
                                ProductCategory selectedCategory4 = null;
                                
                                if (filterByCategory4.equalsIgnoreCase("y") || filterByCategory4.equalsIgnoreCase("yes")) {
                                    System.out.println("Please select a category:");
                                    System.out.println("1. Shoes");
                                    System.out.println("2. Clothing");
                                    System.out.println("3. Accessories");
                                    System.out.println("4. Electronics");
                                    System.out.println("5. Home & Garden");
                                    System.out.println("6. Sports & Outdoors");
                                    System.out.println("7. Books & Media");
                                    
                                    int categoryChoice4 = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice4 >= 1 && categoryChoice4 <= 7) {
                                        selectedCategory4 = ProductCategory.values()[categoryChoice4 - 1];
                                    }
                                }
                                
                                try {
                                    ArrayList<Product> searchResults = ProductSearchService.searchByDescriptionAsProducts(searchDescription);
                                    if (selectedCategory4 != null) {
                                        searchResults = ProductSearchService.filterByCategory(searchResults, selectedCategory4);
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        System.out.println("No products found matching your criteria.");
                                    } else {
                                        System.out.println("Search Results:");
                                        for (Product product : searchResults) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error searching products: " + e.getMessage());
                                }
                                break;
                            case 5:
                                System.out.println("Displaying all the in-stock products:");
                                
                                // Ask if they want to filter by category
                                System.out.println("Would you like to filter by category? (y/n)");
                                String filterByCategory5 = scanner.nextLine();
                                ProductCategory selectedCategory5 = null;
                                
                                if (filterByCategory5.equalsIgnoreCase("y") || filterByCategory5.equalsIgnoreCase("yes")) {
                                    System.out.println("Please select a category:");
                                    System.out.println("1. Shoes");
                                    System.out.println("2. Clothing");
                                    System.out.println("3. Accessories");
                                    System.out.println("4. Electronics");
                                    System.out.println("5. Home & Garden");
                                    System.out.println("6. Sports & Outdoors");
                                    System.out.println("7. Books & Media");
                                    
                                    int categoryChoice5 = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice5 >= 1 && categoryChoice5 <= 7) {
                                        selectedCategory5 = ProductCategory.values()[categoryChoice5 - 1];
                                    }
                                }
                                
                                try {
                                    ArrayList<Product> searchResults = ProductSearchService.searchInStock();
                                    if (selectedCategory5 != null) {
                                        searchResults = ProductSearchService.filterByCategory(searchResults, selectedCategory5);
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        System.out.println("No in-stock products found matching your criteria.");
                                    } else {
                                        System.out.println("In-Stock Products:");
                                        for (Product product : searchResults) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error searching products: " + e.getMessage());
                                }
                                break;
                            case 6:
                                System.out.println("Please select a category to browse:");
                                System.out.println("1. Shoes");
                                System.out.println("2. Clothing");
                                System.out.println("3. Accessories");
                                System.out.println("4. Electronics");
                                System.out.println("5. Home & Garden");
                                System.out.println("6. Sports & Outdoors");
                                System.out.println("7. Books & Media");
                                
                                int categoryChoice6 = scanner.nextInt();
                                scanner.nextLine();
                                
                                if (categoryChoice6 >= 1 && categoryChoice6 <= 7) {
                                    ProductCategory browseCat = ProductCategory.values()[categoryChoice6 - 1];
                                    try {
                                        ArrayList<Product> categoryResults = ProductSearchService.searchByCategoryAsProducts(browseCat);
                                        
                                        if (categoryResults.isEmpty()) {
                                            System.out.println("No products found in the " + browseCat.getDisplayName() + " category.");
                                        } else {
                                            System.out.println("Products in " + browseCat.getDisplayName() + " category:");
                                            for (Product product : categoryResults) {
                                                System.out.println(product.toString());
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Error searching by category: " + e.getMessage());
                                    }
                                } else {
                                    System.out.println("Invalid category selection.");
                                }
                                break;
                            case 7:
                                System.out.println("Displaying the entire marketplace:");
                                try {
                                    // Use ProductSearchService for consistency with other search methods
                                    ArrayList<Product> allProducts = ProductSearchService.getAllProducts();
                                    
                                    if (allProducts.isEmpty()) {
                                        System.out.println("No products available in the marketplace.");
                                    } else {
                                        System.out.println("All Products in Marketplace:");
                                        for (Product product : allProducts) {
                                            System.out.println(product.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error displaying marketplace: " + e.getMessage());
                                }
                                break;
                            default:
                                System.out.println("Please enter a valid choice.");
                                break;
                        }
                        break;
                    case 2:
                        customer.viewCart(customer.getEmail());
                        System.out.println("Shopping Cart Menu");
                        System.out.println("1: Checkout");
                        System.out.println("2: Add item to the shopping cart");
                        int choice5 = scanner.nextInt();
                        scanner.nextLine();
                        switch (choice5) {
                            case 1:
                                
                                checkoutCart(customer);
                                persistMarketplaceToFile();
                                break;

                            
                            case 2:
                                System.out.println("Enter the name of the item you wish to add to the cart");
                                String item = scanner.nextLine();

                                boolean found = false;
                                Product foundProduct = null;

                                outerLoop:  // Label to break all loops
                                for (Seller sellerObj : sellers) {
                                    for (Store store : sellerObj.getStores()) {
                                        for (Product product : store.getProducts()) {
                                            if (product.getName().equalsIgnoreCase(item)) {
                                                foundProduct = product;
                                                found = true;
                                                break outerLoop;  // Exit all loops
                                            }
                                        }
                                    }
                                }

                                if (found) {
                                    int maxQty = foundProduct.getQuantity();
                                    int qtyToAdd = readIntInRange(scanner, "How many would you like? (1-" + maxQty + "): ", 1, maxQty);

                                    // Add a *cart item* with the purchase quantity (not the store inventory quantity)
                                    Product cartItem = new Product(
                                            foundProduct.getName(),
                                            qtyToAdd,
                                            foundProduct.getPrice(),
                                            foundProduct.getDescription(),
                                            foundProduct.getStore(),
                                            foundProduct.getCategory());

                                    customer.addToCart(cartItem);
                                    System.out.println("[OK] Added to cart: " + cartItem.getName() + " x" + qtyToAdd);
                                    System.out.println("Price each: $" + String.format("%.2f", cartItem.getPrice()));
                                } else {
                                    System.out.println("[X] Product '" + item + "' not found.");
                                    System.out.println("Search the marketplace first to see available products.");
                                }

                                break;
                            default:
                                System.out.println("Enter a valid choice!");
                        }
                        break;
                    case 3:
                        customer.viewOrderHistory(scanner);
                        break;
                    case 4:
                        System.out.println("Enter the name of the product whose review you want to give");
                        String productName = scanner.nextLine();
                        System.out.println("Enter the name of the store you bought it from");
                        String storebought = scanner.nextLine();
                        try {
                            int a = 0, b = 0, c = 0;
                            boolean trip = false;
                            BufferedReader br = new BufferedReader(new FileReader(customer.getEmail()));
                            String line2 = br.readLine();
                            while (line2 != null) {
                                String[] temp = line2.split(",");
                                if (temp[0].equals("Name: " + productName) && temp[4].equals("Store: " + storebought)) {
                                    trip = true;
                                }
                                if (trip) {
                                    System.out.println("Enter the review you would like to send");
                                    String review = scanner.nextLine();

                                    for (int i = 0; i < sellers.size(); i++) {
                                        for (int j = 0; j < sellers.get(i).getStores().size(); j++) {
                                            for (int k = 0; k < sellers.get(i).getStores().get(i).getProducts().size(); k++) {
                                                if (sellers.get(i).getStores().get(i).getProducts().get(i).getName().equals(productName) && sellers.get(i).getStores().get(i).getProducts().get(i).getStore().equals(storebought)) {
                                                    sellers.get(i).getStores().get(i).getProducts().get(i).addReview(customer.getEmail(), review);
                                                    a = i;
                                                    b = j;
                                                    c = k;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            if (!trip) {
                                System.out.println("You havent bought that product!!");
                            } else {
                                PrintWriter pw = new PrintWriter(new FileWriter(sellers.get(a).getEmail()));
                                BufferedReader bfr = new BufferedReader(new FileReader(sellers.get(a).getEmail()));
                                String first = bfr.readLine();
                                pw.write(first + "\n");
                                for (int i = 0; i < sellers.get(a).getStores().size(); i++) {
                                    pw.append(sellers.get(a).getStores().get(i).toString() + "\n");
                                    for (int j = 0; j < sellers.get(a).getStores().get(i).getProducts().size(); i++) {
                                        pw.append(sellers.get(a).getStores().get(i).getProducts().get(j).toString() + "\n");
                                    }
                                }
                                pw.close();
                                bfr.close();
                            }
                            br.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    
                }
                }
                System.out.println("Do you want to perform another activity. (yes/no)");
                keepGoing = scanner.nextLine();
            } while (keepGoing.equalsIgnoreCase("yes") || keepGoing.equalsIgnoreCase("y"));
        }
        
        // Close database connection on application exit
        try {
            DatabaseManager.closeConnection();
            System.out.println("Application shutting down. Goodbye!");
        } catch (Exception e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    private static String promptLimitedText(Scanner scanner, String prompt, int maxLength, boolean allowEmpty) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            if (input == null) {
                input = "";
            }
            input = input.trim();
            if (input.isEmpty() && !allowEmpty) {
                System.out.println("This field cannot be empty.");
                continue;
            }
            if (input.length() > maxLength) {
                System.out.println("Input too long. It will be truncated to " + maxLength + " characters.");
                input = input.substring(0, maxLength);
            }
            return input;
        }
    }

    private static String promptUpdatedText(Scanner scanner, String prompt, String currentValue, int maxLength) {
        System.out.println(prompt + " (press Enter to keep \"" + currentValue + "\")");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return currentValue;
        }
        if (input.length() > maxLength) {
            System.out.println("Input too long. It will be truncated to " + maxLength + " characters.");
            input = input.substring(0, maxLength);
        }
        return input;
    }

    private static int promptNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println("Please enter a value of 0 or greater.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }

    private static int promptUpdatedInt(Scanner scanner, String prompt, int currentValue) {
        System.out.println(prompt + " (current value: " + currentValue + ", press Enter to keep current value)");
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println("Please enter a value of 0 or greater.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }

    private static double promptPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Please enter a value greater than 0.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }

    private static double promptUpdatedDouble(Scanner scanner, String prompt, double currentValue) {
        System.out.println(prompt + " (current value: " + currentValue + ", press Enter to keep current value)");
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentValue;
            }
            try {
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Please enter a value greater than 0.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }

    private static ProductCategory promptForCategorySelection(Scanner scanner) {
        System.out.println("Please select a category:");
        for (int i = 0; i < ProductCategory.values().length; i++) {
            System.out.println((i + 1) + ". " + ProductCategory.values()[i].getDisplayName());
        }
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= ProductCategory.values().length) {
                    return ProductCategory.values()[choice - 1];
                }
            } catch (NumberFormatException e) {
                // ignored, prompt again
            }
            System.out.println("Please enter a number between 1 and " + ProductCategory.values().length + ".");
        }
    }

    private static ProductCategory promptUpdatedCategory(Scanner scanner, ProductCategory currentCategory) {
        System.out.println("Press Enter to keep the current category (" + currentCategory.getDisplayName() + ") or enter a new category number:");
        for (int i = 0; i < ProductCategory.values().length; i++) {
            System.out.println((i + 1) + ". " + ProductCategory.values()[i].getDisplayName());
        }
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentCategory;
            }
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= ProductCategory.values().length) {
                    return ProductCategory.values()[choice - 1];
                }
            } catch (NumberFormatException e) {
                // ignored
            }
            System.out.println("Please enter a number between 1 and " + ProductCategory.values().length + " or press Enter to keep the current category.");
        }
    }

    private static String promptChangeReason(Scanner scanner) {
        while (true) {
            System.out.println("Please enter a reason for this change:");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("A reason is required to log this change.");
                continue;
            }
            if (input.length() > MAX_CHANGE_REASON_LENGTH) {
                System.out.println("Reason too long. It will be truncated to " + MAX_CHANGE_REASON_LENGTH + " characters.");
                input = input.substring(0, MAX_CHANGE_REASON_LENGTH);
            }
            return input;
        }
    }

    private static void handleRewardRedemption(Scanner scanner, Customer customer) {
        int points = promptNonNegativeInt(scanner, "How many points would you like to redeem?");
        if (points <= 0) {
            System.out.println("Please enter a positive number of points.");
            return;
        }
        double discount = customer.redeemRewardPoints(points);
        if (discount > 0) {
            System.out.println("Redemption successful. Discount applied: $" + String.format("%.2f", discount));
        } else {
            System.out.println("Unable to redeem points. Please ensure you have enough points.");
        }
    }

    private static class AccountRecord {
        final String email;
        final String password;
        final String role;

        AccountRecord(String email, String password, String role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    private static ArrayList<AccountRecord> loadAccountRecords() {
        ArrayList<AccountRecord> accounts = new ArrayList<>();
        File accountsFile = new File(FileConstants.ACCOUNTS_FILE);
        if (!accountsFile.exists()) {
            return accounts;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(accountsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    accounts.add(new AccountRecord(parts[0].trim(), parts[1].trim(), normalizeRole(parts[2])));
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read the accounts file: " + e.getMessage());
        }

        return accounts;
    }

    private static AccountRecord attemptLogin(Scanner scanner) {
        ArrayList<AccountRecord> accounts = loadAccountRecords();
        if (accounts.isEmpty()) {
            System.out.println("No accounts exist yet. Please create an account first.");
            return null;
        }

        Map<String, AccountRecord> lookup = new HashMap<>();
        for (AccountRecord account : accounts) {
            lookup.put(account.email.toLowerCase(), account);
        }

        int attempts = 0;
        while (attempts < 3) {
            System.out.println(ENTER_YOUR_EMAIL);
            String emailInput = scanner.nextLine().trim();
            if (emailInput.isEmpty()) {
                System.out.println("E-mail is required.");
                attempts++;
                continue;
            }

            AccountRecord record = lookup.get(emailInput.toLowerCase());
            if (record == null) {
                System.out.println("This e-mail does not exist in our database.");
                attempts++;
                continue;
            }

            System.out.println(LOGIN_PASSWORD_PROMPT);
            String passwordInput = scanner.nextLine();
            if (passwordInput == null || passwordInput.trim().isEmpty()) {
                System.out.println("Password is required.");
                attempts++;
                continue;
            }

            if (!record.password.equals(passwordInput)) {
                System.out.println("Incorrect password.");
                attempts++;
                continue;
            }

            System.out.println("Login successful!");
            ensureUserExistsInDatabase(record.email, record.password, record.role);
            return record;
        }

        System.out.println("Maximum login attempts reached. Returning to the main menu.");
        return null;
    }

    private static AccountRecord handleAccountCreation(Scanner scanner) {
        ArrayList<AccountRecord> accounts = loadAccountRecords();
        String email = promptForUniqueEmail(scanner, accounts);
        String password = promptForPassword(scanner);
        String role = promptForUserType(scanner);

        AccountRecord record = new AccountRecord(email, password, role);
        if (!saveAccountRecord(record)) {
            return null;
        }

        if ("SELLER".equals(role)) {
            Seller seller = new Seller(email);
            sellers.add(seller);
            appendSellerRecord(email);
        }

        ensureUserExistsInDatabase(record.email, record.password, record.role);
        return record;
    }

    private static String promptForUniqueEmail(Scanner scanner, ArrayList<AccountRecord> accounts) {
        while (true) {
            System.out.println(ENTER_YOUR_EMAIL);
            String emailInput = scanner.nextLine().trim();

            if (!isValidEmail(emailInput)) {
                System.out.println("Invalid e-mail.");
                continue;
            }

            boolean exists = false;
            for (AccountRecord record : accounts) {
                if (record.email.equalsIgnoreCase(emailInput)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                System.out.println("This e-mail has already been taken.");
                continue;
            }

            return emailInput;
        }
    }

    private static String promptForPassword(Scanner scanner) {
        while (true) {
            System.out.println(CREATE_PASSWORD_PROMPT);
            String passwordInput = scanner.nextLine();
            if (passwordInput != null && passwordInput.length() > 5) {
                return passwordInput;
            }
            System.out.println("Invalid password. Please enter a valid Password:");
        }
    }

    private static String promptForUserType(Scanner scanner) {
        System.out.println(BUYER_OR_SELLER);
        while (true) {
            String input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                return "CUSTOMER";
            } else if ("2".equals(input)) {
                return "SELLER";
            }
            System.out.println("Please enter either 1 or 2.");
        }
    }

    private static boolean saveAccountRecord(AccountRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileConstants.ACCOUNTS_FILE, true))) {
            writer.write(record.email + "," + record.password + "," + record.role + System.lineSeparator());
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to the accounts file.");
            return false;
        }
    }

    private static void appendSellerRecord(String email) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileConstants.SELLERS_FILE, true))) {
            writer.write(email + "," + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error writing to the seller file.");
        }
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.contains("@") &&
            (email.endsWith(".com") || email.endsWith(".edu") || email.endsWith(".gov"));
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "CUSTOMER";
        }
        role = role.trim().toUpperCase();
        return "SELLER".equals(role) ? "SELLER" : "CUSTOMER";
    }

    

    // -------------------------
    // Shopping cart + checkout
    // -------------------------

    private static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(raw);
                if (val < min || val > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static Store findStoreByName(String storeName) {
        if (storeName == null) return null;
        for (Seller s : sellers) {
            for (Store st : s.getStores()) {
                if (st.getName().equalsIgnoreCase(storeName)) {
                    return st;
                }
            }
        }
        return null;
    }

    private static void checkoutCart(Customer customer) {
        ArrayList<Product> cart = customer.getShoppingCart();

        if (cart == null || cart.isEmpty()) {
            System.out.println("There is nothing in your shopping cart.");
            return;
        }

        System.out.println("\n--- Checkout ---");
        double total = 0.0;

        // First, validate all items exist + in stock
        for (Product item : cart) {
            total += item.getPrice() * item.getQuantity();
        }

        // Process purchases (updates inventory + reward points)
        for (Product item : cart) {
            Store store = findStoreByName(item.getStore());
            store.processPurchase(item.getName(), item.getQuantity(), customer);
        }

        // Record order in DB (Order + OrderDetails)
        customer.processPurchase(total);

        // Write purchase history file + clear cart
        customer.writePurchaseHistory(new ArrayList<>(cart));
        customer.clearShoppingCart();

        System.out.println("\n[OK] Checkout complete!");
        System.out.println("Total charged: $" + String.format("%.2f", total));
        System.out.println("Reward points earned: " + (int) Math.floor(total) + " (1 point per $1)\n");
    }

    /**
     * Persist the in-memory marketplace (sellers/stores/products) back to Sellers.txt
     * so quantity changes from checkout remain after restarting the program.
     */
    private static void persistMarketplaceToFile() {
        try {
            FileConstants.ensureDataDir();
            try (PrintWriter pw = new PrintWriter(new FileWriter(FileConstants.SELLERS_FILE, false))) {
                for (Seller seller : sellers) {
                    for (Store store : seller.getStores()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(seller.getEmail()).append(",").append(store.getName());
                        for (Product product : store.getProducts()) {
                            sb.append(",").append(product.toCSV());
                        }
                        pw.println(sb.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Unable to persist Sellers file: " + e.getMessage());
        }
    }

private static void ensureUserExistsInDatabase(String username, String password, String role) {
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(username);
            if (userId <= 0) {
                userDAO.createUser(username, password, username, role.toLowerCase());
            }
        } catch (Exception e) {
            System.err.println("Warning: Unable to sync account with database: " + e.getMessage());
        }
    }
}