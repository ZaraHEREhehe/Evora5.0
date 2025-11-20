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
import com.example.demo1.Theme.ThemeManager;
import com.example.demo1.Pomodoro.PomodoroController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainController {
    private Stage stage;
    private BorderPane root;
    private Sidebar sidebar;
    private SidebarController sidebarController;
    private String userName;
    private int userId;

    // Pastel color palette - single colors (copied exactly from Dashboard)
    private final String PASTEL_PINK = "#FACEEA";
    private final String PASTEL_BLUE = "#C1DAFF";
    private final String PASTEL_LAVENDER = "#D7D8FF";
    private final String PASTEL_PURPLE = "#F0D2F7";
    private final String PASTEL_LILAC = "#E2D6FF";
    private final String PASTEL_ROSE = "#F3D1F3";
    private final String PASTEL_BLUSH = "#FCEDF5";
    private final String PASTEL_SKY = "#E4EFFF";
    private final String PASTEL_MIST = "#F7EFFF";
    private final String PASTEL_IVORY = "#FDF5E7";
    private final String PASTEL_DUSTY_PINK = "#F1DBD0";
    private final String PASTEL_MAUVE = "#D3A29D";
    private final String PASTEL_SAGE = "#8D9383";
    private final String PASTEL_FOREST = "#343A26";

    public MainController(Stage stage, String userName, int userId) {
        this.stage = stage;
        this.userName = userName;
        this.userId = userId;
        initializeUI(); // This should create the scene and set it on the stage
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

        // Dynamic layout with minimum size (copied exactly from Dashboard)
        Scene scene = new Scene(root, 1200, 800); // Default size
        scene.getRoot().setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

        // Apply theme
        ThemeManager.applyTheme(scene, ThemeManager.Theme.PASTEL);

        stage.setScene(scene);
        stage.setTitle("Pastel Productivity Dashboard");

        // Set minimum size and allow resizing
        stage.setMinWidth(1300);
        stage.setMinHeight(600);
        stage.setResizable(true); // Allow users to resize the window

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
        dashboard.setSidebarController(sidebarController); // ADD THIS LINE
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
        try {
            TodoView todoView = new TodoView();
            ScrollPane todoContent = todoView.getContent();

            // Make todo content fill available space
            todoContent.prefWidthProperty().bind(root.widthProperty().subtract(200)); // sidebar width
            todoContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(todoContent);

        } catch (Exception e) {
            System.out.println("❌ Error loading Todo List: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("To-Do List 📝");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading to-do list.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showCalendar() {
        try {
            CalendarView calendarView = new CalendarView();

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

        } catch (Exception e) {
            System.out.println("❌ Error loading Calendar: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("Calendar 📅");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading calendar.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showSettings() {
        try {
            Settings settings = new Settings();
            VBox settingsContent = settings.getContent();

            ScrollPane scrollPane = new ScrollPane(settingsContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");

            // Make settings content responsive
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(scrollPane);

        } catch (Exception e) {
            System.out.println("❌ Error loading Settings: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("Settings ⚙️");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

            Label subtitle = new Label("Error loading settings page.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showWhiteNoisePlayer() {
        try {
            // Use the singleton instance instead of creating a new one
            WhiteNoiseView whiteNoisePlayer = WhiteNoiseView.getInstance();
            Node whiteNoiseContent = whiteNoisePlayer.getContent(); // Changed to Node

            // Apply your dashboard theme to match the rest of the app
            whiteNoiseContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Create a scroll pane for the content (since white noise player is tall)
            ScrollPane scrollPane = new ScrollPane(whiteNoiseContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");
            scrollPane.setPadding(new Insets(20));

            // Make white noise content responsive
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(scrollPane);

        } catch (Exception e) {
            System.out.println("❌ Error loading White Noise Player: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label title = new Label("White Noise Player 🎵");
            title.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Label subtitle = new Label("Error loading white noise player. Please check the console for details.");
            subtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

            fallbackContent.getChildren().addAll(title, subtitle);
            root.setCenter(fallbackContent);
        }
    }

    private void showPomodoroTimer() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo1/Pomodoro/Pomodoro.fxml"));
            Pane pomodoroContent = fxmlLoader.load();

            // Get the controller instance from the FXML loader
            // added these after setting the db up
            PomodoroController pomodoroController = fxmlLoader.getController();
            pomodoroController.setUserId(userId);
            pomodoroController.setSidebar(sidebar);
            // Create pets controller and set it on the pomodoro controller
            PetsController petsController = new PetsController(userId);
            pomodoroController.setPetsController(petsController);

            // Apply the pastel theme to the loaded content
            pomodoroContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make pomodoro content responsive
            pomodoroContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            pomodoroContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(pomodoroContent);
        } catch (Exception e) {
            System.out.println("❌ Error loading Pomodoro FXML: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox fallbackContent = new VBox(20);
            fallbackContent.setPadding(new Insets(40));
            fallbackContent.setAlignment(Pos.CENTER);
            fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            // Make fallback content responsive
            fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            fallbackContent.prefHeightProperty().bind(root.heightProperty());

            Label timerTitle = new Label("Pomodoro Timer 🍅");
            timerTitle.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Label timerSubtitle = new Label("Error loading timer. Please check the FXML file.");
            timerSubtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

            fallbackContent.getChildren().addAll(timerTitle, timerSubtitle);
            root.setCenter(fallbackContent);
        }
    }
}