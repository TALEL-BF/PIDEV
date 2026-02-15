package Controller;

import Entites.Event;
import Services.AssemblyAIEmotionService;
import Services.AssemblyAIEmotionService.EmotionResult;
import Services.AudioRecorderJDK;
import Services.EventServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class EmotionDetectionController {

    @FXML private Button recordButton;
    @FXML private Label statusLabel;
    @FXML private Label emotionLabel;
    @FXML private Label conseilLabel;  // ✅ NOUVEAU
    @FXML private ProgressBar confidenceBar;
    @FXML private VBox eventsContainer;
    @FXML private Button backButton;

    private AudioRecorderJDK recorder;
    @FXML private VBox mainCard;  // ← Ajoute ceci
    private AssemblyAIEmotionService emotionService;
    private EventServices eventServices;
    private boolean isRecording = false;

    @FXML
    public void initialize() {
        System.out.println("✅ Initialisation du contrôleur d'émotions");
        recorder = new AudioRecorderJDK();
        emotionService = new AssemblyAIEmotionService();
        eventServices = new EventServices();

        statusLabel.setText("✅ Prêt à écouter");
        if (conseilLabel != null) {
            conseilLabel.setText(""); // Vide au début
        }
    }

    @FXML
    private void onRecordClick() {
        System.out.println("🔥🔥🔥 BOUTON CLIQUÉ !!! 🔥🔥🔥");
        System.out.println("🎯 isRecording = " + isRecording);

        if (!isRecording) {
            startRecording();
        }
    }

    private void startRecording() {
        System.out.println("🎯 DANS startRecording()");
        isRecording = true;
        recordButton.setText("⏹️ Enregistrement...");
        recordButton.setDisable(true);
        statusLabel.setText("🎤 Parle maintenant... (3 secondes)");
        emotionLabel.setText("");
        if (conseilLabel != null) conseilLabel.setText(""); // Efface le conseil
        confidenceBar.setProgress(0);
        eventsContainer.getChildren().clear();

        System.out.println("=================================");
        System.out.println("🔍 DÉBUT ENREGISTREMENT");
        System.out.println("=================================");

        new Thread(() -> {
            try {
                System.out.println("📁 Création du fichier audio...");
                File audioFile = recorder.enregistrer("emotion_temp.wav");

                System.out.println("✅ Fichier créé: " + audioFile.getAbsolutePath());
                System.out.println("📊 Taille: " + audioFile.length() + " bytes");
                System.out.println("📁 Existe? " + audioFile.exists());

                if (audioFile.length() == 0) {
                    System.out.println("❌ Fichier vide !");
                    throw new Exception("Fichier audio vide");
                }

                analyserEmotion(audioFile);

            } catch (Exception e) {
                System.out.println("❌ ERREUR: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Erreur: " + e.getMessage());
                    resetButton();
                });
            }
        }).start();
    }

    private void analyserEmotion(File audioFile) {
        try {
            System.out.println("=================================");
            System.out.println("🔍 ANALYSE DE L'ÉMOTION");
            System.out.println("=================================");
            System.out.println("📁 Fichier: " + audioFile.getName());

            EmotionResult result = emotionService.detecterEmotion(audioFile);

            System.out.println("✅ Émotion détectée: " + result.emotion);
            System.out.println("📊 Score: " + result.score);
            System.out.println("💬 Conseil: " + result.getConseil());

            javafx.application.Platform.runLater(() -> {
                statusLabel.setText("✅ Analyse terminée !");
                emotionLabel.setText(getEmotionFrench(result.emotion));
                confidenceBar.setProgress(result.score);

                // ✅ CHANGER LES COULEURS SELON L'ÉMOTION
                updateColorsByEmotion(result.emotion);

                // ✅ AFFICHER LE CONSEIL
                if (result.getConseil() != null && !result.getConseil().isEmpty()) {
                    conseilLabel.setText("💡 " + result.getConseil());

                    // ✅ LIRE LE CONSEIL À VOIX HAUTE
                    parler(result.getConseil());

                } else {
                    String defaultConseil = "Prends soin de toi aujourd'hui !";
                    conseilLabel.setText("💡 " + defaultConseil);
                    parler(defaultConseil);
                }

                suggererEvenements(result.emotion);
            });

        } catch (Exception e) {
            System.out.println("❌ ERREUR ANALYSE: " + e.getMessage());
            e.printStackTrace();

            javafx.application.Platform.runLater(() -> {
                statusLabel.setText("❌ Erreur analyse: " + e.getMessage());

                if (conseilLabel != null) {
                    String erreurConseil = "Désolé, je n'ai pas pu analyser ton émotion. Réessaie s'il te plaît.";
                    conseilLabel.setText("💡 " + erreurConseil);
                    parler(erreurConseil);
                }

                confidenceBar.setProgress(0);

                // ✅ REMETTRE LES COULEURS PAR DÉFAUT EN CAS D'ERREUR
                updateColorsByEmotion("neutral");
            });

        } finally {
            javafx.application.Platform.runLater(this::resetButton);

            // Supprimer le fichier temporaire
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
                System.out.println("🧹 Fichier temporaire supprimé");
            }
        }
    }
    private String getEmotionFrench(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy": return "😊 Joie";
            case "sad": return "😢 Tristesse";
            case "angry": return "😠 Colère";
            case "fear": return "😰 Anxiété";
            case "neutral": return "😐 Neutre";
            case "positive": return "😊 Joie";
            case "negative": return "😢 Tristesse";
            default: return "🤔 " + emotion;
        }
    }

    private String getEventCategory(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy":
            case "positive":
                return "social";
            case "sad":
            case "negative":
                return "support";
            case "angry":
                return "sport";
            case "fear":
                return "sensoriel";
            case "neutral":
                return "workshops";
            default:
                return "all";
        }
    }

    private void suggererEvenements(String emotion) {
        eventsContainer.getChildren().clear();

        String category = getEventCategory(emotion);
        System.out.println("📂 Catégorie recherchée: " + category);

        List<Event> tousEvents = eventServices.afficherEvent();
        System.out.println("📊 Nombre total d'événements: " + tousEvents.size());

        for (Event e : tousEvents) {
            if (e.getTypeEvent() != null &&
                    e.getTypeEvent().toLowerCase().contains(category)) {
                eventsContainer.getChildren().add(createEventCard(e));
                System.out.println("➕ Événement ajouté: " + e.getTitre());
            }
        }

        if (eventsContainer.getChildren().isEmpty()) {
            Label noEvents = new Label("Aucun événement trouvé pour ton humeur");
            noEvents.setStyle("-fx-text-fill: #666; -fx-padding: 10; -fx-font-style: italic;");
            eventsContainer.getChildren().add(noEvents);
            System.out.println("⚠️ Aucun événement trouvé pour la catégorie: " + category);
        }
    }

    private VBox createEventCard(Event e) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #e0d7ff;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );

        Label title = new Label(e.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3748;");

        Label date = new Label("📅 " + formatDate(e.getDateDebut()));
        date.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Label lieu = new Label("📍 " + e.getLieu());
        lieu.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        card.getChildren().addAll(title, date, lieu);

        card.setOnMouseClicked(ev -> openEventDetails(e));

        // Hover effect
        card.setOnMouseEntered(ev ->
                card.setStyle(card.getStyle() + "-fx-background-color: #f5f0ff; -fx-border-color: #8b5cf6;")
        );
        card.setOnMouseExited(ev ->
                card.setStyle(card.getStyle() + "-fx-background-color: white; -fx-border-color: #e0d7ff;")
        );

        return card;
    }

    private void openEventDetails(Event event) {
        try {
            System.out.println("🔍 Ouverture des détails pour: " + event.getTitre());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetailsPage.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);
            Stage stage = (Stage) recordButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(event.getTitre());
            stage.centerOnScreen();
        } catch (Exception e) {
            System.out.println("❌ Erreur ouverture détails: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String formatDate(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    private void resetButton() {
        isRecording = false;
        recordButton.setText("🎤 Parler maintenant");
        recordButton.setDisable(false);
        System.out.println("🔄 Bouton réinitialisé");
    }


    private void parler(String texte) {
        new Thread(() -> {
            try {
                System.out.println("🔊 Lecture joyeuse: " + texte);

                // Commande PowerShell avec voix féminine
                String command = String.format(
                        "powershell -Command \"" +
                                "Add-Type -AssemblyName System.Speech; " +
                                "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                // Essayer différentes voix féminines françaises
                                "try { $synth.SelectVoice('Microsoft Hortense Desktop'); } " +
                                "catch { try { $synth.SelectVoice('Microsoft Zira Desktop'); } " +
                                "catch { $synth.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::Female); } } " +
                                // Ajuster la vitesse et le volume pour un son plus joyeux
                                "$synth.Rate = 1; " +        // +1 = un peu plus rapide (joyeux)
                                "$synth.Volume = 100; " +     // Volume max
                                "$synth.Speak('%s');\"",
                        texte.replace("'", "''")
                );

                Runtime.getRuntime().exec(command);
                System.out.println("✅ Commande envoyée avec voix féminine");

            } catch (Exception e) {
                System.out.println("❌ Erreur: " + e.getMessage());
                // Fallback: voix par défaut
                parlerSimple(texte);
            }
        }).start();
    }

    // Méthode de secours
    private void parlerSimple(String texte) {
        try {
            String command = String.format(
                    "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$synth.Speak('%s');\"",
                    texte.replace("'", "''")
            );
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void testPowerShell() {
        try {
            String test = "powershell -Command \"echo 'Test PowerShell OK'\"";
            Process p = Runtime.getRuntime().exec(test);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("📢 PowerShell: " + line);
            }
            System.out.println("✅ PowerShell fonctionne");
        } catch (Exception e) {
            System.out.println("❌ PowerShell ne répond pas: " + e.getMessage());
        }
    }
    private void updateColorsByEmotion(String emotion) {
        String borderColor = "#8b5cf6"; // violet par défaut
        String accentColor = "#8b5cf6"; // violet par défaut
        String textColor = "#8b5cf6";   // violet par défaut

        switch(emotion.toLowerCase()) {
            case "happy":
            case "positive":
                borderColor = "#10b981"; // vert
                accentColor = "#10b981"; // vert
                textColor = "#10b981";   // vert
                break;
            case "sad":
            case "negative":
                borderColor = "#3b82f6"; // bleu
                accentColor = "#3b82f6"; // bleu
                textColor = "#3b82f6";   // bleu
                break;
            case "angry":
                borderColor = "#ef4444"; // rouge
                accentColor = "#ef4444"; // rouge
                textColor = "#ef4444";   // rouge
                break;
            case "fear":
                borderColor = "#8b5cf6"; // violet
                accentColor = "#8b5cf6"; // violet
                textColor = "#8b5cf6";   // violet
                break;
            case "neutral":
                borderColor = "#6b7280"; // gris
                accentColor = "#6b7280"; // gris
                textColor = "#6b7280";   // gris
                break;
        }

        // Mettre à jour le style de la carte
        mainCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 30;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                        "-fx-max-width: 700;"
        );

        // Mettre à jour la couleur du texte d'émotion
        emotionLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        // Mettre à jour la couleur de la barre de progression
        confidenceBar.setStyle("-fx-accent: " + accentColor + ";");
    }


    @FXML
    private void goBackToShowEvent() {
        try {
            System.out.println("🔙 Retour à ShowEvent");
            System.out.println("📁 Chemin recherché: /ShowEvent.fxml");

            URL url = getClass().getResource("/ShowEvent.fxml");
            System.out.println("📌 URL trouvée: " + url);

            if (url == null) {
                showAlert("Erreur", "Fichier ShowEvent.fxml introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner: " + e.getMessage());
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