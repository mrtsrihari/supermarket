package supermarket.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {
    private static final Properties props = new Properties();

    private static String host = "localhost";
    private static int port = 3306;
    private static String dbName = "supermarket_db";
    private static String user = "root";
    private static String password = "Hari123@";
    private static boolean useSSL = false;
    private static boolean allowPublicKeyRetrieval = true;
    private static String serverTimezone = "UTC";

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Could not load db.properties: " + e.getMessage());
            }
        }

        host = getVal("db.host", "DB_HOST", host);
        String portStr = getVal("db.port", "DB_PORT", String.valueOf(port));
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ignored) {}

        dbName = getVal("db.name", "DB_NAME", dbName);
        user = getVal("db.user", "DB_USER", user);
        password = getVal("db.password", "DB_PASSWORD", password);
    }

    private static String getVal(String propKey, String envKey, String fallback) {
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) return env.trim();
        String p = props.getProperty(propKey);
        if (p != null && !p.trim().isEmpty()) return p.trim();
        return fallback;
    }

    public static String getDbUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                "?useSSL=" + useSSL +
                "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval +
                "&serverTimezone=" + serverTimezone;
    }

    public static String getServerUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/?useSSL=" + useSSL +
                "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval +
                "&serverTimezone=" + serverTimezone;
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(getDbUrl(), user, password);
        } catch (SQLException e) {
            initializeDatabase();
            return DriverManager.getConnection(getDbUrl(), user, password);
        }
    }

    public static synchronized void initializeDatabase() {
        // 1. Create database if it doesn't exist
        try (Connection conn = DriverManager.getConnection(getServerUrl(), user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        } catch (SQLException e) {
            System.err.println("Could not create database " + dbName + ": " + e.getMessage());
        }

        // 2. Create tables and initial data
        try (Connection conn = DriverManager.getConnection(getDbUrl(), user, password);
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
