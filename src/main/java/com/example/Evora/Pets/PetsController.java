package com.example.Evora.Pets;

import com.example.Evora.Database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetsController {
    private int currentUserId;
    private PetChangeListener petChangeListener;

    public PetsController(int userId) {
        this.currentUserId = userId;
        System.out.println("Pets module loaded for user: " + userId);
        ensureDefaultPet(); // Make sure user has default pet
    }

    public interface PetChangeListener {
        void onPetChanged();
    }

    public void setPetChangeListener(PetChangeListener listener) {
        this.petChangeListener = listener;
    }

    public void notifyPetChanged() {
        if (petChangeListener != null) {
            petChangeListener.onPetChanged();
        }
    }

    // Ensure user has default pet equipped
    private void ensureDefaultPet() {
        String checkSql = "SELECT current_pet_id FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer currentPetId = rs.getInt("current_pet_id");
                if (rs.wasNull() || currentPetId == 0) {
                    // User has no pet equipped, set default
                    setDefaultPet();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking default pet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setDefaultPet() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update user's current pet
            String updateUserSql = "UPDATE Users SET current_pet_id = 1 WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                stmt.setInt(1, currentUserId);
                stmt.executeUpdate();
            }

            // Add to PetMascot if not exists and set as equipped
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO PetMascot (user_id, pet_type_id, pet_name, is_equipped) " +
                            "SELECT ?, 1, pt.pet_name, 1 FROM PetTypes pt WHERE pt.pet_type_id = 1 " +
                            "AND NOT EXISTS (SELECT 1 FROM PetMascot WHERE user_id = ? AND pet_type_id = 1)")) {
                stmt.setInt(1, currentUserId);
                stmt.setInt(2, currentUserId);
                stmt.executeUpdate();
            }

            System.out.println("Set default pet for user: " + currentUserId);
        } catch (SQLException e) {
            System.err.println("Error setting default pet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get current equipped pet
    public Pet getCurrentPet() {
        String sql = """
            SELECT pt.pet_type_id, 
                   COALESCE(pm.pet_name, pt.pet_name) as pet_name, 
                   pt.species, pt.required_experience, 
                   pt.gif_filename, pt.personality, pt.working_activity,
                   u.experience as user_experience
            FROM Users u
            JOIN PetTypes pt ON u.current_pet_id = pt.pet_type_id
            LEFT JOIN PetMascot pm ON u.user_id = pm.user_id AND pt.pet_type_id = pm.pet_type_id
            WHERE u.user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Pet(
                            rs.getInt("pet_type_id"),
                            rs.getString("pet_name"),
                            rs.getString("species"),
                            rs.getInt("required_experience"),
                            rs.getString("gif_filename"),
                            rs.getString("personality"),
                            rs.getString("working_activity"),
                            rs.getInt("user_experience")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current pet: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Change pet name in database
    public boolean changePetName(int petTypeId, String newName) {
        String sql = "UPDATE PetMascot SET pet_name = ? WHERE user_id = ? AND pet_type_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, petTypeId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Successfully changed pet name to: " + newName);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error changing pet name: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all unlocked pets for this user
    public List<Pet> getUnlockedPets() {
        List<Pet> pets = new ArrayList<>();
        String sql = """
            SELECT pt.pet_type_id, 
                   COALESCE(pm.pet_name, pt.pet_name) as pet_name, 
                   pt.species, pt.required_experience, 
                   pt.gif_filename, pt.personality, pt.working_activity,
                   u.experience as user_experience,
                   pm.is_equipped
            FROM PetMascot pm
            JOIN PetTypes pt ON pm.pet_type_id = pt.pet_type_id
            JOIN Users u ON pm.user_id = u.user_id
            WHERE pm.user_id = ?
            ORDER BY pt.required_experience
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = new Pet(
                            rs.getInt("pet_type_id"),
                            rs.getString("pet_name"),
                            rs.getString("species"),
                            rs.getInt("required_experience"),
                            rs.getString("gif_filename"),
                            rs.getString("personality"),
                            rs.getString("working_activity"),
                            rs.getInt("user_experience")
                    );
                    pet.setEquipped(rs.getBoolean("is_equipped"));
                    pets.add(pet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unlocked pets: " + e.getMessage());
            e.printStackTrace();
        }
        return pets;
    }

    // Get all available pets (for collection view)
    public List<Pet> getAllPets() {
        // Check for new unlocks when viewing all pets
        checkAndUnlockPets();

        List<Pet> pets = new ArrayList<>();
        String sql = """
            SELECT pt.pet_type_id, 
                   COALESCE(pm.pet_name, pt.pet_name) as pet_name, 
                   pt.species, pt.required_experience, 
                   pt.gif_filename, pt.personality, pt.working_activity,
                   u.experience as user_experience,
                   CASE WHEN pm.pet_type_id IS NOT NULL THEN 1 ELSE 0 END as unlocked,
                   pm.is_equipped
            FROM PetTypes pt
            CROSS JOIN Users u
            LEFT JOIN PetMascot pm ON pt.pet_type_id = pm.pet_type_id AND pm.user_id = u.user_id
            WHERE u.user_id = ?
            ORDER BY pt.required_experience
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = new Pet(
                            rs.getInt("pet_type_id"),
                            rs.getString("pet_name"),
                            rs.getString("species"),
                            rs.getInt("required_experience"),
                            rs.getString("gif_filename"),
                            rs.getString("personality"),
                            rs.getString("working_activity"),
                            rs.getInt("user_experience")
                    );
                    pet.setUnlocked(rs.getBoolean("unlocked"));
                    pet.setEquipped(rs.getBoolean("is_equipped"));
                    pets.add(pet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all pets: " + e.getMessage());
            e.printStackTrace();
        }
        return pets;
    }

    // Equip a pet
    public boolean equipPet(int petTypeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // First update Users table
                String updateUserSql = "UPDATE Users SET current_pet_id = ? WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                    stmt.setInt(1, petTypeId);
                    stmt.setInt(2, currentUserId);
                    stmt.executeUpdate();
                }

                // Then update PetMascot to set this pet as equipped and others as not equipped
                String updateMascotSql = "UPDATE PetMascot SET is_equipped = CASE WHEN pet_type_id = ? THEN 1 ELSE 0 END WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateMascotSql)) {
                    stmt.setInt(1, petTypeId);
                    stmt.setInt(2, currentUserId);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error equipping pet: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Check and unlock new pets based on user experience
    public void checkAndUnlockPets() {
        String sql = """
            INSERT INTO PetMascot (user_id, pet_type_id, pet_name)
            SELECT ?, pt.pet_type_id, pt.pet_name
            FROM PetTypes pt
            WHERE pt.required_experience <= (SELECT experience FROM Users WHERE user_id = ?)
            AND pt.pet_type_id NOT IN (
                SELECT pet_type_id FROM PetMascot WHERE user_id = ?
            )
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);

            int unlocked = stmt.executeUpdate();
            if (unlocked > 0) {
                System.out.println("Unlocked " + unlocked + " new pets for user " + currentUserId);
            }
        } catch (SQLException e) {
            System.err.println("Error unlocking pets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get user experience
    public int getUserExperience() {
        String sql = "SELECT experience FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("experience");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user experience: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Get user badges
    // In the PetsController class, update the getUserBadges method and Badge class:

    // Get user badges
    public List<Badge> getUserBadges() {
        List<Badge> badges = new ArrayList<>();
        String sql = """
        SELECT b.badge_id, b.name, b.description, b.condition_type, b.condition_value,
               CASE WHEN ub.badge_id IS NOT NULL THEN 1 ELSE 0 END as earned,
               ub.earned_date,
               -- Calculate progress for each badge
               CASE b.condition_type
                   WHEN 'tasks_completed' THEN (SELECT COUNT(*) FROM ToDoTasks WHERE user_id = ? AND is_completed = 1)
                   WHEN 'pomodoro_sessions' THEN (SELECT COUNT(*) FROM PomodoroSessions WHERE user_id = ? AND status = 'Completed')
                   WHEN 'notes_created' THEN (SELECT COUNT(*) FROM StickyNotes WHERE user_id = ?)
                   WHEN 'mood_entries' THEN (SELECT COUNT(*) FROM MoodLogger WHERE user_id = ?)
                   ELSE 0
               END as current_progress
        FROM Badges b
        LEFT JOIN UserBadges ub ON b.badge_id = ub.badge_id AND ub.user_id = ?
        ORDER BY b.condition_type, b.condition_value
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            stmt.setInt(4, currentUserId);
            stmt.setInt(5, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    badges.add(new Badge(
                            rs.getInt("badge_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("condition_type"),
                            rs.getInt("condition_value"),
                            rs.getBoolean("earned"),
                            rs.getTimestamp("earned_date"),
                            rs.getInt("current_progress")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching badges: " + e.getMessage());
            e.printStackTrace();
        }
        return badges;
    }

    // Updated Badge data class
    public static class Badge {
        private int badgeId;
        private String name;
        private String description;
        private String conditionType;
        private int conditionValue;
        private boolean earned;
        private Timestamp earnedDate;
        private int currentProgress;

        public Badge(int badgeId, String name, String description, String conditionType,
                     int conditionValue, boolean earned, Timestamp earnedDate, int currentProgress) {
            this.badgeId = badgeId;
            this.name = name;
            this.description = description;
            this.conditionType = conditionType;
            this.conditionValue = conditionValue;
            this.earned = earned;
            this.earnedDate = earnedDate;
            this.currentProgress = currentProgress;
        }

        // Getters
        public int getBadgeId() { return badgeId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getConditionType() { return conditionType; }
        public int getConditionValue() { return conditionValue; }
        public boolean isEarned() { return earned; }
        public Timestamp getEarnedDate() { return earnedDate; }
        public int getCurrentProgress() { return currentProgress; }

        // Helper method to get progress percentage
        public int getProgressPercentage() {
            if (earned) return 100;
            return Math.min(100, (currentProgress * 100) / conditionValue);
        }

        // Helper method to get progress text
        public String getProgressText() {
            if (earned) {
                return "Completed!";
            }
            return currentProgress + "/" + conditionValue;
        }
    }

    public PetInfo getCurrentPetForSidebar() {
        PetsController.Pet currentPet = getCurrentPet();
        if (currentPet != null) {
            return new PetInfo(currentPet.getName(), currentPet.getSpecies(), currentPet.getGifFilename());
        }
        return new PetInfo("Luna", "Cat", "cat.gif"); // Default fallback
    }

    // Simple data class for sidebar pet info
    public static class PetInfo {
        private final String name;
        private final String species;
        private final String gifFilename;

        public PetInfo(String name, String species, String gifFilename) {
            this.name = name;
            this.species = species;
            this.gifFilename = gifFilename;
        }

        public String getName() { return name; }
        public String getSpecies() { return species; }
        public String getGifFilename() { return gifFilename; }

        public String getDisplayName() {
            return name + " the " + species;
        }
    }

    // Pet data class
    public static class Pet {
        private int petTypeId;
        private String name;
        private String species;
        private int requiredExperience;
        private String gifFilename;
        private String personality;
        private String workingActivity;
        private int userExperience;
        private boolean unlocked;
        private boolean equipped;

        public Pet(int petTypeId, String name, String species, int requiredExperience,
                   String gifFilename, String personality, String workingActivity, int userExperience) {
            this.petTypeId = petTypeId;
            this.name = name;
            this.species = species;
            this.requiredExperience = requiredExperience;
            this.gifFilename = gifFilename;
            this.personality = personality;
            this.workingActivity = workingActivity;
            this.userExperience = userExperience;
        }

        // Getters and setters
        public int getPetTypeId() { return petTypeId; }
        public String getName() { return name; }
        public String getSpecies() { return species; }
        public int getRequiredExperience() { return requiredExperience; }
        public String getGifFilename() { return gifFilename; }
        public String getPersonality() { return personality; }
        public String getWorkingActivity() { return workingActivity; }
        public int getUserExperience() { return userExperience; }
        public boolean isUnlocked() { return unlocked; }
        public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
        public boolean isEquipped() { return equipped; }
        public void setEquipped(boolean equipped) { this.equipped = equipped; }

        public boolean canUnlock() {
            return userExperience >= requiredExperience;
        }

        public int getRemainingExperience() {
            return Math.max(0, requiredExperience - userExperience);
        }
    }
}