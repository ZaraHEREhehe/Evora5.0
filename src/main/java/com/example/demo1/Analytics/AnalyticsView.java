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

public class AnalyticsView {
    private final AnalyticsController controller;
    private VBox mainContent;
    private TabPane tabPane;
    private Canvas taskChartCanvas;
    private Canvas focusChartCanvas;
    private Canvas moodTrendCanvas;
    private Tooltip chartTooltip;
    private ToggleGroup timeGroup;
    private ToggleButton weekButton, monthButton, yearButton;

    // Pastel color palette
    private final Color bgColor = Color.web("#fdf7ff");
    private final Color cardBg = Color.web("#FFFFFF");
    private final Color textPrimary = Color.web("#5c5470");
    private final Color textSecondary = Color.web("#756f86");
    private final Color borderColor = Color.web("#D8B4FE");

    private final Color[] chartColors = {
            Color.web("#A78BFA"), // Pastel Purple
            Color.web("#F472B6"), // Pastel Pink
            Color.web("#34D399"), // Pastel Green
            Color.web("#60A5FA"), // Pastel Blue
            Color.web("#FBBF24"), // Pastel Yellow
            Color.web("#FB923C")  // Pastel Orange
    };

    public AnalyticsView(int userId, String userName) {
        this.controller = new AnalyticsController(userId, userName);
        this.chartTooltip = new Tooltip();
        this.chartTooltip.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
    }

    public Node create() {
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #fdf7ff;");

        // Header
        VBox header = createHeader();

        // Time range selector
        HBox timeRangeSelector = createTimeRangeSelector();

        // Tab pane for different analytics sections
        tabPane = createTabPane();

        mainContent.getChildren().addAll(header, timeRangeSelector, tabPane);

        return mainContent;
    }

    private VBox createHeader() {
        Label title = new Label("Productivity Analytics");
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        Label subtitle = new Label("Track your progress and celebrate achievements");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: #756f86;");

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
            button.setStyle(button.getStyle() + " -fx-background-color: linear-gradient(to right, #C084FC, #F472B6); -fx-text-fill: white;");
        } else {
            button.setStyle(button.getStyle() + " -fx-background-color: transparent; -fx-border-color: #C084FC; -fx-text-fill: #5c5470;");
        }

