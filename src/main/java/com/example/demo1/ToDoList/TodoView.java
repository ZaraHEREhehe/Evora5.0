// src/main/java/com/example/demo1/ToDoList/TodoView.java
package com.example.demo1.ToDoList;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class TodoView {

    private final Stage stage;
    private final List<Todo> todos;
    private BorderPane root;
    private Scene scene;
    private boolean isFirstShow = true;
    private VBox todoList;
    private StackPane overlayRoot; // ← ADD THIS LINE

    public TodoView(Stage stage) {
        this.stage = stage;
        this.todos = new ArrayList<>(getSampleTodos()); // ← NOW MUTABLE
    }

    public void show() {
        if (scene == null) {
            root = new BorderPane();
            root.setStyle("-fx-background-color: #fdf7ff;");

            SidebarController sidebarController = new SidebarController();
            sidebarController.setStage(stage);
            sidebarController.setOnTabChange(tab -> sidebarController.goTo(tab));

            Sidebar sidebar = new Sidebar(sidebarController, "Zara");
            root.setLeft(sidebar);

            scene = new Scene(root, 1400, 900);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.setTitle("Évora • Todo List");

            root.prefWidthProperty().bind(scene.widthProperty());
            root.prefHeightProperty().bind(scene.heightProperty());

            // === CREATE overlayRoot ONCE ===
            overlayRoot = new StackPane();
            overlayRoot.setAlignment(Pos.TOP_CENTER);

            // Initial content
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
            // Rebuild content inside overlayRoot
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
                        "-fx-padding: 0 12 0 12;" // top, right, bottom, left → vertical = 0, horizontal = 12
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
                    UUID.randomUUID().toString(),
                    text,
                    false,
                    priorityBox.getValue().toLowerCase(),
                    datePicker.getValue() != null ? datePicker.getValue().toString() : null
            );
            todos.add(0, todo);
            input.clear();
            datePicker.setValue(null);
            priorityBox.setValue("Medium");
            refreshTodoList();
            showConfetti();
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

                // === DRAG DETECTED: LIFT + WIGGLE ===
                item.setOnDragDetected(event -> {
                    if (list.getChildren().contains(item)) {
                        Dragboard db = item.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(String.valueOf(list.getChildren().indexOf(item)));
                        db.setContent(content);

                        // Visual: lift + shadow + wiggle
                        item.setEffect(new DropShadow(30, Color.gray(0, 0.3)));
                        item.setTranslateY(-8);
                        item.toFront();

                        // Wiggle animation
                        Timeline wiggle = new Timeline(
                                new KeyFrame(Duration.millis(0),   new javafx.animation.KeyValue(item.rotateProperty(), 0)),
                                new KeyFrame(Duration.millis(80),  new javafx.animation.KeyValue(item.rotateProperty(), -6)),
                                new KeyFrame(Duration.millis(160), new javafx.animation.KeyValue(item.rotateProperty(), 6)),
                                new KeyFrame(Duration.millis(240), new javafx.animation.KeyValue(item.rotateProperty(), -4)),
                                new KeyFrame(Duration.millis(320), new javafx.animation.KeyValue(item.rotateProperty(), 4)),
                                new KeyFrame(Duration.millis(400), new javafx.animation.KeyValue(item.rotateProperty(), 0))
                        );
                        wiggle.play();
                    }
                    event.consume();
                });

                // === DRAG OVER: HIGHLIGHT DROP ZONE ===
                item.setOnDragOver(event -> {
                    if (event.getGestureSource() != item && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        item.setStyle(item.getStyle() + "-fx-border-color: #a78bfa; -fx-border-width: 2; -fx-border-radius: 26;");
                    }
                    event.consume();
                });

                // === DRAG EXIT: REMOVE HIGHLIGHT ===
                item.setOnDragExited(event -> {
                    String style = item.getStyle();
                    style = style.replaceAll("-fx-border-color:[^;]+;", "")
                            .replaceAll("-fx-border-width:[^;]+;", "");
                    item.setStyle(style);
                    event.consume();
                });

                // === DRAG DROPPED: RESET STYLE + REBUILD ===
                item.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString() && list.getChildren().contains(item)) {
                        int draggedIdx = Integer.parseInt(db.getString());
                        int thisIdx = list.getChildren().indexOf(item);
                        if (draggedIdx >= 0 && draggedIdx < todos.size() && thisIdx >= 0 && thisIdx < todos.size()) {
                            Todo dragged = todos.get(draggedIdx);
                            todos.remove(draggedIdx);
                            int targetIdx = thisIdx > draggedIdx ? thisIdx - 1 : thisIdx;
                            todos.add(targetIdx, dragged);
                            success = true;
                            refreshTodoList();
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                // === DRAG DONE: RESET TRANSFORM ===
                item.setOnDragDone(event -> {
                    item.setEffect(null);
                    item.setTranslateY(0);
                    item.setRotate(0);
                    String style = item.getStyle();
                    style = style.replaceAll("-fx-border-color:[^;]+;", "")
                            .replaceAll("-fx-border-width:[^;]+;", "");
                    item.setStyle(style);
                });
            }
        });
    }

    private VBox createTodoItem(Todo todo) {
        VBox item = new VBox(0); // ← ZERO spacing between rows
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle(getGradientStyle(todo.getPriority()) +
                "-fx-background-radius: 26; -fx-border-radius: 26; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3);");

        // === CLIP ===
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(item.widthProperty());
        clip.heightProperty().bind(item.heightProperty());
        clip.setArcWidth(52);
        clip.setArcHeight(52);
        item.setClip(clip);

        if (todo.isCompleted()) item.setOpacity(0.6);

        // === CHECKBOX + TEXT (WRAPS!) ===
        CheckBox check = new CheckBox();
        check.setSelected(todo.isCompleted());
        check.setStyle("-fx-border-width: 2;");

        Label text = new Label(todo.getText());
        text.setWrapText(true);                    // ← WRAP LONG TEXT
        text.setMaxWidth(Double.MAX_VALUE);        // ← Allow full width
        text.setStyle("-fx-text-fill: #1f2937;");
        text.setFont(Font.font("Poppins", 14));
        if (todo.isCompleted()) {
            text.setStyle(text.getStyle() + "-fx-strikethrough: true;");
        }

        HBox top = new HBox(10, check, text);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);

        // === BADGES (TIGHT, NO GAP BELOW) ===
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
        badges.setPadding(new Insets(0, 0, 0, 0)); // ← No extra padding

        // === DELETE BUTTON ===
        Button delete = new Button();
        FontIcon trash = new FontIcon(FontAwesomeSolid.TRASH);
        trash.setIconSize(16);
        trash.setIconColor(Color.web("#dc2626"));
        delete.setGraphic(trash);
        delete.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 8;");

        HBox actions = new HBox(8, new Region(), delete);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actions.getChildren().get(0), Priority.ALWAYS);

        // === FINAL LAYOUT: top + badges + actions in VBox with 0 spacing ===
        item.getChildren().addAll(top, badges, actions);

        // === EVENTS ===
        check.setOnAction(e -> {
            todo.setCompleted(check.isSelected());
            refreshTodoList();
            if (check.isSelected()){
                showConfetti();
                playChime();
            };
        });

        delete.setOnAction(e -> {
            todos.remove(todo);
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

        // 70% opacity → softer, less aggressive
        String bgColor = color + "B3"; // B3 = 70% opacity in hex

        return "-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 2 8; " +           // ← Smaller padding
                "-fx-font-size: 11; " +          // ← Smaller text
                "-fx-background-radius: 16;";   // ← Slightly smaller radius
    }

    private void refreshTodoList() {
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

        Platform.runLater(() -> {
            ScrollPane newContent = buildMainContent();

            // === PRESERVE OVERLAYS (LIKE CONFETTI) ===
            Node overlay = overlayRoot.getChildren().size() > 1 ? overlayRoot.getChildren().get(1) : null;
            overlayRoot.getChildren().setAll(newContent);
            if (overlay != null) {
                overlayRoot.getChildren().add(overlay);
            }

            enableDragAndDrop(todoList);
        });
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
                            new javafx.animation.KeyValue(c.centerYProperty(), -20),
                            new javafx.animation.KeyValue(c.opacityProperty(), 1.0)
                    ),
                    new KeyFrame(Duration.seconds(1.8 + r.nextDouble() * 0.5),
                            new javafx.animation.KeyValue(c.centerYProperty(), scene.getHeight() + 20),
                            new javafx.animation.KeyValue(c.opacityProperty(), 0.0)
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

            // Read entire file into byte array
            byte[] audioBytes = inputStream.readAllBytes();
            inputStream.close();

            // Use ByteArrayInputStream (supports mark/reset)
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // Optional: wait for sound to finish (prevents early GC)
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            System.out.println("Could not play chime: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private List<Todo> getSampleTodos() {
        return new ArrayList<>(Arrays.asList(
                new Todo("1", "Review presentation slides", false, "high", "2025-09-04"),
                new Todo("2", "Call team meeting", true, "medium", "2025-09-03"),
                new Todo("3", "Organize desk workspace", false, "low", null),
                new Todo("4", "Submit expense reports", false, "high", "2025-09-05")
        ));
    }

    private static class Todo {
        private final String id, text, priority, dueDate;
        private boolean completed;

        Todo(String id, String text, boolean completed, String priority, String dueDate) {
            this.id = id; this.text = text; this.completed = completed;
            this.priority = priority; this.dueDate = dueDate;
        }

        public String getId() { return id; }
        public String getText() { return text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public String getPriority() { return priority; }
        public String getDueDate() { return dueDate; }
    }
}