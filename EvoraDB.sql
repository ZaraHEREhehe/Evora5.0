CREATE LOGIN evora_user WITH PASSWORD = 'password123';
USE EvoraDB;
CREATE USER evora_user FOR LOGIN evora_user;
ALTER ROLE db_owner ADD MEMBER evora_user;

CREATE DATABASE EvoraDB
GO
USE EvoraDB
GO

-- Create Users table
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100), 
    password VARCHAR(50),
    created_at DATETIME DEFAULT GETDATE(),
    experience INT DEFAULT 0,
    level INT DEFAULT 1,
    tokens INT DEFAULT 0
);

-- Create Themes table
CREATE TABLE Themes (
    theme_id INT IDENTITY(1,1) PRIMARY KEY,
    theme_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Create ThemeColors table
CREATE TABLE ThemeColors (
    color_id INT IDENTITY(1,1) PRIMARY KEY,
    theme_id INT NOT NULL,
    color_name VARCHAR(50),
    color_hex CHAR(7) CHECK (color_hex LIKE '#______'),
    CONSTRAINT FK_Theme_ThemeColors 
        FOREIGN KEY (theme_id) REFERENCES Themes(theme_id) ON DELETE CASCADE
);

-- Create Sounds table
CREATE TABLE Sounds (
    sound_id INT IDENTITY(1,1) PRIMARY KEY,
    sound_name VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    loop_enabled BIT DEFAULT 1,
    default_volume INT DEFAULT 50 CHECK (default_volume BETWEEN 0 AND 100)
);

-- Create PomodoroSessions table
CREATE TABLE PomodoroSessions (
    session_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    preset_name VARCHAR(50),
    work_duration INT NOT NULL,
    break_duration INT,
    start_time DATETIME DEFAULT GETDATE(),
    end_time DATETIME,
    status VARCHAR(20) CHECK (status IN ('Running','Paused','Completed','Aborted')),
    CONSTRAINT FK_PomodoroSessions_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create Badges table
CREATE TABLE Badges (
    badge_id INT IDENTITY(1,1) PRIMARY KEY,
    badge_icon VARCHAR(200), 
    name VARCHAR(100) NOT NULL,
    description TEXT,
    xp_required INT NOT NULL
);

-- Create ToDoTasks table
CREATE TABLE ToDoTasks (
    task_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    description TEXT,
    priority VARCHAR(10) CHECK (priority IN ('Low','Medium','High')),
    due_date DATE,
    is_completed BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    completed_at DATETIME NULL,
    CONSTRAINT FK_ToDoTasks_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create SessionLogs table
CREATE TABLE SessionLogs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    session_id INT NOT NULL,
    productivity_score INT,
    focus_time INT,
    interruptions INT DEFAULT 0,
    created_date DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_SessionLogs_Session 
        FOREIGN KEY (session_id) REFERENCES PomodoroSessions(session_id) ON DELETE CASCADE
);

-- Create StickyNotes table
CREATE TABLE StickyNotes (
    note_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    content TEXT,
    color_id INT DEFAULT 1,
    position_x FLOAT DEFAULT 0,
    position_y FLOAT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_StickyNotes_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_StickyNotes_Colors 
        FOREIGN KEY (color_id) REFERENCES ThemeColors(color_id) ON DELETE SET NULL
);

-- Create PetMascot table
CREATE TABLE PetMascot (
    pet_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    pet_gif VARCHAR(200) DEFAULT 'ourcatgif.com',
    pet_name VARCHAR(50) DEFAULT 'Évoro',
    experience INT DEFAULT 0,
    level INT DEFAULT 1,
    tokens_cost INT DEFAULT 1,
    CONSTRAINT FK_PetMascot_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create UserBadges table
CREATE TABLE UserBadges (
    user_id INT,
    badge_id INT,
    earned_date DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, badge_id),
    CONSTRAINT FK_UserBadges_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserBadges_Badge 
        FOREIGN KEY (badge_id) REFERENCES Badges(badge_id) ON DELETE CASCADE
);

-- Create MoodLogger table
CREATE TABLE MoodLogger (
    mood_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    mood_icon VARCHAR(200),
    mood_value INT CHECK (mood_value BETWEEN 1 AND 5),
    note VARCHAR(300),
    entry_date DATE DEFAULT GETDATE(),
    CONSTRAINT FK_MoodLogger_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create Alerts table
CREATE TABLE Alerts (
    alert_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    message VARCHAR(255) NOT NULL,
    alert_type VARCHAR(30) CHECK (alert_type IN ('TimerEnd','BreakReminder','Mascot','Custom')),
    created_at DATETIME DEFAULT GETDATE(),
    sound_id INT,
    CONSTRAINT FK_Alerts_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Alerts_Sound 
        FOREIGN KEY (sound_id) REFERENCES Sounds(sound_id) ON DELETE SET NULL
);

-- Create Preferences table
CREATE TABLE Preferences (
    pref_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    theme VARCHAR(20) DEFAULT 'Light',
    notifications_enabled BIT DEFAULT 1,
    auto_start_timer BIT DEFAULT 0,
    default_sound_id INT NULL,
    CONSTRAINT FK_Preferences_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Preferences_Sound 
        FOREIGN KEY (default_sound_id) REFERENCES Sounds(sound_id) ON DELETE SET NULL
);



/******************************************************
*** 	     	Important Changes                   ***
*******************************************************/

-- default theme for sticky notes
INSERT INTO Themes (theme_name, description) 
VALUES ('StickyNotes', 'Default sticky notes color theme');

-- 6 sticky note colors will always be the same
INSERT INTO ThemeColors (theme_id, color_name, color_hex) VALUES
(1, 'Yellow', '#fef08a'),
(1, 'Pink', '#fecaca'),
(1, 'Green', '#bbf7d0'),
(1, 'Blue', '#bfdbfe'),
(1, 'Purple', '#e9d5ff'),
(1, 'Orange', '#fed7aa');

-- dummy user
INSERT INTO Users(username, email, password)
VALUES ('test', 'test', 'test')



-- ADDED sort_order column to enable drag and drop feature

-- Drop the table if it exists to start fresh
IF OBJECT_ID('ToDoTasks', 'U') IS NOT NULL
    DROP TABLE ToDoTasks;

-- Create the ToDoTasks table with proper structure
CREATE TABLE ToDoTasks (
    task_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    description NVARCHAR(500) NOT NULL,
    priority NVARCHAR(10) NOT NULL CHECK (priority IN ('Low', 'Medium', 'High')),
    due_date DATE NULL,
    is_completed BIT NOT NULL DEFAULT 0,
    completed_at DATETIME NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Insert Sample To-Do Tasks for user_id = 1 with proper sort_order
INSERT INTO ToDoTasks (user_id, description, priority, due_date, is_completed, completed_at, sort_order)
VALUES
(1, 'Review presentation slides', 'High', '2025-09-04', 0, NULL, 1),
(1, 'Call team meeting', 'Medium', '2025-09-03', 1, '2025-09-03 14:30:00', 2),
(1, 'Organize desk workspace', 'Low', NULL, 0, NULL, 3),
(1, 'Submit expense reports', 'High', '2025-09-05', 0, NULL, 4),
(1, 'Plan weekend trip', 'Low', '2025-09-07', 0, NULL, 5),
(1, 'Update project documentation', 'Medium', '2025-09-06', 0, NULL, 6),
(1, 'Email client feedback', 'High', '2025-09-02', 1, '2025-09-02 10:15:00', 7),
(1, 'Backup important files', 'Medium', NULL, 0, NULL, 8);

Select * from ToDoTasks


-- for mood module
INSERT INTO MoodLogger(user_id, mood_icon, mood_value, note, entry_date)
VALUES (1, '??', 1, 'hate it here', '2025-11-10'),
(1, '??', 3, 'meow', '2025-11-11'),
(1, '??', 2,'' , '2025-11-12'),
(1, '??', 5, '', '2025-11-16');

-- extra column for pomodoro sessions
ALTER TABLE PomodoroSessions ADD completed_cycles INT DEFAULT 1;
ALTER TABLE PomodoroSessions ADD last_pause_time DATETIME NULL;
-- Add columns to track current state for persistence
ALTER TABLE PomodoroSessions ADD current_time_left INT NULL;
ALTER TABLE PomodoroSessions ADD is_break BIT DEFAULT 0;
ALTER TABLE PomodoroSessions ADD is_running BIT DEFAULT 0;

select * from PomodoroSessions

/****************************************************
*** 			      Pets                        ***
*****************************************************/
-- for pets
ALTER TABLE Users ADD current_pet_id INT DEFAULT 1;

CREATE TABLE PetTypes (
    pet_type_id INT IDENTITY(1,1) PRIMARY KEY,
    pet_name VARCHAR(50) NOT NULL,
    species VARCHAR(50) NOT NULL,
    required_experience INT NOT NULL,
    gif_filename VARCHAR(100) NOT NULL,
    personality VARCHAR(200),
    working_activity VARCHAR(200)
);

INSERT INTO PetTypes (pet_name, species, required_experience, gif_filename, personality, working_activity) VALUES
('Luna', 'Cat', 0, 'cat.gif', 'Curious and independent', 'Typing on tiny keyboard'),
('Pink Luna', 'Cat', 500, 'pinkcat_transparentbg.gif', 'Sweet and affectionate', 'Playing with yarn'),
('Blue Luna', 'Cat', 1000, 'bluecat_transparentbg.gif', 'Calm and observant', 'Napping in sunbeams'),
('Orange Luna', 'Cat', 1500, 'orangecat_transparentbg.gif', 'Playful and energetic', 'Chasing laser pointers'),

('Cocoa', 'Bunny', 500, 'bunny.gif', 'Energetic and helpful', 'Organizing papers with tiny paws'),
('Pink Cocoa', 'Bunny', 1000, 'pinkbunny_transparentbg.gif', 'Gentle and caring', 'Sorting colorful papers'),
('Purple Cocoa', 'Bunny', 3000, 'purplebunny_transparentbg.gif', 'Creative and artistic', 'Drawing with tiny pencils'),
('Green Cocoa', 'Bunny', 1500, 'greenbunny_transparentbg.gif', 'Adventurous and bold', 'Exploring new folders'),

('Hoot', 'Owl', 2000, 'owl.gif', 'Wise and studious', 'Reading miniature books'),
('Purple Hoot', 'Owl', 3000, 'purpleowl_transparentbg.gif', 'Mysterious and insightful', 'Studying ancient texts'),
('Yellow Hoot', 'Owl', 3500, 'yellowowl_transparentbg.gif', 'Cheerful and optimistic', 'Organizing knowledge'),
('Mauve Hoot', 'Owl', 4000, 'mauveowl_transparentbg.gif', 'Philosophical and deep', 'Contemplating big ideas'),
('Gray Hoot', 'Owl', 3500, 'grayowl_transparentbg.gif', 'Serious and focused', 'Researching complex topics'),

('Sage', 'Dragon', 4000, 'dragon.gif', 'Mystical and protective', 'Breathing gentle focus flames'),
('Purple Sage', 'Dragon', 4500, 'purpledragon_transparentbg.gif', 'Magical and enchanting', 'Casting focus spells'),
('Gray Sage', 'Dragon', 5000, 'graydragon_transparentbg.gif', 'Ancient and powerful', 'Guarding your progress'),
('Orange Sage', 'Dragon', 5000, 'orangedragon_transparentbg.gif', 'Fiery and passionate', 'Inspiring creativity');

update PetTypes
set gif_filename = 'brownowl_transparentbg.gif'
where gif_filename = 'mauveowl_transparentbg.gif'

update PetTypes
set pet_name = 'Brown Hoot'
where gif_filename = 'brownowl_transparentbg.gif'

update PetTypes
set gif_filename = 'redowl_transparentbg.gif'
where gif_filename = 'grayowl_transparentbg.gif'

update PetTypes
set pet_name = 'Red Hoot'
where gif_filename = 'redowl_transparentbg.gif'

-- Update PetMascot table to properly handle equipped pets

-- Make sure only one pet is equipped per user
CREATE OR ALTER TRIGGER trg_SingleEquippedPet
ON PetMascot
AFTER INSERT, UPDATE
AS
BEGIN
    IF UPDATE(is_equipped)
    BEGIN
        UPDATE pm
        SET pm.is_equipped = 0
        FROM PetMascot pm
        INNER JOIN inserted i ON pm.user_id = i.user_id
        WHERE pm.pet_type_id != i.pet_type_id
        AND i.is_equipped = 1;
    END
END;


-- for changing pet name
ALTER TABLE PetMascot ADD pet_name VARCHAR(50) NULL;

-- Update existing records
UPDATE pm
SET pm.pet_name = pt.pet_name
FROM PetMascot pm
INNER JOIN PetTypes pt ON pm.pet_type_id = pt.pet_type_id;



select * from petmascot
		
-- Drop existing table and recreate
DROP TABLE PetMascot;

CREATE TABLE PetMascot (
    user_id INT,
    pet_type_id INT,
    unlocked_at DATETIME DEFAULT GETDATE(),
    is_equipped BIT DEFAULT 0,
    PRIMARY KEY (user_id, pet_type_id),
    CONSTRAINT FK_PetMascot_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_PetMascot_Type 
        FOREIGN KEY (pet_type_id) REFERENCES PetTypes(pet_type_id)
);


/****************************************************
*** 		   Badges Walay Queries               ***
*****************************************************/
select * from Badges
-- Update the Badges table structure
DROP TABLE IF EXISTS UserBadges;

DROP TABLE IF EXISTS Badges;

CREATE TABLE Badges (
    badge_id INT IDENTITY(1,1) PRIMARY KEY,
    badge_icon VARCHAR(200), 
    name VARCHAR(100) NOT NULL,
    description TEXT,
    condition_type VARCHAR(50) NOT NULL, -- 'tasks_completed', 'pomodoro_sessions', 'notes_created', 'mood_entries', 'login_streak'
    condition_value INT NOT NULL, -- The threshold value for the condition
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE UserBadges (
    user_id INT,
    badge_id INT,
    earned_date DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, badge_id),
    CONSTRAINT FK_UserBadges_User 
        FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserBadges_Badge 
        FOREIGN KEY (badge_id) REFERENCES Badges(badge_id) ON DELETE CASCADE
);

-- Insert badges with conditions
INSERT INTO Badges (badge_icon, name, description, condition_type, condition_value) VALUES
('🌟', 'First Steps', 'Complete your first task', 'tasks_completed', 1),
('⚡', 'Focus Master', 'Complete pomodoro sessions', 'pomodoro_sessions', 5),
('📝', 'Note Taker', 'Create sticky notes', 'notes_created', 10),
('😊', 'Mood Tracker', 'Log your mood entries', 'mood_entries', 20),
('👑', 'Task Champion', 'Complete many tasks', 'tasks_completed', 50),
('🏆', 'Productivity Guru', 'Complete many pomodoro sessions', 'pomodoro_sessions', 25),
('🔥', 'Consistent Logger', 'Maintain mood tracking streak', 'mood_entries', 50),
('📚', 'Note Archivist', 'Create many notes', 'notes_created', 25);

-- Create a function to check and award badges
CREATE OR ALTER FUNCTION CheckBadgeEligibility(@user_id INT, @condition_type VARCHAR(50))
RETURNS @EligibleBadges TABLE (badge_id INT)
AS
BEGIN
    INSERT INTO @EligibleBadges
    SELECT b.badge_id
    FROM Badges b
    WHERE b.condition_type = @condition_type
    AND b.badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id)
    AND b.condition_value <= (
        CASE 
            WHEN @condition_type = 'tasks_completed' THEN 
                (SELECT COUNT(*) FROM ToDoTasks WHERE user_id = @user_id AND is_completed = 1)
            WHEN @condition_type = 'pomodoro_sessions' THEN 
                (SELECT COUNT(*) FROM PomodoroSessions WHERE user_id = @user_id AND status = 'Completed')
            WHEN @condition_type = 'notes_created' THEN 
                (SELECT COUNT(*) FROM StickyNotes WHERE user_id = @user_id)
            WHEN @condition_type = 'mood_entries' THEN 
                (SELECT COUNT(*) FROM MoodLogger WHERE user_id = @user_id)
            ELSE 0
        END
    );
    
    RETURN;
END;


-- Trigger for task completion
CREATE OR ALTER TRIGGER trg_AwardTaskBadges
ON ToDoTasks
AFTER UPDATE
AS
BEGIN
    IF UPDATE(is_completed)
    BEGIN
        DECLARE @user_id INT;
        SELECT @user_id = user_id FROM inserted;
        
        -- Only proceed if task was marked as completed
        IF EXISTS (SELECT 1 FROM inserted WHERE is_completed = 1)
        BEGIN
            INSERT INTO UserBadges (user_id, badge_id)
            SELECT @user_id, badge_id 
            FROM dbo.CheckBadgeEligibility(@user_id, 'tasks_completed')
            WHERE badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id);
        END
    END
END;

-- Trigger for pomodoro session completion
CREATE OR ALTER TRIGGER trg_AwardPomodoroBadges
ON PomodoroSessions
AFTER UPDATE
AS
BEGIN
    IF UPDATE(status)
    BEGIN
        DECLARE @user_id INT;
        SELECT @user_id = user_id FROM inserted;
        
        -- Only proceed if session was completed
        IF EXISTS (SELECT 1 FROM inserted WHERE status = 'Completed')
        BEGIN
            INSERT INTO UserBadges (user_id, badge_id)
            SELECT @user_id, badge_id 
            FROM dbo.CheckBadgeEligibility(@user_id, 'pomodoro_sessions')
            WHERE badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id);
        END
    END
END;

-- Trigger for sticky note creation
CREATE OR ALTER TRIGGER trg_AwardNoteBadges
ON StickyNotes
AFTER INSERT
AS
BEGIN
    DECLARE @user_id INT;
    SELECT @user_id = user_id FROM inserted;
    
    INSERT INTO UserBadges (user_id, badge_id)
    SELECT @user_id, badge_id 
    FROM dbo.CheckBadgeEligibility(@user_id, 'notes_created')
    WHERE badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id);
