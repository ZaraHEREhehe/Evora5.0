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
     * Trigger navigation to a specific tab
     */
    public void navigate(String tabId) {
        if (onTabChange != null) {
            onTabChange.accept(tabId);
        }
        System.out.println("✨ Navigating to: " + tabId);
    }

    /**
     * Programmatically navigate to a tab (useful for initial setup)
     */
    public void setActiveTab(String tabId) {
        navigate(tabId);
    }

    /**
     * Get the current active tab (if needed for state management)
     */
    public String getCurrentTab() {
        // This would need to be implemented with state tracking
        // For now, it's a placeholder for future enhancement
        return "dashboard";
    }
}