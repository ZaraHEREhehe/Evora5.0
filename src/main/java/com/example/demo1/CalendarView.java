// src/main/java/com/example/demo1/CalendarView.java
package com.example.demo1;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarView {

    private final Stage stage;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private final List<Todo> todos;

    public CalendarView(Stage stage) {
        this.stage = stage;
        this.currentDate = LocalDate.now();
        this.selectedDate = null;
        this.todos = getSampleTodos();
    }

//    public void show() {
//        BorderPane root = new BorderPane();
//        root.setStyle("-fx-background-color: #fdf7ff;");
//
//        // REUSE YOUR SIDEBAR
//        SidebarController sidebarController = new SidebarController();
//        sidebarController.setOnTabChange(tab -> {
//            if ("dashboard".equals(tab)) {
//                new Dashboard(stage).show();
//            }
//        });
//        Sidebar sidebar = new Sidebar(sidebarController, "Zara");
//
//        root.setLeft(sidebar);
//
//        VBox main = new VBox(24);
//        main.setPadding(new Insets(30));
//        main.setAlignment(Pos.TOP_CENTER);
//
//        Label title = new Label("Calendar");
//        title.setFont(Font.font("Poppins", 36));
//        title.setTextFill(Color.web("#5c5470"));
//
//        Label subtitle = new Label("See your tasks at a glance on the calendar!");
//        subtitle.setFont(Font.font("Poppins", 16));
//        subtitle.setTextFill(Color.web("#9189a5"));
//
//        VBox header = new VBox(8, title, subtitle);
//        header.setAlignment(Pos.CENTER);
//
//        VBox legend = createLegendCard();
//        HBox body = new HBox(24);
//        body.setAlignment(Pos.TOP_CENTER);
//
//        VBox calendarCard = createCalendarCard();
//        VBox taskPanel = createTaskPanel();
//
//        body.getChildren().addAll(calendarCard, taskPanel);
//        HBox.setHgrow(calendarCard, Priority.ALWAYS);
//
//        main.getChildren().addAll(header, legend, body);
//
//        ScrollPane scroll = new ScrollPane(main);
//        scroll.setFitToWidth(true);
//        scroll.setStyle("-fx-background-color: transparent;");
//
//        root.setCenter(scroll);
//
//        Scene scene = new Scene(root, 1400, 900);
//        scene.getStylesheets().add("https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap");
//        root.setStyle("-fx-font-family: 'Poppins', sans-serif;");
//
//        stage.setScene(scene);
//        stage.setTitle("Évora • Calendar");
//        stage.show();
//    }
public void show() {
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: #fdf7ff;");

    // === FIXED SIDEBAR SETUP ===
    SidebarController sidebarController = new SidebarController();
    sidebarController.setStage(stage);  // ← THIS LINE WAS MISSING!

    // ← BEST WAY: uses the helper
    // Or manually:
    // if ("dashboard".equals(tab)) {
    //     new Dashboard(stage).show();
    // }
    sidebarController.setOnTabChange(sidebarController::goTo);

    Sidebar sidebar = new Sidebar(sidebarController, "Zara");
    root.setLeft(sidebar);

    // === REST OF YOUR CODE (100% unchanged) ===
    VBox main = new VBox(24);
    main.setPadding(new Insets(30));
    main.setAlignment(Pos.TOP_CENTER);

    Label title = new Label("Calendar");
    title.setFont(Font.font("Poppins", 36));
    title.setTextFill(Color.web("#5c5470"));

    Label subtitle = new Label("See your tasks at a glance on the calendar!");
    subtitle.setFont(Font.font("Poppins", 16));
    subtitle.setTextFill(Color.web("#9189a5"));

    VBox header = new VBox(8, title, subtitle);
    header.setAlignment(Pos.CENTER);

    VBox legend = createLegendCard();
    HBox body = new HBox(24);
    body.setAlignment(Pos.TOP_CENTER);

    VBox calendarCard = createCalendarCard();
    VBox taskPanel = createTaskPanel();

    body.getChildren().addAll(calendarCard, taskPanel);
    HBox.setHgrow(calendarCard, Priority.ALWAYS);

    main.getChildren().addAll(header, legend, body);

    ScrollPane scroll = new ScrollPane(main);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");

    root.setCenter(scroll);

    Scene scene = new Scene(root, 1400, 900);
    scene.getStylesheets().add("https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap");
    root.setStyle("-fx-font-family: 'Poppins', sans-serif;");

    stage.setScene(scene);
    stage.setTitle("Évora • Calendar");
    stage.show();
}



    private VBox createLegendCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
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
        HBox item = new HBox(12);
        Circle dot = new Circle(6, Color.web(color));
        Label label = new Label(text);
        label.setTextFill(Color.web("#4b5563"));
        item.getChildren().addAll(dot, label);
        return item;
    }

    private VBox createCalendarCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER);

        Button prev = createNavButton("Previous", () -> changeMonth(-1));
        Label monthLabel = new Label(getMonthYear());
        monthLabel.setFont(Font.font("Poppins", 24));
        monthLabel.setTextFill(Color.web("#1f2937"));
        Button next = createNavButton("Next", () -> changeMonth(1));

        header.getChildren().addAll(prev, monthLabel, next);

        GridPane dayHeaders = new GridPane();
        dayHeaders.setHgap(8);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setTextFill(Color.web("#6b7280"));
            dayHeaders.add(d, i, 0);
        }

        GridPane grid = createCalendarGrid();
        card.getChildren().addAll(header, dayHeaders, grid);
        return card;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-radius: 50; -fx-padding: 10 20; -fx-background-color: #f3f4f6;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private GridPane createCalendarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 0, col = 0;
        LocalDate today = LocalDate.now();

        LocalDate prev = currentDate.minusMonths(1);
        int prevDays = YearMonth.from(prev).lengthOfMonth();
        for (int i = dayOfWeek - 1; i >= 0; i--) {
            LocalDate d = prev.withDayOfMonth(prevDays - i);
            grid.add(createDayCell(d, true), col++, row);
            if (col == 7) { col = 0; row++; }
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate d = yearMonth.atDay(day);
            grid.add(createDayCell(d, false), col++, row);
            if (col == 7) { col = 0; row++; }
        }

        int remaining = (7 - col) % 7;
        LocalDate next = currentDate.plusMonths(1);
        for (int day = 1; day <= remaining; day++) {
            LocalDate d = next.withDayOfMonth(day);
            grid.add(createDayCell(d, true), col++, row);
        }

        return grid;
    }

    private VBox createDayCell(LocalDate date, boolean isOtherMonth) {
        List<Todo> dayTodos = getTodosForDate(date);
        List<Todo> pending = dayTodos.stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);

        VBox cell = new VBox(8);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(12));
        cell.setMinHeight(80);

        String bg = isSelected ? "#f3e8ff" : isToday ? "#fce7f3" : pending.size() > 0 ? "#fed7aa" : "#ffffff";
        String border = isSelected ? "#c084fc" : isToday ? "#ec4899" : pending.size() > 0 ? "#fb923c" : "#e5e7eb";

        cell.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 20;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-border-radius: 20;
            -fx-cursor: hand;
            """, bg, border));

        if (!isOtherMonth) {
            cell.setOnMouseClicked(e -> {
                selectedDate = date;
                show();
            });
        }

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins", 14));
        dayLabel.setTextFill(isOtherMonth ? Color.web("#9ca3af") :
                isToday ? Color.web("#be185d") :
                        pending.size() > 0 ? Color.web("#ea580c") : Color.web("#374151"));

        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER);
        pending.stream().limit(3).forEach(t -> {
            Circle c = new Circle(4, switch (t.getPriority()) {
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
            dots.getChildren().add(more);
        }

        cell.getChildren().addAll(dayLabel, dots);
        return cell;
    }

    private VBox createTaskPanel() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(400);

        VBox taskCard = new VBox(16);
        taskCard.setPadding(new Insets(24));
        taskCard.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        taskCard.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        Label title = new Label(selectedDate != null ?
                "Tasks for " + selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) :
                "Select a date");
        title.setFont(Font.font("Poppins", 20));
        title.setTextFill(Color.web("#1f2937"));

        VBox list = new VBox(12);
        List<Todo> selected = selectedDate != null ? getTodosForDate(selectedDate) : Collections.emptyList();

        if (selected.isEmpty()) {
            Label empty = new Label(selectedDate == null ? "Click on a date to see tasks" : "No tasks scheduled");
            empty.setTextFill(Color.web("#9ca3af"));
            list.getChildren().add(empty);
        } else {
            selected.forEach(t -> list.getChildren().add(createTodoItem(t)));
        }

        taskCard.getChildren().addAll(title, list);
        VBox stats = createQuickStatsCard();
        panel.getChildren().addAll(taskCard, stats);
        return panel;
    }

    private VBox createTodoItem(Todo todo) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(16));
        item.setBackground(new Background(new BackgroundFill(
                todo.isCompleted() ? Color.web("#d1fae5") :
                        switch (todo.getPriority()) {
                            case "high" -> Color.web("#fee2e2");
                            case "medium" -> Color.web("#fef3c7");
                            default -> Color.web("#ecfdf5");
                        },
                new CornerRadii(20), Insets.EMPTY)));
        item.setStyle("-fx-border-color: " + (todo.isCompleted() ? "#86efac" :
                todo.getPriority().equals("high") ? "#fca5a5" :
                        todo.getPriority().equals("medium") ? "#fde047" : "#86efac") + "; -fx-border-width: 2; -fx-border-radius: 20;");

        HBox top = new HBox(12);
        Circle dot = new Circle(6, switch (todo.getPriority()) {
            case "high" -> Color.web("#ef4444");
            case "medium" -> Color.web("#f59e0b");
            case "low" -> Color.web("#10b981");
            default -> Color.GRAY;
        });
        Label text = new Label(todo.getText());
        text.setTextFill(Color.web("#1f2937"));
        if (todo.isCompleted()) text.setStyle("-fx-strikethrough: true;");

        top.getChildren().addAll(dot, text);

        HBox badges = new HBox(8);
        Label pri = new Label(todo.getPriority().toUpperCase());
        pri.setStyle("-fx-background-color: " + (todo.getPriority().equals("high") ? "#fecaca" :
                todo.getPriority().equals("medium") ? "#fde68a" : "#bbf7d0") + "; -fx-padding: 4 8; -fx-background-radius: 12;");
        pri.setTextFill(todo.getPriority().equals("high") ? Color.web("#991b1b") :
                todo.getPriority().equals("medium") ? Color.web("#92400e") : Color.web("#166534"));

        if (todo.isCompleted()) {
            Label done = new Label("Completed");
            done.setStyle("-fx-background-color: #bbf7d0; -fx-padding: 4 8; -fx-background-radius: 12; -fx-text-fill: #166534;");
            badges.getChildren().add(done);
        }
        badges.getChildren().add(pri);
        item.getChildren().addAll(top, badges);
        return item;
    }

    private VBox createQuickStatsCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        Label title = new Label("Quick Stats");
        title.setFont(Font.font("Poppins", 18));
        title.setTextFill(Color.web("#1f2937"));

        VBox stats = new VBox(12);
        stats.getChildren().addAll(
                statRow("Total Tasks", String.valueOf(todos.size()), "#dbeafe"),
                statRow("Pending", String.valueOf(todos.stream().filter(t -> !t.isCompleted()).count()), "#fed7aa"),
                statRow("Completed", String.valueOf(todos.stream().filter(Todo::isCompleted).count()), "#d1fae5"),
                statRow("High Priority", String.valueOf(todos.stream().filter(t -> "high".equals(t.getPriority()) && !t.isCompleted()).count()), "#fecaca")
        );

        card.getChildren().addAll(title, stats);
        return card;
    }

    // FIXED: SPACE_BETWEEN → Use HBox with Region spacer
    private HBox statRow(String label, String value, String bg) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(label);
        l.setTextFill(Color.web("#6b7280"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.setStyle("-fx-background-color: " + bg + "; -fx-padding: 6 12; -fx-background-radius: 20; -fx-text-fill: #1f2937;");

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private void changeMonth(int delta) {
        currentDate = currentDate.plusMonths(delta);
        show();
    }

    private String getMonthYear() {
        return currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
    }

    private List<Todo> getTodosForDate(LocalDate date) {
        return todos.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().equals(date.toString()))
                .collect(Collectors.toList());
    }

    private List<Todo> getSampleTodos() {
        return Arrays.asList(
                new Todo("1", "Review presentation slides", false, "high", "2025-11-04"),
                new Todo("2", "Call team meeting", true, "medium", "2025-11-03"),
                new Todo("3", "Submit expense reports", false, "high", "2025-11-05"),
                new Todo("4", "Plan weekend trip", false, "low", "2025-11-07"),
                new Todo("5", "Doctor appointment", false, "medium", "2025-11-10"),
                new Todo("6", "Grocery shopping", false, "low", "2025-11-12")
        );
    }

    private static class Todo {
        private final String id;
        private final String text;
        private final boolean completed;
        private final String priority;
        private final String dueDate;

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
        public String getPriority() { return priority; }
        public String getDueDate() { return dueDate; }
    }
}