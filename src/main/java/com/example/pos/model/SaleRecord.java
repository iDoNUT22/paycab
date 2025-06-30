package com.example.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SaleRecord {
    private String saleId;
    private LocalDateTime timestamp;
    private List<SaleItem> items;
    private BigDecimal totalAmount; // Sum of all SaleItem subtotals
    private BigDecimal discountAmount; // Amount discounted
    private BigDecimal finalAmount; // totalAmount - discountAmount

    public SaleRecord(List<SaleItem> items, BigDecimal discountAmount) {
        this.saleId = UUID.randomUUID().toString(); // Generate a unique ID for the sale
        this.timestamp = LocalDateTime.now();
        this.items = items;
        this.totalAmount = items.stream()
                                .map(SaleItem::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
    }

    // Constructor for loading from file
    public SaleRecord(String saleId, LocalDateTime timestamp, List<SaleItem> items, BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal finalAmount) {
        this.saleId = saleId;
        this.timestamp = timestamp;
        this.items = items;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
    }


    public String getSaleId() {
        return saleId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    @Override
    public String toString() {
        return "SaleRecord{" +
               "saleId='" + saleId + '\'' +
               ", timestamp=" + timestamp +
               ", items=" + items +
               ", totalAmount=" + totalAmount +
               ", discountAmount=" + discountAmount +
               ", finalAmount=" + finalAmount +
               '}';
    }
}
