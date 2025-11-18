package com.example.demo1;
import com.example.demo1.Calendar.CalendarView;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.ToDoList.TodoView;
import com.example.demo1.Whitenoise.WhiteNoisePlayer;
import com.example.demo1.Theme.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.InputStream;

public class Dashboard {

    private final Stage stage;
    private BorderPane root;
    private SidebarController sidebarController;

    // Pastel color palette - single colors
    private final String PASTEL_PINK = "#FACEEA";
    private final String PASTEL_BLUE = "#C1DAFF";
    private final String PASTEL_LAVENDER = "#D7D8FF";
    private final String PASTEL_PURPLE = "#F0D2F7";
    private final String PASTEL_LILAC = "#E2D6FF";
    private final String PASTEL_ROSE = "#F3D1F3";
    private final String PASTEL_BLUSH = "#FCEDF5";
    private final String PASTEL_SKY = "#E4EFFF";
    private final String PASTEL_MIST = "#F7EFFF";
    private final String PASTEL_IVORY = "#FDF5E7";
    private final String PASTEL_DUSTY_PINK = "#F1DBD0";
    private final String PASTEL_MAUVE = "#D3A29D";
    private final String PASTEL_SAGE = "#8D9383";
    private final String PASTEL_FOREST = "#343A26";

    public Dashboard(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        root = new BorderPane();

        // Sidebar
        sidebarController = new SidebarController();
        sidebarController.setOnTabChange(this::handleNavigation);
        Sidebar sidebar = new Sidebar(sidebarController, "Zara");
        root.setLeft(sidebar);

        showDashboardContent();

        // Dynamic layout with minimum size
        Scene scene = new Scene(root, 1200, 800); // Default size
        scene.getRoot().setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

        // Apply theme
        ThemeManager.applyTheme(scene, ThemeManager.Theme.PASTEL);

        stage.setScene(scene);
        stage.setTitle("Pastel Productivity Dashboard");

        // Set minimum size and allow resizing
        stage.setMinWidth(1300);
        stage.setMinHeight(600);
        stage.setResizable(true); // Allow users to resize the window

        stage.show();
    }

    private void handleNavigation(String tab) {
        switch (tab) {
            case "dashboard":
                showDashboardContent();
                break;
            case "timer":
                showPomodoroTimer();
                break;
            case "todos":
                showTodoList();
                break;
            case "calendar":
                showCalendar();
                break;
            case "notes":
                System.out.println("Navigating to Notes");
                break;
            case "pet":
                System.out.println("Navigating to Pet");
                break;
            case "stats":
                System.out.println("Navigating to Analytics");
                break;
            case "whitenoise":
                showWhiteNoisePlayer();
                break;
            case "settings":
                showSettings();
                break;
            default:
                System.out.println("Navigating to: " + tab);
        }
    }

