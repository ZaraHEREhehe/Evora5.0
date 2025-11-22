package com.example.demo1.Pomodoro;

import com.example.demo1.Pets.PetsController;
import com.example.demo1.Sidebar.Sidebar;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PomodoroController {

    private Timeline timeline;
    private boolean running = false;
    private boolean isBreak = false;
    private int timeLeft;
    private int workTime = 25 * 60;
    private int breakTime = 5 * 60;
    private int happiness = 85;
    private int sessions = 0;
    private boolean showCustomSettings = false;

    // Database and Pets integration
    private PomodoroSessionManager sessionManager;
    private PetsController petsController;
    private int currentSessionId = -1;
    private int userId;

    // View reference
    private PomodoroView view;

    // Setup methods
    public void setUserId(int userId) {
        this.userId = userId;
        this.sessionManager = new PomodoroSessionManager(userId);
        loadActiveSession();
    }

    public void setSidebar(Sidebar sidebar) {
        sessionManager.setSidebar(sidebar);
    }

    public void setPetsController(PetsController petsController) {
        this.petsController = petsController;
        updatePetDisplay();
    }

    public void setView(PomodoroView view) {
        this.view = view;
        initialize();
    }

    public void initialize() {
        setupTimer();
        setupPresets();
        updateUI();
    }

    private void loadActiveSession() {
        if (sessionManager != null) {
            PomodoroSessionManager.ActiveSessionData activeSession = sessionManager.getActiveSession(userId);
            if (activeSession != null) {
                currentSessionId = activeSession.sessionId;
                workTime = activeSession.workDuration * 60;
                breakTime = activeSession.breakDuration * 60;

                // Restore session state based on status
                if ("Running".equals(activeSession.status)) {
                    restoreRunningSession(activeSession);
                } else if ("Paused".equals(activeSession.status)) {
                    restorePausedSession(activeSession);
                }

                System.out.println("Loaded active session: " + currentSessionId + " - Status: " + activeSession.status);
            } else {
                System.out.println("No active session found for user " + userId);
                resetToNewSession();
            }
        }
    }

    private void restoreRunningSession(PomodoroSessionManager.ActiveSessionData session) {
        isBreak = false;
        long elapsedSeconds = sessionManager.getElapsedSeconds(session.sessionId);
        timeLeft = Math.max(0, workTime - (int)elapsedSeconds);

        if (timeLeft > 0) {
            running = true;
            if (view != null) {
                view.updateStatus("💼 Work Session", false);
                view.updateButtonState(true, false);
                view.updateTimerDisplay(formatTime(timeLeft), (double)(workTime - timeLeft)/workTime);
            }
            timeline.play();
            System.out.println("Restored running session with " + timeLeft + " seconds remaining");
        } else {
            // Session expired while app was closed
            sessionManager.completeSession(currentSessionId);
            currentSessionId = -1;
            resetToNewSession();
        }
    }

    private void restorePausedSession(PomodoroSessionManager.ActiveSessionData session) {
        isBreak = false;
        long elapsedSeconds = sessionManager.getElapsedSeconds(session.sessionId);
        timeLeft = Math.max(0, workTime - (int)elapsedSeconds);

        running = false;
        if (view != null) {
            view.updateStatus("💼 Work Session (Paused)", false);
            view.updateButtonState(false, false);
            view.updateTimerDisplay(formatTime(timeLeft), (double)(workTime - timeLeft)/workTime);
        }
        System.out.println("Restored paused session with " + timeLeft + " seconds remaining");
    }

    private void resetToNewSession() {
        isBreak = false;
        running = false;
        timeLeft = workTime;
        if (view != null) {
            view.updateStatus("💼 Work Session", false);
            view.updateButtonState(false, false);
            view.updateTimerDisplay(formatTime(timeLeft), 0);
            view.setPetVisibility(false);
        }
    }

    private void setupTimer() {
        timeLeft = workTime;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void setupPresets() {
        if (view != null) {
            view.getPresetBox().getItems().addAll("Pomodoro (25/5)", "Short Focus (15/3)", "Deep Work (45/10)",
                    "Study Session (50/10)", "Quick Task (10/2)");
            view.getPresetBox().getSelectionModel().selectFirst();
        }
    }

    private void updateUI() {
        if (view != null) {
            view.updateSessions(sessions);
            view.updateHappiness(happiness);
        }
    }

    // Timer logic
    private void tick() {
        if (timeLeft > 0) {
            timeLeft--;
            double total = isBreak ? breakTime : workTime;
            if (view != null) {
                view.updateTimerDisplay(formatTime(timeLeft), (double)(total - timeLeft)/total);
                view.setPetVisibility(running && !isBreak);
            }
        } else {
            timeline.stop();
            running = false;
            if (view != null) {
                view.setPetVisibility(false);
            }

            if (isBreak) {
                startWorkSession();
            } else {
                // Complete the work session in database
                completeCurrentSession();
                startBreakSession();
            }
        }
    }

    public void toggleTimer() {
        if (running) {
            // Pause timer
            timeline.stop();
            running = false;
            // Update status based on whether it's work or break
            String status = isBreak ? "☕ Break Time (Paused)" : "💼 Work Session (Paused)";
            if (view != null) {
                view.updateStatus(status, isBreak);
                view.updateButtonState(false, isBreak);
                view.setPetVisibility(false);
            }

            // Track pause in database
            if (!isBreak && currentSessionId != -1) {
                pauseCurrentSession();
            }
        } else {
            // Start/resume timer
            running = true;
            // Update status based on whether it's work or break
            String status = isBreak ? "☕ Break Time" : "💼 Work Session";
            if (view != null) {
                view.updateStatus(status, isBreak);
                view.updateButtonState(true, isBreak);
                view.setPetVisibility(!isBreak);
            }

            // Always create session when starting work session
            if (!isBreak && currentSessionId == -1) {
                startNewSession();
            } else if (!isBreak && currentSessionId != -1) {
                // Track resume in database if session was paused
                resumeCurrentSession();
            }

            timeline.play();
        }
    }

    public void resetTimer() {
        timeline.stop();
        running = false;

        if (isBreak) {
            // Reset break session
            timeLeft = breakTime;
            if (view != null) {
                view.updateStatus("☕ Break Time", true);
                view.updateButtonState(false, true);
                view.updateTimerDisplay(formatTime(timeLeft), 0);
            }
        } else {
            // Reset work session
            timeLeft = workTime;
            if (view != null) {
                view.updateStatus("💼 Work Session", false);
                view.updateButtonState(false, false);
                view.updateTimerDisplay(formatTime(timeLeft), 0);
                view.setPetVisibility(false);
            }

            // Abort current session if exists
            if (currentSessionId != -1) {
                abortCurrentSession();
            }
        }
    }

    private void startWorkSession() {
        isBreak = false;
        timeLeft = workTime;
        if (view != null) {
            view.updateStatus("💼 Work Session", false);
            view.updateButtonState(false, false);
            view.updateTimerDisplay(formatTime(timeLeft), 0);
            view.setPetVisibility(false);
        }
    }

    private void startBreakSession() {
        isBreak = true;
        sessions++;
        happiness = Math.min(100, happiness + 10);
        timeLeft = breakTime;
        if (view != null) {
            view.updateStatus("☕ Break Time", true);
            view.updateButtonState(false, true);
            view.updateTimerDisplay(formatTime(timeLeft), 0);
            view.updateSessions(sessions);
            view.updateHappiness(happiness);
            view.setPetVisibility(false);
            view.showHappinessBoost();
        }

        showHappinessBoost();
    }

    // Database integration methods
    private void startNewSession() {
        if (sessionManager == null) {
            System.out.println("ERROR: sessionManager is null!");
            return;
        }

        String presetName = view != null ? view.getPresetBox().getValue() : "Pomodoro (25/5)";
        System.out.println("Starting new session: " + presetName);

        currentSessionId = sessionManager.startSession(presetName, workTime/60, breakTime/60);

        if (currentSessionId != -1) {
            System.out.println("SUCCESS: Session started with ID: " + currentSessionId);
        } else {
            System.out.println("ERROR: Failed to start session");
        }
    }

    private void completeCurrentSession() {
        if (sessionManager == null || currentSessionId == -1) {
            System.out.println("ERROR: Cannot complete session - manager: " + sessionManager + ", sessionId: " + currentSessionId);
            return;
        }

        System.out.println("Completing session: " + currentSessionId);
        sessionManager.completeSession(currentSessionId);
        showCompletionNotification();
        currentSessionId = -1;
        System.out.println("SUCCESS: Session completed and experience awarded");
    }

    private void abortCurrentSession() {
        if (sessionManager == null || currentSessionId == -1) {
            System.out.println("ERROR: Cannot abort session - manager: " + sessionManager + ", sessionId: " + currentSessionId);
            return;
        }

        System.out.println("Aborting session: " + currentSessionId);
        sessionManager.abortSession(currentSessionId);
        currentSessionId = -1;
        System.out.println("SUCCESS: Session aborted");
    }

    private void pauseCurrentSession() {
        if (sessionManager == null || currentSessionId == -1) {
            System.out.println("ERROR: Cannot pause session - no active session");
            return;
        }

        System.out.println("Pausing session: " + currentSessionId);
        sessionManager.pauseSession(currentSessionId);
        System.out.println("SUCCESS: Session paused");
    }

    private void resumeCurrentSession() {
        if (sessionManager == null || currentSessionId == -1) {
            System.out.println("ERROR: Cannot resume session - no active session");
            return;
        }

        System.out.println("Resuming session: " + currentSessionId);
        sessionManager.resumeSession(currentSessionId);
        System.out.println("SUCCESS: Session resumed");
    }

    private void showCompletionNotification() {
        // You can implement a notification system here
        System.out.println("Session completed! +100 XP awarded");
    }

    private void showHappinessBoost() {
        if (view != null) {
            view.showHappinessBoost();
        }
    }

    // Preset and custom timer methods
    public void presetSelected() {
        String preset = view != null ? view.getPresetBox().getValue() : "Pomodoro (25/5)";
        switch (preset) {
            case "Pomodoro (25/5)" -> { workTime = 25*60; breakTime=5*60; }
            case "Short Focus (15/3)" -> { workTime = 15*60; breakTime=3*60; }
            case "Deep Work (45/10)" -> { workTime = 45*60; breakTime=10*60; }
            case "Study Session (50/10)" -> { workTime = 50*60; breakTime=10*60; }
            case "Quick Task (10/2)" -> { workTime = 10*60; breakTime=2*60; }
        }

        // Abort any active session when changing presets
        if (currentSessionId != -1) {
            abortCurrentSession();
        }
        resetTimer();
    }

    public void applyCustomTimer() {
        if (view != null) {
            workTime = (int) view.getWorkSlider().getValue()*60;
            breakTime = (int) view.getBreakSlider().getValue()*60;
        }

        // Abort any active session when changing custom timer
        if (currentSessionId != -1) {
            abortCurrentSession();
        }
        resetTimer();
        toggleCustomSettings();
    }

    public void toggleCustomSettings() {
        showCustomSettings = !showCustomSettings;
        if (view != null) {
            view.setCustomTimerVisibility(showCustomSettings);
        }
    }

    // Pet integration
    private void updatePetDisplay() {
        if (petsController != null && view != null) {
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
            view.updatePetDisplay(currentPet);
        }
    }

    // Helper methods
    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    // New method to check if there's an active session
    public boolean hasActiveSession() {
        return currentSessionId != -1;
    }

    //for pausing when screen closes
    private static PomodoroController instance;

    public PomodoroController() {
        instance = this;
    }

    public static PomodoroController getInstance() {
        return instance;
    }

    public void forcePauseOnExit() {
        if (running && !isBreak && currentSessionId != -1) {
            timeline.stop();
            running = false;
            sessionManager.pauseSession(currentSessionId);
        }
    }

    // Getters for view to access data if needed
    public int getWorkTime() { return workTime; }
    public int getBreakTime() { return breakTime; }
    public int getHappiness() { return happiness; }
    public int getSessions() { return sessions; }
    public boolean isRunning() { return running; }
    public boolean isBreak() { return isBreak; }
}