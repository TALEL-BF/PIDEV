package com.auticare.controllers;

import com.auticare.services.FallbackService;
import com.auticare.entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;


public class RapportIAController {

    @FXML private Button pdfButton;
    @FXML private TextArea rapportArea;
    @FXML private Button genererBtn;
    @FXML private Button backButton;
    @FXML private TextField sujetField;

    private Event currentEvent;
    private FallbackService fallbackService;
    private String dernierRapport = "";
    private String dernierSujet = "";

    // ========== MÉTHODES ==========
    public void setFallbackService(FallbackService service) {
        this.fallbackService = service;
        System.out.println("✅ FallbackService reçu dans RapportIAController");
    }

    @FXML
    public void initialize() {
        genererBtn.setOnAction(e -> genererRapport());
        backButton.setOnAction(e -> goBackToShowEvent());  // ← GARDE LE NOM
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        System.out.println("✅ Événement reçu dans RapportIAController: " + event.getTitre());
    }

    private void genererRapport() {
        String sujet = sujetField.getText().trim();

        if (sujet.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un sujet");
            return;
        }

        genererBtn.setDisable(true);
        genererBtn.setText("⏳ Génération...");
        rapportArea.setText("🤖 Génération du rapport sur \"" + sujet + "\"...\n\nVeuillez patienter...");

        new Thread(() -> {
            try {
                String rapport;

                // ✅ Utiliser FallbackService si disponible
                if (fallbackService != null) {
                    System.out.println("🔄 Utilisation de FallbackService pour le rapport");
                    rapport = fallbackService.genererRapport(sujet);
                } else {
                    System.out.println("⚠️ FallbackService non disponible");
                    rapport = "Erreur: Service non disponible";
                }

                dernierRapport = rapport;
                dernierSujet = sujet;

                javafx.application.Platform.runLater(() -> {
                    rapportArea.setText(rapport);
                    genererBtn.setDisable(false);
                    genererBtn.setText("Générer le rapport");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    rapportArea.setText("❌ Erreur : " + e.getMessage());
                    genererBtn.setDisable(false);
                    genererBtn.setText("Générer le rapport");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setSujet(String sujet) {
        if (sujetField != null) {
            sujetField.setText(sujet);  // ← Met le titre dans le champ
            System.out.println("✅ Sujet défini: " + sujet);

            genererRapport();
        } else {
            System.err.println("⚠️ sujetField est null dans setSujet");
        }
    }

    @FXML
    private void goBackToShowEvent() {  // ← GARDE LE NOM mais retourne aux DÉTAILS
        try {
            if (currentEvent == null) {
                showAlert("Erreur", "Aucun événement à afficher");
                return;
            }

            // ✅ Retourne à EventDetailsPage (pas à ShowEvent)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventDetailsPage.fxml"));
            Parent root = loader.load();

            // Passer l'événement au contrôleur de détails
            EventDetailsController controller = loader.getController();
            controller.setEvent(currentEvent);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails - " + currentEvent.getTitre());
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner aux détails: " + e.getMessage());
        }
    }
}