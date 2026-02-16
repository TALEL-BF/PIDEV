package Controller;

import Entites.Event;
import Entites.Sponsor;
import Services.EventServices;
import Services.SponsorServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class EventDetailsController {

    @FXML private ImageView heroImageView;
    @FXML private ImageView eventImageView;
    @FXML private Label locationMapLabel;  // Nouveau label pour la carte
    @FXML private Label eventTitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label locationLabel;
    @FXML private TextArea descriptionArea;
    @FXML private VBox titleContainer;
    @FXML private FlowPane sponsorsFlowPane;
    @FXML private Button registerBtn;
    @FXML private Button backButton;
    @FXML private Button rapportIABtn;

    // Nouveaux composants
    @FXML private HBox galleryContainer;
    @FXML private FlowPane tagsContainer;
    @FXML private PieChart participantsChart;
    @FXML private Label participantsCountLabel;
    @FXML private Label inscritsCount;
    @FXML private Label restantsCount;
    @FXML private Label locationFullLabel;
    @FXML private HBox similarEventsContainer;
    @FXML private GridPane feedbacksGrid;
    @FXML private Label maxParticipantsLabel;

    private EventServices eventServices = new EventServices();
    private Event currentEvent;

    @FXML
    public void initialize() {
        // Bouton Participer
        if (registerBtn != null) {
            registerBtn.setOnAction(e -> {
                if (currentEvent != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Inscription");
                    alert.setHeaderText("S'inscrire à l'événement");
                    alert.setContentText("Voulez-vous vous inscrire à \"" + currentEvent.getTitre() + "\" ?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Succès");
                        success.setHeaderText(null);
                        success.setContentText("Vous êtes inscrit à l'événement !");
                        success.showAndWait();
                    }
                }
            });
        }

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

        // Initialiser la galerie avec des images par défaut
        initGallery();

        // Initialiser les tags
        initTags();

        // Initialiser les événements similaires
        initSimilarEvents();

        // Initialiser les avis
        initFeedbacks();
    }

    public void setEvent(Event event) {
        this.currentEvent = event;

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
        int inscrits = (int)(Math.random() * maxParticipants);
        int restants = maxParticipants - inscrits;

        if (participantsCountLabel != null) {
            participantsCountLabel.setText(inscrits + "/" + maxParticipants + " inscrits");
        }
        if (inscritsCount != null) {
            inscritsCount.setText(String.valueOf(inscrits));
        }
        if (restantsCount != null) {
            restantsCount.setText(String.valueOf(restants));
        }
        if (maxParticipantsLabel != null) {
            maxParticipantsLabel.setText(maxParticipants + " personnes");
        }

        // Mettre à jour le graphique
        if (participantsChart != null) {
            participantsChart.getData().clear();
            participantsChart.getData().add(new PieChart.Data("Inscrits", inscrits));
            participantsChart.getData().add(new PieChart.Data("Places restantes", restants));
        }

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
            } catch (Exception ex) {}
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
            case "Or": return "#FFD700";
            case "Argent": return "#C0C0C0";
            case "Bronze": return "#CD7F32";
            case "Platine": return "#E5E4E2";
            default: return "#8b5cf6";
        }
    }

    @FXML
    private void openRapportIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RapportIA.fxml"));
            Parent root = loader.load();

            RapportIAController controller = loader.getController();
            controller.setSujet(currentEvent.getTitre());

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
    private void goBackToShowEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowEvent.fxml"));
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

    @FXML
    private void openInGoogleMaps() {
        try {
            String address = locationLabel.getText().replace(" ", "+");
            String url = "https://www.google.com/maps/search/?api=1&query=" + address;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir Google Maps");
        }
    }

    private void initGallery() {
        if (galleryContainer == null) return;

        // Ajouter des images miniatures par défaut
        String[] images = {"event-default.jpg", "event-default.jpg", "event-default.jpg"};
        for (String img : images) {
            ImageView thumb = new ImageView();
            thumb.setFitWidth(100);
            thumb.setFitHeight(70);
            thumb.setPreserveRatio(true);
            thumb.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
            try {
                thumb.setImage(new Image(getClass().getResource("/assets/" + img).toExternalForm()));
            } catch (Exception e) {
                System.out.println("Image non trouvée: " + img);
            }
            galleryContainer.getChildren().add(thumb);
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
            } catch (Exception e) {}

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
    @FXML
    private void openMap() {
        try {
            Desktop.getDesktop().browse(new URI("https://maps.google.com/?q=Espace+Culturel+Tunis"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}