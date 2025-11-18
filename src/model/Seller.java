import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Seller {
    private ArrayList<Store> stores = new ArrayList<>();
    private String email;
    private ProductDAO productDAO;

    public Seller(String email) {
        this.email = email;
        this.productDAO = new ProductDAO();
    }
    
    public Seller(String email, boolean useDatabase) {
        this.email = email;
        if (useDatabase) {
            this.productDAO = new ProductDAO();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.email = name;
    }

    public ArrayList<Store> getStores() {
        return stores;
    }

    public boolean checkIfStoreExists(String storeName) {
        for (Store store : stores) {
            if (store.getName().equalsIgnoreCase(storeName)) {
                return true;
            }
        }
        return false;
    }

    public void addStores(String storeName) {
        Store store = new Store(storeName);
        this.stores.add(store);
    }
    public void addStores(Store store) {
        this.stores.add(store);
    }



    public void createProduct(String storeName, String productName,
                              int quantity, double price, String description, ProductCategory category) {
        // Validate category
        if (category == null) {
            System.out.println("Error: Product category cannot be null");
            return;
        }
        
        int count = 0;
        for (Store s : stores) {
            if (s.getName().equalsIgnoreCase(storeName)) {
                int index = stores.indexOf(s);
                stores.get(index).addProduct(new Product(productName, quantity, price, description, storeName, category));
            }
            count++;
        }
        if (count == 0) {
            System.out.println("You are not affiliated with " + storeName);
        }
    }
    
    /**
     * Creates a product using database storage
     * @param storeName store name
     * @param productName product name
     * @param quantity product quantity
     * @param price product price
     * @param description product description
     * @param category product category
     * @param userId user ID for change logging
     * @return product ID if successful, -1 otherwise
     */
    public int createProductDB(String storeName, String productName,
                               int quantity, double price, String description, 
                               ProductCategory category, int userId) {
        // Validate category
        if (category == null) {
            System.out.println("Error: Product category cannot be null");
            return -1;
        }
        
        // Check if seller is affiliated with the store
        if (!checkIfStoreExists(storeName)) {
            System.out.println("You are not affiliated with " + storeName);
            return -1;
        }
        
        // Add product to database
        int productId = productDAO.addProduct(productName, category.name(), price, quantity, storeName, description);
        
        if (productId > 0) {
            // Also add to in-memory store for backward compatibility
            for (Store s : stores) {
                if (s.getName().equalsIgnoreCase(storeName)) {
                    int index = stores.indexOf(s);
                    stores.get(index).addProduct(new Product(productName, quantity, price, description, storeName, category));
                    break;
                }
            }
        }
        
        return productId;
    }

    // Backward-compatible method that defaults to SHOES category
    public void createProduct(String storeName, String productName,
                              int quantity, double price, String description) {
        createProduct(storeName, productName, quantity, price, description, ProductCategory.SHOES);
    }


    public void removeProduct(String storeName, Product product) {
        for (Store s : stores) {
            if (s.getName().equalsIgnoreCase(storeName)) {
                if (s.getProducts().size() != 0) {
                    int index = stores.indexOf(s);
                    stores.get(index).removeProduct(product);
                } else {
                    System.out.println("There are no products in this store.");
                }

            }
        }
        // whenever this method is called, UPDATE SELLER FILE WITH WRITE TO SELLER
    }
    
    /**
     * Removes a product using database storage
     * @param productId product ID to remove
     * @param storeName store name
     * @param product product object for in-memory removal
     * @return true if successful, false otherwise
     */
    public boolean removeProductDB(int productId, String storeName, Product product) {
        // Check if seller is affiliated with the store
        if (!checkIfStoreExists(storeName)) {
            System.out.println("You are not affiliated with " + storeName);
            return false;
        }
        
        // Remove from database
        boolean success = productDAO.deleteProduct(productId);
        
        if (success) {
            // Also remove from in-memory store for backward compatibility
            for (Store s : stores) {
                if (s.getName().equalsIgnoreCase(storeName)) {
                    if (s.getProducts().size() != 0) {
                        int index = stores.indexOf(s);
                        stores.get(index).removeProduct(product);
                    }
                    break;
                }
            }
        }
        
        return success;
    }

    public void editProduct(Product oldProduct, String newName, String newDesc, String storeName, int newQuantity, double newPrice, ProductCategory newCategory) {
        // Validate category
        if (newCategory == null) {
            System.out.println("Error: Product category cannot be null");
            return;
        }
        
        Product newProduct = new Product(newName, newQuantity, newPrice, newDesc, storeName, newCategory);
        for (Store s : stores) {
            if (s.getName().equals(storeName)) {
                int index = stores.indexOf(s);
                stores.get(index).setProduct(oldProduct, newProduct);
            }
        }
    }
    
    /**
     * Edits a product using database storage with change logging
     * @param productId product ID to edit
     * @param oldProduct old product data
     * @param newName new product name
     * @param newDesc new description
     * @param storeName store name
     * @param newQuantity new quantity
     * @param newPrice new price
     * @param newCategory new category
     * @param userId user ID for change logging
     * @return true if successful, false otherwise
     */
    public boolean editProductDB(int productId, Product oldProduct, String newName, String newDesc, 
                                 String storeName, int newQuantity, double newPrice, 
                                 ProductCategory newCategory, int userId) {
        // Validate category
        if (newCategory == null) {
            System.out.println("Error: Product category cannot be null");
            return false;
        }
        
        // Check if seller is affiliated with the store
        if (!checkIfStoreExists(storeName)) {
            System.out.println("You are not affiliated with " + storeName);
            return false;
        }
        
        // Track changes for logging
        boolean success = true;
        
        // Update name if changed
        if (!oldProduct.getName().equals(newName)) {
            success &= productDAO.updateProduct(productId, "name", oldProduct.getName(), newName, userId);
        }
        
        // Update description if changed
        if (!oldProduct.getDescription().equals(newDesc)) {
            success &= productDAO.updateProduct(productId, "description", oldProduct.getDescription(), newDesc, userId);
        }
        
        // Update price if changed
        if (oldProduct.getPrice() != newPrice) {
            success &= productDAO.updateProduct(productId, "price", 
                String.valueOf(oldProduct.getPrice()), String.valueOf(newPrice), userId);
        }
        
        // Update quantity if changed
        if (oldProduct.getQuantity() != newQuantity) {
            success &= productDAO.updateProduct(productId, "quantity", 
                String.valueOf(oldProduct.getQuantity()), String.valueOf(newQuantity), userId);
        }
        
        // Update category if changed
        if (oldProduct.getCategory() != newCategory) {
            success &= productDAO.updateProduct(productId, "category", 
                oldProduct.getCategory().name(), newCategory.name(), userId);
        }
        
        if (success) {
            // Also update in-memory store for backward compatibility
            Product newProduct = new Product(newName, newQuantity, newPrice, newDesc, storeName, newCategory);
            for (Store s : stores) {
                if (s.getName().equals(storeName)) {
                    int index = stores.indexOf(s);
                    stores.get(index).setProduct(oldProduct, newProduct);
                    break;
                }
            }
        }
        
        return success;
    }

    // Backward-compatible method that preserves the original category
    public void editProduct(Product oldProduct, String newName, String newDesc, String storeName, int newQuantity, double newPrice) {
        ProductCategory originalCategory = oldProduct.getCategory();
        editProduct(oldProduct, newName, newDesc, storeName, newQuantity, newPrice, originalCategory);
    }

    public void writerToSellerFileEditProduct(String oldName, int oldQuantity, double oldPrice, String oldDesc, ProductCategory oldCategory, String storeName,
                                              String newName, int newQuantity, double newPrice, String newDesc, ProductCategory newCategory) {
        // Validate categories
        if (oldCategory == null || newCategory == null) {
            System.out.println("Error: Product categories cannot be null");
            return;
        }
        
        removeProduct(storeName, new Product(oldName, oldQuantity, oldPrice, oldDesc, storeName, oldCategory));
        createProduct(storeName, newName, newQuantity, newPrice, newDesc, newCategory);
        ArrayList<String> otherSellers = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < stores.size(); i++) {
            if (stores.get(i).getName().equalsIgnoreCase(storeName)) {
                index = i;
            }
        }
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line = "";
            while ((line = bfr.readLine()) != null) {
                if (!line.startsWith(this.email) || !line.contains(storeName)) {
                    otherSellers.add(line);
                }
            }
            bfr.close();
        } catch (IOException e) {
            System.out.println();
        }
        try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt"))) {
            for (int i = 0; i < otherSellers.size(); i++) {
                bwr.write(otherSellers.get(i) + "\n");
            }

            bwr.write(this.email + "," + stores.get(index).toString());

            bwr.flush();
        } catch (IOException e) {
            System.out.println();
        }
    }

    // Backward-compatible method that defaults to SHOES category for both old and new products
    public void writerToSellerFileEditProduct(String oldName, int oldQuantity, double oldPrice, String oldDesc, String storeName,
                                              String newName, int newQuantity, double newPrice, String newDesc) {
        writerToSellerFileEditProduct(oldName, oldQuantity, oldPrice, oldDesc, ProductCategory.SHOES, storeName,
                                      newName, newQuantity, newPrice, newDesc, ProductCategory.SHOES);
    }
    public Store searchStore(String storeName) {
        for (Store s : stores) {
            if (s.getName().equalsIgnoreCase(storeName)) {
                int index = stores.indexOf(s);
                return stores.get(index);
            }
        }
        return null;
    }
    public void writeToSellerFileAddProduct(String storeName, String name, int quantity, double price, String description, ProductCategory category) {
        // Validate category before creating product
        if (category == null) {
            System.out.println("Error: Product category cannot be null");
            return;
        }
        
        createProduct(storeName, name, quantity, price, description, category);
        ArrayList<String> otherSellers = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < stores.size(); i++) {
            if (stores.get(i).getName().equalsIgnoreCase(storeName)) {
                index = i;
            }
        }
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line = "";
            while ((line = bfr.readLine()) != null) {
                if (!line.startsWith(this.email) || !line.contains(storeName)) {
                    otherSellers.add(line);
                }
            }
            bfr.close();
        } catch (IOException e) {
            System.out.println();
        }
        try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt"))) {
            for (int i = 0; i < otherSellers.size(); i++) {
                bwr.write(otherSellers.get(i) + "\n");
            }

            bwr.write(this.email + "," + stores.get(index).toString());

            bwr.flush();
        } catch (IOException e) {
            System.out.println();
        }
    }

    // Backward-compatible method that defaults to SHOES category
    public void writeToSellerFileAddProduct(String storeName, String name, int quantity, double price, String description) {
        writeToSellerFileAddProduct(storeName, name, quantity, price, description, ProductCategory.SHOES);
    }

    public void writeToSellerFileRemoveProduct(String storeName, String productName, int quantity, double price, String description, ProductCategory category) {
        // Validate category
        if (category == null) {
            System.out.println("Error: Product category cannot be null");
            return;
        }
        
        removeProduct(storeName, new Product(productName, quantity, price, description, storeName, category));
        ArrayList<String> otherSellers = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < stores.size(); i++) {
            if (stores.get(i).getName().equalsIgnoreCase(storeName)) {
                index = i;
            }
        }
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line = "";
            while ((line = bfr.readLine()) != null) {
                if (!line.startsWith(this.email) || !line.contains(storeName)) {
                    otherSellers.add(line);
                }
            }
            bfr.close();
        } catch (IOException e) {
            System.out.println();
        }
        try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt"))) {
            for (int i = 0; i < otherSellers.size(); i++) {
                bwr.write(otherSellers.get(i) + "\n");
            }

            bwr.write(this.email + "," + stores.get(index).toString());

            bwr.flush();
        } catch (IOException e) {
            System.out.println();
        }
    }

    // Backward-compatible method that defaults to SHOES category
    public void writeToSellerFileRemoveProduct(String storeName, String productName, int quantity, double price, String description) {
        writeToSellerFileRemoveProduct(storeName, productName, quantity, price, description, ProductCategory.SHOES);
    }

    public void writeToSellerFileAddStore(String storeName) {
        ArrayList<String> otherSellers = new ArrayList<>();

        /*
        seller@gmail.com,Jordan,
        seller@seller.com,Nike,
        seller@seller.com,Adidas,
         */
        Store store = new Store(storeName);
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line = "";
            while ((line = bfr.readLine()) != null) {
                if (!line.startsWith(this.email) || !line.contains(storeName)) {
                    otherSellers.add(line);
                }
            }
            bfr.close();
        } catch (IOException e) {
            System.out.println();
        }
        try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Sellers.txt"))) {
            for (int i = 0; i < otherSellers.size(); i++) {
                bwr.write(otherSellers.get(i) + "\n");
            }

            bwr.write(this.email + "," + store.toString() + "\n");

            bwr.flush();
        } catch (IOException e) {
            System.out.println();
        }
    }


    /**
     * Gets sales breakdown by category for a specific store.
     * @param storeName The name of the store to analyze
     * @return A formatted string showing revenue by category
     */
    public String getSalesBreakdownByCategory(String storeName) {
        Store store = searchStore(storeName);
        if (store == null) {
            return "Store not found: " + storeName;
        }
        
        // Initialize category revenue tracking
        double[] categoryRevenue = new double[ProductCategory.values().length];
        int[] categorySales = new int[ProductCategory.values().length];
        
        // Parse sales data to extract category information
        ArrayList<String> sales = store.getSales();
        for (String sale : sales) {
            // Parse sale format: "customer@email.com bought 2 ProductName .Revenue generated: 50.0"
            if (sale.contains("bought") && sale.contains("Revenue generated:")) {
                try {
                    String[] parts = sale.split("Revenue generated: ");
                    if (parts.length == 2) {
                        double revenue = Double.parseDouble(parts[1].trim());
                        
                        // Extract product name from sale string
                        String productInfo = parts[0];
                        String[] productParts = productInfo.split(" bought \\d+ ");
                        if (productParts.length == 2) {
                            String productName = productParts[1].replace(" .", "").trim();
                            
                            // Find the product in store to get its category
                            for (Product product : store.getProducts()) {
                                if (product.getName().equals(productName)) {
                                    int categoryIndex = product.getCategory().ordinal();
                                    categoryRevenue[categoryIndex] += revenue;
                                    categorySales[categoryIndex]++;
                                    break;
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip malformed sales entries
                }
            }
        }
        
        // Format the breakdown
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Sales Breakdown by Category for ").append(storeName).append(":\n");
        double totalRevenue = 0;
        int totalSales = 0;
        
        for (int i = 0; i < ProductCategory.values().length; i++) {
            ProductCategory category = ProductCategory.values()[i];
            if (categoryRevenue[i] > 0) {
                breakdown.append(String.format("  %s: %d sales, $%.2f revenue\n", 
                    category.getDisplayName(), categorySales[i], categoryRevenue[i]));
                totalRevenue += categoryRevenue[i];
                totalSales += categorySales[i];
            }
        }
        
        if (totalRevenue > 0) {
            breakdown.append(String.format("Total: %d sales, $%.2f revenue", totalSales, totalRevenue));
        } else {
            breakdown.append("No sales data available for category breakdown.");
        }
        
        return breakdown.toString();
    }
    
    /**
     * Gets category-based metrics for all stores owned by this seller.
     * @return A formatted string showing category performance across all stores
     */
    public String getCategoryBasedMetrics() {
        StringBuilder metrics = new StringBuilder();
        metrics.append("Category-Based Performance Summary:\n");
        metrics.append("=====================================\n");
        
        // Initialize overall category tracking
        double[] overallCategoryRevenue = new double[ProductCategory.values().length];
        int[] overallCategorySales = new int[ProductCategory.values().length];
        
        for (Store store : stores) {
            metrics.append("\n").append(getSalesBreakdownByCategory(store.getName())).append("\n");
            
            // Add to overall totals
            ArrayList<String> sales = store.getSales();
            for (String sale : sales) {
                if (sale.contains("bought") && sale.contains("Revenue generated:")) {
                    try {
                        String[] parts = sale.split("Revenue generated: ");
                        if (parts.length == 2) {
                            double revenue = Double.parseDouble(parts[1].trim());
                            
                            String productInfo = parts[0];
                            String[] productParts = productInfo.split(" bought \\d+ ");
                            if (productParts.length == 2) {
                                String productName = productParts[1].replace(" .", "").trim();
                                
                                for (Product product : store.getProducts()) {
                                    if (product.getName().equals(productName)) {
                                        int categoryIndex = product.getCategory().ordinal();
                                        overallCategoryRevenue[categoryIndex] += revenue;
                                        overallCategorySales[categoryIndex]++;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip malformed sales entries
                    }
                }
            }
        }
        
        // Add overall summary
        metrics.append("\nOVERALL CATEGORY PERFORMANCE:\n");
        double grandTotal = 0;
        int grandTotalSales = 0;
        
        for (int i = 0; i < ProductCategory.values().length; i++) {
            ProductCategory category = ProductCategory.values()[i];
            if (overallCategoryRevenue[i] > 0) {
                metrics.append(String.format("  %s: %d sales, $%.2f revenue\n", 
                    category.getDisplayName(), overallCategorySales[i], overallCategoryRevenue[i]));
                grandTotal += overallCategoryRevenue[i];
                grandTotalSales += overallCategorySales[i];
            }
        }
        
        if (grandTotal > 0) {
            metrics.append(String.format("Grand Total: %d sales, $%.2f revenue", grandTotalSales, grandTotal));
        } else {
            metrics.append("No sales data available.");
        }
        
        return metrics.toString();
    }

    public void viewStoreInfo() {
        ArrayList<String> sales;
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(email));
            
            // Write category-based metrics first
            printWriter.println(getCategoryBasedMetrics());
            printWriter.println("\n" + "=".repeat(50));
            printWriter.println("DETAILED SALES HISTORY:");
            printWriter.println("=".repeat(50));
            
            for (Store store : stores) {
                printWriter.println("--------------------");
                printWriter.println(store.getName());
                sales = store.getSales();
                if (sales.size() == 0) {
                    printWriter.println("No sales yet.");
                } else {
                    for (String sale : sales) {
                        printWriter.println(sale);
                    }
                }
            }
            printWriter.flush();
            printWriter.close();
        } catch (Exception ex) {
            System.out.println("Error in displaying file");
            return;
        }
    }
    
    /**
     * Loads products from database into the seller's stores
     * This method synchronizes in-memory store data with database
     */
    public void loadProductsFromDatabase() {
        if (productDAO == null) {
            System.err.println("ProductDAO not initialized. Cannot load products from database.");
            return;
        }
        
        for (Store store : stores) {
            // Get products for this store from database
            List<Product> dbProducts = productDAO.getProductsByStore(store.getName());
            
            // Clear existing products and add database products
            store.getProducts().clear();
            for (Product product : dbProducts) {
                store.addProduct(product);
            }
            
            System.out.println("Loaded " + dbProducts.size() + " products for store: " + store.getName());
        }
    }
    
    /**
     * Gets all products for a specific store from the database
     * @param storeName store name to get products for
     * @return List of products from the database
     */
    public List<Product> getStoreProductsFromDB(String storeName) {
        if (productDAO == null) {
            System.err.println("ProductDAO not initialized.");
            return new ArrayList<>();
        }
        
        return productDAO.getProductsByStore(storeName);
    }
    
    /**
     * Searches for a product by ID in the database
     * @param productId product ID to search for
     * @return Product object if found, null otherwise
     */
    public Product findProductByIdDB(int productId) {
        if (productDAO == null) {
            System.err.println("ProductDAO not initialized.");
            return null;
        }
        
        return productDAO.getProductById(productId);
    }
    
    /**
     * Views the audit trail for a product (change history)
     * This allows sellers to see all modifications made to a product
     * @param productId product ID to view audit trail for
     */
    public void viewProductAuditTrail(int productId) {
        if (productDAO == null) {
            System.err.println("ProductDAO not initialized.");
            return;
        }
        
        productDAO.viewProductAuditTrail(productId);
    }
    
    /**
     * Gets the change history for a product
     * @param productId product ID to get history for
     * @return List of ChangeLog entries
     */
    public List<ChangeLog> getProductChangeHistory(int productId) {
        if (productDAO == null) {
            System.err.println("ProductDAO not initialized.");
            return new ArrayList<>();
        }
        
        return productDAO.getProductChangeHistory(productId);
    }
}


