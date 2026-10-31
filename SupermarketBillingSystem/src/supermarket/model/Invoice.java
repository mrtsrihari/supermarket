package supermarket.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Invoice {
    private static int nextInvoiceNumber = 5001;

    private int invoiceNumber;
    private final Customer customer;
    private final Cart cart;
    private final double subtotal;
    private final double discount;
    private final double tax;
    private final double total;
    private final LocalDateTime createdAt;

    public Invoice(Customer customer, Cart cart, double subtotal,
                   double discount, double tax, double total) {
        this.invoiceNumber = nextInvoiceNumber++;
        this.customer = customer;
        this.cart = cart;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }

    public Invoice(int invoiceNumber, Customer customer, Cart cart, double subtotal,
                   double discount, double tax, double total, LocalDateTime createdAt) {
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
        this.cart = cart;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        if (invoiceNumber >= nextInvoiceNumber) {
            nextInvoiceNumber = invoiceNumber + 1;
        }
    }

    public int getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(int invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Customer getCustomer() { return customer; }
    public Cart getCart() { return cart; }
    public double getSubtotal() { return subtotal; }
    public double getDiscount() { return discount; }
    public double getTax() { return tax; }
    public double getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String generateText() {
        StringBuilder out = new StringBuilder();
        out.append("\n===============================================\n");
        out.append("              SMART SUPERMARKET\n");
        out.append("===============================================\n");
        out.append("Invoice: ").append(invoiceNumber).append('\n');
        out.append("Date: ").append(createdAt.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append('\n');
        out.append("Customer: ").append(customer.getName()).append('\n');
        out.append("Phone: ").append(customer.getPhone()).append('\n');
        out.append("-----------------------------------------------\n");
        out.append(String.format("%-22s %-6s %-12s%n", "Product", "Qty", "Amount"));
        if (cart != null) {
            for (CartItem item : cart.getItems()) {
                out.append(String.format("%-22s %-6d ₹%-10.2f%n",
                        item.getProduct().getName(), item.getQuantity(), item.getTotal()));
            }
        }
        out.append("-----------------------------------------------\n");
        out.append(String.format("Subtotal       : ₹%.2f%n", subtotal));
        out.append(String.format("Discount       : ₹%.2f%n", discount));
        out.append(String.format("GST            : ₹%.2f%n", tax));
        out.append(String.format("TOTAL          : ₹%.2f%n", total));
        out.append("===============================================\n");
        out.append("       Thank you for shopping with us!\n");
        return out.toString();
    }
}
