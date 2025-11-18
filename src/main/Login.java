import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Scanner;
import java.io.*;
import java.util.function.BiFunction;

public class Login {
    public static final String WELCOME_PROMPT = "Welcome to our Marketplace!";
    public static final String ENTER_YOUR_EMAIL = "Please enter your e-mail.";
    public static final String SIGN_IN_PROMPT = "1: Sign In\n2: Create an account.";

    public static final String LOGIN_PASSWORD_PROMPT = "Please enter your password.";

    public static final String CREATE_PASSWORD_PROMPT = "Please enter a password greater than 5 characters.";

    public static final String BUYER_OR_SELLER = "1: Customer\n2: Seller";

    private static ArrayList<Seller> sellers = new ArrayList<>();

    public static void loadMarket() {
        File f = new File("Sellers.txt");
        if (f.exists()) {
            try (BufferedReader bfr = new BufferedReader(new FileReader(f))) {
                String line = "";
                ArrayList<String> sellerInfo = new ArrayList<>();
                Seller seller = new Seller("");
                while ((line = bfr.readLine()) != null) {
                    String[] arr = line.split(",");
                    if (arr.length > 2) {
                        boolean no = false;
                        int index = sellers.size();
                        for (int i = 0; i < sellers.size(); i++) {
                            if (arr[0].equals(sellers.get(i).getEmail())) {
                                no = true;
                                index = i;
                            }
                        }

                        if (!no) {
                            seller = new Seller(arr[0]);
                            sellers.add(seller);
                        }
                        Store store = new Store(arr[1]);
                        for (int i = 2; i < arr.length; i += 5) {
                            Product product = new Product(arr[i], Integer.parseInt(arr[i + 1]),
                                    Double.parseDouble(arr[i + 2]), arr[i + 3], arr[i + 4]);
                            store.addProduct(product);
                        }
                        seller.addStores(store.getName());
                        sellers.set(index, seller);
                    } else {
                        /**
                         * WE HOPE THIS FUCKING WORKS REMEMBER IT!!!!!!!
                         */
                        boolean no = false;
                        int index = sellers.size();
                        for (int i = 0; i < sellers.size(); i++) {
                            if (arr[0].equals(sellers.get(i).getEmail())) {
                                no = true;
                                index = i;
                            }
                        }

                        if (!no) {
                            seller = new Seller(arr[0]);
                            sellers.add(seller);
                        }
                        Store store = new Store(arr[1]);
                        seller.addStores(store.getName());
                        sellers.set(index, seller);
                    }
                }

                sellers.add(new Seller(""));
            } catch (IOException e) {
                System.out.println("Error reading to the sellers file.");
            }
        } else {
            try {
                boolean b = f.createNewFile();
            } catch (IOException e) {
                System.out.println("There was an error creating the sellers file.");
            }
        }

    }


