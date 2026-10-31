package supermarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<CartItem>();

    public void addToCart(Product product) { addToCart(product, 1); }

    public void addToCart(Product product, int quantity) {
        if (product == null) throw new IllegalArgumentException("Product not found.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");

        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                int updated = item.getQuantity() + quantity;
                if (updated > product.getQuantity()) throw new IllegalArgumentException("Insufficient stock.");
                item.setQuantity(updated);
                return;
            }
        }
        if (quantity > product.getQuantity()) throw new IllegalArgumentException("Insufficient stock.");
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    public boolean isEmpty() { return items.isEmpty(); }
    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotal).sum();
    }
}
