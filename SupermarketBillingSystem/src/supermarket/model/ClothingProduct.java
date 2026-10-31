package supermarket.model;

public class ClothingProduct extends Product implements Discountable {
    private String size;

    public ClothingProduct(String name, double price, int quantity, String size) {
        super(name, price, quantity);
        this.size = size;
    }

    public ClothingProduct(int productId, String name, double price, int quantity, String size) {
        super(productId, name, price, quantity);
        this.size = size;
    }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    @Override public String getCategory() { return "Clothing"; }
    @Override public double getTaxRate() { return 0.12; }
    @Override public double calculateDiscount(double amount) { return amount * 0.15; }
    @Override public String getDiscountDescription() { return "15% Clothing Discount"; }
    @Override public String getExtraAttribute() { return size; }
}
