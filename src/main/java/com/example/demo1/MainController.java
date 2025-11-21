// MainController.java
package com.example.demo1;

import com.example.demo1.Mood.MoodController;
import com.example.demo1.Mood.MoodView;
import com.example.demo1.Pets.PetsController;
import com.example.demo1.Pets.PetsView;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.Notes.NotesView;
import com.example.demo1.Notes.NotesController;
import com.example.demo1.Calendar.CalendarView;
import com.example.demo1.ToDoList.TodoView;
import com.example.demo1.WhiteNoise.WhiteNoiseView;
import com.example.demo1.Theme.Pastel;
import com.example.demo1.Pomodoro.PomodoroController;
import com.example.demo1.Pomodoro.PomodoroView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainController {
    private Stage stage;
    private BorderPane root;
    private Sidebar sidebar;
    private SidebarController sidebarController;
    private String userName;
    private int userId;

    public MainController(Stage stage, String userName, int userId) {
        this.stage = stage;
        this.userName = userName;
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();

        // Sidebar
        sidebarController = new SidebarController();
        sidebarController.setOnTabChange(this::handleNavigation);
        sidebar = new Sidebar(sidebarController, userName);
        root.setLeft(sidebar);

        // Set initial pet in sidebar
        PetsController petsController = new PetsController(userId);
        PetsController.PetInfo initialPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(initialPet.getName(), initialPet.getSpecies(), initialPet.getGifFilename());

        showDashboard();

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenHeight = screenBounds.getHeight();
        double screenWidth = screenBounds.getWidth();

        // Create scene with full screen dimensions
        Scene scene = new Scene(root, screenWidth, screenHeight);
        scene.getRoot().setStyle("-fx-background-color: " + Pastel.BLUSH + ";");

        stage.setScene(scene);
        stage.setTitle("Pastel Productivity Dashboard");

        // Set minimum size - minimum width but full height
        stage.setMinWidth(1300);
        stage.setMinHeight(screenHeight);

        // Start maximized (full screen)
        stage.setMaximized(true);

        // Allow normal window management
        stage.setResizable(true);

        // Add listener to maintain full height when resizing
        stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            // When width changes, maintain the full height
            if (stage.isMaximized()) {
                return; // Don't interfere when maximized
            }
            // Keep the full screen height
            stage.setHeight(screenHeight);
        });

        stage.show();
    }

    private void handleNavigation(String tab) {
        switch (tab) {
            case "dashboard":
                showDashboard();
                break;
            case "timer":
                showPomodoroTimer();
                break;
            case "mood":
                showMood();
                break;
            case "todos":
                showTodoList();
                break;
            case "calendar":
                showCalendar();
                break;
            case "notes":
                showNotes();
                break;
            case "pet":
                showPets();
                break;
            case "stats":
                System.out.println("Navigating to Analytics");
                break;
            case "whitenoise":
                showWhiteNoisePlayer();
                break;
            case "settings":
                showSettings();
                break;
            default:
                System.out.println("Navigating to: " + tab);
        }
    }

    private void showDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setSidebarController(sidebarController);
        root.setCenter(dashboard.getContent());
    }

    private void showNotes() {
        NotesController notesController = new NotesController(userId);
        notesController.setSidebar(sidebar);
        NotesView notesView = new NotesView(notesController);
        root.setCenter(notesView);
    }

    private void showMood() {
        MoodController moodController = new MoodController(userId);
        MoodView moodView = new MoodView(moodController);
        root.setCenter(moodView);
    }

    private void showPets() {
        PetsController petsController = new PetsController(userId);

        // Set up the listener to update sidebar when pet changes
        petsController.setPetChangeListener(() -> {
            // When pet changes, update the sidebar mascot
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
            sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
        });

        PetsView petsView = new PetsView(petsController);
        root.setCenter(petsView);

        // Also update sidebar with current pet when first loading pets tab
        PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
    }

    private void showTodoList() {
        TodoView todoView = new TodoView(userId);
        ScrollPane todoContent = todoView.getContent();

        // Make todo content fill available space
        todoContent.prefWidthProperty().bind(root.widthProperty().subtract(200)); // sidebar width
        todoContent.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(todoContent);
    }

    private void showCalendar() {
        CalendarView calendarView = new CalendarView(userId);

        // Set the initial width for responsive calculations
        calendarView.setWidth(root.getWidth() - 200); // Account for sidebar

        // Update calendar width when root container resizes
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            calendarView.setWidth(newVal.doubleValue() - 200); // Account for sidebar
        });

        // Set up callback - when calendar content changes, refresh the center
        calendarView.setOnContentChange(() -> {
            // This will be called when dates are clicked or month changes
            ScrollPane refreshedContent = calendarView.getContent();

            // Make refreshed content fill available space
            refreshedContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            refreshedContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(refreshedContent);

            // Update the width for the refreshed content
            calendarView.setWidth(root.getWidth() - 200);
        });

        ScrollPane calendarContent = calendarView.getContent();

        // Make calendar content fill available space
        calendarContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
        calendarContent.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(calendarContent);
    }

    private void showSettings() {
        Settings settings = new Settings();
        VBox settingsContent = settings.getContent();

        ScrollPane scrollPane = new ScrollPane(settingsContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + Pastel.BLUSH + "; -fx-border-color: " + Pastel.BLUSH + ";");

        // Make settings content responsive
        scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
        scrollPane.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(scrollPane);
    }

    private void showWhiteNoisePlayer() {
        // Use the singleton instance instead of creating a new one
        WhiteNoiseView whiteNoisePlayer = WhiteNoiseView.getInstance();
        Node whiteNoiseContent = whiteNoisePlayer.getContent(); // Changed to Node

        // Apply your dashboard theme to match the rest of the app
        whiteNoiseContent.setStyle("-fx-background-color: " + Pastel.BLUSH + ";");

        // Create a scroll pane for the content (since white noise player is tall)
        ScrollPane scrollPane = new ScrollPane(whiteNoiseContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + Pastel.BLUSH + "; -fx-border-color: " + Pastel.BLUSH + ";");
        scrollPane.setPadding(new Insets(20));

        // Make white noise content responsive
        scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
        scrollPane.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(scrollPane);
    }

    private void showPomodoroTimer() {
        // Create the PomodoroController and PomodoroView
        PomodoroController pomodoroController = new PomodoroController();
        PomodoroView pomodoroView = new PomodoroView(pomodoroController);
        VBox pomodoroContent = pomodoroView.getView();

        // Set up the controller with dependencies
        pomodoroController.setUserId(userId);
        pomodoroController.setSidebar(sidebar);

        // Create pets controller and set it on the pomodoro controller
        PetsController petsController = new PetsController(userId);
        pomodoroController.setPetsController(petsController);

        // The background is already set in PomodoroView, so remove this line:
        // pomodoroContent.setStyle("-fx-background-color: " + Pastel.BLUSH + ";");

        // Make pomodoro content responsive
        pomodoroContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
        pomodoroContent.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(pomodoroContent);
    }
}