package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.services.LiveEmotionService;
import com.auticare.services.GeminiService;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Mat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FaceEmotionController implements Initializable {

    // ========== ÉLÉMENTS FXML ==========
    @FXML private Button backButton;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button captureButton;
    @FXML private Button precedentBtn;
    @FXML private Button suivantBtn;

    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private Label emotionLabel;
    @FXML private Label titreLabel;

    // Éléments histoire
    @FXML private VBox histoireContainer;
    @FXML private Label histoireTitreLabel;
    @FXML private Label histoirePhraseLabel;
    @FXML private Label etapeLabel;

    // Éléments explication émotion
    @FXML private VBox explicationContainer;
    @FXML private Label emotionIconeLabel;
    @FXML private Label emotionNomLabel;
    @FXML private Label emotionDescriptionLabel;

    // Score
    @FXML private HBox etoilesContainer;
    @FXML private Label scoreLabel;

    // ========== SERVICES ==========
    private LiveEmotionService emotionService;
    private AnimationTimer timer;
    private boolean isRunning = false;
    private GeminiService geminiService = new GeminiService();

    // ========== VARIABLES POUR LE MODE HISTOIRE ==========
    private boolean modeHistoire = false;
    private Event currentEvent;
    private List<HistoireEtape> etapesHistoire = new ArrayList<>();
    private int etapeActuelle = 0;
    private int score = 0;
    private int totalEtoiles = 0;

    // ========== CLASSE INTERNE POUR LES ÉTAPES ==========
    private class HistoireEtape {
        String texte;
        String emotionAttendue;
        String explication; // Ce que Gemini va générer pour expliquer l'émotion

        HistoireEtape(String texte, String emotionAttendue) {
            this.texte = texte;
            this.emotionAttendue = emotionAttendue;
            this.explication = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialiser les services
            emotionService = new LiveEmotionService();

            // Configurer les boutons
            startButton.setOnAction(e -> startEmotionDetection());
            stopButton.setOnAction(e -> stopEmotionDetection());
            backButton.setOnAction(e -> handleBack());
            captureButton.setOnAction(e -> captureAndAnalyze());
            precedentBtn.setOnAction(e -> etapePrecedente());
            suivantBtn.setOnAction(e -> etapeSuivante());

            // Désactiver stop au départ
            stopButton.setDisable(true);

            // Désactiver les boutons navigation au départ
            precedentBtn.setDisable(true);
            suivantBtn.setDisable(true);
            captureButton.setDisable(true);

            // Status initial
            statusLabel.setText("✅ Prêt à démarrer");
            emotionLabel.setText("👤 En attente...");

            // Cacher les conteneurs d'histoire
            histoireContainer.setVisible(false);
            histoireContainer.setManaged(false);
            explicationContainer.setVisible(false);
            explicationContainer.setManaged(false);
            etoilesContainer.setVisible(false);
            etoilesContainer.setManaged(false);

            setupAnimationTimer();

            System.out.println("✅ FaceEmotionController initialisé");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== ACTIVATION MODE HISTOIRE ==========
    public void setModeHistoire(Event event) {
        System.out.println("🔵 setModeHistoire appelé !");
        System.out.println("🔵 Event reçu: " + (event != null ? event.getTitre() : "null"));

        this.modeHistoire = true;
        this.currentEvent = event;

        // Afficher les conteneurs
        histoireContainer.setVisible(true);
        histoireContainer.setManaged(true);
        etoilesContainer.setVisible(true);
        etoilesContainer.setManaged(true);

        // Changer le titre
        if (titreLabel != null) {
            titreLabel.setText("📖 " + event.getTitre());
        }

        // Générer l'histoire
        genererHistoire();
    }

    // ========== GÉNÉRER L'HISTOIRE ==========
    private void genererHistoire() {
        try {
            String nomEvent = currentEvent.getTitre();
            String prompt = "Crée une histoire pour enfants basée sur: '" + nomEvent + "'.\n" +
                    "L'histoire doit avoir EXACTEMENT 3 phrases.\n" +
                    "Chaque phrase doit commencer par une émoticône: 😊 😢 😠 😲 (joie, tristesse, colère, surprise).\n" +
                    "Les 3 émotions doivent être différentes.\n" +
                    "Format: une phrase par ligne.\n\n" +
                    "Exemple:\n" +
                    "😊 Le petit lion était content d'aller au zoo.\n" +
                    "😢 Il a perdu son doudou préféré.\n" +
                    "😲 Un perroquet coloré lui a parlé !";

            String histoire = geminiService.genererDescription(prompt);
            analyserEmotions(histoire);

        } catch (Exception e) {
            // Histoire par défaut
            String histoireDefaut = "😊 Le petit ours est heureux de jouer.\n" +
                    "😢 Il se sent seul sans ses amis.\n" +
                    "😲 Il découvre un arc-en-ciel magique !";
            analyserEmotions(histoireDefaut);
        }
    }

    // ========== ANALYSER LES ÉMOTIONS ==========
    private void analyserEmotions(String histoire) {
        String[] lignes = histoire.split("\n");
        etapesHistoire.clear();
        etapeActuelle = 0;
        score = 0;

        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (ligne.isEmpty()) continue;

            String emotionAttendue = "";

            if (ligne.startsWith("😊")) {
                emotionAttendue = "Joie";
            } else if (ligne.startsWith("😢")) {
                emotionAttendue = "Tristesse";
            } else if (ligne.startsWith("😲")) {
                emotionAttendue = "Surprise";
            } else if (ligne.startsWith("😠")) {
                emotionAttendue = "Colère";
            } else {
                continue;
            }

            etapesHistoire.add(new HistoireEtape(ligne, emotionAttendue));
        }

        totalEtoiles = etapesHistoire.size();
        mettreAJourScore();

        // Afficher la première étape
        if (totalEtoiles > 0) {
            afficherEtape(etapeActuelle);
            // Activer les boutons
            captureButton.setDisable(false);
            precedentBtn.setDisable(true); // Première étape, pas de précédent
            suivantBtn.setDisable(false);
        }
    }

    // ========== AFFICHER UNE ÉTAPE ==========
    private void afficherEtape(int index) {
        if (index >= 0 && index < etapesHistoire.size()) {
            HistoireEtape etape = etapesHistoire.get(index);

            // Afficher la phrase de l'histoire
            histoirePhraseLabel.setText(etape.texte);
            etapeLabel.setText("Étape " + (index + 1) + "/" + totalEtoiles);

            // Préparer l'explication (sera générée quand l'enfant capture)
            emotionIconeLabel.setText(etape.texte.substring(0, 2)); // L'émoticône
            emotionNomLabel.setText(etape.emotionAttendue);

            // Message d'attente
            emotionDescriptionLabel.setText("Capture ton visage pour découvrir cette émotion !");

            // Cacher l'explication pour l'instant
            explicationContainer.setVisible(false);
            explicationContainer.setManaged(false);

            // Gérer les boutons navigation
            precedentBtn.setDisable(index == 0);
            suivantBtn.setDisable(index == etapesHistoire.size() - 1);
        }
    }

    // ========== CAPTURER ET ANALYSER ==========
    private void captureAndAnalyze() {
        if (!isRunning) {
            statusLabel.setText("❌ Démarre d'abord la caméra !");
            return;
        }

        if (etapeActuelle >= etapesHistoire.size()) {
            return;
        }

        statusLabel.setText("📸 Analyse...");
        String emotionDetectee = emotionService.getCurrentEmotion();
        emotionLabel.setText(emotionDetectee);

        HistoireEtape etapeCourante = etapesHistoire.get(etapeActuelle);

        // Vérifier si l'émotion correspond
        if (emotionDetectee.contains(etapeCourante.emotionAttendue)) {
            // Bonne émotion !
            score++;
            mettreAJourScore();

            // Générer l'explication de l'émotion avec Gemini
            String explication = geminiService.genererExplicationEmotion(etapeCourante.emotionAttendue);
            emotionDescriptionLabel.setText(explication);

            // Afficher l'explication
            explicationContainer.setVisible(true);
            explicationContainer.setManaged(true);

            statusLabel.setText("✅ Bravo ! C'est bien de la " + etapeCourante.emotionAttendue + " !");

        } else {
            statusLabel.setText("🔄 Essaie encore ! Fais une expression de " + etapeCourante.emotionAttendue);
        }
    }

    // ========== NAVIGATION ==========
    private void etapePrecedente() {
        if (etapeActuelle > 0) {
            etapeActuelle--;
            afficherEtape(etapeActuelle);
            explicationContainer.setVisible(false);
            explicationContainer.setManaged(false);
        }
    }

    private void etapeSuivante() {
        if (etapeActuelle < etapesHistoire.size() - 1) {
            etapeActuelle++;
            afficherEtape(etapeActuelle);
            explicationContainer.setVisible(false);
            explicationContainer.setManaged(false);
        }
    }

    // ========== METTRE À JOUR LE SCORE ==========
    private void mettreAJourScore() {
        scoreLabel.setText(score + "/" + totalEtoiles);
    }

    // ========== MÉTHODES CAMÉRA ==========
    private void setupAnimationTimer() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 33_000_000) return;
                lastUpdate = now;

                if (isRunning && emotionService != null && emotionService.isCameraOpen()) {
                    try {
                        Mat frame = emotionService.getCurrentFrame();
                        if (frame != null && !frame.empty()) {
                            Image fxImage = emotionService.matToFxImage(frame);
                            if (fxImage != null) {
                                cameraView.setImage(fxImage);
                            }
                            frame.release();
                        }

                        String emotion = emotionService.getCurrentEmotion();
                        emotionLabel.setText(emotion);

                    } catch (Exception e) {
                        System.err.println("Erreur: " + e.getMessage());
                    }
                }
            }
        };
    }

    private void startEmotionDetection() {
        if (!isRunning) {
            try {
                statusLabel.setText("🎥 Démarrage...");
                emotionService.start();
                isRunning = true;
                startButton.setDisable(true);
                stopButton.setDisable(false);
                timer.start();
            } catch (Exception e) {
                statusLabel.setText("❌ Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void stopEmotionDetection() {
        if (isRunning) {
            emotionService.stop();
            isRunning = false;
            startButton.setDisable(false);
            stopButton.setDisable(true);
            timer.stop();

            cameraView.setImage(null);
            emotionLabel.setText("👤 En attente...");
            statusLabel.setText("✅ Prêt");
        }
    }

    private void handleBack() {
        stopEmotionDetection();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/ShowEvent.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        stopEmotionDetection();
        if (emotionService != null) {
            emotionService.stop();
        }
    }
}