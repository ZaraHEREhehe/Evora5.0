//HelloApplication.java
package com.example.demo1;

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

      //  scene.getStylesheets().add(getClass().getResource("/com/example/demo1/themeapp/styles/pastel.css").toExternalForm());

        stage.setTitle("Évora - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(400);
        stage.setMinHeight(500);
        stage.show();
    }

    // ✅ Method to switch to Dashboard after login
    public static void showDashboard() {
        BorderPane root = new BorderPane();
        MainController mainController = new MainController(root, "Insharah");

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pastel Productivity Dashboard");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}