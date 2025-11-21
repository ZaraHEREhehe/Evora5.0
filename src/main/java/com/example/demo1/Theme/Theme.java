// src/main/java/com/example/demo1/Theme/Theme.java
package com.example.demo1.Theme;

public interface Theme {
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

    // Dashboard specific colors
    String getStatCardColor1();
    String getStatCardColor2();
    String getStatCardColor3();
    String getStatCardColor4();
    String getFocusBoxColor();
    String getAnalyticsBoxColor();
    String getMiniCardColor();
}