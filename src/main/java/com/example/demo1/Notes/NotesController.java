package com.example.demo1.Notes;

import com.example.demo1.Database.DatabaseConnection;
import com.example.demo1.Sidebar.Sidebar;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotesController {
    private int currentUserId;
    private Sidebar sidebar;

    public NotesController(int userId) {
        this.currentUserId = userId;
        System.out.println("Notes module loaded for user: " + userId);
    }
    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    // Create - Add new note
    public int addNote(String content, int colorId, double positionX, double positionY) {
        String sql = "INSERT INTO StickyNotes (user_id, content, color_id, position_x, position_y) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, content);
            stmt.setInt(3, colorId);
            stmt.setDouble(4, positionX);
            stmt.setDouble(5, positionY);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int noteId = generatedKeys.getInt(1);

                        // Increment user experience by 50
                        incrementUserExperience(50);
                        if (sidebar != null)
                            sidebar.refreshExperienceFromDatabase(currentUserId);

                        return noteId; // Return the new note_id
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding note: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Error
    }

    // Method to increment user experience
    private void incrementUserExperience(int experienceToAdd) {
        String sql = "UPDATE Users SET experience = experience + ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, experienceToAdd);
            stmt.setInt(2, currentUserId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Added " + experienceToAdd + " experience to user " + currentUserId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating user experience: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Read - Get all notes for current user
    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT note_id, content, color_id, position_x, position_y, created_at " +
                "FROM StickyNotes WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Note note = new Note(
                            rs.getInt("note_id"),
                            rs.getString("content"),
                            rs.getInt("color_id"),
                            rs.getDouble("position_x"),
                            rs.getDouble("position_y"),
                            rs.getTimestamp("created_at")
                    );
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notes: " + e.getMessage());
            e.printStackTrace();
        }
        return notes;
    }

    // Update - Update note content, color, and position
    public boolean updateNote(int noteId, String content, int colorId, double positionX, double positionY) {
        String sql = "UPDATE StickyNotes SET content = ?, color_id = ?, position_x = ?, position_y = ? " +
                "WHERE note_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            stmt.setInt(2, colorId);
            stmt.setDouble(3, positionX);
            stmt.setDouble(4, positionY);
            stmt.setInt(5, noteId);
            stmt.setInt(6, currentUserId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating note: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Update just position (for dragging)
    public boolean updateNotePosition(int noteId, double positionX, double positionY) {
        String sql = "UPDATE StickyNotes SET position_x = ?, position_y = ? WHERE note_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, positionX);
            stmt.setDouble(2, positionY);
            stmt.setInt(3, noteId);
            stmt.setInt(4, currentUserId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating note position: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Delete - Remove note
    public boolean deleteNote(int noteId) {
        String sql = "DELETE FROM StickyNotes WHERE note_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            stmt.setInt(2, currentUserId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting note: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get color hex by color_id
    public String getColorHex(int colorId) {
        String sql = "SELECT color_hex FROM ThemeColors WHERE color_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, colorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("color_hex");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting color: " + e.getMessage());
        }
        return "#fef08a"; // Default yellow
    }

    // Note data class
    public static class Note {
        private final int noteId;
        private final String content;
        private final int colorId;
        private final double positionX;
        private final double positionY;
        private final Timestamp createdAt;

        public Note(int noteId, String content, int colorId, double positionX, double positionY, Timestamp createdAt) {
            this.noteId = noteId;
            this.content = content;
            this.colorId = colorId;
            this.positionX = positionX;
            this.positionY = positionY;
            this.createdAt = createdAt;
        }

        // Getters
        public int getNoteId() { return noteId; }
        public String getContent() { return content; }
        public int getColorId() { return colorId; }
        public double getPositionX() { return positionX; }
        public double getPositionY() { return positionY; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
}