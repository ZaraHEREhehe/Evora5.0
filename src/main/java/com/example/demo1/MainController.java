// MainController.java
package com.example.demo1;

import com.example.demo1.Analytics.AnalyticsView;
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
import com.example.demo1.Theme.*;
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

public class MainController implements ThemeManager.ThemeChangeListener {
    private Stage stage;
    private BorderPane root;
    private Sidebar sidebar;
    private SidebarController sidebarController;
    private String userName;
    private int userId;
    private ThemeManager themeManager;
    private String currentActiveView = "dashboard";

    // KEEP POMODORO CONTROLLER ALIVE ACROSS TAB SWITCHES
    private PomodoroController pomodoroController;
    private Pane pomodoroContent; // Also keep the UI pane

    public MainController(Stage stage, String userName, int userId) {
        this.stage = stage;
        this.userName = userName;
        this.userId = userId;
        this.themeManager = ThemeManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();

        // Register as theme change listener
        themeManager.addThemeChangeListener(this);

        // Sidebar
        sidebarController = new SidebarController();
        sidebarController.setOnTabChange(this::handleNavigation);
        sidebar = new Sidebar(sidebarController, userName, userId);
        root.setLeft(sidebar);

        // Set initial pet in sidebar
        PetsController petsController = new PetsController(userId);
        PetsController.PetInfo initialPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(initialPet.getName(), initialPet.getSpecies(), initialPet.getGifFilename());

        //refresh experience immediately after mascot setup
        refreshSidebarExperience();

        showDashboard();

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenHeight = screenBounds.getHeight();
        double screenWidth = screenBounds.getWidth();

        // Create scene with full screen dimensions
        Scene scene = new Scene(root, screenWidth, screenHeight);
        applyThemeToScene(scene, themeManager.getCurrentTheme());

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


        // IMPORTANT: Pause Pomodoro when app is closed
        stage.setOnCloseRequest(event -> {
            PomodoroController controller = PomodoroController.getInstance();
            if (controller != null) {
                controller.forcePauseOnExit();
            }
        });

        stage.show();
    }

    //refresh sidebar experience
    public void refreshSidebarExperience() {
        sidebar.refreshExperienceFromDatabase(userId);
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        // Update the scene background
        applyThemeToScene(root.getScene(), newTheme);

        // Refresh the current active view
        refreshCurrentView();

        // Update sidebar theme
        if (sidebar != null) {
            sidebar.updateTheme(newTheme);
        }
    }

