package com.example.demo1.Analytics;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.Map;

public class AnalyticsView {
    private final AnalyticsController controller;
    private VBox mainContent;
    private TabPane tabPane;
    private Canvas taskChartCanvas;
    private Canvas focusChartCanvas;
    private Canvas activityChartCanvas;
    private Canvas moodTrendCanvas;
    private Tooltip chartTooltip;
    private ToggleGroup timeGroup;
    private ToggleButton weekButton, monthButton, yearButton;

    // Color theme mapping for easy customization
    private final Map<String, String> colors = Map.ofEntries(
            // Background colors
            Map.entry("bg_primary", "#fdf7ff"),
            Map.entry("bg_card", "#FFFFFF"),
            Map.entry("bg_nav", "#E6F2FF"),

            // Text colors
            Map.entry("text_primary", "#5c5470"),
            Map.entry("text_secondary", "#756f86"),
            Map.entry("text_dark", "#2A2D3A"),

            // Border and accent colors
            Map.entry("border_primary", "#D8B4FE"),
            Map.entry("accent_purple", "#A78BFA"),
            Map.entry("accent_pink", "#F472B6"),
            Map.entry("accent_green", "#34D399"),
            Map.entry("accent_blue", "#60A5FA"),
            Map.entry("accent_yellow", "#FBBF24"),
            Map.entry("accent_orange", "#FB923C"),

            // Gradient colors for cards
            Map.entry("gradient_task_start", "#F3E8FF"),
            Map.entry("gradient_task_end", "#FCE7F3"),
            Map.entry("gradient_focus_start", "#E0F2FE"),
            Map.entry("gradient_focus_end", "#BAE6FD"),
            Map.entry("gradient_streak_start", "#F0E8FF"),
            Map.entry("gradient_streak_end", "#E8D6FF"),
            Map.entry("gradient_productivity_start", "#E8F5E8"),
            Map.entry("gradient_productivity_end", "#D4F0D4"),
            Map.entry("gradient_mood_start", "#F3E8FF"),
            Map.entry("gradient_mood_end", "#FCE7F3")
    );

    // Chart colors array
    private final Color[] chartColors = {
            Color.web(colors.get("accent_purple")),
            Color.web(colors.get("accent_pink")),
            Color.web(colors.get("accent_green")),
            Color.web(colors.get("accent_blue")),
            Color.web(colors.get("accent_yellow")),
            Color.web(colors.get("accent_orange"))
    };

