package supermarket.test;

import supermarket.dao.CustomerDAO;
import supermarket.dao.ProductDAO;
import supermarket.db.DBConnection;
import supermarket.model.*;

import java.util.List;

public class SupermarketSystemTest {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  RUNNING SUPERMARKET BILLING SYSTEM TEST SUITE   ");
        System.out.println("==================================================");

        testProductHierarchyAndDiscounts();
        testCustomerValidationAndTiers();
        testCartCalculations();
        testDatabaseOperations();

        System.out.println("--------------------------------------------------");
        System.out.printf("Test Results: %d/%d Passed (%s)%n",
                testsPassed, testsRun, (testsPassed == testsRun ? "ALL PASS" : "SOME FAILED"));
        System.out.println("==================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void testProductHierarchyAndDiscounts() {
        System.out.println("[TEST] Product hierarchy and discount calculations");

        GroceryProduct grocery = new GroceryProduct("Apple 1kg", 100.0, 50, "01-12-2026");
        assertEquals("Grocery", grocery.getCategory(), "Grocery category check");
        assertEquals(0.05, grocery.getTaxRate(), 0.001, "Grocery GST rate check (5%)");
        assertEquals(10.0, grocery.calculateDiscount(100.0), 0.001, "Grocery 10% discount check");
        assertEquals("01-12-2026", grocery.getExtraAttribute(), "Grocery expiry date attribute");

        ElectronicProduct electronics = new ElectronicProduct("Mouse", 500.0, 20, 12);
        assertEquals("Electronic", electronics.getCategory(), "Electronics category check");
        assertEquals(0.18, electronics.getTaxRate(), 0.001, "Electronics GST rate check (18%)");
        assertEquals(25.0, electronics.calculateDiscount(500.0), 0.001, "Electronics 5% discount check");

        ClothingProduct clothing = new ClothingProduct("Jacket", 2000.0, 15, "XL");
        assertEquals("Clothing", clothing.getCategory(), "Clothing category check");
        assertEquals(0.12, clothing.getTaxRate(), 0.001, "Clothing GST rate check (12%)");
        assertEquals(300.0, clothing.calculateDiscount(2000.0), 0.001, "Clothing 15% discount check");
    }

    private static void testCustomerValidationAndTiers() {
        System.out.println("[TEST] Customer validation and loyalty tiers");

        Customer validCustomer = new Customer("Alice", "9876543210", "alice@test.com", true);
        assertTrue(validCustomer.isValidPhone(), "Valid phone number accepted");
        assertTrue(validCustomer.isValidEmail(), "Valid email address accepted");
        assertEquals("Silver (5% Member Discount)", validCustomer.getMembershipTier(), "Member gets Silver tier");

        Customer invalidPhone = new Customer("Bob", "12345", "bob@test.com", false);
        assertTrue(!invalidPhone.isValidPhone(), "Invalid phone rejected");

        Customer invalidEmail = new Customer("Charlie", "9876543210", "charlie_invalid", false);
        assertTrue(!invalidEmail.isValidEmail(), "Invalid email rejected");

        Customer goldCustomer = new Customer(101, "Diana", "9876543210", "diana@test.com", true, 250);
        assertTrue(goldCustomer.getMembershipTier().contains("Gold"), "Customer with 250 points gets Gold tier");
        assertEquals(0.07, goldCustomer.getMemberDiscountRate(), 0.001, "Gold customer receives 7% discount rate");
    }

    private static void testCartCalculations() {
        System.out.println("[TEST] Cart subtotal and quantity handling");

        GroceryProduct milk = new GroceryProduct(999, "Fresh Milk", 50.0, 10, "01-11-2026");
        Cart cart = new Cart();
        cart.addToCart(milk, 2);

        assertEquals(100.0, cart.calculateSubtotal(), 0.001, "Cart subtotal for 2 items");
        assertEquals(2, cart.getTotalItems(), "Cart item count");

        // Add more of same product
        cart.addToCart(milk, 3);
        assertEquals(250.0, cart.calculateSubtotal(), 0.001, "Updated subtotal after adding more");
        assertEquals(5, cart.getTotalItems(), "Updated quantity count");

        // Stock overflow check
        boolean caughtOverflow = false;
        try {
            cart.addToCart(milk, 100);
        } catch (IllegalArgumentException e) {
            caughtOverflow = true;
        }
        assertTrue(caughtOverflow, "Throws IllegalArgumentException on insufficient stock");
    }

    private static void testDatabaseOperations() {
        System.out.println("[TEST] Database connectivity and DAO queries");

        if (!DBConnection.testConnection()) {
            System.out.println("  Skipping DB tests (MySQL server not reachable in current environment)");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getAllProducts();
        assertTrue(!products.isEmpty(), "Products loaded from MySQL database");

        CustomerDAO customerDAO = new CustomerDAO();
        int count = customerDAO.getCustomerCount();
        assertTrue(count >= 0, "Customer count query executed successfully");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        testsRun++;
        if (expected.equals(actual)) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            System.err.printf("  ✗ FAILED: %s (Expected: %s, Got: %s)%n", message, expected, actual);
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        testsRun++;
        if (Math.abs(expected - actual) <= delta) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            System.err.printf("  ✗ FAILED: %s (Expected: %f, Got: %f)%n", message, expected, actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            System.err.println("  ✗ FAILED: " + message);
        }
    }
}
