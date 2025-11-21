// File: src/main/java/com/example/demo1/WhiteNoise/WhiteNoiseView.java
package com.example.demo1.WhiteNoise;

import com.example.demo1.Theme.Pastel;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Objects;

public class WhiteNoiseView {
    // Singleton instance
    private static WhiteNoiseView instance;
    private final WhiteNoiseController controller;
    int userId;

    private record Sound(String id, String name, String emoji, String gradient, String borderColor) {
    }

    public void setUserId(int userId) { this.userId = userId; }

    private final Sound[] SOUNDS = {
            new Sound("rain", "Rain", "🌧️",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.BLUE + " 0%, " + Pastel.SKY + " 50%, " + Pastel.LAVENDER + " 100%)", Pastel.BLUE),
            new Sound("coffee", "Coffee Shop", "☕",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.PEACH + " 0%, " + Pastel.CORAL + " 50%, " + Pastel.DUSTY_PINK + " 100%)", Pastel.PEACH),
            new Sound("waves", "Ocean Waves", "🌊",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.SKY + " 0%, " + Pastel.BLUE + " 50%, " + Pastel.LAVENDER + " 100%)", Pastel.SKY),
            new Sound("wind", "Wind", "💨",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.MIST + " 0%, " + Pastel.LAVENDER + " 50%, " + Pastel.LILAC + " 100%)", Pastel.MIST),
            new Sound("forest", "Forest", "🌲",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.MINT + " 0%, " + Pastel.LEMON + " 50%, " + Pastel.PEACH + " 100%)", Pastel.MINT),
            new Sound("piano", "Piano", "🎹",
                    "linear-gradient(from 0% 0% to 100% 100%, " + Pastel.LILAC + " 0%, " + Pastel.LIGHT_LILAC + " 50%, " + Pastel.LIGHT_PURPLE_2 + " 100%)", Pastel.LILAC)
    };

    // Private constructor for singleton
    private WhiteNoiseView() {
        this.controller = WhiteNoiseController.getInstance();
    }

    public Node getContent() {
        return create();
    }

    // Singleton getInstance method
    public static WhiteNoiseView getInstance() {
        if (instance == null) {
            instance = new WhiteNoiseView();
        }
        return instance;
    }

