import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChangeLogDAO handles database operations for the ChangeLog table,
 * providing methods to record and retrieve product change audit trails.
 */
public class ChangeLogDAO {
    
    /**
     * Logs a change to a product in the database
     * @param productId ID of the product that was changed
     * @param userId ID of the user who made the change
     * @param changeType Type of change (e.g., "Price Update", "Stock Update")
     * @param oldValue Previous value before the change
     * @param newValue New value after the change
     * @throws SQLException if database operation fails
     */
    public void logChange(int productId, int userId, String changeType, 
                         String oldValue, String newValue) throws SQLException {
        String sql = """
            INSERT INTO ChangeLog (product_id, user_id, change_type, old_value, new_value)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, changeType);
            pstmt.setString(4, oldValue);
            pstmt.setString(5, newValue);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Change logged successfully: " + changeType);
            }
            
        } catch (SQLException e) {
            System.err.println("Error logging change: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Retrieves the complete change history for a specific product
     * @param productId ID of the product to get history for
     * @return List of ChangeLog entries for the product
     * @throws SQLException if database operation fails
     */
    public List<ChangeLog> getProductHistory(int productId) throws SQLException {
        List<ChangeLog> history = new ArrayList<>();
        String sql = """
            SELECT log_id, product_id, user_id, change_type, old_value, new_value, change_date
            FROM ChangeLog
            WHERE product_id = ?
            ORDER BY change_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChangeLog log = new ChangeLog(
                        rs.getInt("log_id"),
                        rs.getInt("product_id"),
                        rs.getInt("user_id"),
                        rs.getString("change_type"),
                        rs.getString("old_value"),
                        rs.getString("new_value"),
                        rs.getTimestamp("change_date")
                    );
                    history.add(log);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving product history: " + e.getMessage());
            throw e;
        }
        
        return history;
    }
    
    /**
     * Retrieves all change logs made by a specific user
     * @param userId ID of the user to get change history for
     * @return List of ChangeLog entries made by the user
     * @throws SQLException if database operation fails
     */
    public List<ChangeLog> getUserChanges(int userId) throws SQLException {
        List<ChangeLog> changes = new ArrayList<>();
        String sql = """
            SELECT log_id, product_id, user_id, change_type, old_value, new_value, change_date
            FROM ChangeLog
            WHERE user_id = ?
            ORDER BY change_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChangeLog log = new ChangeLog(
                        rs.getInt("log_id"),
                        rs.getInt("product_id"),
                        rs.getInt("user_id"),
                        rs.getString("change_type"),
                        rs.getString("old_value"),
                        rs.getString("new_value"),
                        rs.getTimestamp("change_date")
                    );
                    changes.add(log);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user changes: " + e.getMessage());
            throw e;
        }
        
        return changes;
    }
    
    /**
     * Retrieves all change logs from the database
     * @return List of all ChangeLog entries
     * @throws SQLException if database operation fails
     */
    public List<ChangeLog> getAllChanges() throws SQLException {
        List<ChangeLog> allChanges = new ArrayList<>();
        String sql = """
            SELECT log_id, product_id, user_id, change_type, old_value, new_value, change_date
            FROM ChangeLog
            ORDER BY change_date DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ChangeLog log = new ChangeLog(
                    rs.getInt("log_id"),
                    rs.getInt("product_id"),
                    rs.getInt("user_id"),
                    rs.getString("change_type"),
                    rs.getString("old_value"),
                    rs.getString("new_value"),
                    rs.getTimestamp("change_date")
                );
                allChanges.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all changes: " + e.getMessage());
            throw e;
        }
        
        return allChanges;
    }
    
    /**
     * Displays the change history for a product in a formatted way
     * @param productId ID of the product to display history for
     * @throws SQLException if database operation fails
     */
    public void displayProductHistory(int productId) throws SQLException {
        List<ChangeLog> history = getProductHistory(productId);
        
        if (history.isEmpty()) {
            System.out.println("No change history found for product ID: " + productId);
            return;
        }
        
        System.out.println("\n=== Change History for Product ID: " + productId + " ===");
        System.out.println("----------------------------------------------------------------");
        
        for (ChangeLog log : history) {
            System.out.printf("Date: %s | User ID: %d | Change: %s%n",
                            log.getChangeDate(), log.getUserId(), log.getChangeType());
            System.out.printf("  Old Value: %s -> New Value: %s%n",
                            log.getOldValue(), log.getNewValue());
            System.out.println("----------------------------------------------------------------");
        }
    }
}
