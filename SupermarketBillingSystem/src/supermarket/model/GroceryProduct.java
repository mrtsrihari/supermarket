package supermarket.model;

public class GroceryProduct extends Product implements Discountable {
    private String expiryDate;

    public GroceryProduct(String name, double price, int quantity, String expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    public GroceryProduct(int productId, String name, double price, int quantity, String expiryDate) {
        super(productId, name, price, quantity);
        this.expiryDate = expiryDate;
    }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    @Override public String getCategory() { return "Grocery"; }
    @Override public double getTaxRate() { return 0.05; }
    @Override public double calculateDiscount(double amount) { return amount * 0.10; }
    @Override public String getDiscountDescription() { return "10% Grocery Discount"; }
    @Override public String getExtraAttribute() { return expiryDate; }
}
