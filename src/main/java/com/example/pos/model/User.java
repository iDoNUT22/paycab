package com.example.pos.model;

import java.util.Objects;

public class User {
    private String username;
    private String hashedPassword; // Store hashed passwords, not plain text
    private UserRole role;

    public enum UserRole {
        ADMIN,
        CASHIER
    }

    public User(String username, String hashedPassword, UserRole role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public UserRole getRole() {
        return role;
    }

    // It's generally not a good idea to allow changing username or role easily after creation
    // Setter for password might be needed for password change functionality by an admin or user
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", role=" + role +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
