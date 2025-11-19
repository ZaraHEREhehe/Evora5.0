package com.example.demo1.Pets;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;

public class PetsView extends BorderPane {
    private final PetsController controller;
    private String activeTab = "current";
    private VBox contentArea;
    private HBox tabContainer;

    // Color palette
    private final Color bgColor = Color.web("#fdf7ff");
    private final Color cardBg = Color.web("#FFFFFF");
    private final Color textPrimary = Color.web("#5c5470");
    private final Color textSecondary = Color.web("#756f86");
    private final Color borderColor = Color.web("#D8B4FE");

    public PetsView(PetsController controller) {
        this.controller = controller;
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
        mainContent.setStyle("-fx-background-color: #fdf7ff;");

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
        this.setStyle("-fx-background-color: #fdf7ff;");
    }

    private VBox createHeader() {
        Label title = new Label("Your Pet Companion 🐾");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle(forceTextColor(textPrimary));

        int userExp = controller.getUserExperience();
        Label subtitle = new Label("Unlock new pets as you gain experience! • Your XP: " + userExp + " ✨");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle(forceTextColor(textSecondary));

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
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
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
        if (isActive) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #C084FC, #F472B6);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + toHex(textPrimary) + ";" +
                            "-fx-border-color: #D1D5DB;" +
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
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        Label title = new Label("Achievement Badges 🏆");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));
        title.setAlignment(Pos.CENTER);

        VBox badgesList = new VBox(15);
        List<PetsController.Badge> badges = controller.getUserBadges();

        for (PetsController.Badge badge : badges) {
            HBox badgeItem = createBadgeItem(badge);
            badgesList.getChildren().add(badgeItem);
        }

        if (badges.isEmpty()) {
            Label noBadgesLabel = new Label("No badges earned yet. Keep working to earn achievements! 🌟");
            noBadgesLabel.setStyle(forceTextColor(textSecondary));
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
                    "-fx-background-color: linear-gradient(to right, #E2D6FF, #F0D2F7);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #C084FC;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 3);"
            );
        } else {
            // Unearned badges - soft pastel colors with gentle styling
            badgeItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, #F7EFFF, #FCEDF5);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #E2D6FF;" +
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
            nameLabel.setStyle(forceTextColor(textPrimary) + " -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 0, 0, 0, 1);");
        } else {
            nameLabel.setStyle(forceTextColor(Color.web("#8B7B9D")) + " -fx-opacity: 0.8;");
        }

        // Description with softer color
        Label descLabel = new Label(badge.getDescription());
        descLabel.setFont(Font.font("System", 13));
        if (badge.isEarned()) {
            descLabel.setStyle(forceTextColor(textSecondary));
        } else {
            descLabel.setStyle(forceTextColor(Color.web("#9C8FA8")) + " -fx-opacity: 0.7;");
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
            statusLabel.setStyle("-fx-text-fill: #8B5CF6; -fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.2), 2, 0.3, 0, 1);");

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
            statusLabel.setStyle("-fx-text-fill: #8B5CF6; -fx-opacity: 0.9;");

            progressText.getChildren().addAll(sparkleIcon, statusLabel);

            // Progress bar with cute styling
            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(badge.getProgressPercentage() / 100.0);
            progressBar.setPrefWidth(220);
            progressBar.setPrefHeight(8);
            progressBar.setStyle(
                    "-fx-accent: linear-gradient(to right, #C1DAFF, #D7D8FF); " +
                            "-fx-control-inner-background: #F7EFFF; " +
                            "-fx-border-color: #E2D6FF; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 1;"
            );

            // Percentage label
            Label percentageLabel = new Label(badge.getProgressPercentage() + "%");
            percentageLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            percentageLabel.setStyle("-fx-text-fill: #8B5CF6; -fx-opacity: 0.8;");
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
                        "-fx-background-color: linear-gradient(to right, #E4EFFF, #F3D1F3);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #D7D8FF;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.3, 0, 3);"
                );
            });

            badgeItem.setOnMouseExited(e -> {
                badgeItem.setStyle(
                        "-fx-background-color: linear-gradient(to right, #F7EFFF, #FCEDF5);" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: #E2D6FF;" +
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

    private VBox createPetDisplayCard(PetsController.Pet pet, boolean showEquipButton) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
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
        petName.setStyle(forceTextColor(textPrimary));
        nameBox.getChildren().addAll(petName);

        Label petSpecies = new Label(pet.getSpecies() + " • Unlocked at " + pet.getRequiredExperience() + " XP");
        petSpecies.setFont(Font.font("System", 14));
        petSpecies.setStyle(forceTextColor(textSecondary));

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
        personality.setStyle(forceTextColor(textSecondary));

        Label activity = new Label("Current activity: " + pet.getWorkingActivity());
        activity.setFont(Font.font("System", 12));
        activity.setStyle(forceTextColor(Color.web("#9ca3af")));

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
                "-fx-background-color: " + (isEquipped ? "#10B981" : "#C084FC") + ";" +
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
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(600);
        card.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Your Unlocked Pets 🎉");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));

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
            noPetsLabel.setStyle(forceTextColor(textSecondary));
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
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #E5E7EB;" +
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
        nameLabel.setStyle(forceTextColor(textPrimary));
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
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        Label title = new Label("My Pet Collection 🎀");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));
        title.setAlignment(Pos.CENTER);

        VBox petsList = new VBox(15);
        List<PetsController.Pet> unlockedPets = controller.getUnlockedPets();

        for (PetsController.Pet pet : unlockedPets) {
            HBox petItem = createCollectionPetItem(pet);
            petsList.getChildren().add(petItem);
        }

        if (unlockedPets.isEmpty()) {
            Label noPetsLabel = new Label("No pets unlocked yet. Start gaining experience to unlock pets! 🌟");
            noPetsLabel.setStyle(forceTextColor(textSecondary));
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
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #D1D5DB;" +
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
        nameLabel.setStyle(forceTextColor(textPrimary));

        Label personalityLabel = new Label(pet.getPersonality());
        personalityLabel.setFont(Font.font("System", 14));
        personalityLabel.setStyle(forceTextColor(textSecondary));

        Label activityLabel = new Label("Activity: " + pet.getWorkingActivity());
        activityLabel.setFont(Font.font("System", 12));
        activityLabel.setStyle(forceTextColor(Color.web("#9ca3af")));

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
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(800);

        int userExp = controller.getUserExperience();
        Label expHeader = new Label("Your Total Experience: " + userExp + " XP ✨");
        expHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
        expHeader.setStyle(forceTextColor(textPrimary));
        expHeader.setAlignment(Pos.CENTER);

        Label title = new Label("All Available Pets 🌈");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));
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
                    "-fx-background-color: linear-gradient(to right, #E2D6FF, #F0D2F7);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #C084FC;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;"
            );
        } else {
            petItem.setStyle(
                    "-fx-background-color: linear-gradient(to right, #F3F4F6, #E5E7EB);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #D1D5DB;" +
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
        nameLabel.setStyle(forceTextColor(textPrimary));

        Label expLabel = new Label("Requires " + pet.getRequiredExperience() + " XP 🌟");
        expLabel.setFont(Font.font("System", 14));
        expLabel.setStyle(forceTextColor(textSecondary));

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        if (pet.isUnlocked()) {
            statusLabel.setText("🎉 Unlocked - Click 'My Pets' to equip!");
            statusLabel.setStyle("-fx-text-fill: #059669;");
        } else if (pet.canUnlock()) {
            statusLabel.setText("✨ Ready to unlock! Visit 'Pet Collection' to claim");
            statusLabel.setStyle("-fx-text-fill: #D97706;");
        } else {
            statusLabel.setText("🔒 " + pet.getRemainingExperience() + " XP needed");
            statusLabel.setStyle("-fx-text-fill: #DC2626;");
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