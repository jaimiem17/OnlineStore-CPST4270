import java.util.ArrayList;

public class Store {
    private String name;
    private ArrayList<Customer> customers;
    private ArrayList<Product> products;
    private ArrayList<String> sales = new ArrayList<>();

    private double revenue;

    public Store(String name, ArrayList<Customer> customers, ArrayList<Product> products) {
        this.name = name;
        this.customers = customers;
        this.products = products;
        this.sales = new ArrayList<>();
    }

    public Store(String name) {
        this.name = name;
        this.customers = new ArrayList<>();
        this.products = new ArrayList<>();
        this.sales = new ArrayList<>();
    }

    public boolean checkForProduct(Product product) {
        if (products.contains(product)) {
            return true;
        }
        return false;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public void setProduct(Product oldProduct, Product newProduct) {
        for (int i = 0; i < products.size(); i++) {
            if(products.get(i).equals(oldProduct)){
                products.set(i, newProduct);
            }
        }
    }

    public ArrayList<Product> getProductsByCategory(ProductCategory category) {
        ArrayList<Product> categoryProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getCategory() == category) {
                categoryProducts.add(product);
            }
        }
        return categoryProducts;
    }

    public boolean equals(Object o) {
        Store p = (Store) o;
        return (p.name.equals(name) && p.products.equals(products) && p.customers.equals(customers) &&
                p.sales.equals(sales));
    }

    public void processPurchase(String productName, int quantity, Customer customer) {
        boolean productFound = false;
        for (Product product: products) {
            if (product.getName().equalsIgnoreCase(productName)) {
                productFound = true;
                if (quantity <= product.getQuantity()) {
                    int oldQuantity = product.getQuantity();
                    int newQuantity = oldQuantity - quantity;
                    product.setQuantity(newQuantity);
                    
                    // Update product quantity in database
                    try {
                        ProductDAO productDAO = new ProductDAO();
                        int productId = productDAO.getProductId(product.getName(), this.name);
                        if (productId > 0) {
                            productDAO.updateProduct(productId, "quantity", 
                                String.valueOf(oldQuantity), String.valueOf(newQuantity), -1);
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating product quantity in database: " + e.getMessage());
                    }
                    
                    purchaseDetail(product, quantity, customer);
                    return;
                } else {
                    System.out.println("Sorry! We're out of stock!");
                    return;
                }
            }
        }
        if (!productFound) {
            System.out.println("Sorry this product does not exist in this store!");
        }
    }

    public void purchaseDetail(Product product, int quantity, Customer customer) {
        double revenue = product.getPrice() * quantity;
        sales.add(customer.getEmail() + " bought " + quantity + " " + product.getName() +
                " .Revenue generated: " + revenue );
        this.revenue += revenue;
        
        // Award reward points: 1 point per dollar spent
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.getUserId(customer.getEmail());
            if (userId > 0) {
                int pointsToAward = (int) Math.floor(revenue);
                userDAO.addRewardPoints(userId, pointsToAward);
            }
        } catch (Exception e) {
            System.err.println("Error awarding reward points: " + e.getMessage());
        }
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<String> getSales() {
        return sales;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }
    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        // Append CSV representation of each product so that seller file lines
        // remain machine-parseable by DataMigrationService / ProductSearchService
        for (int i = 0; i < products.size(); i++) {
            sb.append(",");
            sb.append(products.get(i).toCSV());
        }
        return sb.toString();

    }
}
