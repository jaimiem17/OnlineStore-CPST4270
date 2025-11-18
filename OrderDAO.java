import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Order operations.
 * Handles order creation, order detail management, and order history retrieval.
 */
public class OrderDAO {
    
    /**
     * Creates a new order in the database
     * @param userId user ID who placed the order
     * @param totalPrice total price of the order
     * @return order ID if successful, -1 otherwise
     */
    public int createOrder(int userId, double totalPrice) {
        String sql = "INSERT INTO Orders (user_id, order_date, total_price) VALUES (?, CURRENT_DATE, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, totalPrice);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    System.out.println("Order created successfully (ID: " + orderId + ")");
                    return orderId;
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Adds an order detail (line item) to an order
     * @param orderId order ID to add the detail to
     * @param productId product ID for this line item
     * @param quantity quantity of the product ordered
     * @return order detail ID if successful, -1 otherwise
     */
    public int addOrderDetail(int orderId, int productId, int quantity) {
        String sql = "INSERT INTO OrderDetails (order_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderDetailId = rs.getInt(1);
                    System.out.println("Order detail added successfully (ID: " + orderDetailId + ")");
                    return orderDetailId;
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error adding order detail: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Gets order history for a specific user
     * @param userId user ID to retrieve order history for
     * @return List of Order objects with their order details
     */
    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, order_date, total_price FROM Orders WHERE user_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int uid = rs.getInt("user_id");
                Date orderDate = rs.getDate("order_date");
                double totalPrice = rs.getDouble("total_price");
                
                Order order = new Order(orderId, uid, orderDate, totalPrice);
                
                // Load order details for this order
                List<OrderDetail> orderDetails = getOrderDetails(orderId);
                order.setOrderDetails(orderDetails);
                
                orders.add(order);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order history: " + e.getMessage());
        }
        
        return orders;
    }
    
    /**
     * Gets all order details for a specific order
     * @param orderId order ID to retrieve details for
     * @return List of OrderDetail objects
     */
    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = """
            SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity,
                   p.name, p.price
            FROM OrderDetails od
            JOIN Products p ON od.product_id = p.product_id
            WHERE od.order_id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int orderDetailId = rs.getInt("order_detail_id");
                int oid = rs.getInt("order_id");
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                String productName = rs.getString("name");
                double productPrice = rs.getDouble("price");
                
                OrderDetail orderDetail = new OrderDetail(
                    orderDetailId, oid, productId, quantity, productName, productPrice
                );
                
                orderDetails.add(orderDetail);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order details: " + e.getMessage());
        }
        
        return orderDetails;
    }
    
    /**
     * Gets a specific order by ID
     * @param orderId order ID to retrieve
     * @return Order object if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT order_id, user_id, order_date, total_price FROM Orders WHERE order_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int uid = rs.getInt("user_id");
                Date orderDate = rs.getDate("order_date");
                double totalPrice = rs.getDouble("total_price");
                
                Order order = new Order(orderId, uid, orderDate, totalPrice);
                
                // Load order details for this order
                List<OrderDetail> orderDetails = getOrderDetails(orderId);
                order.setOrderDetails(orderDetails);
                
                return order;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Gets all orders in the system (for admin purposes)
     * @return List of all Order objects
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, order_date, total_price FROM Orders ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int userId = rs.getInt("user_id");
                Date orderDate = rs.getDate("order_date");
                double totalPrice = rs.getDouble("total_price");
                
                Order order = new Order(orderId, userId, orderDate, totalPrice);
                
                // Load order details for this order
                List<OrderDetail> orderDetails = getOrderDetails(orderId);
                order.setOrderDetails(orderDetails);
                
                orders.add(order);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        
        return orders;
    }
}
