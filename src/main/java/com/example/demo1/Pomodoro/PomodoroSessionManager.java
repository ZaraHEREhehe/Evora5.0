// PomodoroSessionManager.java
package com.example.demo1.Pomodoro;

import com.example.demo1.Database.DatabaseConnection;
import java.sql.*;

public class PomodoroSessionManager {
    private int userId;

    public PomodoroSessionManager(int userId) {
        this.userId = userId;
    }

    public int startSession(String presetName, int workDuration, int breakDuration) {
        String sql = "INSERT INTO PomodoroSessions (user_id, preset_name, work_duration, break_duration, status) VALUES (?, ?, ?, ?, 'Running')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, presetName);
            stmt.setInt(3, workDuration);
            stmt.setInt(4, breakDuration);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void completeSession(int sessionId) {
        String sql = "UPDATE PomodoroSessions SET status = 'Completed', end_time = GETDATE(), completed_cycles = 1 WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            stmt.executeUpdate();

            // Award experience
            awardExperience(100); //100 no matter what
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void abortSession(int sessionId) {
        String sql = "UPDATE PomodoroSessions SET status = 'Aborted', end_time = GETDATE() WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void awardExperience(int exp) {
        String sql = "UPDATE Users SET experience = experience + ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, exp);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}