    public static void main(String[] args) {
        String email = "";
        String password = "";
        String userType = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println(WELCOME_PROMPT);
        System.out.println(SIGN_IN_PROMPT);
        String response = scanner.nextLine();


        while (!"1".equals(response) && !"2".equals(response)) {
            System.out.println("Please either select 1 or 2.");
            response = scanner.nextLine();
        }


        if ("1".equals(response)) { // sign in
            System.out.println(ENTER_YOUR_EMAIL);
            email = scanner.nextLine();

            System.out.println(LOGIN_PASSWORD_PROMPT);
            password = scanner.nextLine();

            File f = new File("Accounts.txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                ArrayList<String> accountInfo = new ArrayList<>();
                String line = "";
                while ((line = br.readLine()) != null) {
                    accountInfo.add(line);
                }
                br.close();
                ArrayList<String> emails = new ArrayList<>();
                for (int i = 0; i < accountInfo.size(); i++) {
                    emails.add(accountInfo.get(i).split(",")[0]);
                }
                while (!emails.contains(email)) {
                    System.out.println("This e-mail does not exist in our database.");
                    System.out.println(ENTER_YOUR_EMAIL);
                    email = scanner.nextLine();
                }
                int index = emails.indexOf(email);
                while (!accountInfo.get(index).split(",")[1].equals(password)) {
                    System.out.println("Incorrect Password.");
                    System.out.println(LOGIN_PASSWORD_PROMPT);
                    password = scanner.nextLine();
                }
                System.out.println("Login successful!");


                if (accountInfo.get(index).split(",")[2].equals("SELLER")) {
                    userType = "SELLER";
                } else {
                    userType = "BUYER";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else { // create account
            System.out.println(ENTER_YOUR_EMAIL);
            email = scanner.nextLine();

            while (!email.contains("@") && (!email.contains(".com") || !email.contains(".edu") || !email.contains(".gov"))) {
                System.out.println("Invalid e-mail.");
                System.out.println(ENTER_YOUR_EMAIL);
                email = scanner.nextLine();
            }

            File f = new File("Accounts.txt");
            if (f.exists()) {
                try (BufferedReader bfr = new BufferedReader(new FileReader("Accounts.txt"))) {
                    String line = "";
                    while ((line = bfr.readLine()) != null) {
                        String[] arr = line.split(",");
                        while (arr[0].equals(email)) {
                            System.out.println("This e-mail has already been taken.");
                            System.out.println(ENTER_YOUR_EMAIL);
                            email = scanner.nextLine();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Unable to read to the accounts file.");
                }

                System.out.println(CREATE_PASSWORD_PROMPT);
                password = scanner.nextLine();
                if (password == null || password.length() <= 5) { // case of invalid password
                    boolean validPassword = false;
                    while (!validPassword) {
                        System.out.println("Invalid password. Please enter a valid Password:");
                        password = scanner.nextLine();
                        if (password != null && password.length() > 5) {
                            validPassword = true;
                        }
                    }
                }

                System.out.println(BUYER_OR_SELLER);
                userType = scanner.nextLine();
                while (!"1".equals(userType) && !"2".equals(userType)) {
                    System.out.println("Please enter either 1 or 2.");
                }
                if ("1".equals(userType)) {
                    userType = "CUSTOMER";
                } else {
                    userType = "SELLER";
                    Seller seller = new Seller(email);
                    sellers.add(seller); // hope it works
                }


                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Accounts.txt", true))) {
                    bwr.write(email + "," + password + "," + userType + "\n");
                } catch (IOException io) {
                    System.out.println("Error writing to the accounts file.");
                }
            } else {
                f = new File("Accounts.txt");
                System.out.println(CREATE_PASSWORD_PROMPT);
                password = scanner.nextLine();
                if (password == null || password.length() <= 5) { // case of invalid password
                    boolean validPassword = false;
                    while (!validPassword) {
                        System.out.println("Invalid password. Please enter a valid Password:");
                        password = scanner.nextLine();
                        if (password != null && password.length() > 5) {
                            validPassword = true;
                        }
                    }
                }

                System.out.println(BUYER_OR_SELLER);
                userType = scanner.nextLine();
                while (!"1".equals(userType) && !"2".equals(userType)) {
                    System.out.println("Please enter either 1 or 2.");
                }
                if ("1".equals(userType)) {
                    userType = "CUSTOMER";
                } else {
                    userType = "SELLER";
                    Seller seller = new Seller(email);
                    sellers.add(seller); // hope it works
                }
                try (BufferedWriter bwr = new BufferedWriter(new FileWriter("Accounts.txt", true))) {
                    bwr.write(email + "," + password + "," + userType + "\n");
                } catch (IOException e) {
                    System.out.println("Error writing to the accounts file.");
                }
            }
        }

        loadMarket();


        if (userType.equals("SELLER")) {
            Seller s = new Seller(email);
            int index = 0;
            if (sellers.size() == 1) {
                sellers.set(0, s);
                index = sellers.indexOf(s);
            }

            for (int i = 0; i < sellers.size(); i++) {
                if (sellers.get(i).getEmail().equals(email)) {
                    index = i;
                    break;
                }
            }


            String performActivity = "";
            do {
                /**
                 * below are the general variables we will use in your menu
                 */
                // ____________
                String storeName = "";

                // ____________
                System.out.println("Menu");
                System.out.println("1: Add a store");
                System.out.println("2: Add a product one of your Stores");
                System.out.println("3: Remove a product from one of your Stores");
                System.out.println("4: Edit a product from one of your Stores");
                System.out.println("5: View your stores and their details");
                System.out.println("6: View a customer shopping cart");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        System.out.println("What is the name of the store you would like to add?");
                        storeName = scanner.nextLine();
                        sellers.get(index).addStores(storeName);
                        sellers.get(index).writeToSellerFileAddStore(storeName);
                        break;
                    case 2:
                        System.out.println("What is the name of the store you would like to add a product to?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            System.out.println("What is the name of your product?");
                            String productName = scanner.nextLine();
                            System.out.println("How many products do you want to manufacture?");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What will the price of your product be?");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of your product?");
                            String description = scanner.nextLine();
                            sellers.get(index).createProduct(storeName, productName, quantity, price, description);
                            sellers.get(index).writeToSellerFileAddProduct(storeName, productName, quantity, price, description);
                        } else {
                            System.out.println("Sorry, you are not affiliated with " + storeName);
                        }
                        break;
                    case 3:
                        System.out.println("What is the name of the store you would to remove a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            System.out.println("What is the name of your product?");
                            String productName = scanner.nextLine();
                            System.out.println("How many products do you want to manufacture?");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What will the price of your product be?");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of your product?");
                            String description = scanner.nextLine();
                            Product product = new Product(productName, quantity, price, description, storeName);
                            sellers.get(index).removeProduct(storeName, product);
                            sellers.get(index).writeToSellerFileRemoveProduct(storeName, productName, quantity, price, description);
                        }
                        break;
                    case 4:
                        System.out.println("What is the name of the store you would like to edit a product from?");
                        storeName = scanner.nextLine();
                        if (sellers.get(index).checkIfStoreExists(storeName)) {
                            System.out.println("What is the name of your old product?");
                            String productName = scanner.nextLine();
                            System.out.println("What was the quantity of the old products");
                            int quantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What was the price of the old product");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of the old product");
                            String description = scanner.nextLine();
                            Product oldProduct = new Product(productName, quantity, price, description, storeName);

                            System.out.println("What is the name of your new product?");
                            String newProductName = scanner.nextLine();
                            System.out.println("What is the quantity of the new products");
                            int newQuantity = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("What is the price of the new product");
                            double newPrice = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.println("What is the description of the new product");
                            String newDescription = scanner.nextLine();
                            sellers.get(index).editProduct(oldProduct, newProductName, newDescription, storeName, newQuantity, newPrice);
                            sellers.get(index).writerToSellerFileEditProduct(productName, quantity, price, description, storeName,
                                    newProductName, newQuantity, newPrice, newDescription);
                        }
                        break;
                    case 5:
                        for (Store store : sellers.get(index).getStores()) {
                            System.out.println(store.toString());
                        }
                        break;
                    case 6:
                        System.out.println("This doesn't fucking work dumbass.");
                        break;
                    default:
                        System.out.println("YOOOOOO");
                }
                System.out.println("Would you like to perform another activity?");
                performActivity = scanner.nextLine();

            } while ("Yes".equalsIgnoreCase(performActivity) || "y".equalsIgnoreCase(performActivity));
            /**
             * END OF SELLER IMPLEMENTATION
             */

        } else if (userType.equals("CUSTOMER")) {
            System.out.println("WELCOME CUSTOMER!");
            Customer customer = new Customer(email);
            System.out.println("Customer Menu");
            System.out.println("1: Search the market and the various products it has to offer");
            System.out.println("2: View your shopping cart");
            System.out.println("3: View your purchase history");
            System.out.println("4: Enter a review for a product you have purchased");
            int choice3 = scanner.nextInt();
            scanner.nextLine();
            switch (choice3) {
                case 1:
                    System.out.println("On what basis would you like ro search by?");
                    System.out.println("1. NAME");
                    System.out.println("2. PRICE");
                    System.out.println("3. STORE");
                    System.out.println("4. DESCRIPTION");
                    System.out.println("5. QUANTITY");
                    System.out.println("6. NO FILTERS, VIEW ENTIRE MARKETPLACE");
                    int choice4 = scanner.nextInt();
                    scanner.nextLine();
                    switch (choice4) {
                        case 1:
                            System.out.println("What is the name of the product you wish to search by:");
                            String searchName = scanner.nextLine();
                            try {
                                BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                                ArrayList<String> searchByName = new ArrayList<>();
                                String line = "";
                                while ((line = bfr.readLine()) != null) {
                                    String[] arr = line.split(",");
                                    if (searchName.equalsIgnoreCase(arr[2])) {
                                        searchByName.add(line);
                                    }
                                }
                                for (int i = 0; i < searchByName.size(); i++) {
                                    System.out.println(searchByName.get(i));
                                }
                                
                            } catch (IOException io) {
                                System.out.println();
                            }
                            break;
                        case 2:
                            System.out.println("What is the threshold price of your search?");
                            double searchPrice = scanner.nextDouble();
                            scanner.nextLine();
                            try {
                                BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                                ArrayList<String> searchByPrice = new ArrayList<>();
                                String line = "";
                                while ((line = bfr.readLine()) != null) {
                                    String[] arr = line.split(",");
                                    if (searchPrice == Double.parseDouble(arr[3])) {
                                        searchByPrice.add(line);
                                    }
                                }
                                for (int i = 0; i < searchByPrice.size(); i++) {
                                    System.out.println(searchByPrice.get(i));
                                }

                            } catch (IOException io) {
                                System.out.println();
                            }
                            
                            break;
                        case 3:
                            System.out.println("What is the name of the store you would like to search in?");
                            String searchStore = scanner.nextLine();
                            try {
                            BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                            ArrayList<String> searchByStore = new ArrayList<>();
                            String line = "";
                            while ((line = bfr.readLine()) != null) {
                                String[] arr = line.split(",");
                                if (searchStore.equalsIgnoreCase(arr[1])) {
                                    searchByStore.add(line);
                                }
                            }
                            for (int i = 0; i < searchByStore.size(); i++) {
                                System.out.println(searchByStore.get(i));
                            }

                        } catch (IOException io) {
                            System.out.println();
                        }
                            break;
                        case 4:
                            System.out.println("What is the description of product you wish to purchase?");
                            String searchDescription = scanner.nextLine();
                            try {
                                BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                                ArrayList<String> searchByDesc = new ArrayList<>();
                                String line = "";
                                while ((line = bfr.readLine()) != null) {
                                    String[] arr = line.split(",");
                                    if (searchDescription.equalsIgnoreCase(arr[5])) {
                                        searchByDesc.add(line);
                                    }
                                }
                                for (int i = 0; i < searchByDesc.size(); i++) {
                                    System.out.println(searchByDesc.get(i));
                                }

                            } catch (IOException io) {
                                System.out.println();
                            }
                            break;
                        case 5:
                            System.out.println("Displaying all the in-stock products:");
                            try {
                                BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                                ArrayList<String> searchByQuantity = new ArrayList<>();
                                String line = "";
                                while ((line = bfr.readLine()) != null) {
                                    String[] arr = line.split(",");
                                    if (Integer.parseInt(arr[4]) != 0) {
                                        searchByQuantity.add(line);
                                    }
                                }
                                for (int i = 0; i < searchByQuantity.size(); i++) {
                                    System.out.println(searchByQuantity.get(i));
                                }

                            } catch (IOException io) {
                                System.out.println();
                            }
                            break;
                        case 6:
                            System.out.println("Displaying the entire marketplace:");
                            try {
                                BufferedReader bfr = new BufferedReader(new FileReader("Sellers.txt"));
                                ArrayList<String> market = new ArrayList<>();
                                String line = "";
                                while ((line = bfr.readLine()) != null) {
                                    market.add(line);
                                    }
                                for (int i = 0; i < market.size(); i++) {
                                    System.out.println(market.get(i));
                                }

                            } catch (IOException io) {
                                System.out.println();
                            }
                            break;
                        default:
                            System.out.println("Please enter a valid choice!");
                            break;
                    }
                    break;
                case 2:
                    customer.viewCart(customer.email);
                    System.out.println("Shopping Cart Menu");
                    System.out.println("1: Checkout");
                    System.out.println("2: Add item to the shopping cart");
                    int choice5 = scanner.nextInt();
                    scanner.nextLine();
                    switch (choice5) {
                        case 1:
                            try {
                                BufferedReader br = new BufferedReader(new FileReader(email));
                                ArrayList<String> lines = new ArrayList<>();
                                ArrayList<String> cart = new ArrayList<>();
                                String line = "";
                                boolean trip = false;
                                while ((line = br.readLine()) != null) {
                                    lines.add(line);
                                    if (line.equals("-------")) {
                                        trip = true;
                                    }
                                    if (trip) {
                                        cart.add(line);
                                    }
                                }
                                ArrayList<Product> passCart = new ArrayList<>();
                                for (int i = 0; i < cart.size(); i++) {
                                    String[] temp = cart.get(i).split(",");
                                    Product product = new Product(temp[0], Integer.parseInt(temp[1]), Double.parseDouble(temp[2]), temp[3], temp[4]);
                                    passCart.add(product);
                                }
                                for (int i = 0; i < passCart.size(); i++) {
                                    for (int j = 0; j < sellers.size(); j++) {
                                        for (int k = 0; k < sellers.get(i).getStores().size(); k++) {
                                            if (sellers.get(i).getStores().get(k).equals(passCart.get(i).getStore())) {
                                                sellers.get(i).getStores().get(k).processPurchase(passCart.get(i).getName(), passCart.get(i).getQuantity(), customer);
                                            }
                                        }
                                    }
                                }
                                boolean trip1 = false;
                                PrintWriter pw = new PrintWriter(new FileWriter(customer.getEmail(), true));
                                for (int i = 0; i < lines.size(); i++) {
                                    if (lines.get(i).equals("-------")) {
                                        trip1 = true;
                                    }
                                    if (!trip1) {
                                        pw.append(lines.get(i));
                                    }
                                }
                                for (int i = 0; i < passCart.size(); i++) {
                                    pw.append(passCart.get(i).toString());
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            System.out.println("Enter the name of the item you wish to add to the cart");
                            String item = scanner.nextLine();

                            for (int i = 0; i < sellers.size(); i++) {
                                for (int j = 0; j < sellers.get(i).getStores().size(); j++) {
                                    for (int k = 0; k < sellers.get(i).getStores().get(i).getProducts().size(); k++) {
                                        if (sellers.get(i).getStores().get(i).getProducts().get(k).getName().equals(item)) {
                                            customer.addToCart(sellers.get(i).getStores().get(i).getProducts().get(k));
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            System.out.println("Enter a valid choice!");
                    }
                case 3:
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(customer.getEmail()));
                        System.out.println(customer.getEmail() + "'s purchase history: \n");
                        String line = br.readLine();
                        boolean trip = false;
                        while ((line = br.readLine()) != null) {
                            if (line.equals("-------")) {
                                trip = true;
                            }
                            if (!trip) {
                                System.out.println(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    System.out.println("Enter the name of the product whose review you want to give");
                    String productName = scanner.nextLine();
                    System.out.println("Enter the name of the store you bought it from");
                    String storebought = scanner.nextLine();
                    try {
                        int a=0,b=0,c=0;
                        boolean trip = false;
                        BufferedReader br = new BufferedReader(new FileReader(customer.getEmail()));
                        String line2 = br.readLine();
                        while(line2 != null){
                            String[] temp = line2.split(",");
                            if(temp[0].equals("Name: " + productName) && temp[4].equals("Store: " + storebought)){
                                trip = true;
                            }
                            if(trip){
                                System.out.println("Enter the review you would like to send");
                                String review = scanner.nextLine();

                                for(int i = 0; i < sellers.size(); i++){
                                    for(int j = 0; j < sellers.get(i).getStores().size(); j++){
                                        for(int k = 0; k < sellers.get(i).getStores().get(i).getProducts().size(); k++){
                                            if(sellers.get(i).getStores().get(i).getProducts().get(i).getName().equals(productName) && sellers.get(i).getStores().get(i).getProducts().get(i).getStore().equals(storebought)){
                                                sellers.get(i).getStores().get(i).getProducts().get(i).addReview(customer.getEmail(), review);
                                                a = i;
                                                b = j;
                                                c = k;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        if(!trip){
                            System.out.println("You havent bought that product!!");
                        } else {
                            PrintWriter pw = new PrintWriter(new FileWriter(sellers.get(a).getEmail()));
                            BufferedReader bfr = new BufferedReader(new FileReader(sellers.get(a).getEmail()));
                            String first = bfr.readLine();
                            pw.write(first + "\n");
                            for(int i = 0; i < sellers.get(a).getStores().size(); i++){
                                pw.append(sellers.get(a).getStores().get(i).toString() + "\n");
                                for(int j = 0; j < sellers.get(a).getStores().get(i).getProducts().size(); i++){
                                    pw.append(sellers.get(a).getStores().get(i).getProducts().get(j).toString() + "\n");
                                }
                            }
                            pw.close();
                            bfr.close();
                        }
                        br.close();

                    } catch (IOException e){
                        e.printStackTrace();
                    }


            }
            // customer viewing page
        }
    }
}
