package supermarket.service;

import supermarket.dao.CustomerDAO;
import supermarket.dao.InvoiceDAO;
import supermarket.dao.ProductDAO;
import supermarket.db.DBConnection;
import supermarket.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Supermarket {
    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    private static final String STORE_NAME = "SMART SUPERMARKET";
    private static final double MEMBER_DISCOUNT = 0.05;

    public Supermarket() {
        DBConnection.initializeDatabase();
    }

    public String getStoreName() { return STORE_NAME; }

    public int getProductCount() {
        return productDAO.getAllProducts().size();
    }

    public int getCustomerCount() {
        return customerDAO.getCustomerCount();
    }

    public void addProduct(Product product) {
        productDAO.addProduct(product);
    }

    public boolean restockProduct(int productId, int quantity) {
        return productDAO.restockProduct(productId, quantity);
    }

    public boolean deleteProduct(int productId) {
        return productDAO.deleteProduct(productId);
    }

    public void addCustomer(Customer customer) {
        int id = customerDAO.addCustomer(customer);
        if (id > 0) {
            customer.setCustomerId(id);
        }
    }

    public Product findProduct(int id) {
        return productDAO.getProductById(id);
    }

    public void displayProducts() {
        List<Product> products = productDAO.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found in database.");
            return;
        }
        System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                "ID", "Name", "Category", "Price", "Stock");
        for (Product p : products) {
            System.out.println(p);
        }
    }

    public void searchProduct(String keyword) {
        List<Product> found = productDAO.searchByName(keyword);
        if (found.isEmpty()) {
            System.out.println("No matching products.");
        } else {
            System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                    "ID", "Name", "Category", "Price", "Stock");
            found.forEach(System.out::println);
        }
    }

    public void sortById() {
        List<Product> products = productDAO.getAllProducts();
        Collections.sort(products);
        System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                "ID", "Name", "Category", "Price", "Stock");
        products.forEach(System.out::println);
    }

    public void sortByName() {
        List<Product> products = productDAO.getAllProducts();
        products.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                "ID", "Name", "Category", "Price", "Stock");
        products.forEach(System.out::println);
    }

    public void sortByPrice() {
        List<Product> products = productDAO.getAllProducts();
        products.sort(Comparator.comparingDouble(Product::getPrice));
        System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                "ID", "Name", "Category", "Price", "Stock");
        products.forEach(System.out::println);
    }

    public void showProductsBelow(double maxPrice) {
        List<Product> products = productDAO.getProductsBelowPrice(maxPrice);
        if (products.isEmpty()) {
            System.out.println("No products found under ₹" + maxPrice);
            return;
        }
        System.out.printf("%-6s %-22s %-14s %-12s %-6s%n",
                "ID", "Name", "Category", "Price", "Stock");
        products.forEach(System.out::println);
    }

    public Invoice generateBill(Customer customer, Cart cart) {
        if (cart == null || cart.isEmpty()) throw new IllegalStateException("Cart is empty.");

        double subtotal = cart.calculateSubtotal();
        double productDiscount = 0.0;
        double tax = 0.0;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            double amount = item.getTotal();
            double discount = 0.0;

            if (product instanceof Discountable) {
                discount = ((Discountable) product).calculateDiscount(amount);
            }
            productDiscount += discount;
            tax += (amount - discount) * product.getTaxRate();
        }

        double memberDiscount = customer.isMember()
                ? (subtotal - productDiscount) * MEMBER_DISCOUNT : 0.0;
        double totalDiscount = productDiscount + memberDiscount;
        double total = subtotal - totalDiscount + tax;

        // Create the invoice
        Invoice invoice = new Invoice(customer, cart, subtotal, totalDiscount, tax, total);

        // Persist to MySQL database (saves invoice, items, and decrements stock)
        boolean saved = invoiceDAO.saveInvoice(invoice);
        if (saved) {
            System.out.println("✓ Bill recorded to MySQL database (Invoice #" + invoice.getInvoiceNumber() + ").");
        } else {
            System.err.println("⚠ Failed to record bill to MySQL database.");
        }

        return invoice;
    }

    public void saveInvoice(Invoice invoice) {
        String fileName = "Invoice_" + invoice.getInvoiceNumber() + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(invoice.generateText());
            System.out.println("Invoice saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Could not save invoice file: " + e.getMessage());
        }
    }

    public void displayCustomers() {
        List<Customer> list = customerDAO.getAllCustomers();
        if (list.isEmpty()) {
            System.out.println("No customers registered in database.");
            return;
        }
        list.forEach(System.out::println);
    }

    public void salesReport() {
        int count = invoiceDAO.getInvoiceCount();
        if (count == 0) {
            System.out.println("No sales recorded yet in database.");
            return;
        }
        double total = invoiceDAO.getTotalSales();
        double average = invoiceDAO.getAverageBill();
        System.out.println("\n===== DATABASE SALES REPORT =====");
        System.out.println("Total Invoices: " + count);
        System.out.printf("Total Sales   : ₹%.2f%n", total);
        System.out.printf("Average Bill  : ₹%.2f%n", average);
        System.out.println("=================================");
    }
}
