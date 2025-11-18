/**
 * OrderDetail model class representing a line item in an order.
 * Links orders to specific products with quantities.
 */
public class OrderDetail {
    private int orderDetailId;
    private int orderId;
    private int productId;
    private int quantity;
    private String productName;
    private double productPrice;
    
    /**
     * Constructor for creating an OrderDetail object with all fields
     * @param orderDetailId unique identifier for the order detail
     * @param orderId order ID this detail belongs to
     * @param productId product ID for this line item
     * @param quantity quantity of the product ordered
     */
    public OrderDetail(int orderDetailId, int orderId, int productId, int quantity) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    /**
     * Constructor for creating a new OrderDetail without orderDetailId (for new order details)
     * @param orderId order ID this detail belongs to
     * @param productId product ID for this line item
     * @param quantity quantity of the product ordered
     */
    public OrderDetail(int orderId, int productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    /**
     * Constructor with product information for display purposes
     * @param orderDetailId unique identifier for the order detail
     * @param orderId order ID this detail belongs to
     * @param productId product ID for this line item
     * @param quantity quantity of the product ordered
     * @param productName name of the product
     * @param productPrice price of the product
     */
    public OrderDetail(int orderDetailId, int orderId, int productId, int quantity, String productName, double productPrice) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.productPrice = productPrice;
    }
    
    // Getters and Setters
    
    public int getOrderDetailId() {
        return orderDetailId;
    }
    
    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public double getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }
    
    /**
     * Calculates the subtotal for this order detail
     * @return subtotal (quantity * price)
     */
    public double getSubtotal() {
        return quantity * productPrice;
    }
    
    @Override
    public String toString() {
        if (productName != null) {
            return String.format("OrderDetail[id=%d, orderId=%d, product=%s, qty=%d, price=%.2f, subtotal=%.2f]", 
                orderDetailId, orderId, productName, quantity, productPrice, getSubtotal());
        } else {
            return String.format("OrderDetail[id=%d, orderId=%d, productId=%d, qty=%d]", 
                orderDetailId, orderId, productId, quantity);
        }
    }
}
