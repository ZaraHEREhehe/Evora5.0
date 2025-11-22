// src/main/java/com/example/demo1/ToDoList/TodoView.java
package com.example.demo1.ToDoList;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Theme.ThemeManager;
import com.example.demo1.Theme.Theme;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.SnapshotParameters;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class TodoView {

    private Stage stage;
    private TodoController controller;
    private BorderPane root;
    private Scene scene;
    private boolean isFirstShow = true;
    private VBox todoList;
    private StackPane overlayRoot;
    private int currentUserId;
    private Sidebar sidebar;
    private String userName;
    private ThemeManager themeManager;

    public TodoView(int userId) {
        this.currentUserId = userId;
        this.controller = new TodoController(userId);
        this.themeManager = ThemeManager.getInstance();
    }

    public TodoView(int userId, Sidebar sidebar) {
        this.currentUserId = userId;
        this.sidebar = sidebar;
        this.controller = new TodoController(userId);
        this.themeManager = ThemeManager.getInstance();
    }

    // Keep the stage constructor for backward compatibility
    public TodoView(Stage stage) {
        this.stage = stage;
        this.themeManager = ThemeManager.getInstance();
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        if (this.controller == null) {
            this.controller = new TodoController(userId);
        }
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void show() {
        if (scene == null) {
            root = new BorderPane();
            // FIXED: Use theme background color
            Theme currentTheme = themeManager.getCurrentTheme();
            root.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

            // Use the sidebar that was set (likely from MainController)
            if (sidebar != null) {
                root.setLeft(sidebar);
            }

            scene = new Scene(root, 1400, 900);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.setTitle("Évora • Todo List");

            root.prefWidthProperty().bind(scene.widthProperty());
            root.prefHeightProperty().bind(scene.heightProperty());

            overlayRoot = new StackPane();
            overlayRoot.setAlignment(Pos.TOP_CENTER);

            overlayRoot.getChildren().add(buildMainContent());
            root.setCenter(overlayRoot);

            scene.widthProperty().addListener((obs, old, width) -> {
                if (!isFirstShow && width.doubleValue() > 0) {
                    Platform.runLater(() -> {
                        ScrollPane newContent = buildMainContent();
                        overlayRoot.getChildren().setAll(newContent);
                    });
                }
            });

            isFirstShow = false;
        } else {
            ScrollPane newContent = buildMainContent();
            // ✅ Ensure overlayRoot exists even when reusing scene
            if (overlayRoot == null) {
                overlayRoot = new StackPane();
                overlayRoot.setAlignment(Pos.TOP_CENTER);
                root.setCenter(overlayRoot);
            }
            overlayRoot.getChildren().setAll(newContent);
        }
        debugSceneStructure();
        stage.show();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private ScrollPane buildMainContent() {
        VBox main = new VBox(24);
        main.setPadding(new Insets(40));
        main.setAlignment(Pos.TOP_CENTER);

        double scale = getScale();
        main.setStyle(String.format("-fx-font-size: %.2fpx;", 16 * scale));

        // FIXED: Use theme background color for main container
        Theme currentTheme = themeManager.getCurrentTheme();
        main.setStyle(main.getStyle() + "-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

        // FIXED: Use theme text color for title
        Label title = new Label("To-Do List");
        title.setStyle("-fx-font-weight: 700; -fx-text-fill: " + currentTheme.getTextColor() + ";");
        title.setFont(Font.font("Poppins", 36 * scale));

        // FIXED: Use theme text color for subtitle (with reduced opacity for secondary text)
        Label subtitle = new Label("Organize your tasks and get things done!");
        subtitle.setStyle("-fx-text-fill: " + currentTheme.getTextColor() + "AA;"); // AA = ~67% opacity
        subtitle.setFont(Font.font("Poppins", 16 * scale));

        VBox header = new VBox(10, title, subtitle);
        header.setAlignment(Pos.CENTER);

        VBox addCard = createAddCard();
        todoList = createTodoList();

        VBox content = new VBox(24, addCard, todoList);
        content.setAlignment(Pos.TOP_CENTER);

        main.getChildren().addAll(header, content);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scroll;
    }

    private double getScale() {
        double width = scene != null ? scene.getWidth() : 1400;
        return Math.max(0.8, Math.min(1.4, width / 1400.0));
    }

    private VBox createAddCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        Label title = new Label("Add New Task");
        title.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2937;");
        title.setFont(Font.font("Poppins", 18 * getScale()));

        TextField input = new TextField();
        input.setPromptText("What needs to be done?");
        input.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #d8b4fe; -fx-background-color: white; -fx-padding: 12;");
        input.setPrefHeight(48);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Due Date (optional)");
        datePicker.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-color: #d8b4fe; " +
                        "-fx-background-color: white; " +
                        "-fx-padding: 0 12 0 12;"
        );
        datePicker.setPrefWidth(300);

        ChoiceBox<String> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue("Medium");
        priorityBox.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #d8b4fe; -fx-background-color: white;");
        priorityBox.setPrefWidth(120);

        Button addBtn = new Button();
        FontIcon plus = new FontIcon(FontAwesomeSolid.PLUS);
        plus.setIconColor(Color.web("#6b21a8"));
        plus.setIconSize(16);
        addBtn.setGraphic(plus);
        addBtn.setStyle("-fx-background-radius: 20; -fx-background-color: linear-gradient(to right, #e9d5ff, #fbcfe8); -fx-padding: 12;");
        addBtn.setPrefSize(48, 48);

        HBox row = new HBox(12, new Label("Due Date"), datePicker, new Label("Priority"), priorityBox, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        input.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) addTodo(input, datePicker, priorityBox);
        });

        addBtn.setOnAction(e -> addTodo(input, datePicker, priorityBox));

        card.getChildren().addAll(title, input, row);
        return card;
    }

    private void addTodo(TextField input, DatePicker datePicker, ChoiceBox<String> priorityBox) {
        String text = input.getText().trim();
        if (!text.isEmpty()) {
            TodoController.Todo todo = new TodoController.Todo(
                    "", text, false,
                    priorityBox.getValue().toLowerCase(),
                    datePicker.getValue() != null ? datePicker.getValue().toString() : null
            );
            controller.addTodo(currentUserId, todo);
            controller.refreshTodos();

            // Increment exp based on priority of added task
            int expToAdd = getAddTaskExperience(priorityBox.getValue().toLowerCase());
            controller.incrementUserExperience(expToAdd, currentUserId);

            // Refresh sidebar experience if sidebar is available
            if (sidebar != null) {
                sidebar.refreshExperienceFromDatabase(currentUserId);
            }

            input.clear();
            datePicker.setValue(null);
            priorityBox.setValue("Medium");
            refreshTodoList();
        }
    }

    private VBox createTodoList() {
        VBox list = new VBox(12);
        list.setAlignment(Pos.TOP_CENTER);

        List<TodoController.Todo> todos = controller.getTodos();
        if (todos.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setPadding(new Insets(32));
            empty.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
            empty.setEffect(new DropShadow(20, Color.gray(0, 0.15)));
            Label msg = new Label("No tasks yet! Add one above to get started");
            msg.setStyle("-fx-text-fill: #6b7280;");
            msg.setFont(Font.font("Poppins", 16));
            empty.getChildren().add(msg);
            list.getChildren().add(empty);
        } else {
            for (TodoController.Todo todo : todos) {
                list.getChildren().add(createTodoItem(todo));
            }
        }

        enableDragAndDrop(list);
        return list;
    }

    private void enableDragAndDrop(VBox list) {
        list.getChildren().forEach(node -> {
            if (node instanceof VBox item) {
                // Remove existing handlers to avoid duplicates
                item.setOnDragDetected(null);
                item.setOnDragOver(null);
                item.setOnDragExited(null);
                item.setOnDragDropped(null);
                item.setOnDragDone(null);

                item.setOnDragDetected(event -> {
                    if (list.getChildren().contains(item)) {
                        Dragboard dragboard = item.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();

                        int index = list.getChildren().indexOf(item);
                        TodoController.Todo draggedTodo = controller.getTodos().get(index);
                        content.put(TodoController.TODO_FORMAT, draggedTodo);
                        dragboard.setContent(content);

                        SnapshotParameters snapParams = new SnapshotParameters();
                        snapParams.setFill(Color.TRANSPARENT);
                        Image snapshot = item.snapshot(snapParams, null);
                        dragboard.setDragView(snapshot, event.getX(), event.getY());
                        dragboard.setDragViewOffsetX(event.getX());
                        dragboard.setDragViewOffsetY(event.getY());

                        item.setOpacity(0.4);
                        item.setEffect(new DropShadow(30, Color.gray(0, 0.3)));

                        Timeline wiggle = new Timeline(
                                new KeyFrame(Duration.millis(0),   new KeyValue(item.rotateProperty(), 0)),
                                new KeyFrame(Duration.millis(80),  new KeyValue(item.rotateProperty(), -6)),
                                new KeyFrame(Duration.millis(160), new KeyValue(item.rotateProperty(), 6)),
                                new KeyFrame(Duration.millis(240), new KeyValue(item.rotateProperty(), -4)),
                                new KeyFrame(Duration.millis(320), new KeyValue(item.rotateProperty(), 4)),
                                new KeyFrame(Duration.millis(400), new KeyValue(item.rotateProperty(), 0))
                        );
                        wiggle.setCycleCount(Timeline.INDEFINITE);
                        wiggle.play();
                        item.getProperties().put("wiggle", wiggle);

                        // Store the dragged item and index
                        list.getProperties().put("draggedItem", item);
                        list.getProperties().put("draggedIndex", index);

                        event.consume();
                    }
                });

                item.setOnDragOver(event -> {
                    if (event.getGestureSource() != item && event.getDragboard().hasContent(TodoController.TODO_FORMAT)) {
                        event.acceptTransferModes(TransferMode.MOVE);

                        // Store original style if not already stored
                        String originalStyle = (String) item.getProperties().get("originalStyle");
                        if (originalStyle == null) {
                            originalStyle = item.getStyle();
                            item.getProperties().put("originalStyle", originalStyle);
                        }

                        // Highlight drop target
                        item.setStyle(originalStyle + "-fx-border-color: #a78bfa; -fx-border-width: 2; -fx-border-radius: 26;");
                    }
                    event.consume();
                });

                item.setOnDragExited(event -> {
                    // Restore original style when drag exits
                    String originalStyle = (String) item.getProperties().get("originalStyle");
                    if (originalStyle != null) {
                        item.setStyle(originalStyle);
                    }
                    event.consume();
                });

                item.setOnDragDropped(event -> {
                    Dragboard dragboard = event.getDragboard();
                    boolean success = false;

                    if (dragboard.hasContent(TodoController.TODO_FORMAT) && list.getChildren().contains(item)) {
                        TodoController.Todo draggedTodo = (TodoController.Todo) dragboard.getContent(TodoController.TODO_FORMAT);
                        int targetIndex = list.getChildren().indexOf(item);
                        Integer sourceIndex = (Integer) list.getProperties().get("draggedIndex");

                        if (sourceIndex != null && sourceIndex != targetIndex) {
                            VBox draggedItem = (VBox) list.getProperties().get("draggedItem");

                            if (draggedItem != null) {
                                List<TodoController.Todo> currentTodos = controller.getTodos();

                                // ✅ FIXED: Remove from BOTH lists using the original source index
                                list.getChildren().remove(sourceIndex.intValue());
                                currentTodos.remove(sourceIndex.intValue());

                                // ✅ FIXED: Calculate correct insertion index
                                int insertionIndex;
                                if (sourceIndex < targetIndex) {
                                    // Moving DOWN: target index decreased by 1 because we removed an item before it
                                    insertionIndex = targetIndex - 1;
                                } else {
                                    // Moving UP: target index remains the same
                                    insertionIndex = targetIndex;
                                }

                                // Add to new position in BOTH lists
                                list.getChildren().add(insertionIndex, draggedItem);
                                currentTodos.add(insertionIndex, draggedTodo);

                                // ✅ FIX: Restore completed task styling after drag
                                if (draggedTodo.isCompleted()) {
                                    draggedItem.setOpacity(0.6);
                                    // Find and update the text label to add strikethrough
                                    for (Node child : draggedItem.getChildren()) {
                                        if (child instanceof HBox topRow) {
                                            for (Node topChild : topRow.getChildren()) {
                                                if (topChild instanceof Label textLabel) {
                                                    // Ensure strikethrough is applied
                                                    if (!textLabel.getStyle().contains("-fx-strikethrough: true")) {
                                                        textLabel.setStyle(textLabel.getStyle() + "-fx-strikethrough: true;");
                                                    }
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    // Ensure non-completed tasks are fully opaque
                                    draggedItem.setOpacity(1.0);
                                }

                                // Update database with new order
                                controller.updateTaskOrder(currentUserId, currentTodos);
                                success = true;

                                System.out.println("Moved task from position " + sourceIndex + " to " + insertionIndex + " (target was " + targetIndex + ")");
                            }
                        }
                    }

                    event.setDropCompleted(success);
                    event.consume();
                });

                item.setOnDragDone(event -> {
                    // Clean up visual effects
                    item.setOpacity(1.0);
                    item.setEffect(null);
                    item.setRotate(0);

                    // Stop wiggle animation
                    Timeline wiggle = (Timeline) item.getProperties().get("wiggle");
                    if (wiggle != null) {
                        wiggle.stop();
                        item.getProperties().remove("wiggle");
                    }

                    // Restore original style
                    String originalStyle = (String) item.getProperties().get("originalStyle");
                    if (originalStyle != null) {
                        item.setStyle(originalStyle);
                        item.getProperties().remove("originalStyle");
                    }

                    // Clear dragged properties
                    list.getProperties().remove("draggedItem");
                    list.getProperties().remove("draggedIndex");
                    // ✅ FIX: Refresh the entire list to restore proper styling
                    refreshTodoList();
                    event.consume();
                });
            }
        });
    }

    private VBox createTodoItem(TodoController.Todo todo) {
        VBox item = new VBox(0);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle(getGradientStyle(todo.getPriority()) +
                "-fx-background-radius: 26; -fx-border-radius: 26; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3);");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(item.widthProperty());
        clip.heightProperty().bind(item.heightProperty());
        clip.setArcWidth(52);
        clip.setArcHeight(52);
        item.setClip(clip);

        if (todo.isCompleted()) item.setOpacity(0.6);

        CheckBox check = new CheckBox();
        check.setSelected(todo.isCompleted());
        check.setStyle("-fx-border-width: 2;");

        Label text = new Label(todo.getText());
        text.setWrapText(true);
        text.setMaxWidth(Double.MAX_VALUE);
        text.setStyle("-fx-text-fill: #1f2937;");
        text.setFont(Font.font("Poppins", 14));
        if (todo.isCompleted()) {
            text.setStyle(text.getStyle() + "-fx-strikethrough: true;");
        }

        HBox top = new HBox(10, check, text);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label priority = new Label(todo.getPriority().substring(0, 1).toUpperCase() +
                todo.getPriority().substring(1));
        priority.setStyle(getBadgeStyle(todo.getPriority()));

        HBox due = new HBox(6);
        if (todo.getDueDate() != null) {
            FontIcon cal = new FontIcon(FontAwesomeSolid.CALENDAR);
            cal.setIconSize(12);
            cal.setIconColor(Color.web("#6b7280"));
            Label date = new Label(LocalDate.parse(todo.getDueDate())
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
            date.setStyle("-fx-text-fill: #6b7280;");
            due.getChildren().addAll(cal, date);
        }

        HBox badges = new HBox(6, priority);
        if (todo.getDueDate() != null) badges.getChildren().add(due);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.setPadding(new Insets(0));

        Button delete = new Button();
        FontIcon trash = new FontIcon(FontAwesomeSolid.TRASH);
        trash.setIconSize(16);
        trash.setIconColor(Color.web("#dc2626"));
        delete.setGraphic(trash);
        delete.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 8;");

        HBox actions = new HBox(8, new Region(), delete);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actions.getChildren().get(0), Priority.ALWAYS);

        item.getChildren().addAll(top, badges, actions);

        check.setOnAction(e -> {
            // For exp
            boolean wasPreviouslyCompleted = todo.isCompleted();

            // Old
            todo.setCompleted(check.isSelected());
            controller.updateTodo(todo);
            controller.refreshTodos();

            // For exp
            if (check.isSelected() && !wasPreviouslyCompleted) {   // Award experience ONLY when task changes from not completed to completed
                int expToAdd = getCompleteTaskExperience(todo.getPriority());
                controller.incrementUserExperience(expToAdd, currentUserId);

                // Refresh sidebar experience if sidebar is available
                if (sidebar != null) {
                    sidebar.refreshExperienceFromDatabase(currentUserId);
                }
            }

            refreshTodoList();
            if (check.isSelected()) {
                showConfetti();
                playChime();
            }
        });

        delete.setOnAction(e -> {
            controller.deleteTodo(todo.getId());
            controller.refreshTodos();
            refreshTodoList();
        });

        return item;
    }

    private String getGradientStyle(String priority) {
        return switch (priority) {
            case "high" -> "-fx-background-color: linear-gradient(to right, #fecaca, #fbcfe8); -fx-border-color: #fca5a5; ";
            case "medium" -> "-fx-background-color: linear-gradient(to right, #fde68a, #fed7aa); -fx-border-color: #fbbf24; ";
            case "low" -> "-fx-background-color: linear-gradient(to right, #bbf7d0, #ccfbdf); -fx-border-color: #86efac; ";
            default -> "-fx-background-color: white; -fx-border-color: #e5e7eb; ";
        };
    }

    private String getBadgeStyle(String priority) {
        String color = switch (priority) {
            case "high" -> "#ef4444";
            case "medium" -> "#f59e0b";
            case "low" -> "#10b981";
            default -> "#6b7280";
        };
        String bgColor = color + "B3";
        return "-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 2 8; " +
                "-fx-font-size: 11; " +
                "-fx-background-radius: 16;";
    }

    private void refreshTodoList() {
        controller.refreshTodos();
        todoList.getChildren().clear();
        List<TodoController.Todo> todos = controller.getTodos();
        if (todos.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setPadding(new Insets(32));
            empty.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
            empty.setEffect(new DropShadow(20, Color.gray(0, 0.15)));
            Label msg = new Label("No tasks yet! Add one above to get started");
            msg.setStyle("-fx-text-fill: #6b7280;");
            msg.setFont(Font.font("Poppins", 16));
            empty.getChildren().add(msg);
            todoList.getChildren().add(empty);
        } else {
            for (TodoController.Todo todo : todos) {
                todoList.getChildren().add(createTodoItem(todo));
            }
        }

        // Re-enable drag and drop after refresh
        enableDragAndDrop(todoList);
    }

    // Call this method when the TodoView is added to a parent container
    public void initializeAsComponent() {
        // This ensures todoList is created and can be used to find the scene
        if (todoList == null) {
            buildMainContent();
        }
    }

// ✅ FIXED: showConfetti method that creates overlayRoot dynamically
    private void showConfetti() {
        // Try multiple approaches to find a scene and create overlay
        Scene currentScene = null;
        StackPane currentOverlayRoot = null;

        // Approach 1: Try to get scene from todoList
        if (todoList != null && todoList.getScene() != null) {
            currentScene = todoList.getScene();
            // Find or create overlayRoot in the scene structure
            if (currentScene.getRoot() instanceof BorderPane) {
                BorderPane rootPane = (BorderPane) currentScene.getRoot();

                // Look for existing StackPane in center
                if (rootPane.getCenter() instanceof StackPane) {
                    currentOverlayRoot = (StackPane) rootPane.getCenter();
                } else {
                    // Create a new StackPane overlay and set it as center
                    currentOverlayRoot = new StackPane();
                    currentOverlayRoot.setAlignment(Pos.TOP_CENTER);

                    // Store the original center content
                    Node originalCenter = rootPane.getCenter();
                    currentOverlayRoot.getChildren().add(originalCenter);
                    rootPane.setCenter(currentOverlayRoot);
                }
            }
        }
        // Approach 2: Try to get scene from sidebar
        else if (sidebar != null && sidebar.getScene() != null) {
            currentScene = sidebar.getScene();
            // Find or create overlayRoot in the scene structure
            if (currentScene.getRoot() instanceof BorderPane) {
                BorderPane rootPane = (BorderPane) currentScene.getRoot();

                // Look for existing StackPane in center
                if (rootPane.getCenter() instanceof StackPane) {
                    currentOverlayRoot = (StackPane) rootPane.getCenter();
                } else {
                    // Create a new StackPane overlay and set it as center
                    currentOverlayRoot = new StackPane();
                    currentOverlayRoot.setAlignment(Pos.TOP_CENTER);

                    // Store the original center content
                    Node originalCenter = rootPane.getCenter();
                    currentOverlayRoot.getChildren().add(originalCenter);
                    rootPane.setCenter(currentOverlayRoot);
                }
            }
        }

        if (currentScene == null || currentOverlayRoot == null) {
            System.out.println("❌ Cannot show confetti - no scene found or could not create overlay");
            System.out.println("   scene: " + currentScene);
            System.out.println("   overlayRoot: " + currentOverlayRoot);
            return;
        }

        Pane confettiPane = new Pane();
        confettiPane.setMouseTransparent(true);
        confettiPane.setPickOnBounds(false);
        currentOverlayRoot.getChildren().add(confettiPane);

        Random r = new Random();
        for (int i = 0; i < 80; i++) {
            Circle c = new Circle(4 + r.nextDouble() * 3);
            String[] colors = {"#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ec4899"};
            c.setFill(Color.web(colors[r.nextInt(colors.length)]));
            c.setCenterX(r.nextDouble() * currentScene.getWidth());
            c.setCenterY(-20);
            confettiPane.getChildren().add(c);

            Timeline fall = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(c.centerYProperty(), -20),
                            new KeyValue(c.opacityProperty(), 1.0)
                    ),
                    new KeyFrame(Duration.seconds(1.8 + r.nextDouble() * 0.5),
                            new KeyValue(c.centerYProperty(), currentScene.getHeight() + 20),
                            new KeyValue(c.opacityProperty(), 0.0)
                    )
            );
            fall.setOnFinished(e -> confettiPane.getChildren().remove(c));
            fall.play();
        }

        // ✅ FIX: Make final reference for use in lambda
        final StackPane finalOverlayRoot = currentOverlayRoot;
        final Pane finalConfettiPane = confettiPane;

        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            finalOverlayRoot.getChildren().remove(finalConfettiPane);
        }));
        cleanup.play();

        System.out.println("✅ Confetti shown successfully!");
    }

    private void playChime() {
        try {
            String soundPath = "/sounds/chime_16bit.wav";
            InputStream inputStream = getClass().getResourceAsStream(soundPath);
            if (inputStream == null) {
                System.out.println("Sound file not found: " + soundPath);
                return;
            }

            byte[] audioBytes = inputStream.readAllBytes();
            inputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            System.out.println("Could not play chime: " + e.getMessage());
        }
    }

    public ScrollPane getContent() {
        return buildMainContent();
    }

    // Helpers for exp calculation
    private int getAddTaskExperience(String priority) {
        return switch (priority.toLowerCase()) {
            case "high" -> 30;
            case "medium" -> 20;
            case "low" -> 10;
            default -> 15;
        };
    }

    private int getCompleteTaskExperience(String priority) {
        return switch (priority.toLowerCase()) {
            case "high" -> 200;
            case "medium" -> 100;
            case "low" -> 50;
            default -> 75;
        };
    }

    private void debugSceneStructure() {
        System.out.println("=== DEBUG SCENE STRUCTURE ===");
        System.out.println("stage: " + stage);
        System.out.println("scene: " + scene);
        System.out.println("root: " + root);
        System.out.println("overlayRoot: " + overlayRoot);
        System.out.println("todoList: " + todoList);

        if (scene != null) {
            System.out.println("scene.root: " + scene.getRoot());
            if (scene.getRoot() instanceof BorderPane) {
                BorderPane bp = (BorderPane) scene.getRoot();
                System.out.println("center: " + bp.getCenter());
                System.out.println("left: " + bp.getLeft());
            }
        }
        System.out.println("=== END DEBUG ===");
    }
}