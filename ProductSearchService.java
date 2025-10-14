import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Service class for searching and filtering products in the marketplace.
 * Provides category-aware search functionality for various product attributes.
 */
public class ProductSearchService {
    
    /**
     * Searches products by name with optional category filtering.
     * @param name The product name to search for (case-insensitive)
     * @param category Optional category filter (null for no filtering)
     * @return ArrayList of matching product lines from the file
     */
    public static ArrayList<String> searchByName(String name, ProductCategory category) {
        ArrayList<String> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    // Check name match (case-insensitive)
                    if (name.equalsIgnoreCase(arr[2])) {
                        // If no category filter or category matches
                        if (category == null || matchesCategory(arr, category)) {
                            results.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Searches products by name without category filtering (backward compatibility).
     * @param name The product name to search for
     * @return ArrayList of matching product lines
     */
    public static ArrayList<String> searchByName(String name) {
        return searchByName(name, null);
    }
    
    /**
     * Searches products by price with optional category filtering.
     * @param price The exact price to search for
     * @param category Optional category filter (null for no filtering)
     * @return ArrayList of matching product lines from the file
     */
    public static ArrayList<String> searchByPrice(double price, ProductCategory category) {
        ArrayList<String> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    try {
                        // Check price match
                        if (Math.abs(Double.parseDouble(arr[4]) - price) < 0.01) { // Use small epsilon for double comparison
                            // If no category filter or category matches
                            if (category == null || matchesCategory(arr, category)) {
                                results.add(line);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip lines with invalid price format
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Searches products by price without category filtering (backward compatibility).
     * @param price The exact price to search for
     * @return ArrayList of matching product lines
     */
    public static ArrayList<String> searchByPrice(double price) {
        return searchByPrice(price, null);
    }
    
    /**
     * Searches products by store name with optional category filtering.
     * @param storeName The store name to search for (case-insensitive)
     * @param category Optional category filter (null for no filtering)
     * @return ArrayList of matching product lines from the file
     */
    public static ArrayList<String> searchByStore(String storeName, ProductCategory category) {
        ArrayList<String> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    // Check store name match (case-insensitive)
                    if (storeName.equalsIgnoreCase(arr[1])) {
                        // If no category filter or category matches
                        if (category == null || matchesCategory(arr, category)) {
                            results.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Searches products by store name without category filtering (backward compatibility).
     * @param storeName The store name to search for
     * @return ArrayList of matching product lines
     */
    public static ArrayList<String> searchByStore(String storeName) {
        return searchByStore(storeName, null);
    }
    
    /**
     * Searches products by description with optional category filtering.
     * @param description The description to search for (case-insensitive)
     * @param category Optional category filter (null for no filtering)
     * @return ArrayList of matching product lines from the file
     */
    public static ArrayList<String> searchByDescription(String description, ProductCategory category) {
        ArrayList<String> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    // Check description match (case-insensitive)
                    if (description.equalsIgnoreCase(arr[5])) {
                        // If no category filter or category matches
                        if (category == null || matchesCategory(arr, category)) {
                            results.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Searches products by description without category filtering (backward compatibility).
     * @param description The description to search for
     * @return ArrayList of matching product lines
     */
    public static ArrayList<String> searchByDescription(String description) {
        return searchByDescription(description, null);
    }
    
    /**
     * Searches for all products in a specific category.
     * @param category The category to search for
     * @return ArrayList of all products in the specified category
     */
    public static ArrayList<String> searchByCategory(ProductCategory category) {
        ArrayList<String> results = new ArrayList<>();
        
        if (category == null) {
            return results; // Return empty list if no category specified
        }
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    if (matchesCategory(arr, category)) {
                        results.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Gets all available categories from the current inventory.
     * @return ArrayList of ProductCategory values that have products in inventory
     */
    public static ArrayList<ProductCategory> getAvailableCategories() {
        ArrayList<ProductCategory> availableCategories = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) { // Ensure we have enough fields for a product line
                    ProductCategory category = getCategoryFromProductFields(arr);
                    if (category != null && !availableCategories.contains(category)) {
                        availableCategories.add(category);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return availableCategories;
    }
    
    /**
     * Formats search results to clearly display product categories.
     * @param searchResults ArrayList of product lines from search
     * @return ArrayList of formatted strings with category information highlighted
     */
    public static ArrayList<String> formatSearchResultsWithCategory(ArrayList<String> searchResults) {
        ArrayList<String> formattedResults = new ArrayList<>();
        
        for (String result : searchResults) {
            if (result.trim().isEmpty()) {
                continue;
            }
            
            String[] arr = result.split(",");
            if (arr.length >= 6) {
                ProductCategory category = getCategoryFromProductFields(arr);
                String categoryDisplay = (category != null) ? category.getDisplayName() : "Unknown";
                
                // Format: [Category] ProductName - $Price (Qty: X) - Store: StoreName - Description
                String formatted = String.format("[%s] %s - $%.2f (Qty: %s) - Store: %s - %s",
                    categoryDisplay,
                    arr.length > 2 ? arr[2] : "Unknown Product",
                    arr.length > 4 ? parseDoubleOrDefault(arr[4], 0.0) : 0.0,
                    arr.length > 3 ? arr[3] : "0",
                    arr.length > 1 ? arr[1] : "Unknown Store",
                    arr.length > 5 ? arr[5] : "No description"
                );
                
                formattedResults.add(formatted);
            }
        }
        
        return formattedResults;
    }
    
    /**
     * Helper method to safely parse double values.
     * @param value String value to parse
     * @param defaultValue Default value if parsing fails
     * @return Parsed double or default value
     */
    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper method to extract category from product fields.
     * @param productFields Array of product fields from CSV line
     * @return ProductCategory or null if not found/invalid
     */
    private static ProductCategory getCategoryFromProductFields(String[] productFields) {
        // Check if we have a category field (new format)
        if (productFields.length >= 7) {
            try {
                return ProductCategory.valueOf(productFields[6].trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category in file
                return null;
            }
        } else {
            // Legacy format - assume SHOES category
            return ProductCategory.SHOES;
        }
    }
    
    /**
     * Helper method to check if a product line matches a specific category.
     * @param productFields Array of product fields from CSV line
     * @param category The category to match against
     * @return true if the product matches the category, false otherwise
     */
    private static boolean matchesCategory(String[] productFields, ProductCategory category) {
        if (category == null) {
            return true; // No category filter
        }
        
        ProductCategory productCategory = getCategoryFromProductFields(productFields);
        return productCategory == category;
    }
    
    /**
     * Converts string search results to Product objects.
     * @param stringResults ArrayList of product lines from file
     * @return ArrayList of Product objects
     */
    private static ArrayList<Product> convertToProducts(ArrayList<String> stringResults) {
        ArrayList<Product> products = new ArrayList<>();
        
        for (String line : stringResults) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            try {
                ArrayList<Product> lineProducts = DataMigrationService.parseProductsFromLine(line);
                products.addAll(lineProducts);
            } catch (Exception e) {
                System.out.println("Warning: Could not parse product from line: " + line);
            }
        }
        
        return products;
    }
    
    /**
     * Searches products by name and returns Product objects.
     * @param name The product name to search for
     * @return ArrayList of Product objects matching the name
     */
    public static ArrayList<Product> searchByNameAsProducts(String name) {
        ArrayList<String> stringResults = searchByName(name);
        return convertToProducts(stringResults);
    }
    
    /**
     * Searches products by price and returns Product objects.
     * @param price The exact price to search for
     * @return ArrayList of Product objects matching the price
     */
    public static ArrayList<Product> searchByPriceAsProducts(double price) {
        ArrayList<String> stringResults = searchByPrice(price);
        return convertToProducts(stringResults);
    }
    
    /**
     * Searches products by store and returns Product objects.
     * @param storeName The store name to search for
     * @return ArrayList of Product objects from the specified store
     */
    public static ArrayList<Product> searchByStoreAsProducts(String storeName) {
        ArrayList<String> stringResults = searchByStore(storeName);
        return convertToProducts(stringResults);
    }
    
    /**
     * Searches products by description and returns Product objects.
     * @param description The description to search for
     * @return ArrayList of Product objects matching the description
     */
    public static ArrayList<Product> searchByDescriptionAsProducts(String description) {
        ArrayList<String> stringResults = searchByDescription(description);
        return convertToProducts(stringResults);
    }
    
    /**
     * Searches products by category and returns Product objects.
     * @param category The category to search for
     * @return ArrayList of Product objects in the specified category
     */
    public static ArrayList<Product> searchByCategoryAsProducts(ProductCategory category) {
        ArrayList<String> stringResults = searchByCategory(category);
        return convertToProducts(stringResults);
    }
    
    /**
     * Gets all in-stock products (quantity > 0).
     * @return ArrayList of Product objects that are in stock
     */
    public static ArrayList<Product> searchInStock() {
        ArrayList<Product> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) {
                    try {
                        int quantity = Integer.parseInt(arr[3]);
                        if (quantity > 0) {
                            ArrayList<Product> lineProducts = DataMigrationService.parseProductsFromLine(line);
                            results.addAll(lineProducts);
                        }
                    } catch (NumberFormatException e) {
                        // Skip lines with invalid quantity
                        continue;
                    } catch (Exception e) {
                        System.out.println("Warning: Could not parse product from line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Gets all products in the marketplace.
     * @return ArrayList of all Product objects
     */
    public static ArrayList<Product> getAllProducts() {
        ArrayList<Product> results = new ArrayList<>();
        
        try (BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] arr = line.split(",");
                if (arr.length >= 6) {
                    try {
                        ArrayList<Product> lineProducts = DataMigrationService.parseProductsFromLine(line);
                        results.addAll(lineProducts);
                    } catch (Exception e) {
                        System.out.println("Warning: Could not parse product from line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sellers file: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Filters a list of products by category.
     * @param products The list of products to filter
     * @param category The category to filter by
     * @return ArrayList of products matching the category
     */
    public static ArrayList<Product> filterByCategory(ArrayList<Product> products, ProductCategory category) {
        ArrayList<Product> filtered = new ArrayList<>();
        
        for (Product product : products) {
            if (product.getCategory() == category) {
                filtered.add(product);
            }
        }
        
        return filtered;
    }
}