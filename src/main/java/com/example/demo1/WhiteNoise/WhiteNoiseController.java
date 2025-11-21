// File: src/main/java/com/example/demo1/WhiteNoise/WhiteNoiseController.java
package com.example.demo1.WhiteNoise;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

public class WhiteNoiseController {
    // Singleton instance
    private static WhiteNoiseController instance;

    private final Map<String, MediaPlayer> players = new HashMap<>();
    private final Map<String, DoubleProperty> volumes = new HashMap<>();
    private final Map<String, BooleanProperty> playing = new HashMap<>();
    private final DoubleProperty masterVolume = new SimpleDoubleProperty(70);

    // Private constructor for singleton
    private WhiteNoiseController() {
        loadAllSounds();
    }

    // Singleton getInstance method
    public static WhiteNoiseController getInstance() {
        if (instance == null) {
            instance = new WhiteNoiseController();
        }
        return instance;
    }

    // Getters for properties
    public Map<String, DoubleProperty> getVolumes() {
        return volumes;
    }

    public Map<String, BooleanProperty> getPlaying() {
        return playing;
    }

    public DoubleProperty getMasterVolume() {
        return masterVolume;
    }

    public void toggleSound(String soundId) {
        BooleanProperty isPlaying = playing.get(soundId);
        if (isPlaying != null) {
            isPlaying.set(!isPlaying.get());
        }
    }

    public void stopAllSounds() {
        playing.values().forEach(isPlaying -> isPlaying.set(false));
    }

    public void setPresetSounds(String[] soundIds, double[] volumes) {
        // Stop all sounds first
        playing.values().forEach(isPlaying -> isPlaying.set(false));

        // Start and set volumes for specified sounds
        for (int i = 0; i < soundIds.length; i++) {
            String soundId = soundIds[i];
            double volume = volumes[i];

            BooleanProperty playingProp = playing.get(soundId);
            DoubleProperty volumeProp = this.volumes.get(soundId);

            if (playingProp != null) playingProp.set(true);
            if (volumeProp != null) volumeProp.set(volume);
        }
    }

    public int getPlayingCount() {
        int count = 0;
        for (BooleanProperty isPlaying : playing.values()) {
            if (isPlaying != null && isPlaying.get()) {
                count++;
            }
        }
        return count;
    }

    private void loadAllSounds() {
        String[] files = {"rain.wav", "coffee_shop.wav", "ocean_waves.wav", "wind.wav", "forest.wav", "piano_ambient.wav"};
        String[] soundIds = {"rain", "coffee", "waves", "wind", "forest", "piano"};

        for (int i = 0; i < soundIds.length; i++) {
            String soundId = soundIds[i];
            String path = "/Sounds/" + files[i];
            URL url = getClass().getResource(path);

            if (url == null) {
                System.err.println("Missing: " + path);
                initializeSoundProperties(soundId);
                continue;
            }

            try {
                Media media = new Media(url.toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(0);

                DoubleProperty vol = new SimpleDoubleProperty(50);
                BooleanProperty isPlaying = new SimpleBooleanProperty(false);

                // Update volume when individual volume or master volume changes
                Runnable updateVolume = () -> player.setVolume(vol.get() / 100.0 * masterVolume.get() / 100.0);
                vol.addListener((o, ov, nv) -> updateVolume.run());
                masterVolume.addListener((o, ov, nv) -> updateVolume.run());

                // Handle play/pause state changes
                isPlaying.addListener((o, ov, nv) -> {
                    if (nv && player != null) {
                        fadeIn(player, vol.get());
                    } else if (player != null) {
                        fadeOutAndPause(player);
                    }
                });

                players.put(soundId, player);
                volumes.put(soundId, vol);
                playing.put(soundId, isPlaying);

            } catch (Exception e) {
                System.err.println("Failed to load sound: " + files[i] + " → " + e.getMessage());
                initializeSoundProperties(soundId);
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

    private void fadeIn(MediaPlayer player, double target) {
        if (player == null) return;
        player.setVolume(0);
        player.play();
        Timeline fade = new Timeline(new KeyFrame(Duration.seconds(1.8),
                new KeyValue(player.volumeProperty(), target / 100.0 * masterVolume.get() / 100.0, Interpolator.EASE_IN)));
        fade.play();
    }

    private void fadeOutAndPause(MediaPlayer player) {
        if (player == null) return;
        Timeline fade = new Timeline(new KeyFrame(Duration.seconds(1),
                new KeyValue(player.volumeProperty(), 0, Interpolator.EASE_OUT)));
        fade.setOnFinished(e -> player.pause());
        fade.play();
    }

    public void cleanup() {
        players.values().forEach(MediaPlayer::dispose);
        players.clear();
        volumes.clear();
        playing.clear();
    }
}