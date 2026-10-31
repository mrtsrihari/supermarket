package supermarket.dao;

import supermarket.db.DBConnection;
import supermarket.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, extra_attribute FROM products ORDER BY product_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product p = mapRowToProduct(rs);
                if (p != null) {
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }

    public Product getProductById(int productId) {
        String sql = "SELECT product_id, name, category, price, quantity, extra_attribute FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Product> searchByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, extra_attribute FROM products WHERE LOWER(name) LIKE ? ORDER BY product_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRowToProduct(rs);
                    if (p != null) products.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        return products;
    }

    public List<Product> getProductsBelowPrice(double maxPrice) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, extra_attribute FROM products WHERE price <= ? ORDER BY price ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, maxPrice);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRowToProduct(rs);
                    if (p != null) products.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering products by price: " + e.getMessage());
        }
        return products;
    }

    public int addProduct(Product product) {
        String sql = "INSERT INTO products (name, category, price, quantity, extra_attribute) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setString(5, product.getExtraAttribute());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateStock(int productId, int quantityDeducted, Connection conn) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityDeducted);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantityDeducted);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String name = rs.getString("name");
        String category = rs.getString("category");
        double price = rs.getDouble("price");
        int quantity = rs.getInt("quantity");
        String extra = rs.getString("extra_attribute");

        if (category == null) category = "Grocery";

        switch (category.toLowerCase()) {
            case "grocery":
                return new GroceryProduct(id, name, price, quantity, extra);
            case "electronic":
                int warranty = 12;
                try {
                    warranty = Integer.parseInt(extra);
                } catch (NumberFormatException ignored) {}
                return new ElectronicProduct(id, name, price, quantity, warranty);
            case "clothing":
                return new ClothingProduct(id, name, price, quantity, extra);
            default:
                return new GroceryProduct(id, name, price, quantity, extra);
        }
    }
}

