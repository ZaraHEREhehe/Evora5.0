// src/main/java/com/example/Evora/Theme/Theme.java
package com.example.Evora.Theme;

public interface Theme {
    // Basic colors
    String getPrimaryColor();
    String getSecondaryColor();
    String getAccentColor();
    String getBackgroundColor();
    String getCardColor();
    String getTextPrimary();
    String getTextSecondary();
    String getButtonColor();
    String getHoverColor();
    String getSuccessColor();
    String getWarningColor();
    String getErrorColor();

    // Component-specific colors
    String getStatCardColor1();
    String getStatCardColor2();
    String getStatCardColor3();
    String getStatCardColor4();
    String getFocusBoxColor();
    String getAnalyticsBoxColor();
    String getMiniCardColor();

    // Analytics-specific colors
    String getBgPrimary();
    String getBgCard();
    String getBgNav();
    String getTextDark();
    String getBorderPrimary();
    String getAccentPurple();
    String getAccentPink();
    String getAccentGreen();
    String getAccentBlue();
    String getAccentYellow();
    String getAccentOrange();

    // Gradient colors for analytics
    String getGradientTaskStart();
    String getGradientTaskEnd();
    String getGradientFocusStart();
    String getGradientFocusEnd();
    String getGradientStreakStart();
    String getGradientStreakEnd();
    String getGradientProductivityStart();
    String getGradientProductivityEnd();
    String getGradientMoodStart();
    String getGradientMoodEnd();

    // Chart-specific colors
    String getChartBackground();
    String getChartGrid();
    String getChartText();
    String getTextColor();
}