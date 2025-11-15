package com.example.demo1.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        String url = DatabaseConfig.getUrl();
        String username = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();

        // For Windows Authentication (empty username/password)
        if (username.isEmpty() && password.isEmpty()) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, username, password);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Test connection method
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}