import java.sql.SQLException;
import java.util.List;

/**
 * Test class for Product management with multi-category support
 */
public class TestProductManagement {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Product Management with Multi-Category Support ===\n");
        
        try {
            // Initialize database
            System.out.println("1. Initializing database...");
            DatabaseManager.initializeTables();
            System.out.println("✓ Database initialized\n");
            
            // Create test user for seller
            System.out.println("2. Creating test seller user...");
            UserDAO userDAO = new UserDAO();
            boolean userCreated = userDAO.createUser("testseller", "password123", "seller@test.com", "seller");
            if (userCreated) {
                System.out.println("✓ Test seller created");
            }
            int sellerId = userDAO.getUserId("testseller");
            System.out.println("✓ Seller ID: " + sellerId + "\n");
            
            // Test ProductDAO directly
            System.out.println("3. Testing ProductDAO - Adding products with different categories...");
            ProductDAO productDAO = new ProductDAO();
            
            // Add products in different categories
            int product1 = productDAO.addProduct("Running Shoes", "SHOES", 89.99, 50, "Nike Store", "Comfortable running shoes");
            int product2 = productDAO.addProduct("T-Shirt", "CLOTHING", 29.99, 100, "Nike Store", "Cotton t-shirt");
            int product3 = productDAO.addProduct("Laptop", "ELECTRONICS", 999.99, 20, "Tech Store", "High-performance laptop");
            
            System.out.println("✓ Added 3 products with IDs: " + product1 + ", " + product2 + ", " + product3 + "\n");
            
            // Test getting all products
            System.out.println("4. Testing ProductDAO - Getting all products...");
            List<Product> allProducts = productDAO.getAllProducts();
            System.out.println("✓ Found " + allProducts.size() + " products:");
            for (Product p : allProducts) {
                System.out.println("  - " + p.getName() + " (" + p.getCategory().getDisplayName() + ") - $" + p.getPrice());
            }
            System.out.println();
            
            // Test getting products by category
            System.out.println("5. Testing ProductDAO - Getting products by category (SHOES)...");
            List<Product> shoes = productDAO.getProductsByCategory("SHOES");
            System.out.println("✓ Found " + shoes.size() + " shoe products:");
            for (Product p : shoes) {
                System.out.println("  - " + p.getName() + " - $" + p.getPrice());
            }
            System.out.println();
            
            System.out.println("6. Testing ProductDAO - Getting products by category (ELECTRONICS)...");
            List<Product> electronics = productDAO.getProductsByCategory("ELECTRONICS");
            System.out.println("✓ Found " + electronics.size() + " electronics products:");
            for (Product p : electronics) {
                System.out.println("  - " + p.getName() + " - $" + p.getPrice());
            }
            System.out.println();
            
            // Test updating a product
            System.out.println("7. Testing ProductDAO - Updating product price...");
            boolean updated = productDAO.updateProduct(product1, "price", "89.99", "79.99", sellerId);
            if (updated) {
                Product updatedProduct = productDAO.getProductById(product1);
                System.out.println("✓ Product price updated to: $" + updatedProduct.getPrice());
            }
            System.out.println();
            
            // Test Seller with database
            System.out.println("8. Testing Seller class with database operations...");
            Seller seller = new Seller("seller@test.com", true);
            seller.addStores("Adidas Store");
            
            int product4 = seller.createProductDB("Adidas Store", "Soccer Ball", 10, 24.99, 
                "Professional soccer ball", ProductCategory.SPORTS_OUTDOORS, sellerId);
            
            if (product4 > 0) {
                System.out.println("✓ Seller added product with ID: " + product4);
            }
            System.out.println();
            
            // Test getting products by store
            System.out.println("9. Testing ProductDAO - Getting products by store...");
            List<Product> nikeProducts = productDAO.getProductsByStore("Nike Store");
            System.out.println("✓ Found " + nikeProducts.size() + " products in Nike Store:");
            for (Product p : nikeProducts) {
                System.out.println("  - " + p.getName() + " (" + p.getCategory().getDisplayName() + ")");
            }
            System.out.println();
            
            // Test search by name
            System.out.println("10. Testing ProductDAO - Searching products by name...");
            List<Product> searchResults = productDAO.searchProductsByName("shoe");
            System.out.println("✓ Found " + searchResults.size() + " products matching 'shoe':");
            for (Product p : searchResults) {
                System.out.println("  - " + p.getName() + " (" + p.getCategory().getDisplayName() + ")");
            }
            System.out.println();
            
            System.out.println("=== All Tests Completed Successfully! ===");
            
            // Close database connection
            DatabaseManager.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
