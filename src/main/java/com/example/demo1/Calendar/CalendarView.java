// src/main/java/com/example/demo1/calendar/CalendarView.java
package com.example.demo1.Calendar;

import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Sidebar.SidebarController;
import com.example.demo1.ToDoList.Database;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.Cursor;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarView {

    private Stage stage;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private final List<Todo> todos;
    private final Database db;
    private final int CURRENT_USER_ID = 1; // Change later with login system
    private BorderPane root;
    private Scene scene;
    private boolean isFirstShow = true;
    private double lastWidth = 1400;

    public CalendarView() {
        this.currentDate = LocalDate.now();
        this.selectedDate = null;
        this.db = new Database();
        this.todos = loadTodosFromDB();
    }

    // Keep the stage constructor for backward compatibility
    public CalendarView(Stage stage) {
        this(); // Call the no-arg constructor
        this.stage = stage;
    }

    public void show() {
        if (scene == null) {
            root = new BorderPane();
            root.setStyle("-fx-background-color: #fdf7ff;");

            // === SIDEBAR ===
            SidebarController sidebarController = new SidebarController();
            sidebarController.setStage(stage);
            sidebarController.setOnTabChange(tab -> {
                // Handle navigation directly or leave empty if Dashboard handles it
                System.out.println("Navigating to: " + tab);
            });

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

            // === RESPONSIVE REBUILD ON RESIZE ===
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
        VBox main = new VBox(24);
        main.setPadding(new Insets(40));
        main.setAlignment(Pos.TOP_CENTER);

        double scale = getScale();
        main.setStyle(String.format("-fx-font-size: %.2fpx;", 16 * scale));

        // === TITLE ===
        Label title = new Label("Calendar");
        title.setStyle("-fx-font-weight: 700; -fx-text-fill: #5c5470;");
        title.setFont(Font.font("Poppins", 36 * scale));

        Label subtitle = new Label("See your tasks at a glance on the calendar!");
        subtitle.setStyle("-fx-text-fill: #9189a5;");
        subtitle.setFont(Font.font("Poppins", 16 * scale));

        VBox header = new VBox(10, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // === LEGEND ===
        VBox legend = createResponsiveLegend();

        // === RESPONSIVE LAYOUT: Use StackPane with dynamic switching ===
        StackPane layoutContainer = new StackPane();
        layoutContainer.setAlignment(Pos.TOP_CENTER);

        VBox calendarCard = createCalendarCard();
        VBox sidePanel = createSidePanel();

        // Stacked layout (for mobile/small screens)
        VBox stackedLayout = new VBox(24, calendarCard, sidePanel);
        stackedLayout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(calendarCard, Priority.NEVER);

        // Side-by-side layout (for desktop/large screens)
        HBox sideBySideLayout = new HBox(32, calendarCard, sidePanel);
        sideBySideLayout.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(calendarCard, Priority.ALWAYS);
        sidePanel.setMaxWidth(380);

        // Switch layouts based on window width - matching TypeScript breakpoint
        scene.widthProperty().addListener((obs, old, width) -> {
            double currentWidth = width.doubleValue();
            if (currentWidth >= 1024) {
                layoutContainer.getChildren().setAll(sideBySideLayout);
                sidePanel.setMaxWidth(380);
            } else {
                layoutContainer.getChildren().setAll(stackedLayout);
                sidePanel.setMaxWidth(Double.MAX_VALUE);
            }
        });

        // Initial layout setup
        if (scene.getWidth() >= 1024) {
            layoutContainer.getChildren().setAll(sideBySideLayout);
        } else {
            layoutContainer.getChildren().setAll(stackedLayout);
        }

        main.getChildren().addAll(header, legend, layoutContainer);

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

    private VBox createResponsiveLegend() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));
        card.setAlignment(Pos.CENTER);

        FlowPane flow = new FlowPane(24, 12);
        flow.setAlignment(Pos.CENTER);
        flow.setHgap(24);
        flow.setVgap(12);
        flow.setPadding(new Insets(8));

        flow.getChildren().addAll(
                legendItem("High Priority", "#ef4444"),
                legendItem("Medium Priority", "#f59e0b"),
                legendItem("Low Priority", "#10b981"),
                legendItem("Today", "#ec4899"),
                legendItem("Task Due", "#93c5fd")
        );

        flow.maxWidthProperty().bind(scene.widthProperty().subtract(100));
        card.getChildren().add(flow);
        return card;
    }

    private HBox legendItem(String text, String color) {
        HBox item = new HBox(10);
        Circle dot = new Circle(6, Color.web(color));
        Label label = new Label(text);
        label.setTextFill(Color.web("#4b5563"));
        label.setFont(Font.font("Poppins", 13));
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

        // === CELL SIZE & SHAPE (UNCHANGED) ===
        double minCellSize = 50;
        double sideSpace   = 600;
        double cellGap     = 14;

        double availableWidth = scene.getWidth() - sideSpace;
        double cellWidth  = Math.max(minCellSize, (availableWidth / 7) - cellGap);
        double cellHeight = cellWidth * 0.6;

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
        boolean hasTasks = pending.size() > 0;

        VBox cell = new VBox(4);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(6, 8, 8, 8));
        cell.setCursor(Cursor.HAND);

        String bg = isSelected ? "#f3e8ff" :
                isToday ? "#fce7f3" :
                        hasTasks ? "#dbeafe" : "#ffffff";

        String border = isSelected ? "#c084fc" :
                isToday ? "#ec4899" :
                        hasTasks ? "#93c5fd" : "#e5e7eb";

        String opacity = isOtherMonth ? "0.4" : "1.0";

        cell.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 16;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-border-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);
            -fx-opacity: %s;
            -fx-cursor: hand;
            """, bg, border, opacity));

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            Platform.runLater(() -> root.setCenter(buildMainContent()));
        });

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins", 14));
        dayLabel.setTextFill(isOtherMonth ? Color.web("#9ca3af") :
                isToday ? Color.web("#be185d") :
                        hasTasks ? Color.web("#1e40af") : Color.web("#374151"));
        dayLabel.setStyle("-fx-font-weight: bold;");

        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER);
        pending.stream().limit(3).forEach(t -> {
            Circle c = new Circle(3, switch (t.getPriority()) {
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
            more.setFont(Font.font(9));
            more.setStyle("-fx-font-weight: bold;");
            dots.getChildren().add(more);
        }

        cell.getChildren().addAll(dayLabel, dots);
        return cell;
    }

    private VBox createSidePanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.TOP_CENTER);

        panel.prefWidthProperty().bind(
                Bindings.when(scene.widthProperty().lessThan(1024))
                        .then(scene.widthProperty().subtract(80))
                        .otherwise(380.0)
        );

        VBox taskCard = createTaskCard();
        VBox statsCard = createStatsCard();

        panel.getChildren().addAll(taskCard, statsCard);
        VBox.setVgrow(taskCard, Priority.NEVER);
        VBox.setVgrow(statsCard, Priority.NEVER);

        return panel;
    }

    private VBox createTaskCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setBackground(new Background(new BackgroundFill(Color.web("#ffffff", 0.7), new CornerRadii(30), Insets.EMPTY)));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        double scale = getScale();

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(24, 24);
        iconContainer.setPrefSize(24, 24);
        iconContainer.setMaxSize(24, 24);

        Group iconGroup = new Group();

        SVGPath calendarOutline = new SVGPath();
        calendarOutline.setContent("M8 2v4M16 2v4M3 10h18M5 2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z");
        calendarOutline.setStroke(Color.web("#6b7280"));
        calendarOutline.setStrokeWidth(2);
        calendarOutline.setFill(Color.TRANSPARENT);
        calendarOutline.setStrokeLineCap(StrokeLineCap.ROUND);
        calendarOutline.setStrokeLineJoin(StrokeLineJoin.ROUND);

        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M9 12.75L11.25 15 15 9.75");
        checkmark.setStroke(Color.web("#6b7280"));
        checkmark.setStrokeWidth(2);
        checkmark.setFill(Color.TRANSPARENT);
        checkmark.setStrokeLineCap(StrokeLineCap.ROUND);
        checkmark.setStrokeLineJoin(StrokeLineJoin.ROUND);
        checkmark.setTranslateX(2);
        checkmark.setTranslateY(2);

        iconGroup.getChildren().addAll(calendarOutline, checkmark);
        iconContainer.getChildren().add(iconGroup);

        Label title = new Label(selectedDate != null ?
                "Tasks for " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) :
                "Select a date");
        title.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2937;");
        title.setFont(Font.font("Poppins", 18 * scale));

        header.getChildren().addAll(iconContainer, title);

        VBox list = new VBox(10);
        list.setMinHeight(200);
        List<Todo> selected = selectedDate != null ? getTodosForDate(selectedDate) : Collections.emptyList();

        if (selected.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(20, 0, 20, 0));

            StackPane emptyIconContainer = new StackPane();
            emptyIconContainer.setMinSize(48, 48);
            emptyIconContainer.setPrefSize(48, 48);
            emptyIconContainer.setMaxSize(48, 48);
            emptyIconContainer.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;");

            SVGPath emptyIcon = new SVGPath();
            if (selectedDate == null) {
                emptyIcon.setContent("M8 2v4M16 2v4M3 10h18M5 2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z");
            } else {
                emptyIcon.setContent("M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z");
            }
            emptyIcon.setStroke(Color.web("#9ca3af"));
            emptyIcon.setStrokeWidth(2);
            emptyIcon.setFill(Color.TRANSPARENT);
            emptyIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            emptyIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);

            emptyIconContainer.getChildren().add(emptyIcon);

            Label emptyText = new Label(selectedDate == null ? "Click a date to see tasks" : "No tasks for this date");
            emptyText.setTextFill(Color.web("#9ca3af"));
            emptyText.setFont(Font.font("Poppins", 14 * scale));
            emptyText.setAlignment(Pos.CENTER);

            emptyState.getChildren().addAll(emptyIconContainer, emptyText);
            list.getChildren().add(emptyState);
        } else {
            selected.forEach(t -> list.getChildren().add(createTodoItem(t)));
        }

        card.getChildren().addAll(header, list);

        card.prefWidthProperty().bind(
                Bindings.when(scene.widthProperty().lessThan(1024))
                        .then(scene.widthProperty().subtract(100))
                        .otherwise(380.0)
        );

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

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5, switch (todo.getPriority()) {
            case "high" -> Color.web("#ef4444");
            case "medium" -> Color.web("#f59e0b");
            case "low" -> Color.web("#10b981");
            default -> Color.GRAY;
        });

        Label text = new Label(todo.getText());
        text.setTextFill(Color.web("#1f2937"));
        text.setFont(Font.font("Poppins", 14));
        if (todo.isCompleted()) {
            text.setStyle("-fx-strikethrough: true; -fx-opacity: 0.7;");
        }

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

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

        if (todo.isCompleted()) {
            Label completed = new Label("Completed");
            completed.setFont(Font.font("Poppins", 11));
            completed.setStyle("-fx-background-color: #bbf7d0; -fx-text-fill: #166534; -fx-padding: 4 10; -fx-background-radius: 16;");
            badges.getChildren().add(completed);
        }

        top.getChildren().addAll(dot, text);
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

    private Button createNavButton(String type, Runnable action) {
        Button btn = new Button();
        btn.setStyle("-fx-background-radius: 50; -fx-padding: 8 12; -fx-background-color: #f3f4f6; -fx-font-family: 'Poppins'; -fx-cursor: hand;");
        btn.setOnAction(e -> action.run());

        FontIcon icon = new FontIcon();
        icon.setIconColor(Color.web("#4b5563"));
        icon.setIconSize(16);

        if (type.equals("Previous")) {
            icon.setIconLiteral("fas-chevron-left");
        } else {
            icon.setIconLiteral("fas-chevron-right");
        }

        btn.setGraphic(icon);
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
                .filter(t -> t.getDueDate() != null &&
                        LocalDate.parse(t.getDueDate()).equals(date))
                .collect(Collectors.toList());
    }

    // === DATABASE LOADER (REPLACES getSampleTodos) ===
    private List<Todo> loadTodosFromDB() {
        List<Todo> todoList = new ArrayList<>();

        if (!db.isConnected()) {
            System.out.println("DB not connected → Using sample data");
            return getSampleTodos(); // fallback
        }

        String sql = """
            SELECT task_id, description, is_completed, priority, due_date 
            FROM ToDoTasks 
            WHERE user_id = ? AND due_date IS NOT NULL
            ORDER BY due_date
            """;

        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, CURRENT_USER_ID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                todoList.add(new Todo(
                        String.valueOf(rs.getInt("task_id")),
                        rs.getString("description"),
                        rs.getBoolean("is_completed"),
                        rs.getString("priority").toLowerCase(),
                        rs.getDate("due_date") != null ?
                                rs.getDate("due_date").toLocalDate().toString() : null
                ));
            }
            System.out.println("Loaded " + todoList.size() + " tasks from DB");
        } catch (SQLException e) {
            System.out.println("Failed to load tasks from DB");
            e.printStackTrace();
            return getSampleTodos(); // fallback on error
        }

        return todoList;
    }

    // === FALLBACK SAMPLE DATA (UNCHANGED) ===
    private List<Todo> getSampleTodos() {
        return Arrays.asList(
                new Todo("1", "Review presentation slides", false, "high", "2025-11-04"),
                new Todo("2", "Call team meeting", true, "medium", "2025-11-03"),
                new Todo("3", "Submit expense reports", false, "high", "2025-11-05"),
                new Todo("4", "Plan weekend trip", false, "low", "2025-11-04"),
                new Todo("5", "Doctor appointment", false, "medium", "2025-11-10"),
                new Todo("6", "Grocery shopping", false, "low", "2025-11-12")
        );
    }

    // === INNER CLASS (UNCHANGED) ===
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
    public Pane getContent() {
        // Return the main content pane
        return root; // or whatever your main container is
    }
}