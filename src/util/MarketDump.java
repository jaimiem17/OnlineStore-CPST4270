import java.util.ArrayList;

public class MarketDump {
    public static void main(String[] args) {
        System.out.println("Reading marketplace from: " + FileConstants.SELLERS_FILE);
        ArrayList<Product> all = ProductSearchService.getAllProducts();
        System.out.println("Total products found: " + all.size());
        for (Product p : all) {
            System.out.println(p.toCSV());
        }
    }
}
