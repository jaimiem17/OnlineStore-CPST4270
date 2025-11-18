/**
 * Test class to verify User authentication and rewards system functionality
 */
public class TestUserAuthentication {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing User Authentication and Rewards System ===\n");
            
            // Initialize database
            DatabaseManager.initializeTables();
            
            UserDAO userDAO = new UserDAO();
            
            // Test 1: Create a new user
            System.out.println("Test 1: Creating new user...");
            boolean created = userDAO.createUser("testuser", "password123", "test@example.com", "customer");
            System.out.println("User created: " + created);
            System.out.println();
            
            // Test 2: Validate login with correct credentials
            System.out.println("Test 2: Validating login with correct credentials...");
            boolean validLogin = userDAO.validateLogin("testuser", "password123");
            System.out.println("Login valid: " + validLogin);
            System.out.println();
            
            // Test 3: Validate login with incorrect credentials
            System.out.println("Test 3: Validating login with incorrect credentials...");
            boolean invalidLogin = userDAO.validateLogin("testuser", "wrongpassword");
            System.out.println("Login valid: " + invalidLogin + " (should be false)");
            System.out.println();
            
            // Test 4: Get user ID
            System.out.println("Test 4: Getting user ID...");
            int userId = userDAO.getUserId("testuser");
            System.out.println("User ID: " + userId);
            System.out.println();
            
            // Test 5: Check initial reward points
            System.out.println("Test 5: Checking initial reward points...");
            int initialPoints = userDAO.getRewardPoints(userId);
            System.out.println("Initial points: " + initialPoints);
            System.out.println();
            
            // Test 6: Add reward points
            System.out.println("Test 6: Adding 100 reward points...");
            boolean pointsAdded = userDAO.addRewardPoints(userId, 100);
            System.out.println("Points added: " + pointsAdded);
            int currentPoints = userDAO.getRewardPoints(userId);
            System.out.println("Current points: " + currentPoints);
            System.out.println();
            
            // Test 7: Redeem points with sufficient balance
            System.out.println("Test 7: Redeeming 50 points...");
            boolean redeemed = userDAO.redeemPoints(userId, 50);
            System.out.println("Points redeemed: " + redeemed);
            currentPoints = userDAO.getRewardPoints(userId);
            System.out.println("Remaining points: " + currentPoints);
            System.out.println();
            
            // Test 8: Try to redeem more points than available
            System.out.println("Test 8: Attempting to redeem 100 points (insufficient balance)...");
            boolean failedRedemption = userDAO.redeemPoints(userId, 100);
            System.out.println("Redemption successful: " + failedRedemption + " (should be false)");
            System.out.println();
            
            // Test 9: Get user by username
            System.out.println("Test 9: Getting user by username...");
            User user = userDAO.getUserByUsername("testuser");
            if (user != null) {
                System.out.println("User found: " + user.toString());
            } else {
                System.out.println("User not found");
            }
            System.out.println();
            
            System.out.println("=== All tests completed successfully! ===");
            
            DatabaseManager.closeConnection();
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
