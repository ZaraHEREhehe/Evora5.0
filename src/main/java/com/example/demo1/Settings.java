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

        // Introduction Section
        VBox introSection = new VBox(10);
        introSection.setAlignment(Pos.CENTER);
        introSection.setPadding(new Insets(20));
        introSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");

        Label introTitle = new Label("✨ Welcome to Your Magical Productivity Journey!");
        introTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        introTitle.setStyle(getDynamicTextStyle());

        Label introText = new Label("Évora transforms productivity into an enchanting adventure! " +
                "Complete tasks, track your progress, and grow alongside your adorable pet companion. " +
                "Every action you take helps you level up and unlock new magical features!");
        introText.setFont(Font.font("Segoe UI", 14));
        introText.setStyle(getDynamicTextStyle());
        introText.setWrapText(true);
        introText.setAlignment(Pos.CENTER);

        introSection.getChildren().addAll(introTitle, introText);

        // Modules Section - Comprehensive Overview
        VBox modulesSection = new VBox(20);
        modulesSection.setAlignment(Pos.CENTER);
        modulesSection.setPadding(new Insets(20));
        modulesSection.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "; -fx-background-radius: 15;");

        Label modulesTitle = new Label("🎯 Explore Évora's Magical Modules");
        modulesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        modulesTitle.setStyle(getDynamicTextStyle());

        // To-Do List Module
        VBox todoModule = createModuleCard(
                "📝 To-Do List & Task Management",
                "• Create tasks with priorities (Low, Medium, High)\n" +
                        "• Set due dates and organize your workload\n" +
                        "• Drag and drop to reorder tasks effortlessly\n" +
                        "• Tasks automatically appear on your calendar\n" +
                        "• Earn XP: Adding tasks (10-30 XP by priority)\n" +
                        "• Complete tasks for big rewards (50-200 XP by priority)",
                Pastel.BLUE
        );

        // Pomodoro Timer Module
        VBox pomodoroModule = createModuleCard(
                "🍅 Pomodoro Timer",
                "• Stay focused with the 25/5 work-break technique\n" +
                        "• Customizable work and break durations\n" +
                        "• Multiple preset options for different workflows\n" +
                        "• Your pet companion works alongside you\n" +
                        "• Track completed sessions with tomato counters\n" +
                        "• Build consistent work habits with visual progress",
                Pastel.PINK
        );

        // Mood Tracking Module
        VBox moodModule = createModuleCard(
                "😊 Mood Tracking & Wellness",
                "• Log your daily emotions and feelings\n" +
                        "• Add personal notes to each mood entry\n" +
                        "• Visualize your emotional patterns over time\n" +
                        "• Track stress levels and mental wellbeing\n" +
                        "• Earn XP for consistent mood logging\n" +
                        "• Use as a personal journal for self-reflection",
                Pastel.LAVENDER
        );

        // Sticky Notes Module
        VBox notesModule = createModuleCard(
                "📌 Digital Sticky Notes",
                "• Create colorful sticky notes for quick thoughts\n" +
                "• Drag and drop notes anywhere on your board\n" +
                "• Customize colors to organize by category\n" +
                        "• Perfect for brainstorming and quick reminders\n" +
                        "• Use however you want - no rules or structure!\n" +
                        "• Earn XP for every note you create",
                Pastel.GOLD
        );

        // Pet Companion Module
        VBox petModule = createModuleCard(
                "🐾 Pet Companion System",
                "• Adopt and care for your magical pet companion\n" +
                        "• Unlock new pets as you level up (Cat, Bunny, Owl, Dragon)\n" +
                        "• Customize your pet's name and appearance\n" +
                        "• Earn badges for consistent app usage\n" +
                        "• Your pet's happiness grows with your productivity\n" +
                        "• Watch your companion evolve as you progress",
                Pastel.MINT
        );

        // Calendar Module
        VBox calendarModule = createModuleCard(
                "📅 Smart Calendar Integration",
                "• Visual monthly calendar with all your tasks\n" +
                        "• See due dates and pending tasks at a glance\n" +
                        "• Color-coded events based on priority\n" +
                        "• Seamless integration with your to-do list\n" +
                        "• Never miss a deadline with clear visual cues\n" +
                        "• Plan your week with comprehensive overview",
                Pastel.PEACH
        );

        // White Noise Module
        VBox whiteNoiseModule = createModuleCard(
                "🎵 White Noise & Focus Sounds",
                "• Curated collection of focus-enhancing sounds\n" +
                        "• Layer multiple sounds for custom combinations\n" +
                        "• Mix rain, waves, fireplace, and more\n" +
                        "• Create your perfect study or work environment\n" +
                        "• Adjust volumes for each sound individually\n" +
                        "• Background sounds to boost concentration",
                Pastel.SAGE
        );

        // Experience & Leveling System
        VBox levelingModule = createModuleCard(
                "⭐ Experience & Progression System",
                "• Earn XP for every productive action\n" +
                        "• Level up to unlock new features and pets\n" +
                        "• Track your progress with visual experience bars\n" +
                        "• Complete daily streaks for bonus rewards\n" +
                        "• Unlock achievements and special badges\n" +
                        "• Watch your productivity journey unfold!",
                Pastel.PURPLE
        );

        modulesSection.getChildren().addAll(
                modulesTitle, todoModule, pomodoroModule, moodModule,
                notesModule, petModule, calendarModule, whiteNoiseModule, levelingModule
        );

        // Pets Showcase Section
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
        String[] petNames = {"Luna the Cat", "Cocoa the Bunny", "Hoot the Owl", "Sage the Dragon"};
        String[] petDescriptions = {
                "Playful and curious companion",
                "Energetic and joyful friend",
                "Wise and focused buddy",
                "Magical and powerful partner"
        };

        for (int i = 0; i < petGifs.length; i++) {
            VBox petDisplay = createPetDisplay(petGifs[i], petNames[i], petDescriptions[i]);
            petsContainer.getChildren().add(petDisplay);
        }

        petsSection.getChildren().addAll(petsTitle, petsContainer);

        // Final Inspiration Section
        VBox inspirationSection = new VBox(15);
        inspirationSection.setAlignment(Pos.CENTER);
        inspirationSection.setPadding(new Insets(25));
        inspirationSection.setMaxWidth(600);

        // Create beautiful gradient based on theme
        if (isGalaxyTheme()) {
            inspirationSection.setStyle("-fx-background-color: linear-gradient(to right, " +
                    getGalaxyColor("NEBULA_PURPLE") + ", " + getGalaxyColor("COSMIC_PINK") + ", " + getGalaxyColor("STAR_BLUE") + "); " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); " +
                    "-fx-border-color: " + getGalaxyColor("SUPERNOVA_YELLOW") + "; " +
                    "-fx-border-width: 2; -fx-border-radius: 20;");
        } else {
            inspirationSection.setStyle("-fx-background-color: linear-gradient(to right, " +
                    Pastel.PINK + ", " + Pastel.LAVENDER + ", " + Pastel.BLUE + "); " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                    "-fx-border-color: " + Pastel.GOLD + "; " +
                    "-fx-border-width: 2; -fx-border-radius: 20;");
        }

        Label finalTitle = new Label("Your Magical Productivity Adventure Awaits! ✨");
        finalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        finalTitle.setStyle(getDynamicTextStyle() + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");

        Label finalMessage = new Label("Évora is more than just a productivity app - it's your personal journey towards " +
                "better habits, consistent progress, and joyful accomplishment. With every task completed, every mood logged, " +
                "and every session focused, you're not just being productive - you're growing, learning, and creating a magical " +
                "partnership with your companion. Let's make productivity enchanting together! 💫");
        finalMessage.setFont(Font.font("Segoe UI", 13));
        finalMessage.setStyle(getDynamicTextStyle() + " -fx-font-style: italic;");
        finalMessage.setWrapText(true);
        finalMessage.setAlignment(Pos.CENTER);

        Label sparkles = new Label("🌟 ✨ 🌟 ✨ 🌟");
        sparkles.setFont(Font.font(20));
        sparkles.setStyle(getDynamicTextStyle());

        inspirationSection.getChildren().addAll(finalTitle, finalMessage, sparkles);

        content.getChildren().addAll(flower, title, introSection, modulesSection, petsSection, inspirationSection);
        aboutCard.getChildren().add(content);
        return aboutCard;
    }

    private VBox createModuleCard(String title, String description, String color) {
        VBox moduleCard = new VBox(10);
        moduleCard.setAlignment(Pos.TOP_LEFT);
        moduleCard.setPadding(new Insets(15));
        moduleCard.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle(getDynamicTextStyle());

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setStyle(getDynamicTextStyle());
        descLabel.setWrapText(true);

        moduleCard.getChildren().addAll(titleLabel, descLabel);
        return moduleCard;
    }

    private VBox createPetDisplay(String gifFilename, String petName, String description) {
        VBox petDisplay = new VBox(8);
        petDisplay.setAlignment(Pos.CENTER);
        petDisplay.setPadding(new Insets(15));
        petDisplay.setStyle("-fx-background-color: " + themeManager.getCurrentTheme().getCardColor() + "80; -fx-background-radius: 15;");

        try {
            ImageView petImage = new ImageView(new Image(getClass().getResource("/pet_gifs/" + gifFilename).toExternalForm()));
            petImage.setFitWidth(80);
            petImage.setFitHeight(80);
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

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setStyle(getDynamicTextStyle());
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        petDisplay.getChildren().addAll(nameLabel, descLabel);
        return petDisplay;
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