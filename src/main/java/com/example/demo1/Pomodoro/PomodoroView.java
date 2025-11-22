package com.example.demo1.Pomodoro;

import com.example.demo1.Pets.PetsController;
import com.example.demo1.Theme.Pastel;
import com.example.demo1.Theme.Galaxy;
import com.example.demo1.Theme.Theme;
import com.example.demo1.Theme.ThemeManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PomodoroView {
    // UI Components
    private VBox mainContainer;
    private Text circularTimeText;
    private Label statusLabel;
    private Button startPauseButton;
    private Button resetButton;
    private ComboBox<String> presetBox;
    private Slider workSlider;
    private Slider breakSlider;
    private Label happinessLabel;
    private HBox sessionBox;
    private Circle timerCircle;
    private VBox customTimerContainer;
    private Label sessionsCountLabel;
    private Label titleLabel;
    private Label subtitleLabel;
    private VBox petContainer;
    private ProgressBar petHappinessBar;
    private Label petHappinessPercentLabel;
    private VBox timerCard;
    private ThemeManager themeManager;

    private PomodoroController controller;

    public PomodoroView(PomodoroController controller) {
        this.controller = controller;
        this.themeManager = ThemeManager.getInstance();
        initializeComponents();
        setupStyles();
        setupEventHandlers();
        controller.setView(this);

        // Add theme change listener
        themeManager.addThemeChangeListener(this::updateTheme);
    }

    public VBox getView() {
        return mainContainer;
    }

    private void initializeComponents() {
        // Create main container
        mainContainer = new VBox(15);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20));

        // Header
        VBox headerBox = new VBox(3);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-padding: 10 0 5 0;");

        titleLabel = new Label("Pomodoro Timer 🍅");
        subtitleLabel = new Label("Stay focused with the Pomodoro Technique!");
        headerBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Timer Presets
        HBox presetsBox = new HBox(8);
        presetsBox.setAlignment(Pos.CENTER);

        presetBox = new ComboBox<>();
        presetBox.setPrefWidth(180);

        Button settingsButton = new Button("⚙️");
        presetsBox.getChildren().addAll(presetBox, settingsButton);

        // Custom Timer Settings
        customTimerContainer = createCustomTimerContainer();

        // Main Timer Card
        timerCard = createTimerCard();

        // Pet Container
        petContainer = createPetContainer();

        // Add all components to main container
        mainContainer.getChildren().addAll(
                headerBox, presetsBox, customTimerContainer,
                timerCard, petContainer
        );
    }

    private VBox createCustomTimerContainer() {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(380);
        container.setVisible(false);
        container.setManaged(false);

        Label title = new Label("Custom Timer");
        title.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox slidersBox = new HBox(15);
        slidersBox.setAlignment(Pos.CENTER);

        // Work slider
        VBox workBox = new VBox(3);
        Label workLabel = new Label("Work Minutes");
        workLabel.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 11px;");
        workSlider = new Slider();
        workSlider.setMin(5);
        workSlider.setMax(120);
        workSlider.setValue(25);
        workSlider.setBlockIncrement(5);
        workSlider.setPrefWidth(140);
        workBox.getChildren().addAll(workLabel, workSlider);

        // Break slider
        VBox breakBox = new VBox(3);
        Label breakLabel = new Label("Break Minutes");
        breakLabel.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 11px;");
        breakSlider = new Slider();
        breakSlider.setMin(1);
        breakSlider.setMax(60);
        breakSlider.setValue(5);
        breakSlider.setBlockIncrement(1);
        breakSlider.setPrefWidth(140);
        breakBox.getChildren().addAll(breakLabel, breakSlider);

        slidersBox.getChildren().addAll(workBox, breakBox);

        Button applyButton = new Button("⏰ Set Custom Timer");
        applyButton.setStyle("-fx-background-color: linear-gradient(to right, " + Pastel.PINK + ", " + Pastel.PURPLE + "); " +
                "-fx-text-fill: " + Pastel.FOREST + "; -fx-font-weight: bold; -fx-background-radius: 15; " +
                "-fx-padding: 8 16; -fx-font-size: 12px;");

        container.getChildren().addAll(title, slidersBox, applyButton);
        return container;
    }

    private VBox createTimerCard() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(25));

        // Status label
        statusLabel = new Label("💼 Work Session");

        // Circular timer
        StackPane timerPane = new StackPane();

        timerCircle = new Circle(85);
        timerCircle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3);");
        timerCircle.setFill(Color.TRANSPARENT);
        timerCircle.setStrokeWidth(6);

        Circle innerCircle = new Circle(75);
        innerCircle.setFill(Color.WHITE);
        innerCircle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        VBox timeBox = new VBox(3);
        timeBox.setAlignment(Pos.CENTER);
        circularTimeText = new Text("25:00");
        Label timerLabel = new Label("Work");
        timerLabel.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 11px;");
        timeBox.getChildren().addAll(circularTimeText, timerLabel);

        timerPane.getChildren().addAll(timerCircle, innerCircle, timeBox);

        // Controls
        HBox controlsBox = new HBox(12);
        controlsBox.setAlignment(Pos.CENTER);

        startPauseButton = new Button("Start");
        startPauseButton.setPrefSize(90, 35);

        resetButton = new Button("Reset");
        resetButton.setPrefSize(90, 35);

        controlsBox.getChildren().addAll(startPauseButton, resetButton);

        // Session counter
        VBox sessionCounter = new VBox(8);
        sessionCounter.setAlignment(Pos.CENTER);

        Label sessionTitle = new Label("Sessions Completed Today");
        sessionTitle.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 11px;");

        sessionBox = new HBox(6);
        sessionBox.setAlignment(Pos.CENTER);

        sessionsCountLabel = new Label("0 🍅");
        sessionsCountLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        sessionCounter.getChildren().addAll(sessionTitle, sessionBox, sessionsCountLabel);

        card.getChildren().addAll(statusLabel, timerPane, controlsBox, sessionCounter);
        return card;
    }

    private VBox createPetContainer() {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(380);
        container.setVisible(false);
        container.setManaged(true);

        Label title = new Label("Your Pet is Working Hard! 💪");
        title.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label subtitle = new Label("");
        subtitle.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 11px;");

        HBox petContent = new HBox(15);
        petContent.setAlignment(Pos.CENTER);

        // Pixel pet placeholder
        StackPane petPlaceholder = new StackPane();
        petPlaceholder.setStyle("-fx-background-color: linear-gradient(to bottom right, " + Pastel.BLUE + ", " + Pastel.LAVENDER + "); " +
                "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: " + Pastel.PINK + "; " +
                "-fx-border-width: 3; -fx-pref-width: 70; -fx-pref-height: 70;");
        Text petEmoji = new Text("🐱");
        petEmoji.setStyle("-fx-font-size: 20;");
        petPlaceholder.getChildren().add(petEmoji);

        // Happiness bar
        VBox happinessBox = new VBox(3);
        happinessBox.setAlignment(Pos.CENTER);

        Label happinessTitle = new Label("😊 Happiness");
        happinessTitle.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 11px;");

        petHappinessBar = new ProgressBar(0.85);
        petHappinessBar.setPrefWidth(70);

        petHappinessPercentLabel = new Label("85%");
        petHappinessPercentLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 11px;");

        happinessBox.getChildren().addAll(happinessTitle, petHappinessBar, petHappinessPercentLabel);

        petContent.getChildren().addAll(petPlaceholder, happinessBox);
        container.getChildren().addAll(title, subtitle, petContent);
        return container;
    }

    private void setupStyles() {
        updateTheme(themeManager.getCurrentTheme());
    }

    private void updateTheme(Theme theme) {
        // Update main background based on theme
        if (theme instanceof com.example.demo1.Theme.GalaxyTheme) {
            // Galaxy theme - solid dark background
            mainContainer.setStyle("-fx-background-color: " + theme.getBackgroundColor() + ";");

            // For Galaxy theme, use Pastel styling for the timer panel but update text colors for readability
            titleLabel.setStyle("-fx-text-fill: " + Galaxy.MOON_LIGHT + "; -fx-font-size: 24px; -fx-font-weight: bold;");
            subtitleLabel.setStyle("-fx-text-fill: " + Galaxy.STAR_DUST + "; -fx-font-size: 12px;");

            // Keep Pastel timer card styling but ensure text is readable
            timerCard.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 20; " +
                    "-fx-border-radius: 20; -fx-border-color: " + Pastel.PINK + "; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);");

            // Update text colors for Galaxy theme to ensure readability
            statusLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            circularTimeText.setStyle("-fx-fill: " + Pastel.FOREST + "; -fx-font-size: 28px; -fx-font-weight: bold;");

            // Update preset box for Galaxy - use Pastel styling but lighter text
            presetBox.setStyle("-fx-background-color: " + Pastel.IVORY + "; -fx-background-radius: 15; " +
                    "-fx-border-radius: 15; -fx-border-color: " + Pastel.DUSTY_PINK + "; " +
                    "-fx-padding: 6 12; -fx-font-size: 12px; " +
                    "-fx-text-fill: " + Pastel.FOREST + ";");

            // Update settings button for Galaxy - use Pastel styling
            Button settingsButton = getSettingsButton();
            settingsButton.setStyle("-fx-background-color: " + Pastel.IVORY + "; -fx-background-radius: 15; -fx-border-radius: 15; " +
                    "-fx-border-color: " + Pastel.DUSTY_PINK + "; -fx-padding: 6 10; -fx-font-size: 12px;");

        } else {
            // Pastel theme - keep the beautiful original styling
            mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, " +
                    Pastel.SKY + ", " + Pastel.MIST + ", " + Pastel.BLUSH + ");");

            // Keep original Pastel text colors
            titleLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 24px; -fx-font-weight: bold;");
            subtitleLabel.setStyle("-fx-text-fill: " + Pastel.SAGE + "; -fx-font-size: 12px;");

            // Keep original Pastel timer card
            timerCard.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 20; " +
                    "-fx-border-radius: 20; -fx-border-color: " + Pastel.PINK + "; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);");

            // Keep original Pastel status and timer text
            statusLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            circularTimeText.setStyle("-fx-fill: " + Pastel.FOREST + "; -fx-font-size: 28px; -fx-font-weight: bold;");

            // Keep original Pastel preset box
            presetBox.setStyle("-fx-background-color: " + Pastel.IVORY + "; -fx-background-radius: 15; " +
                    "-fx-border-radius: 15; -fx-border-color: " + Pastel.DUSTY_PINK + "; " +
                    "-fx-padding: 6 12; -fx-font-size: 12px;");

            // Keep original Pastel settings button
            Button settingsButton = getSettingsButton();
            settingsButton.setStyle("-fx-background-color: " + Pastel.IVORY + "; -fx-background-radius: 15; -fx-border-radius: 15; " +
                    "-fx-border-color: " + Pastel.DUSTY_PINK + "; -fx-padding: 6 10; -fx-font-size: 12px;");
        }

        // Update buttons with theme-appropriate styles
        updateButtonState(controller.isRunning(), controller.isBreak());

        // Update circle gradient - always use Pastel colors for both themes
        updateCircleGradient(controller.isBreak());

        // Update other containers to use Pastel styling for both themes
        updateContainerStyles(theme);
    }

    private void updateContainerStyles(Theme theme) {
        // For both themes, use Pastel styling for custom timer and pet containers
        customTimerContainer.setStyle("-fx-background-color: " + Pastel.IVORY + "; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: " + Pastel.DUSTY_PINK + "; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        petContainer.setStyle("-fx-background-color: " + Pastel.IVORY + "; " +
                "-fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: " + Pastel.PINK + "; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Ensure text in custom timer container uses Pastel colors
        if (customTimerContainer.getChildren().size() > 0) {
            Label title = (Label) customTimerContainer.getChildren().get(0);
            title.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }

    private void setupEventHandlers() {
        startPauseButton.setOnAction(e -> controller.toggleTimer());
        resetButton.setOnAction(e -> controller.resetTimer());
        presetBox.setOnAction(e -> controller.presetSelected());
        getSettingsButton().setOnAction(e -> controller.toggleCustomSettings());
        getApplyCustomButton().setOnAction(e -> controller.applyCustomTimer());
    }

    // UI Update methods
    public void updateTimerDisplay(String time, double progress) {
        circularTimeText.setText(time);
        updateCircularTimer(progress);
    }

    public void updateStatus(String status, boolean isBreak) {
        statusLabel.setText(status);
        updateCircleGradient(isBreak);
    }

    public void updateSessions(int sessions) {
        sessionBox.getChildren().clear();

        // Always use Pastel colors for session dots
        for(int i = 0; i < Math.max(4, sessions); i++) {
            Circle dot = new Circle(5);
            if(i < sessions) {
                dot.setFill(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web(Pastel.PINK)),
                        new Stop(1, Color.web(Pastel.PURPLE))));
            } else {
                dot.setFill(Color.web(Pastel.DUSTY_PINK));
            }
            sessionBox.getChildren().add(dot);
        }
        sessionsCountLabel.setText(sessions + " 🍅");
    }

    public void updateHappiness(int happiness) {
        petHappinessBar.setProgress(happiness / 100.0);
        petHappinessPercentLabel.setText(happiness + "%");
    }

    public void updatePetDisplay(PetsController.PetInfo currentPet) {
        if (petContainer != null && currentPet != null) {
            // Clear existing content but keep the title and subtitle
            if (petContainer.getChildren().size() > 2) {
                petContainer.getChildren().remove(2);
            }

            VBox petContent = new VBox(10);
            petContent.setAlignment(Pos.CENTER);
            petContent.setPadding(new Insets(10));

            Label petNameLabel = new Label(currentPet.getDisplayName());
            // Always use Pastel colors for pet display
            petNameLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            try {
                ImageView petGif = new ImageView(new Image(getPetGifPath(currentPet.getGifFilename())));
                petGif.setFitWidth(80);
                petGif.setFitHeight(80);
                petGif.setPreserveRatio(true);
                petGif.setStyle("-fx-effect: dropshadow(gaussian, rgba(192, 132, 252, 0.3), 10, 0.5, 0, 2);");
                petContent.getChildren().addAll(petGif, petNameLabel);
            } catch (Exception e) {
                Label petEmoji = new Label(getSpeciesEmoji(currentPet.getSpecies()));
                petEmoji.setFont(Font.font(36));
                petEmoji.setStyle("-fx-text-fill: " + Pastel.FOREST + ";");
                petContent.getChildren().addAll(petEmoji, petNameLabel);
            }

            petContainer.getChildren().add(petContent);
        }
    }

    public void setPetVisibility(boolean visible) {
        petContainer.setVisible(visible);
    }

    public void setCustomTimerVisibility(boolean visible) {
        customTimerContainer.setVisible(visible);
        customTimerContainer.setManaged(visible);
    }

    public void updateButtonState(boolean isRunning, boolean isBreak) {
        Theme currentTheme = themeManager.getCurrentTheme();

        if (isRunning) {
            startPauseButton.setText("Pause");
            startPauseButton.setStyle(getPrimaryButtonStyle(true, currentTheme));
        } else {
            String buttonText = controller.hasActiveSession() ? "Resume" : "Start";
            startPauseButton.setText(buttonText);
            startPauseButton.setStyle(getPrimaryButtonStyle(false, currentTheme));
        }

        resetButton.setStyle(getSecondaryButtonStyle(currentTheme));
    }

    public void showHappinessBoost() {
        Theme currentTheme = themeManager.getCurrentTheme();
        petHappinessPercentLabel.setText("+10 ✨");

        // Always use Pastel burgundy for happiness boost
        petHappinessPercentLabel.setStyle("-fx-text-fill: " + Pastel.BURGUNDY + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        Timeline boostTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.1), e -> petHappinessPercentLabel.setScaleX(1.2)),
                new KeyFrame(Duration.seconds(0.2), e -> petHappinessPercentLabel.setScaleY(1.2)),
                new KeyFrame(Duration.seconds(1), e -> {
                    petHappinessPercentLabel.setScaleX(1.0);
                    petHappinessPercentLabel.setScaleY(1.0);
                    petHappinessPercentLabel.setText(controller.getHappiness() + "%");
                    petHappinessPercentLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 12px;");
                })
        );
        boostTimeline.play();
    }

    public void resetHappinessDisplay() {
        // Always use Pastel forest color
        petHappinessPercentLabel.setStyle("-fx-text-fill: " + Pastel.FOREST + "; -fx-font-size: 12px;");
    }

    // Getters for UI components
    public Button getStartPauseButton() { return startPauseButton; }
    public Button getResetButton() { return resetButton; }
    public ComboBox<String> getPresetBox() { return presetBox; }
    public Button getSettingsButton() {
        HBox presetsBox = (HBox) mainContainer.getChildren().get(1);
        return (Button) presetsBox.getChildren().get(1);
    }
    public Button getApplyCustomButton() {
        return (Button) customTimerContainer.getChildren().get(2);
    }
    public Slider getWorkSlider() { return workSlider; }
    public Slider getBreakSlider() { return breakSlider; }

    // Helper methods
    private void updateCircleGradient(boolean isBreak) {
        // Always use Pastel colors for the circle gradient
        if (isBreak) {
            timerCircle.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(Pastel.BLUE)),
                    new Stop(1, Color.web(Pastel.LAVENDER))));
        } else {
            timerCircle.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(Pastel.PINK)),
                    new Stop(1, Color.web(Pastel.PURPLE))));
        }
    }

    private String getPrimaryButtonStyle(boolean isActive, Theme theme) {
        // Always use Pastel colors for buttons
        if (isActive) {
            return "-fx-background-color: linear-gradient(to right, " + Pastel.MAUVE + ", " + Pastel.BURGUNDY + "); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);";
        } else {
            return "-fx-background-color: linear-gradient(to right, " + Pastel.PINK + ", " + Pastel.PURPLE + "); " +
                    "-fx-text-fill: " + Pastel.FOREST + "; -fx-font-weight: bold; -fx-font-size: 12px; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);";
        }
    }

    private String getSecondaryButtonStyle(Theme theme) {
        // Always use Pastel colors for secondary button
        return "-fx-background-color: " + Pastel.IVORY + "; -fx-text-fill: " + Pastel.FOREST + "; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 15; " +
                "-fx-border-radius: 15; -fx-border-color: " + Pastel.DUSTY_PINK + "; " +
                "-fx-padding: 10 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);";
    }

    private void updateCircularTimer(double progress) {
        double radius = timerCircle.getRadius();
        double circumference = 2 * Math.PI * radius;
        timerCircle.getStrokeDashArray().clear();
        timerCircle.getStrokeDashArray().addAll(circumference);
        timerCircle.setStrokeDashOffset(circumference * (1 - progress));
    }

    private String getPetGifPath(String filename) {
        try {
            return getClass().getResource("/pet_gifs/" + filename).toExternalForm();
        } catch (Exception e) {
            return "";
        }
    }

    private String getSpeciesEmoji(String species) {
        switch (species.toLowerCase()) {
            case "cat": return "🐱";
            case "bunny": return "🐰";
            case "owl": return "🦉";
            case "dragon": return "🐉";
            default: return "❓";
        }
    }

    public ScrollPane getViewAsScrollPane() {
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Allow the content to grow beyond viewport
        mainContainer.setMinHeight(Region.USE_PREF_SIZE);

        return scrollPane;
    }
}