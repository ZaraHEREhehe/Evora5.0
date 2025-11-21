package com.example.demo1.Login;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.net.URL;

public class LoginView extends Application {

    private static Stage primaryStage;
    private MediaPlayer mediaPlayer;
    private LoginController controller;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField signupUsernameField;
    private TextField signupEmailField;
    private PasswordField signupPasswordField;
    private PasswordField signupConfirmPasswordField;

    private VBox loginSection;
    private VBox signupSection;
    private StackPane mainContent;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        controller = new LoginController();

        // Configure stage
        stage.setTitle("Évora - Your Productivity Companion");
        stage.setMaximized(true);
        stage.setResizable(true);

        // Create UI INSTANTLY
        BorderPane root = createLoginUI();
        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);

        // Show immediately - video loads in background
        stage.setScene(scene);
        stage.show();
    }

    private BorderPane createLoginUI() {
        BorderPane root = new BorderPane();
        mainContent = new StackPane();

        // Video section
        Pane videoSection = createVideoSection();

        // Login section
        loginSection = createLoginSection();
        signupSection = createSignupSection();
        signupSection.setVisible(false);

        mainContent.getChildren().addAll(videoSection, loginSection, signupSection);
        root.setCenter(mainContent);

        return root;
    }

    private Pane createVideoSection() {
        StackPane videoContainer = new StackPane();
        videoContainer.setStyle("-fx-background-color: #000000;");

        // Create video - SIMPLE AND FAST
        MediaView mediaView = setupVideoFast();
        mediaView.setPreserveRatio(false);
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty());

        videoContainer.getChildren().add(mediaView);
        return videoContainer;
    }

    private MediaView setupVideoFast() {
        MediaView mediaView = new MediaView();

        try {
            URL videoUrl = getClass().getResource("/Welcome/welcome.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toString());
                mediaPlayer = new MediaPlayer(media);

                // SIMPLE SETUP - NO COMPLEX LISTENERS
                mediaView.setMediaPlayer(mediaPlayer);
                mediaView.setPreserveRatio(false);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0);

                // START PLAYING IMMEDIATELY - don't wait for "ready"
                mediaPlayer.play();

                System.out.println("▶️ Video started instantly");
            }
        } catch (Exception e) {
            System.out.println("Video error: " + e.getMessage());
        }

        return mediaView;
    }

    private VBox createLoginSection() {
        VBox loginSection = new VBox(20);
        loginSection.setAlignment(Pos.CENTER);
        loginSection.setStyle("-fx-background-color: white; " +
                "-fx-padding: 30; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 0 0 0 1;");

        loginSection.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.2));
        loginSection.setMaxWidth(Region.USE_PREF_SIZE);
        loginSection.setMinWidth(350);
        loginSection.prefHeightProperty().bind(primaryStage.heightProperty());
        StackPane.setAlignment(loginSection, Pos.CENTER_RIGHT);

        VBox appHeader = createAppHeader();
        VBox welcomeText = createWelcomeText();
        VBox loginForm = createLoginForm();

        loginSection.getChildren().addAll(appHeader, welcomeText, loginForm);
        return loginSection;
    }

    private VBox createSignupSection() {
        VBox signupSection = new VBox(20);
        signupSection.setAlignment(Pos.CENTER);
        signupSection.setStyle("-fx-background-color: white; " +
                "-fx-padding: 30; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 0 0 0 1;");

        signupSection.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.2));
        signupSection.setMaxWidth(Region.USE_PREF_SIZE);
        signupSection.setMinWidth(350);
        signupSection.prefHeightProperty().bind(primaryStage.heightProperty());
        StackPane.setAlignment(signupSection, Pos.CENTER_RIGHT);

        VBox appHeader = createAppHeader();
        VBox welcomeText = createSignupWelcomeText();
        VBox signupForm = createSignupForm();

        signupSection.getChildren().addAll(appHeader, welcomeText, signupForm);
        return signupSection;
    }

    private VBox createAppHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);

        Label appName = new Label("Évora");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        appName.setTextFill(Color.web("#e75480"));
        appName.setStyle("-fx-font-family: 'Segoe UI', 'Brush Script MT', cursive;");

        Label tagline = new Label("Productivity Companion");
        tagline.setFont(Font.font("Segoe UI", 11));
        tagline.setTextFill(Color.web("#888"));
        tagline.setStyle("-fx-font-style: italic;");

        header.getChildren().addAll(appName, tagline);
        return header;
    }

    private VBox createWelcomeText() {
        VBox welcome = new VBox(5);
        welcome.setAlignment(Pos.CENTER_LEFT);

        Label welcomeBack = new Label("Welcome Back!");
        welcomeBack.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        welcomeBack.setTextFill(Color.web("#333"));

        Label instruction = new Label("Sign in to continue your journey");
        instruction.setFont(Font.font("Segoe UI", 12));
        instruction.setTextFill(Color.web("#666"));

        welcome.getChildren().addAll(welcomeBack, instruction);
        return welcome;
    }

    private VBox createSignupWelcomeText() {
        VBox welcome = new VBox(5);
        welcome.setAlignment(Pos.CENTER_LEFT);

        Label joinUs = new Label("Join Évora!");
        joinUs.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        joinUs.setTextFill(Color.web("#333"));

        Label instruction = new Label("Create your account to get started");
        instruction.setFont(Font.font("Segoe UI", 12));
        instruction.setTextFill(Color.web("#666"));

        welcome.getChildren().addAll(joinUs, instruction);
        return welcome;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPrefWidth(280);

        VBox emailBox = createFormField("Email Address", "Enter your email", false);
        emailField = (TextField) emailBox.getChildren().get(1);

        VBox passwordBox = createFormField("Password", "Enter your password", true);
        passwordField = (PasswordField) passwordBox.getChildren().get(1);

        HBox options = createFormOptions();
        Button loginButton = createLoginButton();
        HBox signupLink = createSignupLink();

        form.getChildren().addAll(
                emailBox, passwordBox, options, loginButton, signupLink
        );

        return form;
    }

    private VBox createSignupForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPrefWidth(280);

        VBox usernameBox = createFormField("Username", "Choose a username", false);
        signupUsernameField = (TextField) usernameBox.getChildren().get(1);

        VBox emailBox = createFormField("Email Address", "Enter your email", false);
        signupEmailField = (TextField) emailBox.getChildren().get(1);

        VBox passwordBox = createFormField("Password", "Create a password", true);
        signupPasswordField = (PasswordField) passwordBox.getChildren().get(1);

        VBox confirmPasswordBox = createFormField("Confirm Password", "Confirm your password", true);
        signupConfirmPasswordField = (PasswordField) confirmPasswordBox.getChildren().get(1);

        Button signupButton = createSignupButton();
        HBox loginLink = createLoginLink();

        form.getChildren().addAll(
                usernameBox, emailBox, passwordBox, confirmPasswordBox, signupButton, loginLink
        );

        return form;
    }

    private VBox createFormField(String labelText, String promptText, boolean isPassword) {
        VBox field = new VBox(5);
        field.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#555"));

        TextInputControl input;
        if (isPassword) {
            input = new PasswordField();
        } else {
            input = new TextField();
        }

        input.setPromptText(promptText);
        input.setPrefHeight(38);
        input.setPrefWidth(280);
        input.setStyle("-fx-background-color: #f8f8f8; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-padding: 0 12; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 13px;");

        field.getChildren().addAll(label, input);
        return field;
    }

    private HBox createFormOptions() {
        HBox options = new HBox(60);
        options.setAlignment(Pos.CENTER_LEFT);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; -fx-text-fill: #666;");

        Button forgotPassword = new Button("Forgot Password?");
        forgotPassword.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #e75480; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; " +
                "-fx-underline: true; " +
                "-fx-padding: 0;");

        options.getChildren().addAll(rememberMe, forgotPassword);
        return options;
    }

    private Button createLoginButton() {
        Button loginButton = new Button("Sign In");
        loginButton.setPrefWidth(280);
        loginButton.setPrefHeight(42);
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #ff9a9e, #fad0c4); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;");

        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle(loginButton.getStyle() +
                    "-fx-effect: dropshadow(gaussian, rgba(255,182,193,0.8), 15, 0.5, 0, 0);");
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(loginButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(255,182,193,0.8), 15, 0.5, 0, 0);", ""));
        });

        loginButton.setOnAction(e -> controller.handleLogin(
                emailField.getText(),
                passwordField.getText()
        ));

        return loginButton;
    }

    private Button createSignupButton() {
        Button signupButton = new Button("Create Account");
        signupButton.setPrefWidth(280);
        signupButton.setPrefHeight(42);
        signupButton.setStyle("-fx-background-color: linear-gradient(to right, #a18cd1, #fbc2eb); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;");

        signupButton.setOnMouseEntered(e -> {
            signupButton.setStyle(signupButton.getStyle() +
                    "-fx-effect: dropshadow(gaussian, rgba(161,140,209,0.8), 15, 0.5, 0, 0);");
        });

        signupButton.setOnMouseExited(e -> {
            signupButton.setStyle(signupButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(161,140,209,0.8), 15, 0.5, 0, 0);", ""));
        });

        signupButton.setOnAction(e -> controller.handleSignup(
                signupUsernameField.getText(),
                signupEmailField.getText(),
                signupPasswordField.getText(),
                signupConfirmPasswordField.getText()
        ));

        return signupButton;
    }

    private HBox createSignupLink() {
        HBox signupLink = new HBox(5);
        signupLink.setAlignment(Pos.CENTER);

        Label noAccount = new Label("Don't have an account?");
        noAccount.setStyle("-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; -fx-text-fill: #666;");

        Button signupBtn = new Button("Sign up");
        signupBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #e75480; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-underline: true; " +
                "-fx-padding: 0;");

        signupBtn.setOnAction(e -> switchToSignup());

        signupLink.getChildren().addAll(noAccount, signupBtn);
        return signupLink;
    }

    private HBox createLoginLink() {
        HBox loginLink = new HBox(5);
        loginLink.setAlignment(Pos.CENTER);

        Label haveAccount = new Label("Already have an account?");
        haveAccount.setStyle("-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; -fx-text-fill: #666;");

        Button loginBtn = new Button("Sign in");
        loginBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #e75480; " +
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-underline: true; " +
                "-fx-padding: 0;");

        loginBtn.setOnAction(e -> switchToLogin());

        loginLink.getChildren().addAll(haveAccount, loginBtn);
        return loginLink;
    }

    private void switchToSignup() {
        loginSection.setVisible(false);
        signupSection.setVisible(true);
        clearLoginFields();
    }

    private void switchToLogin() {
        signupSection.setVisible(false);
        loginSection.setVisible(true);
        clearSignupFields();
    }

    private void clearLoginFields() {
        emailField.clear();
        passwordField.clear();
    }

    private void clearSignupFields() {
        signupUsernameField.clear();
        signupEmailField.clear();
        signupPasswordField.clear();
        signupConfirmPasswordField.clear();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}