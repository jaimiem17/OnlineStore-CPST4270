import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Data Access Object for User operations.
 * Handles user authentication, creation, and reward points management.
 */
public class UserDAO {
    
    /**
     * Validates user login credentials against the database
     * @param username username to validate
     * @param password plain text password to validate
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateLogin(String username, String password) {
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String inputHash = hashPassword(password);
                return storedHash.equals(inputHash);
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a new user in the database
     * @param username unique username for the user
     * @param password plain text password (will be hashed)
     * @param email user's email address
     * @param role user role (customer, seller, admin)
     * @return true if user was created successfully, false otherwise
     */
    public boolean createUser(String username, String password, String email, String role) {
        String sql = "INSERT INTO Users (username, password_hash, email, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User created successfully: " + username);
                
                // Create rewards entry for the new user
                int userId = getUserId(username);
                if (userId > 0) {
                    createRewardsEntry(userId);
                }
                
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the user ID for a given username
     * @param username username to look up
     * @return user ID if found, -1 otherwise
     */
    public int getUserId(String username) {
        String sql = "SELECT user_id FROM Users WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Gets user information by username
     * @param username username to look up
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, email, role FROM Users WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("role")
                );
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Hashes a password using SHA-256
     * @param password plain text password to hash
     * @return hashed password as hexadecimal string
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a rewards entry for a new user
     * @param userId user ID to create rewards for
     */
    private void createRewardsEntry(int userId) {
        String sql = "INSERT INTO Rewards (user_id, points) VALUES (?, 0)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error creating rewards entry: " + e.getMessage());
        }
    }
    
    /**
     * Adds reward points to a user's account
     * @param userId user ID to add points to
     * @param points number of points to add
     * @return true if successful, false otherwise
     */
    public boolean addRewardPoints(int userId, int points) {
        String sql = "UPDATE Rewards SET points = points + ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, points);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Added " + points + " reward points to user " + userId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding reward points: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the current reward points balance for a user
     * @param userId user ID to check
     * @return number of reward points, -1 if error
     */
    public int getRewardPoints(int userId) {
        String sql = "SELECT points FROM Rewards WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("points");
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error getting reward points: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Redeems reward points from a user's account
     * @param userId user ID to redeem points from
     * @param points number of points to redeem
     * @return true if successful, false otherwise (including insufficient balance)
     */
    public boolean redeemPoints(int userId, int points) {
        // First check if user has enough points
        int currentPoints = getRewardPoints(userId);
        
        if (currentPoints < points) {
            System.out.println("Insufficient reward points. Current balance: " + currentPoints);
            return false;
        }
        
        String sql = "UPDATE Rewards SET points = points - ?, last_redeemed = CURRENT_DATE WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, points);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Redeemed " + points + " reward points from user " + userId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error redeeming points: " + e.getMessage());
            return false;
        }
    }
}