    public void createAndShow(Stage stage, SidebarController sidebarController, String userName) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Pastel.BLUSH + ";");

        Sidebar sidebar = new Sidebar(sidebarController, userName, userId);
        root.setLeft(sidebar);

        Node whiteNoiseContent = create();
        root.setCenter(whiteNoiseContent);

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1400, 900);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle("Évora • White Noise");
        stage.show();
    }

    public Node create() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: " + Pastel.BLUSH + ";");
        mainContent.setAlignment(Pos.TOP_CENTER);

        VBox header = createHeader();
        VBox masterControls = createMasterControls();
        GridPane soundGrid = createSoundGrid();
        VBox nowPlaying = createNowPlayingSection();
        VBox presets = createPresetCombinations();

        mainContent.getChildren().addAll(header, masterControls, soundGrid, nowPlaying, presets);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createHeader() {
        Label title = new Label("White Noise Player 🎵");
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: " + Pastel.FOREST + ";");

        Label subtitle = new Label("Create your perfect ambient soundscape");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: " + Pastel.SAGE + ";");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);

        return header;
    }

    private VBox createMasterControls() {
        VBox masterCard = createCard("🎛️ Master Controls", 600);
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Volume slider row
        HBox volumeRow = new HBox(12);
        volumeRow.setAlignment(Pos.CENTER);

        Label volumeIcon = new Label("🔊");
        volumeIcon.setStyle("-fx-font-size: 20; -fx-text-fill: " + Pastel.SAGE + ";");

        Slider masterSlider = new Slider(0, 100, controller.getMasterVolume().get());
        masterSlider.setPrefWidth(300);
        masterSlider.valueProperty().bindBidirectional(controller.getMasterVolume());
        masterSlider.setStyle(
                "-fx-background-color: " + Pastel.MIST + "; " +
                        "-fx-background-radius: 10; " +
                        "-fx-control-inner-background: " + Pastel.LIGHT_LILAC + "; " +
                        "-fx-border-color: " + Pastel.LIGHT_PURPLE_2 + "; " +
                        "-fx-border-radius: 10;"
        );

        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(controller.getMasterVolume().asString("%.0f%%"));
        volumeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + Pastel.FOREST + ";");

        volumeRow.getChildren().addAll(volumeIcon, masterSlider, volumeLabel);

        // Stats and stop button row
        HBox controlsRow = new HBox(20);
        controlsRow.setAlignment(Pos.CENTER);

        // Playing counter
        VBox playingCounter = new VBox(4);
        playingCounter.setAlignment(Pos.CENTER);

        Label playingLabel = new Label("Playing");
        playingLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + Pastel.SAGE + ";");

        Label playingCount = new Label();
        playingCount.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + Pastel.FOREST + ";");

        // Bind playing count to controller's playing states
        playingCount.textProperty().bind(Bindings.createStringBinding(() ->
                        String.valueOf(controller.getPlayingCount()),
                Arrays.stream(SOUNDS)
                        .map(sound -> controller.getPlaying().get(sound.id()))
                        .filter(Objects::nonNull)
                        .toArray(BooleanProperty[]::new)));

        playingCounter.getChildren().addAll(playingLabel, playingCount);

        // Stop All button
        Button stopAll = new Button("⏹️ Stop All");
        stopAll.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Pastel.LOGOUT_RED + "; -fx-border-color: " + Pastel.LIGHT_RED + "; " +
                "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                "-fx-cursor: hand;");
        stopAll.setOnAction(e -> controller.stopAllSounds());

        stopAll.setOnMouseEntered(e -> stopAll.setStyle(
                "-fx-background-color: " + Pastel.LOGOUT_BG + "; -fx-text-fill: " + Pastel.LOGOUT_RED + "; -fx-border-color: " + Pastel.LIGHT_RED + "; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                        "-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        stopAll.setOnMouseExited(e -> stopAll.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + Pastel.LOGOUT_RED + "; -fx-border-color: " + Pastel.LIGHT_RED + "; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                        "-fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        controlsRow.getChildren().addAll(playingCounter, stopAll);
        content.getChildren().addAll(volumeRow, controlsRow);
        masterCard.getChildren().add(content);

        return masterCard;
    }

    private GridPane createSoundGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        for (Sound sound : SOUNDS) {
            VBox soundCard = createSoundCard(sound);
            grid.add(soundCard, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private VBox createSoundCard(Sound sound) {
        VBox card = new VBox();
        card.setPrefSize(200, 220);
        card.setAlignment(Pos.CENTER);

        card.setStyle("-fx-background-color: " + sound.gradient() + "; " +
                "-fx-background-radius: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 6); " +
                "-fx-cursor: hand;");

        // Sound icon with circle background
        Circle iconBackground = new Circle(30);
        iconBackground.setFill(Color.web("rgba(255,255,255,0.3)"));
        iconBackground.setStroke(Color.WHITE);
        iconBackground.setStrokeWidth(2);

        Label icon = new Label(sound.emoji());
        icon.setStyle("-fx-font-size: 24;");

        StackPane iconContainer = new StackPane(iconBackground, icon);

        // Sound name
        Label nameLabel = new Label(sound.name());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");

        // Play/Pause button
        BooleanProperty isPlayingProp = controller.getPlaying().get(sound.id());
        boolean isCurrentlyPlaying = isPlayingProp != null && isPlayingProp.get();

        Button playButton = new Button();
        playButton.setText(isCurrentlyPlaying ? "⏸️ Pause" : "▶️ Play");
        playButton.setStyle(isCurrentlyPlaying
                ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.7); " +
                "-fx-border-width: 1; -fx-background-radius: 18; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: " + Pastel.FOREST + "; -fx-background-radius: 18; " +
                "-fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
        playButton.setPrefWidth(120);

        playButton.setOnAction(e -> controller.toggleSound(sound.id()));

        // Add hover animation to play button
        playButton.setOnMouseEntered(e -> {
            playButton.setStyle(playButton.getStyle() + " -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        playButton.setOnMouseExited(e -> {
            playButton.setStyle(playButton.getStyle().replace(" -fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
        });

        if (isPlayingProp != null) {
            isPlayingProp.addListener((obs, oldVal, newVal) -> {
                playButton.setText(newVal ? "⏸️ Pause" : "▶️ Play");
                playButton.setStyle(newVal
                        ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.7); " +
                        "-fx-border-width: 1; -fx-background-radius: 18; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;"
                        : "-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: " + Pastel.FOREST + "; -fx-background-radius: 18; " +
                        "-fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
            });
        }

        // Volume control
        VBox volumeControl = new VBox(5);
        volumeControl.setAlignment(Pos.CENTER);
        volumeControl.setPadding(new Insets(8, 12, 8, 12));
        volumeControl.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12;");

        HBox volumeSliderContainer = new HBox(8);
        volumeSliderContainer.setAlignment(Pos.CENTER);

        Label volIcon = new Label("🔊");
        volIcon.setStyle("-fx-font-size: 12; -fx-text-fill: white;");

        DoubleProperty volumeProp = controller.getVolumes().get(sound.id());
        Slider volumeSlider = new Slider(0, 100, volumeProp != null ? volumeProp.get() : 50);
        volumeSlider.setPrefWidth(80);
        volumeSlider.setStyle(
                "-fx-background-color: rgba(255,255,255,0.3); " +
                        "-fx-background-radius: 5; " +
                        "-fx-control-inner-background: rgba(255,255,255,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.5); " +
                        "-fx-border-radius: 5;"
        );
        if (volumeProp != null) {
            volumeSlider.valueProperty().bindBidirectional(volumeProp);
        }

        Label volValue = new Label();
        if (volumeProp != null) {
            volValue.textProperty().bind(volumeProp.asString("%.0f%%"));
        } else {
            volValue.setText("50%");
        }
        volValue.setStyle("-fx-font-size: 10; -fx-text-fill: white; -fx-font-weight: 600;");

        volumeSliderContainer.getChildren().addAll(volIcon, volumeSlider, volValue);
        volumeControl.getChildren().add(volumeSliderContainer);

        if (isPlayingProp != null) {
            volumeControl.visibleProperty().bind(isPlayingProp);
            volumeControl.managedProperty().bind(isPlayingProp);
        } else {
            volumeControl.setVisible(false);
            volumeControl.setManaged(false);
        }

        VBox content = new VBox(12, iconContainer, nameLabel, playButton, volumeControl);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 15, 20, 15));
        content.setStyle("-fx-background-color: transparent;");

        card.getChildren().add(content);

        // Add hover animation to the entire card
        setupCardHoverAnimation(card);

        return card;
    }

    private void setupCardHoverAnimation(VBox card) {
        final ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), card);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        final ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        card.setOnMouseEntered(e -> {
            scaleOut.stop();
            scaleIn.playFromStart();
            card.setStyle(card.getStyle().replace(
                    "dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 6)",
                    "dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 10)"
            ));
        });

        card.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.playFromStart();
            card.setStyle(card.getStyle().replace(
                    "dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 10)",
                    "dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 6)"
            ));
        });
    }

    private VBox createNowPlayingSection() {
        VBox nowPlayingCard = createCard("🎶 Now Playing", 600);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        FlowPane playingSoundsContainer = new FlowPane();
        playingSoundsContainer.setHgap(10);
        playingSoundsContainer.setVgap(10);
        playingSoundsContainer.setPrefWrapLength(600);
        playingSoundsContainer.setAlignment(Pos.CENTER);

        updateNowPlayingDisplay(playingSoundsContainer);

        for (Sound sound : SOUNDS) {
            BooleanProperty isPlayingProp = controller.getPlaying().get(sound.id());
            if (isPlayingProp != null) {
                isPlayingProp.addListener((obs, oldVal, newVal) -> {
                    updateNowPlayingDisplay(playingSoundsContainer);
                });
            }
        }

        content.getChildren().add(playingSoundsContainer);
        nowPlayingCard.getChildren().add(content);

        nowPlayingCard.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            for (Sound sound : SOUNDS) {
                BooleanProperty isPlayingProp = controller.getPlaying().get(sound.id());
                if (isPlayingProp != null && isPlayingProp.get()) {
                    return true;
                }
            }
            return false;
        }, Arrays.stream(SOUNDS)
                .map(sound -> controller.getPlaying().get(sound.id()))
                .filter(Objects::nonNull)
                .toArray(BooleanProperty[]::new)));

        nowPlayingCard.managedProperty().bind(nowPlayingCard.visibleProperty());

        return nowPlayingCard;
    }

    private void updateNowPlayingDisplay(FlowPane container) {
        container.getChildren().clear();

        for (Sound sound : SOUNDS) {
            BooleanProperty isPlayingProp = controller.getPlaying().get(sound.id());
            if (isPlayingProp != null && isPlayingProp.get()) {
                HBox soundChip = new HBox(8);
                soundChip.setAlignment(Pos.CENTER_LEFT);
                soundChip.setPadding(new Insets(8, 12, 8, 12));
                soundChip.setStyle("-fx-background-color: " + sound.gradient() + "; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                        "-fx-cursor: hand;");

                Label icon = new Label(sound.emoji());
                icon.setStyle("-fx-font-size: 12;");

                Label name = new Label(sound.name());
                name.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: white;");

                DoubleProperty volumeProp = controller.getVolumes().get(sound.id());
                Label volume = new Label();
                if (volumeProp != null) {
                    volume.textProperty().bind(volumeProp.asString("%.0f%%"));
                } else {
                    volume.setText("50%");
                }
                volume.setStyle("-fx-font-size: 10; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-weight: 600;");

                Button stopBtn = new Button("⏹️");
                stopBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                        "-fx-padding: 4 6; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 8;");
                stopBtn.setOnAction(e -> controller.toggleSound(sound.id()));

                stopBtn.setOnMouseEntered(e -> stopBtn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.5); -fx-text-fill: white; " +
                                "-fx-padding: 4 6; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 8; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"
                ));
                stopBtn.setOnMouseExited(e -> stopBtn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                                "-fx-padding: 4 6; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 8; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"
                ));

                soundChip.getChildren().addAll(icon, name, volume, stopBtn);
                container.getChildren().add(soundChip);
            }
        }
    }

    private VBox createPresetCombinations() {
        VBox presetsCard = createCard("🌟 Popular Combinations", 600);
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        GridPane presetsGrid = new GridPane();
        presetsGrid.setHgap(15);
        presetsGrid.setVgap(15);
        presetsGrid.setAlignment(Pos.CENTER);

        Button cozyCafe = createPresetButton("☕ Cozy Café", "Rain + Coffee Shop ambiance",
                new String[]{"rain", "coffee"}, new double[]{60, 40}, Pastel.PEACH);
        Button natureEscape = createPresetButton("🌲 Nature Escape", "Forest + Gentle Wind",
                new String[]{"forest", "wind"}, new double[]{70, 30}, Pastel.MINT);
        Button oceanSerenity = createPresetButton("🌊 Ocean Serenity", "Waves + Soft Piano",
                new String[]{"waves", "piano"}, new double[]{50, 60}, Pastel.SKY);
        Button rainyStudy = createPresetButton("🎹 Rainy Study", "Light Rain + Piano",
                new String[]{"rain", "piano"}, new double[]{40, 70}, Pastel.LILAC);

        presetsGrid.add(cozyCafe, 0, 0);
        presetsGrid.add(natureEscape, 1, 0);
        presetsGrid.add(oceanSerenity, 0, 1);
        presetsGrid.add(rainyStudy, 1, 1);

        content.getChildren().add(presetsGrid);
        presetsCard.getChildren().add(content);

        return presetsCard;
    }

    private Button createPresetButton(String title, String description,
                                      String[] soundIds, double[] volumes, String borderColor) {
        Button button = new Button();
        button.setPrefSize(250, 80);
        button.setStyle("-fx-border-color: " + borderColor + "; " +
                "-fx-background-color: transparent; " +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; " +
                "-fx-padding: 16; " +
                "-fx-cursor: hand;");
        button.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + Pastel.FOREST + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + Pastel.SAGE + ";");

        textContent.getChildren().addAll(titleLabel, descLabel);
        button.setGraphic(textContent);

        button.setOnAction(e -> controller.setPresetSounds(soundIds, volumes));

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: " + borderColor + "30; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); " +
                    "-fx-scale-x: 1.03; -fx-scale-y: 1.03;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand; " +
                    "-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        return button;
    }

    private VBox createCard(String title, double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setStyle("-fx-background-color: " + Pastel.IVORY + "; -fx-background-radius: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 6);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + Pastel.FOREST + ";");
        titleLabel.setPadding(new Insets(20, 20, 10, 20));
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().add(titleLabel);
        return card;
    }
}