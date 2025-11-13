// src/main/java/com/example/demo1/calendar/CalendarView.java
package com.example.demo1.Calendar;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarView {

    private final Stage stage;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private final List<Todo> todos;
    private BorderPane root;
    private Scene scene;
    private boolean isFirstShow = true;

    // === RESPONSIVE CACHE ===
    private double lastWidth = 1400;

    public CalendarView(Stage stage) {
        this.stage = stage;
        this.currentDate = LocalDate.now();
        this.selectedDate = null;
        this.todos = getSampleTodos();
    }

    public void show() {
        if (scene == null) {
            root = new BorderPane();
            root.setStyle("-fx-background-color: #fdf7ff;");

            // === SIDEBAR ===
            SidebarController sidebarController = new SidebarController();
            sidebarController.setStage(stage);
            sidebarController.setOnTabChange(tab -> sidebarController.goTo(tab));

            Sidebar sidebar = new Sidebar(sidebarController, "Zara");
            root.setLeft(sidebar);

            // === SCENE FIRST ===
            scene = new Scene(root, 1400, 900);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.setTitle("Évora • Calendar");

            root.prefWidthProperty().bind(scene.widthProperty());
            root.prefHeightProperty().bind(scene.heightProperty());

            // === BUILD CONTENT AFTER SCENE ===
            root.setCenter(buildMainContent());

            // === RESPONSIVE ON RESIZE (NO FLICKER) ===
            scene.widthProperty().addListener((obs, old, width) -> {
                if (!isFirstShow && width.doubleValue() > 0) {
                    lastWidth = width.doubleValue();
                    Platform.runLater(() -> root.setCenter(buildMainContent()));
                }
            });

            isFirstShow = false;
        } else {
            root.setCenter(buildMainContent());
        }

        stage.show();
    }

    private ScrollPane buildMainContent() {
        VBox main = new VBox(30);
        main.setPadding(new Insets(40));
        main.setAlignment(Pos.TOP_CENTER);

        double scale = getScale();
        main.setStyle(String.format("-fx-font-size: %.2fpx;", 16 * scale));

        Label title = new Label("Calendar");
        title.setStyle("-fx-font-weight: 700; -fx-text-fill: #5c5470;");
        title.setFont(Font.font("Poppins", 36 * scale));

        Label subtitle = new Label("See your tasks at a glance on the calendar!");
        subtitle.setStyle("-fx-text-fill: #9189a5;");
        subtitle.setFont(Font.font("Poppins", 16 * scale));

        VBox header = new VBox(10, title, subtitle);
        header.setAlignment(Pos.CENTER);

        VBox legend = createLegend();
        HBox body = new HBox(32);
        body.setAlignment(Pos.TOP_CENTER);

        VBox calendarCard = createCalendarCard();
        VBox sidePanel = createSidePanel();

        body.getChildren().addAll(calendarCard, sidePanel);
        HBox.setHgrow(calendarCard, Priority.ALWAYS);

        main.getChildren().addAll(header, legend, body);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scroll;
    }

    private double getScale() {
        double width = scene != null ? scene.getWidth() : lastWidth;
        return Math.max(0.8, Math.min(1.4, width / 1400.0));
    }

    private VBox createLegend() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setMaxWidth(600);
        card.setAlignment(Pos.CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        HBox row = new HBox(40);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
                legendItem("High Priority", "#ef4444"),
                legendItem("Medium Priority", "#f59e0b"),
                legendItem("Low Priority", "#10b981"),
                legendItem("Today", "#ec4899")
        );
        card.getChildren().add(row);
        return card;
    }

    private HBox legendItem(String text, String color) {
        HBox item = new HBox(10);
        Circle dot = new Circle(6, Color.web(color));
        Label label = new Label(text);
        label.setTextFill(Color.web("#4b5563"));
        item.getChildren().addAll(dot, label);
        return item;
    }

    private VBox createCalendarCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(32), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER);

        Button prev = createNavButton("Previous", () -> changeMonth(-1));
        Label monthLabel = new Label(getMonthYear());
        monthLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2937;");
        monthLabel.setFont(Font.font("Poppins", 28 * getScale()));

        Button next = createNavButton("Next", () -> changeMonth(1));
        nav.getChildren().addAll(prev, monthLabel, next);

        GridPane dayHeaders = new GridPane();
        dayHeaders.setHgap(8);
        dayHeaders.setAlignment(Pos.CENTER);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: 600;");
            d.setMinWidth(50);
            d.setAlignment(Pos.CENTER);
            dayHeaders.add(d, i, 0);
        }

        GridPane grid = createCalendarGrid();
        card.getChildren().addAll(nav, dayHeaders, grid);

