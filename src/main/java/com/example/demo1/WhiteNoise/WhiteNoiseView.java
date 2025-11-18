// File: src/main/java/com/example/demo1/WhiteNoise/WhiteNoiseView.java
package com.example.demo1.WhiteNoise;

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
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.*;

public class WhiteNoiseView {

    private final Map<String, MediaPlayer> players = new HashMap<>();
    private final Map<String, DoubleProperty> volumes = new HashMap<>();
    private final Map<String, BooleanProperty> playing = new HashMap<>();
    private final DoubleProperty masterVolume = new SimpleDoubleProperty(70);

    private record Sound(String id, String name, String iconLiteral, String gradientCss) {
    }

    private final Sound[] SOUNDS = {
            new Sound("rain", "Rain", "fa6regular-cloud-rain", "linear-gradient(to bottom right, #93c5fd, #60a5fa)"),
            new Sound("coffee", "Coffee Shop", "fa6regular-mug-hot", "linear-gradient(to bottom right, #fde68a, #f97316)"),
            new Sound("waves", "Ocean Waves", "fa6regular-water", "linear-gradient(to bottom right, #67e8f9, #22d3ee)"),
            new Sound("wind", "Wind", "fa6regular-wind", "linear-gradient(to bottom right, #e2e8f0, #94a3b8)"),
            new Sound("forest", "Forest", "fa6regular-tree", "linear-gradient(to bottom right, #86efac, #22c55e)"),
            new Sound("piano", "Piano", "fa6regular-music", "linear-gradient(to bottom right, #e9d5ff, #c084fc)")
    };

    // Add this method inside WhiteNoiseView class
    public void createAndShow(Stage stage) {
        StackPane root = new StackPane(create());
        root.setStyle("-fx-background-color: #fdf7ff;");

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

    private void loadAllSounds() {
        String[] files = {"rain.wav", "coffee_shop.wav", "ocean_waves.wav", "wind.wav", "forest.wav", "piano_ambient.wav"};
        for (int i = 0; i < SOUNDS.length; i++) {
            Sound s = SOUNDS[i];
            String path = "/Sounds/white_noise/" + files[i];
            URL url = getClass().getResource(path);

            if (url == null) {
                System.err.println("Missing: " + path);
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
                    if (nv) {
                        fadeIn(player, vol.get());
                    } else {
                        fadeOutAndPause(player);
                    }
                });
                players.put(s.id(), player);
                volumes.put(s.id(), vol);
                playing.put(s.id(), isPlaying);

            } catch (Exception e) {
                System.err.println("Failed to load sound: " + files[i] + " → " + e.getMessage());
            }
        }
    }

    private void fadeIn(MediaPlayer p, double target) {
        p.setVolume(0);
        p.play();
        Timeline fade = new Timeline(new KeyFrame(Duration.seconds(1.8),
                new KeyValue(p.volumeProperty(), target / 100.0 * masterVolume.get() / 100.0, Interpolator.EASE_IN)));
        fade.play();
    }

    private void fadeOutAndPause(MediaPlayer p) {
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

        HBox sliderRow = new HBox(12, icon("fa6regular-volume-high", "#6b7280"), new Region(), masterSlider, volLabel);
        HBox.setHgrow(sliderRow.getChildren().get(2), Priority.ALWAYS);
        sliderRow.setAlignment(Pos.CENTER_LEFT);

        Button stopAll = new Button("Stop All");
        stopAll.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 20; -fx-padding: 10 20;");
        stopAll.setOnAction(e -> Arrays.stream(SOUNDS).forEach(s -> playing.get(s.id()).set(false)));

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
        FontIcon icon = new FontIcon(s.iconLiteral());
        icon.setIconSize(48);
        icon.setIconColor(Color.WHITE);

        Circle iconBg = new Circle(40, Color.rgb(255, 255, 255, 0.25));
        iconBg.setStroke(Color.WHITE);
        iconBg.setStrokeWidth(2);
        StackPane iconPane = new StackPane(iconBg, icon);

        Label name = new Label(s.name());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: 600;");

        Button playBtn = new Button(playing.get(s.id()).get() ? "Pause" : "Play");
        playBtn.setStyle(playing.get(s.id()).get()
                ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white;"
                : "-fx-background-color: white; -fx-text-fill: #374151;");
        playBtn.setPrefSize(140, 44);
        playBtn.setStyle(playBtn.getStyle() + "-fx-background-radius: 22; -fx-font-weight: 600;");

        FontIcon playIcon = new FontIcon(playing.get(s.id()).get() ? "fa6regular-pause" : "fa6regular-play");
        playIcon.setIconColor(playing.get(s.id()).get() ? Color.WHITE : Color.web("#374151"));
        playIcon.setIconSize(18);
        playBtn.setGraphic(playIcon);

        playBtn.setOnAction(e -> playing.get(s.id()).set(!playing.get(s.id()).get()));
        playing.get(s.id()).addListener((o, ov, nv) -> {
            playBtn.setText(nv ? "Pause" : "Play");
            playIcon.setIconLiteral(nv ? "far-pause" : "far-play");
            playIcon.setIconColor(nv ? Color.WHITE : Color.web("#374151"));
            playBtn.setStyle(nv
                    ? "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 22;"
                    : "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 22;");
        });

        Slider volSlider = slider(volumes.get(s.id()), 0, 100);
        Label volLabel = new Label();
        volLabel.textProperty().bind(volumes.get(s.id()).asString("%.0f%%"));
        volLabel.setStyle("-fx-text-fill: white;");

        HBox volRow = new HBox(8, icon("fa-solid-volume-high", "white"), volSlider, volLabel);
        volRow.setOpacity(playing.get(s.id()).get() ? 1 : 0);
        playing.get(s.id()).addListener((o, ov, nv) -> FadeTransition.fade(volRow, 0.3, nv ? 1 : 0));

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
            Label chip = new Label(s.name() + "  " + volumes.get(s.id()).intValue() + "%");
            chip.setStyle("-fx-background-radius: 20; -fx-padding: 8 16; -fx-text-fill: white;");
            chip.backgroundProperty().bind(
                    Bindings.createObjectBinding(() ->
                                    new Background(new BackgroundFill(
                                            LinearGradient.valueOf(s.gradientCss()),
                                            new CornerRadii(20), Insets.EMPTY)),
                            playing.get(s.id()))
            );
            chip.visibleProperty().bind(playing.get(s.id()));
            flow.getChildren().add(chip);
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
            Button btn = new Button(p[0] + "\n" + p[1] + " + " + p[3]);
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

    private void applyPreset(String id1, int vol1, String id2, int vol2) {
        Arrays.stream(SOUNDS).forEach(s -> playing.get(s.id()).set(false));
        playing.get(id1).set(true);
        volumes.get(id1).set(vol1);
        playing.get(id2).set(true);
        volumes.get(id2).set(vol2);
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

    private FontIcon icon(String literal, String color) {
        FontIcon i = new FontIcon(literal);
        i.setIconSize(20);
        i.setIconColor(Color.web(color));
        return i;
    }
}

class FadeTransition {
    static void fade(Node node, double durationSec, double to) {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.seconds(durationSec), node);
        ft.setToValue(to);
        ft.play();
    }
}