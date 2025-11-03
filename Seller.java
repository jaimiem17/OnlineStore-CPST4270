import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class Seller {
    private ArrayList<Store> stores = new ArrayList<>();
    private String email;

    public Seller(String email) {
        this.email = email;
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
}


