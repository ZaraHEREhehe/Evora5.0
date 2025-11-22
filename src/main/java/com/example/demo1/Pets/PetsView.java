package com.example.demo1.Pets;

import com.example.demo1.Theme.Pastel;
import com.example.demo1.Theme.Theme;
import com.example.demo1.Theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.scene.Node;
import java.util.List;
import java.util.Optional;

public class PetsView extends BorderPane {
    private final PetsController controller;
    private String activeTab = "current";
    private VBox contentArea;
    private HBox tabContainer;
    private ThemeManager themeManager;

    public PetsView(PetsController controller) {
        this.controller = controller;
        this.themeManager = ThemeManager.getInstance();
        createView();
        refreshData();
    }

    private String forceTextColor(Color color) {
        return "-fx-text-fill: " + toHex(color) + " !important;";
    }

    private void createView() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Use theme for background
        Theme currentTheme = themeManager.getCurrentTheme();
        mainContent.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");

        // Header
        VBox headerBox = createHeader();

        // Tab Navigation
        HBox tabNavigation = createTabNavigation();

        // Content area
        contentArea = new VBox();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setSpacing(25);

        showCurrentPetTab();

        mainContent.getChildren().addAll(headerBox, tabNavigation, contentArea);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.setCenter(scrollPane);
        this.setStyle("-fx-background-color: " + currentTheme.getBackgroundColor() + ";");
    }

    private VBox createHeader() {
        Theme currentTheme = themeManager.getCurrentTheme();

        Label title = new Label("Your Pet Companion 🐾");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));

        int userExp = controller.getUserExperience();
        Label subtitle = new Label("Unlock new pets as you gain experience! • Your XP: " + userExp + " ✨");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle(forceTextColor(Color.web(currentTheme.getTextSecondary())));

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        return headerBox;
    }

    private HBox createTabNavigation() {
        tabContainer = new HBox();
        tabContainer.setAlignment(Pos.CENTER);

        VBox card = new VBox();
        card.setPadding(new Insets(10));

        // Use theme for tab navigation background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + currentTheme.getPrimaryColor() + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");

        HBox tabs = new HBox(10);
        tabs.setAlignment(Pos.CENTER);

        String[] tabData = {
                "current", "Current Pet",
                "collection", "My Pets",
                "all", "Pet Collection",
                "badges", "Badges"
        };

        for (int i = 0; i < tabData.length; i += 2) {
            String tabId = tabData[i];
            String tabLabel = tabData[i + 1];

            Button tabButton = createTabButton(tabId, tabLabel);
            tabs.getChildren().add(tabButton);
        }

        card.getChildren().add(tabs);
        tabContainer.getChildren().add(card);
        return tabContainer;
    }

    private Button createTabButton(String tabId, String tabLabel) {
        Button tabButton = new Button(tabLabel);
        tabButton.setPrefSize(120, 40);
        tabButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        tabButton.setUserData(tabId);

        updateTabButtonStyle(tabButton, tabId.equals(activeTab));

        tabButton.setOnAction(e -> switchTab(tabId));

        return tabButton;
    }

    private void updateTabButtonStyle(Button button, boolean isActive) {
        Theme currentTheme = themeManager.getCurrentTheme();

        if (isActive) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + Pastel.GRADIENT_PURPLE + ", " + Pastel.GRADIENT_PINK + ");" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + currentTheme.getTextPrimary() + ";" +
                            "-fx-border-color: " + Pastel.GRAY_300 + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    private void switchTab(String tabId) {
        activeTab = tabId;

        VBox tabCard = (VBox) tabContainer.getChildren().get(0);
        HBox tabs = (HBox) tabCard.getChildren().get(0);

        for (var child : tabs.getChildren()) {
            Button button = (Button) child;
            String buttonTabId = (String) button.getUserData();
            updateTabButtonStyle(button, buttonTabId.equals(activeTab));
        }

        refreshData();
    }

    private void refreshData() {
        contentArea.getChildren().clear();

        switch (activeTab) {
            case "current":
                showCurrentPetTab();
                break;
            case "collection":
                showCollectionTab();
                break;
            case "all":
                showAllPetsTab();
                break;
            case "badges":
                showBadgesTab();
                break;
        }
    }

    private void showBadgesTab() {
        VBox badgesCard = createBadgesTab();
        contentArea.getChildren().add(badgesCard);
    }

    private VBox createBadgesTab() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));

        // Use theme for card background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" + // Keep original border for consistency
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        Label title = new Label("Achievement Badges 🏆");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));
        title.setAlignment(Pos.CENTER);

        VBox badgesList = new VBox(15);
        List<PetsController.Badge> badges = controller.getUserBadges();

        for (PetsController.Badge badge : badges) {
            HBox badgeItem = createBadgeItem(badge);
            badgesList.getChildren().add(badgeItem);
        }

        if (badges.isEmpty()) {
            Label noBadgesLabel = new Label("No badges earned yet. Keep working to earn achievements! 🌟");
            noBadgesLabel.setStyle(forceTextColor(Color.web(currentTheme.getTextSecondary())));
            noBadgesLabel.setAlignment(Pos.CENTER);
            badgesList.getChildren().add(noBadgesLabel);
        }

        card.getChildren().addAll(title, badgesList);
        return card;
    }

    private HBox createBadgeItem(PetsController.Badge badge) {
        HBox badgeItem = new HBox(20);
        badgeItem.setPadding(new Insets(20));
        badgeItem.setAlignment(Pos.CENTER_LEFT);

        if (badge.isEarned()) {
            // Earned badges - keep the cute purple gradient
            badgeItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + Pastel.LIGHT_LILAC + ", " + Pastel.LIGHT_PURPLE_2 + ");" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: " + Pastel.GRADIENT_PURPLE + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 3);"
            );
        } else {
            // Unearned badges - soft pastel colors with gentle styling
            badgeItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + Pastel.MIST + ", " + Pastel.BLUSH + ");" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: " + Pastel.LIGHT_LILAC + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 2);"
            );
        }

        // Badge icon/emoji - make it cuter for both states
        Label badgeIcon = new Label(getBadgeEmoji(badge.getName()));
        badgeIcon.setFont(Font.font(28));
        if (!badge.isEarned()) {
            badgeIcon.setStyle("-fx-opacity: 0.6;");
        } else {
            badgeIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(192, 132, 252, 0.4), 8, 0.5, 0, 2);");
        }

        VBox badgeInfo = new VBox(8);
        badgeInfo.setAlignment(Pos.CENTER_LEFT);

        // Badge name with cute styling
        Label nameLabel = new Label(badge.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        if (badge.isEarned()) {
            nameLabel.setStyle(forceTextColor(Color.web(Pastel.FOREST)) + " -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 0, 0, 0, 1);");
        } else {
            nameLabel.setStyle(forceTextColor(Color.web(Pastel.DUSTY_PURPLE)) + " -fx-opacity: 0.8;");
        }

        // Description with softer color
        Label descLabel = new Label(badge.getDescription());
        descLabel.setFont(Font.font("System", 13));
        if (badge.isEarned()) {
            descLabel.setStyle(forceTextColor(Color.web(Pastel.SAGE)));
        } else {
            descLabel.setStyle(forceTextColor(Color.web(Pastel.LIGHT_PURPLE_TEXT)) + " -fx-opacity: 0.7;");
        }

        // Progress section - much cuter styling
        VBox progressBox = new VBox(6);
        progressBox.setAlignment(Pos.CENTER_LEFT);

        if (badge.isEarned()) {
            // Earned badge - celebration style!
            HBox earnedBox = new HBox(8);
            earnedBox.setAlignment(Pos.CENTER_LEFT);

            Label celebrationIcon = new Label("🎉");
            celebrationIcon.setFont(Font.font(14));

            Label statusLabel = new Label("Earned on " + badge.getEarnedDate().toLocalDateTime().toLocalDate());
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            statusLabel.setStyle("-fx-text-fill: " + Pastel.VIOLET + "; -fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.2), 2, 0.3, 0, 1);");

            earnedBox.getChildren().addAll(celebrationIcon, statusLabel);
            progressBox.getChildren().add(earnedBox);
        } else {
            // Unearned badge - cute progress display
            VBox progressContent = new VBox(5);

            // Progress text with cute styling
            HBox progressText = new HBox(5);
            progressText.setAlignment(Pos.CENTER_LEFT);

            Label sparkleIcon = new Label("✨");
            sparkleIcon.setFont(Font.font(12));

            Label statusLabel = new Label("Progress: " + badge.getProgressText());
            statusLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
            statusLabel.setStyle("-fx-text-fill: " + Pastel.VIOLET + "; -fx-opacity: 0.9;");

            progressText.getChildren().addAll(sparkleIcon, statusLabel);

            // Progress bar with cute styling
            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(badge.getProgressPercentage() / 100.0);
            progressBar.setPrefWidth(220);
            progressBar.setPrefHeight(8);
            progressBar.setStyle(
                    "-fx-accent: linear-gradient(to right, " + Pastel.BLUE + ", " + Pastel.LAVENDER + "); " +
                            "-fx-control-inner-background: " + Pastel.MIST + "; " +
                            "-fx-border-color: " + Pastel.LIGHT_LILAC + "; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 1;"
            );

            // Percentage label
            Label percentageLabel = new Label(badge.getProgressPercentage() + "%");
            percentageLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            percentageLabel.setStyle("-fx-text-fill: " + Pastel.VIOLET + "; -fx-opacity: 0.8;");
            percentageLabel.setAlignment(Pos.CENTER_RIGHT);

            progressContent.getChildren().addAll(progressText, progressBar, percentageLabel);
            progressBox.getChildren().add(progressContent);
        }

        badgeInfo.getChildren().addAll(nameLabel, descLabel, progressBox);
        HBox.setHgrow(badgeInfo, Priority.ALWAYS);

        badgeItem.getChildren().addAll(badgeIcon, badgeInfo);

        // Add a cute hover effect for unearned badges
        if (!badge.isEarned()) {
            badgeItem.setOnMouseEntered(e -> {
                badgeItem.setStyle(
                        "-fx-background-color: linear-gradient(to right, " + Pastel.SKY + ", " + Pastel.ROSE + ");" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: " + Pastel.LAVENDER + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.3, 0, 3);"
                );
            });

            badgeItem.setOnMouseExited(e -> {
                badgeItem.setStyle(
                        "-fx-background-color: linear-gradient(to right, " + Pastel.MIST + ", " + Pastel.BLUSH + ");" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: " + Pastel.LIGHT_LILAC + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 2);"
                );
            });
        }

        return badgeItem;
    }

    // Add helper method for badge emojis
    private String getBadgeEmoji(String badgeName) {
        if (badgeName.toLowerCase().contains("first")) return "🌟";
        if (badgeName.toLowerCase().contains("focus")) return "⚡";
        if (badgeName.toLowerCase().contains("note")) return "📝";
        if (badgeName.toLowerCase().contains("week")) return "👑";
        if (badgeName.toLowerCase().contains("mood")) return "😊";
        if (badgeName.toLowerCase().contains("master")) return "🏆";
        if (badgeName.toLowerCase().contains("streak")) return "🔥";
        return "🏅";
    }

    private void showCurrentPetTab() {
        PetsController.Pet currentPet = controller.getCurrentPet();
        if (currentPet != null) {
            VBox petDisplayCard = createPetDisplayCard(currentPet, false);
            VBox unlockedPetsCard = createUnlockedPetsCard();
            contentArea.getChildren().addAll(petDisplayCard, unlockedPetsCard);
        }
    }

    // In the createPetDisplayCard method, add an edit button:
    private VBox createPetDisplayCard(PetsController.Pet pet, boolean showEquipButton) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));

        // Use theme for card background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" + // Keep original border for consistency
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(500);
        card.setAlignment(Pos.TOP_CENTER);

        // Pet name with edit option
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER);

        Label petName = new Label(pet.getName());
        petName.setFont(Font.font("System", FontWeight.BOLD, 24));
        petName.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));

        // Cute edit button
        Button editNameBtn = new Button("✏ Edit Name");
        editNameBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16;" +
                        "-fx-text-fill: " + currentTheme.getTextSecondary() + ";" +
                        "-fx-padding: 4 8;"
        );
        editNameBtn.setOnMouseEntered(e -> editNameBtn.setStyle(
                "-fx-background-color: " + Pastel.MIST + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16;" +
                        "-fx-text-fill: " + currentTheme.getTextPrimary() + ";" +
                        "-fx-padding: 4 8;" +
                        "-fx-background-radius: 10;"
        ));
        editNameBtn.setOnMouseExited(e -> editNameBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16;" +
                        "-fx-text-fill: " + currentTheme.getTextSecondary() + ";" +
                        "-fx-padding: 4 8;"
        ));
        editNameBtn.setOnAction(e -> showNameEditDialog(pet));

        nameBox.getChildren().addAll(petName, editNameBtn);

        Label petSpecies = new Label(pet.getSpecies() + " • Unlocked at " + pet.getRequiredExperience() + " XP");
        petSpecies.setFont(Font.font("System", 14));
        petSpecies.setStyle(forceTextColor(Color.web(currentTheme.getTextSecondary())));

        VBox headerBox = new VBox(5, nameBox, petSpecies);
        headerBox.setAlignment(Pos.CENTER);

        // Pet display area
        VBox petDisplay = createPetDisplay(pet);

        card.getChildren().addAll(headerBox, petDisplay);

        if (showEquipButton) {
            Button equipButton = createEquipButton(pet);
            card.getChildren().add(equipButton);
        }

        return card;
    }

    // Add the cute name edit dialog method:
    private void showNameEditDialog(PetsController.Pet pet) {
        // Create a custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("✨ Rename Your Pet ✨");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the cute content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Use theme for dialog background
        Theme currentTheme = themeManager.getCurrentTheme();
        content.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";");

        // Header with emoji
        Label header = new Label("Give " + pet.getName() + " a new name! 🐾");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: " + currentTheme.getTextPrimary() + ";");

        // Pet image/emoji
        StackPane petDisplay = new StackPane();
        petDisplay.setPrefSize(80, 80);

        try {
            ImageView petGif = new ImageView(new Image(getPetGifPath(pet.getGifFilename())));
            petGif.setFitWidth(60);
            petGif.setFitHeight(60);
            petGif.setPreserveRatio(true);
            petDisplay.getChildren().add(petGif);
        } catch (Exception e) {
            Label petEmoji = new Label(getSpeciesEmoji(pet.getSpecies()));
            petEmoji.setFont(Font.font(36));
            petDisplay.getChildren().add(petEmoji);
        }

        // Text field with cute styling
        TextField nameField = new TextField(pet.getName());
        nameField.setPromptText("Enter cute name here...");
        nameField.setPrefWidth(200);
        nameField.setStyle(
                "-fx-background-color: " + currentTheme.getBackgroundColor() + ";" +
                        "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 10;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: " + currentTheme.getTextPrimary() + ";"
        );

        // Instruction text
        Label instruction = new Label("What should we call your adorable companion?");
        instruction.setStyle("-fx-text-fill: " + currentTheme.getTextSecondary() + "; -fx-font-size: 12;");
        instruction.setWrapText(true);
        instruction.setMaxWidth(250);
        instruction.setTextAlignment(TextAlignment.CENTER);

        content.getChildren().addAll(header, petDisplay, instruction, nameField);
        dialog.getDialogPane().setContent(content);

        // Enable/Disable save button depending on whether a name was entered
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Add listener to enable save button only when text is entered
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        // Request focus on the name field
        Platform.runLater(nameField::requestFocus);

        // Convert the result to the new name when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });

        // Style the dialog pane
        dialog.getDialogPane().setStyle(
                "-fx-background-color: " + currentTheme.getCardColor() + ";" +
                        "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0.5, 0, 5);"
        );

        // Style the buttons
        dialog.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Node buttonNode = dialog.getDialogPane().lookupButton(buttonType);
            if (buttonNode instanceof Button) {
                Button button = (Button) buttonNode;
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    button.setStyle(
                            "-fx-background-color: linear-gradient(to right, " + Pastel.GRADIENT_PURPLE + ", " + Pastel.GRADIENT_PINK + ");" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-padding: 8 16;"
                    );
                } else {
                    button.setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-text-fill: " + currentTheme.getTextPrimary() + ";" +
                                    "-fx-border-color: " + Pastel.GRAY_300 + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-padding: 8 16;"
                    );
                }
            }
        });

        // Show the dialog and handle the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty() && !newName.equals(pet.getName())) {
                if (controller.changePetName(pet.getPetTypeId(), newName)) {
                    // Show success notification
                    showSuccessNotification("Name updated to " + newName + "! 🎉");
                    refreshData();
                    // Notify that pet changed so sidebar updates
                    controller.notifyPetChanged();
                } else {
                    showErrorNotification("Failed to update name. Please try again.");
                }
            }
        });
    }

    // Add helper methods for notifications
    private void showSuccessNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success! ✨");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the success alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f0fff4, #dcfce7);" +
                        "-fx-border-color: " + Pastel.LIGHT_GREEN + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;"
        );

        alert.showAndWait();
    }

    private void showErrorNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! 😿");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the error alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #fef2f2, #fee2e2);" +
                        "-fx-border-color: " + Pastel.LIGHT_RED + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;"
        );

        alert.showAndWait();
    }

    private VBox createPetDisplay(PetsController.Pet pet) {
        VBox petDisplay = new VBox(10);
        petDisplay.setAlignment(Pos.CENTER);
        petDisplay.setPadding(new Insets(20, 0, 20, 0));

        // Pet GIF - no fancy border
        StackPane petContainer = new StackPane();
        petContainer.setPrefSize(150, 150);

        try {
            ImageView petGif = new ImageView(new Image(getPetGifPath(pet.getGifFilename())));
            petGif.setFitWidth(120);
            petGif.setFitHeight(120);
            petGif.setPreserveRatio(true);
            petContainer.getChildren().add(petGif);
        } catch (Exception e) {
            Label petEmoji = new Label(getSpeciesEmoji(pet.getSpecies()));
            petEmoji.setFont(Font.font(48));
            petContainer.getChildren().add(petEmoji);
        }

        // Pet personality info
        Label personality = new Label("\"" + pet.getPersonality() + "\"");
        personality.setFont(Font.font("System", FontPosture.ITALIC, 14));
        personality.setStyle(forceTextColor(Color.web(themeManager.getCurrentTheme().getTextSecondary())));

        Label activity = new Label("Current activity: " + pet.getWorkingActivity());
        activity.setFont(Font.font("System", 12));
        activity.setStyle(forceTextColor(Color.web(Pastel.GRAY_400)));

        VBox infoBox = new VBox(5, personality, activity);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setMaxWidth(300);

        petDisplay.getChildren().addAll(petContainer, infoBox);
        return petDisplay;
    }

    private Button createEquipButton(PetsController.Pet pet) {
        PetsController.Pet currentPet = controller.getCurrentPet();
        boolean isEquipped = currentPet != null && currentPet.getPetTypeId() == pet.getPetTypeId();

        Button equipButton = new Button(isEquipped ? "✓ Equipped" : "Equip");
        equipButton.setStyle(
                "-fx-background-color: " + (isEquipped ? Pastel.SUCCESS_GREEN : Pastel.GRADIENT_PURPLE) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: " + (isEquipped ? "default" : "hand") + ";"
        );

        if (!isEquipped) {
            equipButton.setOnAction(e -> {
                if (controller.equipPet(pet.getPetTypeId())) {
                    controller.notifyPetChanged();
                    refreshData();
                }
            });
        }

        return equipButton;
    }

    private VBox createUnlockedPetsCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));

        // Use theme for card background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" + // Keep original border for consistency
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(600);
        card.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Your Unlocked Pets 🎉");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));

        FlowPane petGrid = new FlowPane();
        petGrid.setHgap(20);
        petGrid.setVgap(20);
        petGrid.setAlignment(Pos.CENTER);
        petGrid.setPrefWidth(550);

        List<PetsController.Pet> unlockedPets = controller.getUnlockedPets();
        PetsController.Pet currentPet = controller.getCurrentPet();

        for (PetsController.Pet pet : unlockedPets) {
            if (pet.getPetTypeId() != currentPet.getPetTypeId()) {
                VBox petCard = createUnlockedPetCard(pet);
                petGrid.getChildren().add(petCard);
            }
        }

        if (petGrid.getChildren().isEmpty()) {
            Label noPetsLabel = new Label("No other pets unlocked yet. Keep gaining experience! 🌟");
            noPetsLabel.setStyle(forceTextColor(Color.web(currentTheme.getTextSecondary())));
            noPetsLabel.setAlignment(Pos.CENTER);
            petGrid.getChildren().add(noPetsLabel);
        }

        card.getChildren().addAll(title, petGrid);
        return card;
    }

    private VBox createUnlockedPetCard(PetsController.Pet pet) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + Pastel.GRAY_50 + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: " + Pastel.GRAY_200 + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;"
        );
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(140, 160);

        try {
            ImageView petImage = new ImageView(new Image(getPetGifPath(pet.getGifFilename()), 80, 80, true, true));
            card.getChildren().add(petImage);
        } catch (Exception e) {
            Label emojiLabel = new Label(getSpeciesEmoji(pet.getSpecies()));
            emojiLabel.setFont(Font.font(24));
            card.getChildren().add(emojiLabel);
        }

        Label nameLabel = new Label(pet.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setStyle(forceTextColor(Color.web(Pastel.FOREST)));
        nameLabel.setAlignment(Pos.CENTER);

        Button equipBtn = createEquipButton(pet);

        card.getChildren().addAll(nameLabel, equipBtn);
        return card;
    }

    private void showCollectionTab() {
        VBox collectionCard = createCollectionTab();
        contentArea.getChildren().add(collectionCard);
    }

    private VBox createCollectionTab() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));

        // Use theme for card background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" + // Keep original border for consistency
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        Label title = new Label("My Pet Collection 🎀");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));
        title.setAlignment(Pos.CENTER);

        VBox petsList = new VBox(15);
        List<PetsController.Pet> unlockedPets = controller.getUnlockedPets();

        for (PetsController.Pet pet : unlockedPets) {
            HBox petItem = createCollectionPetItem(pet);
            petsList.getChildren().add(petItem);
        }

        if (unlockedPets.isEmpty()) {
            Label noPetsLabel = new Label("No pets unlocked yet. Start gaining experience to unlock pets! 🌟");
            noPetsLabel.setStyle(forceTextColor(Color.web(currentTheme.getTextSecondary())));
            noPetsLabel.setAlignment(Pos.CENTER);
            petsList.getChildren().add(noPetsLabel);
        }

        card.getChildren().addAll(title, petsList);
        return card;
    }

    private HBox createCollectionPetItem(PetsController.Pet pet) {
        HBox petItem = new HBox(20);
        petItem.setPadding(new Insets(20));
        petItem.setStyle(
                "-fx-background-color: " + Pastel.GRAY_50 + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + Pastel.GRAY_300 + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;"
        );
        petItem.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView petImage = new ImageView(new Image(getPetGifPath(pet.getGifFilename()), 80, 80, true, true));
            petItem.getChildren().add(petImage);
        } catch (Exception e) {
            Label emojiLabel = new Label(getSpeciesEmoji(pet.getSpecies()));
            emojiLabel.setFont(Font.font(36));
            petItem.getChildren().add(emojiLabel);
        }

        VBox petInfo = new VBox(5);
        Label nameLabel = new Label(pet.getName() + " the " + pet.getSpecies());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle(forceTextColor(Color.web(Pastel.FOREST)));

        Label personalityLabel = new Label(pet.getPersonality());
        personalityLabel.setFont(Font.font("System", 14));
        personalityLabel.setStyle(forceTextColor(Color.web(Pastel.SAGE)));

        Label activityLabel = new Label("Activity: " + pet.getWorkingActivity());
        activityLabel.setFont(Font.font("System", 12));
        activityLabel.setStyle(forceTextColor(Color.web(Pastel.GRAY_400)));

        petInfo.getChildren().addAll(nameLabel, personalityLabel, activityLabel);
        HBox.setHgrow(petInfo, Priority.ALWAYS);

        Button equipButton = createEquipButton(pet);

        petItem.getChildren().addAll(petInfo, equipButton);
        return petItem;
    }

    private void showAllPetsTab() {
        VBox allPetsCard = createAllPetsTab();
        contentArea.getChildren().add(allPetsCard);
    }

    private VBox createAllPetsTab() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));

        // Use theme for card background
        Theme currentTheme = themeManager.getCurrentTheme();
        card.setStyle("-fx-background-color: " + currentTheme.getCardColor() + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + Pastel.LIGHT_PURPLE + ";" + // Keep original border for consistency
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        int userExp = controller.getUserExperience();
        Label expHeader = new Label("Your Total Experience: " + userExp + " XP ✨");
        expHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
        expHeader.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));
        expHeader.setAlignment(Pos.CENTER);

        Label title = new Label("All Available Pets 🌈");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(Color.web(currentTheme.getTextPrimary())));
        title.setAlignment(Pos.CENTER);

        VBox petsList = new VBox(15);
        List<PetsController.Pet> allPets = controller.getAllPets();

        for (PetsController.Pet pet : allPets) {
            HBox petItem = createAllPetsItem(pet);
            petsList.getChildren().add(petItem);
        }

        card.getChildren().addAll(expHeader, title, petsList);
        return card;
    }

    private HBox createAllPetsItem(PetsController.Pet pet) {
        HBox petItem = new HBox(20);
        petItem.setPadding(new Insets(20));

        if (pet.isUnlocked()) {
            petItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + Pastel.LIGHT_LILAC + ", " + Pastel.LIGHT_PURPLE_2 + ");" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: " + Pastel.GRADIENT_PURPLE + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;"
            );
        } else {
            petItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + Pastel.GRAY_100 + ", " + Pastel.GRAY_200 + ");" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: " + Pastel.GRAY_300 + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-opacity: 0.9;"
            );
        }
        petItem.setAlignment(Pos.CENTER_LEFT);

        // Always show the pet GIF, even if locked - just with lower opacity
        try {
            ImageView petImage = new ImageView(new Image(getPetGifPath(pet.getGifFilename()), 80, 80, true, true));
            if (!pet.isUnlocked()) {
                petImage.setOpacity(0.7);
            }
            petItem.getChildren().add(petImage);
        } catch (Exception e) {
            Label emojiLabel = new Label(getSpeciesEmoji(pet.getSpecies()));
            emojiLabel.setFont(Font.font(24));
            if (!pet.isUnlocked()) {
                emojiLabel.setOpacity(0.7);
            }
            petItem.getChildren().add(emojiLabel);
        }

        VBox petInfo = new VBox(5);
        Label nameLabel = new Label(pet.getName() + " the " + pet.getSpecies());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle(forceTextColor(Color.web(Pastel.FOREST)));

        Label expLabel = new Label("Requires " + pet.getRequiredExperience() + " XP 🌟");
        expLabel.setFont(Font.font("System", 14));
        expLabel.setStyle(forceTextColor(Color.web(Pastel.SAGE)));

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        if (pet.isUnlocked()) {
            statusLabel.setText("🎉 Unlocked - Click 'My Pets' to equip!");
            statusLabel.setStyle("-fx-text-fill: " + Pastel.DARK_SUCCESS_GREEN + ";");
        } else if (pet.canUnlock()) {
            statusLabel.setText("✨ Ready to unlock! Visit 'Pet Collection' to claim");
            statusLabel.setStyle("-fx-text-fill: " + Pastel.WARNING_ORANGE + ";");
        } else {
            statusLabel.setText("🔒 " + pet.getRemainingExperience() + " XP needed");
            statusLabel.setStyle("-fx-text-fill: " + Pastel.ERROR_RED + ";");
        }

        petInfo.getChildren().addAll(nameLabel, expLabel, statusLabel);
        HBox.setHgrow(petInfo, Priority.ALWAYS);

        petItem.getChildren().add(petInfo);
        return petItem;
    }

    // Helper methods
    private String getPetGifPath(String filename) {
        return getClass().getResource("/pet_gifs/" + filename).toExternalForm();
    }

    private String getSpeciesEmoji(String species) {
        switch (species.toLowerCase()) {
            case "cat": return "🐱";
            case "bunny": return "🐰";
            case "owl": return "🦉";
            case "dragon": return "🐉";
            default: return "❓";
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}