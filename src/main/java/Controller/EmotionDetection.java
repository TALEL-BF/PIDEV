package com.auticare.controllers;
import com.auticare.entities.Event;
import com.auticare.services.*;
import com.auticare.services.AssemblyAIEmotionService.EmotionResult;
import com.auticare.services.AudioRecorderJDK;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmotionDetection {

    @FXML private Button recordButton;
    @FXML private Label statusLabel;
    @FXML private Label emotionLabel;
    @FXML private Label conseilLabel;
    @FXML private ProgressBar confidenceBar;
    @FXML private VBox eventsContainer;
    @FXML private Button backButton;
    @FXML private VBox mainCard;

    private AudioRecorderJDK recorder;
    private AssemblyAIEmotionService emotionService;
    private EventServices eventServices;
    private boolean isRecording = false;
    private FallbackService fallbackService;  // ✅ Utiliser FallbackService au lieu de GeminiService

    @FXML
    public void initialize() {
        System.out.println("✅ Initialisation du contrôleur d'émotions");
        recorder = new AudioRecorderJDK();
        emotionService = new AssemblyAIEmotionService();
        eventServices = new EventServices();

        // ✅ Initialiser FallbackService une seule fois
        String groqKey = "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";
        fallbackService = new FallbackService(groqKey);

        // ✅ Injecter FallbackService dans emotionService
        emotionService.setFallbackService(fallbackService);

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

        // Réinitialiser l'affichage
        emotionLabel.setText("");
        if (conseilLabel != null) conseilLabel.setText("");
        confidenceBar.setProgress(0);
        eventsContainer.getChildren().clear();

        // Créer un timer pour le compte à rebours
        final int[] seconds = {5}; // 5 secondes d'enregistrement
        statusLabel.setText("🎤 Prépare-toi... 5 secondes");

        Timeline countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds[0]--;
            if (seconds[0] > 0) {
                statusLabel.setText("🎤 Parle maintenant... (" + seconds[0] + "s)");
            } else {
                statusLabel.setText("⏹️ Fin de l'enregistrement...");
            }
        }));
        countdown.setCycleCount(5);
        countdown.play();

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

                if (audioFile.length() < 10000) { // Moins de 10KB = fichier trop petit
                    System.out.println("❌ Fichier trop petit: " + audioFile.length() + " bytes");
                    throw new Exception("L'enregistrement est trop court. Assure-toi de parler près du micro !");
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

            // Traduire l'émotion en français pour le prompt
            String emotionFrancais = traduireEmotion(result.emotion);

            // ✅ UTILISER FALLBACK POUR LE CONSEIL (Gemini → Groq → défaut)
            String conseilGemini = fallbackService.genererConseilAvecFallback(emotionFrancais);
            System.out.println("💬 Conseil avec fallback: " + conseilGemini);

            javafx.application.Platform.runLater(() -> {
                statusLabel.setText("✅ Analyse terminée !");
                emotionLabel.setText(getEmotionFrench(result.emotion));
                confidenceBar.setProgress(result.score);

                // ✅ CHANGER LES COULEURS SELON L'ÉMOTION
                updateColorsByEmotion(result.emotion);

                // ✅ AFFICHER LE CONSEIL AVEC FALLBACK
                if (conseilGemini != null && !conseilGemini.isEmpty() && !conseilGemini.contains("Erreur")) {
                    conseilLabel.setText("💡 " + conseilGemini);
                    parler(conseilGemini);
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

                String erreurConseil = "Désolé, je n'ai pas pu analyser ton émotion. Réessaie s'il te plaît.";
                conseilLabel.setText("💡 " + erreurConseil);
                parler(erreurConseil);

                confidenceBar.setProgress(0);
                updateColorsByEmotion("neutral");
            });

        } finally {
            javafx.application.Platform.runLater(this::resetButton);
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
                System.out.println("🧹 Fichier temporaire supprimé");
            }
        }
    }

    private VBox createEventCard(Event e) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #e0d7ff;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 8, 0, 0, 3);"
        );
        card.setPrefWidth(750);

        // Titre avec icône selon le type
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        String emoji = getEmojiForEvent(e);
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px; -fx-min-width: 40;");

        Label title = new Label(e.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2d3748;");
        title.setWrapText(true);

        titleBox.getChildren().addAll(emojiLabel, title);

        // Date et lieu
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label date = new Label("📅 " + formatDate(e.getDateDebut()));
        date.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        Label lieu = new Label("📍 " + e.getLieu());
        lieu.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        infoBox.getChildren().addAll(date, lieu);

        // Participants
        Label participants = new Label("👥 " + e.getMaxParticipant() + " participants");
        participants.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 14px; -fx-font-weight: 600;");

        card.getChildren().addAll(titleBox, infoBox, participants);

        // Hover effect
        card.setOnMouseEntered(ev ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 15;" +
                                "-fx-border-color: #8b5cf6;" +
                                "-fx-border-width: 3;" +
                                "-fx-border-radius: 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.2), 15, 0, 0, 5);"
                )
        );

        card.setOnMouseExited(ev ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 15;" +
                                "-fx-border-color: #e0d7ff;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 8, 0, 0, 3);"
                )
        );

        card.setOnMouseClicked(ev -> openEventDetails(e));

        return card;
    }

    private String getEmojiForEvent(Event e) {
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";
        if (type.contains("social")) return "🎉";
        if (type.contains("support")) return "💙";
        if (type.contains("sport")) return "⚽";
        if (type.contains("sensoriel")) return "🧘";
        if (type.contains("workshop")) return "🎨";
        return "📅";
    }

    // Ajoute cette méthode pour traduire l'émotion
    private String traduireEmotion(String emotion) {
        switch(emotion.toLowerCase()) {
            case "happy":
            case "positive":
                return "heureux(se)";
            case "sad":
            case "negative":
                return "triste";
            case "angry":
                return "en colère";
            case "fear":
            case "anxious":
                return "anxieux(se)";
            case "neutral":
                return "calme";
            default:
                return emotion;
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


    private void suggererEvenements(String emotion) {
        eventsContainer.getChildren().clear();

        // Afficher un message de chargement
        Label loadingLabel = new Label("🤖 IA en cours d'analyse...");
        loadingLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-style: italic; -fx-padding: 10;");
        eventsContainer.getChildren().add(loadingLabel);

        // Récupérer tous les événements
        List<Event> tousEvents = eventServices.afficherEvent();

        // Traduire l'émotion pour Gemini
        String emotionPourGemini = getEmotionForGemini(emotion);

        // Lancer l'appel Gemini dans un thread séparé
        new Thread(() -> {
            try {
                // Construire le prompt pour Gemini
                String prompt = construirePromptEvenements(emotionPourGemini, tousEvents);

                // ✅ Utiliser FallbackService pour la description aussi
                String reponseGemini = fallbackService.genererDescription(prompt);
                System.out.println("📥 Réponse avec fallback: " + reponseGemini);

                // Parser la réponse
                List<Event> evenementsTries = parserRecommandations(reponseGemini, tousEvents);

                // Mettre à jour l'interface
                javafx.application.Platform.runLater(() -> {
                    eventsContainer.getChildren().clear();

                    if (evenementsTries.isEmpty()) {
                        Label noEvents = new Label("Aucun événement trouvé pour ton humeur");
                        noEvents.setStyle("-fx-text-fill: #666; -fx-padding: 10; -fx-font-style: italic;");
                        eventsContainer.getChildren().add(noEvents);
                    } else {
                        // Afficher les événements triés
                        for (Event e : evenementsTries) {
                            eventsContainer.getChildren().add(createEventCard(e));
                        }

                        // Ajouter un petit message
                        Label infoLabel = new Label("✨ Recommandations personnalisées par IA");
                        infoLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 5 0 0 0;");
                        eventsContainer.getChildren().add(infoLabel);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    eventsContainer.getChildren().clear();
                    Label errorLabel = new Label("❌ Erreur de recommandation: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-padding: 10;");
                    eventsContainer.getChildren().add(errorLabel);

                    // Fallback: afficher quelques événements au hasard
                    fallbackSuggestions(tousEvents);
                });
            }
        }).start();
    }

    private String getEmotionForGemini(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy": return "joyeux(se) et plein(e) d'énergie";
            case "sad": return "triste et besoin de réconfort";
            case "angry": return "en colère et besoin de se défouler";
            case "fear": return "anxieux(se) et besoin de calme";
            case "neutral": return "calme et ouvert(e) à découvrir";
            default: return emotion;
        }
    }

    private String construirePromptEvenements(String emotion, List<Event> events) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("L'utilisateur se sent ").append(emotion).append(".\n\n");
        prompt.append("Voici la liste des événements disponibles:\n");

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            prompt.append(i).append(". ");
            prompt.append("Titre: \"").append(e.getTitre()).append("\"\n");
            prompt.append("   Description: \"").append(e.getDescription()).append("\"\n");
            prompt.append("   Type: \"").append(e.getTypeEvent()).append("\"\n");
            prompt.append("   Lieu: \"").append(e.getLieu()).append("\"\n\n");
        }

        prompt.append("IMPORTANT: Analyse le TITRE et la DESCRIPTION de chaque événement.\n");
        prompt.append("Retourne UNIQUEMENT un objet JSON avec cette structure:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\"index\": 0, \"score\": 95, \"raison\": \"...\"},\n");
        prompt.append("    {\"index\": 2, \"score\": 87, \"raison\": \"...\"}\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("Les indices correspondent à la position dans la liste.\n");
        prompt.append("Retourne les 5 meilleurs événements, triés par score.");

        return prompt.toString();
    }

    private List<Event> parserRecommandations(String jsonResponse, List<Event> allEvents) {
        List<Event> sortedEvents = new ArrayList<>();

        try {
            // Nettoyer la réponse
            jsonResponse = nettoyerReponseJSON(jsonResponse);

            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray recommendations = json.getAsJsonArray("recommendations");

            // Map pour stocker les scores
            Map<Integer, Integer> scores = new HashMap<>();

            for (int i = 0; i < recommendations.size(); i++) {
                JsonObject rec = recommendations.get(i).getAsJsonObject();
                int index = rec.get("index").getAsInt();
                int score = rec.get("score").getAsInt();

                if (index >= 0 && index < allEvents.size()) {
                    scores.put(index, score);
                }
            }

            // Trier les indices par score
            List<Integer> sortedIndices = new ArrayList<>(scores.keySet());
            sortedIndices.sort((a, b) -> scores.get(b) - scores.get(a));

            // Créer la liste triée
            for (int index : sortedIndices) {
                sortedEvents.add(allEvents.get(index));
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing: " + e.getMessage());
            // Fallback: retourner quelques événements au hasard
            return fallbackSuggestions(allEvents);
        }

        return sortedEvents;
    }

    private String nettoyerReponseJSON(String texte) {
        // Enlever tout ce qui est avant le premier {
        int debut = texte.indexOf("{");
        if (debut != -1) {
            texte = texte.substring(debut);
        }

        // Enlever tout ce qui est après le dernier }
        int fin = texte.lastIndexOf("}");
        if (fin != -1) {
            texte = texte.substring(0, fin + 1);
        }

        return texte;
    }

    private List<Event> fallbackSuggestions(List<Event> allEvents) {
        List<Event> fallback = new ArrayList<>();
        // Prendre les 3 premiers événements
        for (int i = 0; i < Math.min(3, allEvents.size()); i++) {
            fallback.add(allEvents.get(i));
        }
        return fallback;
    }



    private void openEventDetails(Event event) {
        try {
            System.out.println("🔍 Ouverture des détails pour: " + event.getTitre());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventDetailsPage.fxml"));
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
                        "-fx-padding: 35;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.4), 20, 0, 0, 8);" +
                        "-fx-min-height: 650;" +
                        "-fx-max-width: 1100;"
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
            System.out.println("📁 Chemin recherché: /views/ShowEvent.fxml");

            URL url = getClass().getResource("/views/ShowEvent.fxml");
            System.out.println("📌 URL trouvée: " + url);

            if (url == null) {
                showAlert("Erreur", "Fichier ShowEvent.fxml introuvable dans /views/ !");
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