    private void applyThemeToScene(Scene scene, Theme theme) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-background-color: " + theme.getBackgroundColor() + ";");
        }
    }

    private void refreshCurrentView() {
        // Refresh the currently active view with the new theme
        switch (currentActiveView) {
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
                System.out.println("Refreshing Analytics with new theme");
                break;
            case "whitenoise":
                showWhiteNoisePlayer();
                break;
            case "settings":
                showSettings();
                break;
        }
    }

    private void handleNavigation(String tab) {
        this.currentActiveView = tab;
        refreshSidebarExperience(); //refresh whenever user navigates

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
                showAnalytics();
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
        dashboard.setSidebarController(sidebarController);

        // Apply current theme to dashboard
        Theme currentTheme = themeManager.getCurrentTheme();
        dashboard.getContent();

        root.setCenter(dashboard.getContent());
    }

    private void showNotes() {
        NotesController notesController = new NotesController(userId);
        notesController.setSidebar(sidebar);
        NotesView notesView = new NotesView(notesController);

        // Apply theme to notes view if it supports it
        applyThemeToNode(notesView, themeManager.getCurrentTheme());

        root.setCenter(notesView);
    }

    private void showMood() {
        MoodController moodController = new MoodController(userId);
        MoodView moodView = new MoodView(moodController);

        // Apply theme to mood view if it supports it
        applyThemeToNode(moodView, themeManager.getCurrentTheme());

        root.setCenter(moodView);
    }

    private void showPets() {
        PetsController petsController = new PetsController(userId);
        petsController.setPetChangeListener(() -> {
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
            sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
        });
        PetsView petsView = new PetsView(petsController);

        // Apply theme to pets view if it supports it
        applyThemeToNode(petsView, themeManager.getCurrentTheme());

        root.setCenter(petsView);

        PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
        sidebar.updateMascot(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
    }

    private void showTodoList() {
        TodoView todoView = new TodoView(userId);
        ScrollPane todoContent = todoView.getContent();

        todoView.setUsername(userName);
        todoView.setSidebar(sidebar);

        // Apply theme to todo content
        applyThemeToNode(todoContent, themeManager.getCurrentTheme());

        // Make todo content fill available space
        todoContent.prefWidthProperty().bind(root.widthProperty().subtract(200)); // sidebar width
        todoContent.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(todoContent);
    }

    private void showCalendar() {
        CalendarView calendarView = new CalendarView(userId);

        // Apply theme to calendar view
        applyThemeToNode(calendarView.getContent(), themeManager.getCurrentTheme());

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

            // Apply theme to refreshed content
            applyThemeToNode(refreshedContent, themeManager.getCurrentTheme());

            // Make refreshed content fill available space
            refreshedContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            refreshedContent.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(refreshedContent);

            // Update the width for the refreshed content
            calendarView.setWidth(root.getWidth() - 200);
        });

        ScrollPane calendarContent = calendarView.getContent();

        // Apply theme to calendar content
        applyThemeToNode(calendarContent, themeManager.getCurrentTheme());

        // Make calendar content fill available space
        calendarContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
        calendarContent.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(calendarContent);
    }

    private void showSettings() {
        Settings settings = new Settings(userId);
        VBox settingsContent = settings.getContent();

        ScrollPane scrollPane = new ScrollPane(settingsContent);
        scrollPane.setFitToWidth(true);

        // Apply theme to scroll pane
        Theme currentTheme = themeManager.getCurrentTheme();
        scrollPane.setStyle("-fx-background: " + currentTheme.getBackgroundColor() + "; -fx-border-color: " + currentTheme.getBackgroundColor() + ";");

        // Make settings content responsive
        scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
        scrollPane.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(scrollPane);
    }

    private void showWhiteNoisePlayer() {
        // Use the singleton instance instead of creating a new one
        WhiteNoiseView whiteNoisePlayer = WhiteNoiseView.getInstance();
        Node whiteNoiseContent = whiteNoisePlayer.getContent();

        // Apply current theme
        Theme currentTheme = themeManager.getCurrentTheme();
        whiteNoiseContent.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

        // Create a scroll pane for the content (since white noise player is tall)
        ScrollPane scrollPane = new ScrollPane(whiteNoiseContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + currentTheme.getBackgroundColor() + "; -fx-border-color: " + currentTheme.getBackgroundColor() + ";");
        scrollPane.setPadding(new Insets(20));

        // Make white noise content responsive
        scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
        scrollPane.prefHeightProperty().bind(root.heightProperty());

        root.setCenter(scrollPane);
    }

    private void showPomodoroTimer() {
        try {
        // Create the PomodoroController and PomodoroView
        if (pomodoroController == null)
        {
            pomodoroController = new PomodoroController();
            PomodoroView pomodoroView = new PomodoroView(pomodoroController);
            VBox pomodoroContent = pomodoroView.getView();

            // Apply current theme
            Theme currentTheme = themeManager.getCurrentTheme();
            pomodoroContent.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

            // Set up the controller with dependencies
            pomodoroController.setUserId(userId);
            pomodoroController.setSidebar(sidebar);

            // Create pets controller and set it on the pomodoro controller
            PetsController petsController = new PetsController(userId);
            pomodoroController.setPetsController(petsController);

            // Make pomodoro content responsive
            pomodoroContent.prefWidthProperty().bind(root.widthProperty().subtract(200));
            pomodoroContent.prefHeightProperty().bind(root.heightProperty());
          }
            root.setCenter(pomodoroContent);
        }
        catch (Exception e){
            System.out.println("♻️ Reusing existing Pomodoro Timer (timer continues in background)");
            showFallbackContent("Pomodoro Timer 🍅", "Error loading timer. Please check the FXML file.");
        }
    }

    private void applyThemeToNode(Node node, Theme theme) {
        if (node instanceof Region) {
            Region region = (Region) node;
            // Check if the node has a style that includes background color
            String currentStyle = region.getStyle();
            if (currentStyle != null && currentStyle.contains("-fx-background-color")) {
                // Replace the background color with the new theme
                region.setStyle(currentStyle.replaceAll(
                        "-fx-background-color:\\s*[^;]+;",
                        "-fx-background-color: " + theme.getBackgroundColor() + ";"
                ));
            } else {
                // Add background color if not present
                region.setStyle("-fx-background-color: " + theme.getBackgroundColor() + ";" + currentStyle);
            }
        }
    }

    // Clean up when controller is no longer needed
    public void cleanup() {
        if (themeManager != null) {
            themeManager.removeThemeChangeListener(this);
        }
    }
    private void showAnalytics() {
        try {
            AnalyticsView analyticsView = new AnalyticsView(userId, userName);
            Node analyticsContent = analyticsView.create();

            // Create a scroll pane for the content
            ScrollPane scrollPane = new ScrollPane(analyticsContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: " + Pastel.BLUSH + "; -fx-border-color: " + Pastel.BLUSH + ";");
            scrollPane.setPadding(new Insets(20));

            // Make analytics content responsive
            scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(200));
            scrollPane.prefHeightProperty().bind(root.heightProperty());

            root.setCenter(scrollPane);

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

            Label timerTitle = new Label("Pomodoro Timer 🍅");
            timerTitle.setStyle("-fx-text-fill: " + PASTEL_FOREST + "; -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            Label timerSubtitle = new Label("Error loading timer. Please check the FXML file.");
            timerSubtitle.setStyle("-fx-text-fill: " + PASTEL_SAGE + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

            fallbackContent.getChildren().addAll(timerTitle, timerSubtitle);
            root.setCenter(fallbackContent);
    }
}