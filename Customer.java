import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Customer {
    private ArrayList<Product> shoppingCart = new ArrayList<>();

    public Customer(String email) {
        this.email = email;
    }

    public String email;

    private ArrayList<String> previouslyPurchased;


    public void viewCart(String email) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            File f = new File(email);
            if (!f.isDirectory()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                boolean trip = false;
                String line = br.readLine();
                while (line != null) {
                    if (trip) {
                        temp.add(line);
                    }
                    if (line.equals("-------")) {
                        trip = true;
                    }

                    line = br.readLine();
                }
            }
            for (int i = 0; i < temp.size(); i++) {
                System.out.println(temp.get(i) + "\n");
            }
            // Display current shopping cart with category information
            for (int i = 0; i < shoppingCart.size(); i++) {
                Product product = shoppingCart.get(i);
                System.out.println(String.format("Product: %s | Category: %s | Price: $%.2f | Store: %s", 
                    product.getName(), 
                    product.getCategory().getDisplayName(), 
                    product.getPrice(), 
                    product.getStore()));
            }
        } catch (Exception e) {
            System.out.println("There is nothing in your shopping cart.");
        }
    }

    public void addToCart(Product product) {
        shoppingCart.add(product);
    }

    public void removeProduct(Product product) {
        for (int i = 0; i < shoppingCart.size(); i++) {
            if (shoppingCart.get(i).equals(product)) {
                shoppingCart.remove(shoppingCart.get(i));
            }
        }
    }

    public void writeCart() {
        try {
            File f = new File(email);
            if (!f.isDirectory()) {
                PrintWriter pw = new PrintWriter(new FileOutputStream(f), true);
                pw.append("Shopping cart: \n");
                for (int i = 0; i < shoppingCart.size(); i++) {
                    // Write product with category information in the new format
                    pw.append(shoppingCart.get(i).toString() + "\n");
                }

            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //Done


    //Done


    //Done
    public void addToShoppingCart(Product product) {
        this.shoppingCart.add(product);
    }

    /**
     * Writes purchase history to the customer's file with category information.
     * Maintains backward compatibility with existing purchase data.
     */
    public void writePurchaseHistory(ArrayList<Product> purchasedProducts) {
        try {
            File f = new File(email);
            PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
            
            pw.append("-------\n"); // Separator for purchase history
            pw.append("Purchase History:\n");
            
            for (Product product : purchasedProducts) {
                // Write in new format with category information
                pw.append(product.toString() + "\n");
            }
            
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays purchase history with category information.
     * Handles both old format (without categories) and new format (with categories).
     */
    public void viewPurchaseHistory() {
        try {
            File f = new File(email);
            if (!f.exists()) {
                System.out.println("No purchase history found.");
                return;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            boolean inPurchaseHistory = false;
            
            System.out.println("Purchase History:");
            System.out.println("=================");
            
            while ((line = br.readLine()) != null) {
                if (line.equals("Purchase History:")) {
                    inPurchaseHistory = true;
                    continue;
                }
                
                if (inPurchaseHistory && !line.equals("-------") && !line.trim().isEmpty()) {
                    // Parse the line to determine if it has category information
                    String[] parts = line.split(",");
                    
                    if (parts.length >= 6) {
                        // New format with category (name,quantity,price,description,storeName,category)
                        String name = parts[0];
                        String quantity = parts[1];
                        String price = parts[2];
                        String description = parts[3];
                        String storeName = parts[4];
                        String category = parts.length > 5 ? parts[5] : "Unknown";
                        
                        System.out.println(String.format("Product: %s | Category: %s | Quantity: %s | Price: $%s | Store: %s", 
                            name, category, quantity, price, storeName));
                    } else if (parts.length == 5) {
                        // Old format without category (backward compatibility)
                        String name = parts[0];
                        String quantity = parts[1];
                        String price = parts[2];
                        String description = parts[3];
                        String storeName = parts[4];
                        
                        System.out.println(String.format("Product: %s | Category: Shoes (legacy) | Quantity: %s | Price: $%s | Store: %s", 
                            name, quantity, price, storeName));
                    } else {
                        // Fallback for any other format
                        System.out.println(line);
                    }
                }
                
                if (line.equals("-------")) {
                    inPurchaseHistory = false;
                }
            }
            
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading purchase history: " + e.getMessage());
        }
    }

    /**
     * Gets the shopping cart for processing purchases.
     * @return ArrayList of products in the shopping cart
     */
    public ArrayList<Product> getShoppingCart() {
        return shoppingCart;
    }

    /**
     * Clears the shopping cart after a successful purchase.
     */
    public void clearShoppingCart() {
        shoppingCart.clear();
    }
    
    /**
     * Gets the current reward points balance for this customer
     * @return number of reward points, -1 if error
     */
    public int getRewardPointsBalance() {
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(this.email);
            if (userId > 0) {
                return userDAO.getRewardPoints(userId);
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Error getting reward points: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Redeems reward points for a discount on purchase
     * @param points number of points to redeem
     * @return discount amount (1 point = $0.01), or 0 if redemption failed
     */
    public double redeemRewardPoints(int points) {
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(this.email);
            if (userId > 0) {
                if (userDAO.redeemPoints(userId, points)) {
                    // Each point is worth $0.01
                    double discount = points * 0.01;
                    System.out.println("Successfully redeemed " + points + " points for $" + 
                        String.format("%.2f", discount) + " discount");
                    return discount;
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error redeeming reward points: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Displays the current reward points balance
     */
    public void displayRewardPoints() {
        int points = getRewardPointsBalance();
        if (points >= 0) {
            double value = points * 0.01;
            System.out.println("Current Reward Points: " + points + 
                " (Worth $" + String.format("%.2f", value) + ")");
        } else {
            System.out.println("Unable to retrieve reward points balance.");
        }
    }
    
    /**
     * Processes a purchase by creating database records for the order
     * @param totalPrice total price of the order
     * @return order ID if successful, -1 otherwise
     */
    public int processPurchase(double totalPrice) {
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(this.email);
            
            if (userId <= 0) {
                System.err.println("User not found in database");
                return -1;
            }
            
            // Create the order
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.createOrder(userId, totalPrice);
            
            if (orderId <= 0) {
                System.err.println("Failed to create order");
                return -1;
            }
            
            // Add order details for each item in shopping cart
            for (Product product : shoppingCart) {
                int productId = getProductId(product);
                
                if (productId > 0) {
                    orderDAO.addOrderDetail(orderId, productId, product.getQuantity());
                } else {
                    System.err.println("Warning: Could not find product ID for " + product.getName());
                }
            }
            
            System.out.println("Order #" + orderId + " created successfully!");
            return orderId;
            
        } catch (Exception e) {
            System.err.println("Error processing purchase: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Helper method to get product ID from database
     * @param product Product object to find
     * @return product ID if found, -1 otherwise
     */
    private int getProductId(Product product) {
        try {
            ProductDAO productDAO = new ProductDAO();
            return productDAO.getProductId(product.getName(), product.getStore());
        } catch (Exception e) {
            System.err.println("Error getting product ID: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Displays order history from the database
     */
    public void viewOrderHistory() {
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(this.email);
            
            if (userId <= 0) {
                System.out.println("User not found in database");
                return;
            }
            
            OrderDAO orderDAO = new OrderDAO();
            List<Order> orders = orderDAO.getOrderHistory(userId);
            
            if (orders.isEmpty()) {
                System.out.println("No order history found.");
                return;
            }
            
            System.out.println("\n=== Order History ===");
            System.out.println("=====================");
            
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
                System.out.println("---");
            }
            
        } catch (Exception e) {
            System.err.println("Error viewing order history: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
