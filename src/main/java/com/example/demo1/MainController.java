// MainController.java
package com.example.demo1;

import com.example.demo1.Mood.MoodController;
import com.example.demo1.Mood.MoodView;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.Notes.NotesView;
import com.example.demo1.Notes.NotesController;
import javafx.scene.layout.BorderPane;

public class MainController {
    private BorderPane root;
    private Sidebar sidebar;
    private SidebarController sidebarController;
    private String userName;
    private int userId;

    public MainController(BorderPane root, String userName, int userId) {
        this.root = root;
        this.userName = userName;
        this.userId = userId;
        setupSidebar(userName);
        showDashboard(); // Start with dashboard
    }

    private void setupSidebar(String userName) {
        sidebarController = new SidebarController();
        sidebarController.setOnTabChange(this::handleNavigation);
        sidebar = new Sidebar(sidebarController, userName);
        root.setLeft(sidebar);
    }

    private void handleNavigation(String tabId) {
        switch (tabId) {
            case "dashboard":
                showDashboard();
                break;
            case "notes":
                showNotes();
                break;
            case "mood":
                showMood();
                break;
            case "todos":
                System.out.println("TODO: Implement " + tabId);
                break;
            default:
                System.out.println("Module not implemented: " + tabId);
                break;
        }
    }

    private void showDashboard() {
        Dashboard dashboard = new Dashboard();
        root.setCenter(dashboard.getContent());
    }

    private void showNotes() {
        NotesController notesController = new NotesController(userId);
        NotesView notesView = new NotesView(notesController);

        // Create a proper container for the notes view
        BorderPane notesContainer = new BorderPane();
        notesContainer.setCenter(notesView);
        notesContainer.setStyle("-fx-background-color: #fdf7ff;");

        root.setCenter(notesView);
    }

    private void showMood() {
        MoodController moodController = new MoodController(userId);
        MoodView moodView = new MoodView(moodController);
        root.setCenter(moodView);
    }
}