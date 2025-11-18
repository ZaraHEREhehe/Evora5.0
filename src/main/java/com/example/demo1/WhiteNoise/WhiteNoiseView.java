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
import javafx.scene.paint.LinearGradient;
import java.net.URL;
import java.util.*;

public class WhiteNoiseView {

    private final Map<String, MediaPlayer> players = new HashMap<>();
    private final Map<String, DoubleProperty> volumes = new HashMap<>();
    private final Map<String, BooleanProperty> playing = new HashMap<>();
    private final DoubleProperty masterVolume = new SimpleDoubleProperty(70);

    private record Sound(String id, String name, String emoji, String gradientCss) {
    }

    private final Sound[] SOUNDS = {
            new Sound("rain", "Rain", "🌧️", "linear-gradient(to bottom right, #93c5fd, #60a5fa)"),
            new Sound("coffee", "Coffee Shop", "☕", "linear-gradient(to bottom right, #fde68a, #f97316)"),
            new Sound("waves", "Ocean Waves", "🌊", "linear-gradient(to bottom right, #67e8f9, #22d3ee)"),
            new Sound("wind", "Wind", "💨", "linear-gradient(to bottom right, #e2e8f0, #94a3b8)"),
            new Sound("forest", "Forest", "🌲", "linear-gradient(to bottom right, #86efac, #22c55e)"),
            new Sound("piano", "Piano", "🎹", "linear-gradient(to bottom right, #e9d5ff, #c084fc)")
    };

    public void createAndShow(Stage stage, SidebarController sidebarController, String userName) {
        // Create main layout with sidebar and content
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fdf7ff;");

        // Create and add sidebar
        Sidebar sidebar = new Sidebar(sidebarController, userName);
        root.setLeft(sidebar);

        // Create white noise content
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

    // Update your existing createAndShow method to be compatible
    public void createAndShow(Stage stage) {
        // Create a temporary sidebar controller for backward compatibility
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

        VBox root = new VBox(32);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("White Noise Player");
        title.setStyle("-fx-font-size: 36; -fx-font-weight: 700; -fx-text-fill: #374151;");
        Label subtitle = new Label("Create your perfect ambient soundscape");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16;");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);

        VBox content = new VBox(24,
                header,
                createMasterCard(),
                createSoundGrid(),
                createNowPlayingCard(),
                createPresetsCard()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().add(scroll);
        return root;
    }

    // ... rest of your existing methods remain exactly the same ...
    private void loadAllSounds() {
        String[] files = {"rain.wav", "coffee_shop.wav", "ocean_waves.wav", "wind.wav", "forest.wav", "piano_ambient.wav"};
        for (int i = 0; i < SOUNDS.length; i++) {
            Sound s = SOUNDS[i];
            String path = "/Sounds/" + files[i];
            URL url = getClass().getResource(path);

            if (url == null) {
                System.err.println("Missing: " + path);
                // Initialize with default values even if sound file is missing
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
                // Initialize with default values even if loading fails
                initializeSoundProperties(s.id());
            }
        }
    }

    private void initializeSoundProperties(String soundId) {
        // Initialize properties even if sound file is missing
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

    private Node createMasterCard() {
        VBox card = card("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 24;");
        Label title = new Label("Master Controls");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: 600; -fx-text-fill: #374151;");

        Slider masterSlider = slider(masterVolume, 0, 100);
        Label volLabel = new Label();
        volLabel.textProperty().bind(masterVolume.asString("%.0f%%"));
        volLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        // Use emoji for volume instead of icon
        Label volumeEmoji = new Label("🔊");
        volumeEmoji.setStyle("-fx-font-size: 20; -fx-text-fill: #6b7280;");

        HBox sliderRow = new HBox(12, volumeEmoji, new Region(), masterSlider, volLabel);
        HBox.setHgrow(sliderRow.getChildren().get(2), Priority.ALWAYS);
        sliderRow.setAlignment(Pos.CENTER_LEFT);

        Button stopAll = new Button("Stop All");
        stopAll.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 20; -fx-padding: 10 20;");
        stopAll.setOnAction(e -> Arrays.stream(SOUNDS).forEach(s -> {
            BooleanProperty isPlaying = playing.get(s.id());
            if (isPlaying != null) {
                isPlaying.set(false);
            }
        }));

        VBox content = new VBox(16, title, sliderRow, stopAll);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.CENTER);
        card.getChildren().add(content);
        return card;
    }

    private Node createSoundGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < SOUNDS.length; i++) {
            grid.add(createSoundCard(SOUNDS[i]), i % 3, i / 3);
        }
        return grid;
    }

