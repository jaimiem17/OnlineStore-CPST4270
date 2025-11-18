import java.sql.*;
import java.util.List;

/**
 * Test class for Order and OrderDetail functionality
 */
public class TestOrderSystem {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing Order System ===\n");
            
            // Initialize database
            DatabaseManager.initializeTables();
            
            // Create test user
            UserDAO userDAO = new UserDAO();
            String testUsername = "testcustomer";
            String testEmail = "test@example.com";
            
            // Check if user exists, if not create
            int userId = userDAO.getUserId(testUsername);
            if (userId <= 0) {
                userDAO.createUser(testUsername, "password123", testEmail, "customer");
                userId = userDAO.getUserId(testUsername);
            }
            System.out.println("Test user ID: " + userId + "\n");
            
            // Create test products
            ProductDAO productDAO = new ProductDAO();
            int product1Id = productDAO.addProduct("Test Shoe", "SHOES", 99.99, 10, "Test Store", "A test shoe");
            int product2Id = productDAO.addProduct("Test Shirt", "CLOTHING", 29.99, 20, "Test Store", "A test shirt");
            System.out.println("Created test products: " + product1Id + ", " + product2Id + "\n");
            
            // Create an order
            OrderDAO orderDAO = new OrderDAO();
            double totalPrice = 159.97; // 99.99 + 29.99 + 29.99
            int orderId = orderDAO.createOrder(userId, totalPrice);
            System.out.println("Created order ID: " + orderId + "\n");
            
            // Add order details
            orderDAO.addOrderDetail(orderId, product1Id, 1);
            orderDAO.addOrderDetail(orderId, product2Id, 2);
            System.out.println("Added order details\n");
            
            // Retrieve order history
            System.out.println("=== Retrieving Order History ===");
            List<Order> orders = orderDAO.getOrderHistory(userId);
            
            for (Order order : orders) {
                System.out.println("\nOrder #" + order.getOrderId() + 
                    " - Date: " + order.getOrderDate() + 
                    " - Total: $" + String.format("%.2f", order.getTotalPrice()));
                System.out.println("Items:");
                
                for (OrderDetail detail : order.getOrderDetails()) {
                    System.out.println("  - " + detail.getProductName() + 
                        " x" + detail.getQuantity() + 
                        " @ $" + String.format("%.2f", detail.getProductPrice()) + 
                        " = $" + String.format("%.2f", detail.getSubtotal()));
                }
            }
            
            System.out.println("\n=== Test Completed Successfully ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
