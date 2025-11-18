// File: src/main/java/com/example/demo1/WhiteNoise/WhiteNoiseView.java
package com.example.demo1.WhiteNoise;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

public class WhiteNoiseView {

    private final Map<String, MediaPlayer> players = new HashMap<>();
    private final Map<String, DoubleProperty> volumes = new HashMap<>();
    private final Map<String, BooleanProperty> playing = new HashMap<>();
    private final DoubleProperty masterVolume = new SimpleDoubleProperty(70);

    private record Sound(String id, String name, String emoji, String gradient, String borderColor) {
    }

    private final Sound[] SOUNDS = {
            new Sound("rain", "Rain", "🌧️",
                    "linear-gradient(135deg, #b3d9ff 0%, #99ccff 100%)", "#b3d9ff"),
            new Sound("coffee", "Coffee Shop", "☕",
                    "linear-gradient(135deg, #ffd9b3 0%, #ffcc99 100%)", "#ffd9b3"),
            new Sound("waves", "Ocean Waves", "🌊",
                    "linear-gradient(135deg, #b3e6ff 0%, #99d6ff 100%)", "#b3e6ff"),
            new Sound("wind", "Wind", "💨",
                    "linear-gradient(135deg, #e6f2ff 0%, #d9e6ff 100%)", "#e6f2ff"),
            new Sound("forest", "Forest", "🌲",
                    "linear-gradient(135deg, #c6f5d5 0%, #b3f0c3 100%)", "#c6f5d5"),
            new Sound("piano", "Piano", "🎹",
                    "linear-gradient(135deg, #e6d9ff 0%, #d9ccff 100%)", "#e6d9ff")
    };

    public void createAndShow(Stage stage, SidebarController sidebarController, String userName) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f8ff, #e6f2ff);");

        Sidebar sidebar = new Sidebar(sidebarController, userName);
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

    public void createAndShow(Stage stage) {
        SidebarController tempController = new SidebarController() {
            @Override
            public void navigate(String destination) {
                System.out.println("Navigating to: " + destination);
            }

            @Override
            public void goTo(String destination) {
                System.out.println("Going to: " + destination);
            }
        };

        createAndShow(stage, tempController, "User");
    }

