import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model class representing a customer order in the marketplace system.
 * Stores order information including user, date, total price, and order details.
 */
public class Order {
    private int orderId;
    private int userId;
    private Date orderDate;
    private double totalPrice;
    private List<OrderDetail> orderDetails;
    
    /**
     * Constructor for creating an Order object with all fields
     * @param orderId unique identifier for the order
     * @param userId user ID who placed the order
     * @param orderDate date when the order was placed
     * @param totalPrice total price of the order
     */
    public Order(int orderId, int userId, Date orderDate, double totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.orderDetails = new ArrayList<>();
    }
    
    /**
     * Constructor for creating a new Order without orderId (for new orders)
     * @param userId user ID who placed the order
     * @param totalPrice total price of the order
     */
    public Order(int userId, double totalPrice) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.orderDate = new Date(System.currentTimeMillis());
        this.orderDetails = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }
    
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    /**
     * Adds an order detail to this order
     * @param orderDetail the order detail to add
     */
    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);
    }
    
    @Override
    public String toString() {
        return String.format("Order[id=%d, userId=%d, date=%s, totalPrice=%.2f, items=%d]", 
            orderId, userId, orderDate, totalPrice, orderDetails.size());
    }
}