// === FULLY CUSTOMIZABLE CELL SIZE & SHAPE ===
        double minCellSize = 50;     // ← Try 50–75
        double sideSpace   = 600;    // ← Space for sidebar + side panel
        double cellGap     = 14;     // ← Gap between cells

        double availableWidth = scene.getWidth() - sideSpace;
        double cellWidth  = Math.max(minCellSize, (availableWidth / 7) - cellGap);
        double cellHeight = cellWidth * 0.6;  // ← 0.6–1.0 (flatter = lower)

// Apply to all cells
        for (Node node : grid.getChildren()) {
            if (node instanceof VBox cell) {
                cell.setMinSize(cellWidth, cellHeight);
                cell.setPrefSize(cellWidth, cellHeight);
                cell.setMaxSize(cellWidth, cellHeight);
            }
        }

        return card;
    }

    private GridPane createCalendarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int firstDayOffset = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 0, col = 0;
        LocalDate today = LocalDate.now();

        LocalDate prev = currentDate.minusMonths(1);
        int prevDays = YearMonth.from(prev).lengthOfMonth();
        for (int i = firstDayOffset - 1; i >= 0; i--) {
            LocalDate d = prev.withDayOfMonth(prevDays - i);
            grid.add(createDayCell(d, true, false), col++, row);
            if (col == 7) { col = 0; row++; }
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (col == 7) { col = 0; row++; }
            LocalDate d = yearMonth.atDay(day);
            boolean isToday = d.equals(today);
            grid.add(createDayCell(d, false, isToday), col++, row);
        }

        int remaining = 42 - grid.getChildren().size();
        LocalDate next = currentDate.plusMonths(1);
        for (int day = 1; day <= remaining; day++) {
            if (col == 7) { col = 0; row++; }
            LocalDate d = next.withDayOfMonth(day);
            grid.add(createDayCell(d, true, false), col++, row);
        }

        return grid;
    }

    private VBox createDayCell(LocalDate date, boolean isOtherMonth, boolean isToday) {
        List<Todo> dayTodos = getTodosForDate(date);
        List<Todo> pending = dayTodos.stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());

        boolean isSelected = date.equals(selectedDate);

        VBox cell = new VBox(6);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(10));

        String bg = isSelected ? "#f3e8ff" : isToday ? "#fce7f3" : pending.size() > 0 ? "#fed7aa" : "#ffffff";
        String border = isSelected ? "#c084fc" : isToday ? "#ec4899" : pending.size() > 0 ? "#fb923c" : "#e5e7eb";
        String opacity = isOtherMonth ? "0.4" : "1.0";

        cell.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 20;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-border-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
            -fx-opacity: %s;
            -fx-cursor: hand;
            """, bg, border, opacity));

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            Platform.runLater(() -> root.setCenter(buildMainContent()));
        });

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins", 13));
        dayLabel.setTextFill(isOtherMonth ? Color.web("#9ca3af") :
                isToday ? Color.web("#be185d") :
                        pending.size() > 0 ? Color.web("#ea580c") : Color.web("#374151"));
        dayLabel.setStyle("-fx-font-weight: bold;");

        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER);
        pending.stream().limit(3).forEach(t -> {
            Circle c = new Circle(3.5, switch (t.getPriority()) {
                case "high" -> Color.web("#ef4444");
                case "medium" -> Color.web("#f59e0b");
                case "low" -> Color.web("#10b981");
                default -> Color.GRAY;
            });
            dots.getChildren().add(c);
        });
        if (pending.size() > 3) {
            Label more = new Label("+" + (pending.size() - 3));
            more.setTextFill(Color.web("#6b7280"));
            more.setFont(Font.font(8));
            dots.getChildren().add(more);
        }

        cell.getChildren().addAll(dayLabel, dots);
        return cell;
    }

    private VBox createSidePanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(380);
        panel.setMaxWidth(380);
        panel.setMinWidth(380);

        VBox taskCard = createTaskCard();
        VBox statsCard = createStatsCard();

        panel.getChildren().addAll(taskCard, statsCard);
        return panel;
    }

    private VBox createTaskCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        double scale = getScale();
        Label title = new Label(selectedDate != null ?
                "Tasks for " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) :
                "Select a date");
        title.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2937;");
        title.setFont(Font.font("Poppins", 18 * scale));

        VBox list = new VBox(10);
        list.setMinHeight(200);
        List<Todo> selected = selectedDate != null ? getTodosForDate(selectedDate) : Collections.emptyList();

        if (selected.isEmpty()) {
            Label empty = new Label(selectedDate == null ? "Click a date to see tasks" : "No tasks today");
            empty.setTextFill(Color.web("#9ca3af"));
            empty.setFont(Font.font("Poppins", 14 * scale));
            list.getChildren().add(empty);
        } else {
            selected.forEach(t -> list.getChildren().add(createTodoItem(t)));
        }

        card.getChildren().addAll(title, list);
        return card;
    }

    private VBox createTodoItem(Todo todo) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(14));
        item.setBackground(new Background(new BackgroundFill(
                todo.isCompleted() ? Color.web("#ecfdf5") : Color.web("#ffffff"),
                new CornerRadii(22), Insets.EMPTY)));
        item.setStyle("-fx-border-radius: 22; -fx-border-width: 2; -fx-border-color: " +
                (todo.isCompleted() ? "#86efac" : "#e5e7eb") + ";");

        // === MAIN TEXT ===
        Label text = new Label(todo.getText());
        text.setTextFill(Color.web("#1f2937"));
        text.setFont(Font.font("Poppins", 14));
        if (todo.isCompleted()) {
            text.setStyle("-fx-strikethrough: true; -fx-opacity: 0.7;");
        }

        // === PRIORITY + COMPLETED BADGES ===
        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Priority Badge
        Label priority = new Label(todo.getPriority().toUpperCase());
        priority.setFont(Font.font("Poppins", 11));
        priority.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-text-fill: %s;
        -fx-padding: 4 10;
        -fx-background-radius: 16;
        """,
                todo.getPriority().equals("high") ? "#fecaca" :
                        todo.getPriority().equals("medium") ? "#fde68a" : "#bbf7d0",
                todo.getPriority().equals("high") ? "#991b1b" :
                        todo.getPriority().equals("medium") ? "#92400e" : "#166534"
        ));

        badges.getChildren().add(priority);

        // Completed Badge
        if (todo.isCompleted()) {
            Label completed = new Label("Completed");
            completed.setFont(Font.font("Poppins", 11));
            completed.setStyle("-fx-background-color: #bbf7d0; -fx-text-fill: #166534; -fx-padding: 4 10; -fx-background-radius: 16;");
            badges.getChildren().add(completed);
        }

        // === PRIORITY DOT (optional) ===
        Circle dot = new Circle(5, switch (todo.getPriority()) {
            case "high" -> Color.web("#ef4444");
            case "medium" -> Color.web("#f59e0b");
            case "low" -> Color.web("#10b981");
            default -> Color.GRAY;
        });

        HBox top = new HBox(10, dot, text);
        top.setAlignment(Pos.CENTER_LEFT);

        item.getChildren().addAll(top, badges);
        return item;
    }

    private VBox createStatsCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        double scale = getScale();
        Label title = new Label("Quick Stats");
        title.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2937;");
        title.setFont(Font.font("Poppins", 17 * scale));

        VBox stats = new VBox(10);
        stats.getChildren().addAll(
                statRow("Total Tasks", String.valueOf(todos.size()), "#dbeafe"),
                statRow("Pending", String.valueOf(todos.stream().filter(t -> !t.isCompleted()).count()), "#fed7aa"),
                statRow("Completed", String.valueOf(todos.stream().filter(Todo::isCompleted).count()), "#d1fae5"),
                statRow("High Priority", String.valueOf(todos.stream().filter(t -> "high".equals(t.getPriority()) && !t.isCompleted()).count()), "#fecaca")
        );

        card.getChildren().addAll(title, stats);
        return card;
    }

    private HBox statRow(String label, String value, String bg) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(label);
        l.setTextFill(Color.web("#6b7280"));
        l.setFont(Font.font("Poppins", 13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.setStyle("-fx-background-color: " + bg + "; -fx-padding: 5 10; -fx-background-radius: 16; -fx-text-fill: #1f2937;");
        v.setFont(Font.font("Poppins", 13));

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-radius: 50; -fx-padding: 8 18; -fx-background-color: #f3f4f6; -fx-font-family: 'Poppins';");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void changeMonth(int delta) {
        currentDate = currentDate.plusMonths(delta);
        root.setCenter(buildMainContent());
    }

    private String getMonthYear() {
        return currentDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
    }

    private List<Todo> getTodosForDate(LocalDate date) {
        return todos.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().equals(date.toString()))
                .collect(Collectors.toList());
    }

    private List<Todo> getSampleTodos() {
        return Arrays.asList(
                new Todo("1", "Review presentation slides", false, "high", "2025-09-04"),
                new Todo("2", "Call team meeting", true, "medium", "2025-09-03"),
                new Todo("3", "Submit expense reports", false, "high", "2025-09-05"),
                new Todo("4", "Plan weekend trip", false, "low", "2025-09-07"),
                new Todo("5", "Doctor appointment", false, "medium", "2025-09-10"),
                new Todo("6", "Grocery shopping", false, "low", "2025-09-12")
        );
    }

    private static class Todo {
        private final String id, text, priority, dueDate;
        private final boolean completed;

        Todo(String id, String text, boolean completed, String priority, String dueDate) {
            this.id = id; this.text = text; this.completed = completed;
            this.priority = priority; this.dueDate = dueDate;
        }

        public String getId() { return id; }
        public String getText() { return text; }
        public boolean isCompleted() { return completed; }
        public String getPriority() { return priority; }
        public String getDueDate() { return dueDate; }
    }
}