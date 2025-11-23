package com.example.Evora;

import com.example.Evora.Login.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLoginScreen();
    }
    private void showLoginScreen() {
        try {
            LoginView loginViewApp = new LoginView();
            loginViewApp.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Updated method to accept username - Reusing same stage
    public static void showDashboard(String username, int userId) {
        try {
            System.out.println("🚀 Transitioning to dashboard for: " + username + " (ID: " + userId + ")");

            // Create MainController - it will create its own scene
            MainController mainController = new MainController(primaryStage, username, userId);

            // MainController's constructor already sets the scene, so we're done here
            System.out.println("✅ Dashboard transition complete!");

        } catch (Exception e) {
            System.out.println("❌ Error in showDashboard: " + e.getMessage());
            e.printStackTrace();
            System.out.println("❌ Error loading dashboard: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}