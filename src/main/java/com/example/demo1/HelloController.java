package com.example.demo1;

import com.example.demo1.Database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class HelloController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMe;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean remember = rememberMe.isSelected();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Please enter both email and password.");
            return;
        }

        // Disable login button during authentication
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Perform database authentication
        try {
            if (authenticateUser(email, password)) {
                String username = getUsername(email);
                HelloApplication.showDashboard(username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database: " + e.getMessage());
            System.out.println("error" + e.getMessage());
            e.printStackTrace();
        } finally {
            // Re-enable login button
            loginButton.setDisable(false);
            loginButton.setText("Login");
        }
    }

    @FXML
    protected void onSignupClick() {
        showAlert(Alert.AlertType.INFORMATION, "Sign Up", "No signup functionality yet. Users are manually added to database.");
    }

    // Database authentication method
    private boolean authenticateUser(String email, String password) throws SQLException {
        String sql = "SELECT user_id FROM Users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if user exists with these credentials
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
        return "User"; // Default if not found
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}