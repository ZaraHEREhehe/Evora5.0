package com.example.demo1.Sidebar;

import com.example.demo1.Theme.Pastel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import com.example.demo1.Database.DatabaseConnection;
import java.sql.*;

import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX Sidebar component with beautiful pastel theme
 */
public class Sidebar extends VBox {

    private final SidebarController controller;
    private final Map<String, Button> navButtons = new HashMap<>();
    private VBox mascotContainer; // Store reference to update it
    private VBox experienceContainer; // Store reference to update experience
    private Label expLabel;

    public Sidebar(SidebarController controller, String userName) {
        this.controller = controller;
        setupSidebar();
        createHeader(userName);
        createNavButtons();
        createExperienceSection();
        createMascotSection();
    }

    // Add this method to create the experience display
    private void createExperienceSection() {
        experienceContainer = new VBox(5);
        experienceContainer.setAlignment(Pos.CENTER);
        experienceContainer.setPadding(new Insets(12, 15, 12, 15));
        experienceContainer.setBackground(new Background(new BackgroundFill(
                Color.web(Pastel.IVORY),
                new CornerRadii(12),
                Insets.EMPTY
        )));
        experienceContainer.setBorder(new Border(new BorderStroke(
                Color.web(Pastel.GOLD, 0.3),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1.5)
        )));
        experienceContainer.setEffect(new DropShadow(5, Color.web(Pastel.GOLD, 0.2)));

        // Experience icon and text
        HBox expBox = new HBox(8);
        expBox.setAlignment(Pos.CENTER);

        Label starIcon = new Label("⭐");
        starIcon.setFont(Font.font(14));

        expLabel = new Label("800 XP");
        expLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        expLabel.setTextFill(Color.web(Pastel.FOREST));

        // Sparkle emoji for cuteness
        Label sparkle = new Label("✨");
        sparkle.setFont(Font.font(12));

