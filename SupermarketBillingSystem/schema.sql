-- Supermarket Billing System Database Schema
CREATE DATABASE IF NOT EXISTS supermarket_db;
USE supermarket_db;

CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    extra_attribute VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1001;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_member BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) AUTO_INCREMENT = 10001;

CREATE TABLE IF NOT EXISTS invoices (
    invoice_number INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) AUTO_INCREMENT = 5001;

CREATE TABLE IF NOT EXISTS invoice_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_number INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (invoice_number) REFERENCES invoices(invoice_number) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Seed initial products if products table is empty
INSERT INTO products (name, category, price, quantity, extra_attribute)
SELECT * FROM (
    SELECT 'Rice 5kg' AS name, 'Grocery' AS category, 350.00 AS price, 50 AS quantity, '30-12-2026' AS extra_attribute UNION ALL
    SELECT 'Milk 1L', 'Grocery', 60.00, 100, '05-11-2026' UNION ALL
    SELECT 'Bread', 'Grocery', 45.00, 80, '03-11-2026' UNION ALL
    SELECT 'Headphones', 'Electronic', 1500.00, 25, '12' UNION ALL
    SELECT 'Keyboard', 'Electronic', 900.00, 30, '24' UNION ALL
    SELECT 'Cotton Shirt', 'Clothing', 799.00, 40, 'L' UNION ALL
    SELECT 'Blue Jeans', 'Clothing', 1299.00, 35, '32'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM products);

