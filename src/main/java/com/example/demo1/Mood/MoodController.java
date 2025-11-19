package com.example.demo1.Mood;

import com.example.demo1.Database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoodController {
    private int currentUserId;

    // Mood configuration
    private final String[] moodEmojis = {"😢", "😟", "😐", "😊", "😄"};
    private final String[] moodLabels = {"Very Sad", "Sad", "Neutral", "Happy", "Very Happy"};

    public MoodController(int userId) {
        this.currentUserId = userId;
        System.out.println("Mood module loaded for user: " + userId);
    }

    // Create or update mood entry
    public boolean logMood(int moodValue, String note) {
        LocalDate today = LocalDate.now();
        String sql = """
            MERGE INTO MoodLogger AS target
            USING (SELECT ? AS user_id, ? AS entry_date) AS source
            ON target.user_id = source.user_id AND target.entry_date = source.entry_date
            WHEN MATCHED THEN
                UPDATE SET mood_value = ?, note = ?, mood_icon = ?
            WHEN NOT MATCHED THEN
                INSERT (user_id, mood_value, note, mood_icon, entry_date)
                VALUES (?, ?, ?, ?, ?);
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String moodIcon = moodEmojis[moodValue - 1];

            // Parameters for UPDATE
            stmt.setInt(1, currentUserId);
            stmt.setDate(2, Date.valueOf(today));
            stmt.setInt(3, moodValue);
            stmt.setString(4, note);
            stmt.setString(5, moodIcon);

            // Parameters for INSERT
            stmt.setInt(6, currentUserId);
            stmt.setInt(7, moodValue);
            stmt.setString(8, note);
            stmt.setString(9, moodIcon);
            stmt.setDate(10, Date.valueOf(today));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging mood: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all mood entries for current user
    public List<MoodEntry> getMoodHistory() {
        List<MoodEntry> entries = new ArrayList<>();
        String sql = "SELECT mood_value, note, entry_date FROM MoodLogger WHERE user_id = ? ORDER BY entry_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MoodEntry entry = new MoodEntry(
                            rs.getInt("mood_value"),
                            rs.getString("note"),
                            rs.getDate("entry_date").toLocalDate()
                    );
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching mood history: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    // Get today's mood entry
    public MoodEntry getTodaysMood() {
        String sql = "SELECT mood_value, note, entry_date FROM MoodLogger WHERE user_id = ? AND entry_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MoodEntry(
                            rs.getInt("mood_value"),
                            rs.getString("note"),
                            rs.getDate("entry_date").toLocalDate()
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's mood: " + e.getMessage());
        }
        return null;
    }

    // Calculate average mood
    public double getAverageMood() {
        List<MoodEntry> entries = getMoodHistory();
        if (entries.isEmpty()) return 0;

        double sum = entries.stream().mapToInt(MoodEntry::getMoodValue).sum();
        return Math.round((sum / entries.size()) * 10.0) / 10.0;
    }

    // Get mood configuration
    public String[] getMoodEmojis() {
        return moodEmojis;
    }

    public String[] getMoodLabels() {
        return moodLabels;
    }

    // Mood entry data class
    public static class MoodEntry {
        private final int moodValue;
        private final String note;
        private final LocalDate date;

        public MoodEntry(int moodValue, String note, LocalDate date) {
            this.moodValue = moodValue;
            this.note = note;
            this.date = date;
        }

        public int getMoodValue() { return moodValue; }
        public String getNote() { return note; }
        public LocalDate getDate() { return date; }
    }
}