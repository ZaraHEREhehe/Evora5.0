package com.example.demo1;

import com.example.demo1.Theme.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Load the login page
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ThemeManager.applyTheme(scene, ThemeManager.Theme.PASTEL);
        stage.setTitle("Pomodoro Timer");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(500);
        stage.show();
    }

    // Updated method to accept username - Reusing same stage
    public static void showDashboard(String username, int userId) {
        try {
            MainController mainController = new MainController(primaryStage, username, userId);

            // Update the title
            primaryStage.setTitle("Pastel Productivity Dashboard - Welcome, " + username);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error loading dashboard: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}