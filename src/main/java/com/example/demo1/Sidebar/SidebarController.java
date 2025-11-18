// src/main/java/com/example/demo1/Sidebar/SidebarController.java
package com.example.demo1.Sidebar;

import com.example.demo1.Dashboard;
import com.example.demo1.Calendar.CalendarView;
import com.example.demo1.ToDoList.TodoView;
import com.example.demo1.WhiteNoise.WhiteNoiseView;
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
        System.out.println("Navigating to: " + tabId);
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

        switch (page) {
            case "dashboard" -> new Dashboard(stage).show();
            case "calendar" -> new CalendarView(stage).show();
            case "music" -> new WhiteNoiseView().createAndShow(stage, this, "Aabia");
            case "todos" -> new TodoView(stage).show();
            // case "pet" -> new PetView(stage).show();
            default -> System.out.println("No page for: " + page);
        }
    }
}