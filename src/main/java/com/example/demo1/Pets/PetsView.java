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

public class PetsView extends BorderPane {
    private final PetsController controller;
    private String currentPetType = "luna";
    private String activeTab = "pet";

    // Color palette - matching your Mood module
    private final Color bgColor = Color.web("#fdf7ff");
    private final Color cardBg = Color.web("#FFFFFF");
    private final Color textPrimary = Color.web("#5c5470");
    private final Color textSecondary = Color.web("#756f86");
    private final Color borderColor = Color.web("#D8B4FE");
    private final Color gradientStart = Color.web("#C084FC");
    private final Color gradientEnd = Color.web("#F472B6");

    // Mock data - will be replaced with real data from controller later
    private int petLevel = 5;
    private int petExperience = 750;
    private int experienceToNext = 1000;
    private int happiness = 85;
    private int coins = 245;

    public PetsView(PetsController controller) {
        this.controller = controller;
        createView();
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
        StackPane contentArea = new StackPane();
        contentArea.setPrefHeight(600);

        // Create different tab contents
        VBox petTab = createPetTab();
        VBox badgesTab = createBadgesTab();
        VBox wardrobeTab = createWardrobeTab();

        // Initially show pet tab
        contentArea.getChildren().add(petTab);

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

        Label subtitle = new Label("Take care of your pixel friend and earn rewards!");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle(forceTextColor(textSecondary));

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        return headerBox;
    }

    private HBox createTabNavigation() {
        HBox tabContainer = new HBox();
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
                "pet", "Pet",
                "badges", "Badges",
                "wardrobe", "Shop"
        };

