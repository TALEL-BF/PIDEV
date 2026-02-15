package Controller;



import Entites.Event;
import Entites.Sponsor;
import Services.EventServices;
import Services.SponsorServices;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
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
import javafx.util.Duration;
import javafx.scene.layout.VBox;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ShowEventController {

    @FXML private GridPane eventsGrid;  // Changé de FlowPane à GridPane
    @FXML private FlowPane sponsorCardsPane;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Button emotionDetectionBtn;
    @FXML private StackPane heroStackPane;
    @FXML private ImageView heroImage;
    @FXML private Label rotatingFlower;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageIndicator;
    @FXML private VBox calendarCard;
    @FXML private Label eventsStatLabel;
    @FXML private Label membersStatLabel;
    @FXML private Label locationsStatLabel;

    // Nouveaux filtres (style boutons)
    @FXML private Button filterAll;
    @FXML private Button filterSensory;
    @FXML private Button filterSupport;
    @FXML private Button filterSocial;
    @FXML private Button filterWorkshops;
    @FXML private Button detectHumeurBtn;  // ← Ajoute ceci avec les autres @FXML

    // Anciens filtres (commentés pour compatibilité)
    // @FXML private VBox filterMusicBox;
    // @FXML private VBox filterArtsBox;
    // etc.

    private EventServices eventServices = new EventServices();
    private SponsorServices sponsorServices = new SponsorServices();
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> currentFilteredEvents = new ArrayList<>();
    private int currentPage = 0;
    private final int EVENTS_PER_PAGE = 6;  // 6 événements par page
    private int totalPages = 1;
    private String currentFilter = "all";

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
        setupRotatingFlower();

        // ============ COMPTEUR ============
        updateCountLabel();
        if (emotionDetectionBtn != null) {
            emotionDetectionBtn.setOnAction(e -> openEmotionDetection());
        }
        if (detectHumeurBtn != null) {
            detectHumeurBtn.setOnAction(e -> detecterHumeur());
        }
    }
    private void updateCountLabel() {
        countLabel.setText(currentFilteredEvents.size() + " Événements");
    }
    private void setupFilters() {
        // Style initial : "Tous" en violet, les autres en gris
        setActiveFilter(filterAll);
        setInactiveFilter(filterSensory);
        setInactiveFilter(filterSupport);
        setInactiveFilter(filterSocial);
        setInactiveFilter(filterWorkshops);

        // Renommer les boutons pour correspondre aux types
        filterSensory.setText("Workshop");
        filterSupport.setText("Training");
        filterSocial.setText("Conference");
        filterWorkshops.setText("Social Activity");

        // Actions des filtres avec les types exacts
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
    private void filterByType(String type) {
        currentFilteredEvents = allEvents.stream()
                .filter(e -> type.equals(e.getTypeEvent()))
                .toList();
        updatePaginationAndDisplay();
    }

    private void setActiveFilter(Button btn) {
        btn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 25; " +
                "-fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");
    }

    private void setInactiveFilter(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #4a5568; -fx-background-radius: 25; " +
                "-fx-padding: 8 20; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 25;");
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
                // Re-apply current filter
                applyCurrentFilter();
                return;
            }
        } else {
            List<Event> sourceList = currentFilter.equals("all") ? allEvents : currentFilteredEvents;
            currentFilteredEvents = sourceList.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            e.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                            e.getLieu().toLowerCase().contains(searchText.toLowerCase()))
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

    private void setupRotatingFlower() {
        if (rotatingFlower != null) {
            RotateTransition rotate = new RotateTransition(Duration.seconds(6), rotatingFlower);
            rotate.setByAngle(360);
            rotate.setCycleCount(RotateTransition.INDEFINITE);
            rotate.setInterpolator(Interpolator.LINEAR);
            rotate.play();
        }
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
        double CARD_WIDTH = 260;
        double IMAGE_HEIGHT = 140;
        double CORNER_RADIUS = 20;

        // ===================== CARD =====================
        VBox card = new VBox(0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #faf5ff, #f3e8ff);" +  // ← Violet très très léger
                        "-fx-background-radius: " + CORNER_RADIUS + ";" +
                        "-fx-border-radius: " + CORNER_RADIUS + ";" +
                        "-fx-border-color: #e0d7ff;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 10, 0, 0, 4);"
        );
        // ===================== IMAGE PRINCIPALE =====================
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

        // Badge date
        Label dateBadge = new Label(formatDateWithoutYear(e.getDateDebut()));
        dateBadge.setStyle(
                "-fx-background-color: #8b5cf6;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 18;" +
                        "-fx-background-radius: 30;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;"
        );
        StackPane.setAlignment(dateBadge, Pos.TOP_CENTER);
        dateBadge.setTranslateY(-18);
        imageContainer.getChildren().add(dateBadge);
        imageWrapper.getChildren().add(imageContainer);

        // ===================== BANDE HEURE + LIEU =====================
        HBox infoBand = new HBox(10);
        infoBand.setAlignment(Pos.CENTER_LEFT);
        infoBand.setPadding(new Insets(8, 12, 8, 12));
        infoBand.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #a78bfa);");

        if (e.getHeureDebut() != null && e.getHeureFin() != null) {
            Label timeLabel = new Label("⏰ " + formatTime(e.getHeureDebut()) + " - " + formatTime(e.getHeureFin()));
            timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            infoBand.getChildren().add(timeLabel);
        }

        if (e.getLieu() != null && !e.getLieu().isEmpty()) {
            Label locationLabel = new Label("📍 " + e.getLieu());
            locationLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            infoBand.getChildren().add(locationLabel);
        }

        VBox topSection = new VBox(6);
        topSection.getChildren().addAll(imageWrapper, infoBand);

        // ===================== CONTENU =====================
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));

        // ✅ Titre avec IMAGE au lieu d'émoji
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // Image selon le type d'événement
        ImageView typeIcon = new ImageView();
        typeIcon.setFitWidth(55);
        typeIcon.setFitHeight(55);
        typeIcon.setPreserveRatio(true);

        String imageName = getEventImageName(e);
        try {
            URL imageUrl = getClass().getResource("/assets/" + imageName);
            if (imageUrl != null) {
                typeIcon.setImage(new Image(imageUrl.toString()));
            } else {
                // Image par défaut
                URL defaultIcon = getClass().getResource("/assets/event-default.png");
                if (defaultIcon != null) {
                    typeIcon.setImage(new Image(defaultIcon.toString()));
                }
            }
        } catch (Exception ex) {
            System.out.println("Erreur chargement icône: " + ex.getMessage());
        }

        Label title = new Label(e.getTitre());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        titleBox.getChildren().addAll(typeIcon, title);

        // ✅ Participants avec IMAGE au lieu d'émoji
        HBox participantsBox = new HBox(8);
        participantsBox.setAlignment(Pos.CENTER_LEFT);

        ImageView participantsIcon = new ImageView();
        participantsIcon.setFitWidth(22);
        participantsIcon.setFitHeight(23);
        participantsIcon.setPreserveRatio(true);

        try {
            URL participantsUrl = getClass().getResource("/assets/participants.png");
            if (participantsUrl != null) {
                participantsIcon.setImage(new Image(participantsUrl.toString()));
            }
        } catch (Exception ex) {
            System.out.println("Erreur chargement icône participants: " + ex.getMessage());
        }

        Label participantsValue = new Label(e.getMaxParticipant() + " participants");
        participantsValue.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        participantsBox.getChildren().addAll(participantsIcon, participantsValue);

        // Bouton détails
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle(
                "-fx-background-color: #f5f0ff;" +
                        "-fx-text-fill: #8b5cf6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-color: #d9c9ff;" +
                        "-fx-border-radius: 30;"
        );
        detailsBtn.setOnAction(ev -> openEventDetails(e));

        content.getChildren().addAll(titleBox, participantsBox, detailsBtn);
        card.getChildren().addAll(topSection, content);

        // Hover effect
        card.setOnMouseEntered(ev -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-radius: " + CORNER_RADIUS + ";" +
                            "-fx-border-color: #8b5cf6;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.25), 15, 0, 0, 6);"
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
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 10, 0, 0, 4);"
            );
        });

        return card;
    }

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

        // Configuration pour 4 colonnes maximum
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            eventsGrid.getColumnConstraints().add(col);
        }

        eventsGrid.setHgap(15);
        eventsGrid.setVgap(20);

        int start = pageIndex * EVENTS_PER_PAGE;
        int end = Math.min(start + EVENTS_PER_PAGE, currentFilteredEvents.size());

        if (currentFilteredEvents.isEmpty()) {
            showNoEventsMessage();
            return;
        }

        List<Event> pageEvents = currentFilteredEvents.subList(start, end);

        // Disposition spéciale: 4 cartes sur la 1ère ligne, 2 cartes centrées sur la 2ème ligne
        if (pageEvents.size() == 6) {
            // Ligne 1: 4 cartes (colonnes 0,1,2,3)
            for (int i = 0; i < 4; i++) {
                VBox card = createEventCard(pageEvents.get(i));
                eventsGrid.add(card, i, 0);
            }

            // Ligne 2: 2 cartes centrées (colonnes 1 et 2)
            VBox card5 = createEventCard(pageEvents.get(4));
            eventsGrid.add(card5, 1, 1);

            VBox card6 = createEventCard(pageEvents.get(5));
            eventsGrid.add(card6, 2, 1);
        }
        // Si moins de 6 événements, disposition normale
        else {
            int row = 0;
            int col = 0;

            for (Event e : pageEvents) {
                VBox card = createEventCard(e);
                eventsGrid.add(card, col, row);

                col++;
                if (col >= 4) {
                    col = 0;
                    row++;
                }
            }
        }

        updateCountAndPagination(pageEvents.size(), pageIndex);
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

        countLabel.setText("0 Événements");
        pageIndicator.setText("Page 0 / 0");
        prevPageBtn.setDisable(true);
        nextPageBtn.setDisable(true);
    }

    private void updateCountAndPagination(int pageSize, int pageIndex) {
        countLabel.setText(pageSize + " Événements");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetailsPage.fxml"));
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
    private void openCalendar() {
        try {
            System.out.println("🔍 Tentative d'ouverture du calendrier...");
            System.out.println("📁 Chemin: /EventCalendar.fxml");

            URL url = getClass().getResource("/EventCalendar.fxml");
            System.out.println("📌 URL trouvée: " + url);

            if (url == null) {
                System.out.println("❌ FICHIER INTROUVABLE !");
                showAlert("Erreur", "Fichier EventCalendar.fxml introuvable dans resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            System.out.println("✅ Fichier chargé avec succès !");

            Stage stage = (Stage) rotatingFlower.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Calendrier des événements");
            stage.centerOnScreen();

        } catch (IOException e) {
            System.out.println("❌ IOException: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
        }
    }
    @FXML
    private void openEmotionDetection() {
        try {
            System.out.println("🔍 Ouverture de la détection d'émotions...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmotionDetection.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmotionDetection.fxml"));
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