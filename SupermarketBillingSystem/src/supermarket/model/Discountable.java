package supermarket.model;

public interface Discountable {
    double calculateDiscount(double amount);
    String getDiscountDescription();
}