END;

-- Trigger for mood logging
CREATE OR ALTER TRIGGER trg_AwardMoodBadges
ON MoodLogger
AFTER INSERT
AS
BEGIN
    DECLARE @user_id INT;
    SELECT @user_id = user_id FROM inserted;
    
    INSERT INTO UserBadges (user_id, badge_id)
    SELECT @user_id, badge_id 
    FROM dbo.CheckBadgeEligibility(@user_id, 'mood_entries')
    WHERE badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id);
END;

-- Stored procedure to check and award all eligible badges (useful for initial setup)
CREATE OR ALTER PROCEDURE CheckAllBadgesForUser
    @user_id INT
AS
BEGIN
    -- Check all condition types
    DECLARE @condition_types TABLE (condition_type VARCHAR(50));
    INSERT INTO @condition_types VALUES 
    ('tasks_completed'), ('pomodoro_sessions'), ('notes_created'), ('mood_entries');
    
    DECLARE @current_condition VARCHAR(50);
    
    DECLARE condition_cursor CURSOR FOR 
    SELECT condition_type FROM @condition_types;
    
    OPEN condition_cursor;
    FETCH NEXT FROM condition_cursor INTO @current_condition;
    
    WHILE @@FETCH_STATUS = 0
    BEGIN
        INSERT INTO UserBadges (user_id, badge_id)
        SELECT @user_id, badge_id 
        FROM dbo.CheckBadgeEligibility(@user_id, @current_condition)
        WHERE badge_id NOT IN (SELECT badge_id FROM UserBadges WHERE user_id = @user_id);
        
        FETCH NEXT FROM condition_cursor INTO @current_condition;
    END
    
    CLOSE condition_cursor;
    DEALLOCATE condition_cursor;
