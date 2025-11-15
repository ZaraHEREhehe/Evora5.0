package com.example.demo1;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Dashboard {

    private VBox mainContent;
    private SidebarController sidebarController;

    public Dashboard() {
        this.sidebarController = new SidebarController();
        createContent();
    }

    public Dashboard(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
        createContent();
    }

    public void createContent() {


        // ✅ Main Dashboard content
         mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setBackground(new Background(new BackgroundFill(
                Color.web("#fdf7ff"), CornerRadii.EMPTY, Insets.EMPTY)));

        // 🌸 Title Section
        Label title = new Label("Welcome to Your Dashboard! 🌸");
        title.setFont(Font.font("Poppins", 28));
        title.setTextFill(Color.web("#5c5470"));

        Label subtitle = new Label("Ready to make today productive and fun?");
        subtitle.setFont(Font.font("Poppins", 16));
        subtitle.setTextFill(Color.web("#9189a5"));

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // 🌈 Quick Stats
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("✅", "12", "Tasks Completed", "#a8e6cf", "#dcedc1"),
                createStatCard("⏰", "4", "Pomodoros Today", "#ffd3b6", "#ffaaa5"),
                createStatCard("📝", "8", "Notes Created", "#fff5ba", "#ffe8a3"),
                createStatCard("💖", "😊", "Mood Score", "#ffb6b9", "#fae3d9")
        );

        // ⚡ Quick Actions
        VBox quickActionsBox = new VBox(12);
        quickActionsBox.setAlignment(Pos.CENTER);
        Label quickActionsTitle = new Label("Quick Actions");
        quickActionsTitle.setFont(Font.font("Poppins", 20));
        quickActionsTitle.setTextFill(Color.web("#5c5470"));

        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER);
        actionsRow.getChildren().addAll(
                createGradientButton("Add New Task", "#a8e6cf", "#dcedc1", () -> sidebarController.navigate("todos")),
                createGradientButton("Start Timer", "#ffd3b6", "#ffaaa5", () -> sidebarController.navigate("timer")),
                createGradientButton("Create Note", "#fff5ba", "#ffe8a3", () -> sidebarController.navigate("notes")),
                createGradientButton("Visit Pet", "#ffb6b9", "#fae3d9", () -> sidebarController.navigate("pet"))
        );

        quickActionsBox.getChildren().addAll(quickActionsTitle, actionsRow);

        // 📊 Insights Section
        HBox insightsBox = new HBox(25);
        insightsBox.setAlignment(Pos.CENTER);
        insightsBox.getChildren().addAll(createFocusBox(), createAnalyticsBox());

        mainContent.getChildren().addAll(headerBox, statsBox, quickActionsBox, insightsBox);

      //  root.setCenter(mainContent);

        //Scene scene = new Scene(root, 1200, 700);
        //stage.setScene(scene);
        //stage.setTitle("Pastel Productivity Dashboard");
        //stage.show();
    }

    // 🌼 Stat Cards
    private VBox createStatCard(String emoji, String value, String label, String startColor, String endColor) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(160, 130);
        card.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, null,
                        new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor))),
                new CornerRadii(25), Insets.EMPTY)));

        DropShadow shadow = new DropShadow(10, Color.gray(0, 0.3));
        card.setEffect(shadow);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(26));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Poppins", 26));
        valueLabel.setTextFill(Color.web("#5c5470"));

        Label descLabel = new Label(label);
        descLabel.setTextFill(Color.web("#756f86"));
        descLabel.setFont(Font.font("Poppins", 13));

        card.getChildren().addAll(emojiLabel, valueLabel, descLabel);

        addHoverAnimation(card);
        return card;
    }

    public VBox getContent() {
        return mainContent;
    }

    private void navigateTo(String tab) {
        if (sidebarController != null) {
            sidebarController.navigate(tab);
        }
    }

    private Button createGradientButton(String text, String startColor, String endColor, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Poppins", 13));
        button.setTextFill(Color.WHITE);
        button.setPrefSize(160, 60);
        button.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, null,
                        new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor))),
                new CornerRadii(20), Insets.EMPTY)));
        button.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
        button.setOnAction(e -> action.run());
        addHoverAnimation(button);
        return button;
    }

    private VBox createFocusBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setBackground(new Background(new BackgroundFill(Color.web("#fae3d9"), new CornerRadii(20), Insets.EMPTY)));
        box.setEffect(new DropShadow(10, Color.gray(0, 0.3)));

        Label title = new Label("🎯 Today's Focus");
        title.setFont(Font.font("Poppins", 20));
        title.setTextFill(Color.web("#5c5470"));

        VBox mainGoal = new VBox(5,
                new Label("Main Goal"),
                new Label("Complete the quarterly project presentation")
        );
        mainGoal.setPadding(new Insets(15));
        mainGoal.setBackground(new Background(new BackgroundFill(Color.web("#f6f0ff"),
                new CornerRadii(15), Insets.EMPTY)));
        mainGoal.setStyle("-fx-font-family: 'Poppins'; -fx-text-fill: #756f86;");

        HBox subGoals = new HBox(10,
                createMiniCard("✅ Priority Tasks", "3 high-priority items", "#dcedc1", "#a8e6cf"),
                createMiniCard("⏰ Time Goal", "6 pomodoro sessions", "#ffd3b6", "#ffaaa5")
        );
        subGoals.setAlignment(Pos.CENTER);

        box.getChildren().addAll(title, mainGoal, subGoals);
        return box;
    }

    private VBox createAnalyticsBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(400);
        box.setBackground(new Background(new BackgroundFill(Color.web("#f3e5f5"), new CornerRadii(20), Insets.EMPTY)));
        box.setEffect(new DropShadow(10, Color.gray(0, 0.3)));

        Label title = new Label("📊 Productivity Insights");
        title.setFont(Font.font("Poppins", 20));
        title.setTextFill(Color.web("#5c5470"));

        VBox weekly = createMiniCard("This Week", "90% Task completion rate", "#c8e6c9", "#81c784");
        Label change = new Label("↗ +12% vs last week");
        change.setFont(Font.font("Poppins", 13));
        change.setTextFill(Color.web("#4caf50"));

        HBox miniStats = new HBox(10,
                createMiniCard("Focus Sessions", "23", "#bbdefb", "#64b5f6"),
                createMiniCard("Day Streak", "5", "#d1c4e9", "#9575cd")
        );
        miniStats.setAlignment(Pos.CENTER);

        Button analyticsBtn = createGradientButton("Full Analytics Dashboard →", "#a18cd1", "#fbc2eb",
                () -> System.out.println("Navigate to stats"));
        analyticsBtn.setPrefWidth(350);

        box.getChildren().addAll(title, weekly, change, miniStats, analyticsBtn);
        return box;
    }

    private VBox createMiniCard(String title, String desc, String startColor, String endColor) {
        VBox mini = new VBox(3);
        mini.setAlignment(Pos.CENTER);
        mini.setPadding(new Insets(10));
        mini.setPrefWidth(180);
        mini.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, null,
                        new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor))),
                new CornerRadii(15), Insets.EMPTY)));
        Label t = new Label(title);
        t.setFont(Font.font("Poppins", 13));
        t.setTextFill(Color.web("#5c5470"));
        Label d = new Label(desc);
        d.setFont(Font.font("Poppins", 12));
        d.setTextFill(Color.web("#756f86"));
        mini.getChildren().addAll(t, d);
        return mini;
    }

    private void addHoverAnimation(Region region) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), region);
        region.setOnMouseEntered(e -> {
            st.setToX(1.05);
            st.setToY(1.05);
            st.playFromStart();
        });
        region.setOnMouseExited(e -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.playFromStart();
        });
    }

    private void navigate(String tab) {
        System.out.println("Navigating to: " + tab);
    }
}
