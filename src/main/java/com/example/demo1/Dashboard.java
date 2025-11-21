package com.example.demo1;

import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.Theme.ThemeManager;
import com.example.demo1.Theme.Theme;
import com.example.demo1.Theme.PastelTheme; // Import PastelTheme to check theme type
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.io.InputStream;

public class Dashboard {
    private SidebarController sidebarController;
    private ThemeManager themeManager;

    public Dashboard() {
        this.themeManager = ThemeManager.getInstance();
    }

    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    private void handleActionButton(String action) {
        if (sidebarController != null) {
            switch (action) {
                case "Add Task":
                    sidebarController.navigate("todos");
                    break;
                case "Start Timer":
                    sidebarController.navigate("timer");
                    break;
                case "Create Note":
                    sidebarController.navigate("notes");
                    break;
                case "Visit Pet":
                    sidebarController.navigate("pet");
                    break;
            }
        } else {
            System.out.println("SidebarController not set for action: " + action);
        }
    }

    public VBox getContent() {
        Theme theme = themeManager.getCurrentTheme();

        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20, 30, 20, 30));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: " + theme.getBackgroundColor() + ";");

        // Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10, 0, 15, 0));

        Label title = new Label("Welcome to Your Dashboard! " + getThemeEmoji(theme));
        title.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Ready to make today productive and fun?");
        subtitle.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));

        headerBox.getChildren().addAll(title, subtitle);

        // Container for cards and buttons
        VBox cardsAndButtonsContainer = new VBox(12);
        cardsAndButtonsContainer.setAlignment(Pos.CENTER);
        cardsAndButtonsContainer.setPadding(new Insets(0, 0, 15, 0));

        // Quick Stats - Dynamic Cards
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10, 0, 12, 0));

        String[][] statsData = {
                {"12", "Tasks Completed", theme.getStatCardColor1(), "/Images/Tasks Icon.png"},
                {"4", "Pomodoros Today", theme.getStatCardColor2(), "/Images/Timer Icon.png"},
                {"8", "Notes Created", theme.getStatCardColor3(), "/Images/ToDo Icon.png"},
                {"😊", "Mood Score", theme.getStatCardColor4(), "/Images/Mood Icon.png"}
        };

        for (String[] stat : statsData) {
            VBox statCard = createStatCard(stat[0], stat[1], stat[2], stat[3], theme);
            statsRow.getChildren().add(statCard);
        }

        // Action Buttons
        HBox actionButtonsRow = new HBox(20);
        actionButtonsRow.setAlignment(Pos.CENTER);
        actionButtonsRow.setPadding(new Insets(5, 0, 0, 0));

        Button addTaskBtn = createSmallActionButton("Add Task", theme.getStatCardColor1(), theme);
        Button startTimerBtn = createSmallActionButton("Start Timer", theme.getStatCardColor2(), theme);
        Button createNoteBtn = createSmallActionButton("Create Note", theme.getStatCardColor3(), theme);
        Button visitPetBtn = createSmallActionButton("Visit Pet", theme.getStatCardColor4(), theme);

        actionButtonsRow.getChildren().addAll(addTaskBtn, startTimerBtn, createNoteBtn, visitPetBtn);

        cardsAndButtonsContainer.getChildren().addAll(statsRow, actionButtonsRow);

        // Productivity Insights
        HBox insightsBox = new HBox(25);
        insightsBox.setAlignment(Pos.CENTER);

        VBox focusBox = createFocusBox(theme);
        VBox analyticsBox = createAnalyticsBox(theme);

        insightsBox.getChildren().addAll(focusBox, analyticsBox);

        mainContent.getChildren().addAll(headerBox, cardsAndButtonsContainer, insightsBox);

        return mainContent;
    }

    private VBox createStatCard(String value, String label, String color, String imagePath, Theme theme) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 15, 15, 15));

        card.setPrefSize(180, 200);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 6);");

        // Image/Icon - Only load images for Pastel theme, use emojis for others
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(150, 120);
        imageContainer.setMaxSize(150, 120);
        imageContainer.setStyle("-fx-background-radius: 12; -fx-border-radius: 12;");

        // Check if current theme is PastelTheme
        if (theme instanceof PastelTheme) {
            // Load images only for Pastel theme
            try {
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream != null) {
                    Image image = new Image(imageStream);

                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(140);
                        imageView.setFitHeight(110);
                        imageView.setPreserveRatio(false);
                        imageView.setSmooth(true);
                        imageView.setStyle("-fx-background-radius: 12; -fx-border-radius: 12;");

                        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(140, 110);
                        clip.setArcWidth(12);
                        clip.setArcHeight(12);
                        imageView.setClip(clip);

                        imageContainer.getChildren().add(imageView);
                    } else {
                        setupFallbackEmoji(imageContainer, label, theme);
                    }
                    imageStream.close();
                } else {
                    setupFallbackEmoji(imageContainer, label, theme);
                }
            } catch (Exception e) {
                System.out.println("❌ Error loading image: " + imagePath + " - " + e.getMessage());
                setupFallbackEmoji(imageContainer, label, theme);
            }
        } else {
            // Use emojis for non-Pastel themes
            setupFallbackEmoji(imageContainer, label, theme);
        }

        // Text content
        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setPadding(new Insets(8, 0, 0, 0));
        textContainer.setMaxWidth(150);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(140);

        textContainer.getChildren().addAll(valueLabel, descLabel);
        card.getChildren().addAll(imageContainer, textContainer);
        addHoverAnimation(card);
        return card;
    }

    private Button createSmallActionButton(String text, String color, Theme theme) {
        Button button = new Button(text);
        button.setPrefSize(140, 40);
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-family: 'Segoe UI'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setOnAction(e -> handleActionButton(text));
        addHoverAnimation(button);
        return button;
    }

    private void setupFallbackEmoji(StackPane container, String label, Theme theme) {
        Label emojiLabel = new Label(getFallbackEmoji(label));
        emojiLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: " + theme.getTextPrimary() + ";");

        StackPane emojiBackground = new StackPane();
        emojiBackground.setPrefSize(140, 110);
        emojiBackground.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 12;");
        emojiBackground.getChildren().add(emojiLabel);

        container.getChildren().add(emojiBackground);
    }

    private String getFallbackEmoji(String label) {
        switch (label) {
            case "Tasks Completed": return "✅";
            case "Pomodoros Today": return "⏰";
            case "Notes Created": return "📝";
            case "Mood Score": return "😊";
            default: return "✨";
        }
    }

    private String getThemeEmoji(Theme theme) {
        if (theme instanceof PastelTheme) {
            return "🌸"; // Pastel theme emoji
        } else {
            return "🌌"; // Galaxy/dark theme emoji
        }
    }

    private VBox createFocusBox(Theme theme) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color: " + theme.getFocusBoxColor() + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        Label title = new Label("🎯 Today's Focus");
        title.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // Main Goal
        VBox mainGoal = new VBox(8);
        mainGoal.setPadding(new Insets(12));
        mainGoal.setStyle("-fx-background-color: " + theme.getMiniCardColor() + "; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label goalTitle = new Label("Main Goal");
        goalTitle.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        goalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label goalDesc = new Label("Complete the quarterly project presentation");
        goalDesc.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        goalDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        goalDesc.setWrapText(true);

        mainGoal.getChildren().addAll(goalTitle, goalDesc);

        // Sub Goals
        HBox subGoals = new HBox(15);
        subGoals.setAlignment(Pos.CENTER);

        VBox priorityCard = createMiniCard("✅ Priority Tasks", "3 high-priority items", theme.getStatCardColor1(), theme);
        VBox timeCard = createMiniCard("⏰ Time Goal", "6 pomodoro sessions", theme.getStatCardColor2(), theme);

        subGoals.getChildren().addAll(priorityCard, timeCard);

        box.getChildren().addAll(title, mainGoal, subGoals);
        return box;
    }

    private VBox createAnalyticsBox(Theme theme) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color: " + theme.getAnalyticsBoxColor() + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label title = new Label("📊 Productivity Insights");
        title.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Button viewAnalytics = new Button("View Analytics →");
        viewAnalytics.setStyle("-fx-text-fill: " + theme.getAccentColor() + "; -fx-background-color: transparent; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12px; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleBox.getChildren().addAll(title, viewAnalytics);

        // Weekly Progress
        HBox weeklyBox = new HBox(15);
        weeklyBox.setPadding(new Insets(12));
        weeklyBox.setStyle("-fx-background-color: " + theme.getStatCardColor3() + "; -fx-background-radius: 12; -fx-border-radius: 12;");
        weeklyBox.setAlignment(Pos.CENTER);

        VBox progressText = new VBox(6);
        Label progressTitle = new Label("This Week");
        progressTitle.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressValue = new Label("90%");
        progressValue.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        progressValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label progressDesc = new Label("Task completion rate");
        progressDesc.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        progressDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        progressText.getChildren().addAll(progressTitle, progressValue, progressDesc);

        VBox progressStats = new VBox(3);
        Label progressChange = new Label("↗ +12%");
        progressChange.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressChange.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressCompare = new Label("vs last week");
        progressCompare.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        progressCompare.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));

        progressStats.getChildren().addAll(progressChange, progressCompare);
        weeklyBox.getChildren().addAll(progressText, progressStats);

        // Mini Stats
        HBox miniStats = new HBox(15);
        miniStats.setAlignment(Pos.CENTER);

        VBox focusSessions = createMiniCard("Focus Sessions", "23", theme.getStatCardColor1(), theme);
        VBox dayStreak = createMiniCard("Day Streak", "5", theme.getStatCardColor2(), theme);

        miniStats.getChildren().addAll(focusSessions, dayStreak);

        // Analytics Button
        Button analyticsBtn = new Button("Full Analytics Dashboard");
        analyticsBtn.setPrefWidth(300);
        analyticsBtn.setStyle("-fx-background-color: " + theme.getButtonColor() + "; " +
                "-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-family: 'Segoe UI'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        analyticsBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addHoverAnimation(analyticsBtn);

        box.getChildren().addAll(titleBox, weeklyBox, miniStats, analyticsBtn);
        return box;
    }

    private VBox createMiniCard(String title, String value, String color, Theme theme) {
        VBox mini = new VBox(5);
        mini.setAlignment(Pos.CENTER);
        mini.setPadding(new Insets(12));

        mini.setPrefWidth(140);
        mini.setMaxWidth(Region.USE_PREF_SIZE);
        mini.setMaxHeight(Region.USE_PREF_SIZE);

        mini.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));

        mini.getChildren().addAll(valueLabel, titleLabel);
        return mini;
    }

    private void addHoverAnimation(Region region) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), region);
        region.setOnMouseEntered(e -> {
            st.setToX(1.05);
            st.setToY(1.05);
            st.playFromStart();
            region.setStyle(region.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6);");
        });
        region.setOnMouseExited(e -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.playFromStart();
            region.setStyle(region.getStyle().replace(" -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6);", ""));
        });
    }
}