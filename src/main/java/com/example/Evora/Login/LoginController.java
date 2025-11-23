package com.example.Evora.Login;

import com.example.Evora.Database.DatabaseConnection;
import com.example.Evora.Theme.ThemeService;
import com.example.Evora.Theme.ThemeManager;
import com.example.Evora.HelloApplication;
import javafx.scene.control.Alert;
import java.sql.*;

public class LoginController {

    public void handleLogin(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showAlert("Please fill in all fields", Alert.AlertType.WARNING);
            return;
        }

        // database authentication + setting theme
        try {
            int userId = authenticateUser(email, password);
            if (userId != -1) {
                String username = getUsername(email);
                System.out.println("✅ Login successful! User: " + username + " ID: " + userId);

                // LOAD USER THEME HERE
                loadUserTheme(userId);

                // Navigate to dashboard
                HelloApplication.showDashboard(username, userId);
            } else {
                showAlert("Invalid email or password", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    //helper to laod user theme
    private void loadUserTheme(int userId) {
        String userTheme = ThemeService.getUserTheme(userId);
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setTheme(userTheme);
        System.out.println("🎨 Loaded user theme: " + userTheme);
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
            int newUserId = registerUser(username, email, password);
            if (newUserId != -1) {
                // Set default theme for new user
                ThemeService.saveUserTheme(newUserId, "pastel");

                showAlert("Account created successfully! You can now login.", Alert.AlertType.INFORMATION);
                System.out.println("✅ Signup successful! User: " + username + " Email: " + email + " ID: " + newUserId);
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

    // USER REGISTRATION - returns registered id
    private int registerUser(String username, String email, String password) throws SQLException {
        // First check if email already exists
        if (emailExists(email)) {
            return -1;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert user
            String userSql = "INSERT INTO Users (username, email, password, created_at, experience, level, current_pet_id) OUTPUT INSERTED.user_id VALUES (?, ?, ?, GETDATE(), 0, 1, 1)";

            int newUserId = -1;

            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, email);
                userStmt.setString(3, password);

                try (ResultSet generatedKeys = userStmt.executeQuery()) {
                    if (generatedKeys.next()) {
                        newUserId = generatedKeys.getInt(1);
                        System.out.println("🆕 New user created with ID: " + newUserId);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            // INSERT DEFAULT PET INTO PETMASCOT
            if (newUserId != -1) {
                String petSql = "INSERT INTO PetMascot (user_id, pet_type_id, pet_name, is_equipped) VALUES (?, 1, 'Luna', 1)";
                try (PreparedStatement petStmt = conn.prepareStatement(petSql)) {
                    petStmt.setInt(1, newUserId);
                    int rowsAffected = petStmt.executeUpdate();
                    System.out.println("🐾 Default pet added to PetMascot for user " + newUserId + ", rows affected: " + rowsAffected);
                }

                conn.commit(); // Commit both operations
                return newUserId;
            } else {
                conn.rollback();
                return -1;
            }

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
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