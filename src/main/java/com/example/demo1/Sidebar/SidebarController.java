package com.example.demo1.Sidebar;

import com.example.demo1.Dashboard;
import com.example.demo1.Calendar.CalendarView;
import com.example.demo1.Login.LoginView;
import com.example.demo1.ToDoList.TodoView;
import javafx.stage.Stage;

import java.awt.event.WindowEvent;
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


    // Add this method to your Sidebar class
    public void navigateToLogin() {
        try {
            // Get the current stage
            Stage stage = (Stage) this.getStage();

            // Use your existing LoginView to show the login screen
            LoginView loginView = new LoginView();
            loginView.start(stage);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading login page: " + e.getMessage());
        }
    }



} // ← This is the class closing brace