package com.example.pos;

import com.example.pos.model.User;
import com.example.pos.service.ProductFileHandler;
import com.example.pos.service.UserFileHandler;
import com.example.pos.util.FileUtil;
import com.example.pos.view.LoginDialog;
import com.example.pos.view.ProductManagementPanel;

import javax.swing.*;
import java.awt.*;

public class MainApp {

    private static User currentLoggedInUser;
    private static JFrame mainFrame;

    public static void main(String[] args) {
        // Initialize data files and directories
        FileUtil.initialize();

        // Prepare UserFileHandler
        UserFileHandler userFileHandler = new UserFileHandler();
        userFileHandler.initializeDefaultAdminUser(); // Ensure default admin exists

        // Show login dialog on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null, userFileHandler); // null parent initially
            loginDialog.setVisible(true);

            currentLoggedInUser = loginDialog.getAuthenticatedUser();

            currentLoggedInUser = loginDialog.getAuthenticatedUser();

            currentLoggedInUser = loginDialog.getAuthenticatedUser();

            if (currentLoggedInUser != null) {
                // Create file handlers (or services) that will be needed by the main app window
                ProductFileHandler productFileHandler = new ProductFileHandler();
                SaleFileHandler saleFileHandler = new SaleFileHandler(productFileHandler);

                // Proceed to show main application window
                showMainApplicationWindow(productFileHandler, userFileHandler, saleFileHandler /*, any other services */);
            } else {
                // User closed dialog or failed login, exit application
                JOptionPane.showMessageDialog(null, "Login failed or cancelled. Exiting.", "Login Required", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }

    private static void showMainApplicationWindow(ProductFileHandler productFileHandler, UserFileHandler userFileHandler, SaleFileHandler saleFileHandler) {
        mainFrame = new JFrame("Point of Sale System - Logged in as: " + currentLoggedInUser.getUsername() + " (" + currentLoggedInUser.getRole() + ")");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1024, 768);

        // Pass currentLoggedInUser to panels that need it
        ProductManagementPanel productPanel = new ProductManagementPanel(productFileHandler /*, currentLoggedInUser */);
        // When ProductManagementPanel is updated to use User, uncomment the above and modify constructor

        SalesPanel salesPanel = new SalesPanel(productFileHandler, saleFileHandler, currentLoggedInUser);

        SalesReporter salesReporter = new SalesReporter(saleFileHandler);
        SalesReportsPanel reportsPanel = new SalesReportsPanel(salesReporter);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Sales", salesPanel); // Sales typically first tab for cashiers
        tabbedPane.addTab("Product Management", productPanel);
        tabbedPane.addTab("Sales Reports", reportsPanel);


        // Only show admin-specific tabs if user is ADMIN
        if (currentLoggedInUser.getRole() == User.UserRole.ADMIN) {
            // Example: Add a User Management Tab only for Admins
            // JPanel userManagementPanel = new UserManagementPanel(userFileHandler);
            // tabbedPane.addTab("User Management", userManagementPanel);
        }


        mainFrame.add(tabbedPane, BorderLayout.CENTER);

        // Theme Toggle Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Dark/Light Theme");
        toggleThemeItem.addActionListener(e -> toggleTheme());
        viewMenu.add(toggleThemeItem);
        menuBar.add(viewMenu);
        mainFrame.setJMenuBar(menuBar);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static boolean isDarkTheme = false; // Initial theme state

    private static void toggleTheme() {
        try {
            if (isDarkTheme) {
                // Switch to Metal L&F (default light)
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                isDarkTheme = false;
            } else {
                // Attempt to switch to Nimbus L&F, then try to make it dark
                // This is a basic approach; Nimbus dark theme setup can be more involved
                // or require specific properties. A dedicated dark L&F library is better for production.
                boolean nimbusFound = false;
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        // For a truly dark Nimbus, more properties might need to be set, e.g.:
                        // UIManager.put("control", new Color(128, 128, 128));
                        // UIManager.put("info", new Color(128, 128, 128));
                        // UIManager.put("nimbusBase", new Color(18, 30, 49));
                        // UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                        // UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                        // UIManager.put("nimbusFocus", new Color(115, 164, 209));
                        // UIManager.put("nimbusGreen", new Color(176, 179, 50));
                        // UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                        // UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
                        // UIManager.put("nimbusOrange", new Color(191, 98, 4));
                        // UIManager.put("nimbusRed", new Color(169, 46, 34));
                        // UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                        // UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                        // UIManager.put("text", new Color(230, 230, 230));
                        // A simple dark theme often involves setting background/foreground for key components
                        // For this example, just setting Nimbus might make it somewhat darker than Metal.
                        // A true dark theme usually requires a dedicated Look and Feel library (e.g., FlatLaf, Darcula).
                        nimbusFound = true;
                        break;
                    }
                }
                if (!nimbusFound) {
                    System.err.println("Nimbus L&F not found. Sticking to default.");
                    // Fallback or do nothing if Nimbus isn't available
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    isDarkTheme = false; // Stay in light mode
                    return;
                }
                isDarkTheme = true;
            }
            // Update UI for all components
            if (mainFrame != null) {
                SwingUtilities.updateComponentTreeUI(mainFrame);
            }
            // If login dialog or other dialogs are open, they might need individual updates if shown after L&F change.
        } catch (Exception ex) {
            System.err.println("Failed to set Look and Feel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static User getCurrentLoggedInUser() {
        return currentLoggedInUser;
    }
}
