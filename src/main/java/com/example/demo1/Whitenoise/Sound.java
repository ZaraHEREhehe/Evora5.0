// Sound.java
package com.example.demo1.Whitenoise;

import javafx.beans.property.*;

public class Sound {
    private final String id;
    private final String name;
    private final String icon;
    private final String startColor;
    private final String endColor;
    private final BooleanProperty isPlaying;
    private final DoubleProperty volume;

    public Sound(String id, String name, String icon, String startColor, String endColor) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.startColor = startColor;
        this.endColor = endColor;
        this.isPlaying = new SimpleBooleanProperty(false);
        this.volume = new SimpleDoubleProperty(50);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getStartColor() { return startColor; }
    public String getEndColor() { return endColor; }
    public boolean isPlaying() { return isPlaying.get(); }
    public double getVolume() { return volume.get(); }

    // Property getters

    public BooleanProperty isPlayingProperty() { return isPlaying; }
    public DoubleProperty volumeProperty() { return volume; }

    // Setters
    public void setPlaying(boolean playing) { this.isPlaying.set(playing); }
    public void setVolume(double volume) { this.volume.set(volume); }
}