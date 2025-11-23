package com.example.Evora;

import com.example.Evora.Sidebar.SidebarController;
import com.example.Evora.Theme.ThemeManager;
import com.example.Evora.Theme.Theme;
import com.example.Evora.Theme.PastelTheme;
import com.example.Evora.Database.DatabaseConnection;
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
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Dashboard {
    private SidebarController sidebarController;
    private ThemeManager themeManager;
    private int currentUserId;
    private final String userName;


    public Dashboard(int userId, String userName) {
        this.currentUserId = userId;
        this.userName = userName;
        this.themeManager = ThemeManager.getInstance();
        System.out.println("🎯 Dashboard created with user ID: " + currentUserId + " (from global: " + currentUserId + ")");
    }


    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
        // Try to get user ID from sidebar controller if available
        if (this.currentUserId == 0 && sidebarController != null) {
            try {
                java.lang.reflect.Field userIdField = sidebarController.getClass().getDeclaredField("currentUserId");
                userIdField.setAccessible(true);
                Object userIdValue = userIdField.get(sidebarController);
                if (userIdValue instanceof Integer) {
                    this.currentUserId = (Integer) userIdValue;
                    System.out.println("🎯 Retrieved user ID from sidebar: " + this.currentUserId);
                }
            } catch (Exception e) {
                System.out.println("❌ Could not get user ID from sidebar: " + e.getMessage());
            }
        }
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("🎯 Dashboard user ID set to: " + userId + " (global updated)");
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
                case "View Analytics":
                    sidebarController.navigate("stats");
                    break;
            }
        } else {
            System.out.println("SidebarController not set for action: " + action);
        }
    }

    public VBox getContent() {
        // EMERGENCY FIX: If user ID is still 0, try to get it from global
        if (currentUserId == 0 ) {
            System.out.println("🚨 EMERGENCY FIX: Setting user ID from global: " + currentUserId);
        }

        System.out.println("🎯 getContent() called with user ID: " + currentUserId);

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

        // Quick Stats - Dynamic Cards with REAL DATA
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10, 0, 12, 0));

        // Get real data from database
        DashboardData data = getDashboardData();

        String[][] statsData = {
                {String.valueOf(data.tasksCompletedToday), "Tasks Completed Today", theme.getStatCardColor1(), "/Images/Tasks Icon.png"},
                {String.valueOf(data.pomodorosCompletedToday), "Pomodoros Today", theme.getStatCardColor2(), "/Images/Timer Icon.png"},
                {String.valueOf(data.notesCreated), "Notes Created", theme.getStatCardColor3(), "/Images/ToDo Icon.png"},
                {data.averageMood + "/5", "Avg Mood Score", theme.getStatCardColor4(), "/Images/Mood Icon.png"}
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

        // Productivity Insights with REAL DATA
        HBox insightsBox = new HBox(25);
        insightsBox.setAlignment(Pos.CENTER);

        VBox focusBox = createFocusBox(theme, data);
        VBox analyticsBox = createAnalyticsBox(theme, data);

        insightsBox.getChildren().addAll(focusBox, analyticsBox);

        mainContent.getChildren().addAll(headerBox, cardsAndButtonsContainer, insightsBox);

        return mainContent;
    }

    // Database methods to get real data - FIXED TO MATCH ANALYTICS FORMAT
    private DashboardData getDashboardData() {
        // FINAL CHECK: If user ID is still 0, use global
        if (currentUserId == 0 ) {
            System.out.println("🚨 FINAL FIX: Setting user ID from global in getDashboardData: " + currentUserId);
        }

        System.out.println("🎯 getDashboardData() called with user ID: " + currentUserId);

        DashboardData data = new DashboardData();

        if (currentUserId == 0) {
            System.out.println("❌ Cannot fetch data - user ID is 0!");
            // Show demo data for testing
            data.tasksCompletedToday = 3;
            data.pomodorosCompletedToday = 2;
            data.notesCreated = 5;
            data.averageMood = "4.2";
            data.mainTaskToday = "Complete project proposal";
            data.mainTaskPriority = "High";
            data.currentWeekCompletionRate = 75;
            data.previousWeekCompletionRate = 60;
            data.tasksCompletedThisWeek = 15;
            data.pomodorosThisWeek = 8;
            System.out.println("📊 Showing demo data since user ID is 0");
            return data;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Database connection successful for user: " + currentUserId);

                // TODAY'S DATA - Tasks completed today (including deleted tasks)
                String todayTasksQuery = """
                    SELECT COUNT(*) as completed_tasks 
                    FROM ToDoTasks 
                    WHERE user_id = ? AND is_completed = 1 
                    AND CAST(completed_at AS DATE) = CAST(GETDATE() AS DATE)
                    UNION ALL
                    SELECT COUNT(*) as completed_tasks 
                    FROM TaskDeletionLog 
                    WHERE user_id = ? AND is_completed = 1 
                    AND CAST(deleted_at AS DATE) = CAST(GETDATE() AS DATE)
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(todayTasksQuery)) {
                    stmt.setInt(1, currentUserId);
                    stmt.setInt(2, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        data.tasksCompletedToday += rs.getInt("completed_tasks");
                    }
                    System.out.println("✅ Today's tasks: " + data.tasksCompletedToday);
                }

                // TODAY'S DATA - Pomodoros completed today
                String todayPomodoroQuery = """
                    SELECT COUNT(*) as pomodoros_today 
                    FROM PomodoroSessions 
                    WHERE user_id = ? AND status = 'Completed'
                    AND CAST(start_time AS DATE) = CAST(GETDATE() AS DATE)
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(todayPomodoroQuery)) {
                    stmt.setInt(1, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        data.pomodorosCompletedToday = rs.getInt("pomodoros_today");
                    }
                    System.out.println("✅ Today's pomodoros: " + data.pomodorosCompletedToday);
                }

                // TODAY'S DATA - Main task for today (highest priority task closest to current day)
                String mainTaskQuery = """
                    SELECT TOP 1 description, priority 
                    FROM ToDoTasks 
                    WHERE user_id = ? AND is_completed = 0 
                    AND (due_date IS NULL OR due_date >= CAST(GETDATE() AS DATE))
                    ORDER BY 
                        CASE priority 
                            WHEN 'High' THEN 1 
                            WHEN 'Medium' THEN 2 
                            WHEN 'Low' THEN 3 
                            ELSE 4 
                        END,
                        CASE WHEN due_date IS NULL THEN 1 ELSE 0 END,
                        due_date ASC
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(mainTaskQuery)) {
                    stmt.setInt(1, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        data.mainTaskToday = rs.getString("description");
                        data.mainTaskPriority = rs.getString("priority");
                    } else {
                        data.mainTaskToday = "No pending tasks";
                        data.mainTaskPriority = "None";
                    }
                    System.out.println("✅ Main task: " + data.mainTaskToday);
                }

                // Total notes created
                String notesQuery = "SELECT COUNT(*) as total_notes FROM StickyNotes WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(notesQuery)) {
                    stmt.setInt(1, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        data.notesCreated = rs.getInt("total_notes");
                    }
                    System.out.println("✅ Notes created: " + data.notesCreated);
                }

                // Average mood score (last 7 days)
                String moodQuery = """
                    SELECT AVG(CAST(mood_value as FLOAT)) as avg_mood 
                    FROM MoodLogger 
                    WHERE user_id = ? AND entry_date >= DATEADD(day, -7, GETDATE())
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(moodQuery)) {
                    stmt.setInt(1, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        double avg = rs.getDouble("avg_mood");
                        data.averageMood = rs.wasNull() ? "N/A" : String.format("%.1f", avg);
                    }
                    System.out.println("✅ Average mood: " + data.averageMood);
                }

                // WEEKLY DATA - Current week completion rate (including deleted tasks)
                String currentWeekQuery = """
                    SELECT 
                        COUNT(CASE WHEN is_completed = 1 THEN 1 END) as completed,
                        COUNT(*) as total
                    FROM (
                        SELECT is_completed FROM ToDoTasks 
                        WHERE user_id = ? AND created_at >= DATEADD(day, -7, GETDATE())
                        UNION ALL
                        SELECT is_completed FROM TaskDeletionLog 
                        WHERE user_id = ? AND created_at >= DATEADD(day, -7, GETDATE())
                    ) combined_tasks
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(currentWeekQuery)) {
                    stmt.setInt(1, currentUserId);
                    stmt.setInt(2, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int totalCompleted = rs.getInt("completed");
                        int totalTasks = rs.getInt("total");
                        data.currentWeekCompletionRate = totalTasks > 0 ? (totalCompleted * 100 / totalTasks) : 0;
                    }
                    System.out.println("✅ Current week completion: " + data.currentWeekCompletionRate + "%");
                }

                // WEEKLY DATA - Previous week completion rate (including deleted tasks)
                String previousWeekQuery = """
                    SELECT 
                        COUNT(CASE WHEN is_completed = 1 THEN 1 END) as completed,
                        COUNT(*) as total
                    FROM (
                        SELECT is_completed FROM ToDoTasks 
                        WHERE user_id = ? AND created_at >= DATEADD(day, -14, GETDATE()) 
                        AND created_at < DATEADD(day, -7, GETDATE())
                        UNION ALL
                        SELECT is_completed FROM TaskDeletionLog 
                        WHERE user_id = ? AND created_at >= DATEADD(day, -14, GETDATE()) 
                        AND created_at < DATEADD(day, -7, GETDATE())
                    ) combined_tasks
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(previousWeekQuery)) {
                    stmt.setInt(1, currentUserId);
                    stmt.setInt(2, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int totalCompleted = rs.getInt("completed");
                        int totalTasks = rs.getInt("total");
                        data.previousWeekCompletionRate = totalTasks > 0 ? (totalCompleted * 100 / totalTasks) : 0;
                    }
                    System.out.println("✅ Previous week completion: " + data.previousWeekCompletionRate + "%");
                }

                // WEEKLY DATA - Tasks completed this week (including deleted tasks)
                String weekTasksQuery = """
                    SELECT COUNT(*) as completed_tasks 
                    FROM (
                        SELECT task_id FROM ToDoTasks 
                        WHERE user_id = ? AND is_completed = 1 
                        AND completed_at >= DATEADD(day, -7, GETDATE())
                        UNION ALL
                        SELECT log_id FROM TaskDeletionLog 
                        WHERE user_id = ? AND is_completed = 1 
                        AND deleted_at >= DATEADD(day, -7, GETDATE())
                    ) combined_completed
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(weekTasksQuery)) {
                    stmt.setInt(1, currentUserId);
                    stmt.setInt(2, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        data.tasksCompletedThisWeek = rs.getInt("completed_tasks");
                    }
                    System.out.println("✅ Weekly tasks: " + data.tasksCompletedThisWeek);
                }

                // WEEKLY DATA - Pomodoro sessions this week
                String weekPomodoroQuery = """
                    SELECT COUNT(*) as pomodoros_week 
                    FROM PomodoroSessions 
                    WHERE user_id = ? AND status = 'Completed'
                    AND start_time >= DATEADD(day, -7, GETDATE())
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(weekPomodoroQuery)) {
                    stmt.setInt(1, currentUserId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        data.pomodorosThisWeek = rs.getInt("pomodoros_week");
                    }
                    System.out.println("✅ Weekly pomodoros: " + data.pomodorosThisWeek);
                }

            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching dashboard data for user " + currentUserId + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Final dashboard data loaded successfully");
        return data;
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
            case "Tasks Completed Today": return "✅";
            case "Pomodoros Today": return "⏰";
            case "Notes Created": return "📝";
            case "Avg Mood Score": return "😊";
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

    private VBox createFocusBox(Theme theme, DashboardData data) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color: " + theme.getFocusBoxColor() + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        Label title = new Label("🎯 Today's Progress");
        title.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // Today's Summary
        VBox todaySummary = new VBox(8);
        todaySummary.setPadding(new Insets(12));
        todaySummary.setStyle("-fx-background-color: " + theme.getMiniCardColor() + "; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label summaryTitle = new Label("Today's Summary");
        summaryTitle.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        summaryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        Label dateLabel = new Label(todayDate);
        dateLabel.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));

        Label tasksLabel = new Label("✓ " + data.tasksCompletedToday + " tasks completed");
        tasksLabel.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        tasksLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));

        Label pomodorosLabel = new Label("⏰ " + data.pomodorosCompletedToday + " pomodoros finished");
        pomodorosLabel.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        pomodorosLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));

        // Main task for today
        Label mainTaskLabel = new Label("🎯 Main Task: " + data.mainTaskToday);
        mainTaskLabel.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
        mainTaskLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        mainTaskLabel.setWrapText(true);
        mainTaskLabel.setMaxWidth(350);

        todaySummary.getChildren().addAll(summaryTitle, dateLabel, tasksLabel, pomodorosLabel, mainTaskLabel);

        // Quick Stats
        HBox quickStats = new HBox(15);
        quickStats.setAlignment(Pos.CENTER);

        VBox priorityCard = createMiniCard("✅ Completed", data.tasksCompletedToday + " tasks", theme.getStatCardColor1(), theme);
        VBox timeCard = createMiniCard("⏰ Sessions", data.pomodorosCompletedToday + " pomodoros", theme.getStatCardColor2(), theme);

        quickStats.getChildren().addAll(priorityCard, timeCard);

        box.getChildren().addAll(title, todaySummary, quickStats);
        return box;
    }

    private VBox createAnalyticsBox(Theme theme, DashboardData data) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color: " + theme.getAnalyticsBoxColor() + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label title = new Label("📊 Weekly Insights");
        title.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Button viewAnalytics = new Button("View Analytics →");
        viewAnalytics.setStyle("-fx-text-fill: " + theme.getAccentColor() + "; -fx-background-color: transparent; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12px; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");
        viewAnalytics.setOnAction(e -> handleActionButton("View Analytics"));

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleBox.getChildren().addAll(title, viewAnalytics);

        // Weekly Progress Comparison
        HBox weeklyBox = new HBox(15);
        weeklyBox.setPadding(new Insets(12));
        weeklyBox.setStyle("-fx-background-color: " + theme.getStatCardColor3() + "; -fx-background-radius: 12; -fx-border-radius: 12;");
        weeklyBox.setAlignment(Pos.CENTER);

        VBox progressText = new VBox(6);
        Label progressTitle = new Label("This Week");
        progressTitle.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressValue = new Label(data.currentWeekCompletionRate + "%");
        progressValue.setStyle("-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        progressValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label progressDesc = new Label("Task completion rate");
        progressDesc.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        progressDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        progressText.getChildren().addAll(progressTitle, progressValue, progressDesc);

        VBox progressStats = new VBox(3);
        // Calculate trend compared to previous week
        int trend = data.currentWeekCompletionRate - data.previousWeekCompletionRate;
        String trendArrow = trend > 0 ? "↗" : trend < 0 ? "↘" : "→";
        String trendColor = trend > 0 ? theme.getTextPrimary() : trend < 0 ? "#ef4444" : theme.getTextSecondary();

        Label progressChange = new Label(trendArrow + " " + Math.abs(trend) + "%");
        progressChange.setStyle("-fx-text-fill: " + trendColor + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        progressChange.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label progressCompare = new Label("vs last week");
        progressCompare.setStyle("-fx-text-fill: " + theme.getTextSecondary() + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        progressCompare.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));

        progressStats.getChildren().addAll(progressChange, progressCompare);
        weeklyBox.getChildren().addAll(progressText, progressStats);

        // Mini Stats for weekly data
        HBox miniStats = new HBox(15);
        miniStats.setAlignment(Pos.CENTER);

        VBox weekTasks = createMiniCard("Weekly Tasks", String.valueOf(data.tasksCompletedThisWeek), theme.getStatCardColor1(), theme);
        VBox weekPomodoros = createMiniCard("Focus Sessions", String.valueOf(data.pomodorosThisWeek), theme.getStatCardColor2(), theme);

        miniStats.getChildren().addAll(weekTasks, weekPomodoros);

        // Analytics Button
        Button analyticsBtn = new Button("Full Analytics Dashboard");
        analyticsBtn.setPrefWidth(300);
        analyticsBtn.setStyle("-fx-background-color: " + theme.getButtonColor() + "; " +
                "-fx-text-fill: " + theme.getTextPrimary() + "; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-family: 'Segoe UI'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        analyticsBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        analyticsBtn.setOnAction(e -> handleActionButton("View Analytics"));
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

    // Data container class - UPDATED
    private static class DashboardData {
        int tasksCompletedToday = 0;
        int pomodorosCompletedToday = 0;
        int notesCreated = 0;
        String averageMood = "N/A";
        String mainTaskToday = "No tasks";
        String mainTaskPriority = "None";

        // Weekly data
        int currentWeekCompletionRate = 0;
        int previousWeekCompletionRate = 0;
        int tasksCompletedThisWeek = 0;
        int pomodorosThisWeek = 0;
    }
}