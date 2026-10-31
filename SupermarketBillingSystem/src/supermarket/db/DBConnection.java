package supermarket.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DB_NAME = "supermarket_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Hari123@";

    private static final String SERVER_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private static String getUser() {
        String envUser = System.getenv("DB_USER");
        return (envUser != null && !envUser.trim().isEmpty()) ? envUser : DEFAULT_USER;
    }

    private static String getPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        return (envPass != null) ? envPass : DEFAULT_PASSWORD;
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, getUser(), getPassword());
        } catch (SQLException e) {
            // If the database does not exist yet, initialize it
            initializeDatabase();
            return DriverManager.getConnection(DB_URL, getUser(), getPassword());
        }
    }

    public static synchronized void initializeDatabase() {
        String user = getUser();
        String pass = getPassword();

        // 1. Create database if it doesn't exist
        try (Connection conn = DriverManager.getConnection(SERVER_URL, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            System.err.println("Could not create database " + DB_NAME + ": " + e.getMessage());
        }

        // 2. Create tables and initial data
        try (Connection conn = DriverManager.getConnection(DB_URL, user, pass);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products (" +
                "  product_id INT PRIMARY KEY AUTO_INCREMENT," +
                "  name VARCHAR(100) NOT NULL," +
                "  category VARCHAR(50) NOT NULL," +
                "  price DECIMAL(10, 2) NOT NULL," +
                "  quantity INT NOT NULL," +
                "  extra_attribute VARCHAR(100) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") AUTO_INCREMENT = 1001"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "  customer_id INT PRIMARY KEY AUTO_INCREMENT," +
                "  name VARCHAR(100) NOT NULL," +
                "  phone VARCHAR(20) NOT NULL," +
                "  email VARCHAR(100) NOT NULL," +
                "  is_member BOOLEAN DEFAULT FALSE," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") AUTO_INCREMENT = 10001"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS invoices (" +
                "  invoice_number INT PRIMARY KEY AUTO_INCREMENT," +
                "  customer_id INT NOT NULL," +
                "  subtotal DECIMAL(10, 2) NOT NULL," +
                "  discount DECIMAL(10, 2) NOT NULL," +
                "  tax DECIMAL(10, 2) NOT NULL," +
                "  total DECIMAL(10, 2) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                ") AUTO_INCREMENT = 5001"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS invoice_items (" +
                "  item_id INT PRIMARY KEY AUTO_INCREMENT," +
                "  invoice_number INT NOT NULL," +
                "  product_id INT NOT NULL," +
                "  product_name VARCHAR(100) NOT NULL," +
                "  quantity INT NOT NULL," +
                "  unit_price DECIMAL(10, 2) NOT NULL," +
                "  total_price DECIMAL(10, 2) NOT NULL," +
                "  FOREIGN KEY (invoice_number) REFERENCES invoices(invoice_number) ON DELETE CASCADE," +
                "  FOREIGN KEY (product_id) REFERENCES products(product_id)" +
                ")"
            );

            // Seed initial sample products if empty
            stmt.executeUpdate(
                "INSERT INTO products (name, category, price, quantity, extra_attribute) " +
                "SELECT * FROM (" +
                "  SELECT 'Rice 5kg' AS name, 'Grocery' AS category, 350.00 AS price, 50 AS quantity, '30-12-2026' AS extra_attribute UNION ALL " +
                "  SELECT 'Milk 1L', 'Grocery', 60.00, 100, '05-11-2026' UNION ALL " +
                "  SELECT 'Bread', 'Grocery', 45.00, 80, '03-11-2026' UNION ALL " +
                "  SELECT 'Headphones', 'Electronic', 1500.00, 25, '12' UNION ALL " +
                "  SELECT 'Keyboard', 'Electronic', 900.00, 30, '24' UNION ALL " +
                "  SELECT 'Cotton Shirt', 'Clothing', 799.00, 40, 'L' UNION ALL " +
                "  SELECT 'Blue Jeans', 'Clothing', 1299.00, 35, '32'" +
                ") AS tmp " +
                "WHERE NOT EXISTS (SELECT 1 FROM products)"
            );

        } catch (SQLException e) {
            System.err.println("Error setting up database tables: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}

