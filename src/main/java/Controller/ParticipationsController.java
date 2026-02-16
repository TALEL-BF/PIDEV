package com.auticare.controllers;

import com.auticare.entities.Participation;
import com.auticare.entities.Event;
import com.auticare.services.ParticipationService;
import com.auticare.services.EventServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;
import java.util.Random;

public class ParticipationsController implements Initializable {

    @FXML private Label eventTitleLabel;
    @FXML private FlowPane participantsGrid;
    @FXML private VBox emptyMessage;

    private ParticipationService participationService;
    private EventServices eventServices;
    private Event currentEvent;

    // Liste d'avatars mignons pour les enfants
    private String[] avatars = {
            "🦊", "🐼", "🐨", "🦁", "🐸", "🐧", "🦉", "🐝", "🐳", "🦋",
            "🐌", "🐞", "🦄", "🐶", "🐱", "🐭", "🐹", "🐰", "🦝", "🐮"
    };

    // Couleurs pastel pour les cartes
    private String[] cardColors = {
            "#FFB6C1", "#B0E0E6", "#C1E1C1", "#FFDAB9", "#E6E6FA",
            "#FADADD", "#D8BFD8", "#B0E57C", "#FBCEB1", "#C4A484"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        participationService = new ParticipationService();
        eventServices = new EventServices();
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        eventTitleLabel.setText("🎉 " + event.getTitre() + " 🎉");
        chargerParticipants();
    }

    private void chargerParticipants() {
        List<Participation> participations = participationService
                .afficherParticipationsParEvent(currentEvent.getIdEvent());

        if (participations.isEmpty()) {
            emptyMessage.setVisible(true);
            emptyMessage.setManaged(true);
            participantsGrid.setVisible(false);
        } else {
            emptyMessage.setVisible(false);
            emptyMessage.setManaged(false);
            participantsGrid.setVisible(true);
            participantsGrid.getChildren().clear();

            for (Participation p : participations) {
                participantsGrid.getChildren().add(creerCarteParticipant(p));
            }
        }
    }

    private VBox creerCarteParticipant(Participation p) {
        Random random = new Random();
        String avatar = avatars[random.nextInt(avatars.length)];
        String color = cardColors[random.nextInt(cardColors.length)];

        VBox card = new VBox(15);
        card.setPrefWidth(200);
        card.setPrefHeight(250);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // Avatar géant
        Label avatarLabel = new Label(avatar);
        avatarLabel.setStyle("-fx-font-size: 60px; -fx-background-color: white; -fx-background-radius: 60; -fx-padding: 15;");

        // Nom (anonyme pour l'instant)
        Label nameLabel = new Label("👤 Ami");
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");

        // Date de participation
        String dateStr = p.getParticipation_date()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d3748;");

        // Statut avec émoji
        String statusEmoji = p.getStatus().equals("Confirme") ? "✅" : "⏳";
        Label statusLabel = new Label(statusEmoji + " " + p.getStatus());
        statusLabel.setStyle(
                "-fx-background-color: " + (p.getStatus().equals("Confirme") ? "#10B981" : "#F59E0B") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 15;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;"
        );

        // Bouton annuler (seulement si c'est la participation de l'utilisateur)
        // Pour l'instant, on le met pour toutes les cartes (à adapter avec SessionManager)
        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );

        cancelBtn.setOnAction(e -> handleCancelParticipation(p));

        // Effet de hover sur la carte
        card.setOnMouseEntered(ev -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
            card.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 20;" +
                            "-fx-border-color: #8b5cf6;" +
                            "-fx-border-width: 4;" +
                            "-fx-border-radius: 30;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.3), 15, 0, 0, 8);"
            );
        });

        card.setOnMouseExited(ev -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 20;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 4;" +
                            "-fx-border-radius: 30;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
            );
        });

        card.getChildren().addAll(avatarLabel, nameLabel, dateLabel, statusLabel, cancelBtn);
        return card;
    }

    private void handleCancelParticipation(Participation participation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("🎈 Oh non !");
        alert.setContentText("Veux-tu vraiment annuler ta participation ?");

        // Style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F0E6FF; -fx-background-radius: 30; -fx-border-color: #7B2FF7; -fx-border-width: 3; -fx-border-radius: 30;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            participationService.annulerParticipation(
                    participation.getUser_id(),
                    participation.getEvent_id()
            );

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText("🎉 C'est annulé !");
            success.setContentText("Tu pourras revenir quand tu veux !");

            DialogPane successPane = success.getDialogPane();
            successPane.setStyle("-fx-background-color: #E6FFE6; -fx-background-radius: 30; -fx-border-color: #10B981; -fx-border-width: 3; -fx-border-radius: 30;");

            success.showAndWait();

            chargerParticipants(); // Recharger la liste
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventDetailsPage.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(currentEvent);

            Stage stage = (Stage) eventTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(currentEvent.getTitre());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'événement");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}