import java.util.ArrayList;

public class Product {
    private String name;
    private String storeName;
    private String description;
    private ArrayList<String> review;
    private int quantity;
    private double price;
    private ProductCategory category;

    // Backward-compatible constructor that defaults to SHOES category
    public Product(String name, int quantity, double price, String description, String storeName) {
        this.name = name;
        this.storeName = storeName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.category = ProductCategory.SHOES; // Default to SHOES for backward compatibility
    }

    // New constructor that accepts category parameter
    public Product(String name, int quantity, double price, String description, String storeName, ProductCategory category) {
        this.name = name;
        this.storeName = storeName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product) {
            return (((Product) o).getName().equals(this.name) && ((Product) o).getQuantity() == this.quantity && ((Product) o).getPrice()
                    == this.price && ((Product) o).getDescription().equals(this.description)
                    && ((Product) o).getStore().equals(this.storeName));
        }
        return false;
    }

    public ArrayList<String> getReview() {
        return review;
    }

    public void setReview(ArrayList<String> review) {
        this.review = review;
    }

    public void addReview(String input, String rev){
        review.add(input + ": " + rev);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStore() {
        return this.storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    @Override
    public String toString(){
        return String.format("%s,%d,%.2f,%s,%s,%s", this.name, this.quantity, this.price, this.description, this.storeName, this.category.name());
    }
}