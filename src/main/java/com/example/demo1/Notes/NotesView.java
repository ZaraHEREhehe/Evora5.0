package com.example.demo1.Notes;

import com.example.demo1.Theme.PastelTheme;
import com.example.demo1.Theme.ThemeManager;
import com.example.demo1.Theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class NotesView extends BorderPane {

    private final NotesController controller;
    private Pane boardPane;
    private final List<StickyNote> notes = new ArrayList<>();
    private boolean showAddForm = false;
    private VBox mainContent;
    private ThemeManager themeManager;

    public NotesView(NotesController controller) {
        this.controller = controller;
        this.themeManager = ThemeManager.getInstance();

        boardPane = new Pane();
        boardPane.setPrefSize(650, 1000);
        boardPane.setMinSize(650, 1000);

        // Board background remains consistent across themes
        boardPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef3c7, #fed7aa); " +
                "-fx-border-color: #92400e; -fx-border-width: 4; " +
                "-fx-background-radius: 30; -fx-border-radius: 30;");
        // Add listener for board size changes
        boardPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                addCornerPins(); // Reposition pins when board size changes
            }
        });

        boardPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                addCornerPins(); // Reposition pins when board size changes
            }
        });

        createView();
        loadExistingNotes(); //from db
    }

    private void loadExistingNotes()
    {
        List<NotesController.Note> dbNotes = controller.getNotes();
        for (NotesController.Note dbNote : dbNotes) {
            // Convert color_id (1-6) to colorIndex (0-5)
            int colorIndex = dbNote.getColorId() - 1;
            StickyNote note = new StickyNote(controller, dbNote.getNoteId(), dbNote.getContent(), colorIndex);
            note.setLayoutX(dbNote.getPositionX());
            note.setLayoutY(dbNote.getPositionY());
            boardPane.getChildren().add(note);
            notes.add(note);
        }
    }

    private void createView() {
        Theme currentTheme = themeManager.getCurrentTheme();

        // Main container with proper spacing - USE THEME BACKGROUND
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

        // Title section - USE THEME TEXT COLORS
        Label title = new Label("Sticky Notes");
        title.setFont(Font.font("Poppins", 32));
        title.setStyle("-fx-text-fill: " + currentTheme.getTextPrimary() + ";");

        Label subtitle = new Label("Jot down your thoughts and ideas on your digital corkboard!");
        subtitle.setFont(Font.font("Poppins", 14));
        subtitle.setStyle("-fx-text-fill: " + currentTheme.getTextSecondary() + ";");

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Add Note Button - USE THEME COLORS (same as old version)
        Button addBtn = new Button("+ Add New Note");
        addBtn.setFont(Font.font("Poppins", 14));
        addBtn.setStyle("-fx-background-color: linear-gradient(to right, #FACEEA, #D7D8FF); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 10 20;");
        addBtn.setOnAction(e -> toggleAddForm());

        // Create scrollable container for the board
        ScrollPane scrollPane = new ScrollPane(boardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setPrefViewportWidth(670);

        // Start with just the basic layout
        mainContent.getChildren().addAll(headerBox, addBtn, scrollPane);
        this.setCenter(mainContent);

        // Add realistic pins to all four corners
        addCornerPins();
    }

    private void addCornerPins() {
        // Wait for the boardPane to be laid out to get its actual size
        Platform.runLater(() -> {
            double boardWidth = boardPane.getWidth();
            double boardHeight = boardPane.getHeight();

            // Use percentages of the actual board size instead of hardcoded positions
            double[][] cornerPositions = {
                    {20, 20},                                    // Top-left (fixed offset)
                    {boardWidth - 30, 20},                       // Top-right
                    {20, boardHeight - 30},                      // Bottom-left
                    {boardWidth - 30, boardHeight - 30}          // Bottom-right
            };

            // Clear any existing pins first
            boardPane.getChildren().removeIf(node -> node instanceof RealisticPin);

            for (double[] position : cornerPositions) {
                RealisticPin pin = new RealisticPin(position[0], position[1]);
                boardPane.getChildren().add(pin);
            }
        });
    }

    private void toggleAddForm() {
        if (showAddForm) {
            // Hide form - remove it from mainContent
            mainContent.getChildren().removeIf(node ->
                    node instanceof VBox && ((VBox) node).getStyle().contains("-fx-background-color: rgba(255,255,255,0.95)")
            );
            showAddForm = false;
        } else {
            // Show form - insert it after the button
            showAddForm();
        }
    }

    private void showAddForm() {
        showAddForm = true;

        // Create add note form - KEEP THE SAME STYLE AS ORIGINAL
        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 25; -fx-border-color: #FACEEA; -fx-border-width: 2; " +
                "-fx-border-radius: 25; -fx-background-insets: 0; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");

        form.setMaxWidth(380);
        form.setAlignment(Pos.TOP_CENTER);

        Label formTitle = new Label("New Note");
        formTitle.setFont(Font.font("Poppins", 20));
        formTitle.setTextFill(Color.web("#5c5470"));

        TextArea noteContent = new TextArea();
        noteContent.setPromptText("Write your note here...");
        noteContent.setPrefRowCount(4);

        noteContent.setStyle("""
            -fx-background-color: transparent;
            -fx-background-insets: 0;
            -fx-background-radius: 15;
        
            -fx-control-inner-background: white;
            -fx-control-inner-background-radius: 15;
            -fx-control-inner-background-insets: 0;
        
            /* Remove ScrollPane gray line */
            -fx-box-border: transparent;
            -fx-border-color: #E2D6FF;
            -fx-border-radius: 15;
            -fx-border-width: 1;
            -fx-padding: 10;
        
            /* Remove blue focus glow */
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
            
            /* Remove viewport borders/shadows */
            -fx-shadow-highlight-color: transparent;
            -fx-inner-border: transparent;
            -fx-body-color: transparent;
            -fx-inner-border-horizontal: transparent;
        
            -fx-font-family: 'Poppins';
            -fx-font-size: 14;
        """);

        // Color picker with live preview - more compact
        VBox colorSection = new VBox(8);
        Label colorLabel = new Label("Choose color:");
        colorLabel.setFont(Font.font("Poppins", 12));
        colorLabel.setTextFill(Color.web("#756f86"));

        HBox colorButtons = new HBox(6);
        colorButtons.setAlignment(Pos.CENTER);

        String[] colors = new String[6];
        colors[0] = "#fef08a";
        colors[1] = "#fecaca";
        colors[2] = "#bbf7d0";
        colors[3] = "#bfdbfe";
        colors[4] = "#e9d5ff";
        colors[5] = "#fed7aa";

        List<ToggleButton> colorBtns = new ArrayList<>();
        ToggleGroup colorGroup = new ToggleGroup();

        // Preview pane to show selected color
        Pane colorPreview = new Pane();
        colorPreview.setPrefSize(32, 32);
        colorPreview.setStyle("-fx-background-color: " + colors[0] + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8;");

        HBox colorPickerWithPreview = new HBox(12);
        colorPickerWithPreview.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < colors.length; i++) {
            ToggleButton colorBtn = new ToggleButton();
            // COMPLETELY remove square borders from toggle buttons
            colorBtn.setStyle(
                    "-fx-background-color: " + colors[i] + "; " +
                            "-fx-background-radius: 12; " + // Use 12px for the circle
                            "-fx-min-width: 24; " +
                            "-fx-min-height: 24; " +
                            "-fx-max-width: 24; " +
                            "-fx-max-height: 24; " +
                            "-fx-border-color: transparent; " +
                            "-fx-focus-color: transparent; " +
                            "-fx-faint-focus-color: transparent; " +
                            "-fx-background-insets: 0; " +
                            "-fx-padding: 0; " +
                            // Remove any selection indicators
                            "-fx-border-width: 0;"
            );

            colorBtn.setToggleGroup(colorGroup);
            colorBtn.setUserData(i);

            colorBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    int selectedIndex = (int) colorBtn.getUserData();
                    colorPreview.setStyle("-fx-background-color: " + colors[selectedIndex] + "; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: #d1d5db; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8;");
                }
            });

            colorBtns.add(colorBtn);
        }
        if (!colorBtns.isEmpty()) {
            colorBtns.get(0).setSelected(true);
        }
        colorButtons.getChildren().addAll(colorBtns);

        colorPickerWithPreview.getChildren().addAll(colorPreview, colorButtons);
        colorSection.getChildren().addAll(colorLabel, colorPickerWithPreview);

        // Form buttons - smaller and cuter
        HBox formButtons = new HBox(8);
        formButtons.setAlignment(Pos.CENTER);

        Button submitBtn = new Button("Add Note");
        // COMPLETELY remove square borders from buttons
        submitBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #FACEEA, #D7D8FF); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 18; " +
                        "-fx-border-radius: 18; " +
                        "-fx-padding: 8 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-background-insets: 0;"
        );

        Button cancelBtn = new Button("Cancel");
        // COMPLETELY remove square borders from buttons
        cancelBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #5c5470; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-radius: 18; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 8 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent; " +
                        "-fx-background-insets: 0;"
        );

        formButtons.getChildren().addAll(submitBtn, cancelBtn);
        form.getChildren().addAll(formTitle, noteContent, colorSection, formButtons);

        // Insert the form after the button in mainContent (index 2)
        mainContent.getChildren().add(2, form);

        // Button actions
        submitBtn.setOnAction(e -> {
            if (colorGroup.getSelectedToggle() != null && !noteContent.getText().trim().isEmpty()) {
                int selectedColor = (int) colorGroup.getSelectedToggle().getUserData();
                addNewNote(noteContent.getText().trim(), selectedColor);
                toggleAddForm(); // Hide the form
            }
        });

        cancelBtn.setOnAction(e -> {
            toggleAddForm(); // Hide the form
        });
    }

    private void addNewNote(String content, int colorIndex) {
        // color_ids start at 1
        int colorId = colorIndex + 1;
        double x = Math.random() * 400 + 50;
        double y = Math.random() * 300 + 50;

        int noteId = controller.addNote(content, colorId, x, y);

        if (noteId != -1) {
            StickyNote note = new StickyNote(controller, noteId, content, colorIndex);
            note.setLayoutX(x);
            note.setLayoutY(y);
            boardPane.getChildren().add(note);
            notes.add(note);
        }
    }

    /** Inner class for realistic pin effect */
    private class RealisticPin extends StackPane {
        public RealisticPin(double x, double y) {
            this.setLayoutX(x);
            this.setLayoutY(y);

            // Create a realistic pushpin with metallic gradient and shadow
            Circle pinHead = new Circle(0, 0, 6);
            pinHead.setFill(Color.web("#8c8c8c")); // Metallic gray base

            // Add metallic gradient effect
            DropShadow metallicEffect = new DropShadow();
            metallicEffect.setColor(Color.web("#666666"));
            metallicEffect.setRadius(3);
            metallicEffect.setOffsetX(1);
            metallicEffect.setOffsetY(1);
            pinHead.setEffect(metallicEffect);

            // Pin point (the sharp part)
            Circle pinPoint = new Circle(0, 10, 1, Color.web("#666666"));

            // Pin shaft (thin metal rod)
            Circle pinShaft = new Circle(0, 5, 0.8, Color.web("#999999"));

            // Highlight for metallic effect
            Circle highlight = new Circle(-2, -2, 2, Color.web("#ffffff", 0.3));

            this.getChildren().addAll(pinHead, pinShaft, pinPoint, highlight);
        }
    }

    /** Inner class for draggable sticky notes */
    private class StickyNote extends StackPane {
        private double mouseX, mouseY;
        private final TextArea textArea;
        private int colorIndex;
        private int noteId;
        private final NotesController controller;

        private static final String[] COLORS = new String[6];
        static {
            COLORS[0] = "#fef08a";
            COLORS[1] = "#fecaca";
            COLORS[2] = "#bbf7d0";
            COLORS[3] = "#bfdbfe";
            COLORS[4] = "#e9d5ff";
            COLORS[5] = "#fed7aa";
        }

        public StickyNote(NotesController controller, int noteId, String content, int colorIndex) {
            this.controller = controller;
            this.noteId = noteId;
            this.colorIndex = colorIndex;

            this.setPrefSize(220, 180);
            this.setBackground(new Background(new BackgroundFill(
                    Color.web(COLORS[colorIndex]), new CornerRadii(15), Insets.EMPTY)));
            this.setEffect(new DropShadow(8, Color.gray(0, 0.3)));
            this.setStyle("-fx-border-color: rgba(0,0,0,0.1); -fx-border-width: 1; -fx-border-radius: 15;");

            // Add realistic rotation
            this.setRotate(Math.random() * 6 - 3);

            // Text area
            textArea = new TextArea(content);
            textArea.setWrapText(true);
            textArea.setFont(Font.font("Comic Sans MS", 14));
            textArea.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-text-fill: #374151; " + // Keep consistent text color for notes
                    "-fx-font-weight: normal; " +
                    "-fx-opacity: 1.0; " +
                    "-fx-focus-color: transparent; " +
                    "-fx-faint-focus-color: transparent;");
            textArea.setPrefSize(200, 150);
            textArea.setPadding(new Insets(15, 10, 10, 10));
            textArea.setFocusTraversable(false);
            // Save text changes to database when user stops typing
            textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                // Use a timer to avoid saving on every keystroke
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(3) //save after every 3 s to avoid many updates
                );
                pause.setOnFinished(event -> {
                    controller.updateNote(noteId, newValue, colorIndex + 1, getLayoutX(), getLayoutY());
                });
                pause.playFromStart();
            });


            // Delete button
            Button deleteBtn = new Button("✕");
            deleteBtn.setFont(Font.font("Arial", 12));
            deleteBtn.setTextFill(Color.web("#dc2626"));
            deleteBtn.setStyle("-fx-background-color: rgba(270, 222, 202, 0.9); " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 3; " +
                    "-fx-border-color: transparent; " +
                    "-fx-focus-color: transparent; " +
                    "-fx-faint-focus-color: transparent;");
            deleteBtn.setOpacity(0);
            deleteBtn.setPrefSize(20, 20);
            deleteBtn.setOnAction(e -> {
                Pane parent = (Pane) this.getParent();
                if (parent != null) {
                    parent.getChildren().remove(this);
                    notes.remove(this);
                }
                controller.deleteNote(noteId);
            });

            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(6, 6, 0, 0));

            // Color picker
            HBox colorPicker = new HBox(3);
            colorPicker.setAlignment(Pos.CENTER);
            colorPicker.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                    "-fx-background-radius: 10; -fx-padding: 4; " +
                    "-fx-border-color: #d1d5db; -fx-border-width: 1;");
            colorPicker.setOpacity(0);

            for (int i = 0; i < COLORS.length; i++) {
                Button colorBtn = new Button();
                colorBtn.setStyle("-fx-background-color: " + COLORS[i] + "; " +
                        "-fx-background-radius: 50%; -fx-min-width: 14; -fx-min-height: 14; " +
                        "-fx-border-color: transparent; -fx-padding: 0; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent;");

                final int index = i;
                colorBtn.setOnAction(e -> {
                    this.colorIndex = index;
                    this.setBackground(new Background(new BackgroundFill(
                            Color.web(COLORS[index]), new CornerRadii(15), Insets.EMPTY)));
                    colorPicker.setOpacity(0);

                    // Save color change to database
                    int newColorId = index + 1; // Convert to database color_id
                    controller.updateNote(noteId, textArea.getText(), newColorId,
                            getLayoutX(), getLayoutY());
                });
                colorPicker.getChildren().add(colorBtn);
            }

            StackPane.setAlignment(colorPicker, Pos.TOP_CENTER);
            StackPane.setMargin(colorPicker, new Insets(-35, 0, 0, 0));

            VBox noteLayout = new VBox();
            noteLayout.setAlignment(Pos.TOP_CENTER);
            noteLayout.setSpacing(2);
            noteLayout.getChildren().addAll(colorPicker, textArea);

            this.getChildren().addAll(noteLayout, deleteBtn);

            // Hover effects
            this.setOnMouseEntered(e -> {
                this.setScaleX(1.01);
                this.setScaleY(1.01);
                deleteBtn.setOpacity(1);
                colorPicker.setOpacity(1);
            });

            this.setOnMouseExited(e -> {
                this.setScaleX(1.0);
                this.setScaleY(1.0);
                deleteBtn.setOpacity(0);
                if (!colorPicker.isHover()) {
                    colorPicker.setOpacity(0);
                }
            });

            colorPicker.setOnMouseEntered(e -> colorPicker.setOpacity(1));
            colorPicker.setOnMouseExited(e -> colorPicker.setOpacity(0));

            // Enable dragging with boundary constraints
            setOnMousePressed(this::handleMousePressed);
            setOnMouseDragged(this::handleMouseDragged);
            setOnMouseClicked(e -> this.toFront());

            textArea.setOnMouseClicked(e -> {
                textArea.requestFocus();
                e.consume();
            });
        }

        private void handleMousePressed(MouseEvent event) {
            if (!(event.getTarget() instanceof TextArea)) {
                mouseX = event.getSceneX() - getLayoutX();
                mouseY = event.getSceneY() - getLayoutY();
                this.toFront();
            }
        }

        private void handleMouseDragged(MouseEvent event) {
            if (!(event.getTarget() instanceof TextArea)) {
                double newX = event.getSceneX() - mouseX;
                double newY = event.getSceneY() - mouseY;

                double minX = 0;
                double minY = 0;
                double maxX = boardPane.getWidth() - this.getWidth();
                double maxY = boardPane.getHeight() - this.getHeight();

                newX = Math.max(minX, Math.min(newX, maxX));
                newY = Math.max(minY, Math.min(newY, maxY));

                setLayoutX(newX);
                setLayoutY(newY);

                controller.updateNotePosition(noteId, newX, newY);
            }
        }
    }
}