package com.example.demo1.Mood;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MoodView extends BorderPane {
    private final MoodController controller;
    private Integer currentMood = null;
    private TextArea noteTextArea;
    private VBox mainContent;
    private Canvas chartCanvas;
    private VBox moodSelectionCard;
    private HBox statsCards;
    private VBox recentEntriesCard;
    private Tooltip chartTooltip;

    // Color palette - Softer text colors
    private final Color[] gradientColors = {
            Color.web("#C084FC"), // Purple
            Color.web("#F472B6")  // Pink
    };
    private final Color bgColor = Color.web("#fdf7ff");
    private final Color cardBg = Color.web("#FFFFFF");
    private final Color textPrimary = Color.web("#5c5470"); // Softer dark color
    private final Color textSecondary = Color.web("#756f86"); // Softer secondary
    private final Color borderColor = Color.web("#D8B4FE");

    public MoodView(MoodController controller) {
        this.controller = controller;
        this.chartTooltip = new Tooltip();
        this.chartTooltip.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        createView();
        refreshAllData();
    }

    // Helper method to force text color with !important (ONLY for white backgrounds)
    private String forceTextColor(Color color) {
        return "-fx-text-fill: " + toHex(color) + " !important;";
    }

    private void createView() {
        mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: #fdf7ff;");

        // Header
        VBox headerBox = createHeader();

        // Create cards containers with proper alignment
        moodSelectionCard = new VBox();
        moodSelectionCard.setAlignment(Pos.CENTER);

        statsCards = new HBox(25);
        statsCards.setAlignment(Pos.CENTER);

        VBox chartCard = createChartCard();
        chartCard.setAlignment(Pos.CENTER);

        recentEntriesCard = new VBox();
        recentEntriesCard.setAlignment(Pos.CENTER);

        // Center all cards in the main content
        VBox.setMargin(moodSelectionCard, new Insets(0, 0, 0, 0));
        VBox.setMargin(statsCards, new Insets(0, 0, 0, 0));
        VBox.setMargin(chartCard, new Insets(0, 0, 0, 0));
        VBox.setMargin(recentEntriesCard, new Insets(0, 0, 0, 0));

        mainContent.getChildren().addAll(headerBox, moodSelectionCard, statsCards, chartCard, recentEntriesCard);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.setCenter(scrollPane);
        this.setStyle("-fx-background-color: #fdf7ff;");

        // Initial data load
        refreshAllData();
    }

    private VBox createHeader() {
        Label title = new Label("Mood Logger 😊");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle(forceTextColor(textPrimary)); // FORCE with !important (white background)

        Label subtitle = new Label("How are you feeling today?");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle(forceTextColor(textSecondary)); // FORCE with !important (white background)

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        return headerBox;
    }

    private void refreshAllData() {
        refreshMoodSelectionCard();
        refreshStatisticsCards();
        refreshRecentEntriesCard();
        drawMoodChart();
    }

    private void refreshMoodSelectionCard() {
        moodSelectionCard.getChildren().clear();

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(700);
        card.setAlignment(Pos.TOP_CENTER);

        // Card header with better instructions
        MoodController.MoodEntry todaysMood = controller.getTodaysMood();
        Label cardTitle = new Label(todaysMood != null ? "Update Today's Mood" : "Log Your Mood");
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        cardTitle.setStyle(forceTextColor(textPrimary)); // FORCE with !important (white background)

        Label instructionLabel = new Label("Select how you're feeling today:");
        instructionLabel.setFont(Font.font("System", 14));
        instructionLabel.setStyle(forceTextColor(textSecondary)); // FORCE with !important (white background)

        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(cardTitle, instructionLabel);

        if (todaysMood != null) {
            Label currentMoodLabel = new Label("Current: " +
                    controller.getMoodEmojis()[todaysMood.getMoodValue() - 1] + " " +
                    controller.getMoodLabels()[todaysMood.getMoodValue() - 1]);
            currentMoodLabel.setFont(Font.font("System", 16));
            currentMoodLabel.setStyle(forceTextColor(textSecondary)); // FORCE with !important (white background)
            headerBox.getChildren().add(currentMoodLabel);
        }

        // Mood buttons
        HBox moodButtons = createMoodButtons(card);

        // Mood labels
        HBox moodLabels = createMoodLabels();

        card.getChildren().addAll(headerBox, moodButtons, moodLabels);

        // Center the card in its container
        VBox cardContainer = new VBox(card);
        cardContainer.setAlignment(Pos.CENTER);
        moodSelectionCard.getChildren().add(cardContainer);
    }

    private HBox createMoodButtons(VBox card) {
        HBox moodButtons = new HBox(20);
        moodButtons.setAlignment(Pos.CENTER);

        String[] emojis = controller.getMoodEmojis();
        for (int i = 0; i < emojis.length; i++) {
            final int moodValue = i + 1;
            Button moodButton = new Button(emojis[i]);
            moodButton.setFont(Font.font(28));
            moodButton.setPrefSize(70, 70);
            moodButton.setMinSize(70, 70);
            moodButton.setMaxSize(70, 70);

            updateMoodButtonStyle(moodButton, currentMood != null && moodValue == currentMood);

            moodButton.setOnMouseEntered(e -> {
                if (currentMood == null || moodValue != currentMood) {
                    moodButton.setStyle(
                            "-fx-background-radius: 50%;" +
                                    "-fx-border-radius: 50%;" +
                                    "-fx-border-width: 3;" +
                                    "-fx-border-color: #C084FC;" +
                                    "-fx-background-color: #F3E8FF;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(192, 132, 252, 0.4), 10, 0.5, 0, 2);"
                    );
                }
            });

            moodButton.setOnMouseExited(e -> {
                updateMoodButtonStyle(moodButton, currentMood != null && moodValue == currentMood);
            });

            moodButton.setOnAction(e -> {
                currentMood = moodValue;
                updateAllMoodButtons(moodButtons, moodValue);
                showNoteSection(card);
            });

            moodButtons.getChildren().add(moodButton);
        }
        return moodButtons;
    }

    private void updateMoodButtonStyle(Button button, boolean isSelected) {
        if (isSelected) {
            button.setStyle(
                    "-fx-background-radius: 50%;" +
                            "-fx-border-radius: 50%;" +
                            "-fx-border-width: 3;" +
                            "-fx-border-color: #8B5CF6;" +
                            "-fx-background-color: linear-gradient(to right, #C084FC, #F472B6);" +
                            "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.6), 15, 0.5, 0, 6);" +
                            "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                    "-fx-background-radius: 50%;" +
                            "-fx-border-radius: 50%;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-color: #D1D5DB;" +
                            "-fx-background-color: #F8FAFC;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
            );
        }
    }

    private void updateAllMoodButtons(HBox moodButtons, int selectedMood) {
        for (int i = 0; i < moodButtons.getChildren().size(); i++) {
            Button button = (Button) moodButtons.getChildren().get(i);
            int moodValue = i + 1;
            updateMoodButtonStyle(button, moodValue == selectedMood);
        }
    }

    private HBox createMoodLabels() {
        HBox moodLabels = new HBox(20);
        moodLabels.setAlignment(Pos.CENTER);
        String[] labels = controller.getMoodLabels();
        for (String label : labels) {
            Label moodLabel = new Label(label);
            moodLabel.setFont(Font.font("System", 12));
            moodLabel.setStyle(forceTextColor(textSecondary)); // FORCE with !important (white background)
            moodLabel.setPrefWidth(70);
            moodLabel.setAlignment(Pos.CENTER);
            moodLabels.getChildren().add(moodLabel);
        }
        return moodLabels;
    }

    private void showNoteSection(VBox card) {
        // Remove existing note section if present
        if (card.getChildren().size() > 3) {
            card.getChildren().remove(3, card.getChildren().size());
        }

        VBox noteSection = new VBox(15);
        noteSection.setAlignment(Pos.CENTER);
        noteSection.setMaxWidth(500);

        noteTextArea = new TextArea();
        noteTextArea.setPromptText("How was your day? (optional)");
        noteTextArea.setPrefRowCount(4);
        noteTextArea.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #D8B4FE;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;" +
                        "-fx-font-family: 'System';" +
                        "-fx-font-size: 14;"
        );

        Button logButton = new Button(controller.getTodaysMood() != null ? "Update Mood" : "Log Mood");
        logButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #C084FC, #F472B6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'System';" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 12 40;" +
                        "-fx-font-size: 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.4), 10, 0.5, 0, 3);"
        );
        logButton.setOnAction(e -> logMood());

        noteSection.getChildren().addAll(noteTextArea, logButton);
        card.getChildren().add(noteSection);
    }

    private void refreshStatisticsCards() {
        statsCards.getChildren().clear();

        // Average Mood Card
        double averageMood = controller.getAverageMood();
        String averageEmoji;
        if (controller.getMoodHistory().isEmpty()) {
            averageEmoji = "😐";
        } else {
            int averageMoodIndex = Math.max(0, Math.min((int) Math.round(averageMood) - 1, controller.getMoodEmojis().length - 1));
            averageEmoji = controller.getMoodEmojis()[averageMoodIndex];
        }

        VBox avgMoodCard = createStatCard(
                "Average Mood",
                averageEmoji,
                String.format("%.1f/5", averageMood),
                "#F3E8FF", "#FCE7F3"
        );

        // Entries Logged Card
        List<MoodController.MoodEntry> entries = controller.getMoodHistory();
        VBox entriesCard = createStatCard(
                "Entries Logged",
                "📊",
                entries.size() + " days",
                "#E0F2FE", "#BAE6FD"
        );

        statsCards.getChildren().addAll(avgMoodCard, entriesCard);
    }

    private VBox createStatCard(String title, String emoji, String value, String startColor, String endColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + darkenColor(startColor) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.5, 0, 4);"
        );
        card.setPrefSize(220, 150);
        card.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(32));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE); // White text on gradient - NO !important

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.WHITE); // White text on gradient - NO !important

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel);
        return card;
    }

    private String darkenColor(String color) {
        return color.replaceFirst("#", "#A0");
    }

    private VBox createChartCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);
        card.setAlignment(Pos.CENTER);

        Label cardTitle = new Label("Mood Trend");
        cardTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 22));
        cardTitle.setStyle(forceTextColor(textPrimary)); // FORCE with !important (white background)

        chartCanvas = new Canvas(700, 250);
        chartCanvas.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);");

        // Add mouse interaction for tooltips
        setupChartInteractivity();

        card.getChildren().addAll(cardTitle, chartCanvas);

        // Center the chart card
        VBox cardContainer = new VBox(card);
        cardContainer.setAlignment(Pos.CENTER);
        return cardContainer;
    }

    private void setupChartInteractivity() {
        chartCanvas.setOnMouseMoved(this::handleChartHover);
        chartCanvas.setOnMouseExited(e -> chartTooltip.hide());
    }

    private void handleChartHover(MouseEvent event) {
        List<MoodController.MoodEntry> entries = controller.getMoodHistory();
        if (entries.isEmpty()) return;

        double padding = 60;
        double chartWidth = chartCanvas.getWidth() - 2 * padding;
        double chartHeight = chartCanvas.getHeight() - 2 * padding;

        // Find the closest data point
        double mouseX = event.getX();
        double mouseY = event.getY();

        MoodController.MoodEntry closestEntry = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < entries.size(); i++) {
            MoodController.MoodEntry entry = entries.get(i);
            double x = padding + (chartWidth / (entries.size() - 1)) * i;
            double y = padding + chartHeight - ((entry.getMoodValue() - 1) / 4.0) * chartHeight;

            double distance = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));

            if (distance < 20 && distance < minDistance) {
                minDistance = distance;
                closestEntry = entry;
            }
        }

        if (closestEntry != null) {
            String moodLabel = controller.getMoodLabels()[closestEntry.getMoodValue() - 1];
            String date = closestEntry.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            String note = closestEntry.getNote() != null && !closestEntry.getNote().isEmpty() ?
                    "\nNote: " + closestEntry.getNote() : "";

            chartTooltip.setText(date + "\nMood: " + moodLabel + " (" + closestEntry.getMoodValue() + "/5)" + note);
            chartTooltip.show(chartCanvas, event.getScreenX() + 10, event.getScreenY() + 10);
        } else {
            chartTooltip.hide();
        }
    }

    private void drawMoodChart() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, chartCanvas.getWidth(), chartCanvas.getHeight());

        List<MoodController.MoodEntry> entries = controller.getMoodHistory();
        if (entries.isEmpty()) {
            drawPlaceholderChart(gc);
            return;
        }

        drawChartWithData(gc, entries);
    }

    private void drawPlaceholderChart(GraphicsContext gc) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 16));
        gc.fillText("No mood data yet. Log your first mood!", 220, 125);
    }

    private void drawChartWithData(GraphicsContext gc, List<MoodController.MoodEntry> entries) {
        double padding = 60;
        double chartWidth = chartCanvas.getWidth() - 2 * padding;
        double chartHeight = chartCanvas.getHeight() - 2 * padding;

        drawGrid(gc, padding, chartWidth, chartHeight, entries);
        drawYAxisLabels(gc, padding, chartHeight);
        drawMoodLine(gc, entries, padding, chartWidth, chartHeight);
        drawDataPoints(gc, entries, padding, chartWidth, chartHeight);

        // Add chart title with softer styling
        gc.setFill(Color.web("#5c5470"));
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        gc.fillText("Mood Over Time", chartCanvas.getWidth() / 2 - 45, 25);
    }

    private void drawGrid(GraphicsContext gc, double padding, double chartWidth, double chartHeight, List<MoodController.MoodEntry> entries) {
        gc.setStroke(Color.web("#E5E7EB"));
        gc.setLineWidth(1);

        // Horizontal grid lines
        for (int i = 0; i < 5; i++) {
            double y = padding + (chartHeight / 4) * i;
            gc.strokeLine(padding, y, padding + chartWidth, y);
        }

        // Vertical grid lines
        int verticalDivisions = Math.min(entries.size() - 1, 10);
        for (int i = 0; i <= verticalDivisions; i++) {
            double x = padding + (chartWidth / verticalDivisions) * i;
            gc.strokeLine(x, padding, x, padding + chartHeight);
        }
    }

    private void drawYAxisLabels(GraphicsContext gc, double padding, double chartHeight) {
        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font("System", 11));
        String[] moodLevels = {"5 - 😄", "4 - 😊", "3 - 😐", "2 - 😟", "1 - 😢"};
        for (int i = 0; i < 5; i++) {
            double y = padding + (chartHeight / 4) * i;
            gc.fillText(moodLevels[i], 10, y + 5);
        }
    }

    private void drawMoodLine(GraphicsContext gc, List<MoodController.MoodEntry> entries,
                              double padding, double chartWidth, double chartHeight) {
        gc.setStroke(gradientColors[0]);
        gc.setLineWidth(3);

        for (int i = 0; i < entries.size() - 1; i++) {
            MoodController.MoodEntry current = entries.get(i);
            MoodController.MoodEntry next = entries.get(i + 1);

            double x1 = padding + (chartWidth / (entries.size() - 1)) * i;
            double y1 = padding + chartHeight - ((current.getMoodValue() - 1) / 4.0) * chartHeight;

            double x2 = padding + (chartWidth / (entries.size() - 1)) * (i + 1);
            double y2 = padding + chartHeight - ((next.getMoodValue() - 1) / 4.0) * chartHeight;

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    private void drawDataPoints(GraphicsContext gc, List<MoodController.MoodEntry> entries,
                                double padding, double chartWidth, double chartHeight) {
        gc.setFont(Font.font("System", 9));

        for (int i = 0; i < entries.size(); i++) {
            MoodController.MoodEntry entry = entries.get(i);
            double x = padding + (chartWidth / (entries.size() - 1)) * i;
            double y = padding + chartHeight - ((entry.getMoodValue() - 1) / 4.0) * chartHeight;

            // Draw point with gradient effect
            gc.setFill(gradientColors[0]);
            gc.setStroke(Color.web("#7C3AED"));
            gc.setLineWidth(2);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.strokeOval(x - 5, y - 5, 10, 10);

            // Draw date label for selected points to avoid crowding
            if (i == 0 || i == entries.size() - 1 || i % Math.ceil(entries.size() / 5.0) == 0) {
                String dateStr = entry.getDate().format(DateTimeFormatter.ofPattern("MM/dd"));
                gc.setFill(Color.web("#6B7280"));
                gc.fillText(dateStr, x - 12, chartCanvas.getHeight() - 15);
            }
        }
    }

    private void refreshRecentEntriesCard() {
        recentEntriesCard.getChildren().clear();

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);
        card.setAlignment(Pos.CENTER);

        Label cardTitle = new Label("Recent Entries");
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        cardTitle.setStyle(forceTextColor(textPrimary)); // FORCE with !important (white background)

        VBox entriesContainer = new VBox(15);
        List<MoodController.MoodEntry> recentEntries = controller.getMoodHistory();
        int displayCount = Math.min(5, recentEntries.size());

        for (int i = recentEntries.size() - 1; i >= recentEntries.size() - displayCount; i--) {
            if (i >= 0) {
                MoodController.MoodEntry entry = recentEntries.get(i);
                HBox entryBox = createMoodEntryBox(entry);
                entriesContainer.getChildren().add(entryBox);
            }
        }

        if (entriesContainer.getChildren().isEmpty()) {
            Label noEntriesLabel = new Label("No mood entries yet. Log your first mood!");
            noEntriesLabel.setFont(Font.font("System", 16));
            noEntriesLabel.setStyle(forceTextColor(Color.web("#9CA3AF"))); // FORCE with !important (white background)
            noEntriesLabel.setPadding(new Insets(20));
            entriesContainer.getChildren().add(noEntriesLabel);
        }

        card.getChildren().addAll(cardTitle, entriesContainer);

        // Center the recent entries card
        VBox cardContainer = new VBox(card);
        cardContainer.setAlignment(Pos.CENTER);
        recentEntriesCard.getChildren().add(cardContainer);
    }

    private HBox createMoodEntryBox(MoodController.MoodEntry entry) {
        HBox entryBox = new HBox(20);
        entryBox.setPadding(new Insets(20));
        entryBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #E2D6FF, #F0D2F7);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );
        entryBox.setAlignment(Pos.CENTER_LEFT);

        Label emojiLabel = new Label(controller.getMoodEmojis()[entry.getMoodValue() - 1]);
        emojiLabel.setFont(Font.font(28));

        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(entry.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        dateLabel.setTextFill(Color.WHITE); // White text on gradient - NO !important

        Label moodLabel = new Label(controller.getMoodLabels()[entry.getMoodValue() - 1]);
        moodLabel.setFont(Font.font("System", 14));
        moodLabel.setTextFill(Color.WHITE); // White text on gradient - NO !important

        headerBox.getChildren().addAll(dateLabel, moodLabel);
        detailsBox.getChildren().add(headerBox);

        if (entry.getNote() != null && !entry.getNote().isEmpty()) {
            Label noteLabel = new Label(entry.getNote());
            noteLabel.setFont(Font.font("System", 14));
            noteLabel.setTextFill(Color.WHITE); // White text on gradient - NO !important
            noteLabel.setWrapText(true);
            noteLabel.setMaxWidth(600);
            detailsBox.getChildren().add(noteLabel);
        }

        entryBox.getChildren().addAll(emojiLabel, detailsBox);
        return entryBox;
    }

    private void logMood() {
        if (currentMood != null) {
            String note = noteTextArea.getText();
            boolean success = controller.logMood(currentMood, note);

            if (success) {
                currentMood = null;
                if (noteTextArea != null) {
                    noteTextArea.clear();
                }
                refreshAllData();
                // showAlert("Mood Logged", "Your mood has been logged successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to log mood. Please try again.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}