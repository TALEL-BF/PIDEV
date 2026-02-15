package Controller;

import Services.GeminiService;
import Services.PDFGeneratorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;  // ← Nécessaire// ✅ C'est le bouton JavaFX

import java.awt.*;


import java.io.File;
import java.io.IOException;

public class RapportIAController {


    @FXML private Button pdfButton;  // ← Déclaré
    @FXML private TextArea rapportArea;
    @FXML private Button genererBtn;
    @FXML private Button backButton;
    @FXML private TextField sujetField;
     // ← Bouton PDF

    private GeminiService geminiService = new GeminiService();
    private String dernierRapport = "";  // ← Stocke le dernier rapport généré
    private String dernierSujet = "";    // ← Stocke le dernier sujet

    @FXML
    public void initialize() {
        genererBtn.setOnAction(e -> genererRapport());

        // ✅ Initialiser le bouton PDF
        if (pdfButton != null) {
            pdfButton.setDisable(true);  // Désactivé au début
            pdfButton.setOnAction(e -> genererPDF());
        }
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

        // Désactiver le bouton PDF pendant la génération
        if (pdfButton != null) pdfButton.setDisable(true);

        new Thread(() -> {
            try {
                String rapport = geminiService.genererRapport(sujet);

                // ✅ Sauvegarder pour le PDF
                dernierRapport = rapport;
                dernierSujet = sujet;

                javafx.application.Platform.runLater(() -> {
                    rapportArea.setText(rapport);
                    genererBtn.setDisable(false);
                    genererBtn.setText("Générer le rapport");

                    // ✅ Activer le bouton PDF
                    if (pdfButton != null) pdfButton.setDisable(false);
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

    @FXML
    private void genererPDF() {
        if (dernierRapport.isEmpty()) {
            showAlert("Erreur", "Aucun rapport à exporter. Générez d'abord un rapport.");
            return;
        }

        try {
            PDFGeneratorService pdfService = new PDFGeneratorService();

            // ✅ Générer le PDF avec le rapport déjà créé
            String chemin = pdfService.genererPDF(dernierRapport, dernierSujet);

            showAlert("Succès", "✅ PDF généré avec succès !\n" + chemin);

            // Ouvrir le dossier contenant le PDF
            try {
                Desktop.getDesktop().open(new File("rapports"));
            } catch (Exception ex) {
                System.out.println("📁 Dossier: " + new File("rapports").getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            sujetField.setText(sujet);
            genererRapport();  // Génère automatiquement
        } else {
            System.err.println("⚠️ sujetField est null dans setSujet");
        }
    }
    @FXML
    private void goBackToShowEvent() {  // ← Nouveau nom
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowEvent.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner aux événements");
        }
    }
}