    public Node create() {
        loadAllSounds();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f8ff, #e6f2ff);");
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
        title.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #5a6c7d;");

        Label subtitle = new Label("Create your perfect ambient soundscape");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: #7d8fa1;");

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
        volumeIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #7d8fa1;");

        Slider masterSlider = new Slider(0, 100, masterVolume.get());
        masterSlider.setPrefWidth(300);
        masterSlider.valueProperty().bindBidirectional(masterVolume);
        masterSlider.setStyle("-fx-control-inner-background: #d1e0ff; -fx-background-radius: 10;");

        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(masterVolume.asString("%.0f%%"));
        volumeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #5a6c7d;");

        volumeRow.getChildren().addAll(volumeIcon, masterSlider, volumeLabel);

        // Stats and stop button row
        HBox controlsRow = new HBox(20);
        controlsRow.setAlignment(Pos.CENTER);

        // Playing counter
        VBox playingCounter = new VBox(4);
        playingCounter.setAlignment(Pos.CENTER);

        Label playingLabel = new Label("Playing");
        playingLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7d8fa1;");

        Label playingCount = new Label();
        playingCount.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #5a6c7d;");

        playingCount.textProperty().bind(Bindings.createStringBinding(() -> {
            int count = 0;
            for (Sound sound : SOUNDS) {
                BooleanProperty isPlaying = playing.get(sound.id());
                if (isPlaying != null && isPlaying.get()) {
                    count++;
                }
            }
            return String.valueOf(count);
        }, Arrays.stream(SOUNDS)
                .map(sound -> playing.get(sound.id()))
                .filter(Objects::nonNull)
                .toArray(BooleanProperty[]::new)));

        playingCounter.getChildren().addAll(playingLabel, playingCount);

        // Stop All button
        Button stopAll = new Button("⏹️ Stop All");
        stopAll.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-border-color: #ffb8b8; " +
                "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                "-fx-cursor: hand;");
        stopAll.setOnAction(e -> Arrays.stream(SOUNDS).forEach(s -> {
            BooleanProperty isPlaying = playing.get(s.id());
            if (isPlaying != null) {
                isPlaying.set(false);
            }
        }));

        stopAll.setOnMouseEntered(e -> stopAll.setStyle(
                "-fx-background-color: #fff5f5; -fx-text-fill: #ff6b6b; -fx-border-color: #ffb8b8; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                        "-fx-cursor: hand;"));
        stopAll.setOnMouseExited(e -> stopAll.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-border-color: #ffb8b8; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16; -fx-font-weight: 600; " +
                        "-fx-cursor: hand;"));

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

        // FIX: Apply the gradient background directly to the card
        card.setStyle("-fx-background-color: " + sound.gradient() + "; " +
                "-fx-background-radius: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Sound icon with circle background
        Circle iconBackground = new Circle(30);
        iconBackground.setFill(Color.web("rgba(255,255,255,0.3)"));
        iconBackground.setStroke(Color.WHITE);
        iconBackground.setStrokeWidth(2);

        Label icon = new Label(sound.emoji());
        icon.setStyle("-fx-font-size: 24;");

        StackPane iconContainer = new StackPane(iconBackground, icon);

        // Sound name - no background needed since card has color
        Label nameLabel = new Label(sound.name());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");

        // Play/Pause button
        BooleanProperty isPlayingProp = playing.get(sound.id());
        boolean isCurrentlyPlaying = isPlayingProp != null && isPlayingProp.get();

        Button playButton = new Button();
        playButton.setText(isCurrentlyPlaying ? "⏸️ Pause" : "▶️ Play");
        playButton.setStyle(isCurrentlyPlaying
                ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.6); " +
                "-fx-border-width: 1; -fx-background-radius: 18; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.95); -fx-text-fill: #5a6c7d; -fx-background-radius: 18; " +
                "-fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
        playButton.setPrefWidth(120);

        playButton.setOnAction(e -> {
            if (isPlayingProp != null) {
                isPlayingProp.set(!isPlayingProp.get());
            }
        });

        if (isPlayingProp != null) {
            isPlayingProp.addListener((obs, oldVal, newVal) -> {
                playButton.setText(newVal ? "⏸️ Pause" : "▶️ Play");
                playButton.setStyle(newVal
                        ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.6); " +
                        "-fx-border-width: 1; -fx-background-radius: 18; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;"
                        : "-fx-background-color: rgba(255,255,255,0.95); -fx-text-fill: #5a6c7d; -fx-background-radius: 18; " +
                        "-fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
            });
        }

        // Volume control
        VBox volumeControl = new VBox(5);
        volumeControl.setAlignment(Pos.CENTER);
        volumeControl.setPadding(new Insets(8, 12, 8, 12));
        volumeControl.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 12;");

        HBox volumeSliderContainer = new HBox(8);
        volumeSliderContainer.setAlignment(Pos.CENTER);

        Label volIcon = new Label("🔊");
        volIcon.setStyle("-fx-font-size: 12; -fx-text-fill: white;");

        DoubleProperty volumeProp = volumes.get(sound.id());
        Slider volumeSlider = new Slider(0, 100, volumeProp != null ? volumeProp.get() : 50);
        volumeSlider.setPrefWidth(80);
        volumeSlider.setStyle("-fx-control-inner-background: rgba(255,255,255,0.6);");
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
        content.setStyle("-fx-background-color: transparent;"); // FIX: Make content transparent

        card.getChildren().add(content);

        return card;
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
            BooleanProperty isPlayingProp = playing.get(sound.id());
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
                BooleanProperty isPlayingProp = playing.get(sound.id());
                if (isPlayingProp != null && isPlayingProp.get()) {
                    return true;
                }
            }
            return false;
        }, Arrays.stream(SOUNDS)
                .map(sound -> playing.get(sound.id()))
                .filter(Objects::nonNull)
                .toArray(BooleanProperty[]::new)));

        nowPlayingCard.managedProperty().bind(nowPlayingCard.visibleProperty());

        return nowPlayingCard;
    }

    private void updateNowPlayingDisplay(FlowPane container) {
        container.getChildren().clear();

        for (Sound sound : SOUNDS) {
            BooleanProperty isPlayingProp = playing.get(sound.id());
            if (isPlayingProp != null && isPlayingProp.get()) {
                HBox soundChip = new HBox(8);
                soundChip.setAlignment(Pos.CENTER_LEFT);
                soundChip.setPadding(new Insets(8, 12, 8, 12));
                soundChip.setStyle(sound.gradient() + "; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

                Label icon = new Label(sound.emoji());
                icon.setStyle("-fx-font-size: 12;");

                Label name = new Label(sound.name());
                name.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: white;");

                DoubleProperty volumeProp = volumes.get(sound.id());
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
                stopBtn.setOnAction(e -> {
                    if (isPlayingProp != null) {
                        isPlayingProp.set(false);
                    }
                });

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
                new String[]{"rain", "coffee"}, new double[]{60, 40}, "#ffd9b3");
        Button natureEscape = createPresetButton("🌲 Nature Escape", "Forest + Gentle Wind",
                new String[]{"forest", "wind"}, new double[]{70, 30}, "#c6f5d5");
        Button oceanSerenity = createPresetButton("🌊 Ocean Serenity", "Waves + Soft Piano",
                new String[]{"waves", "piano"}, new double[]{50, 60}, "#b3e6ff");
        Button rainyStudy = createPresetButton("🎹 Rainy Study", "Light Rain + Piano",
                new String[]{"rain", "piano"}, new double[]{40, 70}, "#e6d9ff");

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
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #5a6c7d;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7d8fa1;");

        textContent.getChildren().addAll(titleLabel, descLabel);
        button.setGraphic(textContent);

        button.setOnAction(e -> setPresetSounds(soundIds, volumes));

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: " + borderColor + "20; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-border-color: " + borderColor + "; " +
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-padding: 16; " +
                    "-fx-cursor: hand;");
        });

        return button;
    }

    private VBox createCard(String title, double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #5a6c7d;");
        titleLabel.setPadding(new Insets(20, 20, 10, 20));
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().add(titleLabel);
        return card;
    }

    private void setPresetSounds(String[] soundIds, double[] volumes) {
        Arrays.stream(SOUNDS).forEach(s -> {
            BooleanProperty isPlayingProp = playing.get(s.id());
            if (isPlayingProp != null) {
                isPlayingProp.set(false);
            }
        });

        for (int i = 0; i < soundIds.length; i++) {
            String soundId = soundIds[i];
            double volume = volumes[i];

            BooleanProperty playingProp = playing.get(soundId);
            DoubleProperty volumeProp = this.volumes.get(soundId);

            if (playingProp != null) playingProp.set(true);
            if (volumeProp != null) volumeProp.set(volume);
        }
    }

    private void loadAllSounds() {
        String[] files = {"rain.wav", "coffee_shop.wav", "ocean_waves.wav", "wind.wav", "forest.wav", "piano_ambient.wav"};
        for (int i = 0; i < SOUNDS.length; i++) {
            Sound s = SOUNDS[i];
            String path = "/Sounds/" + files[i];
            URL url = getClass().getResource(path);

            if (url == null) {
                System.err.println("Missing: " + path);
                initializeSoundProperties(s.id());
                continue;
            }

            try {
                Media media = new Media(url.toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(0);

                DoubleProperty vol = new SimpleDoubleProperty(50);
                BooleanProperty isPlaying = new SimpleBooleanProperty(false);

                Runnable update = () -> player.setVolume(vol.get() / 100.0 * masterVolume.get() / 100.0);
                vol.addListener((o, ov, nv) -> update.run());
                masterVolume.addListener((o, ov, nv) -> update.run());
                isPlaying.addListener((o, ov, nv) -> {
                    if (nv && player != null) {
                        fadeIn(player, vol.get());
                    } else if (player != null) {
                        fadeOutAndPause(player);
                    }
                });
                players.put(s.id(), player);
                volumes.put(s.id(), vol);
                playing.put(s.id(), isPlaying);

            } catch (Exception e) {
                System.err.println("Failed to load sound: " + files[i] + " → " + e.getMessage());
                initializeSoundProperties(s.id());
            }
        }
    }

    private void initializeSoundProperties(String soundId) {
        if (!volumes.containsKey(soundId)) {
            volumes.put(soundId, new SimpleDoubleProperty(50));
        }
        if (!playing.containsKey(soundId)) {
            playing.put(soundId, new SimpleBooleanProperty(false));
        }
    }

    private void fadeIn(MediaPlayer p, double target) {
        if (p == null) return;
        p.setVolume(0);
        p.play();
        Timeline fade = new Timeline(new KeyFrame(Duration.seconds(1.8),
                new KeyValue(p.volumeProperty(), target / 100.0 * masterVolume.get() / 100.0, Interpolator.EASE_IN)));
        fade.play();
    }

    private void fadeOutAndPause(MediaPlayer p) {
        if (p == null) return;
        Timeline fade = new Timeline(new KeyFrame(Duration.seconds(1),
                new KeyValue(p.volumeProperty(), 0, Interpolator.EASE_OUT)));
        fade.setOnFinished(e -> p.pause());
        fade.play();
    }
}

class FadeTransition {
    static void fade(Node node, double durationSec, double to) {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.seconds(durationSec), node);
        ft.setToValue(to);
        ft.play();
    }
}