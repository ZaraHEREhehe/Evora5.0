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
}