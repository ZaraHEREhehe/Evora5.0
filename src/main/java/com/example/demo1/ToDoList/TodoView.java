// src/main/java/com/example/demo1/ToDoList/TodoView.java
package com.example.demo1.ToDoList;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
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
import java.util.*;

public class TodoView {

    private Stage stage;
    private Database db;
    private List<Todo> todos;
    private BorderPane root;
    private Scene scene;
    private boolean isFirstShow = true;
    private VBox todoList;
    private StackPane overlayRoot;
    private final int CURRENT_USER_ID = 1;
    private static final DataFormat TODO_FORMAT = new DataFormat("application/x-todo-object");
    String userName;

    private Sidebar sidebar;

    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    public TodoView() {
        this.db = new Database();
        this.todos = db.getTodos(CURRENT_USER_ID);
    }

    // Keep the stage constructor for backward compatibility
    public TodoView(Stage stage) {
        this(); // Call the no-arg constructor
        this.stage = stage;
    }

    public void setUsername(String userName)
    {
        this.userName = userName;
    }


    public void show() {
        if (scene == null) {
            root = new BorderPane();
            root.setStyle("-fx-background-color: #fdf7ff;");

            SidebarController sidebarController = new SidebarController();
            sidebarController.setStage(stage);

            // Remove the goTo callback and use the navigation directly
            sidebarController.setOnTabChange(tab -> {
                System.out.println("Sidebar navigation to: " + tab);
                // The Dashboard will handle the actual navigation
            });

         //   Sidebar sidebar = new Sidebar(sidebarController, userName, CURRENT_USER_ID);
          //  root.setLeft(sidebar);

            //sidebar set in main controller now
            root.setLeft(sidebar);

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
            overlayRoot.getChildren().setAll(newContent);
        }

        stage.show();
    }

    private ScrollPane buildMainContent() {
        VBox main = new VBox(24);
        main.setPadding(new Insets(40));
        main.setAlignment(Pos.TOP_CENTER);

        double scale = getScale();
        main.setStyle(String.format("-fx-font-size: %.2fpx;", 16 * scale));

        Label title = new Label("To-Do List");
        title.setStyle("-fx-font-weight: 700; -fx-text-fill: #5c5470;");
        title.setFont(Font.font("Poppins", 36 * scale));

        Label subtitle = new Label("Organize your tasks and get things done!");
        subtitle.setStyle("-fx-text-fill: #9189a5;");
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
            Todo todo = new Todo(
                    "", text, false,
                    priorityBox.getValue().toLowerCase(),
                    datePicker.getValue() != null ? datePicker.getValue().toString() : null
            );
            db.addTodo(CURRENT_USER_ID, todo);
            todos = db.getTodos(CURRENT_USER_ID);

            //increment exp based on priority of added task
            int expToAdd = getAddTaskExperience(priorityBox.getValue().toLowerCase());
            db.incrementUserExperience(expToAdd, CURRENT_USER_ID);
            sidebar.refreshExperienceFromDatabase(CURRENT_USER_ID);

            input.clear();
            datePicker.setValue(null);
            priorityBox.setValue("Medium");
            refreshTodoList();
        }
    }

    private VBox createTodoList() {
        VBox list = new VBox(12);
        list.setAlignment(Pos.TOP_CENTER);

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
            for (Todo todo : todos) {
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
                        Todo draggedTodo = todos.get(index);
                        content.put(TODO_FORMAT, draggedTodo);
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
                    if (event.getGestureSource() != item && event.getDragboard().hasContent(TODO_FORMAT)) {
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

                    if (dragboard.hasContent(TODO_FORMAT) && list.getChildren().contains(item)) {
                        Todo draggedTodo = (Todo) dragboard.getContent(TODO_FORMAT);
                        int targetIndex = list.getChildren().indexOf(item);
                        Integer sourceIndex = (Integer) list.getProperties().get("draggedIndex");

                        if (sourceIndex != null && sourceIndex != targetIndex) {
                            VBox draggedItem = (VBox) list.getProperties().get("draggedItem");

                            if (draggedItem != null) {
                                // Remove from old position
                                list.getChildren().remove(draggedItem);
                                todos.remove(draggedTodo);

                                // Calculate adjusted target index
                                int adjustedTargetIndex = sourceIndex < targetIndex ? targetIndex : targetIndex;

                                // Add to new position
                                list.getChildren().add(adjustedTargetIndex, draggedItem);
                                todos.add(adjustedTargetIndex, draggedTodo);

                                // Update database with new order
                                db.updateTaskOrder(CURRENT_USER_ID, todos);
                                success = true;

                                System.out.println("Moved task from position " + sourceIndex + " to " + adjustedTargetIndex);
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

                    event.consume();
                });
            }
        });
    }

    private VBox createTodoItem(Todo todo) {
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
            //for exp
            boolean wasPreviouslyCompleted = todo.isCompleted();

            //old
            todo.setCompleted(check.isSelected());
            db.updateTodo(todo);
            todos = db.getTodos(CURRENT_USER_ID);

            //for exp
            if (check.isSelected() && !wasPreviouslyCompleted) {   // Award experience ONLY when task changes from not completed to completed
                int expToAdd = getCompleteTaskExperience(todo.getPriority());
                db.incrementUserExperience(expToAdd, CURRENT_USER_ID);
                sidebar.refreshExperienceFromDatabase(CURRENT_USER_ID);
            }

            refreshTodoList();
            if (check.isSelected()) {
                showConfetti();
                playChime();
            }
        });

        delete.setOnAction(e -> {
            db.deleteTodo(todo.getId());
            todos = db.getTodos(CURRENT_USER_ID);
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
        todos = db.getTodos(CURRENT_USER_ID);
        todoList.getChildren().clear();
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
            for (Todo todo : todos) {
                todoList.getChildren().add(createTodoItem(todo));
            }
        }

        // Re-enable drag and drop after refresh
        enableDragAndDrop(todoList);
    }

    private void showConfetti() {
        if (overlayRoot == null || scene == null) return;
        Pane confettiPane = new Pane();
        confettiPane.setMouseTransparent(true);
        confettiPane.setPickOnBounds(false);
        overlayRoot.getChildren().add(confettiPane);

        Random r = new Random();
        for (int i = 0; i < 80; i++) {
            Circle c = new Circle(4 + r.nextDouble() * 3);
            String[] colors = {"#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ec4899"};
            c.setFill(Color.web(colors[r.nextInt(colors.length)]));
            c.setCenterX(r.nextDouble() * scene.getWidth());
            c.setCenterY(-20);
            confettiPane.getChildren().add(c);

            Timeline fall = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(c.centerYProperty(), -20),
                            new KeyValue(c.opacityProperty(), 1.0)
                    ),
                    new KeyFrame(Duration.seconds(1.8 + r.nextDouble() * 0.5),
                            new KeyValue(c.centerYProperty(), scene.getHeight() + 20),
                            new KeyValue(c.opacityProperty(), 0.0)
                    )
            );
            fall.setOnFinished(e -> confettiPane.getChildren().remove(c));
            fall.play();
        }

        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            overlayRoot.getChildren().remove(confettiPane);
        }));
        cleanup.play();
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

    //helpers for exp calculation
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

    static class Todo implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String id, text, priority, dueDate;
        private boolean completed;

        Todo(String id, String text, boolean completed, String priority, String dueDate) {
            this.id = id;
            this.text = text;
            this.completed = completed;
            this.priority = priority;
            this.dueDate = dueDate;
        }

        public String getId() { return id; }
        public String getText() { return text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public String getPriority() { return priority; }
        public String getDueDate() { return dueDate; }
    }
}
