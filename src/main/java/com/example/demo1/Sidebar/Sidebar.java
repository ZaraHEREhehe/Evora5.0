package com.example.demo1.Sidebar;

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
    private Label expLabel;

    // Pastel color palette matching the dashboard
    private final String PASTEL_BLUSH = "#FCEDF5";
    private final String PASTEL_PINK = "#FACEEA";
    private final String PASTEL_LAVENDER = "#D7D8FF";
    private final String PASTEL_BLUE = "#C1DAFF";
    private final String PASTEL_PURPLE = "#F0D2F7";
    private final String PASTEL_LILAC = "#E2D6FF";
    private final String PASTEL_ROSE = "#F3D1F3";
    private final String PASTEL_SKY = "#E4EFFF";
    private final String PASTEL_MINT = "#C8E6C9";
    private final String PASTEL_PEACH = "#FFDAB9";
    private final String PASTEL_CORAL = "#FFCCBC";
    private final String PASTEL_LEMON = "#FFF9C4";
    private final String PASTEL_IVORY = "#FDF5E7";
    private final String PASTEL_SAGE = "#8D9383";
    private final String PASTEL_FOREST = "#343A26";
    private final String PASTEL_GOLD = "#FFD700";

    public Sidebar(SidebarController controller, String userName, int userId) {
        this.controller = controller;
        setupSidebar();
        createHeader(userName);
        createNavButtons();
        createMascotSection();
        // Initialize with current experience from database
        refreshExperienceFromDatabase(userId);
    }

    // Add this method to update the mascot display with integrated experience
    public void updateMascot(String petName, String species, String gifFilename) {
        if (mascotContainer != null) {
            mascotContainer.getChildren().clear();

            // Create main content container
            VBox contentBox = new VBox(8);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setPadding(new Insets(10));

            try {
                // Try to load the pet GIF
                ImageView petImage = new ImageView(new Image(getPetGifPath(gifFilename), 60, 60, true, true));
                contentBox.getChildren().add(petImage);
            } catch (Exception e) {
                // Fallback to species emoji
                Label emojiLabel = new Label(getSpeciesEmoji(species));
                emojiLabel.setFont(Font.font(36));
                contentBox.getChildren().add(emojiLabel);
            }

            // Pet name only (no species)
            Label mascotText = new Label(petName);
            mascotText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            mascotText.setTextFill(Color.web(PASTEL_FOREST));
            mascotText.setAlignment(Pos.CENTER);
            mascotText.setWrapText(true);
            mascotText.setMaxWidth(150);

            // Experience display integrated below pet name
            HBox expBox = new HBox(5);
            expBox.setAlignment(Pos.CENTER);

            Label starIcon = new Label("⭐");
            starIcon.setFont(Font.font(12));

            expLabel = new Label("Loading...");
            expLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
            expLabel.setTextFill(Color.web(PASTEL_SAGE));

            expBox.getChildren().addAll(starIcon, expLabel);

            contentBox.getChildren().addAll(mascotText, expBox);
            mascotContainer.getChildren().add(contentBox);
        }
    }

    // Simple method to update experience
    public void updateExperience(int newExp) {
        if (expLabel != null) {
            expLabel.setText(newExp + " XP");

            // Cute little animation
            expLabel.setScaleX(1.1);
            expLabel.setScaleY(1.1);
            expLabel.setTextFill(Color.web(PASTEL_GOLD));

            javafx.animation.ScaleTransition scaleTransition =
                    new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), expLabel);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
            pause.setOnFinished(e -> {
                expLabel.setTextFill(Color.web(PASTEL_SAGE));
            });
            pause.play();
        }
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
                Color.web(PASTEL_BLUSH),
                new CornerRadii(0),
                Insets.EMPTY
        );
        this.setBackground(new Background(backgroundFill));

        this.setEffect(new DropShadow(15, 5, 5, Color.gray(0, 0.1)));
        this.setStyle("-fx-border-color: " + PASTEL_PINK + "; -fx-border-width: 0 2 0 0;");
    }

    private void createHeader(String userName) {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 25, 0));

        Label title = new Label("Évora 🌸");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(PASTEL_FOREST));
        title.setStyle("-fx-effect: dropshadow(gaussian, " + PASTEL_PINK + ", 10, 0.3, 0, 2);");

        Label userLabel = new Label("Hello, " + userName + "! 💫");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        userLabel.setTextFill(Color.web(PASTEL_SAGE));
        userLabel.setWrapText(true);
        userLabel.setAlignment(Pos.CENTER);

        Region separator = new Region();
        separator.setPrefHeight(2);
        separator.setBackground(new Background(new BackgroundFill(
                Color.web(PASTEL_PINK),
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
                {"dashboard", "🏠 Dashboard", PASTEL_PINK},
                {"todos", "📝 To-Do List", PASTEL_LAVENDER},
                {"timer", "⏰ Pomodoro Timer", PASTEL_BLUE},
                {"notes", "📒 Notes", PASTEL_PURPLE},
                {"pet", "🐾 Virtual Pet", PASTEL_LILAC},
                {"stats", "📊 Analytics", PASTEL_ROSE},
                {"calendar", "📅 Calendar", PASTEL_SKY},
                {"mood", "😊 Mood Tracker", PASTEL_PEACH},
                {"whitenoise", "🎵 White Noise", PASTEL_CORAL},
                {"settings", "⚙️ Settings", PASTEL_LEMON}
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
        btn.setTextFill(Color.web(PASTEL_FOREST));

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
                btn.setTextFill(Color.web(PASTEL_FOREST));
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
            case "dashboard": return PASTEL_PINK;
            case "todos": return PASTEL_LAVENDER;
            case "timer": return PASTEL_BLUE;
            case "notes": return PASTEL_PURPLE;
            case "pet": return PASTEL_LILAC;
            case "stats": return PASTEL_ROSE;
            case "calendar": return PASTEL_SKY;
            case "mood": return PASTEL_MINT;
            case "whitenoise": return PASTEL_PEACH;
            case "settings": return PASTEL_LEMON;
            default: return PASTEL_PINK;
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
                Color.web(PASTEL_IVORY),
                new CornerRadii(15),
                Insets.EMPTY
        )));
        mascotContainer.setBorder(new Border(new BorderStroke(
                Color.web(PASTEL_PINK, 0.3),
                BorderStrokeStyle.SOLID,
                new CornerRadii(15),
                new BorderWidths(2)
        )));

        // Default mascot display (will be updated when pet changes)
        Label defaultMascot = new Label("🦊");
        defaultMascot.setFont(Font.font(36));

        Label defaultText = new Label("Your Companion");
        defaultText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        defaultText.setTextFill(Color.web(PASTEL_FOREST));

        // Temporary experience display until mascot is updated
        HBox tempExpBox = new HBox(5);
        tempExpBox.setAlignment(Pos.CENTER);
        Label tempStar = new Label("⭐");
        tempStar.setFont(Font.font(12));
        expLabel = new Label("Loading...");
        expLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        expLabel.setTextFill(Color.web(PASTEL_SAGE));
        tempExpBox.getChildren().addAll(tempStar, expLabel);

        mascotContainer.getChildren().addAll(defaultMascot, defaultText, tempExpBox);

        // Logout button
        Button logoutBtn = new Button("🚪 Log Out");
        logoutBtn.setPrefWidth(180);
        logoutBtn.setPrefHeight(45);
        logoutBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        logoutBtn.setTextFill(Color.web("#E57373"));
        logoutBtn.setBackground(new Background(new BackgroundFill(
                Color.web("#FFEBEE"),
                new CornerRadii(12),
                Insets.EMPTY
        )));
        logoutBtn.setBorder(new Border(new BorderStroke(
                Color.web("#E57373", 0.4),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1.5)
        )));

        logoutBtn.setOnMouseEntered(e -> {
            logoutBtn.setScaleX(1.03);
            logoutBtn.setScaleY(1.03);
            logoutBtn.setEffect(new DropShadow(8, Color.web("#E57373")));
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