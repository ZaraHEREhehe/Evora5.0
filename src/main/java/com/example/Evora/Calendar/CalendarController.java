// File: src/main/java/com/example/Evora/Calendar/CalendarController.java
package com.example.Evora.Calendar;

import com.example.Evora.Database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarController {
    private Connection conn;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private final List<Todo> todos;
    private final int CURRENT_USER_ID;
    private Runnable onContentChange;

    public CalendarController(int userId) {
        connectToDatabase();
        this.currentDate = LocalDate.now();
        this.selectedDate = null;
        this.CURRENT_USER_ID = userId;
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

    // Date navigation methods
    public void previousMonth() {
        currentDate = currentDate.minusMonths(1);
        notifyContentChange();
    }

    public void nextMonth() {
        currentDate = currentDate.plusMonths(1);
        notifyContentChange();
    }

    public void selectDate(LocalDate date) {
        selectedDate = date;
        notifyContentChange();
    }

    // Getters
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public String getMonthYear() {
        return currentDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
    }

    public List<Todo> getTodosForDate(LocalDate date) {
        return todos.stream()
                .filter(t -> t.getDueDate() != null &&
                        LocalDate.parse(t.getDueDate()).equals(date))
                .collect(Collectors.toList());
    }

    public List<Todo> getSelectedDateTodos() {
        return selectedDate != null ? getTodosForDate(selectedDate) : Collections.emptyList();
    }

    // Statistics methods
    public int getTotalTasks() {
        return todos.size();
    }

    public int getPendingTasks() {
        return (int) todos.stream().filter(t -> !t.isCompleted()).count();
    }

    public int getCompletedTasks() {
        return (int) todos.stream().filter(Todo::isCompleted).count();
    }

    public int getHighPriorityTasks() {
        return (int) todos.stream().filter(t -> "high".equals(t.getPriority()) && !t.isCompleted()).count();
    }

    private void notifyContentChange() {
        if (onContentChange != null) {
            onContentChange.run();
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

    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return conn;
    }

    // Inner Todo class
    public static class Todo {
        private final String id, text, priority, dueDate;
        private final boolean completed;

        public Todo(String id, String text, boolean completed, String priority, String dueDate) {
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