package Controller;

import Entites.Sponsor;
import Services.SponsorServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SponsorFrontController implements Initializable {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private StackPane heroStackPane;
    @FXML private ImageView heroImage;
    @FXML private StackPane floatingFlowerPane;
    @FXML private ImageView floatingFlowerImage;
    @FXML private FlowPane sponsorPane;

    private SponsorServices sponsorServices = new SponsorServices();
    private List<Sponsor> allSponsors;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger tous les sponsors
        allSponsors = sponsorServices.afficherSponsor();

        // Afficher les sponsors
        loadSponsors(allSponsors);

        // Configuration de la recherche
        setupSearch();

        // Animation de la fleur
        setupFlowerAnimation();

        // Binding hero image
        setupHeroImage();
    }

    private void setupHeroImage() {
        if (heroStackPane != null && heroImage != null) {
            heroImage.fitWidthProperty().bind(heroStackPane.widthProperty());
            heroImage.fitHeightProperty().bind(heroStackPane.heightProperty());
            heroImage.setPreserveRatio(false);
            heroImage.setSmooth(true);
        }
    }

    private void setupFlowerAnimation() {
        if (floatingFlowerImage != null) {
            TranslateTransition flowerAnimation = new TranslateTransition(Duration.seconds(3), floatingFlowerImage);
            flowerAnimation.setByY(-20);
            flowerAnimation.setAutoReverse(true);
            flowerAnimation.setCycleCount(TranslateTransition.INDEFINITE);
            flowerAnimation.play();
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> {
                List<Sponsor> filtered = sponsorServices.afficherSponsor().stream()
                        .filter(s -> s.getNom().toLowerCase().contains(newText.toLowerCase()) ||
                                s.getTypeSponsor().toLowerCase().contains(newText.toLowerCase()) ||
                                s.getEmail().toLowerCase().contains(newText.toLowerCase()))
                        .toList();
                loadSponsors(filtered);
            });
        }
    }

    private void loadSponsors(List<Sponsor> sponsors) {
        if (sponsorPane == null) return;

        sponsorPane.getChildren().clear();

        if (countLabel != null) {
            countLabel.setText(sponsors.size() + " Sponsors");
        }

        for (Sponsor s : sponsors) {
            VBox card = createSponsorCard(s);
            sponsorPane.getChildren().add(card);
        }
    }

    private VBox createSponsorCard(Sponsor s) {
        // =================== CARD SPONSOR ===================
        VBox card = new VBox(12);
        card.setPrefWidth(280);

// Add CSS class (this is how JavaFX uses your app.css)
        card.getStyleClass().add("sponsor-card");


        // =================== IMAGE / LOGO ===================
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle(
                "-fx-background-color: #ffe4e1;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(255,182,193,0.2), 10, 0, 0, 3);"
        );

        ImageView logoImage = new ImageView();
        logoImage.setFitWidth(260);
        logoImage.setFitHeight(140);
        logoImage.setPreserveRatio(false);
        logoImage.setStyle("-fx-background-radius: 15;");

        try {
            if (s.getImage() != null && !s.getImage().isEmpty()) {
                File imgFile = new File(s.getImage());
                if (imgFile.exists()) {
                    logoImage.setImage(new Image("file:" + s.getImage()));
                } else {
                    // Image par défaut
                    URL defaultImgUrl = getClass().getResource("/assets/sponsor-default.png");
                    if (defaultImgUrl != null) {
                        logoImage.setImage(new Image(defaultImgUrl.toString()));
                    }
                }
            } else {
                URL defaultImgUrl = getClass().getResource("/assets/sponsor-default.png");
                if (defaultImgUrl != null) {
                    logoImage.setImage(new Image(defaultImgUrl.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Clip arrondi
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 140);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        logoImage.setClip(clip);

        imageContainer.getChildren().add(logoImage);

        // =================== NOM ===================
        Label nameLabel = new Label(s.getNom());
        nameLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: #8b5cf6;"
        );
        nameLabel.setWrapText(true);

        // =================== TYPE BADGE ===================
        Label typeLabel = new Label(s.getTypeSponsor());
        typeLabel.setStyle(
                "-fx-background-color: #f3e8ff;" +
                        "-fx-text-fill: #8b5cf6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 15;" +
                        "-fx-background-radius: 20;"
        );

        // =================== DESCRIPTION ===================
        Label descLabel = new Label();
        String desc = s.getDescription();
        if (desc != null && desc.length() > 80) {
            desc = desc.substring(0, 80) + "...";
        }
        descLabel.setText(desc != null ? desc : "Partenaire officiel");
        descLabel.setWrapText(true);
        descLabel.setStyle(
                "-fx-text-fill: #4a5568;" +
                        "-fx-font-size: 14px;"
        );

        // =================== BOUTON ===================
        Button viewBtn = new Button("Voir partenariat →");
        viewBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #8b5cf6;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 8 0 0 0;" +
                        "-fx-cursor: hand;"
        );
        viewBtn.setOnAction(ev -> openSponsorDetails(s));

        // =================== ASSEMBLER LA CARTE ===================
        card.getChildren().addAll(imageContainer, nameLabel, typeLabel, descLabel, viewBtn);

        // =================== HOVER EFFECT ===================
        card.setOnMouseEntered(ev -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(ev -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });


        return card;
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void openSponsorDetails(Sponsor sponsor) {
        // TODO: Ouvrir une page de détails du sponsor
        System.out.println("Ouverture du profil de: " + sponsor.getNom());
        showAlert("Sponsor", "Détails de " + sponsor.getNom());
    }

}