END;

EXEC CheckAllBadgesForUser @user_id = 1;


/****************************************************
*** 			New Notes Table                   ***
*****************************************************/

-- Create DeletedStickyNotes table to preserve analytics data
CREATE TABLE DeletedStickyNotes (
    deleted_note_id INT IDENTITY(1,1) PRIMARY KEY,
    original_note_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME,
);


-- Add trigger to automatically move deleted notes to the archive, but only the info we need
CREATE OR ALTER TRIGGER trg_ArchiveDeletedNotes
ON StickyNotes
AFTER DELETE
AS
BEGIN
    INSERT INTO DeletedStickyNotes (original_note_id, user_id, created_at)
    SELECT 
        note_id, user_id, created_at
    FROM deleted;
END;

/****************************************************
*** 			Useless Queries                   ***
*****************************************************/



select * from StickyNotes
select * from ThemeColors
select * from Themes

Select note_id, user_id, content, color_name, position_x, position_y, created_at
From StickyNotes s
Inner Join ThemeColors ts on s.color_id = ts.color_id

select * from users
select * from Badges
select * from UserBadges
select * from PetMascot


select * from PomodoroSessions
select * from Users
sp_help PomodoroSessions
sp_help Users



select * from PomodoroSessions

update Users
set current_pet_id = 1
where user_id = 1


select * from users
select * from petmascot