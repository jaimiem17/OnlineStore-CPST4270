import java.util.ArrayList;

/**
 * Service class for handling data migration from legacy shoe data format
 * to the new product data format with categories.
 */
public class DataMigrationService {
    
    /**
     * Detects whether a CSV line represents old format (5 fields) or new format (6 fields).
     * @param csvLine The CSV line to analyze
     * @return true if it's the old format (5 fields), false if new format (6+ fields)
     */
    public static boolean isLegacyFormat(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return false;
        }
        
        String[] fields = csvLine.split(",");
        
        // Legacy format has exactly 5 product fields after seller email and store name
        // Format: sellerEmail,storeName,productName,quantity,price,description,storeName
        // So we need to check if the product data portion has 5 fields
        
        // If line has less than 7 fields total, it's either incomplete or legacy
        if (fields.length < 7) {
            return true;
        }
        
        // If line has exactly 7 fields, it's legacy format
        // If line has 8 or more fields, it's new format with category
        return fields.length == 7;
    }
    
    /**
     * Parses a product from legacy shoe data format (5 fields).
     * Expected format: name,quantity,price,description,storeName
     * @param productFields Array of product fields from CSV
     * @return Product object with SHOES category assigned
     */
    public static Product parseProductFromLegacyFormat(String[] productFields) {
        if (productFields == null || productFields.length < 5) {
            throw new IllegalArgumentException("Legacy format requires exactly 5 fields: name,quantity,price,description,storeName");
        }
        
        try {
            String name = productFields[0].trim();
            int quantity = Integer.parseInt(productFields[1].trim());
            double price = Double.parseDouble(productFields[2].trim());
            String description = productFields[3].trim();
            String storeName = productFields[4].trim();
            
            // Create product with default SHOES category for backward compatibility
            return new Product(name, quantity, price, description, storeName, ProductCategory.SHOES);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in legacy product data: " + String.join(",", productFields), e);
        }
    }
    
    /**
     * Parses a product from new data format (6 fields with category).
     * Expected format: name,quantity,price,description,storeName,category
     * @param productFields Array of product fields from CSV
     * @return Product object with specified category
     */
    public static Product parseProductFromNewFormat(String[] productFields) {
        if (productFields == null || productFields.length < 6) {
            throw new IllegalArgumentException("New format requires exactly 6 fields: name,quantity,price,description,storeName,category");
        }
        
        try {
            String name = productFields[0].trim();
            int quantity = Integer.parseInt(productFields[1].trim());
            double price = Double.parseDouble(productFields[2].trim());
            String description = productFields[3].trim();
            String storeName = productFields[4].trim();
            String categoryStr = productFields[5].trim();
            
            // Parse category, default to SHOES if invalid
            ProductCategory category = ProductCategory.fromString(categoryStr);
            if (category == null) {
                category = ProductCategory.SHOES; // Fallback for invalid categories
            }
            
            return new Product(name, quantity, price, description, storeName, category);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in new product data: " + String.join(",", productFields), e);
        }
    }
    
    /**
     * Parses a complete CSV line and extracts product data based on format detection.
     * Handles both legacy and new formats automatically.
     * @param csvLine Complete CSV line from Sellers.txt
     * @return ArrayList of Product objects parsed from the line
     */
    public static ArrayList<Product> parseProductsFromLine(String csvLine) {
        ArrayList<Product> products = new ArrayList<>();
        
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return products;
        }
        
        String[] fields = csvLine.split(",");
        
        // Skip lines that don't have product data (just seller info or store info)
        if (fields.length < 7) {
            return products;
        }
        
        boolean isLegacy = isLegacyFormat(csvLine);
        
        // Extract product data starting from index 2 (after seller email and store name)
        if (isLegacy) {
            // Legacy format: sellerEmail,storeName,name,quantity,price,description,storeName
            // Products start at index 2, each product takes 5 fields
            for (int i = 2; i < fields.length; i += 5) {
                if (i + 4 < fields.length) { // Ensure we have all 5 fields
                    String[] productFields = new String[5];
                    System.arraycopy(fields, i, productFields, 0, 5);
                    try {
                        Product product = parseProductFromLegacyFormat(productFields);
                        products.add(product);
                    } catch (IllegalArgumentException e) {
                        // Log error but continue processing other products
                        System.err.println("Error parsing legacy product: " + e.getMessage());
                    }
                }
            }
        } else {
            // New format: sellerEmail,storeName,name,quantity,price,description,storeName,category
            // Products start at index 2, each product takes 6 fields
            for (int i = 2; i < fields.length; i += 6) {
                if (i + 5 < fields.length) { // Ensure we have all 6 fields
                    String[] productFields = new String[6];
                    System.arraycopy(fields, i, productFields, 0, 6);
                    try {
                        Product product = parseProductFromNewFormat(productFields);
                        products.add(product);
                    } catch (IllegalArgumentException e) {
                        // Log error but continue processing other products
                        System.err.println("Error parsing new product: " + e.getMessage());
                    }
                }
            }
        }
        
        return products;
    }
    
    /**
     * Converts a legacy CSV line to new format by adding SHOES category to all products.
     * @param legacyCsvLine CSV line in legacy format
     * @return CSV line in new format with SHOES category added
     */
    public static String convertLegacyLineToNewFormat(String legacyCsvLine) {
        if (legacyCsvLine == null || legacyCsvLine.trim().isEmpty()) {
            return legacyCsvLine;
        }
        
        if (!isLegacyFormat(legacyCsvLine)) {
            return legacyCsvLine; // Already in new format
        }
        
        String[] fields = legacyCsvLine.split(",");
        
        // If it's just seller info or store info (less than 7 fields), return as-is
        if (fields.length < 7) {
            return legacyCsvLine;
        }
        
        StringBuilder newLine = new StringBuilder();
        newLine.append(fields[0]).append(",").append(fields[1]); // seller email and store name
        
        // Process products starting from index 2, each taking 5 fields in legacy format
        for (int i = 2; i < fields.length; i += 5) {
            if (i + 4 < fields.length) { // Ensure we have all 5 fields for a complete product
                // Add the 5 legacy fields
                for (int j = 0; j < 5; j++) {
                    newLine.append(",").append(fields[i + j]);
                }
                // Add SHOES category as the 6th field
                newLine.append(",").append(ProductCategory.SHOES.name());
            }
        }
        
        return newLine.toString();
    }
}