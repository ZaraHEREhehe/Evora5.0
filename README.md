# Evora

Evora is a JavaFX desktop productivity companion that combines task planning, Pomodoro focus sessions, mood tracking, sticky notes, analytics, calendar planning, white-noise playback, themes, and a gamified pet companion system. The app is designed around a soft pastel productivity experience where users earn experience points, unlock companion pets, and track their progress through daily work habits.

## Preview

### Login

![Evora login screen](images/Screenshot%20%281256%29.png)

### Dashboard

![Evora dashboard](images/Screenshot%20%281257%29.png)

### About Section

![Evora about section](images/Screenshot%20%281258%29.png)

### Productivity Modules

![Evora analytics, to-do list, calendar, timer, and notes screens](images/Screenshot%20%281262%29.png)

### Wellness, Pets, and White Noise

![Evora mood tracker, companion pet module, and white-noise player](images/Screenshot%20%281259%29.png)

## Core Features

| Module | What it does |
| --- | --- |
| Authentication | Supports login and signup through SQL Server-backed user records. New users are assigned the default pastel theme and a starter pet. |
| Dashboard | Shows daily productivity stats, completed tasks, Pomodoro count, total notes, average mood, main task, weekly progress, and quick navigation buttons. |
| To-Do List | Lets users create, prioritize, complete, delete, and reorder tasks. Tasks can include due dates and are used by the calendar and analytics modules. |
| Calendar | Provides a monthly task calendar that visualizes task due dates and priority-colored events. |
| Pomodoro Timer | Tracks focus sessions with work and break durations, running/paused/completed states, persistent session records, and XP rewards on completion. |
| Sticky Notes | Provides a digital corkboard for draggable notes with saved position, content, and color. |
| Mood Tracker | Lets users log daily mood values and notes, then review mood history and trends. |
| Analytics | Summarizes completion rate, focus time, streaks, productivity score, weekly activity, mood trends, task stats, Pomodoro sessions, notes, and badges. |
| Companion Pet | Lets users equip, rename, and unlock pets based on experience. Pets appear in the sidebar and Pomodoro experience. |
| White Noise | Plays built-in focus sounds such as rain, coffee shop, ocean waves, wind, forest, fireplace, and piano ambience. |
| Settings | Allows theme switching, username updates, password changes, and access to app information. |
| Themes | Includes pastel and galaxy themes through a shared theme manager. |

## Tech Stack

- Java 24 source/target level
- JavaFX 21.0.6 for the desktop UI
- Maven Wrapper for build and run commands
- Microsoft SQL Server JDBC driver for persistence
- ControlsFX, FormsFX, ValidatorFX, BootstrapFX, Ikonli, and TilesFX UI dependencies
- JavaFX Media for welcome video/audio and white-noise playback

## Project Structure

```text
Evora5.0/
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- module-info.java
|   |   |   `-- com/example/Evora/
|   |   |       |-- HelloApplication.java
|   |   |       |-- MainController.java
|   |   |       |-- Dashboard.java
|   |   |       |-- Settings.java
|   |   |       |-- Analytics/
|   |   |       |-- Calendar/
|   |   |       |-- Database/
|   |   |       |-- Login/
|   |   |       |-- Mood/
|   |   |       |-- Notes/
|   |   |       |-- Pets/
|   |   |       |-- Pomodoro/
|   |   |       |-- Sidebar/
|   |   |       |-- Theme/
|   |   |       |-- ToDoList/
|   |   |       `-- WhiteNoise/
|   |   `-- resources/
|   |       |-- database.properties
|   |       |-- Images/
|   |       |-- pet_gifs/
|   |       |-- Sounds/
|   |       `-- Welcome/
```

## Important Packages

| Package | Purpose |
| --- | --- |
| `com.example.Evora` | App entry point, main navigation controller, dashboard, and settings screen. |
| `Login` | Login, signup, validation, SQL Server authentication, default theme setup, and default pet creation. |
| `Database` | Central database configuration and connection helper. |
| `Sidebar` | Shared navigation rail, active tab handling, user greeting, current pet display, XP refresh, and logout access. |
| `ToDoList` | Task model, task CRUD, priority handling, due dates, ordering, completion, deletion, and XP rewards. |
| `Calendar` | Calendar view and task date integration. |
| `Pomodoro` | Timer UI, session state, persistent Pomodoro session manager, pause/resume/complete/abort behavior, and XP rewards. |
| `Notes` | Sticky note creation, editing, color storage, board positioning, movement, deletion, and XP rewards. |
| `Mood` | Mood logging, daily upsert behavior, mood notes, trend data, and wellness stats. |
| `Analytics` | Productivity metrics, weekly activity, focus time, completion rates, streaks, mood averages, and badge progress. |
| `Pets` | Pet collection, equipped pet state, unlock rules, pet renaming, current pet display, and badge progress. |
| `WhiteNoise` | Singleton white-noise controller/view and built-in audio playback. |
| `Theme` | Pastel and galaxy color systems, theme interface, theme manager, and database-backed theme persistence. |

## Source File Map

