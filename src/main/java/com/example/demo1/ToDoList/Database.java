// src/main/java/com/example/demo1/ToDoList/Database.java
package com.example.demo1.ToDoList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL =
            "jdbc:sqlserver://AABIA\\SQLEXPRESS:1433;" +
                    "databaseName=EvoraDB;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    private static final String USER = "aabia";
    private static final String PASS = "12345678";

    private Connection conn;

    public Database() {
        connect();
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to EvoraDB!");
        } catch (SQLException e) {
            System.out.println("DB Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TodoView.Todo> getTodos(int userId) {
        List<TodoView.Todo> todos = new ArrayList<>();
        String sql = "SELECT task_id, description, priority, due_date, is_completed FROM ToDoTasks WHERE user_id = ? ORDER BY is_completed ASC, due_date ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                todos.add(new TodoView.Todo(
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

    public void addTodo(int userId, TodoView.Todo todo) {
        String sql = "INSERT INTO ToDoTasks (user_id, description, priority, due_date, is_completed) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, todo.getText());
            ps.setString(3, todo.getPriority().substring(0, 1).toUpperCase() + todo.getPriority().substring(1));
            ps.setString(4, todo.getDueDate());
            ps.setBoolean(5, todo.isCompleted());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateTodo(TodoView.Todo todo) {
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