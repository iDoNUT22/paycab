package com.example.pos.view;

import com.example.pos.model.Product;
import com.example.pos.model.SaleItem;
import com.example.pos.model.SaleRecord;
import com.example.pos.model.User;
import com.example.pos.service.ProductFileHandler;
import com.example.pos.service.SaleFileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SalesPanel extends JPanel {

    private final ProductFileHandler productFileHandler;
    private final SaleFileHandler saleFileHandler;
    private final User currentUser;

    private JComboBox<ProductWrapper> productComboBox;
    private JTextField retailBarcodeField;
    private JSpinner quantitySpinner;
    private JButton addToCartButton;
    private JPanel restaurantMenuPanel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JTextField discountField;
    private JButton applyDiscountButton;
    private JLabel totalAmountLabel;
    private JLabel finalAmountLabel;
    private JButton processSaleButton;
    private JButton clearCartButton;

    private JPanel productSelectionContainer;
    private final String RETAIL_MODE_PANEL = "RetailMode";
    private final String RESTAURANT_MODE_PANEL = "RestaurantMode";
    private String currentMode = RETAIL_MODE_PANEL;


    private List<SaleItem> currentCart;
    private BigDecimal currentDiscount = BigDecimal.ZERO;
    private BigDecimal currentTotalBeforeDiscount = BigDecimal.ZERO;
    private BigDecimal currentFinalAmount = BigDecimal.ZERO;


    public SalesPanel(ProductFileHandler productFileHandler, SaleFileHandler saleFileHandler, User currentUser) {
        this.productFileHandler = productFileHandler;
        this.saleFileHandler = saleFileHandler;
        this.currentUser = currentUser;
        this.currentCart = new ArrayList<>();

        initComponents();
        updateTotalsDisplay();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel modeSwitcherPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        modeSwitcherPanel.add(new JLabel("Mode:"));
        String[] modes = {RETAIL_MODE_PANEL, RESTAURANT_MODE_PANEL};
        JComboBox<String> modeComboBox = new JComboBox<>(modes);
        modeComboBox.addActionListener(e -> switchMode((String) modeComboBox.getSelectedItem()));
        modeSwitcherPanel.add(modeComboBox);
        topPanel.add(modeSwitcherPanel, BorderLayout.NORTH);

        productSelectionContainer = new JPanel(new CardLayout());
        createRetailProductSelectionPanel();
        createRestaurantProductSelectionPanel();

        topPanel.add(productSelectionContainer, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        switchMode(modes[0]);


        String[] cartColumnNames = {"Product ID", "Name", "Quantity", "Unit Price", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        add(cartScrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        southPanel.add(new JLabel("Discount (% or amount):"), gbc);
        discountField = new JTextField("0", 10);
        gbc.gridx = 1; gbc.gridy = 0;
        southPanel.add(discountField, gbc);
        applyDiscountButton = new JButton("Apply Discount");
        applyDiscountButton.addActionListener(e -> applyDiscount());
        gbc.gridx = 2; gbc.gridy = 0;
        southPanel.add(applyDiscountButton, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0;
        southPanel.add(new JLabel(""));

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0;
        southPanel.add(new JLabel("Total:"), gbc);
        totalAmountLabel = new JLabel("0.00");
        totalAmountLabel.setFont(totalAmountLabel.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 5; gbc.gridy = 0;
        southPanel.add(totalAmountLabel, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        southPanel.add(new JLabel("Final Amount:"), gbc);
        finalAmountLabel = new JLabel("0.00");
        finalAmountLabel.setFont(finalAmountLabel.getFont().deriveFont(Font.BOLD, 16f));
        finalAmountLabel.setForeground(Color.BLUE);
        gbc.gridx = 5; gbc.gridy = 1;
        southPanel.add(finalAmountLabel, gbc);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearCartButton = new JButton("Clear Cart");
        clearCartButton.addActionListener(e -> clearCart());
        actionButtonPanel.add(clearCartButton);

        processSaleButton = new JButton("Process Sale");
        processSaleButton.setFont(processSaleButton.getFont().deriveFont(Font.BOLD));
        processSaleButton.addActionListener(e -> processSale());
        actionButtonPanel.add(processSaleButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6; gbc.fill = GridBagConstraints.HORIZONTAL;
        southPanel.add(actionButtonPanel, gbc);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void createRetailProductSelectionPanel() {
        JPanel retailPanel = new JPanel(new BorderLayout(5,5));

        JPanel inputSelectionPanel = new JPanel(new BorderLayout(5,5)); // Changed to BorderLayout for better structure

        // Panel for ComboBox and its search field
        JPanel comboSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboSearchPanel.add(new JLabel("Search & Select Product:"));
        JTextField productSearchField = new JTextField(15); // New search field for ComboBox
        productSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadProductsIntoComboBox(productSearchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadProductsIntoComboBox(productSearchField.getText()); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadProductsIntoComboBox(productSearchField.getText()); }
        });
        comboSearchPanel.add(productSearchField);

        productComboBox = new JComboBox<>();
        productComboBox.setPreferredSize(new Dimension(250, productComboBox.getPreferredSize().height));
        comboSearchPanel.add(productComboBox);
        inputSelectionPanel.add(comboSearchPanel, BorderLayout.NORTH);

        // Panel for Barcode/ID entry (can be separate or integrated)
        JPanel barcodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barcodePanel.add(new JLabel("OR Enter Barcode/ID:"));
        retailBarcodeField = new JTextField(15); // Increased size slightly
        retailBarcodeField.addActionListener(e -> addItemFromBarcode());
        barcodePanel.add(retailBarcodeField);
        inputSelectionPanel.add(barcodePanel, BorderLayout.CENTER);

        retailPanel.add(inputSelectionPanel, BorderLayout.NORTH); // This was NORTH, inputSelectionPanel is now NORTH of retailPanel

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        actionPanel.add(quantitySpinner);

        addToCartButton = new JButton("Add to Cart (Selected)");
        addToCartButton.addActionListener(e -> addItemToCartFromSelection());
        actionPanel.add(addToCartButton);
        retailPanel.add(actionPanel, BorderLayout.CENTER);

        productSelectionContainer.add(retailPanel, RETAIL_MODE_PANEL);
    }

    private void createRestaurantProductSelectionPanel() {
        restaurantMenuPanel = new JPanel(new BorderLayout());
        restaurantMenuPanel.setBorder(BorderFactory.createTitledBorder("Menu Categories"));

        JLabel placeholderLabel = new JLabel("Restaurant menu will be built here.", SwingConstants.CENTER);
        restaurantMenuPanel.add(placeholderLabel, BorderLayout.CENTER);

        productSelectionContainer.add(restaurantMenuPanel, RESTAURANT_MODE_PANEL);
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        CardLayout cl = (CardLayout) (productSelectionContainer.getLayout());
        if (RETAIL_MODE_PANEL.equals(mode)) {
            cl.show(productSelectionContainer, RETAIL_MODE_PANEL);
            if(productComboBox != null) loadProductsIntoComboBox();
             if(retailBarcodeField != null) retailBarcodeField.requestFocusInWindow();
            System.out.println("Switched to Retail Mode");
        } else if (RESTAURANT_MODE_PANEL.equals(mode)) {
            cl.show(productSelectionContainer, RESTAURANT_MODE_PANEL);
            buildRestaurantMenu();
            System.out.println("Switched to Restaurant Mode");
        }
    }

    private void buildRestaurantMenu() {
        if (restaurantMenuPanel == null) return;

        restaurantMenuPanel.removeAll();
        restaurantMenuPanel.setBorder(BorderFactory.createTitledBorder("Menu Categories"));

        List<Product> allProducts = productFileHandler.loadProducts();
        if (allProducts.isEmpty()) {
            restaurantMenuPanel.add(new JLabel("No products available.", SwingConstants.CENTER));
            restaurantMenuPanel.revalidate();
            restaurantMenuPanel.repaint();
            return;
        }

        Map<String, List<Product>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(p -> {
                    String category = p.getCategory();
                    return (category == null || category.trim().isEmpty()) ? "Uncategorized" : category.trim();
                }));

        if (productsByCategory.isEmpty() && !allProducts.isEmpty()) {
             productsByCategory.put("Uncategorized", allProducts);
        }
        if (productsByCategory.isEmpty()){
             restaurantMenuPanel.add(new JLabel("No categorized products found.", SwingConstants.CENTER));
             restaurantMenuPanel.revalidate();
             restaurantMenuPanel.repaint();
             return;
        }

        JTabbedPane categoryTabs = new JTabbedPane();
        productsByCategory.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String category = entry.getKey();
                List<Product> productsInCategory = entry.getValue();

                JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                JScrollPane categoryScrollPane = new JScrollPane(categoryPanel,
                                                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                categoryScrollPane.getVerticalScrollBar().setUnitIncrement(16);

                for (Product product : productsInCategory) {
                    // Show stock on restaurant buttons as well
                    JButton productButton = new JButton("<html><center>" + product.getName() + "<br>(" + product.getPrice().toPlainString() + ")<br>Stock: " + product.getQuantityInStock() + "</center></html>");
                    productButton.setPreferredSize(new Dimension(130, 80)); // Adjusted size for stock info
                    productButton.setToolTipText(product.getName() + " - " + product.getPrice().toPlainString() + " (Stock: " + product.getQuantityInStock() + ")");
                    productButton.setMargin(new Insets(2,2,2,2));
                    productButton.setEnabled(product.getQuantityInStock() > 0); // Disable if out of stock
                    productButton.addActionListener(e -> {
                        addItemToCartLogic(product, 1);
                    });
                    categoryPanel.add(productButton);
                }
                categoryTabs.addTab(category, categoryScrollPane);
            });

        restaurantMenuPanel.add(categoryTabs, BorderLayout.CENTER);
        restaurantMenuPanel.revalidate();
        restaurantMenuPanel.repaint();
    }

    private void loadProductsIntoComboBox(String filterText) {
        if (productComboBox == null) return;
        productComboBox.removeAllItems();
        List<Product> products = productFileHandler.loadProducts();
        String filterLower = filterText == null ? "" : filterText.toLowerCase().trim();

        products.stream()
                .filter(p -> filterLower.isEmpty() ||
                             p.getName().toLowerCase().contains(filterLower) ||
                             p.getId().toLowerCase().contains(filterLower))
                .forEach(p -> productComboBox.addItem(new ProductWrapper(p)));
    }

    // Overload for initial load or when no filter
    private void loadProductsIntoComboBox() {
        loadProductsIntoComboBox("");
    }

    private void addItemToCartFromSelection() {
        if (productComboBox == null || productComboBox.getSelectedItem() == null) {
             JOptionPane.showMessageDialog(this, "No product selected from dropdown.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ProductWrapper selectedWrapper = (ProductWrapper) productComboBox.getSelectedItem();
        Product selectedProduct = selectedWrapper.getProduct();
        int quantity = (Integer) quantitySpinner.getValue();
        addItemToCartLogic(selectedProduct, quantity);
    }

    private void addItemFromBarcode() {
        if (retailBarcodeField == null) return;
        String productId = retailBarcodeField.getText().trim();
        if (productId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode/ID field is empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Optional<Product> productOpt = productFileHandler.getProductById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int quantity = (Integer) quantitySpinner.getValue();
            addItemToCartLogic(product, quantity);
            retailBarcodeField.setText("");
            retailBarcodeField.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Product with ID/Barcode '" + productId + "' not found.", "Product Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItemToCartLogic(Product product, int quantityToAdd) {
        if (quantityToAdd <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<Product> freshProductOpt = productFileHandler.getProductById(product.getId());
        if (!freshProductOpt.isPresent()) {
            JOptionPane.showMessageDialog(this, "Product " + product.getName() + " not found. Cannot add to cart.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product freshProduct = freshProductOpt.get();
        int currentStock = freshProduct.getQuantityInStock();

        Optional<SaleItem> existingItemOpt = currentCart.stream()
                .filter(item -> item.getProduct().getId().equals(freshProduct.getId()))
                .findFirst();

        int quantityAlreadyInCart = existingItemOpt.map(SaleItem::getQuantity).orElse(0);
        int effectiveStockAvailable = currentStock - quantityAlreadyInCart;

        if (quantityToAdd > effectiveStockAvailable) {
            if (effectiveStockAvailable <= 0) {
                 JOptionPane.showMessageDialog(this, "No more stock available for " + freshProduct.getName() + ". Currently in cart: " + quantityAlreadyInCart + ", Stock: " + currentStock, "Out of Stock", JOptionPane.ERROR_MESSAGE);
                return; // Cannot add any more
            } else {
                JOptionPane.showMessageDialog(this, "Not enough stock for " + freshProduct.getName() + ". Available to add: " + effectiveStockAvailable + ". Adding this amount.", "Stock Limited", JOptionPane.WARNING_MESSAGE);
                quantityToAdd = effectiveStockAvailable; // Adjust to max available to add
            }
        }

        if (quantityToAdd <= 0) return; // If after adjustment, nothing to add

        if (existingItemOpt.isPresent()) {
            SaleItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantityToAdd);
        } else {
            currentCart.add(new SaleItem(freshProduct, quantityToAdd));
        }

        refreshCartTable();
        updateTotalsDisplay();
        if (quantitySpinner != null) quantitySpinner.setValue(1);
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (SaleItem item : currentCart) {
            cartTableModel.addRow(new Object[]{
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPriceAtSale().toPlainString(),
                    item.getSubtotal().toPlainString()
            });
        }
    }

    private void applyDiscount() {
        String discountText = discountField.getText().trim();
        BigDecimal tempDiscountValue = BigDecimal.ZERO;

        try {
            if (discountText.endsWith("%")) {
                BigDecimal percentage = new BigDecimal(discountText.substring(0, discountText.length() - 1));
                if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
                    JOptionPane.showMessageDialog(this, "Discount percentage must be between 0 and 100.", "Invalid Discount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tempDiscountValue = currentTotalBeforeDiscount.multiply(percentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            } else {
                tempDiscountValue = new BigDecimal(discountText);
                if (tempDiscountValue.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Discount amount cannot be negative.", "Invalid Discount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (tempDiscountValue.compareTo(currentTotalBeforeDiscount) > 0 && currentTotalBeforeDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    tempDiscountValue = currentTotalBeforeDiscount;
                     JOptionPane.showMessageDialog(this, "Discount cannot exceed total amount. Adjusted to " + tempDiscountValue.toPlainString(), "Discount Adjusted", JOptionPane.WARNING_MESSAGE);
                     discountField.setText(tempDiscountValue.toPlainString());
                }
            }
            this.currentDiscount = tempDiscountValue.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid discount format. Enter a number or percentage (e.g., 10 or 5%).", "Invalid Discount", JOptionPane.ERROR_MESSAGE);
            this.currentDiscount = BigDecimal.ZERO;
        }
        updateTotalsDisplay();
    }

    private void updateTotalsDisplay() {
        currentTotalBeforeDiscount = currentCart.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        totalAmountLabel.setText(currentTotalBeforeDiscount.toPlainString());

        if (this.currentDiscount.compareTo(currentTotalBeforeDiscount) > 0 && currentTotalBeforeDiscount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentDiscount = currentTotalBeforeDiscount;
            discountField.setText(this.currentDiscount.toPlainString());
        } else if (currentTotalBeforeDiscount.compareTo(BigDecimal.ZERO) == 0) {
             this.currentDiscount = BigDecimal.ZERO;
             discountField.setText("0");
        }

        currentFinalAmount = currentTotalBeforeDiscount.subtract(this.currentDiscount).setScale(2, RoundingMode.HALF_UP);
        if (currentFinalAmount.compareTo(BigDecimal.ZERO) < 0) {
            currentFinalAmount = BigDecimal.ZERO;
        }

        finalAmountLabel.setText(currentFinalAmount.toPlainString());
    }

    private void clearCart() {
        currentCart.clear();
        this.currentDiscount = BigDecimal.ZERO;
        discountField.setText("0");
        refreshCartTable();
        updateTotalsDisplay();
        JOptionPane.showMessageDialog(this, "Cart cleared.", "Cart Cleared", JOptionPane.INFORMATION_MESSAGE);
    }

    private void processSale() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items to proceed.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        applyDiscount();

        // --- BEGIN INVENTORY CHECK AND UPDATE ---
        for (SaleItem item : currentCart) {
            Optional<Product> productOpt = productFileHandler.getProductById(item.getProduct().getId());
            if (!productOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Product " + item.getProduct().getName() + " not found. Sale cancelled.", "Critical Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Product product = productOpt.get();
            if (product.getQuantityInStock() < item.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Stock for " + product.getName() + " is insufficient (Available: " + product.getQuantityInStock() + ", In Cart: " + item.getQuantity() + "). Please adjust cart. Sale cancelled.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        List<Product> productsToUpdate = new ArrayList<>();
        for (SaleItem item : currentCart) {
            Product product = productFileHandler.getProductById(item.getProduct().getId()).get(); // Already checked existence
            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
            productsToUpdate.add(product);
        }

        boolean allStockUpdated = true;
        for (Product p : productsToUpdate) {
            if (!productFileHandler.updateProduct(p)) {
                allStockUpdated = false;
                System.err.println("Critical: Failed to update stock for product ID: " + p.getId() + " after sale commitment. Data inconsistency possible.");
            }
        }
        if (!allStockUpdated) {
            JOptionPane.showMessageDialog(this, "CRITICAL WARNING: Some product stock levels FAILED to update after sale. Manual correction needed!", "Stock Update Failure", JOptionPane.ERROR_MESSAGE);
        }
        // --- END INVENTORY CHECK AND UPDATE ---

        SaleRecord saleRecord = new SaleRecord(new ArrayList<>(currentCart), this.currentDiscount);
        saleFileHandler.saveSale(saleRecord);

        showReceipt(saleRecord);
        clearCart();

        if (RETAIL_MODE_PANEL.equals(currentMode)) {
             if (productComboBox != null) loadProductsIntoComboBox();
        } else if (RESTAURANT_MODE_PANEL.equals(currentMode)) {
            buildRestaurantMenu();
        }

        JOptionPane.showMessageDialog(this, "Sale processed successfully! Sale ID: " + saleRecord.getSaleId(), "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showReceipt(SaleRecord saleRecord) {
        JDialog receiptDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sale Receipt", true);
        receiptDialog.setSize(400, 500);
        receiptDialog.setLayout(new BorderLayout());

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder receiptText = new StringBuilder();
        receiptText.append("----------- SALE RECEIPT -----------\n");
        receiptText.append("Sale ID: ").append(saleRecord.getSaleId()).append("\n");
        receiptText.append("Date: ").append(saleRecord.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receiptText.append("Cashier: ").append(currentUser.getUsername()).append("\n");
        receiptText.append("------------------------------------\n");
        receiptText.append(String.format("%-20s %5s %8s %10s\n", "Item", "Qty", "Price", "Subtotal"));
        receiptText.append("------------------------------------\n");

        for (SaleItem item : saleRecord.getItems()) {
            receiptText.append(String.format("%-20.20s %5d %8.2f %10.2f\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPriceAtSale(),
                    item.getSubtotal()));
        }
        receiptText.append("------------------------------------\n");
        receiptText.append(String.format("%34s %10.2f\n", "Total:", saleRecord.getTotalAmount()));
        if (saleRecord.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            receiptText.append(String.format("%34s %10.2f\n", "Discount:", saleRecord.getDiscountAmount().negate()));
        }
        receiptText.append(String.format("%34s %10.2f\n", "FINAL AMOUNT:", saleRecord.getFinalAmount()));
        receiptText.append("------------------------------------\n");
        receiptText.append("Thank you for your purchase!\n");
        receiptText.append("------------------------------------\n");

        receiptArea.setText(receiptText.toString());
        receiptDialog.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> receiptDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        receiptDialog.add(buttonPanel, BorderLayout.SOUTH);

        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setVisible(true);
    }

    private static class ProductWrapper {
        private final Product product;
        public ProductWrapper(Product product) { this.product = product; }
        public Product getProduct() { return product; }
        @Override public String toString() {
            return product.getName() + " (" + product.getPrice().toPlainString() + ") - Stock: " + product.getQuantityInStock();
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
            ProductWrapper that = (ProductWrapper) o;
            return product.getId().equals(that.product.getId());
        }
        @Override public int hashCode() { return product.getId().hashCode(); }
    }
}
