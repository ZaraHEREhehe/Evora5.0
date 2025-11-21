package com.example.demo1.Sidebar;

import com.example.demo1.Login.LoginView;
import com.example.demo1.Theme.Pastel;
import com.example.demo1.Theme.Theme;
import com.example.demo1.Theme.ThemeManager;
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
import javafx.stage.Stage;

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
    private Label title;
    private Label userLabel;
    private Region separator;
    private Button logoutBtn;
    private ThemeManager themeManager;

    // In your Sidebar class, add this to the constructor:
    public Sidebar(SidebarController controller, String userName, int userId) {
        this.controller = controller;
        this.themeManager = ThemeManager.getInstance();

        // Add theme change listener
        themeManager.addThemeChangeListener(this::updateTheme);

        setupSidebar();
        createHeader(userName);
       // createMascotSection();
        createNavButtons();
        createMascotSection();
        // Initialize with current experience from database
        refreshExperienceFromDatabase(userId);

        // Apply initial theme
        applyTheme(themeManager.getCurrentTheme());
    }

    // Remove the old updateTheme method and replace with:
    public void updateTheme(Theme newTheme) {
        applyTheme(newTheme);
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
            mascotText.setTextFill(Color.web(Pastel.FOREST));
            mascotText.setAlignment(Pos.CENTER);
            mascotText.setWrapText(true);
            mascotText.setMaxWidth(150);

            // Experience display integrated below pet name
            HBox expBox = new HBox(5);
            expBox.setAlignment(Pos.CENTER);

            Label starIcon = new Label("⭐");
            starIcon.setFont(Font.font(12));

            expBox.getChildren().addAll(starIcon, expLabel);

            contentBox.getChildren().addAll(mascotText, expBox);
            mascotContainer.getChildren().add(contentBox);
        }
    }

    private void applyTheme(Theme theme) {
        // Update background
        this.setBackground(new Background(new BackgroundFill(
                Color.web(theme.getBackgroundColor()),
                new CornerRadii(0),
                Insets.EMPTY
        )));

        // Update border
        this.setStyle("-fx-border-color: " + theme.getPrimaryColor() + "; -fx-border-width: 0 2 0 0;");

        // Update header
        if (title != null) {
            title.setTextFill(Color.web(theme.getTextPrimary()));
            title.setStyle("-fx-effect: dropshadow(gaussian, " + theme.getPrimaryColor() + ", 10, 0.3, 0, 2);");
        }

        if (userLabel != null) {
            userLabel.setTextFill(Color.web(theme.getTextSecondary()));
        }

        if (separator != null) {
            separator.setBackground(new Background(new BackgroundFill(
                    Color.web(theme.getPrimaryColor()),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));
        }

        // Update nav buttons
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            updateButtonTheme(entry.getValue(), entry.getKey(), theme);
        }

        // Update experience section
        if (experienceContainer != null) {
            experienceContainer.setBackground(new Background(new BackgroundFill(
                    Color.web(theme.getCardColor()),
                    new CornerRadii(12),
                    Insets.EMPTY
            )));
            experienceContainer.setBorder(new Border(new BorderStroke(
                    Color.web(theme.getWarningColor(), 0.3),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(1.5)
            )));

            if (expLabel != null) {
                expLabel.setTextFill(Color.web(theme.getTextPrimary()));
            }

            // Update experience container effect
            experienceContainer.setEffect(new DropShadow(5, Color.web(theme.getWarningColor(), 0.2)));
        }

        // Update mascot section
        if (mascotContainer != null) {
            mascotContainer.setBackground(new Background(new BackgroundFill(
                    Color.web(theme.getCardColor()),
                    new CornerRadii(15),
                    Insets.EMPTY
            )));
            mascotContainer.setBorder(new Border(new BorderStroke(
                    Color.web(theme.getPrimaryColor(), 0.3),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(15),
                    new BorderWidths(2)
            )));

            // Update mascot text color if it exists
            mascotContainer.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .map(node -> (Label) node)
                    .forEach(label -> {
                        if (!label.getText().matches(".*[🐱🐰🦉🐉🦊⭐✨].*")) { // Not emoji labels
                            label.setTextFill(Color.web(theme.getTextPrimary()));
                        }
                    });
        }

        // Update logout button
        if (logoutBtn != null) {
            logoutBtn.setTextFill(Color.web(theme.getErrorColor()));
            logoutBtn.setBackground(new Background(new BackgroundFill(
                    Color.web(theme.getCardColor()),
                    new CornerRadii(12),
                    Insets.EMPTY
            )));
            logoutBtn.setBorder(new Border(new BorderStroke(
                    Color.web(theme.getErrorColor(), 0.4),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(1.5)
            )));
        }
    }

    private void updateButtonTheme(Button btn, String buttonId, Theme theme) {
        String color = getButtonColor(buttonId, theme);
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
        btn.setTextFill(Color.web(theme.getTextPrimary()));

        // Update hover effects
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.02);
            btn.setScaleY(1.02);
            btn.setEffect(new DropShadow(10, Color.web(color).darker()));
        });
    }

    private String getButtonColor(String buttonId, Theme theme) {
        // Simple color mapping that works with all themes
        switch (buttonId) {
            case "dashboard":
                return theme.getPrimaryColor();        // PINK
            case "todos":
                return theme.getSecondaryColor();         // LAVENDER
            case "timer": return theme.getAccentColor();            // PURPLE
            case "notes": return theme.getStatCardColor3();         // BLUE (better than success green)
            case "pet": return theme.getStatCardColor4();           // LILAC
            case "stats": return theme.getAccentColor();             // LIGHT_PURPLE (better than warning orange)
            case "calendar": return theme.getStatCardColor2();      // LAVENDER
            case "mood": return theme.getStatCardColor1();          // PINK
            case "whitenoise": return theme.getStatCardColor2();    // LAVENDER
            case "settings": return theme.getAccentColor();      // BLUE
            default: return theme.getPrimaryColor();
        }
    }

    // Simple method to update experience - just call this whenever exp changes
    public void updateExperience(int newExp) {
        if (expLabel != null) {
            expLabel.setText(newExp + " XP");

            // Cute little animation
            expLabel.setScaleX(1.1);
            expLabel.setScaleY(1.1);
            expLabel.setTextFill(Color.web(Pastel.GOLD));

            javafx.animation.ScaleTransition scaleTransition =
                    new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), expLabel);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
            pause.setOnFinished(e -> {
                expLabel.setTextFill(Color.web(Pastel.SAGE));
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
        this.setEffect(new DropShadow(15, 5, 5, Color.gray(0, 0.1)));
    }

    private void createHeader(String userName) {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 25, 0));

        title = new Label("Évora 🌸");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));

        userLabel = new Label("Hello, " + userName + "! 💫");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        userLabel.setWrapText(true);
        userLabel.setAlignment(Pos.CENTER);

        separator = new Region();
        separator.setPrefHeight(2);
        separator.setMaxWidth(180);

        header.getChildren().addAll(title, userLabel, separator);
        this.getChildren().add(header);
    }

    private void createNavButtons() {
        VBox navBox = new VBox(12);
        navBox.setAlignment(Pos.TOP_CENTER);
        navBox.setFillWidth(true);

        String[][] items = {
                {"dashboard", "🏠 Dashboard", ""},
                {"todos", "📝 To-Do List", ""},
                {"timer", "⏰ Pomodoro Timer", ""},
                {"notes", "📒 Notes", ""},
                {"pet", "🐾 Virtual Pet", ""},
                {"stats", "📊 Analytics", ""},
                {"calendar", "📅 Calendar", ""},
                {"mood", "😊 Mood Tracker", ""},
                {"whitenoise", "🎵 White Noise", ""},
                {"settings", "⚙️ Settings", ""}
        };

        for (String[] item : items) {
            Button btn = createNavButton(item[0], item[1]);
            navButtons.put(item[0], btn);
            navBox.getChildren().add(btn);
        }

        this.getChildren().add(navBox);
    }

    private Button createNavButton(String id, String label) {
        Button btn = new Button(label);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

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
        Theme currentTheme = themeManager.getCurrentTheme();

        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            Button btn = entry.getValue();
            String originalColor = getButtonColor(entry.getKey(), currentTheme);

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
                btn.setTextFill(Color.web(currentTheme.getTextPrimary()));
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

        // Temporary experience display until mascot is updated
        HBox tempExpBox = new HBox(5);
        tempExpBox.setAlignment(Pos.CENTER);
        Label tempStar = new Label("⭐");
        tempStar.setFont(Font.font(12));
        expLabel = new Label("Loading...");
        expLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        expLabel.setTextFill(Color.web(Pastel.SAGE));
        tempExpBox.getChildren().addAll(tempStar, expLabel);

        mascotContainer.getChildren().addAll(defaultMascot, defaultText, tempExpBox);

        // Logout button
        logoutBtn = new Button("🚪 Log Out");
        logoutBtn.setPrefWidth(180);
        logoutBtn.setPrefHeight(45);
        logoutBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        logoutBtn.setOnMouseEntered(e -> {
            logoutBtn.setScaleX(1.03);
            logoutBtn.setScaleY(1.03);
            Theme currentTheme = themeManager.getCurrentTheme();
            logoutBtn.setEffect(new DropShadow(8, Color.web(currentTheme.getErrorColor())));
        });

        logoutBtn.setOnMouseExited(e -> {
            logoutBtn.setScaleX(1.0);
            logoutBtn.setScaleY(1.0);
            logoutBtn.setEffect(null);
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("Logging out...");

            // ADD THIS CODE TO NAVIGATE TO LOGIN PAGE
            try {
                // Get the current stage from any node in the scene
                Stage stage = (Stage) logoutBtn.getScene().getWindow();

                // Use your existing LoginView to show the login screen
                LoginView loginView = new LoginView();
                loginView.start(stage);

                System.out.println("✅ Successfully logged out and returned to login screen");

            } catch (Exception ex) {
                System.err.println("❌ Error during logout: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        mascotBox.getChildren().addAll(mascotContainer, logoutBtn);
        this.getChildren().add(mascotBox);
    }
}