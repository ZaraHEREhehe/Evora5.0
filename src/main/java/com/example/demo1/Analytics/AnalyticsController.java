package com.example.demo1.Analytics;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.demo1.Database.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class AnalyticsController {

    private final int userId;
    private final String userName;
    private String currentTimeRange = "week";

    // Statistics properties
    private final IntegerProperty tasksCompleted = new SimpleIntegerProperty(0);
    private final IntegerProperty totalTasks = new SimpleIntegerProperty(0);
    private final IntegerProperty pomodoroSessions = new SimpleIntegerProperty(0);
    private final IntegerProperty focusMinutes = new SimpleIntegerProperty(0);
    private final IntegerProperty moodEntries = new SimpleIntegerProperty(0);
    private final DoubleProperty averageMood = new SimpleDoubleProperty(0.0);
    private final IntegerProperty currentStreak = new SimpleIntegerProperty(0);
    private final IntegerProperty longestStreak = new SimpleIntegerProperty(0);
    private final IntegerProperty productivityScore = new SimpleIntegerProperty(0);

    // Data for charts
    private final ObservableList<WeeklyData> weeklyData = FXCollections.observableArrayList();
    private final ObservableList<MoodDistribution> moodDistribution = FXCollections.observableArrayList();
    private final ObservableList<Achievement> achievements = FXCollections.observableArrayList();

    public AnalyticsController(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        refreshData();
    }

    // Get user XP from database
    public int getUserXP() {
        String sql = "SELECT experience FROM Users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("experience");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get real achievements from database with user progress
    public List<Achievement> getRealAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        String sql = """
            SELECT 
                b.badge_id,
                b.badge_icon,
                b.name,
                b.description,
                b.condition_type,
                b.condition_value,
                CASE WHEN ub.user_id IS NOT NULL THEN 1 ELSE 0 END as is_unlocked,
                ub.earned_date,
                -- Calculate current progress based on condition_type
                CASE 
                    WHEN b.condition_type = 'tasks_completed' THEN 
                        (SELECT COUNT(*) FROM (
                            SELECT task_id FROM ToDoTasks WHERE user_id = ? AND is_completed = 1
                            UNION ALL
                            SELECT log_id FROM TaskDeletionLog WHERE user_id = ? AND is_completed = 1
                        ) AS combined_tasks)
                    WHEN b.condition_type = 'pomodoro_sessions' THEN 
                        (SELECT COUNT(*) FROM PomodoroSessions WHERE user_id = ? AND status = 'Completed')
                    WHEN b.condition_type = 'notes_created' THEN 
                        (SELECT COUNT(*) FROM StickyNotes WHERE user_id = ?)
                    WHEN b.condition_type = 'mood_entries' THEN 
                        (SELECT COUNT(*) FROM MoodLogger WHERE user_id = ?)
                    WHEN b.condition_type = 'streak_days' THEN 
                        -- Calculate current streak from all activity data
                        (SELECT COUNT(*) FROM (
                            SELECT DISTINCT activity_date 
                            FROM (
                                SELECT CAST(created_at as DATE) as activity_date FROM ToDoTasks WHERE user_id = ? AND is_completed = 1
                                UNION
                                SELECT CAST(deleted_at as DATE) as activity_date FROM TaskDeletionLog WHERE user_id = ? AND is_completed = 1
                                UNION
                                SELECT CAST(start_time as DATE) as activity_date FROM PomodoroSessions WHERE user_id = ? AND status = 'Completed'
                                UNION
                                SELECT CAST(entry_date as DATE) as activity_date FROM MoodLogger WHERE user_id = ?
                                UNION
                                SELECT CAST(created_at as DATE) as activity_date FROM StickyNotes WHERE user_id = ?
                            ) all_activities
                            WHERE activity_date IS NOT NULL
                        ) user_activities)
                    ELSE 0
                END as current_progress
            FROM Badges b
            LEFT JOIN UserBadges ub ON b.badge_id = ub.badge_id AND ub.user_id = ?
            ORDER BY b.badge_id
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set all 11 parameters
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);
            stmt.setInt(6, userId);
            stmt.setInt(7, userId);
            stmt.setInt(8, userId);
            stmt.setInt(9, userId);
            stmt.setInt(10, userId);
            stmt.setInt(11, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String emoji = getEmojiForBadge(rs.getString("badge_icon"));
                Achievement achievement = new Achievement(
                        rs.getString("name"),
                        rs.getString("description"),
                        emoji,
                        rs.getInt("current_progress"),
                        rs.getInt("condition_value"),
                        rs.getBoolean("is_unlocked"),
                        getAchievementColor(rs.getString("condition_type"))
                );

                achievements.add(achievement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Return empty list instead of mock data
            return new ArrayList<>();
        }

        return achievements;
    }

    private String getEmojiForBadge(String badgeIcon) {
        if (badgeIcon == null) return "🏆";

        // Your badges use emoji icons directly, so return them as-is
        switch (badgeIcon) {
            case "🌟": return "🌟";
            case "⚡": return "⚡";
            case "📝": return "📝";
            case "😊": return "😊";
            case "👑": return "👑";
            case "🏆": return "🏆";
            case "🔥": return "🔥";
            case "📚": return "📚";
            default: return "🏆";
        }
    }

    private String getAchievementColor(String conditionType) {
        switch (conditionType) {
            case "tasks_completed": return "#F472B6";
            case "pomodoro_sessions": return "#60A5FA";
            case "notes_created": return "#34D399";
            case "mood_entries": return "#A78BFA";
            case "streak_days": return "#FBBF24";
            default: return "#FBBF24";
        }
    }

    public void setTimeRange(String timeRange) {
        this.currentTimeRange = timeRange.toLowerCase();
        refreshData();
    }

    public String getCurrentTimeRange() {
        return currentTimeRange;
    }

    private void refreshData() {
        loadStatistics();
        generateWeeklyData();
        generateMoodDistribution();
        generateAchievements();
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName(DatabaseConfig.getDriver());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
        );
    }

    private void loadStatistics() {
        try (Connection conn = getConnection()) {
            loadTaskStatistics(conn);
            loadPomodoroStatistics(conn);
            loadMoodStatistics(conn);
            loadStreakStatistics(conn);
            loadProductivityScore(conn);
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
            // Don't load mock data - just leave values at 0
        }
    }

    private void loadTaskStatistics(Connection conn) throws SQLException {
        // Get completed tasks count for current time range
        String completedSql = """
            SELECT COUNT(*) as completed_count
            FROM (
                SELECT task_id FROM ToDoTasks 
                WHERE user_id = ? AND is_completed = 1 
                AND created_at """ + getDateFilterForTasks() + """
                UNION ALL
                SELECT log_id FROM TaskDeletionLog 
                WHERE user_id = ? AND is_completed = 1
                AND deleted_at """ + getDateFilterForTasks() + """
            ) combined_tasks
            """;

        try (PreparedStatement stmt = conn.prepareStatement(completedSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tasksCompleted.set(rs.getInt("completed_count"));
            }
        }

        // Total tasks count for current time range
        totalTasks.set(getTotalTasksCount(conn));
    }

    private int getTotalTasksCount(Connection conn) throws SQLException {
        String sql = "SELECT ("
                + "    SELECT COUNT(*) FROM ToDoTasks WHERE user_id = ? " + getDateFilterForToDoTasks()
                + "    ) + ("
                + "    SELECT COUNT(*) FROM TaskDeletionLog WHERE user_id = ? " + getDateFilterForToDoTasks()
                + ") as total";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private void loadPomodoroStatistics(Connection conn) throws SQLException {
        String dateFilter = getDateFilterForPomodoro();
        String sessionSql = "SELECT COUNT(*) as sessions FROM PomodoroSessions " +
                "WHERE user_id = ? AND status = 'Completed' " + dateFilter;

        try (PreparedStatement stmt = conn.prepareStatement(sessionSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                pomodoroSessions.set(rs.getInt("sessions"));
            }
        }

        focusMinutes.set(pomodoroSessions.get() * 25);
    }

    private void loadMoodStatistics(Connection conn) throws SQLException {
        String dateFilter = getDateFilterForMood();

        String countSql = "SELECT COUNT(*) as entries FROM MoodLogger WHERE user_id = ? " + dateFilter;
        try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                moodEntries.set(rs.getInt("entries"));
            }
        }

        String avgSql = "SELECT AVG(CAST(mood_value as FLOAT)) as avg_mood FROM MoodLogger " +
                "WHERE user_id = ? " + dateFilter;
        try (PreparedStatement stmt = conn.prepareStatement(avgSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg_mood");
                averageMood.set(rs.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0);
            }
        }
    }

    private void loadStreakStatistics(Connection conn) throws SQLException {
        // Calculate streaks from all activity data within current time range
        String activitySql = """
            SELECT DISTINCT activity_date 
            FROM (
                -- Current task completions
                SELECT CAST(created_at as DATE) as activity_date 
                FROM ToDoTasks 
                WHERE user_id = ? AND is_completed = 1
                """ + getDateFilterForToDoTasks() + """
                UNION
                -- Historical completed tasks from deletion log
                SELECT CAST(deleted_at as DATE) as activity_date 
                FROM TaskDeletionLog 
                WHERE user_id = ? AND is_completed = 1
                """ + getDateFilterForToDoTasks() + """
                UNION
                -- Pomodoro sessions
                SELECT CAST(start_time as DATE) as activity_date 
                FROM PomodoroSessions 
                WHERE user_id = ? AND status = 'Completed'
                """ + getDateFilterForPomodoro() + """
                UNION
                -- Mood entries
                SELECT CAST(entry_date as DATE) as activity_date 
                FROM MoodLogger 
                WHERE user_id = ?
                """ + getDateFilterForMood() + """
                UNION
                -- Sticky notes
                SELECT CAST(created_at as DATE) as activity_date 
                FROM StickyNotes 
                WHERE user_id = ?
                """ + getDateFilterForToDoTasks() + """
            ) activities
            WHERE activity_date IS NOT NULL
            ORDER BY activity_date DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(activitySql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);
            ResultSet rs = stmt.executeQuery();

            List<LocalDate> activityDates = new ArrayList<>();
            while (rs.next()) {
                activityDates.add(rs.getDate("activity_date").toLocalDate());
            }

            // Calculate streaks
            int currentStreakCount = calculateCurrentStreak(activityDates);
            int longestStreakCount = calculateLongestStreak(activityDates);

            currentStreak.set(currentStreakCount);
            longestStreak.set(longestStreakCount);
        }
    }

    private int calculateCurrentStreak(List<LocalDate> activityDates) {
        if (activityDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate currentDate = today;

        // Check consecutive days backwards from today
        while (activityDates.contains(currentDate)) {
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> activityDates) {
        if (activityDates.isEmpty()) return 0;

        // Sort dates in ascending order for streak calculation
        Collections.sort(activityDates);

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < activityDates.size(); i++) {
            LocalDate currentDate = activityDates.get(i);
            LocalDate previousDate = activityDates.get(i - 1);

            if (previousDate.plusDays(1).equals(currentDate)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }

    private void loadProductivityScore(Connection conn) throws SQLException {
        int score = 0;

        // Factor 1: Task completion rate (up to 40 points)
        if (totalTasks.get() > 0) {
            double completionRate = (double) tasksCompleted.get() / totalTasks.get();
            score += (int) (completionRate * 40);
        }

        // Factor 2: Focus sessions relative to time range target (up to 30 points)
        int targetSessions = getTargetSessionsForTimeRange();
        if (targetSessions > 0) {
            double sessionRate = Math.min((double) pomodoroSessions.get() / targetSessions, 1.0);
            score += (int) (sessionRate * 30);
        }

        // Factor 3: Consistency - active days vs total days in time range (up to 30 points)
        int activeDays = getActiveDaysCount(conn);
        int totalDaysInRange = getTotalDaysInTimeRange();
        if (totalDaysInRange > 0) {
            double activityRate = Math.min((double) activeDays / totalDaysInRange, 1.0);
            score += (int) (activityRate * 30);
        }

        productivityScore.set(Math.min(score, 100));
    }

    private int getActiveDaysCount(Connection conn) throws SQLException {
        String activitySql = """
            SELECT COUNT(DISTINCT activity_date) as active_days 
            FROM (
                SELECT CAST(created_at as DATE) as activity_date FROM ToDoTasks 
                WHERE user_id = ? """ + getDateFilterForToDoTasks() + """
                UNION 
                SELECT CAST(start_time as DATE) as activity_date FROM PomodoroSessions 
                WHERE user_id = ? """ + getDateFilterForPomodoro() + """
                UNION 
                SELECT CAST(entry_date as DATE) as activity_date FROM MoodLogger 
                WHERE user_id = ? """ + getDateFilterForMood() + """
            ) activities
            """;

        try (PreparedStatement stmt = conn.prepareStatement(activitySql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("active_days");
            }
        }
        return 0;
    }

    private int getTargetSessionsForTimeRange() {
        switch (currentTimeRange) {
            case "month": return 20;
            case "year": return 104;
            case "week":
            default: return 5;
        }
    }

    private int getTotalDaysInTimeRange() {
        switch (currentTimeRange) {
            case "month": return 30;
            case "year": return 365;
            case "week":
            default: return 7;
        }
    }

    // Date filter methods for different tables
    private String getDateFilterForToDoTasks() {
        switch (currentTimeRange) {
            case "month":
                return "AND created_at >= DATEADD(month, -1, GETDATE())";
            case "year":
                return "AND created_at >= DATEADD(year, -1, GETDATE())";
            case "week":
            default:
                return "AND created_at >= DATEADD(day, -7, GETDATE())";
        }
    }

    private String getDateFilterForPomodoro() {
        switch (currentTimeRange) {
            case "month":
                return "AND start_time >= DATEADD(month, -1, GETDATE())";
            case "year":
                return "AND start_time >= DATEADD(year, -1, GETDATE())";
            case "week":
            default:
                return "AND start_time >= DATEADD(day, -7, GETDATE())";
        }
    }

    private String getDateFilterForMood() {
        switch (currentTimeRange) {
            case "month":
                return "AND entry_date >= DATEADD(month, -1, GETDATE())";
            case "year":
                return "AND entry_date >= DATEADD(year, -1, GETDATE())";
            case "week":
            default:
                return "AND entry_date >= DATEADD(day, -7, GETDATE())";
        }
    }

    private String getDateFilterForTasks() {
        switch (currentTimeRange) {
            case "month":
                return ">= DATEADD(month, -1, GETDATE())";
            case "year":
                return ">= DATEADD(year, -1, GETDATE())";
            case "week":
            default:
                return ">= DATEADD(day, -7, GETDATE())";
        }
    }

    private void generateWeeklyData() {
        weeklyData.clear();

        try (Connection conn = getConnection()) {
            String tasksSql = getTasksQuery();
            String pomodoroSql = getPomodoroQuery();
            String moodSql = getMoodQuery();

            Map<String, Integer> dailyTasks = new LinkedHashMap<>();
            Map<String, Integer> dailyPomodoros = new LinkedHashMap<>();
            Map<String, Double> dailyMoods = new LinkedHashMap<>();

            initializePeriods(dailyTasks, dailyPomodoros, dailyMoods);

            try (PreparedStatement stmt = conn.prepareStatement(tasksSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String period = rs.getString("period");
                    dailyTasks.put(period, rs.getInt("tasks"));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(pomodoroSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String period = rs.getString("period");
                    dailyPomodoros.put(period, rs.getInt("pomodoros"));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(moodSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String period = rs.getString("period");
                    double mood = rs.getDouble("avg_mood");
                    if (!rs.wasNull()) {
                        dailyMoods.put(period, mood);
                    }
                }
            }

            // Only add data points where we have actual mood data
            for (String period : dailyTasks.keySet()) {
                // Check if we have mood data for this period
                if (dailyMoods.containsKey(period) && dailyMoods.get(period) > 0) {
                    weeklyData.add(new WeeklyData(
                            period,
                            dailyTasks.get(period),
                            dailyPomodoros.get(period),
                            (int) Math.round(dailyMoods.get(period))
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error generating weekly data: " + e.getMessage());
            // Don't generate any data - leave weeklyData empty
        }
    }

    private String getTasksQuery() {
        switch (currentTimeRange) {
            case "month":
                return """
                SELECT period, SUM(tasks) as tasks
                FROM (
                    SELECT 
                        'Week ' + CAST(DATEPART(WEEK, created_at) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, created_at), 0)) + 1 as VARCHAR) as period,
                        1 as tasks
                    FROM ToDoTasks 
                    WHERE user_id = ? AND is_completed = 1 
                    AND created_at >= DATEADD(month, -1, GETDATE())
                    UNION ALL
                    SELECT 
                        'Week ' + CAST(DATEPART(WEEK, deleted_at) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, deleted_at), 0)) + 1 as VARCHAR) as period,
                        1 as tasks
                    FROM TaskDeletionLog 
                    WHERE user_id = ? AND is_completed = 1
                    AND deleted_at >= DATEADD(month, -1, GETDATE())
                ) combined
                GROUP BY period
                ORDER BY MIN(
                    CASE 
                        WHEN period = 'Week 1' THEN 1
                        WHEN period = 'Week 2' THEN 2
                        WHEN period = 'Week 3' THEN 3
                        WHEN period = 'Week 4' THEN 4
                        WHEN period = 'Week 5' THEN 5
                        ELSE 6
                    END
                )
                """;
            case "year":
                return """
                SELECT period, SUM(tasks) as tasks
                FROM (
                    SELECT 
                        DATENAME(MONTH, created_at) as period,
                        1 as tasks
                    FROM ToDoTasks 
                    WHERE user_id = ? AND is_completed = 1 
                    AND created_at >= DATEADD(year, -1, GETDATE())
                    UNION ALL
                    SELECT 
                        DATENAME(MONTH, deleted_at) as period,
                        1 as tasks
                    FROM TaskDeletionLog 
                    WHERE user_id = ? AND is_completed = 1
                    AND deleted_at >= DATEADD(year, -1, GETDATE())
                ) combined
                GROUP BY period
                ORDER BY MIN(
                    CASE 
                        WHEN period = 'January' THEN 1
                        WHEN period = 'February' THEN 2
                        WHEN period = 'March' THEN 3
                        WHEN period = 'April' THEN 4
                        WHEN period = 'May' THEN 5
                        WHEN period = 'June' THEN 6
                        WHEN period = 'July' THEN 7
                        WHEN period = 'August' THEN 8
                        WHEN period = 'September' THEN 9
                        WHEN period = 'October' THEN 10
                        WHEN period = 'November' THEN 11
                        WHEN period = 'December' THEN 12
                        ELSE 13
                    END
                )
                """;
            case "week":
            default:
                return """
                SELECT period, SUM(tasks) as tasks
                FROM (
                    SELECT 
                        DATENAME(WEEKDAY, created_at) as period,
                        1 as tasks
                    FROM ToDoTasks 
                    WHERE user_id = ? AND is_completed = 1 
                    AND created_at >= DATEADD(day, -7, GETDATE())
                    UNION ALL
                    SELECT 
                        DATENAME(WEEKDAY, deleted_at) as period,
                        1 as tasks
                    FROM TaskDeletionLog 
                    WHERE user_id = ? AND is_completed = 1
                    AND deleted_at >= DATEADD(day, -7, GETDATE())
                ) combined
                GROUP BY period
                ORDER BY MIN(
                    CASE 
                        WHEN period = 'Monday' THEN 1
                        WHEN period = 'Tuesday' THEN 2
                        WHEN period = 'Wednesday' THEN 3
                        WHEN period = 'Thursday' THEN 4
                        WHEN period = 'Friday' THEN 5
                        WHEN period = 'Saturday' THEN 6
                        WHEN period = 'Sunday' THEN 7
                        ELSE 8
                    END
                )
                """;
        }
    }

    private String getPomodoroQuery() {
        switch (currentTimeRange) {
            case "month":
                return "SELECT " +
                        "DATEPART(WEEK, start_time) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, start_time), 0)) + 1 as week_num, " +
                        "'Week ' + CAST(DATEPART(WEEK, start_time) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, start_time), 0)) + 1 as VARCHAR) as period, " +
                        "COUNT(*) as pomodoros " +
                        "FROM PomodoroSessions " +
                        "WHERE user_id = ? AND status = 'Completed' " +
                        "AND start_time >= DATEADD(month, -1, GETDATE()) " +
                        "GROUP BY DATEPART(WEEK, start_time) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, start_time), 0)) + 1 " +
                        "ORDER BY week_num";
            case "year":
                return "SELECT " +
                        "DATENAME(MONTH, start_time) as period, " +
                        "COUNT(*) as pomodoros " +
                        "FROM PomodoroSessions " +
                        "WHERE user_id = ? AND status = 'Completed' " +
                        "AND start_time >= DATEADD(year, -1, GETDATE()) " +
                        "GROUP BY DATENAME(MONTH, start_time), MONTH(start_time) " +
                        "ORDER BY MONTH(start_time)";
            case "week":
            default:
                return "SELECT " +
                        "DATENAME(WEEKDAY, start_time) as period, " +
                        "COUNT(*) as pomodoros " +
                        "FROM PomodoroSessions " +
                        "WHERE user_id = ? AND status = 'Completed' " +
                        "AND start_time >= DATEADD(day, -7, GETDATE()) " +
                        "GROUP BY DATENAME(WEEKDAY, start_time), CAST(start_time as DATE) " +
                        "ORDER BY MIN(CAST(start_time as DATE))";
        }
    }

    private String getMoodQuery() {
        switch (currentTimeRange) {
            case "month":
                return "SELECT " +
                        "DATEPART(WEEK, entry_date) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, entry_date), 0)) + 1 as week_num, " +
                        "'Week ' + CAST(DATEPART(WEEK, entry_date) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, entry_date), 0)) + 1 as VARCHAR) as period, " +
                        "AVG(CAST(mood_value as FLOAT)) as avg_mood " +
                        "FROM MoodLogger " +
                        "WHERE user_id = ? " +
                        "AND entry_date >= DATEADD(month, -1, GETDATE()) " +
                        "GROUP BY DATEPART(WEEK, entry_date) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, entry_date), 0)) + 1 " +
                        "ORDER BY week_num";
            case "year":
                return "SELECT " +
                        "DATENAME(MONTH, entry_date) as period, " +
                        "AVG(CAST(mood_value as FLOAT)) as avg_mood " +
                        "FROM MoodLogger " +
                        "WHERE user_id = ? " +
                        "AND entry_date >= DATEADD(year, -1, GETDATE()) " +
                        "GROUP BY DATENAME(MONTH, entry_date), MONTH(entry_date) " +
                        "ORDER BY MONTH(entry_date)";
            case "week":
            default:
                return "SELECT " +
                        "DATENAME(WEEKDAY, entry_date) as period, " +
                        "AVG(CAST(mood_value as FLOAT)) as avg_mood " +
                        "FROM MoodLogger " +
                        "WHERE user_id = ? " +
                        "AND entry_date >= DATEADD(day, -7, GETDATE()) " +
                        "GROUP BY DATENAME(WEEKDAY, entry_date), entry_date " +
                        "ORDER BY MIN(entry_date)";
        }
    }

    private void initializePeriods(Map<String, Integer> tasks, Map<String, Integer> pomodoros, Map<String, Double> moods) {
        switch (currentTimeRange) {
            case "month":
                for (int week = 1; week <= 5; week++) {
                    String period = "Week " + week;
                    tasks.put(period, 0);
                    pomodoros.put(period, 0);
                    // Don't pre-populate moods - leave empty
                }
                break;
            case "year":
                String[] months = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};
                for (String month : months) {
                    tasks.put(month, 0);
                    pomodoros.put(month, 0);
                    // Don't pre-populate moods - leave empty
                }
                break;
            case "week":
            default:
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                for (String day : days) {
                    tasks.put(day, 0);
                    pomodoros.put(day, 0);
                    // Don't pre-populate moods - leave empty
                }
                break;
        }
    }

    private void generateMoodDistribution() {
        moodDistribution.clear();

        try (Connection conn = getConnection()) {
            String dateFilter = getDateFilterForMood();
            String sql = "SELECT " +
                    "SUM(CASE WHEN mood_value = 5 THEN 1 ELSE 0 END) as excellent, " +
                    "SUM(CASE WHEN mood_value = 4 THEN 1 ELSE 0 END) as good, " +
                    "SUM(CASE WHEN mood_value = 3 THEN 1 ELSE 0 END) as neutral, " +
                    "SUM(CASE WHEN mood_value <= 2 THEN 1 ELSE 0 END) as low, " +
                    "COUNT(*) as total " +
                    "FROM MoodLogger WHERE user_id = ? " + dateFilter;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int total = rs.getInt("total");
                    if (total > 0) {
                        moodDistribution.addAll(
                                Arrays.asList(
                                        new MoodDistribution("Excellent", (int) ((rs.getInt("excellent") / (double) total) * 100), "#FF9FB5"),
                                        new MoodDistribution("Good", (int) ((rs.getInt("good") / (double) total) * 100), "#B5E5B5"),
                                        new MoodDistribution("Neutral", (int) ((rs.getInt("neutral") / (double) total) * 100), "#B5E5FF"),
                                        new MoodDistribution("Low", (int) ((rs.getInt("low") / (double) total) * 100), "#FFE5B5")
                                )
                        );
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating mood distribution: " + e.getMessage());
        }

        // If no data, add empty distribution
        moodDistribution.addAll(
                Arrays.asList(
                        new MoodDistribution("Excellent", 0, "#FF9FB5"),
                        new MoodDistribution("Good", 0, "#B5E5B5"),
                        new MoodDistribution("Neutral", 0, "#B5E5FF"),
                        new MoodDistribution("Low", 0, "#FFE5B5")
                )
        );
    }

    private void generateAchievements() {
        achievements.clear();
        achievements.addAll(getRealAchievements());
    }

    // Getter methods
    public IntegerProperty tasksCompletedProperty() { return tasksCompleted; }
    public IntegerProperty totalTasksProperty() { return totalTasks; }
    public IntegerProperty pomodoroSessionsProperty() { return pomodoroSessions; }
    public IntegerProperty focusMinutesProperty() { return focusMinutes; }
    public IntegerProperty moodEntriesProperty() { return moodEntries; }
    public DoubleProperty averageMoodProperty() { return averageMood; }
    public IntegerProperty currentStreakProperty() { return currentStreak; }
    public IntegerProperty longestStreakProperty() { return longestStreak; }
    public IntegerProperty productivityScoreProperty() { return productivityScore; }

    public ObservableList<WeeklyData> getWeeklyData() { return weeklyData; }
    public ObservableList<MoodDistribution> getMoodDistribution() { return moodDistribution; }
    public ObservableList<Achievement> getAchievements() { return achievements; }

    // Data classes
    public static class WeeklyData {
        private final String day;
        private final int tasks;
        private final int pomodoros;
        private final int mood;

        public WeeklyData(String day, int tasks, int pomodoros, int mood) {
            this.day = day;
            this.tasks = tasks;
            this.pomodoros = pomodoros;
            this.mood = mood;
        }

        public String getDay() { return day; }
        public int getTasks() { return tasks; }
        public int getPomodoros() { return pomodoros; }
        public int getMood() { return mood; }
    }

    public static class MoodDistribution {
        private final String mood;
        private final int value;
        private final String color;

        public MoodDistribution(String mood, int value, String color) {
            this.mood = mood;
            this.value = value;
            this.color = color;
        }

        public String getMood() { return mood; }
        public int getValue() { return value; }
        public String getColor() { return color; }
    }

    public static class Achievement {
        private final String title;
        private final String description;
        private final String emoji;
        private final int progress;
        private final int maxProgress;
        private final boolean unlocked;
        private final String color;

        public Achievement(String title, String description, String emoji,
                           int progress, int maxProgress, boolean unlocked, String color) {
            this.title = title;
            this.description = description;
            this.emoji = emoji;
            this.progress = progress;
            this.maxProgress = maxProgress;
            this.unlocked = unlocked;
            this.color = color;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
        public int getProgress() { return progress; }
        public int getMaxProgress() { return maxProgress; }
        public boolean isUnlocked() { return unlocked; }
        public String getColor() { return color; }
    }
}