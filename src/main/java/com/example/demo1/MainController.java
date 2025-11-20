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

    // 🔥 KEEP POMODORO CONTROLLER ALIVE ACROSS TAB SWITCHES 🔥
    private PomodoroController pomodoroController;
    private Pane pomodoroContent; // Also keep the UI pane

    // Pastel color palette
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
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();

        // Sidebar
        sidebarController = new SidebarController();
        sidebarController.setOnTabChange(this::handleNavigation);
        sidebar = new Sidebar(sidebarController, userName, userId);
        root.setLeft(sidebar);

        // Set initial pet in sidebar
        PetsController petsController = new PetsController(userId);
        PetsController.PetInfo initialPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(initialPet.getName(), initialPet.getSpecies(), initialPet.getGifFilename());

        showDashboard();

        Scene scene = new Scene(root, 1200, 800);
        scene.getRoot().setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");
        ThemeManager.applyTheme(scene, ThemeManager.Theme.PASTEL);

        stage.setScene(scene);
        stage.setTitle("Pastel Productivity Dashboard");
        stage.setMinWidth(1300);
        stage.setMinHeight(600);
        stage.setResizable(true);

        // IMPORTANT: Pause Pomodoro when app is closed
        stage.setOnCloseRequest(event -> {
            PomodoroController controller = PomodoroController.getInstance();
            if (controller != null) {
                controller.forcePauseOnExit();
            }
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
        petsController.setPetChangeListener(() -> {
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
            sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
        });
        PetsView petsView = new PetsView(petsController);
        root.setCenter(petsView);

        PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
    }

    private void showTodoList() {
        try {
            TodoView todoView = new TodoView();
            ScrollPane todoContent = todoView.getContent();
            todoView.setUsername(userName);
            todoContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            todoContent.prefHeightProperty().bind(root.heightProperty());
            root.setCenter(todoContent);
        } catch (Exception e) {
            System.out.println("❌ Error loading Todo List: " + e.getMessage());
            e.printStackTrace();
            showFallbackContent("To-Do List 📝", "Error loading to-do list.");
        }
    }

    private void showCalendar() {
        try {
            CalendarView calendarView = new CalendarView();
            calendarView.setWidth(root.getWidth() - 200);

            root.widthProperty().addListener((obs, oldVal, newVal) -> {
                calendarView.setWidth(newVal.doubleValue() - 200);
            });

            calendarView.setOnContentChange(() -> {
                ScrollPane refreshedContent = calendarView.getContent();
                refreshedContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
                refreshedContent.prefHeightProperty().bind(root.heightProperty());
                root.setCenter(refreshedContent);
                calendarView.setWidth(root.getWidth() - 200);
            });

            ScrollPane calendarContent = calendarView.getContent();
            calendarContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            calendarContent.prefHeightProperty().bind(root.heightProperty());
            root.setCenter(calendarContent);
        } catch (Exception e) {
            System.out.println("❌ Error loading Calendar: " + e.getMessage());
            e.printStackTrace();
            showFallbackContent("Calendar 📅", "Error loading calendar.");
        }
    }

    private void showSettings() {
        try {
            Settings settings = new Settings();
            VBox settingsContent = settings.getContent();
            ScrollPane scrollPane = new ScrollPane(settingsContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());
            root.setCenter(scrollPane);
        } catch (Exception e) {
            System.out.println("❌ Error loading Settings: " + e.getMessage());
            e.printStackTrace();
            showFallbackContent("Settings ⚙️", "Error loading settings page.");
        }
    }

    private void showWhiteNoisePlayer() {
        try {
            WhiteNoiseView whiteNoisePlayer = new WhiteNoiseView();
            VBox whiteNoiseContent = whiteNoisePlayer.getContent();
            whiteNoiseContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");

            ScrollPane scrollPane = new ScrollPane(whiteNoiseContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + PASTEL_BLUSH + "; -fx-border-color: " + PASTEL_BLUSH + ";");
            scrollPane.setPadding(new Insets(20));
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());
            root.setCenter(scrollPane);
        } catch (Exception e) {
            System.out.println("❌ Error loading White Noise Player: " + e.getMessage());
            e.printStackTrace();
            showFallbackContent("White Noise Player 🎵", "Error loading white noise player.");
        }
    }

    private void showPomodoroTimer() {
        try {
            // 🔥 ONLY CREATE POMODORO CONTROLLER ONCE 🔥
            if (pomodoroController == null) {
                System.out.println("✨ Creating NEW Pomodoro Timer instance");

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo1/Pomodoro/Pomodoro.fxml"));
                pomodoroContent = fxmlLoader.load();

                // Initialize the controller ONCE
                pomodoroController = fxmlLoader.getController();
                pomodoroController.setUserId(userId);
                pomodoroController.setSidebar(sidebar);

                PetsController petsController = new PetsController(userId);
                pomodoroController.setPetsController(petsController);

                pomodoroContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");
                pomodoroContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
                pomodoroContent.prefHeightProperty().bind(root.heightProperty());

                System.out.println("✅ Pomodoro Timer initialized - will run in background");
            } else {
                System.out.println("♻️ Reusing existing Pomodoro Timer (timer continues in background)");
            }

            // Just show the existing content - timer keeps running!
            root.setCenter(pomodoroContent);

        } catch (Exception e) {
            System.out.println("❌ Error loading Pomodoro FXML: " + e.getMessage());
            e.printStackTrace();
            showFallbackContent("Pomodoro Timer 🍅", "Error loading timer. Please check the FXML file.");
        }
    }

    private void showFallbackContent(String title, String message) {
        VBox fallbackContent = new VBox(20);
        fallbackContent.setPadding(new Insets(40));
        fallbackContent.setAlignment(Pos.CENTER);
        fallbackContent.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");
        fallbackContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
        fallbackContent.prefHeightProperty().bind(root.heightProperty());

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(message);
        subtitleLabel.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px;");

        fallbackContent.getChildren().addAll(titleLabel, subtitleLabel);
        root.setCenter(fallbackContent);
    }
}