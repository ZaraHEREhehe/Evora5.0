package com.example.demo1.Notes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class NotesView extends BorderPane {

    private final NotesController controller;
    private Pane boardPane;
    private final List<StickyNote> notes = new ArrayList<>();
    private boolean showAddForm = false;

    public NotesView(NotesController controller) {
        this.controller = controller;

        // Initialize boardPane first
        boardPane = new Pane();
        boardPane.setPrefSize(800, 1000);
        boardPane.setMinSize(800, 1000);
        boardPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef3c7, #fed7aa); " +
                "-fx-border-color: #92400e; -fx-border-width: 4; " +
                "-fx-background-radius: 30; -fx-border-radius: 30;");

        createView();
    }

    private void createView() {
        // Main container with proper spacing
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: #fdf7ff;");

        // Title section
        Label title = new Label("Sticky Notes");
        title.setFont(Font.font("Poppins", 32));
        title.setTextFill(Color.web("#5c5470"));

        Label subtitle = new Label("Jot down your thoughts and ideas on your digital corkboard!");
        subtitle.setFont(Font.font("Poppins", 14));
        subtitle.setTextFill(Color.web("#2E2E2E"));

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Add Note Button
        Button addBtn = new Button("+ Add New Note");
        addBtn.setFont(Font.font("Poppins", 14));
        addBtn.setStyle("-fx-background-color: linear-gradient(to right, #FACEEA, #D7D8FF); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddForm());

        // Create scrollable container for the board
        ScrollPane scrollPane = new ScrollPane(boardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setPrefViewportWidth(850);

        mainContent.getChildren().addAll(headerBox, addBtn, scrollPane);
        this.setCenter(mainContent);
    }

    private void showAddForm() {
        if (showAddForm) return;

        showAddForm = true;

        // Create add note form
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 30; -fx-border-color: #FACEEA; -fx-border-width: 2;");
        form.setMaxWidth(400);
        form.setAlignment(Pos.TOP_CENTER);

        Label formTitle = new Label("New Note");
        formTitle.setFont(Font.font("Poppins", 18));
        formTitle.setTextFill(Color.web("#5c5470"));

        TextArea noteContent = new TextArea();
        noteContent.setPromptText("Write your note here...");
        noteContent.setPrefRowCount(6);
        noteContent.setStyle("-fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: #E2D6FF; -fx-background-color: white; " +
                "-fx-text-fill: #374151; -fx-font-family: 'Poppins';");

        // Color picker with live preview
        VBox colorSection = new VBox(8);
        Label colorLabel = new Label("Choose color:");
        colorLabel.setFont(Font.font("Poppins", 12));
        colorLabel.setTextFill(Color.web("#756f86"));

        HBox colorButtons = new HBox(8);
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
        colorPreview.setPrefSize(40, 40);
        colorPreview.setStyle("-fx-background-color: " + colors[0] + "; -fx-background-radius: 10; -fx-border-color: #d1d5db; -fx-border-width: 1;");

        HBox colorPickerWithPreview = new HBox(15);
        colorPickerWithPreview.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < colors.length; i++) {
            ToggleButton colorBtn = new ToggleButton();
            colorBtn.setStyle("-fx-background-color: " + colors[i] + "; " +
                    "-fx-background-radius: 50%; -fx-min-width: 28; -fx-min-height: 28; " +
                    "-fx-border-color: transparent;");
            colorBtn.setToggleGroup(colorGroup);
            colorBtn.setUserData(i);

            colorBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    int selectedIndex = (int) colorBtn.getUserData();
                    colorPreview.setStyle("-fx-background-color: " + colors[selectedIndex] +
                            "; -fx-background-radius: 10; -fx-border-color: #d1d5db; -fx-border-width: 1;");
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

        // Form buttons
        HBox formButtons = new HBox(10);
        formButtons.setAlignment(Pos.CENTER);

        Button submitBtn = new Button("Add Note");
        submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #FACEEA, #D7D8FF); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 8 16;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #5c5470; -fx-border-color: #d1d5db; " +
                "-fx-border-radius: 20; -fx-padding: 8 16;");

        formButtons.getChildren().addAll(submitBtn, cancelBtn);
        form.getChildren().addAll(formTitle, noteContent, colorSection, formButtons);

        // Add form to center temporarily
        StackPane overlay = new StackPane(form);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        overlay.setAlignment(Pos.CENTER);

        this.setCenter(overlay);

        // Button actions
        submitBtn.setOnAction(e -> {
            if (colorGroup.getSelectedToggle() != null) {
                int selectedColor = (int) colorGroup.getSelectedToggle().getUserData();
                addNewNote(noteContent.getText(), selectedColor);
            }
            showAddForm = false;
            refreshView();
        });

        cancelBtn.setOnAction(e -> {
            showAddForm = false;
            refreshView();
        });
    }

    private void refreshView() {
        this.getChildren().clear();
        createView();
    }

    private void addNewNote(String content, int colorIndex) {
        if (content == null || content.trim().isEmpty()) return;

        StickyNote note = new StickyNote(controller, content, colorIndex);
        note.setLayoutX(Math.random() * 500 + 50);
        note.setLayoutY(Math.random() * 300 + 50);
        boardPane.getChildren().add(note);
        notes.add(note);
    }

    /** Inner class for draggable sticky notes */
    private class StickyNote extends StackPane {
        private double mouseX, mouseY;
        private final TextArea textArea;
        private int colorIndex;

        private static final String[] COLORS = new String[6];
        static {
            COLORS[0] = "#fef08a";
            COLORS[1] = "#fecaca";
            COLORS[2] = "#bbf7d0";
            COLORS[3] = "#bfdbfe";
            COLORS[4] = "#e9d5ff";
            COLORS[5] = "#fed7aa";
        }

        public StickyNote(NotesController controller, String content, int colorIndex) {
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
                    "-fx-text-fill: #374151; " +
                    "-fx-font-weight: normal; " +
                    "-fx-opacity: 1.0;");
            textArea.setPrefSize(200, 150);
            textArea.setPadding(new Insets(15, 10, 10, 10));
            textArea.setFocusTraversable(false);

            // FIXED: Delete button with proper "X" character
            Button deleteBtn = new Button("✕"); // This should show as "X"
            deleteBtn.setFont(Font.font("Arial", 12)); // Use Arial for better X rendering
            deleteBtn.setTextFill(Color.web("#dc2626"));
            deleteBtn.setStyle("-fx-background-color: rgba(254, 202, 202, 0.9); " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 3; " +
                    "-fx-border-color: transparent;");
            deleteBtn.setOpacity(0);
            deleteBtn.setPrefSize(20, 20);
            deleteBtn.setOnAction(e -> {
                Pane parent = (Pane) this.getParent();
                if (parent != null) {
                    parent.getChildren().remove(this);
                    notes.remove(this);
                }
            });

            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(6, 6, 0, 0));

            // FIXED: Color picker with proper event handling
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
                        "-fx-border-color: transparent; -fx-padding: 0;");

                final int index = i;
                colorBtn.setOnAction(e -> {
                    this.colorIndex = index;
                    this.setBackground(new Background(new BackgroundFill(
                            Color.web(COLORS[index]), new CornerRadii(15), Insets.EMPTY)));
                    // Hide color picker after selection
                    colorPicker.setOpacity(0);
                });
                colorPicker.getChildren().add(colorBtn);
            }

            // Position color picker above the note
            StackPane.setAlignment(colorPicker, Pos.TOP_CENTER);
            StackPane.setMargin(colorPicker, new Insets(-35, 0, 0, 0));

            // Layout with color picker above text area
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
                // Only hide color picker if mouse is not over it
                if (!colorPicker.isHover()) {
                    colorPicker.setOpacity(0);
                }
            });

            // FIXED: Proper color picker hover handling
            colorPicker.setOnMouseEntered(e -> {
                colorPicker.setOpacity(1);
            });

            colorPicker.setOnMouseExited(e -> {
                colorPicker.setOpacity(0);
            });

            // Enable dragging
            setOnMousePressed(this::handleMousePressed);
            setOnMouseDragged(this::handleMouseDragged);
            setOnMouseClicked(e -> this.toFront());

            // Make text area clickable for editing
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
                setLayoutX(event.getSceneX() - mouseX);
                setLayoutY(event.getSceneY() - mouseY);
            }
        }
    }
}