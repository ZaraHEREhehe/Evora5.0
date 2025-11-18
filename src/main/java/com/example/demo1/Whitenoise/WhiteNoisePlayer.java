package com.example.demo1.Whitenoise;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class WhiteNoisePlayer {

    private ObservableList<Sound> sounds;
    private DoubleProperty masterVolume = new SimpleDoubleProperty(70);
    private Map<String, MediaPlayer> mediaPlayers = new HashMap<>();

    public VBox getContent() {
        initializeSounds();
        createMediaPlayers();

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f9ff, #e1f5fe);");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = createHeader();

        // Sound Grid
        GridPane soundGrid = createSoundGrid();

        // Preset Combinations - 2x2 grid
        VBox presets = createPresetCombinations();

        // Now Playing Section
        VBox nowPlaying = createNowPlayingSection();

        mainLayout.getChildren().addAll(header, soundGrid, presets, nowPlaying);
        return mainLayout;
    }

    public void showInNewWindow() {
        VBox content = getContent();
        Scene scene = new Scene(content, 1000, 1200);
        Stage stage = new Stage();
        stage.setTitle("White Noise Player 🎵");
        stage.setScene(scene);
        stage.show();

        stage.setOnHidden(e -> cleanup());
    }

    private void initializeSounds() {
        sounds = FXCollections.observableArrayList(
                new Sound("rain", "Rain", "☔", "#4facfe", "#00f2fe"),
                new Sound("coffee", "Coffee Shop", "☕", "#f6d365", "#fda085"),
                new Sound("waves", "Ocean Waves", "🌊", "#43e97b", "#38f9d7"),
                new Sound("wind", "Wind", "💨", "#a8edea", "#fed6e3"),
                new Sound("forest", "Forest", "🌲", "#a3bded", "#6991c7"),
                new Sound("piano", "Piano", "🎹", "#cd9cf2", "#f6f3ff")
        );
    }

    private void createMediaPlayers() {
        Map<String, String> soundFiles = Map.of(
                "rain", "sounds/rain.mp3",
                "coffee", "sounds/coffee-shop.mp3",
                "waves", "sounds/ocean_waves.mp3",
                "wind", "/sounds/wind.mp3",
                "forest", "/sounds/forest.mp3",
                "piano", "/sounds/piano.mp3"
        );

        for (Sound sound : sounds) {
            try {
                String audioFile = soundFiles.get(sound.getId());
                if (audioFile != null) {
                    java.net.URL resource = getClass().getResource(audioFile);
                    if (resource != null) {
                        Media media = new Media(resource.toString());
                        MediaPlayer player = new MediaPlayer(media);
                        player.setCycleCount(MediaPlayer.INDEFINITE);

                        player.volumeProperty().bind(
                                masterVolume.divide(100).multiply(sound.volumeProperty().divide(100))
                        );

                        mediaPlayers.put(sound.getId(), player);
                    } else {
                        System.err.println("Audio file not found: " + audioFile);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading audio for " + sound.getId() + ": " + e.getMessage());
            }
        }
    }

    private VBox createHeader() {
        Label title = new Label("White Noise Player 🎵");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#374151"));

        Label subtitle = new Label("Create your perfect ambient soundscape");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setTextFill(Color.web("#6B7280"));

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);

        return header;
    }

    private GridPane createSoundGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int col = 0;
        int row = 0;
        for (Sound sound : sounds) {
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
        card.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);",
                sound.getStartColor(), sound.getEndColor()
        ));

        // Sound icon
        Circle iconBackground = new Circle(30);
        iconBackground.setFill(Color.web("rgba(255,255,255,0.2)"));

        Label icon = new Label(sound.getIcon());
        icon.setFont(Font.font(24));
        icon.setTextFill(Color.WHITE);

        StackPane iconContainer = new StackPane(iconBackground, icon);

        // Sound name
        Label nameLabel = new Label(sound.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        // Play/Pause button
        Button playButton = new Button();
        playButton.setGraphic(new Label(sound.isPlayingProperty().get() ? "⏸️" : "▶️"));
        playButton.setText(sound.isPlayingProperty().get() ? "Pause" : "Play");
        playButton.setStyle("-fx-background-radius: 15; -fx-padding: 8 16; -fx-font-weight: bold;");

        // Update button style based on playing state
        if (sound.isPlayingProperty().get()) {
            playButton.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.5); -fx-background-radius: 15; -fx-padding: 8 16; -fx-font-weight: bold;");
        } else {
            playButton.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 15; -fx-padding: 8 16; -fx-font-weight: bold;");
        }

        playButton.setOnAction(e -> toggleSound(sound));

        // Volume control (always visible for easy access)
        VBox volumeControl = new VBox(5);
        volumeControl.setAlignment(Pos.CENTER);

        HBox volumeSliderContainer = new HBox(8);
        volumeSliderContainer.setAlignment(Pos.CENTER);

        Label volIcon = new Label("🔊");
        volIcon.setFont(Font.font(12));

        Slider volumeSlider = new Slider(0, 100, sound.getVolume());
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().bindBidirectional(sound.volumeProperty());

        Label volValue = new Label(sound.getVolume() + "%");
        volValue.setFont(Font.font(10));
        volValue.setTextFill(Color.WHITE);
        volValue.textProperty().bind(sound.volumeProperty().asString("%.0f%%"));

        volumeSliderContainer.getChildren().addAll(volIcon, volumeSlider, volValue);
        volumeControl.getChildren().add(volumeSliderContainer);

        VBox content = new VBox(10, iconContainer, nameLabel, playButton, volumeControl);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 15, 20, 15));

        card.getChildren().add(content);

        return card;
    }

    private VBox createPresetCombinations() {
        VBox presetsCard = createCard("✨ Popular Combinations", 600);
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Create 2x2 grid for preset combinations
        GridPane presetsGrid = new GridPane();
        presetsGrid.setHgap(15);
        presetsGrid.setVgap(15);
        presetsGrid.setAlignment(Pos.CENTER);

        // Cozy Café
        Button cozyCafe = createPresetButton("☕ Cozy Café", "Rain + Coffee Shop ambiance",
                new String[]{"rain", "coffee"}, new double[]{60, 40}, "#93c5fd");

        // Nature Escape
        Button natureEscape = createPresetButton("🌲 Nature Escape", "Forest + Gentle Wind",
                new String[]{"forest", "wind"}, new double[]{70, 30}, "#86efac");

        // Ocean Serenity
        Button oceanSerenity = createPresetButton("🌊 Ocean Serenity", "Waves + Soft Piano",
                new String[]{"waves", "piano"}, new double[]{50, 60}, "#67e8f9");

        // Rainy Study
        Button rainyStudy = createPresetButton("🎹 Rainy Study", "Light Rain + Piano",
                new String[]{"rain", "piano"}, new double[]{40, 70}, "#d8b4fe");

        // Add to grid - 2x2 layout
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
        button.setPrefSize(250, 80); // Fixed size for consistent 2x2 grid
        button.setStyle("-fx-border-color: " + borderColor + "; " +
                "-fx-background-color: transparent; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-padding: 16; " +
                "-fx-cursor: hand;");
        button.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#374151"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 12));
        descLabel.setTextFill(Color.web("#6B7280"));

        textContent.getChildren().addAll(titleLabel, descLabel);
        button.setGraphic(textContent);

        button.setOnAction(e -> setPresetSounds(soundIds, volumes));

        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: " + lightenColor(borderColor) + "; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand;");
        });

        return button;
    }

    private VBox createNowPlayingSection() {
        VBox nowPlayingCard = createCard("🎶 Now Playing", 400);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        FlowPane playingSoundsContainer = new FlowPane();
        playingSoundsContainer.setHgap(10);
        playingSoundsContainer.setVgap(10);
        playingSoundsContainer.setPrefWrapLength(600);
        playingSoundsContainer.setAlignment(Pos.CENTER);

        sounds.addListener((javafx.collections.ListChangeListener.Change<? extends Sound> c) -> {
            updateNowPlayingDisplay(playingSoundsContainer);
        });

        for (Sound sound : sounds) {
            sound.isPlayingProperty().addListener((obs, oldVal, newVal) -> {
                updateNowPlayingDisplay(playingSoundsContainer);
            });
        }

        content.getChildren().add(playingSoundsContainer);
        nowPlayingCard.getChildren().add(content);

        nowPlayingCard.visibleProperty().bind(
                javafx.beans.binding.Bindings.size(
                        sounds.filtered(s -> s.isPlayingProperty().get())
                ).greaterThan(0)
        );
        nowPlayingCard.managedProperty().bind(nowPlayingCard.visibleProperty());

        return nowPlayingCard;
    }

    private void updateNowPlayingDisplay(FlowPane container) {
        container.getChildren().clear();

        for (Sound sound : sounds) {
            if (sound.isPlayingProperty().get()) {
                HBox soundChip = new HBox(8);
                soundChip.setAlignment(Pos.CENTER_LEFT);
                soundChip.setPadding(new Insets(8, 12, 8, 12));
                soundChip.setStyle(String.format(
                        "-fx-background-color: linear-gradient(to right, %s, %s); -fx-background-radius: 15; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);",
                        sound.getStartColor(), sound.getEndColor()
                ));

                Label icon = new Label(sound.getIcon());
                icon.setFont(Font.font(12));

                Label name = new Label(sound.getName());
                name.setFont(Font.font("System", FontWeight.MEDIUM, 12));
                name.setTextFill(Color.WHITE);

                Label volume = new Label(sound.getVolume() + "%");
                volume.setFont(Font.font(10));
                volume.setTextFill(Color.web("rgba(255,255,255,0.75)"));
                volume.textProperty().bind(sound.volumeProperty().asString("%.0f%%"));

                // Stop button for individual sound
                Button stopBtn = new Button("⏹️");
                stopBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 2; -fx-font-size: 10;");
                stopBtn.setOnAction(e -> {
                    sound.isPlayingProperty().set(false);
                    MediaPlayer player = mediaPlayers.get(sound.getId());
                    if (player != null) {
                        player.pause();
                        player.seek(Duration.ZERO);
                    }
                });

                soundChip.getChildren().addAll(icon, name, volume, stopBtn);
                container.getChildren().add(soundChip);
            }
        }
    }

    private VBox createCard(String title, double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#374151"));
        titleLabel.setPadding(new Insets(20, 20, 10, 20));
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().add(titleLabel);
        return card;
    }

    private void toggleSound(Sound sound) {
        boolean newPlayingState = !sound.isPlayingProperty().get();
        sound.isPlayingProperty().set(newPlayingState);

        MediaPlayer player = mediaPlayers.get(sound.getId());
        if (player != null) {
            if (newPlayingState) {
                player.play();
            } else {
                player.pause();
                player.seek(Duration.ZERO);
            }
        }
    }

    private void stopAllSounds() {
        for (Sound sound : sounds) {
            sound.isPlayingProperty().set(false);
            MediaPlayer player = mediaPlayers.get(sound.getId());
            if (player != null) {
                player.pause();
                player.seek(Duration.ZERO);
            }
        }
    }

    private void setPresetSounds(String[] soundIds, double[] volumes) {
        stopAllSounds();

        for (int i = 0; i < soundIds.length; i++) {
            String soundId = soundIds[i];
            double volume = volumes[i];

            for (Sound sound : sounds) {
                if (sound.getId().equals(soundId)) {
                    sound.volumeProperty().set(volume);
                    sound.isPlayingProperty().set(true);

                    MediaPlayer player = mediaPlayers.get(soundId);
                    if (player != null) {
                        player.play();
                    }
                    break;
                }
            }
        }
    }

    private String lightenColor(String color) {
        // Simple color lightening for hover effects
        return color + "40"; // Add transparency to lighten
    }

    public void cleanup() {
        for (MediaPlayer player : mediaPlayers.values()) {
            player.dispose();
        }
    }
}