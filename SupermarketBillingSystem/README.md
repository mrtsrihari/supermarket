# Smart Supermarket Billing System (MySQL Connected)

## Requirements
- JDK 8 or later (tested on JDK 21)
- MySQL Server 8.0 or later (tested on MySQL 9.7)
- MySQL Connector/J (included in `lib/mysql-connector-j-9.7.0.jar`)
- Eclipse, IntelliJ IDEA, VS Code, or Terminal

## Database Configuration
The application connects to MySQL using the following default configuration:
- **Host**: `localhost:3306`
- **Database**: `supermarket_db`
- **User**: `root`
- **Password**: `Hari123@`

*(Optional)* You can override credentials using environment variables:
```bash
set DB_USER=root
set DB_PASSWORD=your_password
```

The database and tables are **automatically created and seeded** on the first run, or you can run `schema.sql` manually.

### Database Tables
- `products`: Product catalog with category, price, quantity, and category-specific attributes (expiry date, warranty, size).
- `customers`: Customer details including membership status.
- `invoices`: Sales transactions with subtotal, discount, GST tax, and total.
- `invoice_items`: Line items for each invoice linking products, quantities, and prices.

## Running the Application

### Option 1: Quick Run (Windows)
Double-click `run.bat` or run in terminal:
```cmd
.\run.bat
```

### Option 2: Terminal / PowerShell
From the project root:
```bash
javac -cp "lib/*" -d out src/supermarket/model/*.java src/supermarket/exception/*.java src/supermarket/db/*.java src/supermarket/dao/*.java src/supermarket/service/*.java src/supermarket/app/*.java
java -cp "out;lib/*" supermarket.app.SupermarketBillingSystem
```

### Option 3: Eclipse / IDE Setup
1. Open Eclipse and choose **File > Import > General > Existing Projects into Workspace**.
2. Select the `SupermarketBillingSystem` directory.
3. The project includes preconfigured `.project` and `.classpath` referencing `lib/mysql-connector-j-9.7.0.jar`.
4. Right-click `supermarket.app.SupermarketBillingSystem` -> **Run As > Java Application**.

## Modules
- `supermarket.db`: `DBConnection` manager with automatic schema creation and health checks.
- `supermarket.dao`: Data Access Objects (`ProductDAO`, `CustomerDAO`, `InvoiceDAO`) handling transactional MySQL queries.
- `supermarket.model`: Object-oriented product hierarchy (`GroceryProduct`, `ElectronicProduct`, `ClothingProduct`), `Customer`, `Cart`, `Invoice`.
- `supermarket.service`: `Supermarket` business logic orchestrating inventory, pricing, taxes, and billing with MySQL.
- `supermarket.exception`: Custom exceptions such as `InvalidCustomerException`.
- `supermarket.app`: Console UI and interactive menus.
