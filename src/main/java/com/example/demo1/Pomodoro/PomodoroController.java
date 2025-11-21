package com.example.demo1.Pomodoro;

import com.example.demo1.Pets.PetsController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import com.example.demo1.Sidebar.Sidebar;
import javafx.util.Duration;

public class PomodoroController {
    private PomodoroView pomodoroViewUI;
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

    public PomodoroController() {
        this.pomodoroViewUI = new PomodoroView();
        setupEventHandlers();
        setupTimer();
        updateUI();
    }

    public PomodoroView getPomodoroUI() {
        return pomodoroViewUI;
    }

    private void setupEventHandlers() {
        // Timer controls
        pomodoroViewUI.getStartPauseButton().setOnAction(e -> toggleTimer());
        pomodoroViewUI.getResetButton().setOnAction(e -> resetTimer());

        // Preset selection
        pomodoroViewUI.getPresetBox().setOnAction(e -> presetSelected());

        // Custom timer controls
        pomodoroViewUI.getSettingsButton().setOnAction(e -> toggleCustomSettings());
        pomodoroViewUI.getApplyCustomButton().setOnAction(e -> applyCustomTimer());
    }

    private void setupTimer() {
        timeLeft = workTime;
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 0);
        pomodoroViewUI.updateStatus("💼 Work Session", false);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        pomodoroViewUI.updateButtonState(false, false);
        pomodoroViewUI.updateSessions(0);
    }

    // Setup methods
    public void setUserId(int userId) {
        this.userId = userId;
        this.sessionManager = new PomodoroSessionManager(userId);
        loadActiveSession();
    }

    public void setSidebar(Sidebar sidebar) {
        if (sessionManager != null) {
            sessionManager.setSidebar(sidebar);
        }
    }

    public void setPetsController(PetsController petsController) {
        this.petsController = petsController;
        updatePetDisplay();
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
                resetToDefault();
            }
        }
    }

    private void restoreRunningSession(PomodoroSessionManager.ActiveSessionData session) {
        isBreak = false;
        long elapsedSeconds = sessionManager.getElapsedSeconds(session.sessionId);
        timeLeft = Math.max(0, workTime - (int)elapsedSeconds);

        if (timeLeft > 0) {
            running = false; // Start paused so user can choose to resume
            pomodoroViewUI.updateStatus("💼 Work Session (Paused)", false);
            pomodoroViewUI.updateButtonState(false, false);
            pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), (double)(workTime - timeLeft)/workTime);
            System.out.println("Restored running session with " + timeLeft + " seconds remaining");
        } else {
            sessionManager.completeSession(currentSessionId);
            currentSessionId = -1;
            resetToDefault();
        }
    }

    private void restorePausedSession(PomodoroSessionManager.ActiveSessionData session) {
        isBreak = false;
        long elapsedSeconds = sessionManager.getElapsedSeconds(session.sessionId);
        timeLeft = Math.max(0, workTime - (int)elapsedSeconds);

        running = false;
        pomodoroViewUI.updateStatus("💼 Work Session (Paused)", false);
        pomodoroViewUI.updateButtonState(false, false);
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), (double)(workTime - timeLeft)/workTime);
        System.out.println("Restored paused session with " + timeLeft + " seconds remaining");
    }

    private void resetToDefault() {
        isBreak = false;
        running = false;
        timeLeft = workTime;
        pomodoroViewUI.updateStatus("💼 Work Session", false);
        pomodoroViewUI.updateButtonState(false, false);
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 0);
        pomodoroViewUI.setPetVisibility(false);
    }

    // Timer logic
    private void tick() {
        if (timeLeft > 0) {
            timeLeft--;
            pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 1.0 - ((double) timeLeft / (isBreak ? breakTime : workTime)));

            // Show pet during active work sessions
            if (running && !isBreak) {
                pomodoroViewUI.setPetVisibility(true);
            }
        } else {
            timeline.stop();
            running = false;
            pomodoroViewUI.setPetVisibility(false);

            if (isBreak) {
                startWorkSession();
            } else {
                // Complete the work session in database
                completeCurrentSession();
                startBreakSession();
            }
        }
    }

    private void toggleTimer() {
        if (running) {
            // Pause timer
            timeline.stop();
            running = false;
            pomodoroViewUI.updateButtonState(false, isBreak);
            pomodoroViewUI.setPetVisibility(false);

            // Track pause in database
            if (!isBreak && currentSessionId != -1) {
                pauseCurrentSession();
            }
        } else {
            // Start/resume timer
            running = true;

            // Always create session when starting work session
            if (!isBreak && currentSessionId == -1) {
                startNewSession();
            } else if (!isBreak && currentSessionId != -1) {
                // Track resume in database if session was paused
                resumeCurrentSession();
            }

            timeline.play();
            pomodoroViewUI.updateButtonState(true, isBreak);

            // Show pet for work sessions
            if (!isBreak) {
                pomodoroViewUI.setPetVisibility(true);
            }
        }
    }

    private void resetTimer() {
        timeline.stop();
        running = false;
        timeLeft = isBreak ? breakTime : workTime;
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 0);
        pomodoroViewUI.updateButtonState(false, isBreak);
        pomodoroViewUI.setPetVisibility(false);

        // Abort current session if exists
        if (currentSessionId != -1 && !isBreak) {
            abortCurrentSession();
        }
    }

    private void startWorkSession() {
        isBreak = false;
        timeLeft = workTime;
        pomodoroViewUI.updateStatus("💼 Work Session", false);
        pomodoroViewUI.updateButtonState(false, false);
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 0);
        pomodoroViewUI.setPetVisibility(true);
    }

    private void startBreakSession() {
        isBreak = true;
        sessions++;
        happiness = Math.min(100, happiness + 10);
        pomodoroViewUI.updateHappiness(happiness);
        timeLeft = breakTime;
        pomodoroViewUI.updateStatus("☕ Break Time", true);
        pomodoroViewUI.updateButtonState(false, true);
        pomodoroViewUI.updateSessions(sessions);
        pomodoroViewUI.updateTimerDisplay(formatTime(timeLeft), 0);
        pomodoroViewUI.setPetVisibility(false);

        pomodoroViewUI.showHappinessBoost();
    }

    // Database integration methods
    private void startNewSession() {
        if (sessionManager == null) {
            System.out.println("ERROR: sessionManager is null!");
            return;
        }

        String presetName = pomodoroViewUI.getPresetBox().getValue();
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
        System.out.println("Session completed! +100 XP awarded");
    }

    // Preset and custom timer methods
    private void presetSelected() {
        String preset = pomodoroViewUI.getPresetBox().getValue();
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

    private void applyCustomTimer() {
        workTime = (int) pomodoroViewUI.getWorkSlider().getValue() * 60;
        breakTime = (int) pomodoroViewUI.getBreakSlider().getValue() * 60;

        // Abort any active session when changing custom timer
        if (currentSessionId != -1) {
            abortCurrentSession();
        }
        resetTimer();
        toggleCustomSettings();
    }

    private void toggleCustomSettings() {
        showCustomSettings = !showCustomSettings;
        pomodoroViewUI.setCustomTimerVisibility(showCustomSettings);
    }

    private void updateUI() {
        // Initial UI state updates
        pomodoroViewUI.updateHappiness(happiness);
        pomodoroViewUI.updateSessions(sessions);
    }

    // Pet integration
    private void updatePetDisplay() {
        if (petsController != null) {
            PetsController.PetInfo currentPet = petsController.getCurrentPetForSidebar();
            pomodoroViewUI.updatePetDisplay(currentPet);
        }
    }

    // Helper method
    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}