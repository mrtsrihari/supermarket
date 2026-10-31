package supermarket.app;

import supermarket.db.DBConnection;
import supermarket.model.*;
import supermarket.service.Supermarket;
import supermarket.exception.InvalidCustomerException;
import java.util.Scanner;

public class SupermarketBillingSystem {
    private static final Scanner input = new Scanner(System.in);
    private static Supermarket shop;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SMART SUPERMARKET BILLING SYSTEM           ");
        System.out.println("==================================================");

        // Test database connection
        if (DBConnection.testConnection()) {
            System.out.println("✓ Connected to MySQL database [supermarket_db]");
        } else {
            System.err.println("✗ Warning: Could not connect to MySQL database.");
        }

        shop = new Supermarket();

        mainMenu();
    }

    private static void mainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n----------------- MAIN MENU -----------------");
            System.out.println("1. Display Products");
            System.out.println("2. Search Product");
            System.out.println("3. Sort Products");
            System.out.println("4. Products Below Price");
            System.out.println("5. Create Bill");
            System.out.println("6. Display Customers");
            System.out.println("7. Sales Report");
            System.out.println("8. Restock Product");
            System.out.println("9. Add New Product");
            System.out.println("10. Exit");
            System.out.println("---------------------------------------------");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: shop.displayProducts(); break;
                case 2: searchProduct(); break;
                case 3: sortMenu(); break;
                case 4: shop.showProductsBelow(readDouble("Maximum price: ")); break;
                case 5: createBill(); break;
                case 6: shop.displayCustomers(); break;
                case 7: shop.salesReport(); break;
                case 8: restockMenu(); break;
                case 9: addNewProductMenu(); break;
                case 10: running = false; System.out.println("Thank you for using Smart Supermarket Billing System!"); break;
                default: System.out.println("Invalid choice. Please enter 1-10.");
            }
        }
    }

    private static void searchProduct() {
        System.out.print("Enter product name: ");
        shop.searchProduct(input.nextLine().trim());
    }

    private static void sortMenu() {
        System.out.println("1. Product ID  2. Name  3. Price");
        int choice = readInt("Sort choice: ");
        switch (choice) {
            case 1: shop.sortById(); break;
            case 2: shop.sortByName(); break;
            case 3: shop.sortByPrice(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void createBill() {
        try {
            System.out.println("\n--- Customer Details ---");
            System.out.print("Name: ");
            String name = input.nextLine().trim();
            System.out.print("Phone: ");
            String phone = input.nextLine().trim();
            System.out.print("Email: ");
            String email = input.nextLine().trim();
            System.out.print("Membership (Y/N): ");
            boolean member = input.nextLine().trim().equalsIgnoreCase("Y");

            Customer customer = new Customer(name, phone, email, member);

            if (name.isEmpty() || !customer.isValidPhone() || !customer.isValidEmail()) {
                throw new InvalidCustomerException("Enter a valid name, 10-digit phone and valid email.");
            }

            Cart cart = new Cart();
            addProductsToCart(cart);
            if (cart.isEmpty()) {
                System.out.println("Cart is empty. Bill cancelled.");
                return;
            }

            displayCart(cart);
            System.out.print("Generate bill (Y/N)? ");
            if (!input.nextLine().trim().equalsIgnoreCase("Y")) {
                System.out.println("Bill cancelled.");
                return;
            }

            shop.addCustomer(customer);
            Invoice invoice = shop.generateBill(customer, cart);
            System.out.println(invoice.generateText());
            shop.saveInvoice(invoice);

        } catch (InvalidCustomerException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addProductsToCart(Cart cart) {
        while (true) {
            shop.displayProducts();
            int id = readInt("Enter product ID (0 to finish): ");
            if (id == 0) break;

            Product product = shop.findProduct(id);
            if (product == null) {
                System.out.println("Product not found with ID: " + id);
                continue;
            }

            int quantity = readInt("Enter quantity: ");
            try {
                cart.addToCart(product, quantity);
                System.out.println("Added to cart.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void restockMenu() {
        shop.displayProducts();
        int id = readInt("Enter product ID to restock: ");
        Product p = shop.findProduct(id);
        if (p == null) {
            System.out.println("Product not found with ID: " + id);
            return;
        }
        int addQty = readInt("Enter quantity to add: ");
        if (addQty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }
        boolean ok = shop.restockProduct(id, addQty);
        if (ok) {
            System.out.println("✓ Successfully restocked " + p.getName() + " (+" + addQty + " units).");
        } else {
            System.out.println("✗ Failed to update stock in database.");
        }
    }

    private static void addNewProductMenu() {
        System.out.println("\n--- Add New Product ---");
        System.out.println("Select Category: 1. Grocery  2. Electronic  3. Clothing");
        int catChoice = readInt("Category choice: ");
        System.out.print("Product Name: ");
        String name = input.nextLine().trim();
        double price = readDouble("Price: ");
        int stock = readInt("Stock Quantity: ");

        Product newProduct = null;
        switch (catChoice) {
            case 1:
                System.out.print("Expiry Date (DD-MM-YYYY): ");
                String expiry = input.nextLine().trim();
                newProduct = new GroceryProduct(name, price, stock, expiry);
                break;
            case 2:
                int warranty = readInt("Warranty (months): ");
                newProduct = new ElectronicProduct(name, price, stock, warranty);
                break;
            case 3:
                System.out.print("Size (e.g. S, M, L, XL, 32, 34): ");
                String size = input.nextLine().trim();
                newProduct = new ClothingProduct(name, price, stock, size);
                break;
            default:
                System.out.println("Invalid category selected.");
                return;
        }

        try {
            shop.addProduct(newProduct);
            System.out.println("✓ Product '" + name + "' added to database inventory.");
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    private static void displayCart(Cart cart) {
        System.out.println("\n--- CART ---");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%-22s Qty: %-4d Amount: ₹%.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getTotal());
        }
        System.out.printf("Subtotal: ₹%.2f%n", cart.calculateSubtotal());
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}
