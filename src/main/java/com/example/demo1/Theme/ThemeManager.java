// src/main/java/com/example/demo1/Theme/ThemeManager.java
package com.example.demo1.Theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private static Theme currentTheme;
    private List<ThemeChangeListener> listeners = new ArrayList<>();

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private ThemeManager() {
        this.currentTheme = new PastelTheme();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        notifyThemeChanged();
    }

    public void setTheme(String themeName) {
        switch (themeName.toLowerCase()) {
            case "galaxy":
                this.currentTheme = new GalaxyTheme();
                break;
            case "pastel":
            default:
                this.currentTheme = new PastelTheme();
                break;
        }
        notifyThemeChanged();
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public String getThemeName() {
        if (currentTheme instanceof GalaxyTheme) {
            return "galaxy";
        } else {
            return "pastel";
        }
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyThemeChanged() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }
}