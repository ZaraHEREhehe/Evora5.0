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

    public void setOnTabChange(Consumer<String> callback) {
        this.onTabChange = callback;
    }

    public void navigate(String tabId) {
        if (onTabChange != null) {
            onTabChange.accept(tabId);
        }
    }

    // ← THIS IS THE MAGIC METHOD
    public void goTo(String page) {
        if (stage == null) {
            System.err.println("ERROR: Stage not set in SidebarController!");
            return;
        }
    /**
     * Programmatically navigate to a tab (useful for initial setup)
     */
    public void setActiveTab(String tabId) {
        navigate(tabId);
    }

        switch (page) {
            case "dashboard" -> new Dashboard(stage).show();
            case "calendar" -> new CalendarView(stage).show();
            // Add more later:
             case "todos" -> new TodoView(stage).show();
            // case "pet" -> new PetView(stage).show();
            default -> System.out.println("No page for: " + page);
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