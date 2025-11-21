package com.example.demo1;

import com.example.demo1.Login.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
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

    // FIXED: Properly transition to dashboard
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
        }
    }

    public static void main(String[] args) {
        launch();
    }
}