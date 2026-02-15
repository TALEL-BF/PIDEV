package com.auticare.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

@Component
public class EventsDashboardController {

    @FXML
    private FlowPane eventsGrid;

    @FXML
    private Label eventsCountLabel;

    @FXML
    public void initialize() {
        populateEvents();
    }

    private void populateEvents() {
        // Mocking event cards based on the user's template
        eventsGrid.getChildren().clear();

        eventsGrid.getChildren()
                .add(createEventCard("bawbaw", "20 Fév", "14:30 - 16:30", "bizerte", "13 participants"));
        eventsGrid.getChildren().add(createEventCard("zoooo", "20 Fév", "14:30 - 16:30", "bizerte", "12 participants"));
        eventsGrid.getChildren()
                .add(createEventCard("cuisine", "13 Fév", "14:30 - 18:30", "bizerte", "11 participants"));
        eventsGrid.getChildren()
                .add(createEventCard("playyy", "13 Fév", "14:30 - 16:30", "bizerte", "10 participants"));
    }

    private VBox createEventCard(String title, String date, String time, String location, String participants) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(220);
        card.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        // Thumbnail placeholder
        VBox thumb = new VBox();
        thumb.setPrefHeight(130);
        thumb.setStyle("-fx-background-color: #d1d8e0; -fx-background-radius: 15 15 0 0;");

        // Date Badge Overlay (Simulated)
        StackPane thumbStack = new StackPane();
        Label dateBadge = new Label(date);
        dateBadge.getStyleClass().add("date-badge");
        StackPane.setAlignment(dateBadge, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(dateBadge, new javafx.geometry.Insets(10));

        thumbStack.getChildren().addAll(thumb, dateBadge);

        // Content
        VBox content = new VBox(5);
        content.setPadding(new javafx.geometry.Insets(0, 15, 0, 15));

        Label timeLoc = new Label("🕒 " + time + "  📍 " + location);
        timeLoc.getStyleClass().add("event-meta");
        timeLoc.setStyle(
                "-fx-background-color: #a29bfe; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

        Label eventTitle = new Label(title);
        eventTitle.getStyleClass().add("event-title");

        Label parts = new Label("👥 " + participants);
        parts.getStyleClass().add("event-meta");

        content.getChildren().addAll(timeLoc, eventTitle, parts);

        card.getChildren().addAll(thumbStack, content);
        return card;
    }
}
