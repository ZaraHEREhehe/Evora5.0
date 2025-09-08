package com.example.demo1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Dashboard {

    private Stage stage;

    public Dashboard(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("Welcome to Your Dashboard! 🌸");
        title.setFont(new Font("Arial", 24));
        Label subtitle = new Label("Ready to make today productive and fun?");
        subtitle.setTextFill(Color.GRAY);

        VBox headerBox = new VBox(5, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Quick Stats Section
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("12", "Tasks Completed", Color.LIGHTGREEN),
                createStatCard("4", "Pomodoros Today", Color.ORANGE),
                createStatCard("8", "Notes Created", Color.GOLD),
                createStatCard("😊", "Mood Score", Color.PINK)
        );

        // Quick Actions Section
        VBox quickActionsBox = new VBox(10);
        quickActionsBox.setAlignment(Pos.CENTER);
        Label quickActionsTitle = new Label("Quick Actions");
        quickActionsTitle.setFont(new Font("Arial", 18));

        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER);
        actionsRow.getChildren().addAll(
                createActionButton("Add New Task", () -> navigate("todos")),
                createActionButton("Start Timer", () -> navigate("timer")),
                createActionButton("Create Note", () -> navigate("notes")),
                createActionButton("Visit Pet", () -> navigate("pet"))
        );

        quickActionsBox.getChildren().addAll(quickActionsTitle, actionsRow);

        // Productivity Insights Section
        HBox insightsBox = new HBox(20);
        insightsBox.setAlignment(Pos.CENTER);

        VBox focusBox = createFocusBox();
        VBox analyticsBox = createAnalyticsBox();

        insightsBox.getChildren().addAll(focusBox, analyticsBox);

        // Add everything to root
        root.getChildren().addAll(headerBox, statsBox, quickActionsBox, insightsBox);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("Dashboard");
        stage.show();
    }

    // Helper methods
    private VBox createStatCard(String value, String label, Color color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setBackground(new Background(new BackgroundFill(color.deriveColor(1, 1, 1, 0.3),
                new CornerRadii(15), Insets.EMPTY)));
        card.setPrefSize(150, 120);

        Label valueLabel = new Label(value);
        valueLabel.setFont(new Font("Arial", 22));
        Label descLabel = new Label(label);
        descLabel.setTextFill(Color.DARKGRAY);

        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }

    private Button createActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefSize(150, 60);
        button.setStyle("-fx-background-color: linear-gradient(to right, #ff9a9e, #fad0c4); "
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private VBox createFocusBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK,
                new CornerRadii(15), Insets.EMPTY)));

        Label title = new Label("🎯 Today's Focus");
        title.setFont(new Font("Arial", 18));

        VBox mainGoal = new VBox(5,
                new Label("Main Goal:"),
                new Label("Complete the quarterly project presentation"));
        mainGoal.setPadding(new Insets(10));
        mainGoal.setBackground(new Background(new BackgroundFill(Color.LAVENDER,
                new CornerRadii(10), Insets.EMPTY)));

        HBox tasksBox = new HBox(10,
                new VBox(new Label("✅ Priority Tasks"), new Label("3 high-priority items")),
                new VBox(new Label("⏰ Time Goal"), new Label("6 pomodoro sessions"))
        );
        tasksBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(title, mainGoal, tasksBox);
        return box;
    }

    private VBox createAnalyticsBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,
                new CornerRadii(15), Insets.EMPTY)));

        Label title = new Label("📊 Productivity Insights");
        title.setFont(new Font("Arial", 18));

        VBox weeklyStats = new VBox(
                new Label("This Week: 90% Task completion rate"),
                new Label("↗ +12% vs last week")
        );
        weeklyStats.setPadding(new Insets(10));
        weeklyStats.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                new CornerRadii(10), Insets.EMPTY)));

        HBox miniStats = new HBox(10,
                new VBox(new Label("23"), new Label("Focus sessions")),
                new VBox(new Label("5"), new Label("Day streak"))
        );
        miniStats.setAlignment(Pos.CENTER);

        Button analyticsBtn = new Button("Full Analytics Dashboard →");
        analyticsBtn.setStyle("-fx-background-color: linear-gradient(to right, #a18cd1, #fbc2eb); "
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        analyticsBtn.setOnAction(e -> navigate("stats"));

        box.getChildren().addAll(title, weeklyStats, miniStats, analyticsBtn);
        return box;
    }

    // Placeholder for navigation logic
    private void navigate(String tab) {
        System.out.println("Navigating to: " + tab);
    }
}
