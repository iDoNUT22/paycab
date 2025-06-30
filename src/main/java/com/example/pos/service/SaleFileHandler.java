package com.example.pos.service;

import com.example.pos.model.Product;
import com.example.pos.model.SaleItem;
import com.example.pos.model.SaleRecord;
import com.example.pos.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaleFileHandler {

    private static final String DELIMITER = "\\|";
    private static final String JOIN_DELIMITER = "|";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // SalesDB.txt format: SALE_ID|TIMESTAMP|ITEM_COUNT|TOTAL_AMOUNT|DISCOUNT_AMOUNT|FINAL_AMOUNT
    // Example: sale123|2023-01-01T10:15:30|2|25.98|0.00|25.98
    //
    // SaleItemsDB.txt format: SALE_ID|PRODUCT_ID|QUANTITY|PRICE_AT_SALE|SUBTOTAL
    // Example: sale123|P001|1|12.99|12.99

    private final ProductFileHandler productFileHandler; // To fetch product details for SaleItem

    public SaleFileHandler(ProductFileHandler productFileHandler) {
        this.productFileHandler = productFileHandler;
    }

    public List<SaleRecord> loadSales() {
        List<SaleRecord> sales = new ArrayList<>();
        if (!Files.exists(FileUtil.SALES_DB_PATH) || !Files.exists(FileUtil.SALE_ITEMS_DB_PATH)) {
            return sales;
        }

        // 1. Load all sale items and group them by saleId
        Map<String, List<SaleItem>> saleItemsMap = loadSaleItemsGroupedBySaleId();

        // 2. Load sale headers and construct SaleRecord objects
        try (BufferedReader reader = Files.newBufferedReader(FileUtil.SALES_DB_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length == 6) { // SALE_ID|TIMESTAMP|ITEM_COUNT|TOTAL_AMOUNT|DISCOUNT_AMOUNT|FINAL_AMOUNT
                    try {
                        String saleId = parts[0];
                        LocalDateTime timestamp = LocalDateTime.parse(parts[1], TIMESTAMP_FORMATTER);
                        // int itemCount = Integer.parseInt(parts[2]); // Not strictly needed if we use the items list
                        BigDecimal totalAmount = new BigDecimal(parts[3]);
                        BigDecimal discountAmount = new BigDecimal(parts[4]);
                        BigDecimal finalAmount = new BigDecimal(parts[5]);

                        List<SaleItem> itemsForThisSale = saleItemsMap.getOrDefault(saleId, new ArrayList<>());
                        // The constructor recalculates amounts, so we pass the loaded items
                        // and use the specific constructor that takes all fields.
                        sales.add(new SaleRecord(saleId, timestamp, itemsForThisSale, totalAmount, discountAmount, finalAmount));

                    } catch (DateTimeParseException | NumberFormatException e) {
                        System.err.println("Skipping malformed sale record line: " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed sale record line (incorrect parts): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading sales records: " + e.getMessage());
        }
        return sales;
    }

    private Map<String, List<SaleItem>> loadSaleItemsGroupedBySaleId() {
        List<SaleItemRecord> itemRecords = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileUtil.SALE_ITEMS_DB_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                // SALE_ID|PRODUCT_ID|QUANTITY|PRICE_AT_SALE|SUBTOTAL
                if (parts.length == 5) {
                    try {
                        itemRecords.add(new SaleItemRecord(parts[0], parts[1], Integer.parseInt(parts[2]), new BigDecimal(parts[3]), new BigDecimal(parts[4])));
                    } catch (NumberFormatException e) {
                         System.err.println("Skipping malformed sale item line (number format): " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed sale item line (incorrect parts): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading sale items: " + e.getMessage());
        }

        // Convert SaleItemRecord to SaleItem by fetching Product details
        // This is a bit inefficient (N+1 if many products) but necessary with current structure.
        // A cache in ProductFileHandler or passing a list of all products could optimize.
        return itemRecords.stream()
            .map(rec -> {
                Optional<Product> productOpt = productFileHandler.getProductById(rec.productId);
                if (productOpt.isPresent()) {
                    // Use the constructor that accepts historical price
                    SaleItem saleItem = new SaleItem(productOpt.get(), rec.quantity, rec.priceAtSale);
                    // Validate if the loaded subtotal matches calculation (optional)
                    if (saleItem.getSubtotal().compareTo(rec.subtotal) != 0) {
                        System.err.println("Warning: Subtotal mismatch for loaded sale item. SaleID: " + rec.saleId +
                                           ", ProductID: " + rec.productId + ". Expected: " + rec.subtotal +
                                           ", Calculated: " + saleItem.getSubtotal());
                        // Decide on handling: trust file subtotal, or recalculate.
                        // For now, the SaleItem calculates its own subtotal based on qty and priceAtSale.
                    }
                    return new AbstractMapEntry<>(rec.saleId, saleItem);
                } else {
                    System.err.println("Product with ID " + rec.productId + " not found for sale item. Skipping.");
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }


    public void saveSale(SaleRecord saleRecord) {
        // 1. Save the main sale record to SalesDB.txt (append)
        String saleRecordLine = String.join(JOIN_DELIMITER,
                saleRecord.getSaleId(),
                saleRecord.getTimestamp().format(TIMESTAMP_FORMATTER),
                String.valueOf(saleRecord.getItems().size()),
                saleRecord.getTotalAmount().toPlainString(),
                saleRecord.getDiscountAmount().toPlainString(),
                saleRecord.getFinalAmount().toPlainString()
        );

        try (BufferedWriter writer = Files.newBufferedWriter(FileUtil.SALES_DB_PATH, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(saleRecordLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving sale record: " + e.getMessage());
            // Consider how to handle partial saves (e.g., if sale items fail next)
            return; // Exit if we can't save the main record
        }

        // 2. Save each sale item to SaleItemsDB.txt (append)
        try (BufferedWriter writer = Files.newBufferedWriter(FileUtil.SALE_ITEMS_DB_PATH, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (SaleItem item : saleRecord.getItems()) {
                String itemLine = String.join(JOIN_DELIMITER,
                        saleRecord.getSaleId(),
                        item.getProduct().getId(),
                        String.valueOf(item.getQuantity()),
                        item.getPriceAtSale().toPlainString(), // This should be the price at the time of sale
                        item.getSubtotal().toPlainString()
                );
                writer.write(itemLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving sale items: " + e.getMessage());
            // If items fail to save, the main sale record is already saved. This is a consistency issue.
            // Transactional behavior is hard with flat files. One strategy could be to write to temp files
            // and then rename, or to have a recovery mechanism. For this scope, we acknowledge the risk.
        }
    }

    // Helper inner class to temporarily hold data read from SaleItemsDB.txt
    // before Product object is fetched.
    private static class SaleItemRecord {
        String saleId;
        String productId;
        int quantity;
        BigDecimal priceAtSale;
        BigDecimal subtotal;

        SaleItemRecord(String saleId, String productId, int quantity, BigDecimal priceAtSale, BigDecimal subtotal) {
            this.saleId = saleId;
            this.productId = productId;
            this.quantity = quantity;
            this.priceAtSale = priceAtSale;
            this.subtotal = subtotal;
        }
    }

    // Helper for mapping in stream
    private static class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public AbstractMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
