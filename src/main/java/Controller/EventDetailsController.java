package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.entities.Sponsor;
import com.auticare.services.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventDetailsController {

    @FXML
    private ImageView heroImageView;
    @FXML
    private ImageView eventImageView;
    @FXML
    private Button viewParticipantsBtn;

    @FXML
    private Label weatherLabel;

    @FXML
    private Label locationMapLabel;  // Nouveau label pour la carte
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Button histoireBtn;
    @FXML
    private Label timeLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private VBox weatherCard;
    @FXML
    private Label weatherEmojiLabel;
    @FXML
    private Label weatherTempValue;
    @FXML
    private Button collectionBtn;
    @FXML
    private Label weatherHumidityValue;
    @FXML
    private Label weatherWindValue;
    @FXML
    private Label weatherCityLabel;
    private FallbackService fallbackService;
    @FXML
    private Label weatherTempLabel;

    @FXML
    private Label weatherHumidityLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private VBox titleContainer;
    @FXML
    private FlowPane sponsorsFlowPane;
    @FXML
    private Button registerBtn;
    @FXML
    private Button backButton;
    @FXML
    private Button rapportIABtn;
    @FXML
    private MediaView videoMediaView;
    @FXML
    private Button generateVideoBtn;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label statusLabel;


    private MediaPlayer mediaPlayer;

    // Nouveaux composants
    @FXML
    private HBox galleryContainer;
    private Event event;
    @FXML
    private FlowPane tagsContainer;
    @FXML
    private PieChart participantsChart;
    @FXML
    private Label participantsCountLabel;
    @FXML
    private Label inscritsCount;
    // Dans EventDetailsController.java - avec les autres services



    @FXML
    private Label restantsCount;
    @FXML
    private Label locationFullLabel;
    @FXML
    private HBox similarEventsContainer;
    @FXML
    private GridPane feedbacksGrid;
    @FXML
    private Label maxParticipantsLabel;
    @FXML
    private Label noiseEmojiLabel;
    @FXML
    private Label noiseLabel;
    @FXML
    private Label sensoryEmojiLabel;
    @FXML
    private Label sensoryLabel;
    @FXML
    private Label ageEmojiLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private Label activityEmojiLabel;
    @FXML
    private Label activityLabel;
    @FXML
    private Label recommendationIcon;
    @FXML
    private Label recommendationLabel;
    @FXML
    private Button analyzeBtn;
    private GeminiService geminiService = new GeminiService();
    private Map<Integer, JsonObject> analysisCache = new HashMap<>(); // ← AJOUTE CETTE LIGNE
    private WeatherService weatherService = new WeatherService();


    private EventServices eventServices = new EventServices();
    private Event currentEvent;


    private void updateBadgesFromJson(JsonObject json) {
        try {
            System.out.println("🔄 Mise à jour des badges avec: " + json.toString());

            // Niveau sonore
            String noise = getJsonString(json, "noiseLevel", "Calme");
            noiseLabel.setText(noise);
            noiseEmojiLabel.setText(getNoiseEmoji(noise));

            // Stimulation sensorielle
            String sensory = getJsonString(json, "sensoryStimulation", "Faible");
            sensoryLabel.setText(sensory);
            sensoryEmojiLabel.setText(getSensoryEmoji(sensory));

            // Âge recommandé
            String age = getJsonString(json, "recommendedAge", "5-10 ans");
            ageLabel.setText(age);

            // Niveau d'activité
            String activity = getJsonString(json, "activityLevel", "Assis");
            activityLabel.setText(activity);
            activityEmojiLabel.setText(getActivityEmoji(activity));

            // Recommandation
            String recommendation = getJsonString(json, "recommendation", "Bon");
            recommendationLabel.setText(recommendation);
            recommendationIcon.setText(getRecommendationEmoji(recommendation));

            System.out.println("✅ Badges mis à jour");

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage analyse: " + e.getMessage());
        }
    }

    private String getNoiseEmoji(String noise) {
        switch (noise.toLowerCase()) {
            case "calme":
                return "🔇";
            case "moyen":
                return "🔉";
            case "bruyant":
                return "🔊";
            default:
                return "🔊";
        }
    }


    private String getActivityEmoji(String activity) {
        switch (activity.toLowerCase()) {
            case "assis":
                return "🪑";
            case "modéré":
                return "🚶";
            case "actif":
                return "🏃";
            default:
                return "🚶";
        }
    }

    @FXML
    private void openFaceEmotionV2() {
        try {
            System.out.println("🔍 Ouverture de FaceEmotionControllerV2...");

            // Charger le FXML de la V2
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FaceEmotionV2.fxml"));
            Parent root = loader.load();

            // Passer l'événement courant au contrôleur V2
            FaceEmotionControllerV2 controller = loader.getController();
            controller.setModeHistoire(currentEvent);  // Utilise currentEvent qui existe déjà

            // Changer de scène
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détection d'émotions V2 - " + currentEvent.getTitre());
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la détection V2: " + e.getMessage());
        }
    }


    @FXML
    public void initialize() {



        String groqKey = "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";  // TA CLÉ
        fallbackService = new FallbackService(groqKey);
        if (histoireBtn != null) {
            histoireBtn.setOnAction(e -> openHistoireMode());
        }

        if (analyzeBtn != null) {
            analyzeBtn.setOnAction(e -> analyzeEvent());  // ← Utilise la méthode corrigée
            System.out.println("✅ Bouton Analyse IA configuré avec Fallback");
        } else {
            System.out.println("❌ analyzeBtn est NULL ! Vérifie le fx:id dans le FXML");
        }



        // Modifier le bouton Participer


        // Bouton Rapport IA
        if (rapportIABtn != null) {
            System.out.println("✅ Bouton Rapport IA trouvé, ajout de l'action");
            rapportIABtn.setOnAction(e -> {
                System.out.println("🔍 Clic sur bouton Rapport IA");
                openRapportIA();
            });
        } else {
            System.out.println("❌ Bouton Rapport IA est NULL !");
        }

        // Bouton retour
        if (backButton != null) {
            backButton.setOnAction(e -> goBackToShowEvent());
        }
        if (videoMediaView == null) {
            System.out.println("❌ videoMediaView est NULL ! Vérifie le fx:id dans le FXML");
        } else {
            System.out.println("✅ videoMediaView est bien injecté");
        }


        // Initialiser la galerie avec des images par défaut

        // Initialiser les tags
        initTags();

        // Initialiser les événements similaires
        initSimilarEvents();

        // Initialiser les avis
        initFeedbacks();
    }



    public void setEvent(Event event) {
        this.currentEvent = event;
        this.event = event;
        if (event.getLieu() != null && !event.getLieu().isEmpty()) {
            String city = extractCityFromLieu(event.getLieu());
            updateWeatherInfo(city);
        }

        // Titre avec image
        if (titleContainer != null) {
            HBox titleBox = new HBox(15);
            titleBox.setAlignment(Pos.CENTER_LEFT);

            ImageView typeIcon = new ImageView();
            typeIcon.setFitWidth(60);
            typeIcon.setFitHeight(60);
            typeIcon.setPreserveRatio(true);

            String imageName = getEventImageName(event);
            try {
                URL imageUrl = getClass().getResource("/assets/" + imageName);
                if (imageUrl != null) {
                    typeIcon.setImage(new Image(imageUrl.toString()));
                } else {
                    URL defaultIcon = getClass().getResource("/assets/event-default.png");
                    if (defaultIcon != null) {
                        typeIcon.setImage(new Image(defaultIcon.toString()));
                    }
                }
            } catch (Exception ex) {
                System.out.println("Erreur chargement icône: " + ex.getMessage());
            }

            Label titleLabel = new Label(event.getTitre());
            titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #8b5cf6;");

            titleBox.getChildren().addAll(typeIcon, titleLabel);
            titleContainer.getChildren().clear();
            titleContainer.getChildren().add(titleBox);
        } else {
            eventTitleLabel.setText(event.getTitre());
        }

        dateLabel.setText(formatDate(event.getDateDebut()));
        timeLabel.setText(formatTime(event.getHeureDebut()) + " à " + formatTime(event.getHeureFin()));
        locationLabel.setText(event.getLieu());
        descriptionArea.setText(event.getDescription());

        // Mettre à jour les nouveaux labels
        if (locationFullLabel != null) {
            locationFullLabel.setText(event.getLieu());
        }

        // Calculer les participants (simulé - à remplacer par des vraies données)
        int maxParticipants = event.getMaxParticipant();


        if (maxParticipantsLabel != null) {
            maxParticipantsLabel.setText(maxParticipants + " personnes");
        }

        // Mettre à jour le graphique

        chargerHeroImage();
        chargerEventImage(event);
        chargerSponsors(event.getIdEvent());

    }

    private String getEventImageName(Event e) {
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";

        if (type.contains("workshop")) return "workshop.png";
        if (type.contains("training")) return "training.png";
        if (type.contains("conference")) return "conference.png";
        if (type.contains("social")) return "social.png";
        if (type.contains("therapy")) return "therapy.png";

        return "event-default.png";
    }

    private void chargerHeroImage() {
        try {
            if (heroImageView != null) {
                heroImageView.setImage(new Image(getClass().getResource("/assets/bom4.png").toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerEventImage(Event event) {
        try {
            if (eventImageView != null) {
                Image image;
                if (event.getImage() != null && !event.getImage().isEmpty()) {
                    File imgFile = new File(event.getImage());
                    if (imgFile.exists()) {
                        image = new Image("file:" + event.getImage(), 750, 350, false, true);
                    } else {
                        image = new Image(getClass().getResource("/assets/event-default.jpg").toExternalForm());
                    }
                } else {
                    image = new Image(getClass().getResource("/assets/event-default.jpg").toExternalForm());
                }

                eventImageView.setImage(image);

                Rectangle clip = new Rectangle();
                clip.setWidth(eventImageView.getFitWidth());
                clip.setHeight(eventImageView.getFitHeight());
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                eventImageView.setClip(clip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void chargerSponsors(int eventId) {
        if (sponsorsFlowPane == null) return;

        sponsorsFlowPane.getChildren().clear();

        List<Sponsor> sponsors = eventServices.getSponsorsForEvent(eventId);

        if (sponsors.isEmpty()) {
            Label noSponsors = new Label("Aucun sponsor pour cet événement");
            noSponsors.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 10;");
            sponsorsFlowPane.getChildren().add(noSponsors);
        } else {
            for (Sponsor s : sponsors) {
                sponsorsFlowPane.getChildren().add(createSponsorCard(s));
            }
        }
    }

    private VBox createSponsorCard(Sponsor sponsor) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #f0e8ff;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.05), 5, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );
        card.setPrefWidth(160);
        card.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView();
        logoImage.setFitWidth(100);
        logoImage.setFitHeight(70);
        logoImage.setPreserveRatio(true);
        logoImage.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        try {
            if (sponsor.getImage() != null && !sponsor.getImage().isEmpty()) {
                File imgFile = new File(sponsor.getImage());
                if (imgFile.exists()) {
                    Image image = new Image("file:" + sponsor.getImage(), 100, 70, true, true);
                    logoImage.setImage(image);
                } else {
                    logoImage.setImage(new Image(getClass().getResource("/assets/logo.png").toExternalForm()));
                }
            } else {
                logoImage.setImage(new Image(getClass().getResource("/assets/logo.png").toExternalForm()));
            }
        } catch (Exception e) {
            try {
                logoImage.setImage(new Image(getClass().getResource("/assets/logo.png").toExternalForm()));
            } catch (Exception ex) {
            }
        }

        Label nameLabel = new Label(sponsor.getNom());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(sponsor.getTypeSponsor());
        String typeColor = getSponsorTypeColor(sponsor.getTypeSponsor());
        typeLabel.setStyle(
                "-fx-background-color: " + typeColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 3 12; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(logoImage, nameLabel, typeLabel);

        Tooltip tooltip = new Tooltip(
                sponsor.getNom() + "\n" +
                        "Type: " + sponsor.getTypeSponsor() + "\n" +
                        "Email: " + sponsor.getEmail() + "\n" +
                        "Tél: " + sponsor.getTelephone()
        );
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2d3748; -fx-text-fill: white;");
        Tooltip.install(card, tooltip);

        card.setOnMouseEntered(ev -> {
            card.setStyle(
                    "-fx-background-color: #f5f0ff;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 15;" +
                            "-fx-border-color: #8b5cf6;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 8, 0, 0, 3);" +
                            "-fx-cursor: hand;"
            );
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(ev -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 15;" +
                            "-fx-border-color: #f0e8ff;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.05), 5, 0, 0, 2);" +
                            "-fx-cursor: hand;"
            );
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    private String getSponsorTypeColor(String type) {
        switch (type) {
            case "Or":
                return "#FFD700";
            case "Argent":
                return "#C0C0C0";
            case "Bronze":
                return "#CD7F32";
            case "Platine":
                return "#E5E4E2";
            default:
                return "#8b5cf6";
        }
    }



    @FXML
    private void openRapportIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RapportIA.fxml"));
            Parent root = loader.load();

            RapportIAController controller = loader.getController();

            // ✅ Passer l'événement ET le sujet (titre)
            controller.setEvent(currentEvent);
            controller.setSujet(currentEvent.getTitre());  // ← AJOUTE ÇA !
            controller.setFallbackService(fallbackService);

            Stage stage = (Stage) rapportIABtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rapport IA - " + currentEvent.getTitre());
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le rapport IA");
        }
    }

    @FXML
    private void openHistoireMode() {
        System.out.println("📖 Ouverture du mode histoire pour : " + currentEvent.getTitre());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FaceEmotion.fxml"));
            Parent root = loader.load();

            FaceEmotionController controller = loader.getController();
            controller.setModeHistoire(currentEvent);

            Stage stage = (Stage) histoireBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Histoire - " + currentEvent.getTitre());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le mode histoire");
        }
    }

    @FXML
    private void goBackToShowEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ShowEvent.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste des événements");
        }
    }


    private void initTags() {
        if (tagsContainer == null) return;

        String[] tags = {"Bien-être", "Atelier", "Groupe", "Écoute"};
        for (String tag : tags) {
            Label tagLabel = new Label("#" + tag);
            tagLabel.setStyle(
                    "-fx-background-color: #f0e8ff; " +
                            "-fx-text-fill: #8b5cf6; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 20; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
            );
            tagsContainer.getChildren().add(tagLabel);
        }
    }

    private void initSimilarEvents() {
        if (similarEventsContainer == null) return;

        // Ajouter des événements similaires (simulés)
        for (int i = 1; i <= 4; i++) {
            VBox card = new VBox(5);
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 15; " +
                            "-fx-padding: 10; " +
                            "-fx-border-color: #e0d7ff; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 15; " +
                            "-fx-cursor: hand;"
            );
            card.setPrefWidth(180);

            ImageView img = new ImageView();
            img.setFitWidth(160);
            img.setFitHeight(90);
            img.setPreserveRatio(true);
            try {
                img.setImage(new Image(getClass().getResource("/assets/event-default.jpg").toExternalForm()));
            } catch (Exception e) {
            }

            Label title = new Label("Événement " + i);
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            card.getChildren().addAll(img, title);
            similarEventsContainer.getChildren().add(card);
        }
    }

    private void initFeedbacks() {
        if (feedbacksGrid == null) return;

        // Ajouter des avis simulés
        String[][] avis = {
                {"Marie Dubois", "⭐⭐⭐⭐⭐", "Ambiance calme et accueillante"},
                {"Thomas Martin", "⭐⭐⭐⭐", "Très belle expérience"},
                {"Sophie Bernard", "⭐⭐⭐⭐⭐", "L'équipe est super attentionnée"},
                {"Lucas Petit", "⭐⭐⭐⭐", "Atelier très intéressant"}
        };

        for (int i = 0; i < avis.length; i++) {
            VBox card = new VBox(8);
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 15; " +
                            "-fx-padding: 15; " +
                            "-fx-border-color: #e5dbff; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 15;"
            );

            Label name = new Label(avis[i][0]);
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label stars = new Label(avis[i][1]);
            stars.setStyle("-fx-text-fill: #f59e0b;");

            Label comment = new Label(avis[i][2]);
            comment.setWrapText(true);
            comment.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12px;");

            card.getChildren().addAll(name, stars, comment);
            feedbacksGrid.add(card, i % 2, i / 2);
        }
    }

    private String formatDate(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        String[] months = {"janv.", "févr.", "mars", "avr.", "mai", "juin",
                "juil.", "août", "sept.", "oct.", "nov.", "déc."};
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        int year = Integer.parseInt(parts[0]);
        return day + " " + months[month] + " " + year;
    }

    private String formatTime(java.sql.Time time) {
        if (time == null) return "?";
        return time.toString().substring(0, 5);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void checkExistingVideo() {
        if (currentEvent == null) return;  // ✅ Vérification

        // Vérifier si une vidéo a déjà été générée pour cet événement
        String videoPath = "videos/" + currentEvent.getTitre().replaceAll(" ", "_") + ".mp4";
        java.io.File videoFile = new java.io.File(videoPath);

        if (videoFile.exists()) {
            playVideo(videoFile.toURI().toString());
            statusLabel.setText("Vidéo disponible");
        }
    }


    private void playVideo(String videoUrl) {
        try {
            // Arrêter l'ancienne vidéo si elle joue
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            Media media = new Media(videoUrl);
            mediaPlayer = new MediaPlayer(media);
            videoMediaView.setMediaPlayer(mediaPlayer);

            // Jouer en boucle
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();

        } catch (Exception e) {
            statusLabel.setText("Erreur lecture vidéo: " + e.getMessage());
        }
    }

    private void saveVideoLocally(String videoUrl) {
        try {
            // Créer le dossier videos s'il n'existe pas
            String folderPath = "videos/";
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(folderPath));

            // Nom du fichier
            String fileName = currentEvent.getTitre().replaceAll(" ", "_") + ".mp4";
            String filePath = folderPath + fileName;

            // Télécharger et sauvegarder
            try (java.io.InputStream in = new java.net.URL(videoUrl).openStream()) {
                java.nio.file.Files.copy(in, java.nio.file.Paths.get(filePath),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Vidéo sauvegardée: " + filePath);

        } catch (Exception e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }

    private String extractCityFromLieu(String lieu) {
        // Si le lieu est juste "Paris", retourne "Paris"
        // Si c'est une adresse, prend le dernier mot ou une logique simple
        String[] parts = lieu.split(" ");
        return parts[parts.length - 1]; // Dernier mot = ville probablement
    }

    private void updateWeatherInfo(String city) {
        new Thread(() -> {
            try {
                String weatherInfo = weatherService.getWeatherForCity(city);
                System.out.println("📥 Météo reçue: " + weatherInfo);

                // Format: "Bizerte : 15,0°C, légère pluie, 77% humidité, 35 km/h"
                String[] parts = weatherInfo.split(" : ");
                String cityName = parts[0];
                String details = parts.length > 1 ? parts[1] : "";

                String temperature = "?°C";
                String humidity = "?%";
                String wind = "? km/h";

                if (details.contains(",")) {
                    String[] detailParts = details.split(",");

                    // ✅ RECONSTRUIRE LA TEMPÉRATURE (car elle peut être coupée par la virgule)
                    StringBuilder tempBuilder = new StringBuilder();
                    boolean tempFound = false;

                    for (int i = 0; i < detailParts.length; i++) {
                        String part = detailParts[i].trim();

                        if (part.contains("°C")) {
                            // Si on a déjà commencé à construire la température
                            if (tempFound) {
                                tempBuilder.append(",").append(part);
                                temperature = tempBuilder.toString();
                            } else {
                                temperature = part;
                            }
                        } else if (part.matches("\\d+") && i + 1 < detailParts.length && detailParts[i + 1].contains("°C")) {
                            // Cas où la température est splitée: "15" et "0°C"
                            tempBuilder = new StringBuilder(part + "," + detailParts[i + 1].trim());
                            temperature = tempBuilder.toString();
                            i++; // Sauter la prochaine partie
                            tempFound = true;
                        } else if (part.contains("%")) {
                            humidity = part;
                        } else if (part.contains("km/h")) {
                            wind = part;
                        }
                    }
                }

                System.out.println("📊 Température finale: '" + temperature + "'");

                final String temp = temperature;
                final String hum = humidity;
                final String wnd = wind;
                final String cityFinal = cityName;

                javafx.application.Platform.runLater(() -> {
                    weatherTempValue.setText(temp);
                    weatherHumidityValue.setText(hum);
                    weatherWindValue.setText(wnd);
                    weatherCityLabel.setText(cityFinal);

                    System.out.println("✅ Interface mise à jour:");
                    System.out.println("   Temp: " + temp);
                    System.out.println("   Hum: " + hum);
                    System.out.println("   Vent: " + wnd);
                    System.out.println("   Ville: " + cityFinal);
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    weatherTempValue.setText("?°C");
                    weatherHumidityValue.setText("?%");
                    weatherWindValue.setText("? km/h");
                    weatherCityLabel.setText(city);
                });
            }
        }).start();
    }

    private String getWeatherEmoji(String condition) {
        String cond = condition.toLowerCase();

        if (cond.contains("soleil") || cond.contains("ensoleillé") || cond.contains("clair")) {
            return "☀️";
        } else if (cond.contains("nuage") || cond.contains("couvert")) {
            return "☁️";
        } else if (cond.contains("pluie") || cond.contains("pleut")) {
            return "🌧️";
        } else if (cond.contains("orage") || cond.contains("tonnerre")) {
            return "⛈️";
        } else if (cond.contains("neige")) {
            return "❄️";
        } else if (cond.contains("brouillard")) {
            return "🌫️";
        } else if (cond.contains("vent")) {
            return "💨";
        } else {
            return "🌡️";
        }
    }


    // ✅ AJOUTE AUSSI CETTE MÉTHODE SI ELLE N'EXISTE PAS
    private String getJsonString(JsonObject json, String key, String defaultValue) {
        try {
            return json.has(key) ? json.get(key).getAsString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @FXML
    private void openPlanningPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PlanningView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Planning de la semaine");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le planning");
        }
    }

    // ✅ GARDE TES MÉTHODES D'EMOJI EXISTANTES
    @FXML
    private void openUserPlanning() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventPlanningUser.fxml"));
            Parent root = loader.load();

            EventPlanningUserController controller = loader.getController();
            controller.setEvent(currentEvent);

            // ✅ Version POPUP (nouvelle fenêtre)
            Stage popupStage = new Stage();
            popupStage.setTitle("Planning - " + currentEvent.getTitre());
            popupStage.setScene(new Scene(root));
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Bloque la fenêtre parent
            popupStage.showAndWait(); // Attend que la popup soit fermée

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le planning");
        }
    }

    private String getSensoryEmoji(String sensory) {
        switch (sensory.toLowerCase()) {
            case "faible":
                return "🧘";
            case "moyenne":
                return "🎨";
            case "élevée":
                return "⚡";
            default:
                return "🧘";
        }
    }


    private String getRecommendationEmoji(String rec) {
        switch (rec.toLowerCase()) {
            case "excellent":
                return "🌟🌟🌟🌟🌟";
            case "bon":
                return "🌟🌟🌟🌟";
            case "moyen":
                return "🌟🌟🌟";
            case "déconseillé":
                return "⚠️";
            default:
                return "❓";
        }
    }

    // ===================== ANALYSE GEMINI =====================
    // ===================== ANALYSE AVEC FALLBACK =====================
    private void analyzeEvent() {
        if (currentEvent == null) {
            System.out.println("❌ currentEvent est null");
            return;
        }

        analyzeBtn.setDisable(true);
        analyzeBtn.setText("⏳ Analyse en cours...");

        System.out.println("🔍 Début analyse pour: " + currentEvent.getTitre());

        new Thread(() -> {
            try {
                int eventId = currentEvent.getIdEvent();

                // Vérifier le cache
                if (analysisCache.containsKey(eventId)) {
                    System.out.println("✅ Résultat trouvé dans le cache");
                    javafx.application.Platform.runLater(() -> {
                        updateBadgesFromJson(analysisCache.get(eventId));
                        analyzeBtn.setDisable(false);
                        analyzeBtn.setText("🔍 Analyser avec IA");
                    });
                    return;
                }

                System.out.println("🔄 Appel à FallbackService...");
                String resultat = fallbackService.analyserEvenement(
                        currentEvent.getTitre(),
                        currentEvent.getDescription(),
                        currentEvent.getLieu()
                );

                System.out.println("📥 Réponse brute: '" + resultat + "'");

                // ✅ SOLUTION : NETTOYER LA RÉPONSE AVANT DE PARSER
                resultat = nettoyerReponseJSON(resultat);
                System.out.println("📥 Réponse nettoyée: '" + resultat + "'");

                // Maintenant parser le JSON
                JsonObject json = JsonParser.parseString(resultat).getAsJsonObject();

                // Sauvegarder dans le cache
                analysisCache.put(eventId, json);

                javafx.application.Platform.runLater(() -> {
                    updateBadgesFromJson(json);
                    analyzeBtn.setDisable(false);
                    analyzeBtn.setText("🔍 Analyser avec IA");
                    System.out.println("✅ Interface mise à jour");
                });

            } catch (Exception e) {
                System.err.println("❌ ERREUR: " + e.getMessage());
                e.printStackTrace();

                // ✅ VALEURS PAR DÉFAUT EN CAS D'ERREUR
                JsonObject defaultJson = new JsonObject();
                defaultJson.addProperty("noiseLevel", "Calme");
                defaultJson.addProperty("sensoryStimulation", "Moyenne");
                defaultJson.addProperty("recommendedAge", "5-10 ans");
                defaultJson.addProperty("activityLevel", "Modéré");
                defaultJson.addProperty("recommendation", "Bon");

                JsonObject finalDefault = defaultJson;
                javafx.application.Platform.runLater(() -> {
                    updateBadgesFromJson(finalDefault);
                    analyzeBtn.setDisable(false);
                    analyzeBtn.setText("🔍 Analyser avec IA");
                    showAlert("Info", "Analyse par défaut utilisée (problème de connexion)");
                });
            }
        }).start();
    }

    // ✅ AJOUTE CETTE MÉTHODE DE NETTOYAGE
    private String nettoyerReponseJSON(String texte) {
        if (texte == null || texte.isEmpty()) {
            return "{}";
        }

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

        // Remplacer les caractères mal encodés
        texte = texte.replace("Mod�r�", "Modéré")
                .replace("�", "é")
                .replace("è", "è")
                .replace("ê", "ê")
                .replace("à", "à")
                .replace("ç", "ç")
                .replace("œ", "oe");

        return texte;
    }
    // Dans n'importe quel contrôleur (ex: EventDetailsController)


}







