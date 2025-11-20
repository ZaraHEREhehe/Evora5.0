package com.example.demo1.Pomodoro;

import java.time.Duration;
import java.time.LocalDateTime;
import com.example.demo1.Sidebar.Sidebar;
import com.example.demo1.Database.DatabaseConnection;
import java.sql.*;

public class PomodoroSessionManager {
    private int userId;
    private Sidebar sidebar;

    public PomodoroSessionManager(int userId) {
        this.userId = userId;
    }
    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    // Data class for active sessions
    public static class ActiveSessionData {
        public int sessionId;
        public String status;
        public int workDuration;
        public int breakDuration;
        public LocalDateTime startTime;
        public LocalDateTime lastPauseTime;

        public ActiveSessionData(int sessionId, String status, int workDuration, int breakDuration,
                                 LocalDateTime startTime, LocalDateTime lastPauseTime) {
            this.sessionId = sessionId;
            this.status = status;
            this.workDuration = workDuration;
            this.breakDuration = breakDuration;
            this.startTime = startTime;
            this.lastPauseTime = lastPauseTime;
        }
    }

    // Get active session (only Running or Paused)
    public ActiveSessionData getActiveSession(int userId) {
        String sql = "SELECT session_id, status, work_duration, break_duration, start_time, last_pause_time " +
                "FROM PomodoroSessions WHERE user_id = ? AND status IN ('Running', 'Paused') " +
                "ORDER BY session_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ActiveSessionData(
                        rs.getInt("session_id"),
                        rs.getString("status"),
                        rs.getInt("work_duration"),
                        rs.getInt("break_duration"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("last_pause_time") != null ?
                                rs.getTimestamp("last_pause_time").toLocalDateTime() : null
                );
            }
        } catch (SQLException e) {
            System.out.println("ERROR in getActiveSession: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Calculate elapsed seconds
    public long getElapsedSeconds(int sessionId) {
        String sql = "SELECT start_time, last_pause_time, status FROM PomodoroSessions WHERE session_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                // If session is paused, use last_pause_time as end point
                if ("Paused".equals(rs.getString("status")) && rs.getTimestamp("last_pause_time") != null) {
                    LocalDateTime pauseTime = rs.getTimestamp("last_pause_time").toLocalDateTime();
                    return Duration.between(startTime, pauseTime).getSeconds();
                }

                // For running sessions, calculate up to now
                return Duration.between(startTime, now).getSeconds();
            }
        } catch (SQLException e) {
            System.out.println("ERROR in getElapsedSeconds: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int startSession(String presetName, int workDuration, int breakDuration) {
        String sql = "INSERT INTO PomodoroSessions (user_id, preset_name, work_duration, break_duration, start_time, status) VALUES (?, ?, ?, ?, GETDATE(), 'Running')";
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
            awardExperience(100);
            if (sidebar != null)
                sidebar.refreshExperienceFromDatabase(userId);
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

    public void pauseSession(int sessionId) {
        String sql = "UPDATE PomodoroSessions SET status = 'Paused', last_pause_time = GETDATE() WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Database: Session " + sessionId + " paused");
            } else {
                System.out.println("Database: No session found to pause with ID: " + sessionId);
            }
        } catch (SQLException e) {
            System.out.println("Database ERROR in pauseSession: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resumeSession(int sessionId) {
        String sql = "UPDATE PomodoroSessions SET status = 'Running', last_pause_time = NULL WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Database: Session " + sessionId + " resumed");
            } else {
                System.out.println("Database: No session found to resume with ID: " + sessionId);
            }
        } catch (SQLException e) {
            System.out.println("Database ERROR in resumeSession: " + e.getMessage());
            e.printStackTrace();
        }
    }
}