package supermarket.model;

public abstract class Product implements Comparable<Product> {
    private static int nextId = 1001;
    public static final double MAX_DISCOUNT = 0.30;

    private final int productId;
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product details.");
        }
        this.productId = nextId++;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(int productId, String name, double price, int quantity) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product details.");
        }
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        if (productId >= nextId) {
            nextId = productId + 1;
        }
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name;
    }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.quantity = quantity;
    }

    public abstract String getCategory();
    public abstract double getTaxRate();
    public abstract String getExtraAttribute();

    public double calculateTax(double taxableAmount) {
        return taxableAmount * getTaxRate();
    }

    @Override
    public int compareTo(Product other) {
        return Integer.compare(productId, other.productId);
    }

    @Override
    public String toString() {
        return String.format("%-6d %-22s %-14s ₹%-10.2f %-6d",
                productId, name, getCategory(), price, quantity);
    }
}
