import java.sql.*;

/**
 * DatabaseManager handles H2 database connection setup, table creation, and connection management
 * for the marketplace application database migration.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./marketplace";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private static Connection connection = null;
    
    /**
     * Gets a connection to the H2 database
     * @return Connection object for database operations
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load H2 driver
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("H2 Driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Initializes all database tables based on the schema from mod4.txt
     * @throws SQLException if table creation fails
     */
    public static void initializeTables() throws SQLException {
        Connection conn = getConnection();
        
        try (Statement stmt = conn.createStatement()) {
            // Create Users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    role VARCHAR(20) DEFAULT 'customer'
                )
                """;
            stmt.execute(createUsersTable);
            System.out.println("Users table created/verified successfully.");
            
            // Create Rewards table
            String createRewardsTable = """
                CREATE TABLE IF NOT EXISTS Rewards (
                    reward_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    points INT DEFAULT 0,
                    last_redeemed DATE,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """;
            stmt.execute(createRewardsTable);
            System.out.println("Rewards table created/verified successfully.");
            
            // Create Products table
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS Products (
                    product_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    category VARCHAR(50),
                    price DECIMAL(10,2) NOT NULL,
                    quantity INT DEFAULT 0
                )
                """;
            stmt.execute(createProductsTable);
            System.out.println("Products table created/verified successfully.");
            
            // Create Orders table
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS Orders (
                    order_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    order_date DATE DEFAULT CURRENT_DATE,
                    total_price DECIMAL(10,2),
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """;
            stmt.execute(createOrdersTable);
            System.out.println("Orders table created/verified successfully.");
            
            // Create OrderDetails table
            String createOrderDetailsTable = """
                CREATE TABLE IF NOT EXISTS OrderDetails (
                    order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT,
                    product_id INT,
                    quantity INT,
                    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
                    FOREIGN KEY (product_id) REFERENCES Products(product_id)
                )
                """;
            stmt.execute(createOrderDetailsTable);
            System.out.println("OrderDetails table created/verified successfully.");
            
            // Create ChangeLog table
            String createChangeLogTable = """
                CREATE TABLE IF NOT EXISTS ChangeLog (
                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                    product_id INT,
                    user_id INT,
                    change_type VARCHAR(50),
                    old_value VARCHAR(255),
                    new_value VARCHAR(255),
                    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (product_id) REFERENCES Products(product_id),
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """;
            stmt.execute(createChangeLogTable);
            System.out.println("ChangeLog table created/verified successfully.");
            
            System.out.println("All database tables initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Closes the database connection and performs cleanup
     * @throws SQLException if connection close fails
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            System.out.println("Database connection closed successfully.");
        }
    }
    
    /**
     * Test method to verify database setup
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("Testing DatabaseManager...");
            initializeTables();
            
            // Test connection
            Connection conn = getConnection();
            System.out.println("Connection test successful: " + !conn.isClosed());
            
            closeConnection();
            System.out.println("DatabaseManager test completed successfully.");
            
        } catch (SQLException e) {
            System.err.println("DatabaseManager test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}