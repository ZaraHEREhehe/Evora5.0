// src/main/java/com/example/Evora/Theme/GalaxyTheme.java
package com.example.Evora.Theme;

public class GalaxyTheme implements Theme {
    @Override public String getPrimaryColor() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getSecondaryColor() { return Galaxy.STAR_BLUE; }
    @Override public String getAccentColor() { return Galaxy.COSMIC_PINK; }
    @Override public String getBackgroundColor() { return Galaxy.DEEP_SPACE; }
    @Override public String getCardColor() { return Galaxy.SPACE_GRAY; }
    @Override public String getTextPrimary() { return Galaxy.MOON_LIGHT; }
    @Override public String getTextSecondary() { return Galaxy.STAR_DUST; }
    @Override public String getButtonColor() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getHoverColor() { return Galaxy.COSMIC_PINK; }
    @Override public String getSuccessColor() { return Galaxy.GALACTIC_TEAL; }
    @Override public String getWarningColor() { return Galaxy.SUPERNOVA_YELLOW; }
    @Override public String getErrorColor() { return Galaxy.COSMIC_PINK; }

    @Override public String getStatCardColor1() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getStatCardColor2() { return Galaxy.STAR_BLUE; }
    @Override public String getStatCardColor3() { return Galaxy.COSMIC_PINK; }
    @Override public String getStatCardColor4() { return Galaxy.GALACTIC_TEAL; }
    @Override public String getFocusBoxColor() { return Galaxy.VOID_BLACK; }
    @Override public String getAnalyticsBoxColor() { return Galaxy.SPACE_GRAY; }
    @Override public String getMiniCardColor() { return Galaxy.NEBULA_PURPLE; }

    // Analytics-specific colors
    @Override public String getBgPrimary() { return Galaxy.DEEP_SPACE; }
    @Override public String getBgCard() { return Galaxy.SPACE_GRAY; }
    @Override public String getBgNav() { return Galaxy.VOID_BLACK; }
    @Override public String getTextDark() { return Galaxy.MOON_LIGHT; }
    @Override public String getBorderPrimary() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getAccentPurple() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getAccentPink() { return Galaxy.COSMIC_PINK; }
    @Override public String getAccentGreen() { return Galaxy.GALACTIC_TEAL; }
    @Override public String getAccentBlue() { return Galaxy.STAR_BLUE; }
    @Override public String getAccentYellow() { return Galaxy.SUPERNOVA_YELLOW; }
    @Override public String getAccentOrange() { return Galaxy.COMET_ORANGE; }

    // Gradient colors for analytics cards
    @Override public String getGradientTaskStart() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getGradientTaskEnd() { return Galaxy.COSMIC_PINK; }
    @Override public String getGradientFocusStart() { return Galaxy.STAR_BLUE; }
    @Override public String getGradientFocusEnd() { return Galaxy.GALACTIC_TEAL; }
    @Override public String getGradientStreakStart() { return Galaxy.NEBULA_PURPLE; }
    @Override public String getGradientStreakEnd() { return Galaxy.STAR_BLUE; }
    @Override public String getGradientProductivityStart() { return Galaxy.GALACTIC_TEAL; }
    @Override public String getGradientProductivityEnd() { return Galaxy.STAR_BLUE; }
    @Override public String getGradientMoodStart() { return Galaxy.COSMIC_PINK; }
    @Override public String getGradientMoodEnd() { return Galaxy.NEBULA_PURPLE; }

    // Chart-specific colors for better visibility
    @Override public String getChartBackground() { return "#FFFFFF"; } // White chart background
    @Override public String getChartGrid() { return "#E5E7EB"; } // Light gray grid
    @Override public String getChartText() { return "#374151"; } // Dark gray text for charts
    @Override public String getTextColor() { return " #FFFFFF";}
}