public class QuickVerify {
    public static void main(String[] args) {
        Product p = new Product("iPad", 100, 399.99, "No description provided", "Apple", ProductCategory.ELECTRONICS);
        Store s = new Store("Apple");
        s.addProduct(p);
        String line = "seller@example.com," + s.toString();
        System.out.println("Written line:\n" + line);
        try {
            java.util.ArrayList<Product> products = DataMigrationService.parseProductsFromLine(line);
            System.out.println("Parsed products count: " + products.size());
            for (Product pr : products) {
                System.out.println("Parsed CSV: " + pr.toCSV());
            }
        } catch (Exception e) {
            System.err.println("Error parsing: " + e.getMessage());
        }
    }
}
