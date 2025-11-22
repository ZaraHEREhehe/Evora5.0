// src/main/java/com/example/demo1/Theme/PastelTheme.java
package com.example.demo1.Theme;

public class PastelTheme implements Theme {
    @Override public String getPrimaryColor() { return Pastel.PINK; }
    @Override public String getSecondaryColor() { return Pastel.LAVENDER; }
    @Override public String getAccentColor() { return Pastel.PURPLE; }
    @Override public String getBackgroundColor() { return Pastel.BLUSH; }
    @Override public String getCardColor() { return Pastel.WHITE; }
    @Override public String getTextPrimary() { return Pastel.FOREST; }
    @Override public String getTextSecondary() { return Pastel.SAGE; }
    @Override public String getButtonColor() { return Pastel.LAVENDER; }
    @Override public String getHoverColor() { return Pastel.LIGHT_PURPLE; }
    @Override public String getSuccessColor() { return Pastel.SUCCESS_GREEN; }
    @Override public String getWarningColor() { return Pastel.WARNING_ORANGE; }
    @Override public String getErrorColor() { return Pastel.ERROR_RED; }

    @Override public String getStatCardColor1() { return Pastel.PINK; }
    @Override public String getStatCardColor2() { return Pastel.LAVENDER; }
    @Override public String getStatCardColor3() { return Pastel.BLUE; }
    @Override public String getStatCardColor4() { return Pastel.LILAC; }
    @Override public String getFocusBoxColor() { return Pastel.IVORY; }
    @Override public String getAnalyticsBoxColor() { return Pastel.IVORY; }
    @Override public String getMiniCardColor() { return Pastel.DUSTY_PINK; }

    // Analytics-specific colors
    @Override public String getBgPrimary() { return Pastel.MIST; }
    @Override public String getBgCard() { return Pastel.WHITE; }
    @Override public String getBgNav() { return Pastel.SKY; }
    @Override public String getTextDark() { return Pastel.FOREST; }
    @Override public String getBorderPrimary() { return Pastel.LIGHT_PURPLE; }
    @Override public String getAccentPurple() { return Pastel.GRADIENT_PURPLE; }
    @Override public String getAccentPink() { return Pastel.GRADIENT_PINK; }
    @Override public String getAccentGreen() { return Pastel.SUCCESS_GREEN; }
    @Override public String getAccentBlue() { return Pastel.BLUE; }
    @Override public String getAccentYellow() { return Pastel.LEMON; }
    @Override public String getAccentOrange() { return Pastel.PEACH; }

    // Gradient colors for analytics cards
    @Override public String getGradientTaskStart() { return Pastel.LILAC; }
    @Override public String getGradientTaskEnd() { return Pastel.ROSE; }
    @Override public String getGradientFocusStart() { return Pastel.SKY; }
    @Override public String getGradientFocusEnd() { return Pastel.BLUE; }
    @Override public String getGradientStreakStart() { return Pastel.LAVENDER; }
    @Override public String getGradientStreakEnd() { return Pastel.LILAC; }
    @Override public String getGradientProductivityStart() { return Pastel.MINT; }
    @Override public String getGradientProductivityEnd() { return Pastel.LIGHT_GREEN; }
    @Override public String getGradientMoodStart() { return Pastel.LILAC; }
    @Override public String getGradientMoodEnd() { return Pastel.ROSE; }

    // Chart-specific colors
    @Override public String getChartBackground() { return Pastel.WHITE; }
    @Override public String getChartGrid() { return Pastel.GRAY_200; }
    @Override public String getChartText() { return Pastel.FOREST; }
    @Override public String getTextColor() {return "#000000";}
}