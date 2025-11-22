package com.example.demo1.Theme;

import com.example.demo1.Database.DatabaseConnection;
import java.sql.*;

public class ThemeService {

    public static String getUserTheme(int userId) {
        String sql = "SELECT theme FROM preferences WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("theme");
            } else {
                // Create default preferences if they don't exist
                createDefaultPreferences(userId);
                return "pastel";
            }
        } catch (SQLException e) {
            System.err.println("Error getting user theme: " + e.getMessage());
            e.printStackTrace();
            return "pastel"; // Default fallback
        }
    }

    public static void saveUserTheme(int userId, String themeName) {
        // MS SQL Server syntax - use MERGE (UPSERT)
        String sql = "MERGE preferences AS target " +
                "USING (SELECT ? AS user_id, ? AS theme) AS source " +
                "ON target.user_id = source.user_id " +
                "WHEN MATCHED THEN " +
                "    UPDATE SET theme = source.theme " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (user_id, theme) VALUES (source.user_id, source.theme);";

        System.out.println("💾 Saving theme '" + themeName + "' for user ID: " + userId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, themeName);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Theme saved successfully! Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            System.err.println("❌ Error saving user theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDefaultPreferences(int userId) {
        String sql = "INSERT INTO preferences (user_id, theme) VALUES (?, 'pastel')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating default preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }
}