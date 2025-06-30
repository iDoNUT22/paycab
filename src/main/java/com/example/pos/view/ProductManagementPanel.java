package com.example.pos.view;

import com.example.pos.model.Product;
import com.example.pos.service.ProductFileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductManagementPanel extends JPanel {

    private final ProductFileHandler productFileHandler;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField categoryField;
    private JTextField imagePathField;
    private JTextField stockField; // For quantityInStock

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    private JLabel imagePreviewLabel;
    private JTextField searchField; // For filtering the product table
    private TableRowSorter<DefaultTableModel> sorter;


    public ProductManagementPanel(ProductFileHandler productFileHandler) {
        this.productFileHandler = productFileHandler;
        initComponents();
        loadProductsToTable();
        clearFormFields(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Search Panel (Top North)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Product (ID or Name):"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"ID", "Name", "Price", "Category", "Image Path", "Stock"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter); // Apply the sorter

        JScrollPane scrollPane = new JScrollPane(productTable);
        // The table is now part of a central panel that might include other things
        // For now, let's assume scrollPane is added to BorderLayout.CENTER directly or via an intermediate panel
        add(scrollPane, BorderLayout.CENTER);


        // Form for product details
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        idField = new JTextField(20);
        idField.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(idField, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(nameField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        priceField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(priceField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        categoryField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(categoryField, gbc);

        // Image Path
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Image Path:"), gbc);
        imagePathField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(imagePathField, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Stock:"), gbc);
        stockField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(stockField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add New");
        updateButton = new JButton("Update Selected");
        deleteButton = new JButton("Delete Selected");
        clearButton = new JButton("Clear Form");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Image Preview Panel (to the right of the table/form or integrated)
        // For simplicity, let's add it to the right of the main form and table area
        // This means changing the main layout of ProductManagementPanel from BorderLayout
        // to something that can accommodate table+form on left/center, and image on right.
        // Or, add image preview within the southPanel, next to the form.

        // Let's try adding it to the right of the form in the southPanel.
        // This requires formPanel to not take full width of southPanel.center.
        // A more common layout: Table (CENTER), Form+ImagePreview (SOUTH in a new JPanel with BorderLayout: Form WEST, Image EAST)

        JPanel detailsAndImagePanel = new JPanel(new BorderLayout(10,0));
        detailsAndImagePanel.add(formPanel, BorderLayout.CENTER);

        imagePreviewLabel = new JLabel("No Image", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(200, 200)); // Example size
        imagePreviewLabel.setBorder(BorderFactory.createTitledBorder("Image Preview"));
        JScrollPane imageScrollPane = new JScrollPane(imagePreviewLabel); // In case image is larger
        imageScrollPane.setPreferredSize(new Dimension(220, 220));
        detailsAndImagePanel.add(imageScrollPane, BorderLayout.EAST);

        southPanel.remove(formPanel); // Remove formPanel added directly
        southPanel.add(detailsAndImagePanel, BorderLayout.CENTER); // Add combined panel

        add(southPanel, BorderLayout.SOUTH);


        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
                displaySelectedProductImage(); // New method call
            }
        });

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearFormFields(true));
    }

    private void filterTable() {
        String searchText = searchField.getText().trim();
        if (searchText.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // Filter by ID (column 0) or Name (column 1), case insensitive
            //RowFilter<DefaultTableModel, Object> idFilter = RowFilter.regexFilter("(?i)" + searchText, 0);
            //RowFilter<DefaultTableModel, Object> nameFilter = RowFilter.regexFilter("(?i)" + searchText, 1);
            //sorter.setRowFilter(RowFilter.orFilter(List.of(idFilter, nameFilter)));
             sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0, 1));
        }
    }

    private void loadProductsToTable() {
        tableModel.setRowCount(0);
        List<Product> products = productFileHandler.loadProducts();
        for (Product product : products) {
            tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getPrice().toPlainString(),
                    product.getCategory(),
                    product.getImagePath(),
                    product.getQuantityInStock() // Display stock
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            priceField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            categoryField.setText(tableModel.getValueAt(selectedRow, 3).toString());
            imagePathField.setText(tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "");
            stockField.setText(tableModel.getValueAt(selectedRow, 5).toString());
            idField.setEditable(false);
        }
    }

    private void displaySelectedProductImage() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
            return;
        }

        String imagePathStr = (String) tableModel.getValueAt(selectedRow, 4);
        if (imagePathStr != null && !imagePathStr.trim().isEmpty()) {
            File imageFile = new File(imagePathStr.trim());
            if (imageFile.exists() && imageFile.isFile()) {
                try {
                    ImageIcon imageIcon = new ImageIcon(imagePathStr.trim());
                    // Scale image to fit the label while maintaining aspect ratio
                    Image image = imageIcon.getImage();
                    int previewWidth = imagePreviewLabel.getWidth() > 0 ? imagePreviewLabel.getWidth() -10 : 180; // Approx size, subtract padding
                    int previewHeight = imagePreviewLabel.getHeight() > 0 ? imagePreviewLabel.getHeight() -30 : 180; // Approx size, subtract border/title

                    if (previewWidth <=0) previewWidth = 180; // Default if label not yet sized
                    if (previewHeight <=0) previewHeight = 180;

                    Image scaledImage = image.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                    imagePreviewLabel.setIcon(new ImageIcon(scaledImage));
                    imagePreviewLabel.setText(null); // Remove "No Image" text
                } catch (Exception ex) {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Error loading");
                    System.err.println("Error loading image: " + imagePathStr + " - " + ex.getMessage());
                }
            } else {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("Image not found");
            }
        } else {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image Path");
        }
    }


    private void clearFormFields(boolean prepareForNew) {
        if (prepareForNew) {
            idField.setText("P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            idField.setEditable(false);
        } else {
            idField.setText("");
        }
        nameField.setText("");
        priceField.setText("");
        categoryField.setText("");
        imagePathField.setText("");
        stockField.setText("0");
        productTable.clearSelection();
        if (imagePreviewLabel != null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
        }
    }

    private void addProduct() {
        String id = idField.getText().trim();
        if (id.isEmpty() || !id.startsWith("P")) {
            id = "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            idField.setText(id);
        }

        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String category = categoryField.getText().trim();
        String imagePath = imagePathField.getText().trim();
        String stockStr = stockField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Price, Category, and Stock are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock format. Must be an integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (productFileHandler.getProductById(id).isPresent()) {
             JOptionPane.showMessageDialog(this, "Product ID already exists. Please clear form for a new ID.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        Product newProduct = new Product(id, name, price, category, imagePath, stock);
        productFileHandler.addProduct(newProduct);
        loadProductsToTable();
        clearFormFields(true);
        JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String category = categoryField.getText().trim();
        String imagePath = imagePathField.getText().trim();
        String stockStr = stockField.getText().trim();


        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Price, Category, and Stock are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(priceStr);
             if (price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock format. Must be an integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product updatedProduct = new Product(id, name, price, category, imagePath, stock);
        if (productFileHandler.updateProduct(updatedProduct)) {
            loadProductsToTable();
            clearFormFields(true);
            JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update product. Ensure ID is correct.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idToDelete = tableModel.getValueAt(selectedRow, 0).toString();
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete product ID: " + idToDelete + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            if (productFileHandler.deleteProduct(idToDelete)) {
                loadProductsToTable();
                clearFormFields(true);
                JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