    private void showTodoList() {
        try {
            TodoView todoView = new TodoView();
            ScrollPane todoContent = todoView.getContent();

            // Make todo content fill available space
            todoContent.prefWidthProperty().bind(root.widthProperty().subtract(200)); // sidebar width
            todoContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(todoContent);

        } catch (Exception e) {
            System.out.println("❌ Error loading Todo List: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("To-Do List 📝");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading to-do list.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }
    private void showCalendar() {
        try {
            CalendarView calendarView = new CalendarView();

            // Set the initial width for responsive calculations
            calendarView.setWidth(root.getWidth() - 200); // Account for sidebar

            // Update calendar width when root container resizes
            root.widthProperty().addListener((obs, oldVal, newVal) -> {
                calendarView.setWidth(newVal.doubleValue() - 200); // Account for sidebar
            });

            // Set up callback - when calendar content changes, refresh the center
            calendarView.setOnContentChange(() -> {
                // This will be called when dates are clicked or month changes
                ScrollPane refreshedContent = calendarView.getContent();

                // Make refreshed content fill available space
                refreshedContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
                refreshedContent.prefHeightProperty().bind(root.heightProperty());

                root.setCenter(refreshedContent);

                // Update the width for the refreshed content
                calendarView.setWidth(root.getWidth() - 200);
            });

            ScrollPane calendarContent = calendarView.getContent();

            // Make calendar content fill available space
            calendarContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            calendarContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(calendarContent);

        } catch (Exception e) {
            System.out.println("❌ Error loading Calendar: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("Calendar 📅");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading calendar.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }
    private void showSettings() {
        try {
            Settings settings = new Settings();
            VBox settingsContent = settings.getContent();

            ScrollPane scrollPane = new ScrollPane(settingsContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");

            // Make settings content responsive
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(scrollPane);

        } catch (Exception e) {
            System.out.println("❌ Error loading Settings: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("Settings ⚙️");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading settings page.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showWhiteNoisePlayer() {
        try {
            WhiteNoisePlayer whiteNoisePlayer = new WhiteNoisePlayer();
            VBox whiteNoiseContent = whiteNoisePlayer.getContent();

            // Apply your dashboard theme to match the rest of the app
            whiteNoiseContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Create a scroll pane for the content (since white noise player is tall)
            ScrollPane scrollPane = new ScrollPane(whiteNoiseContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");
            scrollPane.setPadding(new Insets(20));

            // Make white noise content responsive
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(scrollPane);

        } catch (Exception e) {
            System.out.println("❌ Error loading White Noise Player: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("White Noise Player 🎵");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Label subtitle = new Label("Error loading white noise player. Please check the console for details.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showDashboardContent() {
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20, 30, 20, 30));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

        // Make main content responsive
        mainContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
        mainContent.prefHeightProperty().bind(root.heightProperty());

        // Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10, 0, 15, 0));

        Label title = new Label("Welcome to Your Dashboard! 🌸");
        title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Ready to make today productive and fun?");
        subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
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
                {"12", "Tasks Completed", PASTEL_PINK, "/com/example/demo1/Images/Tasks Icon.png"},
                {"4", "Pomodoros Today", PASTEL_LAVENDER, "/com/example/demo1/Images/Timer Icon.png"},
                {"8", "Notes Created", PASTEL_BLUE, "/com/example/demo1/Images/ToDo Icon.png"},
                {"😊", "Mood Score", PASTEL_LILAC, "/com/example/demo1/Images/Mood Icon.png"}
        };

        for (String[] stat : statsData) {
            VBox statCard = createStatCard(stat[0], stat[1], stat[2], stat[3]);
            statsRow.getChildren().add(statCard);
        }

        // Action Buttons
        HBox actionButtonsRow = new HBox(20);
        actionButtonsRow.setAlignment(Pos.CENTER);
        actionButtonsRow.setPadding(new Insets(5, 0, 0, 0));

        Button addTaskBtn = createSmallActionButton("Add Task", PASTEL_PINK);
        Button startTimerBtn = createSmallActionButton("Start Timer", PASTEL_LAVENDER);
        Button createNoteBtn = createSmallActionButton("Create Note", PASTEL_BLUE);
        Button visitPetBtn = createSmallActionButton("Visit Pet", PASTEL_LILAC);

        actionButtonsRow.getChildren().addAll(addTaskBtn, startTimerBtn, createNoteBtn, visitPetBtn);

        cardsAndButtonsContainer.getChildren().addAll(statsRow, actionButtonsRow);

        // Productivity Insights
        HBox insightsBox = new HBox(25);
        insightsBox.setAlignment(Pos.CENTER);

        // Make insights box responsive
        insightsBox.prefWidthProperty().bind(root.widthProperty().subtract(250));
        insightsBox.setMaxWidth(Region.USE_PREF_SIZE);

        VBox focusBox = createFocusBox();
        VBox analyticsBox = createAnalyticsBox();

        insightsBox.getChildren().addAll(focusBox, analyticsBox);

        mainContent.getChildren().addAll(headerBox, cardsAndButtonsContainer, insightsBox);
        root.setCenter(mainContent);
    }

    private VBox createStatCard(String value, String label, String color, String imagePath) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 15, 15, 15));

        // Use preferred size with proper max size constraints
        card.setPrefSize(180, 200);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 6);");

        // Image/Icon
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(150, 120);
        imageContainer.setMaxSize(150, 120);
        imageContainer.setStyle("-fx-background-radius: 12; -fx-border-radius: 12;");

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
                    setupFallbackEmoji(imageContainer, label);
                }
                imageStream.close();
            } else {
                setupFallbackEmoji(imageContainer, label);
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading image: " + imagePath + " - " + e.getMessage());
            setupFallbackEmoji(imageContainer, label);
        }

        // Text content
        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setPadding(new Insets(8, 0, 0, 0));
        textContainer.setMaxWidth(150);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(140);

        textContainer.getChildren().addAll(valueLabel, descLabel);
        card.getChildren().addAll(imageContainer, textContainer);
        addHoverAnimation(card);
        return card;
    }

    private Button createSmallActionButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(140, 40);
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-family: 'Segoe UI'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setOnAction(e -> handleActionButton(text));
        addHoverAnimation(button);
        return button;
    }

    private void setupFallbackEmoji(StackPane container, String label) {
        Label emojiLabel = new Label(getFallbackEmoji(label));
        emojiLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: " + PASTEL_FOREST + ";");

        StackPane emojiBackground = new StackPane();
        emojiBackground.setPrefSize(140, 110);
        emojiBackground.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-background-radius: 12;");
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

    private void handleActionButton(String action) {
        switch (action) {
            case "Add Task": sidebarController.navigate("todos"); break;
            case "Start Timer": sidebarController.navigate("timer"); break;
            case "Create Note": sidebarController.navigate("notes"); break;
            case "Visit Pet": sidebarController.navigate("pet"); break;
        }
    }

    private VBox createFocusBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        // Use preferred width with proper max size constraints
        box.setPrefWidth(400);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        box.setStyle("-fx-background-color: " + PASTEL_IVORY + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        Label title = new Label("🎯 Today's Focus");
        title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // Main Goal
        VBox mainGoal = new VBox(8);
        mainGoal.setPadding(new Insets(12));
        mainGoal.setStyle("-fx-background-color: " + PASTEL_DUSTY_PINK + "; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label goalTitle = new Label("Main Goal");
        goalTitle.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        goalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label goalDesc = new Label("Complete the quarterly project presentation");
        goalDesc.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        goalDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        goalDesc.setWrapText(true);

        mainGoal.getChildren().addAll(goalTitle, goalDesc);

        // Sub Goals
        HBox subGoals = new HBox(15);
        subGoals.setAlignment(Pos.CENTER);

        VBox priorityCard = createMiniCard("✅ Priority Tasks", "3 high-priority items", PASTEL_PINK);
        VBox timeCard = createMiniCard("⏰ Time Goal", "6 pomodoro sessions", PASTEL_LAVENDER);

        subGoals.getChildren().addAll(priorityCard, timeCard);

        box.getChildren().addAll(title, mainGoal, subGoals);
        return box;
    }

    private VBox createAnalyticsBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        // Use preferred width with proper max size constraints
        box.setPrefWidth(400);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        box.setStyle("-fx-background-color: " + PASTEL_IVORY + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label title = new Label("📊 Productivity Insights");
        title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Button viewAnalytics = new Button("View Analytics →");
        viewAnalytics.setStyle("-fx-text-fill: " + PASTEL_PURPLE + "; -fx-background-color: transparent; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12px; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");
        viewAnalytics.setOnAction(e -> sidebarController.navigate("stats"));

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleBox.getChildren().addAll(title, viewAnalytics);

        // Weekly Progress
        HBox weeklyBox = new HBox(15);
        weeklyBox.setPadding(new Insets(12));
        weeklyBox.setStyle("-fx-background-color: " + PASTEL_ROSE + "; -fx-background-radius: 12; -fx-border-radius: 12;");
        weeklyBox.setAlignment(Pos.CENTER);

        VBox progressText = new VBox(6);
        Label progressTitle = new Label("This Week");
        progressTitle.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressValue = new Label("90%");
        progressValue.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        progressValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label progressDesc = new Label("Task completion rate");
        progressDesc.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        progressDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        progressText.getChildren().addAll(progressTitle, progressValue, progressDesc);

        VBox progressStats = new VBox(3);
        Label progressChange = new Label("↗ +12%");
        progressChange.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressChange.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressCompare = new Label("vs last week");
        progressCompare.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        progressCompare.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));

        progressStats.getChildren().addAll(progressChange, progressCompare);
        weeklyBox.getChildren().addAll(progressText, progressStats);

        // Mini Stats
        HBox miniStats = new HBox(15);
        miniStats.setAlignment(Pos.CENTER);

        VBox focusSessions = createMiniCard("Focus Sessions", "23", PASTEL_BLUE);
        VBox dayStreak = createMiniCard("Day Streak", "5", PASTEL_PURPLE);

        miniStats.getChildren().addAll(focusSessions, dayStreak);

        // Analytics Button
        Button analyticsBtn = new Button("Full Analytics Dashboard");
        analyticsBtn.setPrefWidth(300);
        analyticsBtn.setStyle("-fx-background-color: " + PASTEL_LAVENDER + "; " +
                "-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-family: 'Segoe UI'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        analyticsBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        analyticsBtn.setOnAction(e -> sidebarController.navigate("stats"));
        addHoverAnimation(analyticsBtn);

        box.getChildren().addAll(titleBox, weeklyBox, miniStats, analyticsBtn);
        return box;
    }

    private VBox createMiniCard(String title, String value, String color) {
        VBox mini = new VBox(5);
        mini.setAlignment(Pos.CENTER);
        mini.setPadding(new Insets(12));

        // Use preferred width with proper max size constraints
        mini.setPrefWidth(140);
        mini.setMaxWidth(Region.USE_PREF_SIZE);
        mini.setMaxHeight(Region.USE_PREF_SIZE);

        mini.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));

        mini.getChildren().addAll(valueLabel, titleLabel);
        return mini;
    }

    private void showPomodoroTimer() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo1/Pomodoro/Pomodoro.fxml"));
            Pane pomodoroContent = fxmlLoader.load();

            // Apply the pastel theme to the loaded content
            pomodoroContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make pomodoro content responsive
            pomodoroContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            pomodoroContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(pomodoroContent);
        } catch (Exception e) {
            System.out.println("❌ Error loading Pomodoro FXML: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label timerTitle = new Label("Pomodoro Timer 🍅");
            timerTitle.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Label timerSubtitle = new Label("Error loading timer. Please check the FXML file.");
            timerSubtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

            fallbackContent.getChildren().addAll(timerTitle, timerSubtitle);
            root.setCenter(fallbackContent);
        }
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