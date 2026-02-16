package com.auticare.controllers;  // ← CHANGÉ

import com.auticare.entities.Event;  // ← CHANGÉ
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EventPlanningUserController {

    @FXML private VBox planningContainer;
    @FXML private VBox emptyStateContainer;
    @FXML private Button closeButton;

    private Event currentEvent;

    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> closeWindow());
    }

    public void setEvent(Event event) {
        this.currentEvent = event;

        // Charger le planning s'il existe
        if (event.getPlanning() != null && !event.getPlanning().isEmpty()) {
            try {
                JsonObject planning = JsonParser.parseString(event.getPlanning()).getAsJsonObject();
                displayPlanning(planning);
            } catch (Exception e) {
                System.out.println("❌ Erreur parsing planning: " + e.getMessage());
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private void displayPlanning(JsonObject planning) {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        planningContainer.setVisible(true);
        planningContainer.setManaged(true);
        planningContainer.getChildren().clear();

        if (planning.has("schedule")) {
            JsonArray schedule = planning.getAsJsonArray("schedule");

            // Ajouter chaque activité sous forme de ligne simple
            for (int i = 0; i < schedule.size(); i++) {
                JsonObject slot = schedule.get(i).getAsJsonObject();
                if (slot.has("time") && slot.has("activity")) {
                    String time = slot.get("time").getAsString();
                    String activity = slot.get("activity").getAsString();

                    HBox activityRow = createSimpleActivityRow(time, activity);
                    planningContainer.getChildren().add(activityRow);
                }
            }
        }
    }

    private HBox createSimpleActivityRow(String time, String activity) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5 0;");

        Label timeLabel = new Label("🕐 " + time);
        timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4c1d95; -fx-min-width: 100;");

        // Obtenir un emoji selon l'activité
        String emoji = getEmojiForActivity(activity);
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 18px; -fx-min-width: 35;");

        Label activityLabel = new Label(activity);
        activityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d3748;");
        activityLabel.setWrapText(true);

        row.getChildren().addAll(timeLabel, emojiLabel, activityLabel);
        return row;
    }

    private String getEmojiForActivity(String activity) {
        String act = activity.toLowerCase();
        if (act.contains("accueil")) return "👋";
        if (act.contains("médit") || act.contains("yoga")) return "🧘";
        if (act.contains("atelier") || act.contains("peinture") || act.contains("créatif")) return "🎨";
        if (act.contains("pause") || act.contains("repas")) return "🍽️";
        if (act.contains("jeu") || act.contains("collectif")) return "🎮";
        if (act.contains("sensoriel")) return "🌿";
        if (act.contains("départ") || act.contains("clôture")) return "👋";
        if (act.contains("calme")) return "😌";
        return "📅";
    }

    private void showEmptyState() {
        planningContainer.setVisible(false);
        planningContainer.setManaged(false);
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}