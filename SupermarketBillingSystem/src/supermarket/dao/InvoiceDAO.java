package supermarket.dao;

import supermarket.db.DBConnection;
import supermarket.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    private final ProductDAO productDAO = new ProductDAO();

    public boolean saveInvoice(Invoice invoice) {
        String insertInvoiceSql = "INSERT INTO invoices (customer_id, subtotal, discount, tax, total, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO invoice_items (invoice_number, product_id, product_name, quantity, unit_price, total_price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int invoiceNumber = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertInvoiceSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, invoice.getCustomer().getCustomerId());
                stmt.setDouble(2, invoice.getSubtotal());
                stmt.setDouble(3, invoice.getDiscount());
                stmt.setDouble(4, invoice.getTax());
                stmt.setDouble(5, invoice.getTotal());
                stmt.setTimestamp(6, Timestamp.valueOf(invoice.getCreatedAt()));

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoiceNumber = rs.getInt(1);
                        invoice.setInvoiceNumber(invoiceNumber);
                    }
                }
            }

            if (invoiceNumber == -1) {
                conn.rollback();
                return false;
            }

            // Save items & update stock
            if (invoice.getCart() != null) {
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                    for (CartItem item : invoice.getCart().getItems()) {
                        Product p = item.getProduct();
                        itemStmt.setInt(1, invoiceNumber);
                        itemStmt.setInt(2, p.getProductId());
                        itemStmt.setString(3, p.getName());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setDouble(5, p.getPrice());
                        itemStmt.setDouble(6, item.getTotal());
                        itemStmt.addBatch();

                        // Deduct stock in DB
                        boolean updated = productDAO.updateStock(p.getProductId(), item.getQuantity(), conn);
                        if (!updated) {
                            conn.rollback();
                            System.err.println("Insufficient stock in database for product: " + p.getName());
                            return false;
                        }
                    }
                    itemStmt.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public int getInvoiceCount() {
        String sql = "SELECT COUNT(*) FROM invoices";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error fetching invoice count: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalSales() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM invoices";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating total sales: " + e.getMessage());
        }
        return 0.0;
    }

    public double getAverageBill() {
        String sql = "SELECT COALESCE(AVG(total), 0) FROM invoices";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating average bill: " + e.getMessage());
        }
        return 0.0;
    }

    public List<Invoice> getAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.invoice_number, i.customer_id, i.subtotal, i.discount, i.tax, i.total, i.created_at, " +
                "c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, c.is_member " +
                "FROM invoices i " +
                "JOIN customers c ON i.customer_id = c.customer_id " +
                "ORDER BY i.invoice_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer cust = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("customer_email"),
                        rs.getBoolean("is_member")
                );

                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                Invoice inv = new Invoice(
                        rs.getInt("invoice_number"),
                        cust,
                        null,
                        rs.getDouble("subtotal"),
                        rs.getDouble("discount"),
                        rs.getDouble("tax"),
                        rs.getDouble("total"),
                        createdAt
                );
                list.add(inv);
            }
        } catch (SQLException e) {
            System.err.println("Error reading invoices: " + e.getMessage());
        }
        return list;
    }

    public List<String> getTopSellingProducts(int limit) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT product_name, SUM(quantity) AS total_units, SUM(total_price) AS total_revenue " +
                     "FROM invoice_items " +
                     "GROUP BY product_name " +
                     "ORDER BY total_units DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(String.format("%-20s Sold: %-4d Revenue: ₹%.2f",
                            rs.getString("product_name"),
                            rs.getInt("total_units"),
                            rs.getDouble("total_revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top selling products: " + e.getMessage());
        }
        return list;
    }

    public java.util.Map<String, Double> getCategorySales() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.category, SUM(ii.total_price) AS category_revenue " +
                     "FROM invoice_items ii " +
                     "JOIN products p ON ii.product_id = p.product_id " +
                     "GROUP BY p.category " +
                     "ORDER BY category_revenue DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("category_revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error calculating category sales: " + e.getMessage());
        }
        return map;
    }
}

