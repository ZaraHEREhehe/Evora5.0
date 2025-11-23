// src/main/java/com/example/Evora/calendar/CalendarView.java
package com.example.Evora.Calendar;

import com.example.Evora.Database.DatabaseConnection;
import com.example.Evora.Theme.Theme;
import com.example.Evora.Theme.ThemeManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
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
    private Connection conn;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private final List<Todo> todos;
    private int CURRENT_USER_ID;
    private Runnable onContentChange;
    private DoubleProperty widthProperty = new SimpleDoubleProperty(1400);
    private ThemeManager themeManager;

    public CalendarView(int userId) {
        connectToDatabase();
        this.currentDate = LocalDate.now();
        this.selectedDate = null;
        this.CURRENT_USER_ID = userId;
        this.themeManager = ThemeManager.getInstance();
        this.todos = loadTodosFromDB();
    }

    private void connectToDatabase() {
        try {
            conn = DatabaseConnection.getConnection();
            System.out.println("Connected to EvoraDB from Calendar!");
        } catch (SQLException e) {
            System.out.println("DB Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setOnContentChange(Runnable callback) {
        this.onContentChange = callback;
    }

    public void setWidth(double width) {
        widthProperty.set(width);
    }

    public ScrollPane getContent() {
        return buildMainContent();
    }

    private ScrollPane buildMainContent() {
        VBox main = new VBox(24);
        main.setPadding(new Insets(40));
        main.setAlignment(Pos.TOP_CENTER);

        // Set background based on theme
        Theme currentTheme = themeManager.getCurrentTheme();
        main.setBackground(new Background(new BackgroundFill(
                Color.web(currentTheme.getBackgroundColor()),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        // Bind font scaling to width property
        main.styleProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(() ->
                                String.format("-fx-font-size: %.2fpx;", 16 * getScale()),
                        widthProperty
                )
        );

        // === TITLE ===
        Label title = new Label("Calendar");
        title.setStyle("-fx-font-weight: 700;");
        title.setTextFill(Color.web(currentTheme.getTextPrimary()));
        title.fontProperty().bind(
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                                Font.font("Poppins", 36 * getScale()),
                        widthProperty
                )
        );

        Label subtitle = new Label("See your tasks at a glance on the calendar!");
        subtitle.setTextFill(Color.web(currentTheme.getTextSecondary()));
        subtitle.fontProperty().bind(
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                                Font.font("Poppins", 16 * getScale()),
                        widthProperty
                )
        );

        VBox header = new VBox(10, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // === LEGEND ===
        VBox legend = createResponsiveLegend();

        // === RESPONSIVE LAYOUT CONTAINER ===
        StackPane layoutContainer = new StackPane();
        layoutContainer.setAlignment(Pos.TOP_CENTER);

        // Create the main components
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

        // Responsive binding for side panel width
        sidePanel.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(() ->
                                widthProperty.get() >= 1024 ? 380.0 : Double.MAX_VALUE,
                        widthProperty
                )
        );

        // Responsive layout switching
        layoutContainer.getChildren().add(sideBySideLayout);
        layoutContainer.getChildren().add(stackedLayout);

        // Show/hide layouts based on screen width
        sideBySideLayout.visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(() ->
                                widthProperty.get() >= 1024,
                        widthProperty
                )
        );
        stackedLayout.visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(() ->
                                widthProperty.get() < 1024,
                        widthProperty
                )
        );

        main.getChildren().addAll(header, legend, layoutContainer);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scroll;
    }

    private double getScale() {
        return Math.max(0.8, Math.min(1.4, widthProperty.get() / 1400.0));
    }

    private VBox createResponsiveLegend() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));

        Theme currentTheme = themeManager.getCurrentTheme();
        card.setBackground(new Background(new BackgroundFill(
                Color.web(currentTheme.getCardColor()),
                new CornerRadii(30),
                Insets.EMPTY
        )));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));
        card.setAlignment(Pos.CENTER);

        FlowPane flow = new FlowPane(24, 12);
        flow.setAlignment(Pos.CENTER);
        flow.setHgap(24);
        flow.setVgap(12);
        flow.setPadding(new Insets(8));

        // Use theme-appropriate colors for legend
        String highPriorityColor = getPriorityColor("high");
        String mediumPriorityColor = getPriorityColor("medium");
        String lowPriorityColor = getPriorityColor("low");
        String todayColor = getTodayColor();
        String taskDueColor = getTaskDueColor();

        flow.getChildren().addAll(
                legendItem("High Priority", highPriorityColor),
                legendItem("Medium Priority", mediumPriorityColor),
                legendItem("Low Priority", lowPriorityColor),
                legendItem("Today", todayColor),
                legendItem("Task Due", taskDueColor)
        );

        card.getChildren().add(flow);
        return card;
    }

    private HBox legendItem(String text, String color) {
        HBox item = new HBox(10);
        Circle dot = new Circle(6, Color.web(color));
        Label label = new Label(text);
        label.setTextFill(Color.web(themeManager.getCurrentTheme().getTextPrimary()));
        label.setFont(Font.font("Poppins", 13));
        item.getChildren().addAll(dot, label);
        return item;
    }

    private VBox createCalendarCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));

        Theme currentTheme = themeManager.getCurrentTheme();
        card.setBackground(new Background(new BackgroundFill(
                Color.web(currentTheme.getCardColor()),
                new CornerRadii(32),
                Insets.EMPTY
        )));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER);

        Button prev = createNavButton("Previous", () -> changeMonth(-1));

        Label monthLabel = new Label(getMonthYear());
        monthLabel.setStyle("-fx-font-weight: 600;");
        monthLabel.setTextFill(Color.web(currentTheme.getTextPrimary()));
        monthLabel.fontProperty().bind(
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                                Font.font("Poppins", 28 * getScale()),
                        widthProperty
                )
        );

        Button next = createNavButton("Next", () -> changeMonth(1));
        nav.getChildren().addAll(prev, monthLabel, next);

        GridPane dayHeaders = new GridPane();
        dayHeaders.setHgap(8);
        dayHeaders.setAlignment(Pos.CENTER);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setTextFill(Color.web(currentTheme.getTextSecondary()));
            d.setStyle("-fx-font-weight: 600;");
            d.setMinWidth(50);
            d.setAlignment(Pos.CENTER);
            dayHeaders.add(d, i, 0);
        }

        GridPane grid = createCalendarGrid();
        card.getChildren().addAll(nav, dayHeaders, grid);

        // === DYNAMIC CELL SIZING WITH BINDING ===
        double minCellSize = 50;
        double sideSpace = 600;
        double cellGap = 14;

        // Calculate cell size based on current width
        grid.getChildren().forEach(node -> {
            if (node instanceof VBox cell) {
                cell.minWidthProperty().bind(
                        javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                            double availableWidth = widthProperty.get() - sideSpace;
                            return Math.max(minCellSize, (availableWidth / 7) - cellGap);
                        }, widthProperty)
                );
                cell.minHeightProperty().bind(
                        javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                            double availableWidth = widthProperty.get() - sideSpace;
                            double cellWidth = Math.max(minCellSize, (availableWidth / 7) - cellGap);
                            return cellWidth * 0.6;
                        }, widthProperty)
                );
                cell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                cell.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            }
        });

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

        // Previous month days
        LocalDate prev = currentDate.minusMonths(1);
        int prevDays = YearMonth.from(prev).lengthOfMonth();
        for (int i = firstDayOffset - 1; i >= 0; i--) {
            LocalDate d = prev.withDayOfMonth(prevDays - i);
            grid.add(createDayCell(d, true, false), col++, row);
            if (col == 7) { col = 0; row++; }
        }

        // Current month days
        for (int day = 1; day <= daysInMonth; day++) {
            if (col == 7) { col = 0; row++; }
            LocalDate d = yearMonth.atDay(day);
            boolean isToday = d.equals(today);
            grid.add(createDayCell(d, false, isToday), col++, row);
        }

        // Next month days
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

        // Use theme-appropriate colors
        String bg = getDayCellBackgroundColor(isSelected, isToday, hasTasks);
        String border = getDayCellBorderColor(isSelected, isToday, hasTasks);
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
            if (onContentChange != null) {
                onContentChange.run();
            }
        });

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins", 14));

        // Use theme-appropriate text colors
        Color textColor = getDayCellTextColor(isOtherMonth, isToday, hasTasks);
        dayLabel.setTextFill(textColor);
        dayLabel.setStyle("-fx-font-weight: bold;");

        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER);
        pending.stream().limit(3).forEach(t -> {
            Circle c = new Circle(3, Color.web(getPriorityColor(t.getPriority())));
            dots.getChildren().add(c);
        });
        if (pending.size() > 3) {
            Label more = new Label("+" + (pending.size() - 3));
            more.setTextFill(Color.web(themeManager.getCurrentTheme().getTextSecondary()));
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

        // Responsive width binding
        panel.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(() ->
                                widthProperty.get() < 1024 ? widthProperty.get() - 80 : 380.0,
                        widthProperty
                )
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

        Theme currentTheme = themeManager.getCurrentTheme();
        card.setBackground(new Background(new BackgroundFill(
                Color.web(currentTheme.getCardColor()),
                new CornerRadii(30),
                Insets.EMPTY
        )));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(24, 24);
        iconContainer.setPrefSize(24, 24);
        iconContainer.setMaxSize(24, 24);

        Group iconGroup = new Group();

        SVGPath calendarOutline = new SVGPath();
        calendarOutline.setContent("M8 2v4M16 2v4M3 10h18M5 2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z");
        calendarOutline.setStroke(Color.web(currentTheme.getTextSecondary()));
        calendarOutline.setStrokeWidth(2);
        calendarOutline.setFill(Color.TRANSPARENT);
        calendarOutline.setStrokeLineCap(StrokeLineCap.ROUND);
        calendarOutline.setStrokeLineJoin(StrokeLineJoin.ROUND);

        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M9 12.75L11.25 15 15 9.75");
        checkmark.setStroke(Color.web(currentTheme.getTextSecondary()));
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
        title.setStyle("-fx-font-weight: 600;");
        title.setTextFill(Color.web(currentTheme.getTextPrimary()));
        title.fontProperty().bind(
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                                Font.font("Poppins", 18 * getScale()),
                        widthProperty
                )
        );

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
            emptyIconContainer.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 12;",
                    currentTheme.getBackgroundColor()));

            SVGPath emptyIcon = new SVGPath();
            if (selectedDate == null) {
                emptyIcon.setContent("M8 2v4M16 2v4M3 10h18M5 2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z");
            } else {
                emptyIcon.setContent("M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z");
            }
            emptyIcon.setStroke(Color.web(currentTheme.getTextSecondary()));
            emptyIcon.setStrokeWidth(2);
            emptyIcon.setFill(Color.TRANSPARENT);
            emptyIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            emptyIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);

            emptyIconContainer.getChildren().add(emptyIcon);

            Label emptyText = new Label(selectedDate == null ? "Click a date to see tasks" : "No tasks for this date");
            emptyText.setTextFill(Color.web(currentTheme.getTextSecondary()));
            emptyText.fontProperty().bind(
                    javafx.beans.binding.Bindings.createObjectBinding(() ->
                                    Font.font("Poppins", 14 * getScale()),
                            widthProperty
                    )
            );
            emptyText.setAlignment(Pos.CENTER);

            emptyState.getChildren().addAll(emptyIconContainer, emptyText);
            list.getChildren().add(emptyState);
        } else {
            selected.forEach(t -> list.getChildren().add(createTodoItem(t)));
        }

        card.getChildren().addAll(header, list);

        // Responsive card width
        card.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(() ->
                                widthProperty.get() < 1024 ? widthProperty.get() - 100 : 380.0,
                        widthProperty
                )
        );

        return card;
    }

    private VBox createTodoItem(Todo todo) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(14));

        Theme currentTheme = themeManager.getCurrentTheme();
        String bgColor = todo.isCompleted() ? getCompletedTaskColor() : currentTheme.getCardColor();
        String borderColor = todo.isCompleted() ? getCompletedTaskBorderColor() : currentTheme.getTextSecondary();

        item.setBackground(new Background(new BackgroundFill(
                Color.web(bgColor),
                new CornerRadii(22),
                Insets.EMPTY
        )));
        item.setStyle("-fx-border-radius: 22; -fx-border-width: 2; -fx-border-color: " + borderColor + ";");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5, Color.web(getPriorityColor(todo.getPriority())));

        Label text = new Label(todo.getText());
        text.setTextFill(Color.web(currentTheme.getTextPrimary()));
        text.setFont(Font.font("Poppins", 14));
        if (todo.isCompleted()) {
            text.setStyle("-fx-strikethrough: true; -fx-opacity: 0.7;");
        }

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        Label priority = new Label(todo.getPriority().toUpperCase());
        priority.setFont(Font.font("Poppins", 11));

        String[] priorityColors = getPriorityBadgeColors(todo.getPriority());
        priority.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-text-fill: %s;
        -fx-padding: 4 10;
        -fx-background-radius: 16;
        """, priorityColors[0], priorityColors[1]));

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

        Theme currentTheme = themeManager.getCurrentTheme();
        card.setBackground(new Background(new BackgroundFill(
                Color.web(currentTheme.getCardColor()),
                new CornerRadii(30),
                Insets.EMPTY
        )));
        card.setEffect(new DropShadow(20, Color.gray(0, 0.15)));

        Label title = new Label("Quick Stats");
        title.setStyle("-fx-font-weight: 600;");
        title.setTextFill(Color.web(currentTheme.getTextPrimary()));
        title.fontProperty().bind(
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                                Font.font("Poppins", 17 * getScale()),
                        widthProperty
                )
        );

        VBox stats = new VBox(10);
        stats.getChildren().addAll(
                statRow("Total Tasks", String.valueOf(todos.size()), currentTheme.getStatCardColor1()),
                statRow("Pending", String.valueOf(todos.stream().filter(t -> !t.isCompleted()).count()), currentTheme.getStatCardColor2()),
                statRow("Completed", String.valueOf(todos.stream().filter(Todo::isCompleted).count()), currentTheme.getStatCardColor3()),
                statRow("High Priority", String.valueOf(todos.stream().filter(t -> "high".equals(t.getPriority()) && !t.isCompleted()).count()), currentTheme.getStatCardColor4())
        );

        card.getChildren().addAll(title, stats);
        return card;
    }

    private HBox statRow(String label, String value, String bg) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(label);
        l.setTextFill(Color.web(themeManager.getCurrentTheme().getTextSecondary()));
        l.setFont(Font.font("Poppins", 13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.setStyle("-fx-background-color: " + bg + "; -fx-padding: 5 10; -fx-background-radius: 16;");
        v.setTextFill(Color.web(themeManager.getCurrentTheme().getTextPrimary()));
        v.setFont(Font.font("Poppins", 13));

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private Button createNavButton(String type, Runnable action) {
        Button btn = new Button();
        Theme currentTheme = themeManager.getCurrentTheme();

        btn.setStyle(String.format("-fx-background-radius: 50; -fx-padding: 8 12; -fx-background-color: %s; -fx-font-family: 'Poppins'; -fx-cursor: hand;",
                currentTheme.getButtonColor()));
        btn.setOnAction(e -> action.run());

        FontIcon icon = new FontIcon();
        icon.setIconColor(Color.web(currentTheme.getTextPrimary()));
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
        if (onContentChange != null) {
            onContentChange.run();
        }
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

    // Theme-based color methods
    private String getPriorityColor(String priority) {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            // Galaxy theme colors
            switch (priority) {
                case "high": return "#ef4444";    // Keep original for visibility
                case "medium": return "#f59e0b";  // Keep original for visibility
                case "low": return "#10b981";     // Keep original for visibility
                default: return "#9ca3af";
            }
        } else {
            // Pastel theme colors (original)
            switch (priority) {
                case "high": return "#ef4444";
                case "medium": return "#f59e0b";
                case "low": return "#10b981";
                default: return "#9ca3af";
            }
        }
    }

    private String[] getPriorityBadgeColors(String priority) {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            // Galaxy theme badge colors
            switch (priority) {
                case "high": return new String[]{"#fecaca", "#991b1b"};
                case "medium": return new String[]{"#fde68a", "#92400e"};
                case "low": return new String[]{"#bbf7d0", "#166534"};
                default: return new String[]{"#d1d5db", "#374151"};
            }
        } else {
            // Pastel theme badge colors (original)
            switch (priority) {
                case "high": return new String[]{"#fecaca", "#991b1b"};
                case "medium": return new String[]{"#fde68a", "#92400e"};
                case "low": return new String[]{"#bbf7d0", "#166534"};
                default: return new String[]{"#d1d5db", "#374151"};
            }
        }
    }

    private String getTodayColor() {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            return "#ec4899"; // Keep original pink for visibility
        } else {
            return "#ec4899"; // Original
        }
    }

    private String getTaskDueColor() {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            return "#93c5fd"; // Keep original blue for visibility
        } else {
            return "#93c5fd"; // Original
        }
    }

    private String getDayCellBackgroundColor(boolean isSelected, boolean isToday, boolean hasTasks) {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            // Galaxy theme backgrounds
            if (isSelected) return "#4a5568";     // Darker selection
            if (isToday) return "#2d3748";        // Dark today
            if (hasTasks) return "#3182ce";       // Blue for tasks
            return "#2d3748";                     // Default dark
        } else {
            // Pastel theme backgrounds (original)
            if (isSelected) return "#f3e8ff";
            if (isToday) return "#fce7f3";
            if (hasTasks) return "#dbeafe";
            return "#ffffff";
        }
    }

    private String getDayCellBorderColor(boolean isSelected, boolean isToday, boolean hasTasks) {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            // Galaxy theme borders
            if (isSelected) return "#c084fc";
            if (isToday) return "#ec4899";
            if (hasTasks) return "#63b3ed";
            return "#4a5568";
        } else {
            // Pastel theme borders (original)
            if (isSelected) return "#c084fc";
            if (isToday) return "#ec4899";
            if (hasTasks) return "#93c5fd";
            return "#e5e7eb";
        }
    }

    private Color getDayCellTextColor(boolean isOtherMonth, boolean isToday, boolean hasTasks) {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            // Galaxy theme text colors
            if (isOtherMonth) return Color.web("#718096");
            if (isToday) return Color.web("#fbb6ce");
            if (hasTasks) return Color.web("#90cdf4");
            return Color.web("#e2e8f0");
        } else {
            // Pastel theme text colors (original)
            if (isOtherMonth) return Color.web("#9ca3af");
            if (isToday) return Color.web("#be185d");
            if (hasTasks) return Color.web("#1e40af");
            return Color.web("#374151");
        }
    }

    private String getCompletedTaskColor() {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            return "#2d3748"; // Dark completed task
        } else {
            return "#ecfdf5"; // Original light completed task
        }
    }

    private String getCompletedTaskBorderColor() {
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme instanceof com.example.Evora.Theme.GalaxyTheme) {
            return "#4a5568"; // Dark border
        } else {
            return "#86efac"; // Original light border
        }
    }

    private List<Todo> loadTodosFromDB() {
        List<Todo> todoList = new ArrayList<>();

        if (!isConnected()) {
            System.out.println("DB not connected → Using sample data");
            return getSampleTodos();
        }

        String sql = """
            SELECT task_id, description, is_completed, priority, due_date 
            FROM ToDoTasks 
            WHERE user_id = ? AND due_date IS NOT NULL
            ORDER BY due_date
            """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
            return getSampleTodos();
        }

        return todoList;
    }

    // === FALLBACK SAMPLE DATA ===
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

    // === INNER CLASS ===
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

    // REQUIRED FOR CalendarView
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // REQUIRED FOR CalendarView
    public Connection getConnection() {
        return conn;
    }
}