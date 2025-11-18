package com.example.demo1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Settings {

    // Pastel color palette matching your dashboard
    private final String PASTEL_PINK = "#FACEEA";
    private final String PASTEL_BLUE = "#C1DAFF";
    private final String PASTEL_LAVENDER = "#D7D8FF";
    private final String PASTEL_PURPLE = "#F0D2F7";
    private final String PASTEL_LILAC = "#E2D6FF";
    private final String PASTEL_ROSE = "#F3D1F3";
    private final String PASTEL_BLUSH = "#FCEDF5";
    private final String PASTEL_SKY = "#E4EFFF";
    private final String PASTEL_MINT = "#C8E6C9";
    private final String PASTEL_PEACH = "#FFDAB9";
    private final String PASTEL_CORAL = "#FFCCBC";
    private final String PASTEL_LEMON = "#FFF9C4";
    private final String PASTEL_IVORY = "#FDF5E7";
    private final String PASTEL_DUSTY_PINK = "#F1DBD0";
    private final String PASTEL_SAGE = "#8D9383";
    private final String PASTEL_FOREST = "#343A26";

    public VBox getContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: " + PASTEL_BLUSH + ";");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = createHeader();

        // Theme Selection
        VBox themeSection = createThemeSelection();

        // Mascot Selection
        VBox mascotSection = createMascotSelection();

        // App Information
        VBox aboutSection = createAboutSection();

        // Quick Actions
        VBox quickActions = createQuickActions();

        mainLayout.getChildren().addAll(header, themeSection, mascotSection, aboutSection, quickActions);
        return mainLayout;
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Settings ⚙️");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(PASTEL_FOREST));

        Label subtitle = new Label("Customize your Évora experience");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setTextFill(Color.web(PASTEL_SAGE));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createThemeSelection() {
        VBox themeCard = createCard("🎨 Choose Your Theme", 800);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Theme options
        HBox themesContainer = new HBox(20);
        themesContainer.setAlignment(Pos.CENTER);

        String[][] themes = {
                {"classic", "🎀 Classic Pastel", "Soft pink, mint, lavender, peach", PASTEL_PINK, PASTEL_LAVENDER},
                {"nature", "🌿 Nature Pastel", "Leafy green, sky blue, sand beige", PASTEL_MINT, PASTEL_PEACH},
                {"galaxy", "🌌 Galaxy Pastel", "Pastel purple, midnight blue, starry accents", PASTEL_PURPLE, PASTEL_LILAC}
        };

        for (String[] theme : themes) {
            VBox themeOption = createThemeOption(theme[0], theme[1], theme[2], theme[3], theme[4]);
            themesContainer.getChildren().add(themeOption);
        }

        content.getChildren().add(themesContainer);
        themeCard.getChildren().add(content);
        return themeCard;
    }

    private VBox createThemeOption(String id, String name, String description, String color1, String color2) {
        VBox themeOption = new VBox(10);
        themeOption.setAlignment(Pos.CENTER);
        themeOption.setPadding(new Insets(20));
        themeOption.setPrefSize(200, 180);
        themeOption.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;",
                color1, color2
        ));

        // Color preview circle
        StackPane colorPreview = new StackPane();
        colorPreview.setPrefSize(60, 60);
        colorPreview.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 30; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);",
                color1, color2
        ));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web(PASTEL_FOREST));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setTextFill(Color.web(PASTEL_FOREST));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(150);

        themeOption.getChildren().addAll(colorPreview, nameLabel, descLabel);

        // Add hover effect
        themeOption.setOnMouseEntered(e -> {
            themeOption.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6); -fx-cursor: hand;",
                    color1, color2
            ));
        });

        themeOption.setOnMouseExited(e -> {
            themeOption.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;",
                    color1, color2
            ));
        });

        return themeOption;
    }

    private VBox createMascotSelection() {
        VBox mascotCard = createCard("Choose Your Mascot", 800);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Mascot options
        GridPane mascotsGrid = new GridPane();
        mascotsGrid.setHgap(15);
        mascotsGrid.setVgap(15);
        mascotsGrid.setAlignment(Pos.CENTER);

        String[][] mascots = {
                {"cat", "🐱 Kitty", "Playful and curious companion", "Purr-fect productivity partner!", PASTEL_PINK},
                {"owl", "🦉 Hoot", "Wise and focused helper", "Wise choices lead to success!", PASTEL_BLUE},
                {"bunny", "🐰 Hop", "Energetic and encouraging friend", "Hop to it! You've got this!", PASTEL_MINT},
                {"bookworm", "🐛 Worm", "Studious and detail-oriented buddy", "Knowledge is the best adventure!", PASTEL_PEACH}
        };

        int col = 0;
        int row = 0;
        for (String[] mascot : mascots) {
            HBox mascotOption = createMascotOption(mascot[0], mascot[1], mascot[2], mascot[3], mascot[4]);
            mascotsGrid.add(mascotOption, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        content.getChildren().add(mascotsGrid);
        mascotCard.getChildren().add(content);
        return mascotCard;
    }

    private HBox createMascotOption(String id, String name, String description, String tagline, String color) {
        HBox mascotOption = new HBox(15);
        mascotOption.setAlignment(Pos.CENTER_LEFT);
        mascotOption.setPadding(new Insets(15));
        mascotOption.setPrefSize(350, 100);
        mascotOption.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-cursor: hand;",
                color
        ));

        // Emoji
        Label emoji = new Label(name.split(" ")[0]); // Get the emoji part
        emoji.setFont(Font.font(24));

        // Text content
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name.substring(3)); // Remove emoji from name
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web(PASTEL_FOREST));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setTextFill(Color.web(PASTEL_SAGE));

        Label taglineLabel = new Label("\"" + tagline + "\"");
        taglineLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        taglineLabel.setTextFill(Color.web(PASTEL_SAGE));
        taglineLabel.setStyle("-fx-font-style: italic;");

        textContent.getChildren().addAll(nameLabel, descLabel, taglineLabel);

        HBox.setHgrow(textContent, Priority.ALWAYS);
        mascotOption.getChildren().addAll(emoji, textContent);

        // Add hover effect
        mascotOption.setOnMouseEntered(e -> {
            mascotOption.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 4); -fx-cursor: hand;",
                    color
            ));
        });

        mascotOption.setOnMouseExited(e -> {
            mascotOption.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-cursor: hand;",
                    color
            ));
        });

        return mascotOption;
    }

    private VBox createAboutSection() {
        VBox aboutCard = createCard("About Évora", 600);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label flower = new Label("🌸");
        flower.setFont(Font.font(48));

        Label title = new Label("Évora");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PASTEL_FOREST));

        Label subtitle = new Label("Your cozy productivity companion");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setTextFill(Color.web(PASTEL_SAGE));

        VBox descriptionBox = new VBox(10);
        descriptionBox.setAlignment(Pos.CENTER);
        descriptionBox.setPadding(new Insets(15));
        descriptionBox.setStyle("-fx-background-color: " + PASTEL_PINK + "; -fx-background-radius: 15;");
        descriptionBox.setMaxWidth(500);

        Label description = new Label("Évora is designed to make productivity fun and stress-free. " +
                "With cute mascots, calming pastel themes, and gentle reminders, " +
                "we're here to help you achieve your goals while feeling good about it! 💖");
        description.setFont(Font.font("Segoe UI", 12));
        description.setTextFill(Color.web(PASTEL_FOREST));
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);

        descriptionBox.getChildren().add(description);

        content.getChildren().addAll(flower, title, subtitle, descriptionBox);
        aboutCard.getChildren().add(content);
        return aboutCard;
    }

    private VBox createQuickActions() {
        VBox actionsCard = createCard("Quick Actions", 800);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(15);
        actionsGrid.setVgap(15);
        actionsGrid.setAlignment(Pos.CENTER);

        String[][] actions = {
                {"📊 Export Data", "Download your productivity data", PASTEL_BLUE},
                {"🔄 Reset Progress", "Start fresh with a clean slate", PASTEL_MINT},
                {"📱 Mobile App", "Get Évora on your phone", PASTEL_LAVENDER},
                {"💬 Send Feedback", "Help us improve Évora", PASTEL_PINK}
        };

        int col = 0;
        int row = 0;
        for (String[] action : actions) {
            VBox actionButton = createActionButton(action[0], action[1], action[2]);
            actionsGrid.add(actionButton, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        content.getChildren().add(actionsGrid);
        actionsCard.getChildren().add(content);
        return actionsCard;
    }

    private VBox createActionButton(String title, String description, String color) {
        VBox actionButton = new VBox(8);
        actionButton.setAlignment(Pos.CENTER_LEFT);
        actionButton.setPadding(new Insets(15));
        actionButton.setPrefSize(350, 80);
        actionButton.setStyle(String.format(
                "-fx-background-color: white; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 15; " +
                        "-fx-background-radius: 15; -fx-cursor: hand;",
                color
        ));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(PASTEL_FOREST));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setTextFill(Color.web(PASTEL_SAGE));

        actionButton.getChildren().addAll(titleLabel, descLabel);

        // Add hover effect
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 15; " +
                            "-fx-background-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);",
                    lightenColor(color), color
            ));
        });

        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle(String.format(
                    "-fx-background-color: white; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 15; " +
                            "-fx-background-radius: 15; -fx-cursor: hand;",
                    color
            ));
        });

        return actionButton;
    }

    private VBox createCard(String title, double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setStyle("-fx-background-color: " + PASTEL_IVORY + "; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(PASTEL_FOREST));
        titleLabel.setPadding(new Insets(20, 20, 10, 20));
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().add(titleLabel);
        return card;
    }

    private String lightenColor(String color) {
        // Simple color lightening for hover effects
        return color + "80"; // Add transparency to lighten
    }
}