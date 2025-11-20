package com.example.demo1.Analytics;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AnalyticsView {

    private final AnalyticsController controller;
    private VBox mainContent;
    private TabPane tabPane;

    public AnalyticsView(int userId, String userName) {
        this.controller = new AnalyticsController(userId, userName);
    }

    public Node create() {
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #f0f8ff 0%, #e6f2ff 50%, #dcedff 100%);");

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
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: linear-gradient(from 0% 0% to 100% 100%, #FF9FB5 0%, #A28BF0 50%, #8fb8ff 100%);");

        Label subtitle = new Label("Track your progress and celebrate achievements");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: #8d9dad;");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        return header;
    }

    private HBox createTimeRangeSelector() {
        HBox selector = new HBox(10);
        selector.setAlignment(Pos.CENTER_RIGHT);

        ToggleGroup timeGroup = new ToggleGroup();

        String[] timeRanges = {"Week", "Month", "Year"};
        for (String range : timeRanges) {
            ToggleButton button = new ToggleButton(range);
            button.setToggleGroup(timeGroup);
            button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12;");

            if (range.equals("Week")) {
                button.setSelected(true);
                button.setStyle(button.getStyle() + " -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FF9FB5 0%, #ff8fa9 50%, #ff7f9d 100%); -fx-text-fill: white;");
            } else {
                button.setStyle(button.getStyle() + " -fx-background-color: transparent; -fx-border-color: " + controller.getColors().get("pink") + "; -fx-text-fill: " + controller.getColors().get("pink") + ";");
            }

            button.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FF9FB5 0%, #ff8fa9 50%, #ff7f9d 100%); -fx-text-fill: white;");
                    // Update controller with new time range
                    controller.setTimeRange(button.getText().toLowerCase());
                    // Refresh the tab content
                    refreshTabContent();
                } else {
                    button.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: " + controller.getColors().get("pink") + "; -fx-text-fill: " + controller.getColors().get("pink") + ";");
                }
            });

            selector.getChildren().add(button);
        }

        return selector;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab overviewTab = new Tab("Overview", createOverviewContent());
        Tab productivityTab = new Tab("Tasks & Focus", createProductivityContent());
        Tab wellbeingTab = new Tab("Wellbeing", createWellbeingContent());
        Tab achievementsTab = new Tab("Achievements", createAchievementsContent());

        // Style the tabs
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

        int completionRate = (int) ((controller.tasksCompletedProperty().get() / (double) controller.totalTasksProperty().get()) * 100);
        int focusHours = controller.focusMinutesProperty().get() / 60;
        int focusMinutesRemaining = controller.focusMinutesProperty().get() % 60;

        // Task Completion Card
        VBox taskCard = createStatCard(
                "Task Completion",
                completionRate + "%",
                controller.tasksCompletedProperty().get() + " of " + controller.totalTasksProperty().get() + " tasks",
                "linear-gradient(from 0% 0% to 100% 100%, #FF9FB5 0%, #ff8fa9 50%, #ff7f9d 100%)",
                "✓"
        );

        // Focus Time Card
        VBox focusCard = createStatCard(
                "Focus Time",
                focusHours + "h " + focusMinutesRemaining + "m",
                controller.pomodoroSessionsProperty().get() + " sessions",
                "linear-gradient(from 0% 0% to 100% 100%, #B5E5FF 0%, #a0daff 50%, #8bcfff 100%)",
                "⏰"
        );

        // Current Streak Card
        VBox streakCard = createStatCard(
                "Current Streak",
                controller.currentStreakProperty().get() + " days",
                "Best: " + controller.longestStreakProperty().get() + " days",
                "linear-gradient(from 0% 0% to 100% 100%, #A28BF0 0%, #9378e6 50%, #8465dc 100%)",
                "⚡"
        );

        // Coins Earned Card
        VBox coinsCard = createStatCard(
                "Coins Earned",
                String.valueOf(controller.coinsEarnedProperty().get()),
                "Pet happiness: " + controller.petHappinessProperty().get() + "%",
                "linear-gradient(from 0% 0% to 100% 100%, #B5E5B5 0%, #a0d8a0 50%, #8bcb8b 100%)",
                "⭐"
        );

        grid.add(taskCard, 0, 0);
        grid.add(focusCard, 1, 0);
        grid.add(streakCard, 0, 1);
        grid.add(coinsCard, 1, 1);

        return grid;
    }

    private VBox createStatCard(String title, String value, String subtitle, String gradient, String emoji) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + gradient + "; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefSize(200, 120);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        VBox textContent = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #2A2D3A; -fx-font-size: 14; -fx-font-weight: bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #2A2D3A; -fx-font-size: 24; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #2A2D3A; -fx-font-size: 12;");

        textContent.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24;");

        header.getChildren().addAll(textContent, emojiLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        card.getChildren().add(header);
        return card;
    }

    private VBox createActivityChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(20));
        chartContainer.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fbff 50%, #f0f8ff 100%); " +
                "-fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        String timeRange = controller.getCurrentTimeRange();
        String chartTitle = getChartTitle(timeRange);

        Label title = new Label(chartTitle);
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        // Create line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setLegendVisible(true);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(300);
        lineChart.setStyle("-fx-background-color: transparent; -fx-legend-visible: true;");

        // Remove chart background and grid lines
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);

        // Tasks series
        XYChart.Series<String, Number> tasksSeries = new XYChart.Series<>();
        tasksSeries.setName("Tasks Completed");

        // Pomodoro series
        XYChart.Series<String, Number> pomodoroSeries = new XYChart.Series<>();
        pomodoroSeries.setName("Pomodoro Sessions");

        for (AnalyticsController.WeeklyData data : controller.getWeeklyData()) {
            tasksSeries.getData().add(new XYChart.Data<>(data.getDay(), data.getTasks()));
            pomodoroSeries.getData().add(new XYChart.Data<>(data.getDay(), data.getPomodoros()));
        }

        lineChart.getData().addAll(tasksSeries, pomodoroSeries);

        // Style the lines
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Circle circle = new Circle(6);
                if (series.getName().equals("Tasks Completed")) {
                    circle.setFill(Color.web(controller.getColors().get("pink")));
                } else {
                    circle.setFill(Color.web(controller.getColors().get("blue")));
                }
                data.setNode(circle);
            }
        }

        chartContainer.getChildren().addAll(title, lineChart);
        return chartContainer;
    }

    private String getChartTitle(String timeRange) {
        switch (timeRange) {
            case "month":
                return "Monthly Activity";
            case "year":
                return "Yearly Activity";
            case "week":
            default:
                return "Weekly Activity";
        }
    }

    private Node createProductivityContent() {
        VBox content = new VBox(20);

        // Charts row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);

        VBox taskChart = createBarChart("Task Completion", "tasks", controller.getColors().get("pink"));
        VBox focusChart = createBarChart("Focus Sessions", "pomodoros", controller.getColors().get("blue"));

        chartsRow.getChildren().addAll(taskChart, focusChart);

        // Stats cards
        HBox statsRow = createProductivityStats();

        content.getChildren().addAll(chartsRow, statsRow);
        return content;
    }

    private VBox createBarChart(String title, String dataType, String color) {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(20));
        chartContainer.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fbff 50%, #f0f8ff 100%); " +
                "-fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        chartContainer.setPrefWidth(400);

        Label chartTitle = new Label(title);
        chartTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(250);
        barChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (AnalyticsController.WeeklyData data : controller.getWeeklyData()) {
            Number value = dataType.equals("tasks") ? data.getTasks() : data.getPomodoros();
            series.getData().add(new XYChart.Data<>(data.getDay(), value));
        }

        barChart.getData().add(series);

        // Style the bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        chartContainer.getChildren().addAll(chartTitle, barChart);
        return chartContainer;
    }

    private HBox createProductivityStats() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        // Calculate average session length (25 minutes per pomodoro)
        String avgSession = "25 min";
        String sessionNote = "Standard pomodoro length";

        // Calculate goal achievement based on current time range
        int totalPossible = 0;
        int actualCompleted = 0;

        for (AnalyticsController.WeeklyData data : controller.getWeeklyData()) {
            actualCompleted += data.getTasks();
            // Estimate potential tasks based on time range
            if (controller.getCurrentTimeRange().equals("week")) {
                totalPossible += 3; // Assume 3 tasks per day is achievable
            } else if (controller.getCurrentTimeRange().equals("month")) {
                totalPossible += 15; // Assume 15 tasks per week
            } else {
                totalPossible += 60; // Assume 60 tasks per month
            }
        }

        int goalPercentage = totalPossible > 0 ? (actualCompleted * 100) / totalPossible : 0;
        goalPercentage = Math.min(goalPercentage, 100);

        // Calculate productivity trend (simplified - compare first half vs second half)
        int trend = calculateProductivityTrend();

        VBox avgSessionCard = createMiniStatCard("⏰", "Average Focus Session", avgSession, sessionNote, "linear-gradient(from 0% 0% to 100% 100%, #B5E5FF 0%, #a0daff 50%, #8bcfff 100%)");
        VBox goalCard = createMiniStatCard("🎯", "Goal Achievement", goalPercentage + "%", "Based on your capacity", "linear-gradient(from 0% 0% to 100% 100%, #B5E5B5 0%, #a0d8a0 50%, #8bcb8b 100%)");
        VBox trendCard = createMiniStatCard("📈", "Productivity Trend", getTrendArrow(trend), getTrendDescription(trend), "linear-gradient(from 0% 0% to 100% 100%, #A28BF0 0%, #9378e6 50%, #8465dc 100%)");

        statsRow.getChildren().addAll(avgSessionCard, goalCard, trendCard);
        return statsRow;
    }

    private int calculateProductivityTrend() {
        // Simple trend calculation: compare first half vs second half of data
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

        if (firstHalfTasks == 0) return 100; // Avoid division by zero

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

    private VBox createMiniStatCard(String emoji, String title, String value, String subtitle, String gradient) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: " + gradient + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);");
        card.setPrefSize(180, 120);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2A2D3A; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2A2D3A;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2A2D3A; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private Node createWellbeingContent() {
        VBox content = new VBox(20);

        // Charts row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);

        VBox moodDistributionChart = createMoodDistributionChart();
        VBox moodTrendChart = createMoodTrendChart();

        chartsRow.getChildren().addAll(moodDistributionChart, moodTrendChart);

        // Stats cards
        HBox statsRow = createWellbeingStats();

        content.getChildren().addAll(chartsRow, statsRow);
        return content;
    }

    private VBox createMoodDistributionChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(20));
        chartContainer.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fbff 50%, #f0f8ff 100%); " +
                "-fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        chartContainer.setPrefWidth(400);

        Label title = new Label("Mood Distribution");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefHeight(250);

        for (AnalyticsController.MoodDistribution mood : controller.getMoodDistribution()) {
            PieChart.Data slice = new PieChart.Data(mood.getMood() + ": " + mood.getValue() + "%", mood.getValue());
            pieChart.getData().add(slice);

            // Set colors
            slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + mood.getColor() + ";");
                }
            });
        }

        chartContainer.getChildren().addAll(title, pieChart);
        return chartContainer;
    }

    private VBox createMoodTrendChart() {
        VBox chartContainer = new VBox(15);
        chartContainer.setPadding(new Insets(20));
        chartContainer.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fbff 50%, #f0f8ff 100%); " +
                "-fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        chartContainer.setPrefWidth(400);

        String timeRange = controller.getCurrentTimeRange();
        String titleText = getMoodTrendTitle(timeRange);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(1);
        yAxis.setUpperBound(5);
        yAxis.setTickUnit(1);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setPrefHeight(250);
        lineChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (AnalyticsController.WeeklyData data : controller.getWeeklyData()) {
            series.getData().add(new XYChart.Data<>(data.getDay(), data.getMood()));
        }

        lineChart.getData().add(series);

        // Style the line
        for (XYChart.Data<String, Number> data : series.getData()) {
            Circle circle = new Circle(6);
            circle.setFill(Color.web(controller.getColors().get("purple")));
            data.setNode(circle);
        }

        chartContainer.getChildren().addAll(title, lineChart);
        return chartContainer;
    }

    private String getMoodTrendTitle(String timeRange) {
        switch (timeRange) {
            case "month":
                return "Monthly Mood Trend";
            case "year":
                return "Yearly Mood Trend";
            case "week":
            default:
                return "Weekly Mood Trend";
        }
    }

    private HBox createWellbeingStats() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        VBox moodCard = createGradientStatCard("❤️", "Average Mood",
                String.format("%.1f/5", controller.averageMoodProperty().get()),
                getMoodTimeRangeText(),
                "linear-gradient(from 0% 0% to 100% 100%, #FF9FB5 0%, #ff8fa9 50%, #ff7f9d 100%)",
                "linear-gradient(from 0% 0% to 100% 100%, #A28BF0 0%, #9378e6 50%, #8465dc 100%)");

        VBox petCard = createGradientStatCard("⭐", "Pet Happiness",
                controller.petHappinessProperty().get() + "%",
                getPetHappinessMessage(),
                "linear-gradient(from 0% 0% to 100% 100%, #B5E5FF 0%, #a0daff 50%, #8bcfff 100%)",
                "linear-gradient(from 0% 0% to 100% 100%, #B5E5B5 0%, #a0d8a0 50%, #8bcb8b 100%)");

        statsRow.getChildren().addAll(moodCard, petCard);
        return statsRow;
    }

    private String getMoodTimeRangeText() {
        switch (controller.getCurrentTimeRange()) {
            case "month":
                return "This month";
            case "year":
                return "This year";
            case "week":
            default:
                return "This week";
        }
    }

    private String getPetHappinessMessage() {
        int happiness = controller.petHappinessProperty().get();
        if (happiness >= 90) return "Your pet adores you!";
        else if (happiness >= 70) return "Your pet is very happy!";
        else if (happiness >= 50) return "Your pet is content";
        else return "Your pet needs attention";
    }

    private VBox createGradientStatCard(String emoji, String title, String value, String subtitle, String gradient1, String gradient2) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + gradient1 + "; " +
                "-fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPrefSize(250, 100);

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24;");

        VBox textContent = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2A2D3A;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2A2D3A;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2A2D3A;");

        textContent.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        content.getChildren().addAll(emojiLabel, textContent);

        card.getChildren().add(content);
        return card;
    }

    private Node createAchievementsContent() {
        VBox content = new VBox(20);

        // Achievements grid
        FlowPane achievementsGrid = new FlowPane();
        achievementsGrid.setHgap(15);
        achievementsGrid.setVgap(15);
        achievementsGrid.setPrefWrapLength(800);

        for (AnalyticsController.Achievement achievement : controller.getAchievements()) {
            VBox achievementCard = createAchievementCard(achievement);
            achievementsGrid.getChildren().add(achievementCard);
        }

        // Progress summary
        VBox progressSummary = createProgressSummary();

        content.getChildren().addAll(achievementsGrid, progressSummary);
        return content;
    }

    private VBox createAchievementCard(AnalyticsController.Achievement achievement) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));

        String cardStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8f8f8 50%, #f0f0f0 100%); " +
                "-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);";

        if (achievement.isUnlocked()) {
            cardStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFF9C4 0%, #FFECB3 50%, #FFE082 100%); " +
                    "-fx-background-radius: 15; -fx-border-color: #FFD54F; -fx-border-width: 2; " +
                    "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);";
        }

        card.setStyle(cardStyle);
        card.setPrefSize(250, 150);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon container
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle("-fx-background-color: " + (achievement.isUnlocked() ?
                "linear-gradient(from 0% 0% to 100% 100%, #FFD54F 0%, #FFC107 50%, #FFB300 100%)" :
                achievement.getColor()) + "; -fx-background-radius: 10; -fx-padding: 8;");
        iconContainer.setMinSize(40, 40);

        Label iconLabel = new Label(achievement.isUnlocked() ? "🏆" : achievement.getEmoji());
        iconLabel.setStyle("-fx-font-size: 16;");
        iconContainer.getChildren().add(iconLabel);

        VBox textContent = new VBox(5);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(achievement.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #6d7d8d;");

        if (achievement.isUnlocked()) {
            Label badge = new Label("Unlocked");
            badge.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFEB3B 0%, #FFD740 50%, #FFC400 100%); -fx-text-fill: #FF6F00; -fx-font-size: 10; " +
                    "-fx-padding: 2 6; -fx-background-radius: 10;");
            titleRow.getChildren().addAll(titleLabel, badge);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        Label descriptionLabel = new Label(achievement.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #8d9dad;");
        descriptionLabel.setWrapText(true);

        // Progress bar
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
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + achievement.getColor() + "; -fx-background-color: #e0e0e0;");

        progressContainer.getChildren().addAll(progressLabels, progressBar);

        textContent.getChildren().addAll(titleRow, descriptionLabel, progressContainer);
        header.getChildren().addAll(iconContainer, textContent);

        card.getChildren().add(header);
        return card;
    }

    private VBox createProgressSummary() {
        VBox summary = new VBox(15);
        summary.setPadding(new Insets(20));
        summary.setAlignment(Pos.CENTER);
        summary.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #D4C2FF 0%, #E2D6FF 50%, #F0D2F7 100%); " +
                "-fx-background-radius: 20; -fx-border-color: " + controller.getColors().get("purple") + "; -fx-border-width: 2; " +
                "-fx-border-radius: 20;");

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
        overallProgress.setStyle("-fx-accent: " + controller.getColors().get("purple") + "; -fx-background-color: #e0e0e0; -fx-pref-height: 8;");

        summary.getChildren().addAll(trophyIcon, title, subtitle, overallProgress);
        return summary;
    }
}