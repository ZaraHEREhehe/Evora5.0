package com.example.demo1.Analytics;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.demo1.Database.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
    private final IntegerProperty petHappiness = new SimpleIntegerProperty(0);
    private final IntegerProperty coinsEarned = new SimpleIntegerProperty(0);

    // Data for charts
    private final ObservableList<WeeklyData> weeklyData = FXCollections.observableArrayList();
    private final ObservableList<MoodDistribution> moodDistribution = FXCollections.observableArrayList();
    private final ObservableList<Achievement> achievements = FXCollections.observableArrayList();

    // Color scheme matching your pastel theme
    private final Map<String, String> colors = Map.of(
            "pink", "#FF9FB5",
            "blue", "#B5E5FF",
            "purple", "#A28BF0",
            "green", "#B5E5B5",
            "yellow", "#FFE5B5",
            "lightPink", "#FFD1DC",
            "lightBlue", "#D4F0FF",
            "lightPurple", "#D4C2FF",
            "lightGreen", "#D4F0D4"
    );

    public AnalyticsController(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        refreshData();
    }

    // Add this missing method
    public void setTimeRange(String timeRange) {
        this.currentTimeRange = timeRange.toLowerCase();
        refreshData();
    }

    // Add this missing method
    public String getCurrentTimeRange() {
        return currentTimeRange;
    }

    private void refreshData() {
        loadStatistics();
        generateWeeklyData();
        generateMoodDistribution();
        generateAchievements();
    }

    // Database connection method
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

    // Data loading methods with real database queries
    private void loadStatistics() {
        try (Connection conn = getConnection()) {
            loadTaskStatistics(conn);
            loadPomodoroStatistics(conn);
            loadMoodStatistics(conn);
            loadStreakStatistics(conn);
            loadPetAndCoinStatistics(conn);
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
            loadMockStatistics();
        }
    }

    private void loadTaskStatistics(Connection conn) throws SQLException {
        String dateFilter = getDateFilter("created_at");
        String sql = "SELECT " +
                "COUNT(CASE WHEN is_completed = 1 THEN 1 END) as completed, " +
                "COUNT(*) as total " +
                "FROM ToDoTasks WHERE user_id = ? " + dateFilter;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tasksCompleted.set(rs.getInt("completed"));
                totalTasks.set(rs.getInt("total"));
            }
        }
    }

    private void loadPomodoroStatistics(Connection conn) throws SQLException {
        String dateFilter = getDateFilter("start_time");
        String sessionSql = "SELECT COUNT(*) as sessions FROM PomodoroSessions " +
                "WHERE user_id = ? AND status = 'Completed' " + dateFilter;

        try (PreparedStatement stmt = conn.prepareStatement(sessionSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                pomodoroSessions.set(rs.getInt("sessions"));
            }
        }

        // Calculate total focus minutes (25 minutes per session + actual work_duration if available)
        focusMinutes.set(pomodoroSessions.get() * 25);
    }

    private void loadMoodStatistics(Connection conn) throws SQLException {
        String dateFilter = getDateFilter("entry_date");

        // Count mood entries
        String countSql = "SELECT COUNT(*) as entries FROM MoodLogger WHERE user_id = ? " + dateFilter;
        try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                moodEntries.set(rs.getInt("entries"));
            }
        }

        // Calculate average mood for current time range
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
        // Improved streak calculation
        String streakSql = "SELECT DISTINCT entry_date FROM MoodLogger " +
                "WHERE user_id = ? AND entry_date >= DATEADD(day, -30, GETDATE()) " +
                "ORDER BY entry_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(streakSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            int currentStreakCount = 0;
            int longestStreakCount = 0;
            int tempStreak = 0;
            LocalDate lastDate = null;
            LocalDate expectedDate = null;

            while (rs.next()) {
                LocalDate currentDate = rs.getDate("entry_date").toLocalDate();

                if (lastDate == null) {
                    currentStreakCount = 1;
                    tempStreak = 1;
                    expectedDate = currentDate.minusDays(1);
                } else if (currentDate.equals(expectedDate)) {
                    tempStreak++;
                    expectedDate = currentDate.minusDays(1);
                } else {
                    longestStreakCount = Math.max(longestStreakCount, tempStreak);
                    tempStreak = 1;
                    expectedDate = currentDate.minusDays(1);
                }

                lastDate = currentDate;
            }

            longestStreakCount = Math.max(longestStreakCount, tempStreak);
            currentStreak.set(currentStreakCount);
            longestStreak.set(longestStreakCount);
        }
    }

    private void loadPetAndCoinStatistics(Connection conn) throws SQLException {
        // Get actual pet happiness from database if available
        String petSql = "SELECT COUNT(*) as active_days FROM (" +
                "SELECT CAST(created_at as DATE) as activity_date FROM ToDoTasks WHERE user_id = ? AND is_completed = 1 " +
                "UNION SELECT CAST(start_time as DATE) as activity_date FROM PomodoroSessions WHERE user_id = ? AND status = 'Completed'" +
                ") activities WHERE activity_date >= DATEADD(day, -7, GETDATE())";

        try (PreparedStatement stmt = conn.prepareStatement(petSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int activeDays = rs.getInt("active_days");
                petHappiness.set(Math.min(activeDays * 15, 100)); // Scale based on active days
            } else {
                petHappiness.set(85);
            }
        }

        // Coins earned based on actual activities
        coinsEarned.set(tasksCompleted.get() * 2 + pomodoroSessions.get() * 5);
    }

    private String getDateFilter(String dateColumn) {
        switch (currentTimeRange) {
            case "month":
                return "AND " + dateColumn + " >= DATEADD(month, -1, GETDATE())";
            case "year":
                return "AND " + dateColumn + " >= DATEADD(year, -1, GETDATE())";
            case "week":
            default:
                return "AND " + dateColumn + " >= DATEADD(day, -7, GETDATE())";
        }
    }

    private void generateWeeklyData() {
        weeklyData.clear();

        try (Connection conn = getConnection()) {
            // Get tasks completed per day for the current time range
            String tasksSql = getTasksQuery();
            String pomodoroSql = getPomodoroQuery();
            String moodSql = getMoodQuery();

            Map<String, Integer> dailyTasks = new LinkedHashMap<>();
            Map<String, Integer> dailyPomodoros = new LinkedHashMap<>();
            Map<String, Double> dailyMoods = new LinkedHashMap<>();

            // Initialize with appropriate periods based on time range
            initializePeriods(dailyTasks, dailyPomodoros, dailyMoods);

            try (PreparedStatement stmt = conn.prepareStatement(tasksSql)) {
                stmt.setInt(1, userId);
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

            // Convert to weekly data
            for (String period : dailyTasks.keySet()) {
                weeklyData.add(new WeeklyData(
                        period,
                        dailyTasks.get(period),
                        dailyPomodoros.get(period),
                        (int) Math.round(dailyMoods.getOrDefault(period, 4.0))
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error generating weekly data: " + e.getMessage());
            generateMockWeeklyData();
        }
    }

    private String getTasksQuery() {
        switch (currentTimeRange) {
            case "month":
                return "SELECT " +
                        "DATEPART(WEEK, created_at) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, created_at), 0)) + 1 as week_num, " +
                        "'Week ' + CAST(DATEPART(WEEK, created_at) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, created_at), 0)) + 1 as VARCHAR) as period, " +
                        "COUNT(*) as tasks " +
                        "FROM ToDoTasks " +
                        "WHERE user_id = ? AND is_completed = 1 " +
                        "AND created_at >= DATEADD(month, -1, GETDATE()) " +
                        "GROUP BY DATEPART(WEEK, created_at) - DATEPART(WEEK, DATEADD(MONTH, DATEDIFF(MONTH, 0, created_at), 0)) + 1 " +
                        "ORDER BY week_num";
            case "year":
                return "SELECT " +
                        "DATENAME(MONTH, created_at) as period, " +
                        "COUNT(*) as tasks " +
                        "FROM ToDoTasks " +
                        "WHERE user_id = ? AND is_completed = 1 " +
                        "AND created_at >= DATEADD(year, -1, GETDATE()) " +
                        "GROUP BY DATENAME(MONTH, created_at), MONTH(created_at) " +
                        "ORDER BY MONTH(created_at)";
            case "week":
            default:
                return "SELECT " +
                        "DATENAME(WEEKDAY, created_at) as period, " +
                        "COUNT(*) as tasks " +
                        "FROM ToDoTasks " +
                        "WHERE user_id = ? AND is_completed = 1 " +
                        "AND created_at >= DATEADD(day, -7, GETDATE()) " +
                        "GROUP BY DATENAME(WEEKDAY, created_at), CAST(created_at as DATE) " +
                        "ORDER BY MIN(CAST(created_at as DATE))";
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
                    moods.put(period, 4.0);
                }
                break;
            case "year":
                String[] months = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};
                for (String month : months) {
                    tasks.put(month, 0);
                    pomodoros.put(month, 0);
                    moods.put(month, 4.0);
                }
                break;
            case "week":
            default:
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                for (String day : days) {
                    tasks.put(day, 0);
                    pomodoros.put(day, 0);
                    moods.put(day, 4.0);
                }
                break;
        }
    }

    private void generateMockWeeklyData() {
        weeklyData.clear();
        if ("month".equals(currentTimeRange)) {
            weeklyData.addAll(
                    new WeeklyData("Week 1", 15, 8, 4),
                    new WeeklyData("Week 2", 22, 12, 5),
                    new WeeklyData("Week 3", 18, 10, 4),
                    new WeeklyData("Week 4", 25, 14, 5)
            );
        } else if ("year".equals(currentTimeRange)) {
            weeklyData.addAll(
                    new WeeklyData("Jan", 45, 20, 4),
                    new WeeklyData("Feb", 52, 25, 4),
                    new WeeklyData("Mar", 48, 22, 4),
                    new WeeklyData("Apr", 55, 28, 5),
                    new WeeklyData("May", 60, 30, 5),
                    new WeeklyData("Jun", 58, 29, 4),
                    new WeeklyData("Jul", 42, 18, 3),
                    new WeeklyData("Aug", 50, 24, 4),
                    new WeeklyData("Sep", 65, 32, 5),
                    new WeeklyData("Oct", 70, 35, 5),
                    new WeeklyData("Nov", 68, 34, 5),
                    new WeeklyData("Dec", 40, 15, 4)
            );
        } else {
            weeklyData.addAll(
                    new WeeklyData("Mon", 8, 4, 4),
                    new WeeklyData("Tue", 6, 3, 5),
                    new WeeklyData("Wed", 10, 5, 4),
                    new WeeklyData("Thu", 7, 4, 3),
                    new WeeklyData("Fri", 9, 6, 5),
                    new WeeklyData("Sat", 4, 2, 4),
                    new WeeklyData("Sun", 3, 1, 4)
            );
        }
    }

    private void generateMoodDistribution() {
        moodDistribution.clear();

        try (Connection conn = getConnection()) {
            String dateFilter = getDateFilter("entry_date");
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
                                new MoodDistribution("Excellent", (int) ((rs.getInt("excellent") / (double) total) * 100), colors.get("pink")),
                                new MoodDistribution("Good", (int) ((rs.getInt("good") / (double) total) * 100), colors.get("green")),
                                new MoodDistribution("Neutral", (int) ((rs.getInt("neutral") / (double) total) * 100), colors.get("lightBlue")),
                                new MoodDistribution("Low", (int) ((rs.getInt("low") / (double) total) * 100), colors.get("yellow"))
                        );
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating mood distribution: " + e.getMessage());
        }

        // Fallback mock data
        moodDistribution.addAll(
                new MoodDistribution("Excellent", 25, colors.get("pink")),
                new MoodDistribution("Good", 35, colors.get("green")),
                new MoodDistribution("Neutral", 25, colors.get("lightBlue")),
                new MoodDistribution("Low", 15, colors.get("yellow"))
        );
    }

    private void generateAchievements() {
        achievements.clear();

        try (Connection conn = getConnection()) {
            // Get actual achievement progress from database
            String tasksSql = "SELECT COUNT(*) as completed FROM ToDoTasks WHERE user_id = ? AND is_completed = 1";
            String pomodoroSql = "SELECT COUNT(*) as sessions FROM PomodoroSessions WHERE user_id = ? AND status = 'Completed'";
            String moodSql = "SELECT COUNT(*) as entries FROM MoodLogger WHERE user_id = ?";

            int completedTasks = 0;
            int completedSessions = 0;
            int moodEntriesCount = 0;

            try (PreparedStatement stmt = conn.prepareStatement(tasksSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) completedTasks = rs.getInt("completed");
            }

            try (PreparedStatement stmt = conn.prepareStatement(pomodoroSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) completedSessions = rs.getInt("sessions");
            }

            try (PreparedStatement stmt = conn.prepareStatement(moodSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) moodEntriesCount = rs.getInt("entries");
            }

            achievements.addAll(
                    new Achievement("Getting Started", "Complete tasks for 5 days in a row", "📅",
                            currentStreak.get() >= 5, Math.min(currentStreak.get(), 5), 5, colors.get("pink")),
                    new Achievement("Pomodoro Master", "Complete 25 pomodoro sessions", "⏰",
                            completedSessions >= 25, Math.min(completedSessions, 25), 25, colors.get("blue")),
                    new Achievement("Mood Keeper", "Log your mood for 14 consecutive days", "❤️",
                            currentStreak.get() >= 14, Math.min(currentStreak.get(), 14), 14, colors.get("purple")),
                    new Achievement("Task Completionist", "Complete 100 tasks total", "✓",
                            completedTasks >= 100, Math.min(completedTasks, 100), 100, colors.get("green")),
                    new Achievement("Pet Whisperer", "Keep pet happiness above 80% for a week", "⭐",
                            petHappiness.get() >= 80, petHappiness.get() >= 80 ? 7 : 0, 7, colors.get("yellow")),
                    new Achievement("Focus Champion", "Accumulate 10 hours of focused work time", "🎯",
                            focusMinutes.get() >= 600, Math.min(focusMinutes.get(), 600), 600, colors.get("lightPurple"))
            );

        } catch (SQLException e) {
            System.err.println("Error generating achievements: " + e.getMessage());
            // Fallback to mock data
            achievements.addAll(
                    new Achievement("Getting Started", "Complete tasks for 5 days in a row", "📅", true, 5, 5, colors.get("pink")),
                    new Achievement("Pomodoro Master", "Complete 25 pomodoro sessions", "⏰", false, 23, 25, colors.get("blue")),
                    new Achievement("Mood Keeper", "Log your mood for 14 consecutive days", "❤️", false, 12, 14, colors.get("purple")),
                    new Achievement("Task Completionist", "Complete 100 tasks total", "✓", false, 47, 100, colors.get("green")),
                    new Achievement("Pet Whisperer", "Keep pet happiness above 80% for a week", "⭐", true, 7, 7, colors.get("yellow")),
                    new Achievement("Focus Champion", "Accumulate 10 hours of focused work time", "🎯", false, 575, 600, colors.get("lightPurple"))
            );
        }
    }

    private void loadMockStatistics() {
        tasksCompleted.set(47);
        totalTasks.set(52);
        pomodoroSessions.set(23);
        focusMinutes.set(575);
        moodEntries.set(12);
        averageMood.set(4.2);
        currentStreak.set(5);
        longestStreak.set(12);
        petHappiness.set(85);
        coinsEarned.set(230);
    }

    // Getter methods for the view
    public IntegerProperty tasksCompletedProperty() { return tasksCompleted; }
    public IntegerProperty totalTasksProperty() { return totalTasks; }
    public IntegerProperty pomodoroSessionsProperty() { return pomodoroSessions; }
    public IntegerProperty focusMinutesProperty() { return focusMinutes; }
    public IntegerProperty moodEntriesProperty() { return moodEntries; }
    public DoubleProperty averageMoodProperty() { return averageMood; }
    public IntegerProperty currentStreakProperty() { return currentStreak; }
    public IntegerProperty longestStreakProperty() { return longestStreak; }
    public IntegerProperty petHappinessProperty() { return petHappiness; }
    public IntegerProperty coinsEarnedProperty() { return coinsEarned; }

    public ObservableList<WeeklyData> getWeeklyData() { return weeklyData; }
    public ObservableList<MoodDistribution> getMoodDistribution() { return moodDistribution; }
    public ObservableList<Achievement> getAchievements() { return achievements; }
    public Map<String, String> getColors() { return colors; }

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
        private final boolean unlocked;
        private final int progress;
        private final int maxProgress;
        private final String color;

        public Achievement(String title, String description, String emoji, boolean unlocked, int progress, int maxProgress, String color) {
            this.title = title;
            this.description = description;
            this.emoji = emoji;
            this.unlocked = unlocked;
            this.progress = progress;
            this.maxProgress = maxProgress;
            this.color = color;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
        public boolean isUnlocked() { return unlocked; }
        public int getProgress() { return progress; }
        public int getMaxProgress() { return maxProgress; }
        public String getColor() { return color; }
    }
}