        for (int i = 0; i < tabData.length; i += 2) {
            String tabId = tabData[i];
            String tabLabel = tabData[i + 1];

            Button tabButton = new Button(tabLabel);
            tabButton.setPrefSize(100, 40);
            tabButton.setFont(Font.font("System", FontWeight.BOLD, 14));

            if (activeTab.equals(tabId)) {
                tabButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #C084FC, #F472B6);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-radius: 20;" +
                                "-fx-cursor: hand;"
                );
            } else {
                tabButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + toHex(textPrimary) + ";" +
                                "-fx-border-color: #D1D5DB;" +
                                "-fx-border-width: 2;" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-radius: 20;" +
                                "-fx-cursor: hand;"
                );
            }

            final String finalTabId = tabId;
            tabButton.setOnAction(e -> switchTab(finalTabId, tabs, tabButton));

            tabs.getChildren().add(tabButton);
        }

        card.getChildren().add(tabs);
        tabContainer.getChildren().add(card);
        return tabContainer;
    }

    private void switchTab(String tabId, HBox tabs, Button clickedButton) {
        activeTab = tabId;

        // Update button styles
        for (var child : tabs.getChildren()) {
            Button button = (Button) child;
            if (button == clickedButton) {
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

        // Update content (in a real implementation, you'd switch the actual content)
    }

    private VBox createPetTab() {
        VBox petTab = new VBox(25);
        petTab.setAlignment(Pos.TOP_CENTER);

        // Pet Display Card
        VBox petDisplayCard = createPetDisplayCard();

        // Pet Selection Card
        VBox petSelectionCard = createPetSelectionCard();

        petTab.getChildren().addAll(petDisplayCard, petSelectionCard);
        return petTab;
    }

    private VBox createPetDisplayCard() {
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

        // Pet name and level
        Label petName = new Label("Luna");
        petName.setFont(Font.font("System", FontWeight.BOLD, 24));
        petName.setStyle(forceTextColor(textPrimary));

        Label petLevelLabel = new Label("Level " + petLevel + " • Luna the Cat");
        petLevelLabel.setFont(Font.font("System", 14));
        petLevelLabel.setStyle(forceTextColor(textSecondary));

        VBox headerBox = new VBox(5, petName, petLevelLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Pet display area
        VBox petDisplay = createPetDisplay();

        // Stats
        VBox stats = createStatsSection();

        card.getChildren().addAll(headerBox, petDisplay, stats);
        return card;
    }

    private VBox createPetDisplay() {
        VBox petDisplay = new VBox(10);
        petDisplay.setAlignment(Pos.CENTER);
        petDisplay.setPadding(new Insets(20, 0, 20, 0));

        // Pet image placeholder (you can replace with actual pixel art)
        StackPane petContainer = new StackPane();
        petContainer.setPrefSize(120, 120);

        // Background circle
        Region background = new Region();
        background.setPrefSize(120, 120);
        background.setStyle(
                "-fx-background-color: linear-gradient(to right, #FFB347, #FFCC33);" +
                        "-fx-background-radius: 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 4);"
        );

        // Pet emoji as placeholder
        Label petEmoji = new Label("🐱");
        petEmoji.setFont(Font.font(48));

        petContainer.getChildren().addAll(background, petEmoji);

        // Pet personality info
        Label personality = new Label("\"Curious and independent\"");
        personality.setFont(Font.font("System", FontPosture.ITALIC, 14));
        personality.setStyle(forceTextColor(textSecondary));

        Label activity = new Label("Current activity: Typing on tiny keyboard");
        activity.setFont(Font.font("System", 12));
        activity.setStyle(forceTextColor(Color.web("#9ca3af")));

        VBox infoBox = new VBox(5, personality, activity);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setMaxWidth(300);

        petDisplay.getChildren().addAll(petContainer, infoBox);
        return petDisplay;
    }

    private VBox createStatsSection() {
        VBox stats = new VBox(15);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setMaxWidth(400);

        // Experience progress
        VBox experienceSection = createProgressBar(
                "Experience",
                petExperience + " / " + experienceToNext,
                (double) petExperience / experienceToNext,
                "#8B5CF6", "#C084FC"
        );

        // Happiness progress
        VBox happinessSection = createProgressBar(
                "Happiness",
                happiness + "%",
                happiness / 100.0,
                "#EC4899", "#F472B6"
        );

        // Coins
        HBox coinsSection = new HBox(10);
        coinsSection.setAlignment(Pos.CENTER_LEFT);

        Label coinsLabel = new Label("Coins");
        coinsLabel.setFont(Font.font("System", 14));
        coinsLabel.setStyle(forceTextColor(textSecondary));
        coinsLabel.setPrefWidth(100);

        Pane coinBadge = createBadge(coins + " coins", "#FEF3C7", "#D97706");

        coinsSection.getChildren().addAll(coinsLabel, coinBadge);

        stats.getChildren().addAll(experienceSection, happinessSection, coinsSection);
        return stats;
    }

    private VBox createProgressBar(String label, String value, double progress, String startColor, String endColor) {
        VBox section = new VBox(5);

        HBox labels = new HBox();
        labels.setAlignment(Pos.CENTER_LEFT);

        Label sectionLabel = new Label(label);
        sectionLabel.setFont(Font.font("System", 14));
        sectionLabel.setStyle(forceTextColor(textSecondary));
        sectionLabel.setPrefWidth(100);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", 14));
        valueLabel.setStyle(forceTextColor(textSecondary));

        labels.getChildren().addAll(sectionLabel, valueLabel);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        // Progress bar background
        StackPane progressBar = new StackPane();
        progressBar.setPrefHeight(8);
        progressBar.setMaxWidth(400);
        progressBar.setStyle(
                "-fx-background-color: #E5E7EB;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-radius: 4;"
        );

        // Progress bar fill
        Region progressFill = new Region();
        progressFill.setPrefHeight(8);
        progressFill.setMaxWidth(400 * progress);
        progressFill.setStyle(
                "-fx-background-color: linear-gradient(to right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-radius: 4;"
        );

        progressBar.getChildren().add(progressFill);

        section.getChildren().addAll(labels, progressBar);
        return section;
    }

    private Pane createBadge(String text, String bgColor, String textColor) {
        HBox badge = new HBox(5);
        badge.setPadding(new Insets(5, 10, 5, 10));
        badge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;"
        );
        badge.setAlignment(Pos.CENTER);

        Label badgeText = new Label(text);
        badgeText.setFont(Font.font("System", FontWeight.BOLD, 12));
        badgeText.setStyle("-fx-text-fill: " + textColor + ";");

        badge.getChildren().add(badgeText);
        return badge;
    }

    private VBox createPetSelectionCard() {
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

        Label title = new Label("Choose Your Pet");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));

        // Pet selection grid
        GridPane petGrid = new GridPane();
        petGrid.setHgap(15);
        petGrid.setVgap(15);
        petGrid.setAlignment(Pos.CENTER);

        String[][] pets = {
                {"luna", "Luna the Cat", "🐱", "#FFB347", "#FFCC33"},
                {"hoot", "Hoot the Owl", "🦉", "#A78BFA", "#C4B5FD"},
                {"cocoa", "Cocoa the Bunny", "🐰", "#F472B6", "#FDA4AF"}
        };

        for (int i = 0; i < pets.length; i++) {
            String petId = pets[i][0];
            String petName = pets[i][1];
            String emoji = pets[i][2];
            String startColor = pets[i][3];
            String endColor = pets[i][4];

            Button petButton = createPetButton(petId, petName, emoji, startColor, endColor);
            petGrid.add(petButton, i % 3, i / 3);
        }

        card.getChildren().addAll(title, petGrid);
        return card;
    }

    private Button createPetButton(String petId, String petName, String emoji, String startColor, String endColor) {
        Button petButton = new Button();
        petButton.setPrefSize(120, 100);
        petButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-color: " + (currentPetType.equals(petId) ? "#8B5CF6" : "transparent") + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(24));

        Label nameLabel = new Label(petName.split(" ")[0]);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setStyle("-fx-text-fill: white;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(100);
        nameLabel.setAlignment(Pos.CENTER);

        content.getChildren().addAll(emojiLabel, nameLabel);
        petButton.setGraphic(content);

        petButton.setOnAction(e -> {
            currentPetType = petId;
            // In real implementation, update the pet display
        });

        return petButton;
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
        card.setMaxWidth(600);

        Label title = new Label("Achievement Badges");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));
        title.setAlignment(Pos.CENTER);

        VBox badgesList = new VBox(15);

        String[][] badgeData = {
                {"⭐", "First Steps", "Complete your first task", "true", "10"},
                {"⚡", "Focus Master", "Complete 5 pomodoro sessions", "true", "25"},
                {"❤️", "Note Taker", "Create 10 sticky notes", "false", "15"},
                {"👑", "Week Warrior", "Stay productive for 7 days straight", "false", "50"},
                {"🏆", "Mood Tracker", "Log your mood 20 times", "true", "20"}
        };

        for (String[] badge : badgeData) {
            HBox badgeItem = createBadgeItem(badge[0], badge[1], badge[2],
                    Boolean.parseBoolean(badge[3]), Integer.parseInt(badge[4]));
            badgesList.getChildren().add(badgeItem);
        }

        card.getChildren().addAll(title, badgesList);
        return card;
    }

    private HBox createBadgeItem(String icon, String name, String description, boolean earned, int coinReward) {
        HBox badgeItem = new HBox(15);
        badgeItem.setPadding(new Insets(20));
        badgeItem.setStyle(
                "-fx-background-color: " + (earned ? "linear-gradient(to right, #DCFCE7, #BBF7D0)" : "#F3F4F6") + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + (earned ? "#86EFAC" : "#D1D5DB") + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-opacity: " + (earned ? "1.0" : "0.6") + ";"
        );
        badgeItem.setAlignment(Pos.CENTER_LEFT);

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        StackPane iconContainer = new StackPane(iconLabel);
        iconContainer.setPrefSize(40, 40);
        iconContainer.setStyle(
                "-fx-background-color: " + (earned ? "#BBF7D0" : "#E5E7EB") + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );
        iconContainer.setAlignment(Pos.CENTER);

        // Text content
        VBox textContent = new VBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle(forceTextColor(textPrimary));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 14));
        descLabel.setStyle(forceTextColor(textSecondary));

        textContent.getChildren().addAll(nameLabel, descLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Coin reward badge
        if (earned) {
            Pane coinBadge = createBadge("+" + coinReward + " coins", "#FEF3C7", "#D97706");
            badgeItem.getChildren().addAll(iconContainer, textContent, coinBadge);
        } else {
            badgeItem.getChildren().addAll(iconContainer, textContent);
        }

        return badgeItem;
    }

    private VBox createWardrobeTab() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: " + toHex(cardBg) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: " + toHex(borderColor) + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 6);");
        card.setMaxWidth(600);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Wardrobe Shop");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle(forceTextColor(textPrimary));

        Pane coinBadge = createBadge(coins + " coins", "#FEF3C7", "#D97706");

        header.getChildren().addAll(title, coinBadge);

        VBox shopItems = new VBox(15);

        String[][] itemData = {
                {"👑", "Golden Crown", "hat", "100"},
                {"🤓", "Smart Glasses", "accessory", "50"},
                {"🎀", "Fancy Bow Tie", "accessory", "75"},
                {"🎩", "Top Hat", "hat", "80"},
                {"🌸", "Flower Crown", "hat", "60"}
        };

        for (String[] item : itemData) {
            HBox shopItem = createShopItem(item[0], item[1], item[2], Integer.parseInt(item[3]));
            shopItems.getChildren().add(shopItem);
        }

        card.getChildren().addAll(header, shopItems);
        return card;
    }

    private HBox createShopItem(String emoji, String name, String type, int cost) {
        HBox shopItem = new HBox(15);
        shopItem.setPadding(new Insets(20));
        shopItem.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #D1D5DB;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;"
        );
        shopItem.setAlignment(Pos.CENTER_LEFT);

        // Emoji
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(24));

        // Text content
        VBox textContent = new VBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle(forceTextColor(textPrimary));

        Label typeLabel = new Label(type);
        typeLabel.setFont(Font.font("System", 14));
        typeLabel.setStyle(forceTextColor(textSecondary));
        typeLabel.setText(type.substring(0, 1).toUpperCase() + type.substring(1));

        textContent.getChildren().addAll(nameLabel, typeLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Buy button
        Button buyButton = new Button(cost + " coins");
        buyButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #FFB347, #FFCC33);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        shopItem.getChildren().addAll(emojiLabel, textContent, buyButton);
        return shopItem;
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}