import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Scanner;
import java.io.*;
import java.util.function.BiFunction;

public class Marketplace {
    public static final String WELCOME_PROMPT = "Welcome to our Marketplace!";
    public static final String ENTER_YOUR_EMAIL = "Please enter your e-mail.";
    public static final String SIGN_IN_PROMPT = "1: Sign In\n2: Create an account.";

    public static final String LOGIN_PASSWORD_PROMPT = "Please enter your password.";

    public static final String CREATE_PASSWORD_PROMPT = "Please enter a password greater than 5 characters.";

    public static final String BUYER_OR_SELLER = "1: Customer\n2: Seller";

    private static ArrayList<Seller> sellers = new ArrayList<>();

    public static void loadMarket() {
        File f = new File("Sellers.txt");
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
                        
                        // Create store
                        Store store = new Store(storeName);
                        
                        // Parse products using migration service
                        try {
                            ArrayList<Product> products = DataMigrationService.parseProductsFromLine(currentLine);
                            for (Product product : products) {
                                store.addProduct(product);
                            }
                        } catch (Exception e) {
                            System.out.println("Warning: Could not parse products from line: " + currentLine);
                            System.out.println("Error: " + e.getMessage());
                            // Continue processing other lines
                        }
                        
                        seller.addStores(store);
                        
                    } else if (arr.length == 2) {
                        // Line contains only seller and store info (no products)
                        String sellerEmail = arr[0].replaceAll(",", "");
                        String storeName = arr[1].replaceAll(",", "");
                        
                        // Find or create seller
                        Seller seller = findOrCreateSeller(sellerEmail);
                        
                        // Create empty store
                        Store store = new Store(storeName);
                        seller.addStores(store);
                        
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
                System.out.println("There was an error creating the sellers file.");
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
     * Migrates the data file from legacy format to new format.
     * @param lines All lines from the original file
     */
    private static void migrateDataFile(ArrayList<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Sellers.txt"))) {
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
        String email = "";
        String password = "";
        String userType = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println(WELCOME_PROMPT);
        System.out.println(SIGN_IN_PROMPT);
        String response = scanner.nextLine();


        while (!"1".equals(response) && !"2".equals(response)) {
            System.out.println("Please either select 1 or 2.");
            response = scanner.nextLine();
        }


        if ("1".equals(response)) { // sign in
            System.out.println(ENTER_YOUR_EMAIL);
            email = scanner.nextLine();

            System.out.println(LOGIN_PASSWORD_PROMPT);
            password = scanner.nextLine();

            File f = new File("Accounts.txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                ArrayList<String> accountInfo = new ArrayList<>();
                String line = "";
                while ((line = br.readLine()) != null) {
                    accountInfo.add(line);
                }
                br.close();
                ArrayList<String> emails = new ArrayList<>();
                for (int i = 0; i < accountInfo.size(); i++) {
                    emails.add(accountInfo.get(i).split(",")[0]);
                }
                while (!emails.contains(email)) {
                    System.out.println("This e-mail does not exist in our database.");
                    System.out.println(ENTER_YOUR_EMAIL);
                    email = scanner.nextLine();
                }
                int index = emails.indexOf(email);
                while (!accountInfo.get(index).split(",")[1].equals(password)) {
                    System.out.println("Incorrect Password.");
                    System.out.println(LOGIN_PASSWORD_PROMPT);
                    password = scanner.nextLine();
                }
                System.out.println("Login successful!");


                if (accountInfo.get(index).split(",")[2].equals("SELLER")) {
                    userType = "SELLER";
                } else {
                    userType = "CUSTOMER";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else { // create account
            System.out.println(ENTER_YOUR_EMAIL);
            email = scanner.nextLine();

            while (!email.contains("@") && (!email.contains(".com") || !email.contains(".edu") || !email.contains(".gov"))) {
                System.out.println("Invalid e-mail.");
                System.out.println(ENTER_YOUR_EMAIL);
                email = scanner.nextLine();
            }

            File f = new File("Accounts.txt");
            if (f.exists()) {
                try (BufferedReader bfr = new BufferedReader(new FileReader("Accounts.txt"))) {
                    String line = "";
                    while ((line = bfr.readLine()) != null) {
                        String[] arr = line.split(",");
                        while (arr[0].equals(email)) {
                            System.out.println("This e-mail has already been taken.");
                            System.out.println(ENTER_YOUR_EMAIL);
                            email = scanner.nextLine();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Unable to read to the accounts file.");
                }

                System.out.println(CREATE_PASSWORD_PROMPT);
                password = scanner.nextLine();
                if (password == null || password.length() <= 5) { // case of invalid password
                    boolean validPassword = false;
                    while (!validPassword) {
                        System.out.println("Invalid password. Please enter a valid Password:");
                        password = scanner.nextLine();
                        if (password != null && password.length() > 5) {
                            validPassword = true;
                        }
                    }
                }

                System.out.println(BUYER_OR_SELLER);
                userType = scanner.nextLine();
                while (!"1".equals(userType) && !"2".equals(userType)) {
                    System.out.println("Please enter either 1 or 2.");
                }
                if ("1".equals(userType)) {
                    userType = "CUSTOMER";
                } else {
                    userType = "SELLER";
                    Seller seller = new Seller(email);
                    sellers.add(seller); // hope it works
                }


                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Accounts.txt", true))) {
                    bwr.write(email + "," + password + "," + userType + "\n");
                } catch (IOException io) {
                    System.out.println("Error writing to the accounts file.");
                }

                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt", true))) {
                    bwr.write(email + "," + "\n");
                } catch (IOException io) {
                    System.out.println("Error writing to the seller file.");
                }
            } else {
                f = new File("Accounts.txt");
                System.out.println(CREATE_PASSWORD_PROMPT);
                password = scanner.nextLine();
                if (password == null || password.length() <= 5) { // case of invalid password
                    boolean validPassword = false;
                    while (!validPassword) {
                        System.out.println("Invalid password. Please enter a valid Password:");
                        password = scanner.nextLine();
                        if (password != null && password.length() > 5) {
                            validPassword = true;
                        }
                    }
                }

                System.out.println(BUYER_OR_SELLER);
                userType = scanner.nextLine();
                while (!"1".equals(userType) && !"2".equals(userType)) {
                    System.out.println("Please enter either 1 or 2.");
                }
                if ("1".equals(userType)) {
                    userType = "CUSTOMER";
                } else {
                    userType = "SELLER";
                    Seller seller = new Seller(email);
                    sellers.add(seller); // hope it works
                }
                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Accounts.txt", true))) {
                    bwr.write(email + "," + password + "," + userType + "\n");
                } catch (IOException e) {
                    System.out.println("Error writing to the accounts file.");
                }
                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt", true))) {
                    bwr.write(email + "," + "\n");
                } catch (IOException io) {
                    System.out.println("Error writing to the seller file.");
                }
            }
        }
        
        loadMarket();




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
                System.out.println("6: View a customer shopping cart");
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
                            System.out.println("What is the name of your product?");
                            String productName = scanner.nextLine();
                            System.out.println("How many products do you want to manufacture?");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What will the price of your product be?");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of your product?");
                            String description = scanner.nextLine();
                            
                            // Category selection
                            System.out.println("Please select a category for your product:");
                            System.out.println("1. Shoes");
                            System.out.println("2. Clothing");
                            System.out.println("3. Accessories");
                            System.out.println("4. Electronics");
                            System.out.println("5. Home & Garden");
                            System.out.println("6. Sports & Outdoors");
                            System.out.println("7. Books & Media");
                            
                            int categoryChoice = 0;
                            boolean validCategory = false;
                            while (!validCategory) {
                                try {
                                    categoryChoice = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice >= 1 && categoryChoice <= 7) {
                                        validCategory = true;
                                    } else {
                                        System.out.println("Please enter a number between 1 and 7.");
                                    }
                                } catch (Exception e) {
                                    System.out.println("Please enter a valid number between 1 and 7.");
                                    scanner.nextLine(); // Clear invalid input
                                }
                            }
                            
                            ProductCategory category = ProductCategory.values()[categoryChoice - 1];
                            sellers.get(index).writeToSellerFileAddProduct(storeName, productName, quantity, price, description, category);
                        } else {
                            System.out.println("Sorry, you are not affiliated with " + storeName);
                        }
                        break;
                    case 3:
                        System.out.println("What is the name of the store you would to remove a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            System.out.println("What was the name of the product?");
                            String productName = scanner.nextLine();
                            System.out.println("How many of these products were manufactured (quantity).");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What was the price of your product?");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What was the description of your product?");
                            String description = scanner.nextLine();
                            sellers.get(index).writeToSellerFileRemoveProduct(storeName, productName, quantity, price, description);
                        }
                        break;
                    case 4:
                        System.out.println("What is the name of the store you would like to edit a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            System.out.println("What is the name of your old product?");
                            String productName = scanner.nextLine();
                            System.out.println("What was the quantity of the old product");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What was the price of the old product");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of the old product");
                            String description = scanner.nextLine();

                            System.out.println("What do you want the new name of the product to be?");
                            String newProductName = scanner.nextLine();
                            System.out.println("What is the new quantity?");
                            int newQuantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What is the new price?");
                            double newPrice = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the new description?");
                            String newDescription = scanner.nextLine();
                            
                            // Category selection for new product
                            System.out.println("Please select a new category for your product:");
                            System.out.println("1. Shoes");
                            System.out.println("2. Clothing");
                            System.out.println("3. Accessories");
                            System.out.println("4. Electronics");
                            System.out.println("5. Home & Garden");
                            System.out.println("6. Sports & Outdoors");
                            System.out.println("7. Books & Media");
                            
                            int categoryChoice = 0;
                            boolean validCategory = false;
                            while (!validCategory) {
                                try {
                                    categoryChoice = scanner.nextInt();
                                    scanner.nextLine();
                                    if (categoryChoice >= 1 && categoryChoice <= 7) {
                                        validCategory = true;
                                    } else {
                                        System.out.println("Please enter a number between 1 and 7.");
                                    }
                                } catch (Exception e) {
                                    System.out.println("Please enter a valid number between 1 and 7.");
                                    scanner.nextLine(); // Clear invalid input
                                }
                            }
                            
                            ProductCategory newCategory = ProductCategory.values()[categoryChoice - 1];
                            // For editing, we assume the old product was SHOES category (backward compatibility)
                            ProductCategory oldCategory = ProductCategory.SHOES;
                            sellers.get(index).writerToSellerFileEditProduct(productName, quantity, price, description, oldCategory, storeName,
                                    newProductName, newQuantity, newPrice, newDescription, newCategory);
                        }
                        break;
                    case 5:
                        for (Store store : sellers.get(index).getStores()) {
                            System.out.println(store.toString());
                        }
                        break;
                    case 6:
                        System.out.println("");
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
                int choice3 = scanner.nextInt();
                scanner.nextLine();
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
                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(email));
                                    ArrayList<String> lines = new ArrayList<>();
                                    ArrayList<String> cart = new ArrayList<>();
                                    String line = "";
                                    boolean trip = false;
                                    while ((line = br.readLine()) != null) {
                                        lines.add(line);
                                        if (line.equals("-------")) {
                                            trip = true;
                                        }
                                        if (trip) {
                                            cart.add(line);
                                        }
                                    }
                                    ArrayList<Product> passCart = new ArrayList<>();
                                    for (int i = 0; i < cart.size(); i++) {
                                        String[] temp = cart.get(i).split(",");
                                        Product product = new Product(temp[0], Integer.parseInt(temp[1]), Double.parseDouble(temp[2]), temp[3], temp[4]);
                                        passCart.add(product);
                                    }
                                    for (int i = 0; i < passCart.size(); i++) {
                                        for (int j = 0; j < sellers.size(); j++) {
                                            for (int k = 0; k < sellers.get(i).getStores().size(); k++) {
                                                if (sellers.get(i).getStores().get(k).equals(passCart.get(i).getStore())) {
                                                    sellers.get(i).getStores().get(k).processPurchase(passCart.get(i).getName(), passCart.get(i).getQuantity(), customer);
                                                }
                                            }
                                        }
                                    }
                                    boolean trip1 = false;
                                    PrintWriter pw = new PrintWriter(new FileWriter(customer.getEmail(), true));
                                    for (int i = 0; i < lines.size(); i++) {
                                        if (lines.get(i).equals("-------")) {
                                            trip1 = true;
                                        }
                                        if (!trip1) {
                                            pw.append(lines.get(i));
                                        }
                                    }
                                    for (int i = 0; i < passCart.size(); i++) {
                                        pw.append(passCart.get(i).toString());
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                System.out.println("Enter the name of the item you wish to add to the cart");
                                String item = scanner.nextLine();

                                for (int i = 0; i < sellers.size(); i++) {
                                    for (int j = 0; j < sellers.get(i).getStores().size(); j++) {
                                        for (int k = 0; k < sellers.get(i).getStores().get(i).getProducts().size(); k++) {
                                            if (sellers.get(i).getStores().get(i).getProducts().get(k).getName().equals(item)) {
                                                Product product = sellers.get(i).getStores().get(i).getProducts().get(k);
                                                customer.addToCart(product);
                                            }
                                        }
                                    }
                                }
                                break;
                            default:
                                System.out.println("Enter a valid choice!");
                        }
                        break;
                    case 3:
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(customer.getEmail()));
                            System.out.println(customer.getEmail() + "'s purchase history: \n");
                            String line = br.readLine();
                            boolean trip = false;
                            while ((line = br.readLine()) != null) {
                                if (line.equals("-------")) {
                                    trip = true;
                                }
                                if (!trip) {
                                    System.out.println(line);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
                System.out.println("Do you want to perform another activity. (yes/no)");
                keepGoing = scanner.nextLine();
            } while (keepGoing.equalsIgnoreCase("yes") || keepGoing.equalsIgnoreCase("y"));
        }


    }
}

