package com.example.pos.service;

import com.example.pos.model.User;
import com.example.pos.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserFileHandler {

    private static final String DELIMITER = "\\|";
    private static final String JOIN_DELIMITER = "|";

    // UserDB.txt expected format: username|hashedPassword|ROLE
    // Example: admin|hashed_admin123|ADMIN

    // Basic password hashing (replace with a strong hashing library in a real app)
    // For this example, let's use a placeholder.
    // In a real application, use something like bcrypt or Argon2.
    public static String simpleHash(String password) {
        // THIS IS NOT SECURE. FOR DEMONSTRATION PURPOSES ONLY.
        return "hashed_" + password;
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(FileUtil.USER_DB_PATH)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(FileUtil.USER_DB_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length == 3) {
                    try {
                        String username = parts[0];
                        String hashedPassword = parts[1];
                        User.UserRole role = User.UserRole.valueOf(parts[2].toUpperCase());
                        users.add(new User(username, hashedPassword, role));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping malformed user line (role format error): " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed user line (incorrect parts): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        try (BufferedWriter writer = Files.newBufferedWriter(FileUtil.USER_DB_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (User user : users) {
                String line = String.join(JOIN_DELIMITER,
                        user.getUsername(),
                        user.getHashedPassword(),
                        user.getRole().name());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public void addUser(User user) {
        List<User> users = loadUsers();
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            System.err.println("User with username " + user.getUsername() + " already exists.");
            return;
        }
        users.add(user);
        saveUsers(users);
    }

    public Optional<User> findUserByUsername(String username) {
        return loadUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    // Method to initialize a default admin user if UserDB.txt is empty
    public void initializeDefaultAdminUser() {
        List<User> users = loadUsers();
        if (users.isEmpty()) {
            User adminUser = new User("admin", simpleHash("admin123"), User.UserRole.ADMIN);
            users.add(adminUser);
            saveUsers(users);
            System.out.println("Default admin user created: admin / admin123");
        }
    }
}
