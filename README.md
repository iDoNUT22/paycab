# Java Swing Point of Sale (POS) System

A simple Point of Sale system built with Java Swing for managing products, sales, and reports. It's designed for both retail and restaurant use cases (with simplified restaurant mode).

## Features

-   **Product Management**: Add, update, delete products (ID, name, price, category, image path, stock).
-   **Sales Functionality**:
    -   Retail mode: Select products via dropdown/barcode, add to cart.
    -   Restaurant mode: Select products from a categorized menu (buttons).
    -   Apply discounts (percentage or fixed amount).
    -   Process sales and generate text-based receipts.
-   **Sales Reporting**: View daily, weekly, monthly, or all-time sales summaries and detailed transaction lists. Export reports to CSV.
-   **User Authentication**: Basic login for Admin/Cashier roles (default admin: `admin`/`admin123`).
-   **Inventory Tracking**: Product stock is tracked and updated with each sale.
-   **Data Persistence**: Uses plain text files (`ProductDB.txt`, `SalesDB.txt`, `SaleItemsDB.txt`, `UserDB.txt`) stored in a `data` subdirectory.
-   **UI**:
    -   Tabbed interface for different modules.
    -   Product image preview in Product Management.
    -   Basic light/dark theme toggle (Metal/Nimbus).

## Build Instructions

This project uses Apache Maven. To build the project and create a runnable JAR:

1.  Ensure you have Java (JDK 8 or later) and Maven installed.
2.  Navigate to the project's root directory in your terminal.
3.  Run the command:
    ```bash
    mvn clean package
    ```
4.  This will compile the code, run tests (if any), and create a JAR file in the `target/` directory (e.g., `swing-pos-system-1.0-SNAPSHOT.jar`).

## How to Run

1.  After building, navigate to the `target/` directory.
2.  Run the application using the command:
    ```bash
    java -jar swing-pos-system-1.0-SNAPSHOT.jar
    ```
    (Replace `swing-pos-system-1.0-SNAPSHOT.jar` with the actual JAR file name if different).

3.  The application will start, and you will be prompted with a login dialog.
    -   **Default Admin Credentials**:
        -   Username: `admin`
        -   Password: `admin123`

## Data Files

The application stores its data in plain text files within a `data` subdirectory created in the same location where the JAR is run.

-   **`ProductDB.txt`**: `ID|Name|Price|Category|ImagePath|StockQuantity`
    -   Example: `P001|Burger|5.99|Food|images/burger.jpg|50`
-   **`UserDB.txt`**: `username|hashedPassword|ROLE`
    -   Example: `admin|hashed_admin123|ADMIN` (Password hashing is a placeholder `hashed_` prefix).
-   **`SalesDB.txt`**: `SALE_ID|TIMESTAMP|ITEM_COUNT|TOTAL_AMOUNT|DISCOUNT_AMOUNT|FINAL_AMOUNT`
    -   Example: `sale123|2023-01-01T10:15:30|2|25.98|0.00|25.98`
-   **`SaleItemsDB.txt`**: `SALE_ID|PRODUCT_ID|QUANTITY|PRICE_AT_SALE|SUBTOTAL`
    -   Example: `sale123|P001|1|12.99|12.99`

## Notes

-   The "Restaurant Mode" in sales provides a menu-button layout but does not include advanced features like table management or order modifiers.
-   The theme toggle provides a basic switch between Swing's Metal and Nimbus Look and Feels. A more polished dark theme would require a dedicated library or more extensive UIManager property settings.
-   Ensure the `images` directory (if used for product images) is structured as expected relative to the application's runtime location if you use local file paths for images. For example, if `ImagePath` is `images/burger.jpg`, the JAR should be run from a directory that contains an `images` subdirectory with `burger.jpg` inside it. If using absolute paths for images, ensure they are correct for the environment where the JAR is run.
```
