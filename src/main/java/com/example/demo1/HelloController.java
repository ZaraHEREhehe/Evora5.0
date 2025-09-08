package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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

        // ✅ Simple login check (replace with real authentication later)
        if (!email.isEmpty() && !password.isEmpty()) {
            // If login is valid → go to dashboard
            HelloApplication.showDashboard();
        } else {
            // If login fails → show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("Invalid Credentials");
            alert.setContentText("Please enter both email and password.");
            alert.showAndWait();
        }
    }

    @FXML
    protected void onSignupClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sign Up");
        alert.setHeaderText(null);
        alert.setContentText("Redirecting to signup page...");
        alert.showAndWait();
    }
}
