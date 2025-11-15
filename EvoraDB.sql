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

/* actual important changes */

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

select * from Users

INSERT INTO Users(username, email, password)
VALUES ('test', 'test', 'test')


select * from StickyNotes
select * from ThemeColors
select * from Themes

Select note_id, user_id, content, color_name, position_x, position_y, created_at
From StickyNotes s
Inner Join ThemeColors ts on s.color_id = ts.color_id