        button.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-min-width: 80; -fx-background-color: linear-gradient(to right, #C084FC, #F472B6); -fx-text-fill: white;");
                controller.setTimeRange(button.getText().toLowerCase());
                refreshTabContent();
            } else {
                button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-min-width: 80; -fx-background-color: transparent; -fx-border-color: #C084FC; -fx-text-fill: #5c5470;");
            }
        });

        return button;
    }

    private void resetTimeRangeToWeekly() {
        weekButton.setSelected(true);
        monthButton.setSelected(false);
        yearButton.setSelected(false);
        controller.setTimeRange("week");
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab overviewTab = new Tab("Overview", createOverviewContent());
        Tab productivityTab = new Tab("Tasks & Focus", createProductivityContent());
        Tab wellbeingTab = new Tab("Wellbeing", createWellbeingContent());
        Tab achievementsTab = new Tab("Achievements", createAchievementsContent());

        // Reset time range to weekly when switching tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                resetTimeRangeToWeekly();
                refreshTabContent();
            }
        });

        for (Tab tab : new Tab[]{overviewTab, productivityTab, wellbeingTab, achievementsTab}) {
            tab.setStyle("-fx-font-size: 14; -fx-padding: 10 20;");
        }

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
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        // Stats cards
        GridPane statsGrid = createStatsCards();

        // Activity chart
        VBox activityChart = createActivityChart();

        content.getChildren().addAll(statsGrid, activityChart);
        return content;
    }

    private GridPane createStatsCards() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int completionRate = controller.totalTasksProperty().get() > 0 ?
                (int) ((controller.tasksCompletedProperty().get() / (double) controller.totalTasksProperty().get()) * 100) : 0;
        int focusHours = controller.focusMinutesProperty().get() / 60;
        int focusMinutesRemaining = controller.focusMinutesProperty().get() % 60;

        VBox taskCard = createStatCard(
                "Task Completion",
                completionRate + "%",
                controller.tasksCompletedProperty().get() + " of " + controller.totalTasksProperty().get() + " tasks",
                "#F3E8FF", "#FCE7F3", "✓"
        );

        VBox focusCard = createStatCard(
                "Focus Time",
                focusHours + "h " + focusMinutesRemaining + "m",
                controller.pomodoroSessionsProperty().get() + " sessions",
                "#E0F2FE", "#BAE6FD", "⏰"
        );

        VBox streakCard = createStatCard(
                "Current Streak",
                controller.currentStreakProperty().get() + " days",
                "Best: " + controller.longestStreakProperty().get() + " days",
                "#F0E8FF", "#E8D6FF", "⚡"
        );

        VBox coinsCard = createStatCard(
                "Coins Earned",
                String.valueOf(controller.coinsEarnedProperty().get()),
                "Pet happiness: " + controller.petHappinessProperty().get() + "%",
                "#E8F5E8", "#D4F0D4", "⭐"
        );

        grid.add(taskCard, 0, 0);
        grid.add(focusCard, 1, 0);
        grid.add(streakCard, 0, 1);
        grid.add(coinsCard, 1, 1);

        return grid;
    }

    private VBox createStatCard(String title, String value, String subtitle, String startColor, String endColor, String emoji) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + darkenColor(startColor) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.5, 0, 4);"
        );
        card.setPrefSize(250, 140);
        card.setAlignment(Pos.CENTER);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24;");

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2A2D3A; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2A2D3A;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2A2D3A; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        headerBox.getChildren().addAll(emojiLabel, textBox);

        card.getChildren().add(headerBox);
        return card;
    }

    private String darkenColor(String color) {
        return color.replaceFirst("#", "#A0");
    }

    private VBox createActivityChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setMaxWidth(850);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Weekly Activity");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        Canvas canvas = new Canvas(750, 350);
        drawActivityChart(canvas);

        chartContainer.getChildren().addAll(title, canvas);
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

        gc.setFont(Font.font("System", 11));

        // Draw tasks data points
        gc.setFill(chartColors[1]);
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getTasks() / (double) maxValue) * chartHeight);

            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setStroke(Color.web("#7C3AED"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 5, y - 5, 10, 10);

            // Value label
            gc.setFill(Color.web("#5c5470"));
            gc.fillText(String.valueOf(data.get(i).getTasks()), x - 8, y - 10);
        }

        // Draw pomodoro data points
        gc.setFill(chartColors[3]);
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getPomodoros() / (double) maxValue) * chartHeight);

            gc.fillRect(x - 4, y - 4, 8, 8);
            gc.setStroke(Color.web("#2563EB"));
            gc.setLineWidth(2);
            gc.strokeRect(x - 4, y - 4, 8, 8);

            // Value label
            gc.setFill(Color.web("#5c5470"));
            gc.fillText(String.valueOf(data.get(i).getPomodoros()), x - 8, y - 15);
        }

        // Day labels with proper spacing
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            gc.setFill(Color.web("#6B7280"));
            gc.fillText(data.get(i).getDay(), x - 15, padding + chartHeight + 25);
        }

        // Add legend with proper spacing
        gc.setFill(chartColors[1]);
        gc.fillText("Tasks Completed", padding + 10, padding - 20);
        gc.setFill(chartColors[3]);
        gc.fillText("Focus Sessions", padding + 150, padding - 20);
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
        chartContainer.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(450);
        chartContainer.setAlignment(Pos.CENTER);

        Label chartTitle = new Label("Daily Task Completion");
        chartTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        taskChartCanvas = new Canvas(400, 250);
        drawBarChart(taskChartCanvas, "tasks");

        chartContainer.getChildren().addAll(chartTitle, taskChartCanvas);
        return chartContainer;
    }

    private VBox createFocusSessionChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(450);
        chartContainer.setAlignment(Pos.CENTER);

        Label chartTitle = new Label("Focus Session Intensity");
        chartTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        focusChartCanvas = new Canvas(400, 250);
        drawBarChart(focusChartCanvas, "pomodoros");

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

        double padding = 50;
        double chartWidth = canvas.getWidth() - 2 * padding;
        double chartHeight = canvas.getHeight() - 2 * padding;
        double barWidth = (chartWidth / data.size()) * 0.6; // 60% of available space

        int maxValue = data.stream().mapToInt(d -> dataType.equals("tasks") ? d.getTasks() : d.getPomodoros()).max().orElse(10);
        maxValue = Math.max(maxValue, 10);

        // Draw bars with proper spacing
        for (int i = 0; i < data.size(); i++) {
            double value = dataType.equals("tasks") ? data.get(i).getTasks() : data.get(i).getPomodoros();
            double barHeight = (value / maxValue) * chartHeight;
            double x = padding + i * (chartWidth / data.size()) + (chartWidth / data.size() - barWidth) / 2;
            double y = padding + chartHeight - barHeight;

            Color barColor = dataType.equals("tasks") ? chartColors[1] : chartColors[3];
            gc.setFill(barColor);
            gc.fillRect(x, y, barWidth, barHeight);

            // Add value label
            gc.setFill(Color.web("#5c5470"));
            gc.setFont(Font.font("System", 11));
            gc.fillText(String.valueOf((int)value), x + barWidth/2 - 5, y - 8);

            // Add day label with proper spacing
            gc.fillText(data.get(i).getDay(), x + barWidth/2 - 10, padding + chartHeight + 20);
        }

        // Y-axis labels
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", 11));
        for (int i = 0; i <= 5; i++) {
            double y = padding + chartHeight - (chartHeight / 5) * i;
            int value = (int) ((i / 5.0) * maxValue);
            gc.fillText(String.valueOf(value), padding - 20, y + 5);
        }
    }

    private HBox createProductivityStats() {
        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10));

        String avgSession = "25 min";
        String sessionNote = "Perfect pomodoro length";

        int totalPossible = 0;
        int actualCompleted = 0;

        for (AnalyticsController.WeeklyData data : controller.getWeeklyData()) {
            actualCompleted += data.getTasks();
            totalPossible += 3; // Assume 3 tasks per day is achievable
        }

        int goalPercentage = totalPossible > 0 ? (actualCompleted * 100) / totalPossible : 0;
        goalPercentage = Math.min(goalPercentage, 100);

        int trend = calculateProductivityTrend();

        VBox avgSessionCard = createProductivityStatCard("⏰", "Average Focus Session", avgSession, sessionNote, "#E0F2FE", "#BAE6FD");
        VBox goalCard = createProductivityStatCard("🎯", "Daily Goal Achievement", goalPercentage + "%",
                Math.min(actualCompleted, 7) + " out of 7 days", "#E8F5E8", "#D4F0D4");
        VBox trendCard = createProductivityStatCard("📈", "Productivity Trend", getTrendArrow(trend),
                getTrendDescription(trend), "#F0E8FF", "#E8D6FF");

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
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2A2D3A; -fx-text-alignment: center; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2A2D3A; -fx-text-alignment: center;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2A2D3A; -fx-text-alignment: center; -fx-wrap-text: true;");
        subtitleLabel.setMaxWidth(180);

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    // ... (rest of the methods remain the same as previous version, including:
    // calculateProductivityTrend, getTrendArrow, getTrendDescription,
    // createWellbeingContent, createMoodDistributionChart, drawMoodDistributionChart,
    // createMoodTrendChart, drawMoodTrendChart, createWellbeingStats,
    // createAchievementsContent, createAchievementCard, createProgressSummary,
    // drawPlaceholderChart, toHex)

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
        chartContainer.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(450);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Mood Distribution");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        Canvas canvas = new Canvas(400, 280);
        drawMoodDistributionChart(canvas);

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
        double radius = Math.min(centerX, centerY) - 30;

        double total = distribution.stream().mapToInt(AnalyticsController.MoodDistribution::getValue).sum();
        double startAngle = 0;

        for (AnalyticsController.MoodDistribution mood : distribution) {
            double sliceAngle = 360 * (mood.getValue() / total);

            gc.setFill(Color.web(mood.getColor()));
            gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, sliceAngle, javafx.scene.shape.ArcType.ROUND);

            // Draw label
            double labelAngle = startAngle + sliceAngle / 2;
            double labelRadius = radius * 0.7;
            double labelX = centerX + labelRadius * Math.cos(Math.toRadians(labelAngle));
            double labelY = centerY + labelRadius * Math.sin(Math.toRadians(labelAngle));

            gc.setFill(Color.web("#5c5470"));
            gc.setFont(Font.font("System", 11));
            gc.fillText(mood.getValue() + "%", labelX - 10, labelY);

            startAngle += sliceAngle;
        }

        // Draw legend with proper spacing
        double legendY = 20;
        gc.setFont(Font.font("System", 12));
        for (AnalyticsController.MoodDistribution mood : distribution) {
            gc.setFill(Color.web(mood.getColor()));
            gc.fillRect(30, legendY, 15, 15);
            gc.setFill(Color.web("#5c5470"));
            gc.fillText(mood.getMood() + " (" + mood.getValue() + "%)", 50, legendY + 12);
            legendY += 25;
        }
    }

    private VBox createMoodTrendChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        chartContainer.setPrefWidth(450);
        chartContainer.setAlignment(Pos.CENTER);

        Label title = new Label("Weekly Mood Trend");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #5c5470;");

        moodTrendCanvas = new Canvas(400, 280);
        drawMoodTrendChart(moodTrendCanvas);

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

        double padding = 50;
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

        // Draw data points
        for (int i = 0; i < data.size(); i++) {
            double x = padding + (chartWidth / (data.size() - 1)) * i;
            double y = padding + chartHeight - ((data.get(i).getMood() - 1) / 4.0) * chartHeight;

            gc.setFill(chartColors[0]);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setStroke(Color.web("#7C3AED"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 5, y - 5, 10, 10);

            // Value label
            gc.setFill(Color.web("#5c5470"));
            gc.setFont(Font.font("System", 11));
            gc.fillText(String.valueOf(data.get(i).getMood()), x - 5, y - 10);

            // Day label
            gc.fillText(data.get(i).getDay(), x - 10, padding + chartHeight + 20);
        }

        // Y-axis labels
        String[] moodLevels = {"5 - 😄", "4 - 😊", "3 - 😐", "2 - 😟", "1 - 😢"};
        gc.setFont(Font.font("System", 11));
        for (int i = 0; i < 5; i++) {
            double y = padding + (chartHeight / 4) * i;
            gc.setFill(Color.web("#6B7280"));
            gc.fillText(moodLevels[i], 5, y + 5);
        }
    }

    private HBox createWellbeingStats() {
        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10));

        VBox moodCard = createProductivityStatCard("❤️", "Average Mood",
                String.format("%.1f/5", controller.averageMoodProperty().get()),
                getMoodTimeRangeText(), "#F3E8FF", "#FCE7F3");

        VBox petCard = createProductivityStatCard("⭐", "Pet Happiness",
                controller.petHappinessProperty().get() + "%",
                getPetHappinessMessage(), "#E8F5E8", "#D4F0D4");

        statsRow.getChildren().addAll(moodCard, petCard);
        return statsRow;
    }

    private String getMoodTimeRangeText() {
        return "This week"; // Always show "This week" since we reset to weekly
    }

    private String getPetHappinessMessage() {
        int happiness = controller.petHappinessProperty().get();
        if (happiness >= 90) return "Your pet adores you!";
        else if (happiness >= 70) return "Your pet is very happy!";
        else if (happiness >= 50) return "Your pet is content";
        else return "Your pet needs attention";
    }

    private Node createAchievementsContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        FlowPane achievementsGrid = new FlowPane();
        achievementsGrid.setHgap(20);
        achievementsGrid.setVgap(20);
        achievementsGrid.setPrefWrapLength(900);
        achievementsGrid.setAlignment(Pos.CENTER);
        achievementsGrid.setPadding(new Insets(10));

        for (AnalyticsController.Achievement achievement : controller.getAchievements()) {
            VBox achievementCard = createAchievementCard(achievement);
            achievementsGrid.getChildren().add(achievementCard);
        }

        VBox progressSummary = createProgressSummary();

        content.getChildren().addAll(achievementsGrid, progressSummary);
        return content;
    }

    private VBox createAchievementCard(AnalyticsController.Achievement achievement) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefSize(280, 160); // Increased width to prevent ellipsis
        card.setMinWidth(280);

        String cardStyle = "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f8f8); " +
                "-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);";

        if (achievement.isUnlocked()) {
            cardStyle = "-fx-background-color: linear-gradient(to bottom right, #FFF9C4, #FFECB3); " +
                    "-fx-background-radius: 15; -fx-border-color: #FFD54F; -fx-border-width: 2; " +
                    "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);";
        }

        card.setStyle(cardStyle);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle("-fx-background-color: " + (achievement.isUnlocked() ?
                "linear-gradient(to bottom right, #FFD54F, #FFC107)" : achievement.getColor()) +
                "; -fx-background-radius: 10; -fx-padding: 8;");
        iconContainer.setMinSize(45, 45);

        Label iconLabel = new Label(achievement.isUnlocked() ? "🏆" : achievement.getEmoji());
        iconLabel.setStyle("-fx-font-size: 18;");
        iconContainer.getChildren().add(iconLabel);

        VBox textContent = new VBox(8);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        textContent.setMaxWidth(200);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(achievement.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #6d7d8d; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);

        if (achievement.isUnlocked()) {
            Label badge = new Label("Unlocked");
            badge.setStyle("-fx-background-color: linear-gradient(to right, #FFEB3B, #FFD740); -fx-text-fill: #FF6F00; -fx-font-size: 10; " +
                    "-fx-padding: 3 8; -fx-background-radius: 10;");
            titleRow.getChildren().addAll(titleLabel, badge);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        Label descriptionLabel = new Label(achievement.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #8d9dad; -fx-wrap-text: true;");
        descriptionLabel.setMaxWidth(180);

        VBox progressContainer = new VBox(5);
        HBox progressLabels = new HBox();
        progressLabels.setAlignment(Pos.CENTER);
        HBox.setHgrow(progressLabels, Priority.ALWAYS);

        Label progressText = new Label("Progress");
        progressText.setStyle("-fx-font-size: 11; -fx-text-fill: #8d9dad;");

        Label progressValue = new Label(achievement.getProgress() + "/" + achievement.getMaxProgress());
        progressValue.setStyle("-fx-font-size: 11; -fx-text-fill: #8d9dad;");

        progressLabels.getChildren().addAll(progressText, progressValue);
        HBox.setHgrow(progressValue, Priority.ALWAYS);
        HBox.setMargin(progressValue, new Insets(0, 0, 0, 0));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(achievement.getProgress() / (double) achievement.getMaxProgress());
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: " + achievement.getColor() + "; -fx-background-color: #e0e0e0;");

        progressContainer.getChildren().addAll(progressLabels, progressBar);

        textContent.getChildren().addAll(titleRow, descriptionLabel, progressContainer);
        header.getChildren().addAll(iconContainer, textContent);

        card.getChildren().add(header);
        return card;
    }

    private VBox createProgressSummary() {
        VBox summary = new VBox(15);
        summary.setPadding(new Insets(25));
        summary.setAlignment(Pos.CENTER);
        summary.setStyle("-fx-background-color: linear-gradient(to bottom right, #D4C2FF, #E2D6FF); " +
                "-fx-background-radius: 20; -fx-border-color: #A28BF0; -fx-border-width: 2; " +
                "-fx-border-radius: 20;");
        summary.setPrefWidth(600);

        Label trophyIcon = new Label("🏆");
        trophyIcon.setStyle("-fx-font-size: 32;");

        Label title = new Label("Achievement Hunter");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        long unlockedCount = controller.getAchievements().stream().filter(AnalyticsController.Achievement::isUnlocked).count();
        Label subtitle = new Label("You've unlocked " + unlockedCount + " out of " + controller.getAchievements().size() + " achievements!");
        subtitle.setStyle("-fx-font-size: 14; -fx-text-fill: #8d9dad; -fx-text-alignment: center;");

        ProgressBar overallProgress = new ProgressBar();
        overallProgress.setProgress(unlockedCount / (double) controller.getAchievements().size());
        overallProgress.setPrefWidth(400);
        overallProgress.setStyle("-fx-accent: #A28BF0; -fx-background-color: #e0e0e0; -fx-pref-height: 8;");

        summary.getChildren().addAll(trophyIcon, title, subtitle, overallProgress);
        return summary;
    }

    private void drawPlaceholderChart(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 16));
        gc.fillText("No data available yet. Keep using the app!", width/2 - 120, height/2);
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}