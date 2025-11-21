// src/main/java/com/example/demo1/Theme/GalaxyTheme.java
package com.example.demo1.Theme;


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
}