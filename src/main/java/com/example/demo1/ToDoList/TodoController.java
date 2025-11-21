// src/main/java/com/example/demo1/ToDoList/Database.java
package com.example.demo1.ToDoList;

import com.example.demo1.Database.DatabaseConnection;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoController {
    private Connection conn;
    private int CURRENT_USER_ID;
    private List<Todo> todos;

    public TodoController(int userId) {
        connectToDatabase();
        CURRENT_USER_ID = userId;
        this.todos = getTodos(CURRENT_USER_ID);
    }

    private void connectToDatabase() {
        try {
            conn = DatabaseConnection.getConnection();
            System.out.println("Connected to EvoraDB!");
        } catch (SQLException e) {
            System.out.println("DB Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Todo> getTodos(int userId) {
        List<Todo> todos = new ArrayList<>();
        if (!isConnected()) return todos;

        String sql = """
        SELECT task_id, description, priority, due_date, is_completed 
        FROM ToDoTasks 
        WHERE user_id = ? 
        ORDER BY sort_order ASC, task_id ASC
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                todos.add(new Todo(
                        String.valueOf(rs.getInt("task_id")),
                        rs.getString("description"),
                        rs.getBoolean("is_completed"),
                        rs.getString("priority").toLowerCase(),
                        rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate().toString() : null
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return todos;
    }

    public void updateTaskOrder(int userId, List<Todo> todos) {
        if (!isConnected()) return;

        String sql = "UPDATE ToDoTasks SET sort_order = ? WHERE task_id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < todos.size(); i++) {
                ps.setInt(1, i); // sort_order = position
                ps.setInt(2, Integer.parseInt(todos.get(i).getId()));
                ps.setInt(3, userId);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Task order saved to DB!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTodo(int userId, Todo todo) {
        if (!isConnected()) return;
        String sql = """
        INSERT INTO ToDoTasks (user_id, description, priority, due_date, is_completed, sort_order)
        VALUES (?, ?, ?, ?, ?, (SELECT COALESCE(MAX(sort_order), -1) + 1 FROM ToDoTasks WHERE user_id = ?))
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, todo.getText());
            ps.setString(3, todo.getPriority().substring(0,1).toUpperCase() + todo.getPriority().substring(1));
            ps.setDate(4, todo.getDueDate() != null ? Date.valueOf(todo.getDueDate()) : null);
            ps.setBoolean(5, todo.isCompleted());
            ps.setInt(6, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateTodo(Todo todo) {
        String sql = "UPDATE ToDoTasks SET is_completed = ?, completed_at = ? WHERE task_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, todo.isCompleted());
            ps.setTimestamp(2, todo.isCompleted() ? Timestamp.valueOf(java.time.LocalDateTime.now()) : null);
            ps.setInt(3, Integer.parseInt(todo.getId()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteTodo(String taskId) {
        String sql = "DELETE FROM ToDoTasks WHERE task_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(taskId));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public void incrementUserExperience(int experienceToAdd) {
        String sql = "UPDATE Users SET experience = experience + ? WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, experienceToAdd);
            ps.setInt(2, CURRENT_USER_ID);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Added " + experienceToAdd + " experience to user " + CURRENT_USER_ID);
            }
        } catch (SQLException e) {
            System.err.println("Error updating user experience: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getCurrentUserId() {
        return CURRENT_USER_ID;
    }

    public void refreshTodos() {
        this.todos = getTodos(CURRENT_USER_ID);
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