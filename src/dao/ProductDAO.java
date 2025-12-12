import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations.
 * Handles CRUD operations for products with multi-category support.
 */
public class ProductDAO {
    
    /**
     * Adds a new product to the database
     * @param name product name
     * @param category product category
     * @param price product price
     * @param quantity product quantity
     * @param storeName store name (for compatibility, stored in description field temporarily)
     * @param description product description
     * @return product ID if successful, -1 otherwise
     */
    public int addProduct(String name, String category, double price, int quantity, String storeName, String description) {
        String sql = "INSERT INTO Products (name, category, price, quantity, store_name, description) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, storeName);
            pstmt.setString(6, description);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int productId = rs.getInt(1);
                    System.out.println("Product added successfully: " + name + " (ID: " + productId + ")");
                    return productId;
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Adds a product from a Product object
     * @param product Product object to add
     * @return product ID if successful, -1 otherwise
     */
    public int addProduct(Product product) {
        return addProduct(
            product.getName(),
            product.getCategory().name(),
            product.getPrice(),
            product.getQuantity(),
            product.getStore(),
            product.getDescription()
        );
    }
    
    /**
     * Gets all products from the database
     * @return List of Product objects
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, store_name, description FROM Products";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Gets products by category
     * @param category category name to filter by
     * @return List of Product objects in the specified category
     */
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, store_name, description FROM Products WHERE category = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting products by category: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Gets products by store name
     * @param storeName store name to filter by
     * @return List of Product objects from the specified store
     */
    public List<Product> getProductsByStore(String storeName) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, store_name, description FROM Products WHERE store_name = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, storeName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting products by store: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Gets a product by ID
     * @param productId product ID to retrieve
     * @return Product object if found, null otherwise
     */
    public Product getProductById(int productId) {
        String sql = "SELECT product_id, name, category, price, quantity, store_name, description FROM Products WHERE product_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createProductFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting product by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Updates a product in the database
     * @param productId product ID to update
     * @param field field name to update (price, quantity, name, description, category)
     * @param oldValue old value for logging
     * @param newValue new value to set
     * @param userId user ID making the change (for change logging)
     * @return true if successful, false otherwise
     */
    public boolean updateProduct(int productId, String field, String oldValue, String newValue, int userId) {
        return updateProduct(productId, field, oldValue, newValue, userId, "");
    }
    
    public boolean updateProduct(int productId, String field, String oldValue, String newValue, int userId, String changeReason) {
        String sql = "";
        
        // Determine which field to update
        switch (field.toLowerCase()) {
            case "price":
                sql = "UPDATE Products SET price = ? WHERE product_id = ?";
                break;
            case "quantity":
                sql = "UPDATE Products SET quantity = ? WHERE product_id = ?";
                break;
            case "name":
                sql = "UPDATE Products SET name = ? WHERE product_id = ?";
                break;
            case "description":
                sql = "UPDATE Products SET description = ? WHERE product_id = ?";
                break;
            case "category":
                sql = "UPDATE Products SET category = ? WHERE product_id = ?";
                break;
            default:
                System.err.println("Invalid field name: " + field);
                return false;
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newValue);
            pstmt.setInt(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Product updated successfully: " + field + " changed from " + oldValue + " to " + newValue);
                
                // Log the change to ChangeLog table
                try {
                    ChangeLogDAO changeLogDAO = new ChangeLogDAO();
                    String changeType = field.substring(0, 1).toUpperCase() + field.substring(1) + " Update";
                    if (changeReason != null && !changeReason.trim().isEmpty()) {
                        changeType = changeType + " - " + changeReason.trim();
                    }
                    changeLogDAO.logChange(productId, userId, changeType, oldValue, newValue);
                } catch (SQLException e) {
                    System.err.println("Warning: Failed to log change: " + e.getMessage());
                    // Continue execution even if logging fails
                }
                
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates an entire product record
     * @param productId product ID to update
     * @param name new product name
     * @param category new category
     * @param price new price
     * @param quantity new quantity
     * @param description new description
     * @param userId user ID making the change
     * @return true if successful, false otherwise
     */
    public boolean updateProduct(int productId, String name, String category, double price, int quantity, String description, int userId) {
        return updateProduct(productId, name, category, price, quantity, description, userId, "");
    }
    
    public boolean updateProduct(int productId, String name, String category, double price, int quantity, String description, int userId, String changeReason) {
        // First, get the old values for change logging
        Product oldProduct = getProductById(productId);
        if (oldProduct == null) {
            System.err.println("Product not found: " + productId);
            return false;
        }
        
        String sql = "UPDATE Products SET name = ?, category = ?, price = ?, quantity = ?, description = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, description);
            pstmt.setInt(6, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Product updated successfully: " + name);
                
                // Log changes for each field that was modified
                try {
                    ChangeLogDAO changeLogDAO = new ChangeLogDAO();
                    
                    // Log price change
                    String reasonSuffix = (changeReason != null && !changeReason.trim().isEmpty()) ? " - " + changeReason.trim() : "";
                    
                    if (oldProduct.getPrice() != price) {
                        changeLogDAO.logChange(productId, userId, "Price Update" + reasonSuffix,
                            String.valueOf(oldProduct.getPrice()), String.valueOf(price));
                    }
                    
                    // Log quantity change
                    if (oldProduct.getQuantity() != quantity) {
                        changeLogDAO.logChange(productId, userId, "Quantity Update" + reasonSuffix,
                            String.valueOf(oldProduct.getQuantity()), String.valueOf(quantity));
                    }
                    
                    // Log name change
                    if (!oldProduct.getName().equals(name)) {
                        changeLogDAO.logChange(productId, userId, "Name Update" + reasonSuffix,
                            oldProduct.getName(), name);
                    }
                    
                    // Log category change
                    if (!oldProduct.getCategory().name().equals(category)) {
                        changeLogDAO.logChange(productId, userId, "Category Update" + reasonSuffix,
                            oldProduct.getCategory().name(), category);
                    }
                    
                    // Log description change
                    if (!oldProduct.getDescription().equals(description)) {
                        changeLogDAO.logChange(productId, userId, "Description Update" + reasonSuffix,
                            oldProduct.getDescription(), description);
                    }
                    
                } catch (SQLException e) {
                    System.err.println("Warning: Failed to log changes: " + e.getMessage());
                    // Continue execution even if logging fails
                }
                
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a product from the database
     * @param productId product ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Product deleted successfully (ID: " + productId + ")");
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Searches products by name (partial match)
     * @param searchTerm search term to match against product names
     * @return List of matching Product objects
     */
    public List<Product> searchProductsByName(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, store_name, description FROM Products WHERE LOWER(name) LIKE ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching products by name: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Gets a product ID by name and store name
     * @param productName product name to search for
     * @param storeName store name to match
     * @return product ID if found, -1 otherwise
     */
    public int getProductId(String productName, String storeName) {
        String sql = "SELECT product_id FROM Products WHERE name = ? AND store_name = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productName);
            pstmt.setString(2, storeName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("product_id");
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error getting product ID: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Displays the audit trail for a product (for sellers and admins)
     * @param productId product ID to view audit trail for
     */
    public void viewProductAuditTrail(int productId) {
        try {
            ChangeLogDAO changeLogDAO = new ChangeLogDAO();
            changeLogDAO.displayProductHistory(productId);
        } catch (SQLException e) {
            System.err.println("Error viewing audit trail: " + e.getMessage());
        }
    }
    
    /**
     * Gets the change history for a product
     * @param productId product ID to get history for
     * @return List of ChangeLog entries
     */
    public List<ChangeLog> getProductChangeHistory(int productId) {
        try {
            ChangeLogDAO changeLogDAO = new ChangeLogDAO();
            return changeLogDAO.getProductHistory(productId);
        } catch (SQLException e) {
            System.err.println("Error getting product change history: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Helper method to create a Product object from a ResultSet
     * @param rs ResultSet containing product data
     * @return Product object or null if error
     */
    private Product createProductFromResultSet(ResultSet rs) {
        try {
            String name = rs.getString("name");
            String categoryStr = rs.getString("category");
            double price = rs.getDouble("price");
            int quantity = rs.getInt("quantity");
            String storeName = rs.getString("store_name");
            String description = rs.getString("description");
            
            // Convert category string to ProductCategory enum
            ProductCategory category = ProductCategory.fromString(categoryStr);
            if (category == null) {
                category = ProductCategory.SHOES; // Default fallback
            }
            
            return new Product(name, quantity, price, description, storeName, category);
            
        } catch (SQLException e) {
            System.err.println("Error creating product from result set: " + e.getMessage());
            return null;
        }
    }
}