        expBox.getChildren().addAll(starIcon, expLabel, sparkle);
        experienceContainer.getChildren().add(expBox);
        this.getChildren().add(experienceContainer);
    }

    // Simple method to update experience - just call this whenever exp changes
    public void updateExperience(int newExp) {
        expLabel.setText(newExp + " XP");

        // Cute little animation
        experienceContainer.setScaleX(1.05);
        experienceContainer.setScaleY(1.05);

        javafx.animation.ScaleTransition scaleTransition =
                new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), experienceContainer);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        // Add sparkle effect temporarily
        experienceContainer.setEffect(new DropShadow(10, Color.web(Pastel.GOLD)));

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> {
            experienceContainer.setEffect(new DropShadow(5, Color.web(Pastel.GOLD, 0.2)));
        });
        pause.play();
    }

    public void refreshExperienceFromDatabase(int userId) {
        int currentExp = getCurrentUserExperience(userId);
        updateExperience(currentExp);
    }

    private int getCurrentUserExperience(int userId) {
        String sql = "SELECT experience FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("experience");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user experience: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Add this method to update the mascot display
    public void updateMascot(String petName, String species, String gifFilename) {
        if (mascotContainer != null) {
            mascotContainer.getChildren().clear();

            try {
                // Try to load the pet GIF
                ImageView petImage = new ImageView(new Image(getPetGifPath(gifFilename), 60, 60, true, true));
                mascotContainer.getChildren().add(petImage);
            } catch (Exception e) {
                // Fallback to species emoji
                Label emojiLabel = new Label(getSpeciesEmoji(species));
                emojiLabel.setFont(Font.font(36));
                mascotContainer.getChildren().add(emojiLabel);
            }

            Label mascotText = new Label(petName + " the " + species);
            mascotText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            mascotText.setTextFill(Color.web(Pastel.FOREST));
            mascotText.setAlignment(Pos.CENTER);
            mascotText.setWrapText(true);
            mascotText.setMaxWidth(150);

            mascotContainer.getChildren().add(mascotText);
        }
    }

    // Helper method to get GIF path
    private String getPetGifPath(String filename) {
        return getClass().getResource("/pet_gifs/" + filename).toExternalForm();
    }

    // Helper method to get species emoji
    private String getSpeciesEmoji(String species) {
        switch (species.toLowerCase()) {
            case "cat": return "🐱";
            case "bunny": return "🐰";
            case "owl": return "🦉";
            case "dragon": return "🐉";
            default: return "🦊";
        }
    }

    private void setupSidebar() {
        this.setPrefWidth(260);
        this.setMinHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setPadding(new Insets(25, 20, 30, 20));
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);

        BackgroundFill backgroundFill = new BackgroundFill(
                Color.web(Pastel.BLUSH),
                new CornerRadii(0),
                Insets.EMPTY
        );
        this.setBackground(new Background(backgroundFill));

        this.setEffect(new DropShadow(15, 5, 5, Color.gray(0, 0.1)));
        this.setStyle("-fx-border-color: " + Pastel.PINK + "; -fx-border-width: 0 2 0 0;");
    }

    private void createHeader(String userName) {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 25, 0));

        Label title = new Label("Évora 🌸");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(Pastel.FOREST));
        title.setStyle("-fx-effect: dropshadow(gaussian, " + Pastel.PINK + ", 10, 0.3, 0, 2);");

        Label userLabel = new Label("Hello, " + userName + "! 💫");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        userLabel.setTextFill(Color.web(Pastel.SAGE));
        userLabel.setWrapText(true);
        userLabel.setAlignment(Pos.CENTER);

        Region separator = new Region();
        separator.setPrefHeight(2);
        separator.setBackground(new Background(new BackgroundFill(
                Color.web(Pastel.PINK),
                new CornerRadii(10),
                Insets.EMPTY
        )));
        separator.setMaxWidth(180);

        header.getChildren().addAll(title, userLabel, separator);
        this.getChildren().add(header);
    }

    private void createNavButtons() {
        VBox navBox = new VBox(12);
        navBox.setAlignment(Pos.TOP_CENTER);
        navBox.setFillWidth(true);

        String[][] items = {
                {"dashboard", "🏠 Dashboard", Pastel.PINK},
                {"todos", "📝 To-Do List", Pastel.LAVENDER},
                {"timer", "⏰ Pomodoro Timer", Pastel.BLUE},
                {"notes", "📒 Notes", Pastel.PURPLE},
                {"pet", "🐾 Virtual Pet", Pastel.LILAC},
                {"stats", "📊 Analytics", Pastel.ROSE},
                {"calendar", "📅 Calendar", Pastel.SKY},
                {"mood", "😊 Mood Tracker", Pastel.MINT},
                {"whitenoise", "🎵 White Noise", Pastel.PEACH},
                {"settings", "⚙️ Settings", Pastel.LEMON}
        };

        for (String[] item : items) {
            Button btn = createNavButton(item[0], item[1], item[2]);
            navButtons.put(item[0], btn);
            navBox.getChildren().add(btn);
        }

        this.getChildren().add(navBox);
    }

    private Button createNavButton(String id, String label, String color) {
        Button btn = new Button(label);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.setTextFill(Color.web(Pastel.FOREST));

        btn.setBackground(new Background(new BackgroundFill(
                Color.web(color),
                new CornerRadii(12),
                new Insets(2)
        )));

        btn.setBorder(new Border(new BorderStroke(
                Color.web(color).darker(),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1.5)
        )));

        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.02);
            btn.setScaleY(1.02);
            btn.setEffect(new DropShadow(10, Color.web(color).darker()));
        });

        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            btn.setEffect(null);
        });

        btn.setOnAction(e -> {
            highlightButton(id);
            controller.navigate(id);
        });

        return btn;
    }

    private void highlightButton(String activeId) {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            Button btn = entry.getValue();
            String originalColor = getOriginalColor(entry.getKey());

            if (entry.getKey().equals(activeId)) {
                btn.setBackground(new Background(new BackgroundFill(
                        Color.web(originalColor).brighter(),
                        new CornerRadii(12),
                        new Insets(2)
                )));
                btn.setTextFill(Color.WHITE);
                btn.setBorder(new Border(new BorderStroke(
                        Color.web(originalColor).brighter(),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(12),
                        new BorderWidths(2)
                )));
                btn.setEffect(new DropShadow(15, Color.web(originalColor).brighter()));
            } else {
                btn.setBackground(new Background(new BackgroundFill(
                        Color.web(originalColor),
                        new CornerRadii(12),
                        new Insets(2)
                )));
                btn.setTextFill(Color.web(Pastel.FOREST));
                btn.setBorder(new Border(new BorderStroke(
                        Color.web(originalColor).darker(),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(12),
                        new BorderWidths(1.5)
                )));
                btn.setEffect(null);
                btn.setScaleX(1.0);
                btn.setScaleY(1.0);
            }
        }
    }

    private String getOriginalColor(String buttonId) {
        switch (buttonId) {
            case "dashboard": return Pastel.PINK;
            case "todos": return Pastel.LAVENDER;
            case "timer": return Pastel.BLUE;
            case "notes": return Pastel.PURPLE;
            case "pet": return Pastel.LILAC;
            case "stats": return Pastel.ROSE;
            case "calendar": return Pastel.SKY;
            case "mood": return Pastel.MINT;
            case "whitenoise": return Pastel.PEACH;
            case "settings": return Pastel.LEMON;
            default: return Pastel.PINK;
        }
    }

    private void createMascotSection() {
        VBox mascotBox = new VBox(15);
        mascotBox.setAlignment(Pos.CENTER);
        mascotBox.setPadding(new Insets(30, 0, 0, 0));

        // Store reference to mascot container so we can update it later
        mascotContainer = new VBox(8);
        mascotContainer.setAlignment(Pos.CENTER);
        mascotContainer.setPadding(new Insets(15));
        mascotContainer.setBackground(new Background(new BackgroundFill(
                Color.web(Pastel.IVORY),
                new CornerRadii(15),
                Insets.EMPTY
        )));
        mascotContainer.setBorder(new Border(new BorderStroke(
                Color.web(Pastel.PINK, 0.3),
                BorderStrokeStyle.SOLID,
                new CornerRadii(15),
                new BorderWidths(2)
        )));

        // Default mascot display (will be updated when pet changes)
        Label defaultMascot = new Label("🦊");
        defaultMascot.setFont(Font.font(36));

        Label defaultText = new Label("Your Companion");
        defaultText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        defaultText.setTextFill(Color.web(Pastel.FOREST));

        mascotContainer.getChildren().addAll(defaultMascot, defaultText);

        // Logout button
        Button logoutBtn = new Button("🚪 Log Out");
        logoutBtn.setPrefWidth(180);
        logoutBtn.setPrefHeight(45);
        logoutBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        logoutBtn.setTextFill(Color.web(Pastel.LOGOUT_RED));
        logoutBtn.setBackground(new Background(new BackgroundFill(
                Color.web(Pastel.LOGOUT_BG),
                new CornerRadii(12),
                Insets.EMPTY
        )));
        logoutBtn.setBorder(new Border(new BorderStroke(
                Color.web(Pastel.LOGOUT_RED, 0.4),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1.5)
        )));

        logoutBtn.setOnMouseEntered(e -> {
            logoutBtn.setScaleX(1.03);
            logoutBtn.setScaleY(1.03);
            logoutBtn.setEffect(new DropShadow(8, Color.web(Pastel.LOGOUT_RED)));
        });

        logoutBtn.setOnMouseExited(e -> {
            logoutBtn.setScaleX(1.0);
            logoutBtn.setScaleY(1.0);
            logoutBtn.setEffect(null);
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("Logging out...");
            // Add logout logic here
        });

        mascotBox.getChildren().addAll(mascotContainer, logoutBtn);
        this.getChildren().add(mascotBox);
    }
}