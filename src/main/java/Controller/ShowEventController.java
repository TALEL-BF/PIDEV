package com.auticare.controllers;



import com.auticare.entities.Event;
import com.auticare.entities.Sponsor;
import com.auticare.services.EventServices;
import com.auticare.services.GeminiService;
import com.auticare.services.SponsorServices;
import java.time.Duration;
import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
// ... autres imports
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Box;

import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowEventController {

    @FXML private GridPane eventsGrid;  // Changé de FlowPane à GridPane
    @FXML private FlowPane sponsorCardsPane;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Label nextEventLocationLabel;  // ← Ajoute cette ligne avec les autres @FXML
    @FXML private Button emotionDetectionBtn;
    @FXML private StackPane heroStackPane;
    @FXML private ImageView heroImage;
    @FXML private Label rotatingFlower;
    @FXML private Label hoursLabel;
    @FXML private Label minutesLabel;
    @FXML private Label secondsLabel;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Button faceEmotionBtn;  // ← Ajoute avec les autres @FXML
    @FXML private Label pageIndicator;

    @FXML private Label eventsStatLabel;
    @FXML private Label membersStatLabel;
    @FXML private Label locationsStatLabel;

    // Nouveaux filtres (style boutons)
    @FXML private Button filterAll;
    @FXML private Button filterSensory;
    @FXML private Button filterSupport;
    @FXML private Button filterSocial;
    @FXML private Button filterWorkshops;
    @FXML private Button detectHumeurBtn;
    @FXML private ImageView floatingParticipationsBtn;
    @FXML private Label countdownLabel;
    @FXML private Label nextEventLabel;

    private Timeline countdownTimer;
    private LocalDateTime nextEventTime;
    @FXML private StackPane hoursBox;
    @FXML private StackPane minutesBox;
    @FXML private StackPane secondsBox;


    // ← Ajoute ceci avec les autres @FXML



    private EventServices eventServices = new EventServices();
    private SponsorServices sponsorServices = new SponsorServices();
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> currentFilteredEvents = new ArrayList<>();
    private int currentPage = 0;
    private final int EVENTS_PER_PAGE = 6;  // 6 événements par page
    private int totalPages = 1;
    private String currentFilter = "all";
    // En haut de ta classe, avec les autres variables
    private GeminiService geminiService = new GeminiService();
    private Map<Integer, JsonObject> analysisCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Load all events
        allEvents = eventServices.afficherEvent();
        currentFilteredEvents = new ArrayList<>(allEvents);
        updateTotalPages();



        // Load sponsors
        loadSponsors();

        // Setup filters
        setupFilters();
        updateStatistics();

        // Load first page
        currentPage = 0;
        loadPage(currentPage);

        // ============ RECHERCHE ============
        setupSearch();

        // ============ PAGINATION ============
        setupPagination();

        // ============ ROTATION FLEUR ============


        // ============ COMPTEUR ============

        if (emotionDetectionBtn != null) {
            emotionDetectionBtn.setOnAction(e -> openEmotionDetection());
        }
        if (detectHumeurBtn != null) {
            detectHumeurBtn.setOnAction(e -> detecterHumeur());
        }
    }
    @FXML
    private void openFaceEmotion() {
        try {
            System.out.println("🔍 Ouverture de la détection d'émotions...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FaceEmotion.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) eventsGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détection d'émotions");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la détection d'émotions");
        }
    }


    // Nouvelle méthode pour le style initial (tous avec bordure violette)
    private void setInitialFilterStyle(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #8b5cf6; " +
                "-fx-background-radius: 40; -fx-padding: 10 25; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-cursor: hand; -fx-border-color: #8b5cf6; " +
                "-fx-border-width: 2; -fx-border-radius: 40;");
    }

    private void setupFilters() {
        // Style initial : tous les boutons avec bordure violette
        setInitialFilterStyle(filterAll);
        setInitialFilterStyle(filterSensory);
        setInitialFilterStyle(filterSupport);
        setInitialFilterStyle(filterSocial);
        setInitialFilterStyle(filterWorkshops);

        // Rendre "TOUS" actif au départ
        setActiveFilter(filterAll);

        // ✅ AJOUTER LES ICÔNES AUX FILTRES
        filterAll.setText(" TOUS");
        filterSensory.setText("🎨 WORKSHOP");
        filterSupport.setText("📚 TRAINING");
        filterSocial.setText("🎤 CONFERENCE");
        filterWorkshops.setText("👥 SOCIAL ACTIVITY");

        // Actions des filtres
        filterAll.setOnAction(e -> {
            currentFilter = "all";
            currentFilteredEvents = new ArrayList<>(allEvents);
            setActiveFilter(filterAll);
            setInactiveFilter(filterSensory);
            setInactiveFilter(filterSupport);
            setInactiveFilter(filterSocial);
            setInactiveFilter(filterWorkshops);
            updatePaginationAndDisplay();
        });

        filterSensory.setOnAction(e -> {
            currentFilter = "Workshop";
            filterByType("Workshop");
            setActiveFilter(filterSensory);
            setInactiveFilter(filterAll);
            setInactiveFilter(filterSupport);
            setInactiveFilter(filterSocial);
            setInactiveFilter(filterWorkshops);
        });

        filterSupport.setOnAction(e -> {
            currentFilter = "Training";
            filterByType("Training");
            setActiveFilter(filterSupport);
            setInactiveFilter(filterAll);
            setInactiveFilter(filterSensory);
            setInactiveFilter(filterSocial);
            setInactiveFilter(filterWorkshops);
        });

        filterSocial.setOnAction(e -> {
            currentFilter = "Conference";
            filterByType("Conference");
            setActiveFilter(filterSocial);
            setInactiveFilter(filterAll);
            setInactiveFilter(filterSensory);
            setInactiveFilter(filterSupport);
            setInactiveFilter(filterWorkshops);
        });

        filterWorkshops.setOnAction(e -> {
            currentFilter = "Social Activity";
            filterByType("Social Activity");
            setActiveFilter(filterWorkshops);
            setInactiveFilter(filterAll);
            setInactiveFilter(filterSensory);
            setInactiveFilter(filterSupport);
            setInactiveFilter(filterSocial);
        });
    }
    @FXML
    private void openParticipationsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ParticipationsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) floatingParticipationsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes participations");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page des participations");
        }
    }

    // Style initial (tous avec bordure violette) - PLUS PETIT


    // Bouton actif (fond violet) - PLUS PETIT
    // Style actif avec couleur spécifique
    // Bouton actif avec taille MOYENNE
    // Bouton actif avec taille ENTRE-DEUX
    // Bouton actif avec taille UN PEU PLUS GRANDE
    private void setActiveFilter(Button btn) {
        if (btn == filterSensory) {
            btn.setStyle("-fx-background-color: #F97316; -fx-text-fill: white; " +  // Orange pour Workshop
                    "-fx-background-radius: 30; -fx-padding: 9 20; " +   // ← PLUS GRAND (8 20 au lieu de 7 18)
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND (14px au lieu de 13px)
                    "-fx-cursor: hand; -fx-border-color: #F97316; " +
                    "-fx-border-width: 2; -fx-border-radius: 30;");
        } else if (btn == filterSupport) {
            btn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +  // Vert pour Training
                    "-fx-background-radius: 30; -fx-padding: 9 20; " +   // ← PLUS GRAND
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND
                    "-fx-cursor: hand; -fx-border-color: #10B981; " +
                    "-fx-border-width: 2; -fx-border-radius: 30;");
        } else if (btn == filterSocial) {
            btn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +  // Bleu pour Conference
                    "-fx-background-radius: 30; -fx-padding: 9 20; " +   // ← PLUS GRAND
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND
                    "-fx-cursor: hand; -fx-border-color: #3B82F6; " +
                    "-fx-border-width: 2; -fx-border-radius: 30;");
        } else if (btn == filterWorkshops) {
            btn.setStyle("-fx-background-color: #EC4899; -fx-text-fill: white; " +  // Rose pour Social
                    "-fx-background-radius: 30; -fx-padding: 9 20; " +   // ← PLUS GRAND
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND
                    "-fx-cursor: hand; -fx-border-color: #EC4899; " +
                    "-fx-border-width: 2; -fx-border-radius: 30;");
        } else {
            btn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; " +  // Violet pour TOUS
                    "-fx-background-radius: 30; -fx-padding: 9 20; " +   // ← PLUS GRAND
                    "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND
                    "-fx-cursor: hand; -fx-border-color: #8b5cf6; " +
                    "-fx-border-width: 2; -fx-border-radius: 30;");
        }
    }

    // Style inactif avec taille UN PEU PLUS GRANDE
    private void setInactiveFilter(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #8b5cf6; " +
                "-fx-background-radius: 30; -fx-padding: 8 20; " +   // ← PLUS GRAND
                "-fx-font-weight: bold; -fx-font-size: 14px; " +    // ← PLUS GRAND
                "-fx-cursor: hand; -fx-border-color: #8b5cf6; " +
                "-fx-border-width: 2; -fx-border-radius: 30;");
    }


    private void filterByType(String type) {
        currentFilteredEvents = allEvents.stream()
                .filter(e -> type.equals(e.getTypeEvent()))
                .toList();
        updatePaginationAndDisplay();
    }












    private void filterByKeyword(String keyword) {
        String[] keywords = keyword.split("\\|");
        currentFilteredEvents = allEvents.stream()
                .filter(e -> matchesFilter(e, keywords))
                .toList();
        updatePaginationAndDisplay();
    }

    private boolean matchesFilter(Event e, String... keywords) {
        String desc = e.getDescription().toLowerCase();
        String titre = e.getTitre().toLowerCase();
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";

        for (String k : keywords) {
            if (desc.contains(k.toLowerCase()) ||
                    titre.contains(k.toLowerCase()) ||
                    type.contains(k.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterEventsBySearch(newText);
        });
    }

    private void filterEventsBySearch(String searchText) {
        if (searchText.isEmpty()) {
            if (currentFilter.equals("all")) {
                currentFilteredEvents = new ArrayList<>(allEvents);
            } else {
                applyCurrentFilter();
                return;
            }
        } else {
            String searchLower = searchText.toLowerCase();

            List<Event> sourceList = currentFilter.equals("all") ? allEvents : currentFilteredEvents;
            currentFilteredEvents = sourceList.stream()
                    .filter(e ->
                            // Recherche dans le TEXTE
                            e.getTitre().toLowerCase().contains(searchLower) ||
                                    e.getDescription().toLowerCase().contains(searchLower) ||
                                    e.getLieu().toLowerCase().contains(searchLower) ||

                                    // ✅ RECHERCHE DANS LES CHIFFRES
                                    String.valueOf(e.getMaxParticipant()).contains(searchText) ||          // ← Cherche dans le nombre de participants
                                    (e.getHeureDebut() != null && e.getHeureDebut().toString().contains(searchText)) || // ← Cherche dans l'heure
                                    (e.getHeureFin() != null && e.getHeureFin().toString().contains(searchText)) ||     // ← Cherche dans l'heure
                                    (e.getDateDebut() != null && e.getDateDebut().toString().contains(searchText)) ||   // ← Cherche dans la date
                                    (e.getDateFin() != null && e.getDateFin().toString().contains(searchText))          // ← Cherche dans la date
                    )
                    .toList();
        }
        updatePaginationAndDisplay();
    }

    private void applyCurrentFilter() {
        switch(currentFilter) {
            case "Workshop":
                filterByType("Workshop");
                break;
            case "Training":
                filterByType("Training");
                break;
            case "Conference":
                filterByType("Conference");
                break;
            case "Social Activity":
                filterByType("Social Activity");
                break;
            default:
                currentFilteredEvents = new ArrayList<>(allEvents);
        }
    }

    private void setupPagination() {
        prevPageBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage(currentPage);
            }
        });

        nextPageBtn.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadPage(currentPage);
            }
        });
    }



    private void updateTotalPages() {
        totalPages = (int) Math.ceil((double) currentFilteredEvents.size() / EVENTS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
    }

    private void updatePaginationAndDisplay() {
        updateTotalPages();
        currentPage = 0;
        loadPage(currentPage);
    }

    private VBox createEventCard(Event e) {
        double CARD_WIDTH = 300;
        double IMAGE_HEIGHT = 150;
        double CORNER_RADIUS = 20;

        // ===================== CARTE =====================
        VBox card = new VBox(0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: " + CORNER_RADIUS + ";" +
                        "-fx-border-radius: " + CORNER_RADIUS + ";" +
                        "-fx-border-color: #e0d7ff;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 12, 0, 0, 4);"
        );

        // ===================== IMAGE AVEC BADGE =====================
        VBox imageWrapper = new VBox();
        imageWrapper.setPadding(new Insets(8, 8, 0, 8));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(IMAGE_HEIGHT);

        Rectangle clip = new Rectangle(CARD_WIDTH - 16, IMAGE_HEIGHT);
        clip.setArcWidth(CORNER_RADIUS);
        clip.setArcHeight(CORNER_RADIUS);

        ImageView eventImage = new ImageView();
        eventImage.setFitWidth(CARD_WIDTH - 16);
        eventImage.setFitHeight(IMAGE_HEIGHT);
        eventImage.setPreserveRatio(false);
        eventImage.setClip(clip);
        loadEventImage(e, eventImage);

        imageContainer.getChildren().add(eventImage);

        // BADGE TYPE
        String typeEvent = e.getTypeEvent() != null ? e.getTypeEvent() : "Événement";
        String typeColor = getTypeColor(typeEvent);

        Label typeBadge = new Label(typeEvent.toUpperCase());
        typeBadge.setStyle(
                "-fx-background-color: " + typeColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 30;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"
        );
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        typeBadge.setTranslateX(10);
        typeBadge.setTranslateY(10);
        imageContainer.getChildren().add(typeBadge);

        imageWrapper.getChildren().add(imageContainer);

        // ===================== BANDE VIOLETTE AVEC ÉMOJIS =====================
        HBox infoBand = new HBox();
        infoBand.setAlignment(Pos.CENTER_LEFT);
        infoBand.setPadding(new Insets(6, 10, 6, 10));
        infoBand.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #a78bfa);");

        // LIEU avec émoji 📍
        if (e.getLieu() != null && !e.getLieu().isEmpty()) {
            Label locationEmoji = new Label("📍");
            locationEmoji.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

            Label locationLabel = new Label(" " + e.getLieu());
            locationLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

            HBox locationBox = new HBox(3, locationEmoji, locationLabel);
            locationBox.setAlignment(Pos.CENTER_LEFT);
            infoBand.getChildren().add(locationBox);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        infoBand.getChildren().add(spacer);

        // HEURE avec émoji ⏰
        if (e.getHeureDebut() != null && e.getHeureFin() != null) {
            Label timeEmoji = new Label("⏰");
            timeEmoji.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

            Label timeLabel = new Label(" " + formatTime(e.getHeureDebut()) + " - " + formatTime(e.getHeureFin()));
            timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

            HBox timeBox = new HBox(3, timeEmoji, timeLabel);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            infoBand.getChildren().add(timeBox);
        }

        // ===================== CONTENU TEXTE =====================
        VBox content = new VBox(6);
        content.setPadding(new Insets(12, 12, 12, 12));

        // TITRE
        Label title = new Label(e.getTitre());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // DESCRIPTION
        Label descriptionLabel = null;
        if (e.getDescription() != null && !e.getDescription().isEmpty()) {
            String shortDesc = e.getDescription().length() > 70 ?
                    e.getDescription().substring(0, 70) + "..." :
                    e.getDescription();

            descriptionLabel = new Label(shortDesc);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-padding: 2 0 5 0;");
        }

        // DATE avec émoji 📅
        HBox dateBox = new HBox(3);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label dateEmoji = new Label("📅");
        dateEmoji.setStyle("-fx-font-size: 16px; -fx-text-fill: #6B7280;");

        Label dateLabel = new Label(" " + formatDateWithoutYear(e.getDateDebut()));
        dateLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        dateBox.getChildren().addAll(dateEmoji, dateLabel);

        // PARTICIPANTS avec émoji 👥
        HBox participantsBox = new HBox(3);
        participantsBox.setAlignment(Pos.CENTER_LEFT);

        Label participantsEmoji = new Label("👥");
        participantsEmoji.setStyle("-fx-font-size: 16px; -fx-text-fill: #6B7280;");

        Label participantsLabel = new Label(" " + e.getMaxParticipant() + " participants");
        participantsLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        participantsBox.getChildren().addAll(participantsEmoji, participantsLabel);

        // Ajout dans l'ordre
        content.getChildren().add(title);
        if (descriptionLabel != null) {
            content.getChildren().add(descriptionLabel);
        }
        content.getChildren().add(dateBox);
        content.getChildren().add(participantsBox);

        // ===================== BOUTON DÉGRADÉ VIOLET =====================
        Button detailsBtn = new Button("VOIR DÉTAILS");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #9F7AEA, #7B2FF7);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 40;" +
                        "-fx-padding: 12 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.3), 8, 0, 0, 2);"
        );
        detailsBtn.setOnAction(ev -> openEventDetails(e));

        // Hover bouton
        detailsBtn.setOnMouseEntered(ev -> {
            detailsBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 40;" +
                            "-fx-padding: 12 15;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(109,40,217,0.5), 12, 0, 0, 4);" +
                            "-fx-scale-x: 1.02; -fx-scale-y: 1.02;"
            );
        });

        detailsBtn.setOnMouseExited(ev -> {
            detailsBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #9F7AEA, #7B2FF7);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 40;" +
                            "-fx-padding: 12 15;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.3), 8, 0, 0, 2);"
            );
        });

        // Assembler
        card.getChildren().addAll(imageWrapper, infoBand, content, detailsBtn);
        VBox.setMargin(detailsBtn, new Insets(0, 12, 12, 12));

        // ===================== HOVER CARTE =====================
        card.setOnMouseEntered(ev -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-color: #8b5cf6;" +
                            "-fx-border-width: 2.5;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.4), 20, 0, 0, 8);"
            );
        });

        card.setOnMouseExited(ev -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-color: #e0d7ff;" +
                            "-fx-border-width: 2;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 12, 0, 0, 4);"
            );
        });

        return card;
    }
    // Méthode pour donner une couleur à l'emoji selon le type d'événement
    private String getEmojiColor(Event e) {
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";

        if (type.contains("workshop") || type.contains("atelier")) {
            return "#F97316"; // Orange
        } else if (type.contains("training") || type.contains("formation")) {
            return "#10B981"; // Vert
        } else if (type.contains("conference")) {
            return "#3B82F6"; // Bleu
        } else if (type.contains("social")) {
            return "#EC4899"; // Rose
        } else if (type.contains("music") || type.contains("musique")) {
            return "#8B5CF6"; // Violet
        } else {
            return "#8B5CF6"; // Violet par défaut
        }
    }

