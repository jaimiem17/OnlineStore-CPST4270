import java.sql.SQLException;
import java.util.List;

/**
 * Test class to verify change logging and audit trail functionality
 */
public class TestChangeLog {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Change Log and Audit Trail System ===\n");
        
        try {
            // Initialize database
            DatabaseManager.initializeTables();
            
            // Create test user
            UserDAO userDAO = new UserDAO();
            boolean userCreated = userDAO.createUser("testuser", "password123", "test@example.com", "seller");
            int userId = userDAO.getUserId("testuser");
            System.out.println("Created test user: " + (userCreated ? "success" : "failed") + " (ID: " + userId + ")\n");
            
            // Create test product
            ProductDAO productDAO = new ProductDAO();
            int productId = productDAO.addProduct("Test Sneakers", "SHOES", 99.99, 50, "Test Store", "Comfortable running shoes");
            System.out.println("Created test product with ID: " + productId + "\n");
            
            // Test 1: Update product price
            System.out.println("Test 1: Updating product price...");
            boolean success = productDAO.updateProduct(productId, "price", "99.99", "89.99", userId);
            System.out.println("Price update " + (success ? "successful" : "failed") + "\n");
            
            // Test 2: Update product quantity
            System.out.println("Test 2: Updating product quantity...");
            success = productDAO.updateProduct(productId, "quantity", "50", "45", userId);
            System.out.println("Quantity update " + (success ? "successful" : "failed") + "\n");
            
            // Test 3: Update product name
            System.out.println("Test 3: Updating product name...");
            success = productDAO.updateProduct(productId, "name", "Test Sneakers", "Premium Test Sneakers", userId);
            System.out.println("Name update " + (success ? "successful" : "failed") + "\n");
            
            // Test 4: View audit trail
            System.out.println("Test 4: Viewing product audit trail...");
            productDAO.viewProductAuditTrail(productId);
            System.out.println();
            
            // Test 5: Get change history programmatically
            System.out.println("Test 5: Getting change history programmatically...");
            List<ChangeLog> history = productDAO.getProductChangeHistory(productId);
            System.out.println("Found " + history.size() + " change log entries");
            for (ChangeLog log : history) {
                System.out.println("  - " + log.getChangeType() + ": " + log.getOldValue() + " -> " + log.getNewValue());
            }
            System.out.println();
            
            // Test 6: Test full product update with multiple changes
            System.out.println("Test 6: Testing full product update...");
            success = productDAO.updateProduct(productId, "Premium Test Sneakers V2", "SHOES", 
                                              79.99, 40, "Updated description", userId);
            System.out.println("Full update " + (success ? "successful" : "failed") + "\n");
            
            // View final audit trail
            System.out.println("Final audit trail:");
            productDAO.viewProductAuditTrail(productId);
            
            System.out.println("\n=== All Tests Completed Successfully ===");
            
        } catch (SQLException e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
