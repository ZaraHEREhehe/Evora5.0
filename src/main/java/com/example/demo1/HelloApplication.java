package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;  // store reference to stage

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Load the login page
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Évora - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(400);
        stage.setMinHeight(500);
        stage.show();
    }

    // ✅ Method to switch to Dashboard after login
    public static void showDashboard() {
        Dashboard dashboard = new Dashboard(primaryStage);
        dashboard.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