// ===================== MÉTHODES POUR LES COULEURS =====================

    private String getTypeColor(String typeEvent) {
        if (typeEvent == null) return "#8B5CF6";

        switch(typeEvent.toLowerCase()) {
            case "workshop":
            case "atelier":
                return "#F97316"; // Orange
            case "training":
            case "formation":
                return "#10B981"; // Vert
            case "conference":
                return "#3B82F6"; // Bleu
            case "social activity":
            case "activité sociale":
                return "#EC4899"; // Rose
            case "music":
            case "musique":
                return "#8B5CF6"; // Violet
            case "sport":
                return "#EF4444"; // Rouge
            default:
                return "#8B5CF6"; // Violet par défaut
        }
    }

    private String getSensoryBgColor(String level) {
        switch(level) {
            case "Faible": return "#D1FAE5";  // Vert clair
            case "Moyenne": return "#FEF3C7"; // Jaune clair
            case "Élevée": return "#FEE2E2";  // Rouge clair
            default: return "#F3F4F6";
        }
    }

    private String getSensoryColor(String level) {
        switch(level) {
            case "Faible": return "#065F46";  // Vert foncé
            case "Moyenne": return "#92400E"; // Jaune foncé
            case "Élevée": return "#991B1B";  // Rouge foncé
            default: return "#4B5563";
        }
    }

    private String getSensoryLabel(String sensoryLevel) {
        switch(sensoryLevel) {
            case "Faible": return "Low Sensory";
            case "Moyenne": return "Moderate";
            case "Élevée": return "High Sensory";
            default: return "Low Sensory";
        }
    }
    private String formatDateWithMonth(java.sql.Date date) {
        if (date == null) return "?";
        // Convertir java.sql.Date en LocalDate
        return date.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

// ===================== MÉTHODES UTILITAIRES POUR LES COULEURS =====================








// ===================== MÉTHODES UTILITAIRES =====================










    // ✅ Nouvelle méthode pour obtenir le nom de l'image selon le type
    private String getEventImageName(Event e) {
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";

        if (type.contains("workshop")) return "workshop.png";
        if (type.contains("training")) return "training.png";
        if (type.contains("conference")) return "conference.png";
        if (type.contains("social")) return "social.png";
        if (type.contains("therapy")) return "therapy.png";

        return "event-default.png"; // Image par défaut
    }


    // Helper to load image with fallback
    private void loadEventImage(Event e, ImageView imageView) {
        try {
            if(e.getImage() != null && !e.getImage().isEmpty()) {
                File f = new File(e.getImage());
                if(f.exists()) {
                    imageView.setImage(new Image("file:" + e.getImage(), imageView.getFitWidth(), imageView.getFitHeight(), false, true));
                } else loadDefaultImage(imageView, imageView.getFitWidth(), imageView.getFitHeight());
            } else loadDefaultImage(imageView, imageView.getFitWidth(), imageView.getFitHeight());
        } catch(Exception ex) { loadDefaultImage(imageView, imageView.getFitWidth(), imageView.getFitHeight()); }
    }

    private String formatDateWithoutYear(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        return day + " " + months[month];
    }

    private void loadDefaultImage(ImageView imageView, double width, double height) {
        try {
            URL defaultImgUrl = getClass().getResource("/assets/event-default.jpg");
            if (defaultImgUrl != null) {
                Image defaultImage = new Image(defaultImgUrl.toString(), width, height, false, true);
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    private void loadPage(int pageIndex) {
        eventsGrid.getChildren().clear();
        eventsGrid.getColumnConstraints().clear();

        // Configuration pour 3 colonnes maximum (au lieu de 4)
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);  // 100% / 3 = 33.33%
            col.setHgrow(Priority.ALWAYS);
            eventsGrid.getColumnConstraints().add(col);
        }

        eventsGrid.setHgap(25);  // Espacement horizontal augmenté (15 → 25)
        eventsGrid.setVgap(30);   // Espacement vertical augmenté (20 → 30)
        eventsGrid.setAlignment(Pos.CENTER); // Centrer la grille

        int start = pageIndex * EVENTS_PER_PAGE;
        int end = Math.min(start + EVENTS_PER_PAGE, currentFilteredEvents.size());

        if (currentFilteredEvents.isEmpty()) {
            showNoEventsMessage();
            return;
        }

        List<Event> pageEvents = currentFilteredEvents.subList(start, end);

        // Disposition pour 3 colonnes
        int row = 0;
        int col = 0;

        for (Event e : pageEvents) {
            VBox card = createEventCard(e);
            eventsGrid.add(card, col, row);

            // Centrer les cartes si moins de 3 sur la dernière ligne
            GridPane.setHalignment(card, HPos.CENTER);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        // Si la ligne n'est pas pleine, centrer les cartes restantes
        if (col > 0) {
            // Ajouter des colonnes vides pour centrer
            while (col < 3) {
                Region spacer = new Region();
                eventsGrid.add(spacer, col, row);
                col++;
            }
        }

        updateCountAndPagination(pageEvents.size(), pageIndex);
    }
    @FXML
    private void showNewEvents() {
        System.out.println("🔗 Nouveaux événements cliqués !");
        // Ici tu peux ajouter la logique pour filtrer les nouveaux événements
        // Par exemple :
        // currentFilter = "new";
        // updatePaginationAndDisplay();
    }
    // Méthode pour charger l'image par défaut avec les bonnes dimensions

    private void loadDefaultImage(ImageView imageView) {
        try {
            URL defaultImgUrl = getClass().getResource("/assets/event-default.jpg");
            if (defaultImgUrl != null) {
                Image defaultImage = new Image(defaultImgUrl.toString(), 260, 140, false, true);
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCardHoverEffects(VBox card) {
        card.setOnMouseEntered(ev -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #8b5cf6;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.1), 10, 0, 0, 3);"
            );
            card.setScaleX(1.01);
            card.setScaleY(1.01);
        });

        card.setOnMouseExited(ev -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 5, 0, 0, 1);"
            );
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
    }



    private void showNoEventsMessage() {
        Label noEvents = new Label("Aucun événement trouvé");
        noEvents.setStyle("-fx-text-fill: #718096; -fx-font-size: 18px; -fx-font-weight: 600; -fx-padding: 40;");
        eventsGrid.getChildren().add(noEvents);


        pageIndicator.setText("Page 0 / 0");
        prevPageBtn.setDisable(true);
        nextPageBtn.setDisable(true);
    }

    private void updateCountAndPagination(int pageSize, int pageIndex) {

        pageIndicator.setText("Page " + (pageIndex + 1) + " / " + totalPages);
        prevPageBtn.setDisable(currentPage == 0);
        nextPageBtn.setDisable(currentPage >= totalPages - 1);
    }

    // ================= LOAD SPONSORS =================
    private void loadSponsors() {
        if (sponsorCardsPane == null) return;

        sponsorCardsPane.getChildren().clear();
        List<Sponsor> sponsors = sponsorServices.afficherSponsor();

        if (sponsors.isEmpty()) {
            Label noSponsor = new Label("Aucun sponsor pour le moment");
            noSponsor.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-font-style: italic;");
            sponsorCardsPane.getChildren().add(noSponsor);
            return;
        }

        for (Sponsor s : sponsors) {
            VBox card = createSponsorCard(s);
            sponsorCardsPane.getChildren().add(card);
        }
    }

    private VBox createSponsorCard(Sponsor s) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);" +
                        "-fx-border-color: #f3e8ff;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;"
        );

        ImageView logoImage = new ImageView();
        logoImage.setFitWidth(120);
        logoImage.setFitHeight(80);
        logoImage.setPreserveRatio(true);

        try {
            if (s.getImage() != null && !s.getImage().isEmpty()) {
                File imgFile = new File(s.getImage());
                if (imgFile.exists()) {
                    logoImage.setImage(new Image("file:" + s.getImage()));
                } else {
                    loadDefaultSponsorImage(logoImage);
                }
            } else {
                loadDefaultSponsorImage(logoImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultSponsorImage(logoImage);
        }

        Label nameLabel = new Label(s.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #8b5cf6;");
        nameLabel.setWrapText(true);

        Label typeLabel = new Label(s.getTypeSponsor());
        typeLabel.setStyle(
                "-fx-background-color: #f3e8ff;" +
                        "-fx-text-fill: #8b5cf6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 4 12;" +
                        "-fx-background-radius: 20;"
        );

        card.getChildren().addAll(logoImage, nameLabel, typeLabel);
        return card;
    }

    private void loadDefaultSponsorImage(ImageView imageView) {
        URL defaultImg = getClass().getResource("/assets/sponsor-default.png");
        if (defaultImg != null) {
            imageView.setImage(new Image(defaultImg.toString()));
        }
    }

    // ================= FORMAT DATE =================


    // ================= OPEN EVENT DETAILS =================
    private void openEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventDetailsPage.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);
            Stage stage = (Stage) eventsGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(event.getTitre());

            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de détails: " + ex.getMessage());
        }
    }

    // ================= SHOW ALERT =================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ================= FORMAT TIME =================
    private String formatTime(java.sql.Time time) {
        if (time == null) return "?";
        String timeStr = time.toString();
        return timeStr.length() >= 5 ? timeStr.substring(0, 5) : timeStr;
    }
    private String formatDate(java.sql.Date date) {
        if (date == null) return "?";
        String[] parts = date.toString().split("-");
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        return day + " " + months[month] + " " + parts[0];
    }
    // ================= GET EMOJI SELON LE TYPE D'ÉVÉNEMENT =================
    private String getEventEmoji(Event e) {
        String titre = e.getTitre().toLowerCase();
        String type = e.getTypeEvent() != null ? e.getTypeEvent().toLowerCase() : "";
        String desc = e.getDescription().toLowerCase();

        if (titre.contains("musique") || type.contains("musique") || type.contains("music") || desc.contains("musique")) {
            return "🎵";
        } else if (titre.contains("art") || type.contains("art") || type.contains("peinture") || desc.contains("art")) {
            return "🎨";
        } else if (titre.contains("théâtre") || type.contains("theatre") || type.contains("spectacle") || desc.contains("théâtre")) {
            return "🎭";
        } else if (titre.contains("fête") || type.contains("fete") || type.contains("party") || desc.contains("fête")) {
            return "🎉";
        } else if (titre.contains("sport") || type.contains("sport") || desc.contains("sport")) {
            return "⚽";
        } else if (titre.contains("jeu") || type.contains("jeu") || type.contains("game") || desc.contains("jeu")) {
            return "🎮";
        } else if (titre.contains("rencontre") || type.contains("meeting") || type.contains("social") || desc.contains("rencontre")) {
            return "🤝";
        } else if (titre.contains("nature") || type.contains("nature") || type.contains("parc") || desc.contains("nature")) {
            return "🌿";
        } else if (titre.contains("atelier") || type.contains("workshop") || desc.contains("atelier")) {
            return "🔧";
        } else if (titre.contains("conférence") || type.contains("conference") || desc.contains("conférence")) {
            return "📢";
        } else {
            return "📅"; // Emoji par défaut
        }
    }
    private void setupHeroImage() {
        if (heroImage != null) {
            // ✅ FORCER le redimensionnement
            heroImage.setPreserveRatio(false);
            heroImage.setFitWidth(1050);
            heroImage.setFitHeight(220);

            // Charger l'image
            try {
                URL heroUrl = getClass().getResource("/assets/jeux (6).jpg");
                if (heroUrl != null) {
                    heroImage.setImage(new Image(heroUrl.toString()));
                }
            } catch (Exception e) {
                System.out.println("Erreur chargement hero image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void openEmotionDetection() {
        try {
            System.out.println("🔍 Ouverture de la détection d'émotions...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EmotionDetection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) eventsGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détection d'émotions");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la détection d'émotions");
        }
    }
    @FXML
    private void detecterHumeur() {
        try {
            System.out.println("🔍 Navigation vers la détection d'humeur...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EmotionDetection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) detectHumeurBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détection d'humeur");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de détection");
        }
    }
    private void updateStatistics() {
        // Récupérer les vraies données
        int totalEvents = allEvents.size();
        int totalMembers = calculerNombreMembres(); // Si tu as cette méthode
        int totalLocations = (int) allEvents.stream()
                .map(Event::getLieu)
                .distinct()
                .count();

        // Mettre à jour les labels
        // Tu dois ajouter des @FXML pour ces labels
        eventsStatLabel.setText(totalEvents + " Événements");
        membersStatLabel.setText(totalMembers + " Membres");
        locationsStatLabel.setText(totalLocations + " Lieux");
    }
    private int calculerNombreMembres() {
        // Somme de tous les maxParticipant de tous les événements
        return allEvents.stream()
                .mapToInt(Event::getMaxParticipant)
                .sum();
    }
}