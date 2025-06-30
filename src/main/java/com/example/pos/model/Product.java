package com.example.pos.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private String id;
    private String name;
    private BigDecimal price;
    private String category;
    private String imagePath; // Path to the image file
    private int quantityInStock;

    public Product(String id, String name, BigDecimal price, String category, String imagePath, int quantityInStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.quantityInStock = quantityInStock;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    @Override
    public String toString() {
        return "Product{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", price=" + price +
               ", category='" + category + '\'' +
               ", imagePath='" + imagePath + '\'' +
               ", quantityInStock=" + quantityInStock +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
