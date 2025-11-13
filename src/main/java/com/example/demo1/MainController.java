// MainController.java
package com.example.demo1;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.Notes.NotesView;
import com.example.demo1.Notes.NotesController;
import javafx.scene.layout.BorderPane;

public class MainController {
    private BorderPane root;
    private Sidebar sidebar;
    private SidebarController sidebarController;

    public MainController(BorderPane root, String userName) {
        this.root = root;
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
            case "todos":
                // Add your other modules here
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
        NotesController notesController = new NotesController();
        NotesView notesView = new NotesView(notesController);

        // Create a proper container for the notes view
        BorderPane notesContainer = new BorderPane();
        notesContainer.setCenter(notesView);
        notesContainer.setStyle("-fx-background-color: #fdf7ff;");

        root.setCenter(notesView);
    }
}