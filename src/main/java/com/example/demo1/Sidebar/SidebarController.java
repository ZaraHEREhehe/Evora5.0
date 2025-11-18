package com.example.demo1.Sidebar;

import com.example.demo1.Dashboard;
import com.example.demo1.Calendar.CalendarView;
import com.example.demo1.ToDoList.TodoView;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class SidebarController {

    private Consumer<String> onTabChange;
    private Stage stage;

    public SidebarController() {}

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    // ← REMOVE THE goTo METHOD COMPLETELY and use this instead:
    public void setOnTabChange(Consumer<String> callback) {
        this.onTabChange = callback;
    }

    public void navigate(String tabId) {
        System.out.println("✨ Navigating to: " + tabId);
        if (onTabChange != null) {
            onTabChange.accept(tabId);
        }
    }

    /**
     * Programmatically navigate to a tab (useful for initial setup)
     */
    public void setActiveTab(String tabId) {
        navigate(tabId);
    }


    public String getCurrentTab() {
        // This would need to be implemented with state tracking
        // For now, it's a placeholder for future enhancement
        return "dashboard";
    }
} // ← This is the class closing brace