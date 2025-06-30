package com.example.pos.service;

import com.example.pos.model.Product;
import com.example.pos.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductFileHandler {

    private static final String DELIMITER = "\\|"; // Pipe is a special regex char, needs escaping for split
    private static final String JOIN_DELIMITER = "|";

    // ProductDB.txt expected format: ID|Name|Price|Category|ImagePath|StockQuantity
    // Example: P001|Burger|5.99|Food|images/burger.jpg|50

    // Load all products from ProductDB.txt
    public List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        if (!Files.exists(FileUtil.PRODUCT_DB_PATH)) {
            return products; // Return empty list if file doesn't exist
        }
        try (BufferedReader reader = Files.newBufferedReader(FileUtil.PRODUCT_DB_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(DELIMITER);
                if (parts.length == 6) { // Expect 6 parts now
                    try {
                        String id = parts[0];
                        String name = parts[1];
                        BigDecimal price = new BigDecimal(parts[2]);
                        String category = parts[3];
                        String imagePath = parts[4];
                        int quantityInStock = Integer.parseInt(parts[5]);
                        products.add(new Product(id, name, price, category, imagePath, quantityInStock));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed product line (price or quantity format error): " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed product line (incorrect parts, expected 6): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
            // In a real app, consider throwing a custom exception or handling more gracefully
        }
        return products;
    }

    // Save all products to ProductDB.txt, overwriting the existing file
    public void saveProducts(List<Product> products) {
        try (BufferedWriter writer = Files.newBufferedWriter(FileUtil.PRODUCT_DB_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Product product : products) {
                String line = String.join(JOIN_DELIMITER,
                        product.getId(),
                        product.getName(),
                        product.getPrice().toPlainString(),
                        product.getCategory(),
                        product.getImagePath(),
                        String.valueOf(product.getQuantityInStock()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
        }
    }

    // Add a single product
    public void addProduct(Product product) {
        List<Product> products = loadProducts();
        if (products.stream().anyMatch(p -> p.getId().equals(product.getId()))) {
            System.err.println("Product with ID " + product.getId() + " already exists. Cannot add.");
            // Optionally throw an exception here
            return;
        }
        products.add(product);
        saveProducts(products);
    }

    // Update an existing product
    public boolean updateProduct(Product productToUpdate) {
        List<Product> products = loadProducts();
        Optional<Product> existingProductOpt = products.stream()
                .filter(p -> p.getId().equals(productToUpdate.getId()))
                .findFirst();

        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            existingProduct.setName(productToUpdate.getName());
            existingProduct.setPrice(productToUpdate.getPrice());
            existingProduct.setCategory(productToUpdate.getCategory());
            existingProduct.setImagePath(productToUpdate.getImagePath());
            saveProducts(products);
            return true;
        } else {
            System.err.println("Product with ID " + productToUpdate.getId() + " not found. Cannot update.");
            return false;
        }
    }

    // Delete a product by ID
    public boolean deleteProduct(String productId) {
        List<Product> products = loadProducts();
        boolean removed = products.removeIf(p -> p.getId().equals(productId));
        if (removed) {
            saveProducts(products);
        } else {
            System.err.println("Product with ID " + productId + " not found. Cannot delete.");
        }
        return removed;
    }

    // Get a product by ID
    public Optional<Product> getProductById(String productId) {
        return loadProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst();
    }
}
