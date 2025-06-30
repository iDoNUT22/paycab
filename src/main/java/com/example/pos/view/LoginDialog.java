package com.example.pos.view;

import com.example.pos.model.User;
import com.example.pos.service.UserFileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private UserFileHandler userFileHandler;
    private User authenticatedUser = null;

    public LoginDialog(Frame parent, UserFileHandler userFileHandler) {
        super(parent, "Login", true); // true for modal
        this.userFileHandler = userFileHandler;
        initComponents();
        pack();
        setLocationRelativeTo(parent); // Center relative to parent frame
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Or HIDE_ON_CLOSE
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Status Label
        statusLabel = new JLabel(" ", SwingConstants.CENTER); // Initial empty space
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(statusLabel, gbc);

        // Action listener for login button
        loginButton.addActionListener(this::performLogin);
        // Allow login on Enter key in password field
        passwordField.addActionListener(this::performLogin);
    }

    private void performLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty.");
            return;
        }

        Optional<User> userOpt = userFileHandler.findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // IMPORTANT: Use the same hashing mechanism as when storing the user.
            // UserFileHandler.simpleHash is a placeholder.
            String enteredPasswordHashed = UserFileHandler.simpleHash(password);
            if (user.getHashedPassword().equals(enteredPasswordHashed)) {
                authenticatedUser = user;
                statusLabel.setText("Login successful!");
                statusLabel.setForeground(Color.GREEN);
                // Close the dialog after a short delay or immediately
                // Timer delay = new Timer(1000, ev -> dispose());
                // delay.setRepeats(false);
                // delay.start();
                dispose(); // Close the dialog
            } else {
                statusLabel.setText("Invalid username or password.");
                authenticatedUser = null;
            }
        } else {
            statusLabel.setText("Invalid username or password.");
            authenticatedUser = null;
        }
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
