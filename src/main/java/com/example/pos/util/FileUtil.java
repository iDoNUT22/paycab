package com.example.pos.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static final String DATA_DIRECTORY = "data";
    public static final Path PRODUCT_DB_PATH = Paths.get(DATA_DIRECTORY, "ProductDB.txt");
    public static final Path SALES_DB_PATH = Paths.get(DATA_DIRECTORY, "SalesDB.txt");
    public static final Path SALE_ITEMS_DB_PATH = Paths.get(DATA_DIRECTORY, "SaleItemsDB.txt");
    public static final Path USER_DB_PATH = Paths.get(DATA_DIRECTORY, "UserDB.txt");

    public static void ensureDataFilesExist() {
        try {
            Path dataDir = Paths.get(DATA_DIRECTORY);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("Created data directory: " + dataDir.toAbsolutePath());
            }

            if (!Files.exists(PRODUCT_DB_PATH)) {
                Files.createFile(PRODUCT_DB_PATH);
                System.out.println("Created file: " + PRODUCT_DB_PATH.toAbsolutePath());
            }
            if (!Files.exists(SALES_DB_PATH)) {
                Files.createFile(SALES_DB_PATH);
                System.out.println("Created file: " + SALES_DB_PATH.toAbsolutePath());
            }
            if (!Files.exists(SALE_ITEMS_DB_PATH)) {
                Files.createFile(SALE_ITEMS_DB_PATH);
                System.out.println("Created file: " + SALE_ITEMS_DB_PATH.toAbsolutePath());
            }
            if (!Files.exists(USER_DB_PATH)) {
                Files.createFile(USER_DB_PATH);
                System.out.println("Created file: " + USER_DB_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            // In a real app, this should be handled more gracefully, maybe by exiting
            // or informing the user in the UI.
            System.err.println("Error ensuring data files exist: " + e.getMessage());
            // For Swing, you might show a JOptionPane error dialog.
            // JOptionPane.showMessageDialog(null, "Error initializing data files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // For now, we'll print to stderr and continue, which might lead to issues if files can't be created.
        }
    }

    // Helper method to initialize files when MainApp starts
    public static void initialize() {
        ensureDataFilesExist();
    }
}
