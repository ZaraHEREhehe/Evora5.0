package com.example.demo1.Login;

import com.example.demo1.Database.DatabaseConnection;
import com.example.demo1.HelloApplication;
import javafx.scene.control.Alert;
import java.sql.*;

public class LoginController {

    public void handleLogin(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showAlert("Please fill in all fields", Alert.AlertType.WARNING);
            return;
        }

        // Use REAL database authentication
        try {
            int userId = authenticateUser(email, password);
            if (userId != -1) {
                String username = getUsername(email);
                System.out.println("✅ Login successful! User: " + username + " ID: " + userId);

                // Navigate to dashboard - THIS IS THE KEY
                HelloApplication.showDashboard(username, userId);
            } else {
                showAlert("Invalid email or password", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void handleSignup(String username, String email, String password, String confirmPassword) {
        if (username == null || username.isEmpty() || email == null || email.isEmpty() ||
                password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields", Alert.AlertType.WARNING);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 6) {
            showAlert("Password must be at least 6 characters long", Alert.AlertType.WARNING);
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Please enter a valid email address", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (registerUser(username, email, password)) {
                showAlert("Account created successfully! You can now login.", Alert.AlertType.INFORMATION);
                System.out.println("✅ Signup successful! User: " + username + " Email: " + email);
            } else {
                showAlert("Email already exists. Please use a different email.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // REAL DATABASE AUTHENTICATION
    private int authenticateUser(String email, String password) throws SQLException {
        String sql = "SELECT user_id FROM Users WHERE email = ? AND password = ?";
        System.out.println("🔐 Authenticating: " + email);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    System.out.println("✅ User found with ID: " + userId);
                    return userId;
                } else {
                    System.out.println("❌ No user found with these credentials");
                    return -1;
                }
            }
        }
    }

    // REAL USER REGISTRATION
    private boolean registerUser(String username, String email, String password) throws SQLException {
        // First check if email already exists
        if (emailExists(email)) {
            return false;
        }

        String sql = "INSERT INTO Users (username, email, password, created_at, experience, level) VALUES (?, ?, ?, GETDATE(), 0, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Check if email already exists
    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT user_id FROM Users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if email exists
            }
        }
    }

    // Get username from email
    private String getUsername(String email) throws SQLException {
        String sql = "SELECT username FROM Users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return "User";
    }

    // Simple email validation
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Évora");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}