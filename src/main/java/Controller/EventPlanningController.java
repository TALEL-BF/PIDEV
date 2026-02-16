package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.entities.PlanningRow;
import com.auticare.services.EventServices;
import com.auticare.services.FallbackService;
import com.auticare.controllers.EventHome;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventPlanningController {

    @FXML private Button backButton;
    @FXML private Label eventTitleLabel;
    @FXML private ProgressBar loadingBar;
    @FXML private Button generateBtn;
    @FXML private Button savePlanningBtn;
    @FXML private Button deletePlanningBtn;
    @FXML private Button addRowBtn;
    @FXML private Button removeRowBtn;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label locationLabel;
    @FXML private Label participantsLabel;
    @FXML private Label typeLabel;
    @FXML private TextArea planningArea;
    @FXML private Button genererPlanningBtn;
    private FallbackService fallbackService;
    @FXML private VBox planningContainer;
    @FXML private VBox scheduleContainer;
    @FXML private Label conseilLabel;
    @FXML private Label preparationLabel;
    @FXML private VBox emptyStateContainer;

    private Event currentEvent;
    private EventHome eventHome;


    private List<PlanningRow> planningRows = new ArrayList<>();


    public void setEventHome(EventHome controller) {
        this.eventHome = controller;
        System.out.println("✅ EventHome lié au EventPlanningController");
    }

    @FXML
    public void initialize() {
        String groqKey = "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";
        fallbackService = new FallbackService(groqKey);

        // Configurer les boutons
        setupButtons();
    }

    private void setupButtons() {
        backButton.setOnAction(e -> goBack());
        generateBtn.setOnAction(e -> generatePlanning());
        savePlanningBtn.setOnAction(e -> savePlanning());
        deletePlanningBtn.setOnAction(e -> deletePlanning());
        addRowBtn.setOnAction(e -> addPlanningRow("00:00-00:30", "Nouvelle activité"));
        removeRowBtn.setOnAction(e -> {
            if (!scheduleContainer.getChildren().isEmpty()) {
                scheduleContainer.getChildren().remove(scheduleContainer.getChildren().size() - 1);
                if (!planningRows.isEmpty()) {
                    planningRows.remove(planningRows.size() - 1);
                }
            }
        });

        // Si le bouton genererPlanningBtn existe, le configurer aussi
        if (genererPlanningBtn != null) {
            genererPlanningBtn.setOnAction(e -> genererPlanningAvecFallback());
        }
    }

    // ✅ NOUVELLE MÉTHODE - Utilise FallbackService
    private void genererPlanningAvecFallback() {
        if (currentEvent == null) return;

        genererPlanningBtn.setDisable(true);
        genererPlanningBtn.setText("⏳ Génération...");
        if (planningArea != null) {
            planningArea.setText("Génération du planning...");
        }

        new Thread(() -> {
            String planning = fallbackService.genererPlanning(currentEvent);

            javafx.application.Platform.runLater(() -> {
                if (planningArea != null) {
                    planningArea.setText(planning);
                }
                genererPlanningBtn.setDisable(false);
                genererPlanningBtn.setText("📅 Générer planning");
            });
        }).start();
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        updateEventInfo();

        // Vérifier si un planning existe déjà
        if (event.getPlanning() != null && !event.getPlanning().isEmpty()) {
            try {
                JsonObject existingPlanning = JsonParser.parseString(event.getPlanning()).getAsJsonObject();
                displayExistingPlanning(existingPlanning);

                // ✅ GARDER le bouton de génération actif pour qu'il puisse regénérer
                generateBtn.setDisable(false);
                generateBtn.setText("✨ Regénérer le planning avec IA ✨");

                // Afficher le bouton supprimer
                deletePlanningBtn.setVisible(true);
                deletePlanningBtn.setManaged(true);

            } catch (Exception e) {
                System.out.println("❌ Erreur parsing planning existant: " + e.getMessage());
            }
        } else {
            // Pas de planning
            deletePlanningBtn.setVisible(false);
            deletePlanningBtn.setManaged(false);
            generateBtn.setDisable(false);
            generateBtn.setText("✨ Générer le planning avec IA ✨");
        }
    }



    // ✅ AJOUTER cette méthode pour recevoir FallbackService depuis EventTableController
    public void setFallbackService(FallbackService service) {
        this.fallbackService = service;
        System.out.println("✅ FallbackService injecté dans EventPlanningController");
    }

    private void updateEventInfo() {
        if (currentEvent == null) return;

        eventTitleLabel.setText("Planning - " + currentEvent.getTitre());
        dateLabel.setText(formatDate(currentEvent.getDateDebut()));
        timeLabel.setText(formatTime(currentEvent.getHeureDebut()) + " - " + formatTime(currentEvent.getHeureFin()));
        locationLabel.setText(currentEvent.getLieu());
        participantsLabel.setText(currentEvent.getMaxParticipant() + " participants");
        typeLabel.setText(currentEvent.getTypeEvent());
    }

    private void generatePlanning() {
        loadingBar.setVisible(true);
        generateBtn.setDisable(true);
        generateBtn.setText("⏳ Génération en cours...");

        new Thread(() -> {
            try {
                // ✅ Utiliser FallbackService au lieu de GeminiService directement
                String prompt = String.format(
                        "Génère un planning pour un événement pour enfants autistes avec ces informations:\n" +
                                "Titre: %s\nDescription: %s\nLieu: %s\nParticipants: %d\nType: %s\n\n" +
                                "IMPORTANT: Crée des activités basées UNIQUEMENT sur le TITRE de l'événement.\n" +
                                "Retourne UNIQUEMENT un objet JSON avec cette structure:\n" +
                                "{\n" +
                                "  \"schedule\": [\n" +
                                "    {\"time\": \"14:00-14:30\", \"activity\": \"activité basée sur le titre\"},\n" +
                                "    {\"time\": \"14:30-15:30\", \"activity\": \"autre activité\"},\n" +
                                "    {\"time\": \"15:30-15:45\", \"activity\": \"pause sensorielle\"},\n" +
                                "    {\"time\": \"15:45-16:45\", \"activity\": \"autre activité\"},\n" +
                                "    {\"time\": \"16:45-17:00\", \"activity\": \"clôture\"}\n" +
                                "  ],\n" +
                                "  \"conseil\": \"Conseil personnalisé...\",\n" +
                                "  \"preparation\": \"Liste des choses à préparer...\"\n" +
                                "}",
                        currentEvent.getTitre(),
                        currentEvent.getDescription(),
                        currentEvent.getLieu(),
                        currentEvent.getMaxParticipant(),
                        currentEvent.getTypeEvent()

                );

                // ✅ Utiliser FallbackService au lieu de GeminiService
                String resultat = fallbackService.genererDescription(prompt);
                System.out.println("📥 Réponse Fallback: " + resultat);

                // Nettoyer la réponse
                resultat = nettoyerJSON(resultat);
                JsonObject json = JsonParser.parseString(resultat).getAsJsonObject();

                javafx.application.Platform.runLater(() -> {
                    displayGeneratedPlanning(json);
                    loadingBar.setVisible(false);
                    generateBtn.setDisable(false);
                    generateBtn.setText("✨ Générer le planning avec IA ✨");
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loadingBar.setVisible(false);
                    generateBtn.setDisable(false);
                    generateBtn.setText("✨ Générer le planning avec IA ✨");
                    showAlert("Erreur", "Impossible de générer le planning: " + e.getMessage());
                });
            }
        }).start();
    }

    private String nettoyerJSON(String texte) {
        int debut = texte.indexOf("{");
        int fin = texte.lastIndexOf("}");
        if (debut != -1 && fin != -1) {
            return texte.substring(debut, fin + 1);
        }
        return texte;
    }

    private void displayGeneratedPlanning(JsonObject json) {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        planningContainer.setVisible(true);
        planningContainer.setManaged(true);

        scheduleContainer.getChildren().clear();
        planningRows.clear();

        if (json.has("schedule")) {
            JsonArray schedule = json.getAsJsonArray("schedule");
            for (var item : schedule) {
                JsonObject slot = item.getAsJsonObject();
                String time = slot.get("time").getAsString();
                String activity = slot.get("activity").getAsString();
                addPlanningRow(time, activity);
            }
        }

        // ✅ Gestion du conseil (peut être string ou objet)
        if (json.has("conseil")) {
            if (json.get("conseil").isJsonArray()) {
                JsonArray conseilArray = json.getAsJsonArray("conseil");
                StringBuilder conseilText = new StringBuilder();
                for (var item : conseilArray) {
                    conseilText.append("• ").append(item.getAsString()).append("\n");
                }
                conseilLabel.setText(conseilText.toString());
            } else {
                conseilLabel.setText(json.get("conseil").getAsString());
            }
        }

        // ✅ Gestion de la préparation (tableau ou string)
        if (json.has("preparation")) {
            if (json.get("preparation").isJsonArray()) {
                JsonArray prepArray = json.getAsJsonArray("preparation");
                StringBuilder prepText = new StringBuilder();
                for (var item : prepArray) {
                    prepText.append("• ").append(item.getAsString()).append("\n");
                }
                preparationLabel.setText(prepText.toString());
            } else {
                preparationLabel.setText(json.get("preparation").getAsString());
            }
        }

        FadeTransition fade = new FadeTransition(Duration.millis(500), planningContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void displayExistingPlanning(JsonObject planning) {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        planningContainer.setVisible(true);
        planningContainer.setManaged(true);

        scheduleContainer.getChildren().clear();
        planningRows.clear();

        if (planning.has("schedule")) {
            JsonArray schedule = planning.getAsJsonArray("schedule");
            for (var item : schedule) {
                JsonObject slot = item.getAsJsonObject();
                String time = slot.get("time").getAsString();
                String activity = slot.get("activity").getAsString();
                addPlanningRow(time, activity);
            }
        }

        if (planning.has("conseils")) {
            conseilLabel.setText(planning.get("conseils").getAsString());
        }

        if (planning.has("preparation")) {
            preparationLabel.setText(planning.get("preparation").getAsString());
        }
    }

    private void addPlanningRow(String time, String activity) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField timeField = new TextField(time);
        timeField.setPrefWidth(100);
        timeField.setStyle("-fx-background-color: white; -fx-border-color: #c4b5fd; -fx-border-radius: 5; -fx-padding: 5;");

        TextField activityField = new TextField(activity);
        activityField.setPrefWidth(300);
        activityField.setStyle("-fx-background-color: white; -fx-border-color: #c4b5fd; -fx-border-radius: 5; -fx-padding: 5;");

        Button deleteBtn = new Button("✖");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            scheduleContainer.getChildren().remove(row);
            planningRows.removeIf(r -> r.getTime().equals(time) && r.getActivity().equals(activity));
            updateEventHoursFromPlanning(); // ← AJOUTE ÇA
        });

        // ✅ Détecter les changements d'heure
        timeField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.equals(old)) {
                updateEventHoursFromPlanning();
            }
        });

        row.getChildren().addAll(timeField, activityField, deleteBtn);

        int rowIndex = scheduleContainer.getChildren().size();

        activityField.textProperty().addListener((obs, old, newValue) -> {
            if (rowIndex < planningRows.size()) {
                planningRows.get(rowIndex).setActivity(newValue);
            }
        });

        scheduleContainer.getChildren().add(row);
        planningRows.add(new PlanningRow(time, activity));
    }
    private void updateEventHoursFromPlanning() {
        if (scheduleContainer.getChildren().isEmpty()) return;

        try {
            // Première ligne = heure de début
            HBox firstRow = (HBox) scheduleContainer.getChildren().get(0);
            TextField firstTimeField = (TextField) firstRow.getChildren().get(0);
            String firstTime = firstTimeField.getText().split("-")[0].trim();

            // Dernière ligne = heure de fin
            HBox lastRow = (HBox) scheduleContainer.getChildren().get(scheduleContainer.getChildren().size() - 1);
            TextField lastTimeField = (TextField) lastRow.getChildren().get(0);
            String lastTime = lastTimeField.getText().split("-")[1].trim();

            // Mettre à jour les champs dans l'interface (si présents)
            // Note: Ces champs sont dans EventDetails, pas ici

            System.out.println("🕐 Nouveaux horaires: " + firstTime + " → " + lastTime);

            // Option: Afficher une alerte pour prévenir
            Label infoLabel = new Label("⚠️ N'oublie pas de mettre à jour l'événement");
            infoLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px;");

        } catch (Exception e) {
            System.out.println("❌ Erreur parsing heure: " + e.getMessage());
        }
    }

    private void savePlanning() {
        try {
            if (planningRows.isEmpty()) {
                showAlert("Attention", "Aucun planning à sauvegarder");
                return;
            }

            JsonObject planningJson = new JsonObject();
            JsonArray scheduleArray = new JsonArray();

            String newStartTime = null;
            String newEndTime = null;

            for (int i = 0; i < scheduleContainer.getChildren().size(); i++) {
                HBox row = (HBox) scheduleContainer.getChildren().get(i);
                TextField timeField = (TextField) row.getChildren().get(0);
                TextField activityField = (TextField) row.getChildren().get(1);

                JsonObject slot = new JsonObject();
                String timeValue = timeField.getText();
                slot.addProperty("time", timeValue);
                slot.addProperty("activity", activityField.getText());
                scheduleArray.add(slot);

                // Extraire les heures pour l'événement
                String[] times = timeValue.split("-");
                if (i == 0 && times.length > 0) {
                    newStartTime = times[0].trim();
                }
                if (i == scheduleContainer.getChildren().size() - 1 && times.length > 1) {
                    newEndTime = times[1].trim();
                }
            }

            planningJson.add("schedule", scheduleArray);
            planningJson.addProperty("conseils", conseilLabel.getText());
            planningJson.addProperty("preparation", preparationLabel.getText());
            planningJson.addProperty("generatedAt", LocalDateTime.now().toString());
            planningJson.addProperty("lastModified", LocalDateTime.now().toString());

            String planningString = planningJson.toString();

            // ✅ Mettre à jour l'événement avec les nouveaux horaires
            if (newStartTime != null && newEndTime != null) {
                try {
                    // Convertir String en Time
                    java.sql.Time sqlStartTime = java.sql.Time.valueOf(newStartTime + ":00");
                    java.sql.Time sqlEndTime = java.sql.Time.valueOf(newEndTime + ":00");

                    currentEvent.setHeureDebut(sqlStartTime);
                    currentEvent.setHeureFin(sqlEndTime);
                    System.out.println("✅ Heures de l'événement mises à jour");
                } catch (Exception e) {
                    System.out.println("⚠️ Erreur conversion heures: " + e.getMessage());
                }
            }

            currentEvent.setPlanning(planningString);

            EventServices eventServices = new EventServices();
            eventServices.modifierEvent(currentEvent);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("✅ Planning sauvegardé !");
            alert.setContentText("Le planning a été sauvegardé avec succès.");

            alert.showAndWait().ifPresent(response -> {
                goBackToEventHome();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de sauvegarder le planning: " + e.getMessage());
        }
    }
    private void goBackToEventHome() {
        try {
            if (eventHome != null) {
                eventHome.loadEventsTable();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventTable.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Liste des événements");
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste: " + e.getMessage());
        }
    }

    private void deletePlanning() {
        // Demander confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le planning");
        confirm.setContentText("Es-tu sûr de vouloir supprimer ce planning ? Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer de la base de données
                    EventServices eventServices = new EventServices();
                    eventServices.supprimerPlanning(currentEvent.getIdEvent());

                    // Mettre à jour l'objet courant
                    currentEvent.setPlanning(null);

                    // ✅ Afficher message et retourner à EventHome
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText("🗑️ Planning supprimé");
                    success.setContentText("Le planning a été supprimé de la base de données.");

                    success.showAndWait().ifPresent(resp -> {
                        goBackToEventHome();  // ← Utilise la méthode modifiée
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de supprimer le planning: " + e.getMessage());
                }
            }
        });
    }

    private void goBack() {
        try {
            if (eventHome != null) {
                eventHome.loadEventsTable();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventTable.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Liste des événements");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        String[] months = {"janv.", "févr.", "mars", "avr.", "mai", "juin",
                "juil.", "août", "sept.", "oct.", "nov.", "déc."};
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        return day + " " + months[month] + " " + parts[0];
    }

    private String formatTime(java.sql.Time time) {
        if (time == null) return "?";
        return time.toString().substring(0, 5);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}