package supermarket.dao;

import supermarket.db.DBConnection;
import supermarket.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public CustomerDAO() {
        ensureLoyaltyColumn();
    }

    private void ensureLoyaltyColumn() {
        String sql = "ALTER TABLE customers ADD COLUMN IF NOT EXISTS loyalty_points INT DEFAULT 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ignored) {
            // Column may already exist
        }
    }

    public int addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email, is_member, loyalty_points) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setBoolean(4, customer.isMember());
            stmt.setInt(5, customer.getLoyaltyPoints());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setCustomerId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving customer to database: " + e.getMessage());
        }
        return -1;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT customer_id, name, phone, email, is_member, loyalty_points FROM customers WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getBoolean("is_member"),
                            rs.getInt("loyalty_points")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, is_member, loyalty_points FROM customers ORDER BY customer_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getBoolean("is_member"),
                        rs.getInt("loyalty_points")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }
        return list;
    }

    public boolean addLoyaltyPoints(int customerId, int points) {
        if (points <= 0) return false;
        String sql = "UPDATE customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, points);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating loyalty points: " + e.getMessage());
            return false;
        }
    }

    public int getCustomerCount() {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer count: " + e.getMessage());
        }
        return 0;
    }
}