```text
src/main/java/
|-- module-info.java
`-- com/example/Evora/
    |-- Dashboard.java
    |-- HelloApplication.java
    |-- MainController.java
    |-- Settings.java
    |-- Analytics/
    |   |-- AnalyticsController.java
    |   `-- AnalyticsView.java
    |-- Calendar/
    |   |-- CalendarController.java
    |   `-- CalendarView.java
    |-- Database/
    |   |-- DatabaseConfig.java
    |   `-- DatabaseConnection.java
    |-- Login/
    |   |-- LoginController.java
    |   `-- LoginView.java
    |-- Mood/
    |   |-- MoodController.java
    |   `-- MoodView.java
    |-- Notes/
    |   |-- NotesController.java
    |   `-- NotesView.java
    |-- Pets/
    |   |-- PetsController.java
    |   `-- PetsView.java
    |-- Pomodoro/
    |   |-- PomodoroController.java
    |   |-- PomodoroSessionManager.java
    |   `-- PomodoroView.java
    |-- Sidebar/
    |   |-- Sidebar.java
    |   `-- SidebarController.java
    |-- Theme/
    |   |-- Galaxy.java
    |   |-- GalaxyTheme.java
    |   |-- Pastel.java
    |   |-- PastelTheme.java
    |   |-- Theme.java
    |   |-- ThemeManager.java
    |   `-- ThemeService.java
    |-- ToDoList/
    |   |-- TodoController.java
    |   `-- TodoView.java
    `-- WhiteNoise/
        |-- WhiteNoiseController.java
        `-- WhiteNoiseView.java
```

## Resources

| Folder | Contents |
| --- | --- |
| `src/main/resources/Images` | 4 dashboard stat card icons for tasks, timer, notes, and mood. |
| `src/main/resources/pet_gifs` | 26 animated pet assets for cats, bunnies, owls, dragons, and color variants. |
| `src/main/resources/Sounds` | 8 white-noise and ambience audio files. |
| `src/main/resources/Welcome` | Welcome video shown by the login/welcome experience. |
| `src/main/resources/database.properties` | SQL Server connection settings used by `DatabaseConfig`. |

## Database

Evora stores app data in SQL Server. The code references these main tables:

- `Users`
- `ToDoTasks`
- `TaskDeletionLog`
- `PomodoroSessions`
- `StickyNotes`
- `MoodLogger`
- `PetTypes`
- `PetMascot`
- `Badges`
- `UserBadges`
- user theme storage used through `ThemeService`

The app expects a database named `EvoraDB` by default, although the exact database name can be changed in `database.properties`.

Example local configuration:

```properties
db.url=jdbc:sqlserver://YOUR_HOST\\SQLEXPRESS:1433;databaseName=EvoraDB;encrypt=false
db.username=your_username
db.password=your_password
db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

For Windows Authentication, `DatabaseConnection` also supports an empty username and password if the JDBC URL and SQL Server setup allow it.

## Prerequisites

- JDK 24
- SQL Server or SQL Server Express
- TCP/IP enabled for SQL Server, commonly on port `1433`
- A configured `EvoraDB` database with the tables used by the app
- Maven is optional because the project includes Maven Wrapper scripts

## Run Locally

From the project root:

```powershell
.\mvnw.cmd clean javafx:run
```

On macOS/Linux:

```bash
./mvnw clean javafx:run
```

The JavaFX Maven plugin runs:

```text
com.example.demo1/com.example.Evora.HelloApplication
```

## Build

```powershell
.\mvnw.cmd clean package
```

## Test

```powershell
.\mvnw.cmd test
```

The project includes JUnit 5 dependencies, but no dedicated test source tree is currently present in the inspected structure.

## User Flow

1. A user opens the app and lands on the login screen.
2. Existing users sign in with email and password.
3. New users sign up with username, email, and password.
4. On signup, Evora creates the user, saves the default pastel theme, and assigns the default pet.
5. After login, `HelloApplication` hands control to `MainController`.
6. `MainController` builds the shared layout with sidebar navigation and loads the dashboard.
7. Feature views are swapped into the center of the app while the sidebar remains available.
8. Productive actions update SQL Server records and can increase the user's XP.
9. XP unlocks more pets and contributes to visible progress across the dashboard, sidebar, analytics, and pet module.

## Experience Points and Progression

Evora rewards productive actions with XP. Based on the current implementation:

- Completing a Pomodoro session awards XP.
- Creating sticky notes awards XP.
- Task actions can award XP based on priority.
- Pet unlocks are based on the user's total experience.
- Badges track progress across tasks, Pomodoro sessions, notes, and mood entries.

## Themes

Evora uses a shared theme system:

- `PastelTheme` provides the soft pink, mint, lavender, blue, peach, and cream visual language.
- `GalaxyTheme` provides a darker cosmic alternative.
- `ThemeManager` stores the active theme and notifies theme-aware screens.
- `ThemeService` persists user theme choices in the database.

## Notes for Contributors

- The app is built mostly with Java code-based JavaFX views rather than FXML.
- `MainController` owns navigation and keeps the Pomodoro controller alive across tab switches.
- Database access is performed directly with JDBC and prepared statements.
- The app currently stores passwords as plain text in the database; use hashing before production use.
- Keep real database credentials out of commits when sharing the project.
- The `pom.xml` currently declares the SQL Server JDBC dependency twice; Maven can still resolve it, but it can be cleaned up later.
- The generated `out/` and IDE metadata directories under `src/main/java/com/example/Evora` appear to be local build/IDE artifacts and are not part of the app architecture.

## Project Identity

Evora is positioned as a magical productivity companion: part planner, part wellness logger, part focus tool, and part playful pet progression system. Its main goal is to make daily productivity feel softer, more visual, and more rewarding.
