package com.example.demo1.Sidebar;

import java.util.function.Consumer;

/**
 * Sidebar controller to handle navigation callbacks
 */
public class SidebarController {

    private Consumer<String> onTabChange;

    public SidebarController() {}

    /**
     * Set the callback to handle navigation when a sidebar item is clicked
     */
    public void setOnTabChange(Consumer<String> callback) {
        this.onTabChange = callback;
    }

    /**
     * Trigger navigation
     */
    public void navigate(String tabId) {
        if (onTabChange != null) {
            onTabChange.accept(tabId);
        }
        System.out.println("Navigating to: " + tabId);
    }
}
