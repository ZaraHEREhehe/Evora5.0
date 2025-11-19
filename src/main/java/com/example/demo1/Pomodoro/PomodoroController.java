package com.example.demo1.Pomodoro;

import com.example.demo1.Pets.PetsController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PomodoroController {

    @FXML private Text circularTimeText;
    @FXML private Label statusLabel;
    @FXML private Button startPauseButton;
    @FXML private Button resetButton;
    @FXML private ComboBox<String> presetBox;
    @FXML private Slider workSlider;
    @FXML private Slider breakSlider;
    @FXML private Label happinessLabel;
    @FXML private HBox sessionBox;
    @FXML private Circle timerCircle;
    @FXML private VBox mainContainer;
    @FXML private VBox customTimerContainer;
    @FXML private Label sessionsCountLabel;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox petContainer;
    @FXML private ProgressBar petHappinessBar;
    @FXML private Label petHappinessPercentLabel;
    @FXML private VBox timerCard;

    private Timeline timeline;
    private boolean running = false;
    private boolean isBreak = false;
    private int timeLeft;
    private int workTime = 25 * 60;
    private int breakTime = 5 * 60;
    private int happiness = 85;
    private int sessions = 0;
    private boolean showCustomSettings = false;

    // Pastel color palette
    private final Color PASTEL_PINK = Color.web("#FACEEA");
    private final Color PASTEL_BLUE = Color.web("#C1DAFF");
    private final Color PASTEL_LAVENDER = Color.web("#D7D8FF");
    private final Color PASTEL_PURPLE = Color.web("#F0D2F7");
    private final Color PASTEL_LILAC = Color.web("#E2D6FF");
    private final Color PASTEL_ROSE = Color.web("#F3D1F3");
    private final Color PASTEL_BLUSH = Color.web("#FCEDF5");
    private final Color PASTEL_SKY = Color.web("#E4EFFF");
    private final Color PASTEL_MIST = Color.web("#F7EFFF");
    private final Color PASTEL_IVORY = Color.web("#FDF5E7");
    private final Color PASTEL_KHAKI = Color.web("#CDBFAA");
    private final Color PASTEL_SLATE = Color.web("#ADB697");
    private final Color PASTEL_DUSTY_PINK = Color.web("#F1DBD0");
    private final Color PASTEL_MAUVE = Color.web("#D3A29D");
    private final Color PASTEL_BURGUNDY = Color.web("#A36361");
    private final Color PASTEL_SAGE = Color.web("#8D9383");
    private final Color PASTEL_FOREST = Color.web("#343A26");
    private final Color PASTEL_BROWN = Color.web("#483C2B");

    private PetsController petsController;

    @FXML
    public void initialize() {
        setupStyles();
        setupPresets();
        setupTimer();
        updateUI();
    }

    private void setupStyles() {
        // Main container styling with beautiful gradient
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, " +
                toHex(PASTEL_SKY) + ", " + toHex(PASTEL_MIST) + ", " + toHex(PASTEL_BLUSH) + ");");
        mainContainer.setSpacing(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Title styling
        titleLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        subtitleLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_SAGE) + "; -fx-font-size: 12px;");

        // Timer card styling - moved upwards
        timerCard.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 20; " +
                "-fx-border-radius: 20; -fx-border-color: " + toHex(PASTEL_PINK) + "; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);");
        timerCard.setPadding(new Insets(25));
        timerCard.setSpacing(15);

        // Hide custom settings and pet container initially
        customTimerContainer.setVisible(false);
        petContainer.setVisible(false);
        petContainer.setManaged(true);
    }

    private void setupPresets() {
        presetBox.getItems().addAll("Pomodoro (25/5)", "Short Focus (15/3)", "Deep Work (45/10)",
                "Study Session (50/10)", "Quick Task (10/2)");
        presetBox.getSelectionModel().selectFirst();

        // Style the ComboBox
        presetBox.setStyle("-fx-background-color: " + toHex(PASTEL_IVORY) + "; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-border-color: " + toHex(PASTEL_DUSTY_PINK) + "; " +
                "-fx-padding: 6 12; -fx-font-size: 12px;");
    }

    private void setupTimer() {
        timeLeft = workTime;
        circularTimeText.setText(formatTime(timeLeft));
        circularTimeText.setStyle("-fx-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-size: 28px; -fx-font-weight: bold;");

        // Gradient stroke for circle - dynamic based on session type
        updateCircleGradient();
        timerCircle.setFill(Color.TRANSPARENT);
        timerCircle.setStrokeWidth(6);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        statusLabel.setText("💼 Work Session");
        statusLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        startPauseButton.setText("Start");
        startPauseButton.setStyle(getPrimaryButtonStyle(false));

        updateSessions();
        updateCircularTimer(0);
    }

    private void updateCircleGradient() {
        if (isBreak) {
            timerCircle.setStroke(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, PASTEL_BLUE),
                    new Stop(1, PASTEL_LAVENDER)
            ));
        } else {
            timerCircle.setStroke(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, PASTEL_PINK),
                    new Stop(1, PASTEL_PURPLE)
            ));
        }
    }

    private String getPrimaryButtonStyle(boolean isActive) {
        if (isActive) {
            return "-fx-background-color: linear-gradient(to right, " + toHex(PASTEL_MAUVE) + ", " + toHex(PASTEL_BURGUNDY) + "); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);";
        } else {
            if (isBreak) {
                return "-fx-background-color: linear-gradient(to right, " + toHex(PASTEL_BLUE) + ", " + toHex(PASTEL_LAVENDER) + "); " +
                        "-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-weight: bold; -fx-font-size: 12px; " +
                        "-fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);";
            } else {
                return "-fx-background-color: linear-gradient(to right, " + toHex(PASTEL_PINK) + ", " + toHex(PASTEL_PURPLE) + "); " +
                        "-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-weight: bold; -fx-font-size: 12px; " +
                        "-fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);";
            }
        }
    }

    private String getSecondaryButtonStyle() {
        return "-fx-background-color: " + toHex(PASTEL_IVORY) + "; -fx-text-fill: " + toHex(PASTEL_FOREST) + "; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-border-color: " + toHex(PASTEL_DUSTY_PINK) + "; " +
                "-fx-padding: 10 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);";
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void tick() {
        if (timeLeft > 0) {
            timeLeft--;
            circularTimeText.setText(formatTime(timeLeft));
            double total = isBreak ? breakTime : workTime;
            updateCircularTimer((double)(total - timeLeft)/total);

            // Show pet during active work sessions
            if (running && !isBreak) {
                petContainer.setVisible(true);
            }
        } else {
            timeline.stop();
            running = false;
            petContainer.setVisible(false);
            if (isBreak) {
                startWorkSession();
            } else {
                startBreakSession();
            }
        }
    }

    private void startWorkSession() {
        isBreak = false;
        timeLeft = workTime;
        statusLabel.setText("💼 Work Session");
        startPauseButton.setText("Start");
        startPauseButton.setStyle(getPrimaryButtonStyle(false));
        updateCircleGradient();
        updateCircularTimer(0);
        circularTimeText.setText(formatTime(timeLeft));
        petContainer.setVisible(true); // Show pet for work sessions
    }

    private void startBreakSession() {
        isBreak = true;
        sessions++;
        happiness = Math.min(100, happiness + 10);
        happinessLabel.setText("😊 Happiness: " + happiness + "%");
        timeLeft = breakTime;
        statusLabel.setText("☕ Break Time");
        startPauseButton.setText("Start");
        startPauseButton.setStyle(getPrimaryButtonStyle(false));
        updateCircleGradient();
        updateSessions();
        updateCircularTimer(0);
        circularTimeText.setText(formatTime(timeLeft));

        petContainer.setVisible(false); // Hide pet during breaks

        // Show happiness boost notification
        showHappinessBoost();
    }

    private void showHappinessBoost() {
        petHappinessPercentLabel.setText("+" + 10 + " ✨");
        petHappinessPercentLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_BURGUNDY) + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        Timeline boostTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.1), e -> petHappinessPercentLabel.setScaleX(1.2)),
                new KeyFrame(Duration.seconds(0.2), e -> petHappinessPercentLabel.setScaleY(1.2)),
                new KeyFrame(Duration.seconds(1), e -> {
                    petHappinessPercentLabel.setScaleX(1.0);
                    petHappinessPercentLabel.setScaleY(1.0);
                    petHappinessPercentLabel.setText(happiness + "%");
                    petHappinessPercentLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-size: 12px;");
                })
        );
        boostTimeline.play();
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void updateCircularTimer(double progress) {
        double radius = timerCircle.getRadius();
        double circumference = 2 * Math.PI * radius;
        timerCircle.getStrokeDashArray().clear();
        timerCircle.getStrokeDashArray().addAll(circumference);
        timerCircle.setStrokeDashOffset(circumference * (1 - progress));
    }

    @FXML
    private void toggleTimer() {
        if (running) {
            timeline.stop();
            running = false;
            startPauseButton.setText("Start");
            startPauseButton.setStyle(getPrimaryButtonStyle(false));
            petContainer.setVisible(false);
        } else {
            running = true;
            if (timeLeft == 0)
                timeLeft = isBreak ? breakTime : workTime;
            timeline.play();
            startPauseButton.setText("Pause");
            startPauseButton.setStyle(getPrimaryButtonStyle(true));
        }
    }

    @FXML
    private void resetTimer() {
        timeline.stop();
        running = false;
        timeLeft = isBreak ? breakTime : workTime;
        circularTimeText.setText(formatTime(timeLeft));
        updateCircularTimer(0);
        startPauseButton.setText("Start");
        startPauseButton.setStyle(getPrimaryButtonStyle(false));
        petContainer.setVisible(false);
    }

    @FXML
    private void presetSelected() {
        String preset = presetBox.getValue();
        switch (preset) {
            case "Pomodoro (25/5)" -> { workTime = 25*60; breakTime=5*60; }
            case "Short Focus (15/3)" -> { workTime = 15*60; breakTime=3*60; }
            case "Deep Work (45/10)" -> { workTime = 45*60; breakTime=10*60; }
            case "Study Session (50/10)" -> { workTime = 50*60; breakTime=10*60; }
            case "Quick Task (10/2)" -> { workTime = 10*60; breakTime=2*60; }
        }
        resetTimer();
    }

    @FXML
    private void applyCustomTimer() {
        workTime = (int) workSlider.getValue()*60;
        breakTime = (int) breakSlider.getValue()*60;
        resetTimer();
        toggleCustomSettings();
    }

    @FXML
    private void toggleCustomSettings() {
        showCustomSettings = !showCustomSettings;
        customTimerContainer.setVisible(showCustomSettings);
        customTimerContainer.setManaged(showCustomSettings);
    }

    private void updateSessions() {
        sessionBox.getChildren().clear();
        for(int i=0;i<Math.max(4, sessions);i++){
            Circle dot = new Circle(5);
            if(i < sessions) {
                dot.setFill(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                        new Stop(0, PASTEL_PINK),
                        new Stop(1, PASTEL_PURPLE)));
            } else {
                dot.setFill(PASTEL_DUSTY_PINK);
            }
            sessionBox.getChildren().add(dot);
        }
        sessionsCountLabel.setText(sessions + " 🍅");
    }

    private void updateUI() {
        // Update button styles
        resetButton.setStyle(getSecondaryButtonStyle());

        // Update sliders style
        String sliderStyle = "-fx-control-inner-background: " + toHex(PASTEL_IVORY) + "; " +
                "-fx-background-color: " + toHex(PASTEL_DUSTY_PINK) + "; " +
                "-fx-background-radius: 10;";
        workSlider.setStyle(sliderStyle);
        breakSlider.setStyle(sliderStyle);

        // Update custom timer container style
        customTimerContainer.setStyle("-fx-background-color: " + toHex(PASTEL_IVORY) + "; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: " + toHex(PASTEL_DUSTY_PINK) + "; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Update pet container style
        petContainer.setStyle("-fx-background-color: " + toHex(PASTEL_IVORY) + "; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: " + toHex(PASTEL_PINK) + "; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
    }

    //pets
    public void setPetsController(PetsController petsController) {
        this.petsController = petsController;
        updatePetDisplay();
    }

    // Add this method to update the pet display
    private void updatePetDisplay() {
        if (petsController != null) {
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();

            // Clear previous pet content
            petContainer.getChildren().clear();

            // Create pet display
            VBox petContent = new VBox(10);
            petContent.setAlignment(Pos.CENTER);
            petContent.setPadding(new Insets(10));

            // Pet name
            Label petNameLabel = new Label(currentPet.getDisplayName());
            petNameLabel.setStyle("-fx-text-fill: " + toHex(PASTEL_FOREST) + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            // Pet GIF
            try {
                ImageView petGif = new ImageView(new Image(getPetGifPath(currentPet.getGifFilename())));
                petGif.setFitWidth(80);
                petGif.setFitHeight(80);
                petGif.setPreserveRatio(true);

                // Add subtle animation effect
                petGif.setStyle("-fx-effect: dropshadow(gaussian, rgba(192, 132, 252, 0.3), 10, 0.5, 0, 2);");

                petContent.getChildren().addAll(petGif, petNameLabel);
            } catch (Exception e) {
                // Fallback to emoji if GIF fails to load
                Label petEmoji = new Label(getSpeciesEmoji(currentPet.getSpecies()));
                petEmoji.setFont(Font.font(36));
                petEmoji.setStyle("-fx-text-fill: " + toHex(PASTEL_FOREST) + ";");

                petContent.getChildren().addAll(petEmoji, petNameLabel);
            }

            petContainer.getChildren().add(petContent);
        }
    }

    // Helper method to get pet GIF path (similar to the one in PetsView)
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
            default: return "❓";
        }
    }

}