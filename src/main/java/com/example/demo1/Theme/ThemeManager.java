package com.example.demo1.Theme;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme {
        PASTEL,
        GALAXY,
        NATURE
    }

    private static Theme currentTheme = Theme.PASTEL;

    public static void applyTheme(Scene scene, Theme theme) {
        scene.getStylesheets().clear();
        currentTheme = theme;

        String cssFile = null;

        switch (theme) {
            case PASTEL:
                cssFile = "/com/example/demo1/Theme/pastel.css";
                break;
            case GALAXY:
                cssFile = "/com/example/demo1/Theme/galaxy.css";
                break;
            case NATURE:
                cssFile = "/com/example/demo1/Theme/nature.css";
                break;
        }

        if (cssFile != null && ThemeManager.class.getResource(cssFile) != null) {
            scene.getStylesheets().add(ThemeManager.class.getResource(cssFile).toExternalForm());
        } else {
            System.err.println("Theme CSS file not found: " + cssFile);
        }
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}