    public AnalyticsView(int userId, String userName) {
        this.controller = new AnalyticsController(userId, userName);
        this.chartTooltip = new Tooltip();
        this.chartTooltip.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.95); -fx-text-fill: " + colors.get("text_primary") + "; -fx-border-color: " + colors.get("border_primary") + "; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    public Node create() {
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: " + colors.get("bg_primary") + ";");

        // Header
        VBox header = createHeader();

        // Create time range selector FIRST (this initializes the buttons)
        HBox timeRangeSelector = createTimeRangeSelector();

        // Then create tab pane
        tabPane = createTabPane();

        // Custom Navigation Bar
        HBox navBar = createCustomNavBar();

        // Listen for tab changes to show/hide time range
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                boolean isAchievementsTab = "Achievements".equals(newTab.getText());
                timeRangeSelector.setVisible(!isAchievementsTab);
                timeRangeSelector.setManaged(!isAchievementsTab);
            }
        });

        mainContent.getChildren().addAll(header, navBar, timeRangeSelector, tabPane);

        return mainContent;
    }

    private HBox createCustomNavBar() {
        HBox navBar = new HBox();
        navBar.setStyle("-fx-background-color: " + colors.get("bg_nav") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");
        navBar.setPadding(new Insets(8));
        navBar.setMaxWidth(Double.MAX_VALUE);

        // Create navigation buttons
        ToggleButton overviewBtn = createNavButton("Overview", true);
        ToggleButton tasksBtn = createNavButton("Tasks & Focus", false);
        ToggleButton wellbeingBtn = createNavButton("Wellbeing", false);
        ToggleButton achievementsBtn = createNavButton("Achievements", false);

        // Create toggle group for navigation
        ToggleGroup navGroup = new ToggleGroup();
        overviewBtn.setToggleGroup(navGroup);
        tasksBtn.setToggleGroup(navGroup);
        wellbeingBtn.setToggleGroup(navGroup);
        achievementsBtn.setToggleGroup(navGroup);

        // Set button actions
        overviewBtn.setOnAction(e -> switchToTab("Overview"));
        tasksBtn.setOnAction(e -> switchToTab("Tasks & Focus"));
        wellbeingBtn.setOnAction(e -> switchToTab("Wellbeing"));
        achievementsBtn.setOnAction(e -> switchToTab("Achievements"));

        // Distribute buttons evenly
        HBox.setHgrow(overviewBtn, Priority.ALWAYS);
        HBox.setHgrow(tasksBtn, Priority.ALWAYS);
        HBox.setHgrow(wellbeingBtn, Priority.ALWAYS);
        HBox.setHgrow(achievementsBtn, Priority.ALWAYS);

        overviewBtn.setMaxWidth(Double.MAX_VALUE);
        tasksBtn.setMaxWidth(Double.MAX_VALUE);
        wellbeingBtn.setMaxWidth(Double.MAX_VALUE);
        achievementsBtn.setMaxWidth(Double.MAX_VALUE);

        navBar.getChildren().addAll(overviewBtn, tasksBtn, wellbeingBtn, achievementsBtn);
        return navBar;
    }

    private ToggleButton createNavButton(String text, boolean selected) {
        ToggleButton button = new ToggleButton(text);
        button.setStyle("-fx-background-radius: 20; -fx-padding: 12 20; -fx-font-size: 14; -fx-font-weight: bold;");

        if (selected) {
            button.setSelected(true);
            button.setStyle(button.getStyle() +
                    " -fx-background-color: linear-gradient(to right, " + colors.get("accent_purple") + ", " + colors.get("accent_pink") + ");" +
                    " -fx-text-fill: white;");
        } else {
            button.setStyle(button.getStyle() +
                    " -fx-background-color: transparent;" +
                    " -fx-text-fill: " + colors.get("text_primary") + ";");
        }

        button.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-radius: 20; -fx-padding: 12 20; -fx-font-size: 14; -fx-font-weight: bold;" +
                        " -fx-background-color: linear-gradient(to right, " + colors.get("accent_purple") + ", " + colors.get("accent_pink") + ");" +
                        " -fx-text-fill: white;");
            } else {
                button.setStyle("-fx-background-radius: 20; -fx-padding: 12 20; -fx-font-size: 14; -fx-font-weight: bold;" +
                        " -fx-background-color: transparent;" +
                        " -fx-text-fill: " + colors.get("text_primary") + ";");
            }
        });

        return button;
    }

    private void switchToTab(String tabName) {
        resetTimeRangeToWeekly();
        switch (tabName) {
            case "Overview":
                tabPane.getSelectionModel().select(0);
                break;
            case "Tasks & Focus":
                tabPane.getSelectionModel().select(1);
                break;
            case "Wellbeing":
                tabPane.getSelectionModel().select(2);
                break;
            case "Achievements":
                tabPane.getSelectionModel().select(3);
                break;
        }
        refreshTabContent();
    }

    private VBox createHeader() {
        Label title = new Label("Productivity Analytics");
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        Label subtitle = new Label("Track your progress and celebrate achievements");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: " + colors.get("text_secondary") + ";");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        return header;
    }

    private HBox createTimeRangeSelector() {
        HBox selector = new HBox(10);
        selector.setAlignment(Pos.CENTER_RIGHT);

        timeGroup = new ToggleGroup();

        weekButton = createTimeRangeButton("Week", true);
        monthButton = createTimeRangeButton("Month", false);
        yearButton = createTimeRangeButton("Year", false);

        selector.getChildren().addAll(weekButton, monthButton, yearButton);

        return selector;
    }

    private ToggleButton createTimeRangeButton(String text, boolean selected) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(timeGroup);
        button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-min-width: 80;");

        if (selected) {
            button.setSelected(true);
            button.setStyle(button.getStyle() + " -fx-background-color: linear-gradient(to right, #C084FC, " + colors.get("accent_pink") + "); -fx-text-fill: white;");
        } else {
            button.setStyle(button.getStyle() + " -fx-background-color: transparent; -fx-border-color: #C084FC; -fx-text-fill: " + colors.get("text_primary") + ";");
        }

        button.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-min-width: 80; -fx-background-color: linear-gradient(to right, #C084FC, " + colors.get("accent_pink") + "); -fx-text-fill: white;");
                controller.setTimeRange(button.getText().toLowerCase());
                refreshTabContent();
            } else {
                button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-min-width: 80; -fx-background-color: transparent; -fx-border-color: #C084FC; -fx-text-fill: " + colors.get("text_primary") + ";");
            }
        });

        return button;
    }

    private void resetTimeRangeToWeekly() {
        if (weekButton != null) {
            weekButton.setSelected(true);
        }
        if (monthButton != null) {
            monthButton.setSelected(false);
        }
        if (yearButton != null) {
            yearButton.setSelected(false);
        }
        if (controller != null) {
            controller.setTimeRange("week");
        }
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab overviewTab = new Tab("Overview", createOverviewContent());
        Tab productivityTab = new Tab("Tasks & Focus", createProductivityContent());
        Tab wellbeingTab = new Tab("Wellbeing", createWellbeingContent());
        Tab achievementsTab = new Tab("Achievements", createAchievementsContent());

        // Nuclear option - hide everything
        tabPane.setStyle(
                "-fx-tab-max-height: 0; " +
                        "-fx-tab-min-height: 0; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 0; " +
                        "-fx-background-insets: 0; " +
                        "-fx-border-insets: 0; " +
                        "-fx-border-width: 0; " +
                        "-fx-control-inner-background: transparent; " +
                        "-fx-shadow-highlight-color: transparent; " +
                        "-fx-outer-border: transparent; " +
                        "-fx-inner-border: transparent; " +
                        "-fx-body-color: transparent; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent;"
        );

        // Hide individual tabs
        for (Tab tab : tabPane.getTabs()) {
            tab.setStyle(
                    "-fx-padding: 0; " +
                            "-fx-background-color: transparent; " +
                            "-fx-background-insets: 0; " +
                            "-fx-border-width: 0;"
            );
        }

        // Reset time range to weekly when switching tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                resetTimeRangeToWeekly();
                refreshTabContent();
            }
        });

        tabPane.getTabs().addAll(overviewTab, productivityTab, wellbeingTab, achievementsTab);

        return tabPane;
    }

    private void refreshTabContent() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            String tabText = selectedTab.getText();
            switch (tabText) {
                case "Overview":
                    selectedTab.setContent(createOverviewContent());
                    break;
                case "Tasks & Focus":
                    selectedTab.setContent(createProductivityContent());
                    break;
                case "Wellbeing":
                    selectedTab.setContent(createWellbeingContent());
                    break;
                case "Achievements":
                    selectedTab.setContent(createAchievementsContent());
                    break;
            }
        }
    }

    private Node createOverviewContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        // Stats cards - all in one row horizontally
        HBox statsRow = createOverviewStatsCards();

        // Activity chart
        VBox activityChart = createActivityChart();

        content.getChildren().addAll(statsRow, activityChart);
        return content;
    }

    private HBox createOverviewStatsCards() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10));

        int completionRate = controller.totalTasksProperty().get() > 0 ?
                (int) ((controller.tasksCompletedProperty().get() / (double) controller.totalTasksProperty().get()) * 100) : 0;
        int focusHours = controller.focusMinutesProperty().get() / 60;
        int focusMinutesRemaining = controller.focusMinutesProperty().get() % 60;

        VBox taskCard = createOverviewStatCard(
                "Task Completion",
                completionRate + "%",
                controller.tasksCompletedProperty().get() + " of " + controller.totalTasksProperty().get() + " tasks",
                colors.get("gradient_task_start"), colors.get("gradient_task_end"), "✓"
        );

        VBox focusCard = createOverviewStatCard(
                "Focus Time",
                focusHours + "h " + focusMinutesRemaining + "m",
                controller.pomodoroSessionsProperty().get() + " sessions",
                colors.get("gradient_focus_start"), colors.get("gradient_focus_end"), "⏰"
        );

        VBox streakCard = createOverviewStatCard(
                "Current Streak",
                controller.currentStreakProperty().get() + " days",
                "Best: " + controller.longestStreakProperty().get() + " days",
                colors.get("gradient_streak_start"), colors.get("gradient_streak_end"), "⚡"
        );

        VBox productivityCard = createOverviewStatCard(
                "Productivity Score",
                controller.productivityScoreProperty().get() + "%",
                getProductivityMessage(controller.productivityScoreProperty().get()),
                colors.get("gradient_productivity_start"), colors.get("gradient_productivity_end"), "📊"
        );

        statsRow.getChildren().addAll(taskCard, focusCard, streakCard, productivityCard);
        return statsRow;
    }

    private String getProductivityMessage(int score) {
        if (score >= 90) return "Excellent performance!";
        else if (score >= 70) return "Great work!";
        else if (score >= 50) return "Good progress";
        else return "Keep going!";
    }

    private VBox createOverviewStatCard(String title, String value, String subtitle, String startColor, String endColor, String emoji) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + darkenColor(startColor) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.5, 0, 4);"
        );
        card.setPrefSize(220, 140);
        card.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 28;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center; -fx-wrap-text: true;");
        subtitleLabel.setMaxWidth(180);

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private String darkenColor(String color) {
        return color.replaceFirst("#", "#A0");
    }

    private VBox createActivityChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + colors.get("bg_card") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setMaxWidth(1000);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Weekly Activity");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        activityChartCanvas = new Canvas(900, 400); // Larger canvas
        drawActivityChart(activityChartCanvas);
        setupChartHover(activityChartCanvas, "activity");

        chartContainer.getChildren().addAll(title, activityChartCanvas);
        return chartContainer;
    }

    private void drawActivityChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<AnalyticsController.WeeklyData> data = controller.getWeeklyData();
        if (data.isEmpty()) {
            drawPlaceholderChart(gc, canvas.getWidth(), canvas.getHeight());
            return;
        }

        double padding = 80;
        double chartWidth = canvas.getWidth() - 2 * padding;
        double chartHeight = canvas.getHeight() - 2 * padding;

        drawActivityGrid(gc, padding, chartWidth, chartHeight, data);
        drawActivityLines(gc, data, padding, chartWidth, chartHeight);
        drawActivityDataPoints(gc, data, padding, chartWidth, chartHeight);
    }

    private void drawActivityGrid(GraphicsContext gc, double padding, double chartWidth, double chartHeight, List<AnalyticsController.WeeklyData> data) {
        gc.setStroke(Color.web("#E5E7EB"));
        gc.setLineWidth(1);

        // Horizontal grid lines
        int maxTasks = data.stream().mapToInt(AnalyticsController.WeeklyData::getTasks).max().orElse(10);
        int maxPomodoros = data.stream().mapToInt(AnalyticsController.WeeklyData::getPomodoros).max().orElse(10);
        int maxValue = Math.max(maxTasks, maxPomodoros);
        maxValue = Math.max(maxValue, 10);

        for (int i = 0; i <= 5; i++) {
            double y = padding + chartHeight - (chartHeight / 5) * i;
            gc.strokeLine(padding, y, padding + chartWidth, y);

            // Y-axis labels
            gc.setFill(Color.web("#6B7280"));
            gc.setFont(Font.font("System", 12));
            int value = (int) ((i / 5.0) * maxValue);
            gc.fillText(String.valueOf(value), padding - 25, y + 5);
        }

        // Vertical grid lines
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            gc.strokeLine(x, padding, x, padding + chartHeight);
        }
    }

    private void drawActivityLines(GraphicsContext gc, List<AnalyticsController.WeeklyData> data, double padding, double chartWidth, double chartHeight) {
        int maxTasks = data.stream().mapToInt(AnalyticsController.WeeklyData::getTasks).max().orElse(10);
        int maxPomodoros = data.stream().mapToInt(AnalyticsController.WeeklyData::getPomodoros).max().orElse(10);
        int maxValue = Math.max(maxTasks, maxPomodoros);
        maxValue = Math.max(maxValue, 10);

        // Draw tasks line
        gc.setStroke(chartColors[1]); // Pink
        gc.setLineWidth(3);

        for (int i = 0; i < data.size() - 1; i++) {
            double x1 = padding + (chartWidth / (data.size() - 1)) * i;
            double y1 = padding + chartHeight - ((data.get(i).getTasks() / (double) maxValue) * chartHeight);

            double x2 = padding + (chartWidth / (data.size() - 1)) * (i + 1);
            double y2 = padding + chartHeight - ((data.get(i + 1).getTasks() / (double) maxValue) * chartHeight);

            gc.strokeLine(x1, y1, x2, y2);
        }

        // Draw pomodoros line
        gc.setStroke(chartColors[3]); // Blue
        gc.setLineWidth(3);

        for (int i = 0; i < data.size() - 1; i++) {
            double x1 = padding + (chartWidth / (data.size() - 1)) * i;
            double y1 = padding + chartHeight - ((data.get(i).getPomodoros() / (double) maxValue) * chartHeight);

            double x2 = padding + (chartWidth / (data.size() - 1)) * (i + 1);
            double y2 = padding + chartHeight - ((data.get(i + 1).getPomodoros() / (double) maxValue) * chartHeight);

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    private void drawActivityDataPoints(GraphicsContext gc, List<AnalyticsController.WeeklyData> data, double padding, double chartWidth, double chartHeight) {
        int maxTasks = data.stream().mapToInt(AnalyticsController.WeeklyData::getTasks).max().orElse(10);
        int maxPomodoros = data.stream().mapToInt(AnalyticsController.WeeklyData::getPomodoros).max().orElse(10);
        int maxValue = Math.max(maxTasks, maxPomodoros);
        maxValue = Math.max(maxValue, 10);

        // Draw tasks data points (without value labels)
        gc.setFill(chartColors[1]);
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getTasks() / (double) maxValue) * chartHeight);

            gc.fillOval(x - 6, y - 6, 12, 12);
            gc.setStroke(Color.web("#7C3AED"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 6, y - 6, 12, 12);
        }

        // Draw pomodoro data points (without value labels)
        gc.setFill(chartColors[3]);
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getPomodoros() / (double) maxValue) * chartHeight);

            gc.fillRect(x - 5, y - 5, 10, 10);
            gc.setStroke(Color.web("#2563EB"));
            gc.setLineWidth(2);
            gc.strokeRect(x - 5, y - 5, 10, 10);
        }

        // Day labels with shortened names to prevent overlap
        gc.setFont(Font.font("System", 12));
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            gc.setFill(Color.web("#6B7280"));

            // Use shortened day names
            String dayLabel = getShortenedDayName(data.get(i).getDay());
            gc.fillText(dayLabel, x - 10, padding + chartHeight + 25);
        }

        // Add legend with proper spacing
        gc.setFill(chartColors[1]);
        gc.fillText("Tasks Completed", padding + 10, padding - 20);
        gc.setFill(chartColors[3]);
        gc.fillText("Focus Sessions", padding + 150, padding - 20);
    }

    private void setupChartHover(Canvas canvas, String chartType) {
        canvas.setOnMouseMoved(event -> handleChartHover(event, canvas, chartType));
        canvas.setOnMouseExited(event -> chartTooltip.hide());
    }

    private void handleChartHover(MouseEvent event, Canvas canvas, String chartType) {
        List<AnalyticsController.WeeklyData> data = controller.getWeeklyData();
        if (data.isEmpty()) return;

        double padding = 80;
        double chartWidth = canvas.getWidth() - 2 * padding;
        double chartHeight = canvas.getHeight() - 2 * padding;

        double mouseX = event.getX();
        double mouseY = event.getY();

        AnalyticsController.WeeklyData closestData = null;
        double minDistance = Double.MAX_VALUE;

        int maxTasks = data.stream().mapToInt(AnalyticsController.WeeklyData::getTasks).max().orElse(10);
        int maxPomodoros = data.stream().mapToInt(AnalyticsController.WeeklyData::getPomodoros).max().orElse(10);
        int maxValue = Math.max(maxTasks, maxPomodoros);
        maxValue = Math.max(maxValue, 10);

        for (int i = 0; i < data.size(); i++) {
            AnalyticsController.WeeklyData entry = data.get(i);
            double x = padding + (chartWidth / (data.size() - 1)) * i;

            // Check tasks point
            double yTasks = padding + chartHeight - ((entry.getTasks() / (double) maxValue) * chartHeight);
            double distanceTasks = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - yTasks, 2));

            // Check pomodoros point
            double yPomodoros = padding + chartHeight - ((entry.getPomodoros() / (double) maxValue) * chartHeight);
            double distancePomodoros = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - yPomodoros, 2));

            if (distanceTasks < 20 && distanceTasks < minDistance) {
                minDistance = distanceTasks;
                closestData = entry;
            }
            if (distancePomodoros < 20 && distancePomodoros < minDistance) {
                minDistance = distancePomodoros;
                closestData = entry;
            }
        }

        if (closestData != null) {
            String tooltipText = String.format("%s\nTasks: %d\nFocus Sessions: %d\nMood: %d/5",
                    closestData.getDay(), closestData.getTasks(), closestData.getPomodoros(), closestData.getMood());

            chartTooltip.setText(tooltipText);
            chartTooltip.show(canvas, event.getScreenX() + 10, event.getScreenY() + 10);
        } else {
            chartTooltip.hide();
        }
    }

    private Node createProductivityContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        // Charts row
        HBox chartsRow = new HBox(30);
        chartsRow.setAlignment(Pos.CENTER);
        chartsRow.setPadding(new Insets(10));

        VBox taskChart = createTaskCompletionChart();
        VBox focusChart = createFocusSessionChart();

        chartsRow.getChildren().addAll(taskChart, focusChart);

        // Stats cards - centered below charts
        HBox statsRow = createProductivityStats();
        VBox statsContainer = new VBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.getChildren().add(statsRow);

        content.getChildren().addAll(chartsRow, statsContainer);
        return content;
    }

    private VBox createTaskCompletionChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + colors.get("bg_card") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(550); // Wider container
        chartContainer.setAlignment(Pos.CENTER);

        Label chartTitle = new Label("Daily Task Completion");
        chartTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        taskChartCanvas = new Canvas(500, 300); // Larger canvas
        drawBarChart(taskChartCanvas, "tasks");
        setupChartHover(taskChartCanvas, "tasks");

        chartContainer.getChildren().addAll(chartTitle, taskChartCanvas);
        return chartContainer;
    }

    private VBox createFocusSessionChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + colors.get("bg_card") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(550); // Wider container
        chartContainer.setAlignment(Pos.CENTER);

        Label chartTitle = new Label("Focus Session Intensity");
        chartTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        focusChartCanvas = new Canvas(500, 300); // Larger canvas
        drawBarChart(focusChartCanvas, "pomodoros");
        setupChartHover(focusChartCanvas, "pomodoros");

        chartContainer.getChildren().addAll(chartTitle, focusChartCanvas);
        return chartContainer;
    }

    private void drawBarChart(Canvas canvas, String dataType) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<AnalyticsController.WeeklyData> data = controller.getWeeklyData();
        if (data.isEmpty()) {
            drawPlaceholderChart(gc, canvas.getWidth(), canvas.getHeight());
            return;
        }

        double padding = 60;
        double chartWidth = canvas.getWidth() - 2 * padding;
        double chartHeight = canvas.getHeight() - 2 * padding;
        double barWidth = (chartWidth / data.size()) * 0.6;

        int maxValue = data.stream().mapToInt(d -> dataType.equals("tasks") ? d.getTasks() : d.getPomodoros()).max().orElse(10);
        maxValue = Math.max(maxValue, 10);

        // Draw bars with proper spacing (without value labels on bars)
        for (int i = 0; i < data.size(); i++) {
            double value = dataType.equals("tasks") ? data.get(i).getTasks() : data.get(i).getPomodoros();
            double barHeight = (value / maxValue) * chartHeight;
            double x = padding + i * (chartWidth / data.size()) + (chartWidth / data.size() - barWidth) / 2;
            double y = padding + chartHeight - barHeight;

            Color barColor = dataType.equals("tasks") ? chartColors[1] : chartColors[3];
            gc.setFill(barColor);
            gc.fillRect(x, y, barWidth, barHeight);

            // Add shortened day label with proper spacing
            gc.setFill(Color.web("#6B7280"));
            gc.setFont(Font.font("System", 12));

            // Use shortened day names to prevent overlap
            String dayLabel = getShortenedDayName(data.get(i).getDay());
            gc.fillText(dayLabel, x + barWidth/2 - 10, padding + chartHeight + 25);
        }

        // Y-axis labels
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", 12));
        for (int i = 0; i <= 5; i++) {
            double y = padding + chartHeight - (chartHeight / 5) * i;
            int value = (int) ((i / 5.0) * maxValue);
            gc.fillText(String.valueOf(value), padding - 25, y + 5);
        }
    }

    private HBox createProductivityStats() {
        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10));

        // Calculate average session length (25 minutes per pomodoro)
        String avgSession = "25 min";
        String sessionNote = "Perfect pomodoro length";

        // Calculate daily goal achievement (target: 3 tasks per day)
        int totalDays = 7; // For weekly view
        int targetTasks = totalDays * 3; // 3 tasks per day
        int actualCompleted = controller.tasksCompletedProperty().get();
        int goalPercentage = targetTasks > 0 ? Math.min((actualCompleted * 100) / targetTasks, 100) : 0;

        // Calculate productivity trend (compare current week to previous week)
        int trend = calculateProductivityTrend();

        VBox avgSessionCard = createProductivityStatCard("⏰", "Average Focus Session", avgSession, sessionNote, colors.get("gradient_focus_start"), colors.get("gradient_focus_end"));
        VBox goalCard = createProductivityStatCard("🎯", "Daily Goal Achievement", goalPercentage + "%",
                actualCompleted + " of " + targetTasks + " target tasks", colors.get("gradient_productivity_start"), colors.get("gradient_productivity_end"));
        VBox trendCard = createProductivityStatCard("📈", "Productivity Trend", getTrendArrow(trend),
                getTrendDescription(trend), colors.get("gradient_streak_start"), colors.get("gradient_streak_end"));

        statsRow.getChildren().addAll(avgSessionCard, goalCard, trendCard);
        return statsRow;
    }

    private VBox createProductivityStatCard(String emoji, String title, String value, String subtitle, String startColor, String endColor) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + darkenColor(startColor) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);"
        );
        card.setPrefSize(200, 140);
        card.setMinWidth(200);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 28;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + colors.get("text_dark") + "; -fx-text-alignment: center; -fx-wrap-text: true;");
        subtitleLabel.setMaxWidth(180);

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private int calculateProductivityTrend() {
        var chartData = controller.getWeeklyData();
        if (chartData.size() < 2) return 0;

        int midPoint = chartData.size() / 2;
        int firstHalfTasks = 0;
        int secondHalfTasks = 0;

        for (int i = 0; i < chartData.size(); i++) {
            if (i < midPoint) {
                firstHalfTasks += chartData.get(i).getTasks();
            } else {
                secondHalfTasks += chartData.get(i).getTasks();
            }
        }

        if (firstHalfTasks == 0) return 100;
        return ((secondHalfTasks - firstHalfTasks) * 100) / firstHalfTasks;
    }

    private String getTrendArrow(int trend) {
        if (trend > 10) return "↗ " + Math.abs(trend) + "%";
        else if (trend < -10) return "↘ " + Math.abs(trend) + "%";
        else return "→ " + Math.abs(trend) + "%";
    }

    private String getTrendDescription(int trend) {
        if (trend > 10) return "Improving trend!";
        else if (trend < -10) return "Needs attention";
        else return "Stable performance";
    }

    private Node createWellbeingContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        HBox chartsRow = new HBox(30);
        chartsRow.setAlignment(Pos.CENTER);
        chartsRow.setPadding(new Insets(10));

        VBox moodDistributionChart = createMoodDistributionChart();
        VBox moodTrendChart = createMoodTrendChart();

        chartsRow.getChildren().addAll(moodDistributionChart, moodTrendChart);

        HBox statsRow = createWellbeingStats();
        VBox statsContainer = new VBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.getChildren().add(statsRow);

        content.getChildren().addAll(chartsRow, statsContainer);
        return content;
    }

    private VBox createMoodDistributionChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + colors.get("bg_card") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(550);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Mood Distribution");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        Canvas canvas = new Canvas(500, 350);
        drawMoodDistributionChart(canvas);
        setupPieChartHover(canvas); // Add hover effect to pie chart

        chartContainer.getChildren().addAll(title, canvas);
        return chartContainer;
    }

    private void drawMoodDistributionChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<AnalyticsController.MoodDistribution> distribution = controller.getMoodDistribution();
        if (distribution.isEmpty()) {
            drawPlaceholderChart(gc, canvas.getWidth(), canvas.getHeight());
            return;
        }

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2 - 10;
        double radius = Math.min(centerX, centerY) - 40;

        double total = distribution.stream().mapToInt(AnalyticsController.MoodDistribution::getValue).sum();
        double startAngle = 0;

        // Store slice information for hover detection
        PieSlice[] slices = new PieSlice[distribution.size()];

        for (int i = 0; i < distribution.size(); i++) {
            AnalyticsController.MoodDistribution mood = distribution.get(i);
            double sliceAngle = 360 * (mood.getValue() / total);

            gc.setFill(Color.web(mood.getColor()));
            gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, sliceAngle, javafx.scene.shape.ArcType.ROUND);

            // Store slice information for hover detection
            slices[i] = new PieSlice(startAngle, sliceAngle, centerX, centerY, radius, mood);

            startAngle += sliceAngle;
        }

        // Store slices in canvas properties for hover detection
        canvas.setUserData(slices);
    }

    private void setupPieChartHover(Canvas canvas) {
        canvas.setOnMouseMoved(event -> handlePieChartHover(event, canvas));
        canvas.setOnMouseExited(event -> chartTooltip.hide());
    }

    private void handlePieChartHover(MouseEvent event, Canvas canvas) {
        PieSlice[] slices = (PieSlice[]) canvas.getUserData();
        if (slices == null) return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        // Check if mouse is inside the pie chart circle
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2 - 10;
        double radius = Math.min(centerX, centerY) - 40;

        double distanceFromCenter = Math.sqrt(
                Math.pow(mouseX - centerX, 2) +
                        Math.pow(mouseY - centerY, 2)
        );

        if (distanceFromCenter > radius) {
            chartTooltip.hide();
            return;
        }

        // Calculate angle from center
        double angle = Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX));
        if (angle < 0) angle += 360;

        // Find which slice contains this angle
        for (PieSlice slice : slices) {
            if (slice.containsAngle(angle)) {
                AnalyticsController.MoodDistribution mood = slice.getMoodData();
                String tooltipText = String.format("%s: %d%%", mood.getMood(), mood.getValue());
                chartTooltip.setText(tooltipText);
                chartTooltip.show(canvas, event.getScreenX() + 10, event.getScreenY() + 10);
                return;
            }
        }

        chartTooltip.hide();
    }

    private VBox createMoodTrendChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + colors.get("bg_card") + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + colors.get("border_primary") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(550);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Weekly Mood Trend");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        moodTrendCanvas = new Canvas(500, 350);
        drawMoodTrendChart(moodTrendCanvas);
        setupChartHover(moodTrendCanvas, "mood");

        chartContainer.getChildren().addAll(title, moodTrendCanvas);
        return chartContainer;
    }

    private void drawMoodTrendChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<AnalyticsController.WeeklyData> data = controller.getWeeklyData();
        if (data.isEmpty()) {
            drawPlaceholderChart(gc, canvas.getWidth(), canvas.getHeight());
            return;
        }

        double padding = 60;
        double chartWidth = canvas.getWidth() - 2 * padding;
        double chartHeight = canvas.getHeight() - 2 * padding;

        // Draw mood line
        gc.setStroke(chartColors[0]); // Purple
        gc.setLineWidth(3);

        for (int i = 0; i < data.size() - 1; i++) {
            double x1 = padding + (chartWidth / (data.size() - 1)) * i;
            double y1 = padding + chartHeight - ((data.get(i).getMood() - 1) / 4.0) * chartHeight;

            double x2 = padding + (chartWidth / (data.size() - 1)) * (i + 1);
            double y2 = padding + chartHeight - ((data.get(i + 1).getMood() - 1) / 4.0) * chartHeight;

            gc.strokeLine(x1, y1, x2, y2);
        }

        // Draw data points (without value labels)
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getMood() - 1) / 4.0) * chartHeight;

            gc.setFill(chartColors[0]);
            gc.fillOval(x - 6, y - 6, 12, 12);
            gc.setStroke(Color.web("#7C3AED"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 6, y - 6, 12, 12);

            // Day label with shortened names
            gc.setFill(Color.web("#6B7280"));
            gc.setFont(Font.font("System", 12));

            // Use shortened day names
            String dayLabel = getShortenedDayName(data.get(i).getDay());
            gc.fillText(dayLabel, x - 10, padding + chartHeight + 25);
        }

        // Y-axis labels
        String[] moodLevels = {"5 - 😄", "4 - 😊", "3 - 😐", "2 - 😟", "1 - 😢"};
        gc.setFont(Font.font("System", 12));
        for (int i = 0; i < 5; i++) {
            double y = padding + (chartHeight / 4) * i;
            gc.setFill(Color.web("#6B7280"));
            gc.fillText(moodLevels[i], 10, y + 5);
        }
    }

    private HBox createWellbeingStats() {
        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10));

        VBox moodCard = createProductivityStatCard("❤️", "Average Mood",
                String.format("%.1f/5", controller.averageMoodProperty().get()),
                getMoodTimeRangeText(), colors.get("gradient_mood_start"), colors.get("gradient_mood_end"));

        VBox consistencyCard = createProductivityStatCard("📅", "Mood Entries",
                controller.moodEntriesProperty().get() + " logged",
                "Keep tracking daily", colors.get("gradient_productivity_start"), colors.get("gradient_productivity_end"));

        statsRow.getChildren().addAll(moodCard, consistencyCard);
        return statsRow;
    }

    private String getMoodTimeRangeText() {
        return "This week";
    }

    private VBox createLevelCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #E2D6FF, #F0D2F7); " +
                "-fx-background-radius: 20; -fx-border-color: #C084FC; -fx-border-width: 2; " +
                "-fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);
        card.setAlignment(Pos.CENTER);

        // Get user XP and calculate level
        int userXP = controller.getUserXP();
        int currentLevel = calculateLevel(userXP);
        int xpForNextLevel = getXpForLevel(currentLevel + 1);
        int xpForCurrentLevel = getXpForLevel(currentLevel);
        int xpProgress = userXP - xpForCurrentLevel;
        int xpNeeded = xpForNextLevel - xpForCurrentLevel;
        double progressPercentage = (double) xpProgress / xpNeeded;

        Label levelIcon = new Label("⭐");
        levelIcon.setStyle("-fx-font-size: 32;");

        Label levelTitle = new Label("Level " + currentLevel + " " + getLevelTitle(currentLevel));
        levelTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + colors.get("text_primary") + ";");

        Label xpLabel = new Label(userXP + " Total XP");
        xpLabel.setStyle("-fx-font-size: 16; -fx-text-fill: " + colors.get("text_secondary") + ";");

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(progressPercentage);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(12);
        progressBar.setStyle("-fx-accent: " + colors.get("accent_purple") + "; -fx-background-color: #E5E7EB; -fx-background-radius: 6;");

        Label progressLabel = new Label(xpProgress + "/" + xpNeeded + " XP to Level " + (currentLevel + 1));
        progressLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + colors.get("text_primary") + "; -fx-font-weight: bold;");

        card.getChildren().addAll(levelIcon, levelTitle, xpLabel, progressBar, progressLabel);
        return card;
    }

    private int calculateLevel(int xp) {
        if (xp < 1000) return 1;      // Newbie
        if (xp < 2500) return 2;      // Beginner
        if (xp < 5000) return 3;      // Enthusiast
        if (xp < 10000) return 4;     // Productive
        if (xp < 20000) return 5;     // Focused
        if (xp < 40000) return 6;     // Master
        if (xp < 80000) return 7;     // Guru
        if (xp < 150000) return 8;    // Legend
        if (xp < 300000) return 9;    // Elite
        return 10 + (xp - 300000) / 100000; // Every 100k XP after 300k
    }

    private Node createAchievementsContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        // Level and XP Card
        VBox levelCard = createLevelCard();

        FlowPane achievementsGrid = new FlowPane();
        achievementsGrid.setHgap(25);
        achievementsGrid.setVgap(25);
        achievementsGrid.setPrefWrapLength(1200); // Wider wrap length
        achievementsGrid.setAlignment(Pos.CENTER);
        achievementsGrid.setPadding(new Insets(10));

        // Get real badges from database instead of mock data
        for (AnalyticsController.Achievement achievement : controller.getRealAchievements()) {
            VBox achievementCard = createAchievementCard(achievement);
            achievementsGrid.getChildren().add(achievementCard);
        }

        content.getChildren().addAll(levelCard, achievementsGrid);
        return content;
    }

    private int getXpForLevel(int level) {
        switch (level) {
            case 1: return 0;
            case 2: return 1000;
            case 3: return 2500;
            case 4: return 5000;
            case 5: return 10000;
            case 6: return 20000;
            case 7: return 40000;
            case 8: return 80000;
            case 9: return 150000;
            case 10: return 300000;
            default: return 300000 + (level - 10) * 100000;
        }
    }

    private String getLevelTitle(int level) {
        switch (level) {
            case 1: return "Newbie";
            case 2: return "Beginner";
            case 3: return "Enthusiast";
            case 4: return "Productive";
            case 5: return "Focused";
            case 6: return "Master";
            case 7: return "Guru";
            case 8: return "Legend";
            case 9: return "Elite";
            default: return "Grand Master";
        }
    }

    private VBox createAchievementCard(AnalyticsController.Achievement achievement) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefSize(350, 180); // Much wider cards
        card.setMinWidth(350);

        String cardStyle = "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f8f8); " +
                "-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);";

        if (achievement.isUnlocked()) {
            cardStyle = "-fx-background-color: linear-gradient(to bottom right, #FFF9C4, #FFECB3); " +
                    "-fx-background-radius: 15; -fx-border-color: #FFD54F; -fx-border-width: 2; " +
                    "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);";
        }

        card.setStyle(cardStyle);

        HBox header = new HBox(20); // More spacing
        header.setAlignment(Pos.CENTER_LEFT);

        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle("-fx-background-color: " + (achievement.isUnlocked() ?
                "linear-gradient(to bottom right, #FFD54F, #FFC107)" : achievement.getColor()) +
                "; -fx-background-radius: 10; -fx-padding: 10;");
        iconContainer.setMinSize(50, 50);

        Label iconLabel = new Label(achievement.isUnlocked() ? "🏆" : achievement.getEmoji());
        iconLabel.setStyle("-fx-font-size: 20;");
        iconContainer.getChildren().add(iconLabel);

        VBox textContent = new VBox(8);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        textContent.setMaxWidth(260); // Wider text area

        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(achievement.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #6d7d8d; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(200);

        if (achievement.isUnlocked()) {
            Label badge = new Label("Unlocked");
            badge.setStyle("-fx-background-color: linear-gradient(to right, #FFEB3B, #FFD740); -fx-text-fill: #FF6F00; -fx-font-size: 11; " +
                    "-fx-padding: 4 10; -fx-background-radius: 10;");
            titleRow.getChildren().addAll(titleLabel, badge);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        Label descriptionLabel = new Label(achievement.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #8d9dad; -fx-wrap-text: true;");
        descriptionLabel.setMaxWidth(260);

        VBox progressContainer = new VBox(5);
        HBox progressLabels = new HBox();
        progressLabels.setAlignment(Pos.CENTER);
        HBox.setHgrow(progressLabels, Priority.ALWAYS);

        Label progressText = new Label("Progress");
        progressText.setStyle("-fx-font-size: 12; -fx-text-fill: #8d9dad;");

        Label progressValue = new Label(achievement.getProgress() + "/" + achievement.getMaxProgress());
        progressValue.setStyle("-fx-font-size: 12; -fx-text-fill: #8d9dad;");

        progressLabels.getChildren().addAll(progressText, progressValue);
        HBox.setHgrow(progressValue, Priority.ALWAYS);
        HBox.setMargin(progressValue, new Insets(0, 0, 0, 0));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(achievement.getProgress() / (double) achievement.getMaxProgress());
        progressBar.setPrefWidth(260);
        progressBar.setStyle("-fx-accent: " + achievement.getColor() + "; -fx-background-color: #e0e0e0;");

        progressContainer.getChildren().addAll(progressLabels, progressBar);

        textContent.getChildren().addAll(titleRow, descriptionLabel, progressContainer);
        header.getChildren().addAll(iconContainer, textContent);

        card.getChildren().add(header);
        return card;
    }

    private void drawPlaceholderChart(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 16));
        gc.fillText("No data available yet. Keep using the app!", width/2 - 120, height/2);
    }

    // Helper method to get shortened day names
    private String getShortenedDayName(String dayName) {
        // If the day name starts with "Week", return it as-is (for weekly labels)
        if (dayName.toLowerCase().startsWith("week")) {
            return dayName;
        }

        switch (dayName.toLowerCase()) {
            case "monday": return "Mon";
            case "tuesday": return "Tue";
            case "wednesday": return "Wed";
            case "thursday": return "Thu";
            case "friday": return "Fri";
            case "saturday": return "Sat";
            case "sunday": return "Sun";
            default: return dayName.length() > 3 ? dayName.substring(0, 3) : dayName;
        }
    }

    // Helper class for pie chart hover detection
    private static class PieSlice {
        private final double startAngle;
        private final double sliceAngle;
        private final double centerX;
        private final double centerY;
        private final double radius;
        private final AnalyticsController.MoodDistribution moodData;

        public PieSlice(double startAngle, double sliceAngle, double centerX, double centerY,
                        double radius, AnalyticsController.MoodDistribution moodData) {
            this.startAngle = startAngle;
            this.sliceAngle = sliceAngle;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.moodData = moodData;
        }

        public boolean containsAngle(double angle) {
            // Normalize angles for comparison
            double endAngle = startAngle + sliceAngle;
            return angle >= startAngle && angle <= endAngle;
        }

        public AnalyticsController.MoodDistribution getMoodData() {
            return moodData;
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}