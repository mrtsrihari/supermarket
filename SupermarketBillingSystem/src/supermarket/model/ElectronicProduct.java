package supermarket.model;

public class ElectronicProduct extends Product implements Discountable {
    private int warrantyMonths;

    public ElectronicProduct(String name, double price, int quantity, int warrantyMonths) {
        super(name, price, quantity);
        this.warrantyMonths = warrantyMonths;
    }

    public ElectronicProduct(int productId, String name, double price, int quantity, int warrantyMonths) {
        super(productId, name, price, quantity);
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int months) {
        if (months < 0) throw new IllegalArgumentException("Warranty cannot be negative.");
        warrantyMonths = months;
    }

    @Override public String getCategory() { return "Electronic"; }
    @Override public double getTaxRate() { return 0.18; }
    @Override public double calculateDiscount(double amount) { return amount * 0.05; }
    @Override public String getDiscountDescription() { return "5% Electronics Discount"; }
    @Override public String getExtraAttribute() { return String.valueOf(warrantyMonths); }
}
