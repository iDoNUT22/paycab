package com.example.pos.model;

import java.math.BigDecimal;

public class SaleItem {
    private Product product; // Reference to the Product object
    private int quantity;
    private BigDecimal priceAtSale; // Price of the product unit at the time of sale
    private BigDecimal subtotal; // quantity * priceAtSale

    /**
     * Constructor for creating a new sale item (e.g., adding to cart).
     * Price at sale is taken from the current product price.
     */
    public SaleItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtSale = product.getPrice(); // Capture current price from product
        this.subtotal = this.priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Constructor for loading a sale item from storage, using a historical price.
     */
    public SaleItem(Product product, int quantity, BigDecimal priceAtSale) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale; // Use provided historical price
        this.subtotal = this.priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }


    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Recalculate subtotal if quantity changes
        if (this.priceAtSale != null) {
            this.subtotal = this.priceAtSale.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return "SaleItem{" +
               "productId=" + (product != null ? product.getId() : "null") +
               ", quantity=" + quantity +
               ", priceAtSale=" + priceAtSale +
               ", subtotal=" + subtotal +
               '}';
    }
}
