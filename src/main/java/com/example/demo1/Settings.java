package com.example.demo1;

import com.example.demo1.Theme.*;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Database.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.*;

public class Settings {

    private ThemeManager themeManager;
    private int userId;
    private Sidebar sidebar;

    public Settings(int userId) {
        this.themeManager = ThemeManager.getInstance();
        this.userId = userId;
    }

    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }


    public VBox getContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Apply current theme
        applyTheme(mainLayout, themeManager.getCurrentTheme());

        // Header
        VBox header = createHeader();

        // Theme Selection
        VBox themeSection = createThemeSelection();

        // Profile Settings
        VBox profileSection = createProfileSettings();

        // App Information
        VBox aboutSection = createAboutSection();

        mainLayout.getChildren().addAll(header, themeSection, profileSection, aboutSection);
        return mainLayout;
    }

    private void applyTheme(VBox mainLayout, Theme theme) {
        mainLayout.setStyle("-fx-background-color: " + theme.getBackgroundColor() + ";");
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Settings ⚙");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        // Use dynamic style that checks theme every time
        title.setStyle(getDynamicTextStyle());

        Label subtitle = new Label("Customize your Évora experience");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setStyle(getDynamicTextStyle());

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createThemeSelection() {
        VBox themeCard = createCard("🎨 Choose Your Theme", 800);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Theme options
        HBox themesContainer = new HBox(20);
        themesContainer.setAlignment(Pos.CENTER);

        String[][] themes = {
                {"pastel", "🎀 Classic Pastel", "Soft pink, mint, lavender, peach", Pastel.PINK, Pastel.LAVENDER},
                {"galaxy", "🌌 Galaxy", "Deep space blues, cosmic purples, starry accents", getGalaxyColor("NEBULA_PURPLE"), getGalaxyColor("STAR_BLUE")}
        };

        for (String[] theme : themes) {
            VBox themeOption = createThemeOption(theme[0], theme[1], theme[2], theme[3], theme[4]);
            themesContainer.getChildren().add(themeOption);
        }

        content.getChildren().add(themesContainer);
        themeCard.getChildren().add(content);
        return themeCard;
    }

    private VBox createThemeOption(String id, String name, String description, String color1, String color2) {
        VBox themeOption = new VBox(10);
        themeOption.setAlignment(Pos.CENTER);
        themeOption.setPadding(new Insets(20));
        themeOption.setPrefSize(200, 180);
        themeOption.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;",
                color1, color2
        ));

        // Color preview circle
        StackPane colorPreview = new StackPane();
        colorPreview.setPrefSize(60, 60);
        colorPreview.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 30; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);",
                color1, color2
        ));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        // ALWAYS BLACK TEXT for theme option labels
        nameLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        // ALWAYS BLACK TEXT for theme option labels
        descLabel.setStyle("-fx-text-fill: black; -fx-font-size: 11;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(150);

        themeOption.getChildren().addAll(colorPreview, nameLabel, descLabel);

        // Add hover effect
        themeOption.setOnMouseEntered(e -> {
            themeOption.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6); -fx-cursor: hand;",
                    color1, color2
            ));
        });

        themeOption.setOnMouseExited(e -> {
            themeOption.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;",
                    color1, color2
            ));
        });

        // === FIX: ONLY ONE CLICK HANDLER ===
        themeOption.setOnMouseClicked(e -> {
            // Set the theme in ThemeManager
            themeManager.setTheme(id);

            // Save to database
            ThemeService.saveUserTheme(userId, id);

            // Show notification
            showThemeChangeNotification(id);

            // Debug: Print to confirm it's working
            System.out.println("🎨 Theme changed to: " + id + " for user: " + userId);
        });

        return themeOption;
    }

    private void showThemeChangeNotification(String themeName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Theme Changed");
        alert.setHeaderText(null);
        alert.setContentText("Theme successfully changed to " + themeName);

        // Style the alert with current theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + (isGalaxyTheme() ? "#FFFFFF" : themeManager.getCurrentTheme().getTextPrimary()) + ";");

        alert.showAndWait();
    }

    private VBox createProfileSettings() {
        VBox profileCard = createCard("👤 Profile Settings", 800);
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Change Password Section
        VBox passwordSection = createPasswordSection();

        // Change Username Section
        VBox usernameSection = createUsernameSection();

        content.getChildren().addAll(passwordSection, usernameSection);
        profileCard.getChildren().add(content);
        return profileCard;
    }

    private VBox createPasswordSection() {
        VBox passwordSection = new VBox(10);
        passwordSection.setPadding(new Insets(15));
        passwordSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");

        Label sectionTitle = new Label("🔐 Change Password");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setStyle(getDynamicTextStyle());

        // Current Password
        VBox currentPassBox = new VBox(5);
        Label currentPassLabel = new Label("Current Password");
        currentPassLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        currentPassLabel.setStyle(getDynamicTextStyle());

        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Enter current password");
        currentPassField.setStyle(getTextFieldStyle());
        currentPassBox.getChildren().addAll(currentPassLabel, currentPassField);

        // New Password
        VBox newPassBox = new VBox(5);
        Label newPassLabel = new Label("New Password");
        newPassLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        newPassLabel.setStyle(getDynamicTextStyle());

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Enter new password");
        newPassField.setStyle(getTextFieldStyle());
        newPassBox.getChildren().addAll(newPassLabel, newPassField);

        // Confirm New Password
        VBox confirmPassBox = new VBox(5);
        Label confirmPassLabel = new Label("Confirm New Password");
        confirmPassLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirmPassLabel.setStyle(getDynamicTextStyle());

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setStyle(getTextFieldStyle());
        confirmPassBox.getChildren().addAll(confirmPassLabel, confirmPassField);

        // Update Password Button
        Button updatePassButton = new Button("Update Password 🔑");
        updatePassButton.setStyle(getButtonStyle(themeManager.getCurrentTheme().getPrimaryColor()));
        updatePassButton.setOnAction(e -> updatePassword(currentPassField.getText(), newPassField.getText(), confirmPassField.getText()));

        passwordSection.getChildren().addAll(sectionTitle, currentPassBox, newPassBox, confirmPassBox, updatePassButton);
        return passwordSection;
    }

    private VBox createUsernameSection() {
        VBox usernameSection = new VBox(10);
        usernameSection.setPadding(new Insets(15));
        usernameSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");

        Label sectionTitle = new Label("👤 Change Username");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setStyle(getDynamicTextStyle());

        // New Username
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("New Username");
        usernameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        usernameLabel.setStyle(getDynamicTextStyle());

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter new username");
        usernameField.setStyle(getTextFieldStyle());
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Update Username Button
        Button updateUsernameButton = new Button("Update Username ✨");
        updateUsernameButton.setStyle(getButtonStyle(themeManager.getCurrentTheme().getSecondaryColor()));
        updateUsernameButton.setOnAction(e -> updateUsername(usernameField.getText()));

        usernameSection.getChildren().addAll(sectionTitle, usernameBox, updateUsernameButton);
        return usernameSection;
    }

    private String getTextFieldStyle() {
        return "-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; " +
                "-fx-border-color: " + themeManager.getCurrentTheme().getPrimaryColor() + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 8; -fx-font-size: 14; " +
                "-fx-text-fill: " + (isGalaxyTheme() ? "#FFFFFF" : themeManager.getCurrentTheme().getTextPrimary()) + ";";
    }

    private String getButtonStyle(String color) {
        String baseStyle = "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-padding: 12 25; -fx-cursor: hand;";

        return baseStyle;
    }

    // NEW: Dynamic text style that checks current theme
    private String getDynamicTextStyle() {
        if (isGalaxyTheme()) {
            return "-fx-text-fill: #FFFFFF;"; // White for galaxy
        } else {
            return "-fx-text-fill: " + themeManager.getCurrentTheme().getTextPrimary() + ";";
        }
    }

    // Helper method to check if current theme is Galaxy
    private boolean isGalaxyTheme() {
        return "galaxy".equals(themeManager.getThemeName().toLowerCase());
    }

    private String getGalaxyColor(String colorName) {
        try {
            java.lang.reflect.Field field = Class.forName("com.example.demo1.Theme.Galaxy").getField(colorName);
            return (String) field.get(null);
        } catch (Exception e) {
            switch (colorName) {
                case "NEBULA_PURPLE": return "#9d99d6";
                case "STAR_BLUE": return "#7988ad";
                case "COSMIC_PINK": return "#b8a7c5";
                case "SUPERNOVA_YELLOW": return "#b8a7c5";
                default: return "#9d99d6";
            }
        }
    }

    private void updatePassword(String currentPass, String newPass, String confirmPass) {
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert("Error", "Please fill in all password fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert("Error", "New passwords do not match.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long.");
            return;
        }

        // Verify current password and update in database
        if (verifyCurrentPassword(currentPass)) {
            if (updatePasswordInDatabase(newPass)) {
                showAlert("Success", "Password updated successfully! 🔑");
            } else {
                showAlert("Error", "Failed to update password. Please try again.");
            }
        } else {
            showAlert("Error", "Current password is incorrect.");
        }
    }

    private void updateUsername(String newUsername) {
        if (newUsername.isEmpty()) {
            showAlert("Error", "Please enter a new username.");
            return;
        }

        if (newUsername.length() < 3) {
            showAlert("Error", "Username must be at least 3 characters long.");
            return;
        }

        if (updateUsernameInDatabase(newUsername)) {
            showAlert("Success", "Username updated successfully! ✨");
            sidebar.refreshUsername(newUsername); //refresh username in sidebar
        } else {
            showAlert("Error", "Failed to update username. Please try again.");
        }
    }

    private boolean verifyCurrentPassword(String currentPass) {
        String sql = "SELECT password FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(currentPass);
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean updatePasswordInDatabase(String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateUsernameInDatabase(String newUsername) {
        String sql = "UPDATE Users SET username = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newUsername);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating username: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert with current theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + (isGalaxyTheme() ? "#FFFFFF" : themeManager.getCurrentTheme().getTextPrimary()) + ";");

        alert.showAndWait();
    }
    private VBox createAboutSection() {
        VBox aboutCard = createCard("🌸 About Évora", 800);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

         Label flower = new Label("🌸");
        flower.setFont(Font.font(48));
        flower.setStyle(getDynamicTextStyle());

        Label title = new Label("Évora - Your Magical Productivity Companion");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle(getDynamicTextStyle());
        title.setAlignment(Pos.CENTER);
        title.setWrapText(true);

        // Pets Section
        VBox petsSection = new VBox(15);
        petsSection.setAlignment(Pos.CENTER);
        petsSection.setPadding(new Insets(20));
        petsSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");

        Label petsTitle = new Label("🐾 Meet Your Adorable Companions!");
        petsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        petsTitle.setStyle(getDynamicTextStyle());

        HBox petsContainer = new HBox(30);
        petsContainer.setAlignment(Pos.CENTER);

        String[] petGifs = {"cat.gif", "bunny.gif", "owl.gif", "dragon.gif"};
        String[] petNames = {"Luna", "Cocoa", "Hoot", "Sage"};

        for (int i = 0; i < petGifs.length; i++) {
            VBox petDisplay = createPetDisplay(petGifs[i], petNames[i]);
            petsContainer.getChildren().add(petDisplay);
        }

        petsSection.getChildren().addAll(petsTitle, petsContainer);

        // Features Section
        VBox featuresSection = new VBox(15);
        featuresSection.setAlignment(Pos.CENTER_LEFT);
        featuresSection.setPadding(new Insets(20));
        featuresSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");
        featuresSection.setMaxWidth(600);

        Label featuresTitle = new Label("🎯 How Évora Works Its Magic:");
        featuresTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        featuresTitle.setStyle(getDynamicTextStyle());

        VBox featuresList = new VBox(8);
        featuresList.setAlignment(Pos.CENTER_LEFT);

        String[] features = {
                "🍅 Complete Pomodoro sessions → Earn cute pets & experience!",
                "📝 Add sticky notes & to-dos → Level up your productivity!",
                "😊 Log your daily mood → Gain XP and grow with your pet!",
                "🎵 Use white noise → Focus better and earn rewards!",
                "📅 Track your calendar → Stay organized and productive!",
                "⭐ Level up → Unlock new pets and special abilities!"
        };

        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.setFont(Font.font("Segoe UI", 13));
            featureLabel.setStyle(getDynamicTextStyle());
            featureLabel.setWrapText(true);
            featuresList.getChildren().add(featureLabel);
        }

        featuresSection.getChildren().addAll(featuresTitle, featuresList);

        // Final Message - Gradient Effect Section
        VBox gradientMessageBox = new VBox(10);
        gradientMessageBox.setAlignment(Pos.CENTER);
        gradientMessageBox.setPadding(new Insets(25));
        gradientMessageBox.setMaxWidth(600);

        // Create beautiful gradient based on theme
        if (isGalaxyTheme()) {
            gradientMessageBox.setStyle("-fx-background-color: linear-gradient(to right, " +
                    getGalaxyColor("NEBULA_PURPLE") + ", " + getGalaxyColor("COSMIC_PINK") + ", " + getGalaxyColor("STAR_BLUE") + "); " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
                    "-fx-border-color: " + getGalaxyColor("SUPERNOVA_YELLOW") + "; " +
                    "-fx-border-width: 2; -fx-border-radius: 20;");
        } else {
            gradientMessageBox.setStyle("-fx-background-color: linear-gradient(to right, " +
                    Pastel.PINK + ", " + Pastel.LAVENDER + ", " + Pastel.BLUE + "); " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                    "-fx-border-color: " + Pastel.GOLD + "; " +
                    "-fx-border-width: 2; -fx-border-radius: 20;");
        }

        Label finalMessage = new Label("Évora turns productivity into a magical adventure! " +
                "With every task completed and every mood logged, you're not just being productive - " +
                "you're growing with your adorable companion! 💫");
        finalMessage.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        // FIXED: Use dynamic text color instead of hardcoded white
        finalMessage.setStyle(getDynamicTextStyle() + " -fx-font-style: italic; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");
        finalMessage.setWrapText(true);
        finalMessage.setAlignment(Pos.CENTER);

        Label sparkle = new Label("✨");
        sparkle.setFont(Font.font(24));
        sparkle.setStyle(getDynamicTextStyle());

        gradientMessageBox.getChildren().addAll(finalMessage, sparkle);

        content.getChildren().addAll(flower, title, petsSection, featuresSection, gradientMessageBox);
        aboutCard.getChildren().add(content);
        return aboutCard;
    }

    private VBox createPetDisplay(String gifFilename, String petName) {
        VBox petDisplay = new VBox(8);
        petDisplay.setAlignment(Pos.CENTER);
        petDisplay.setPadding(new Insets(10));

        try {
            ImageView petImage = new ImageView(new Image(getClass().getResource("/pet_gifs/" + gifFilename).toExternalForm()));
            petImage.setFitWidth(100);
            petImage.setFitHeight(100);
            petImage.setPreserveRatio(true);
            petImage.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
            petDisplay.getChildren().add(petImage);
        } catch (Exception e) {
            Label emojiLabel = new Label(getEmojiForPet(petName));
            emojiLabel.setFont(Font.font(36));
            petDisplay.getChildren().add(emojiLabel);
        }

        Label nameLabel = new Label(petName);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setStyle(getDynamicTextStyle());
        petDisplay.getChildren().add(nameLabel);

        return petDisplay;
    }

    private String getEmojiForPet(String petName) {
        switch (petName.toLowerCase()) {
            case "luna": return "🐱";
            case "cocoa": return "🐰";
            case "hoot": return "🦉";
            case "sage": return "🐉";
            default: return "🐾";
        }
    }

    private VBox createCard(String title, double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setStyle(getDynamicTextStyle());
        titleLabel.setPadding(new Insets(20, 20, 10, 20));
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().add(titleLabel);
        return card;
    }
}