    private Node createSoundCard(Sound s) {
        VBox card = card(s.gradientCss() + "; -fx-background-radius: 24;");

        // Use emoji instead of FontIcon
        Label emojiLabel = new Label(s.emoji());
        emojiLabel.setStyle("-fx-font-size: 48;");

        Circle iconBg = new Circle(40, Color.rgb(255, 255, 255, 0.25));
        iconBg.setStroke(Color.WHITE);
        iconBg.setStrokeWidth(2);
        StackPane iconPane = new StackPane(iconBg, emojiLabel);

        Label name = new Label(s.name());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: 600;");

        // Safe access to playing property
        BooleanProperty isPlayingProp = playing.get(s.id());
        boolean isCurrentlyPlaying = isPlayingProp != null && isPlayingProp.get();

        Button playBtn = new Button(isCurrentlyPlaying ? "Pause" : "Play");
        playBtn.setStyle(isCurrentlyPlaying
                ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 22; -fx-font-weight: 600;"
                : "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 22; -fx-font-weight: 600;");
        playBtn.setPrefSize(140, 44);

        // Use emoji for play/pause instead of icons
        Label playEmoji = new Label(isCurrentlyPlaying ? "⏸️" : "▶️");
        playEmoji.setStyle("-fx-font-size: 16;");
        playBtn.setGraphic(playEmoji);

        playBtn.setOnAction(e -> {
            if (isPlayingProp != null) {
                isPlayingProp.set(!isPlayingProp.get());
            }
        });

        if (isPlayingProp != null) {
            isPlayingProp.addListener((o, ov, nv) -> {
                playBtn.setText(nv ? "Pause" : "Play");
                playEmoji.setText(nv ? "⏸️" : "▶️");
                playBtn.setStyle(nv
                        ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 22; -fx-font-weight: 600;"
                        : "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 22; -fx-font-weight: 600;");
            });
        }

        // Safe access to volume property
        DoubleProperty volumeProp = volumes.get(s.id());
        Slider volSlider = slider(volumeProp != null ? volumeProp : new SimpleDoubleProperty(50), 0, 100);
        Label volLabel = new Label();
        if (volumeProp != null) {
            volLabel.textProperty().bind(volumeProp.asString("%.0f%%"));
        } else {
            volLabel.setText("50%");
        }
        volLabel.setStyle("-fx-text-fill: white;");

        // Use emoji for volume instead of icon
        Label volumeEmoji = new Label("🔊");
        volumeEmoji.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

        HBox volRow = new HBox(8, volumeEmoji, volSlider, volLabel);
        if (isPlayingProp != null) {
            volRow.setOpacity(isPlayingProp.get() ? 1 : 0);
            isPlayingProp.addListener((o, ov, nv) -> FadeTransition.fade(volRow, 0.3, nv ? 1 : 0));
        } else {
            volRow.setOpacity(0);
        }

        VBox content = new VBox(20, iconPane, name, playBtn, volRow);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(32));
        card.getChildren().add(content);
        return card;
    }

    private Node createNowPlayingCard() {
        VBox card = card("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 24;");
        Label title = new Label("Now Playing");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: 600; -fx-text-fill: #374151;");

        FlowPane flow = new FlowPane(12, 12);
        flow.setAlignment(Pos.CENTER);

        Arrays.stream(SOUNDS).forEach(s -> {
            BooleanProperty isPlayingProp = playing.get(s.id());
            DoubleProperty volumeProp = volumes.get(s.id());

            if (isPlayingProp != null && volumeProp != null) {
                Label chip = new Label(s.emoji() + " " + s.name() + "  " + volumeProp.intValue() + "%");
                chip.setStyle("-fx-background-radius: 20; -fx-padding: 8 16; -fx-text-fill: white; -fx-font-weight: 600;");
                chip.backgroundProperty().bind(
                        Bindings.createObjectBinding(() ->
                                        new Background(new BackgroundFill(
                                                LinearGradient.valueOf(s.gradientCss()),
                                                new CornerRadii(20), Insets.EMPTY)),
                                isPlayingProp)
                );
                chip.visibleProperty().bind(isPlayingProp);
                flow.getChildren().add(chip);
            }
        });

        VBox content = new VBox(16, title, flow);
        content.setPadding(new Insets(24));
        card.getChildren().add(content);
        return card;
    }

    private Node createPresetsCard() {
        VBox card = card("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 24;");
        Label title = new Label("Popular Combinations");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: 600; -fx-text-fill: #374151;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        String[][] presets = {
                {"Cozy Café", "rain", "60", "coffee", "40"},
                {"Nature Escape", "forest", "70", "wind", "30"},
                {"Ocean Serenity", "waves", "50", "piano", "60"},
                {"Rainy Study", "rain", "40", "piano", "70"}
        };

        for (int i = 0; i < presets.length; i++) {
            String[] p = presets[i];
            // Add emojis to the preset buttons
            String sound1Emoji = getEmojiForSound(p[1]);
            String sound2Emoji = getEmojiForSound(p[3]);
            Button btn = new Button(p[0] + "\n" + sound1Emoji + " " + p[1] + " + " + sound2Emoji + " " + p[3]);
            btn.setStyle("-fx-background-radius: 20; -fx-padding: 16; -fx-text-fill: #374151; -fx-font-size: 14;");
            btn.setPrefWidth(280);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setOnAction(e -> applyPreset(p[1], Integer.parseInt(p[2]), p[3], Integer.parseInt(p[4])));
            grid.add(btn, i % 2, i / 2);
        }

        VBox content = new VBox(16, title, grid);
        content.setPadding(new Insets(24));
        card.getChildren().add(content);
        return card;
    }

    private String getEmojiForSound(String soundId) {
        for (Sound s : SOUNDS) {
            if (s.id().equals(soundId)) {
                return s.emoji();
            }
        }
        return "";
    }

    private void applyPreset(String id1, int vol1, String id2, int vol2) {
        Arrays.stream(SOUNDS).forEach(s -> {
            BooleanProperty isPlayingProp = playing.get(s.id());
            if (isPlayingProp != null) {
                isPlayingProp.set(false);
            }
        });

        BooleanProperty playing1 = playing.get(id1);
        BooleanProperty playing2 = playing.get(id2);
        DoubleProperty volume1 = volumes.get(id1);
        DoubleProperty volume2 = volumes.get(id2);

        if (playing1 != null) playing1.set(true);
        if (playing2 != null) playing2.set(true);
        if (volume1 != null) volume1.set(vol1);
        if (volume2 != null) volume2.set(vol2);
    }

    // Helper methods
    private VBox card(String style) {
        VBox v = new VBox();
        v.setStyle(style + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");
        return v;
    }

    private Slider slider(DoubleProperty value, double min, double max) {
        Slider s = new Slider(min, max, value.get());
        s.valueProperty().bindBidirectional(value);
        s.setPrefWidth(300);
        s.setStyle("-fx-background-radius: 20;");
        return s;
    }
}

class FadeTransition {
    static void fade(Node node, double durationSec, double to) {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.seconds(durationSec), node);
        ft.setToValue(to);
        ft.play();
    }
}