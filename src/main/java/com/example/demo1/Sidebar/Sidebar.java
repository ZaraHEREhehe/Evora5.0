package com.example.demo1.Sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX Sidebar component
 */
public class Sidebar extends VBox {

    private final SidebarController controller;
    private final Map<String, Button> navButtons = new HashMap<>();

    public Sidebar(SidebarController controller, String userName) {
        this.controller = controller;
        this.setPrefWidth(220);
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setAlignment(Pos.TOP_CENTER);
        this.setBackground(new Background(new BackgroundFill(
                Color.web("#ffffff", 0.7), CornerRadii.EMPTY, Insets.EMPTY)));
        this.setEffect(new DropShadow(8, Color.gray(0, 0.2)));

        createHeader(userName);
        createNavButtons();
        createMascotSection();
    }

    private void createHeader(String userName) {
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0,0,10,0));

        Label title = new Label("Évora");
        title.setFont(Font.font("Poppins", 20));
        title.setTextFill(Color.web("#5c5470"));

        Label userLabel = new Label("Hello, " + userName + "! 👋");
        userLabel.setFont(Font.font("Poppins", 13));
        userLabel.setTextFill(Color.web("#9189a5"));

        header.getChildren().addAll(title, userLabel);
        this.getChildren().add(header);
    }

    private void createNavButtons() {
        VBox navBox = new VBox(10);
        navBox.setAlignment(Pos.TOP_CENTER);

        // Navigation items: id -> label, startColor, endColor
        String[][] items = {
                {"dashboard", "Home", "#ffb6b9", "#ff8fa3"},
                {"todos", "To-Do", "#a8e6cf", "#dcedc1"},
                {"calendar", "Calendar", "#80cbc4", "#4db6ac"},
                {"notes", "Notes", "#fff5ba", "#ffe8a3"},
                {"timer", "Timer", "#ffd3b6", "#ffaaa5"},
                {"pet", "Pet", "#ffb6b9", "#fae3d9"},
                {"mood", "Mood", "#d1c4e9", "#9575cd"},
                {"music", "Music", "#bbdefb", "#64b5f6"},
                {"stats", "Analytics", "#a18cd1", "#fbc2eb"},
                {"settings", "Settings", "#cfd8dc", "#b0bec5"}
        };

        for (String[] item : items) {
            Button btn = createNavButton(item[0], item[1], item[2], item[3]);
            navButtons.put(item[0], btn);
            navBox.getChildren().add(btn);
        }

        this.getChildren().add(navBox);
    }

    private Button createNavButton(String id, String label, String startColor, String endColor) {
        Button btn = new Button(label);
        btn.setPrefWidth(180);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Poppins", 14));
        btn.setTextFill(Color.web("#5c5470"));
        btn.setBackground(new Background(new BackgroundFill(Color.web("#ffffff",0.3),
                new CornerRadii(15), Insets.EMPTY)));
        btn.setEffect(new DropShadow(4, Color.gray(0,0.15)));

        btn.setOnAction(e -> {
            highlightButton(id);
            controller.navigate(id);
        });

        return btn;
    }

    private void highlightButton(String activeId) {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(activeId)) {
                entry.getValue().setBackground(new Background(new BackgroundFill(
                        Color.web("#ffd3b6"), new CornerRadii(15), Insets.EMPTY)));
                entry.getValue().setTextFill(Color.WHITE);
            } else {
                entry.getValue().setBackground(new Background(new BackgroundFill(
                        Color.web("#ffffff",0.3), new CornerRadii(15), Insets.EMPTY)));
                entry.getValue().setTextFill(Color.web("#5c5470"));
            }
        }
    }

    private void createMascotSection() {
        VBox mascotBox = new VBox(10);
        mascotBox.setAlignment(Pos.CENTER);
        mascotBox.setPadding(new Insets(15,0,0,0));

        Label mascotPlaceholder = new Label("🦉 Mascot Placeholder");
        mascotPlaceholder.setFont(Font.font("Poppins", 18));

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setPrefWidth(160);
        logoutBtn.setFont(Font.font("Poppins", 14));
        logoutBtn.setTextFill(Color.web("#e53935"));
        logoutBtn.setBackground(new Background(new BackgroundFill(
                Color.web("#ffffff",0.2), new CornerRadii(15), Insets.EMPTY
        )));
        logoutBtn.setOnAction(e -> System.out.println("Logging out..."));

        mascotBox.getChildren().addAll(mascotPlaceholder, logoutBtn);
        this.getChildren().add(mascotBox);
    }
}
