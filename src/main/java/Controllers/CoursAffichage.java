package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Utils.Navigation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CoursAffichage implements Initializable {

    @FXML private Label coursCountLabel;
    @FXML private FlowPane coursFlowPane;
    @FXML private TextField searchField;
    @FXML private Button academicButton, socialButton, autonomieButton, creativiteButton;
    @FXML private Button ajouterCoursButton;

    private CoursServices coursServices;
    private List<Cours> allCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();
        loadCours();
        setupSearch();
        setupFilters();
        setupNavigation();
    }

    private void setupNavigation() {
        if (ajouterCoursButton != null) {
            ajouterCoursButton.setOnAction(event -> {
                System.out.println("➕ Navigation vers l'ajout de cours...");
                Navigation.navigateTo("coursajout.fxml", "Ajouter un cours");
            });
        }
    }

    private void loadCours() {
        allCours = coursServices.getAll();
        coursCountLabel.setText(allCours.size() + " Cours disponibles");
        displayCours(allCours);
    }

    private void displayCours(List<Cours> coursList) {
        coursFlowPane.getChildren().clear();

        for (Cours cours : coursList) {
            VBox courseCard = createCourseCard(cours);
            coursFlowPane.getChildren().add(courseCard);
        }
    }

    private VBox createCourseCard(Cours cours) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPrefHeight(280);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: #EEEEEE; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.1), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #EEEEEE; -fx-border-width: 1; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"));

        Label emojiLabel = new Label(getEmojiForCours(cours));
        emojiLabel.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label descriptionLabel = new Label(cours.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        descriptionLabel.setPrefHeight(40);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.getChildren().addAll(new Label("⭐"), new Label("4.8"));

        HBox participantsBox = new HBox(5);
        participantsBox.setAlignment(Pos.CENTER_LEFT);
        participantsBox.getChildren().addAll(new Label("👥"), new Label("124"));

        footer.getChildren().addAll(ratingBox, participantsBox);

        Button commencerBtn = new Button("Commencer");
        commencerBtn.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");

        commencerBtn.setOnAction(e -> {
            System.out.println("🎓 Début du cours: " + cours.getTitre());
            afficherContenuSimplifie(cours);
        });

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(emojiLabel);

        card.getChildren().addAll(headerBox, titleLabel, descriptionLabel, footer, commencerBtn);
        return card;
    }

    private void afficherContenuSimplifie(Cours cours) {
        Stage contentStage = new Stage();
        contentStage.setTitle(cours.getTitre());
        contentStage.initModality(Modality.APPLICATION_MODAL);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F0F8FF; -fx-border-width: 0;");

        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #F0F8FF; -fx-padding: 40;");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // En-tête avec bouton retour
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setMaxWidth(1200);

        Button backButton = new Button("← Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 18px; -fx-cursor: hand;");
        backButton.setOnAction(e -> contentStage.close());

        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backButton, spacer, titleLabel);

        // Carte de description
        VBox descriptionCard = new VBox(15);
        descriptionCard.setMaxWidth(1200);
        descriptionCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: #7B2FF7;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.1), 10, 0, 0, 5);"
        );

        Label descriptionTitle = new Label("📝 Description du cours");
        descriptionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        Label descriptionContent = new Label(cours.getDescription());
        descriptionContent.setWrapText(true);
        descriptionContent.setStyle("-fx-font-size: 18px; -fx-text-fill: #444444; -fx-line-spacing: 5;");

        descriptionCard.getChildren().addAll(descriptionTitle, descriptionContent);

        // Section des mots
        VBox motsSection = new VBox(15);
        motsSection.setMaxWidth(1200);

        Label motsTitle = new Label("🎯 Mots à apprendre");
        motsTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7; -fx-padding: 10 0;");
        motsSection.getChildren().add(motsTitle);

        FlowPane motsPane = new FlowPane();
        motsPane.setHgap(25);
        motsPane.setVgap(25);
        motsPane.setAlignment(Pos.CENTER);
        motsPane.setMaxWidth(1200);

        if (cours.getMots() != null && !cours.getMots().isEmpty()) {
            String[] motsList = cours.getMots().split(";");
            String[] imagesList = cours.getImages_mots() != null ? cours.getImages_mots().split(";") : new String[0];

            for (int i = 0; i < motsList.length; i++) {
                String mot = motsList[i].trim();
                String image = (i < imagesList.length) ? imagesList[i].trim() : null;

                // Gestion des images multiples pour un même mot
                if (image != null && image.contains(";")) {
                    String[] multipleImages = image.split(";");
                    VBox motAvecMultiImages = createMotCardAvecMultiImages(mot, multipleImages);
                    motsPane.getChildren().add(motAvecMultiImages);
                } else {
                    VBox motCard = createMotCard(mot, image);
                    motsPane.getChildren().add(motCard);
                }
            }
        }

        if (motsPane.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Aucun mot disponible pour ce cours");
            emptyLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #999;");
            motsPane.getChildren().add(emptyLabel);
        }

        motsSection.getChildren().add(motsPane);

        // ========== NOUVELLE SECTION : ÉVALUATIONS ==========
        VBox evaluationsSection = new VBox(15);
        evaluationsSection.setMaxWidth(1200);

        Label evaluationsTitle = new Label("📊 Évaluations du cours");
        evaluationsTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7; -fx-padding: 10 0;");

        // Bouton pour voir les évaluations
        Button voirEvaluationsBtn = new Button("Voir les évaluations");
        voirEvaluationsBtn.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 25; -fx-font-weight: bold; -fx-cursor: hand;");

        voirEvaluationsBtn.setOnAction(e -> {
            // Fermer la fenêtre actuelle et ouvrir la page des évaluations pour ce cours
            contentStage.close();
            Navigation.navigateTo("evaluationaffichage.fxml?coursId=" + cours.getId_cours(),
                    "Évaluations - " + cours.getTitre());
        });

        evaluationsSection.getChildren().addAll(evaluationsTitle, voirEvaluationsBtn);

        // Ajouter toutes les sections au conteneur principal
        mainContainer.getChildren().addAll(headerBox, descriptionCard, motsSection, evaluationsSection);
        scrollPane.setContent(mainContainer);

        Scene scene = new Scene(scrollPane, 1300, 800);
        contentStage.setScene(scene);
        contentStage.show();
    }
    // Créer une carte avec plusieurs images (scrollable horizontal)
    private VBox createMotCardAvecMultiImages(String mot, String[] imageUrls) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(300);
        card.setPrefHeight(350);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-color: #7B2FF7;" +
                        "-fx-border-width: 3;" +
                        "-fx-padding: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.2), 10, 0, 0, 5);"
        );

        // Effet hover
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: #F0E6FF;" +
                                "-fx-background-radius: 30;" +
                                "-fx-border-radius: 30;" +
                                "-fx-border-color: #7B2FF7;" +
                                "-fx-border-width: 4;" +
                                "-fx-padding: 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.4), 20, 0, 0, 10);"
                )
        );

        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 30;" +
                                "-fx-border-radius: 30;" +
                                "-fx-border-color: #7B2FF7;" +
                                "-fx-border-width: 3;" +
                                "-fx-padding: 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.2), 10, 0, 0, 5);"
                )
        );

        // Conteneur pour les images (scrollable horizontal)
        ScrollPane imagesScroll = new ScrollPane();
        imagesScroll.setFitToHeight(true);
        imagesScroll.setPrefHeight(150);
        imagesScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-width: 0;");

        HBox imagesContainer = new HBox(10);
        imagesContainer.setAlignment(Pos.CENTER);
        imagesContainer.setStyle("-fx-padding: 10;");

        if (imageUrls != null && imageUrls.length > 0) {
            for (String imageUrl : imageUrls) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    try {
                        String imagePath = "/images/" + imageUrl.trim();
                        URL imageResource = getClass().getResource(imagePath);

                        if (imageResource != null) {
                            Image img = new Image(imageResource.toExternalForm());
                            ImageView imageView = new ImageView(img);
                            imageView.setFitHeight(100);
                            imageView.setFitWidth(100);
                            imageView.setPreserveRatio(true);

                            // Ajouter un effet de survol pour chaque image
                            imageView.setOnMouseEntered(ev ->
                                    imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.5), 10, 0, 0, 5);")
                            );
                            imageView.setOnMouseExited(ev ->
                                    imageView.setStyle("")
                            );

                            imagesContainer.getChildren().add(imageView);
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs de chargement d'image
                    }
                }
            }
        }

        // Si aucune image n'a été chargée, afficher un emoji
        if (imagesContainer.getChildren().isEmpty()) {
            Label emojiLabel = new Label(getEmojiForMot(mot));
            emojiLabel.setStyle("-fx-font-size: 80px;");
            imagesContainer.getChildren().add(emojiLabel);
        }

        imagesScroll.setContent(imagesContainer);

        // Texte du mot
        Label motLabel = new Label(mot);
        motLabel.setWrapText(true);
        motLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-text-alignment: center;"
        );

        // Indicateur du nombre d'images
        if (imageUrls != null && imageUrls.length > 1) {
            Label countLabel = new Label("📸 " + imageUrls.length + " images");
            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7B2FF7; -fx-font-weight: bold;");
            card.getChildren().addAll(imagesScroll, motLabel, countLabel);
        } else {
            card.getChildren().addAll(imagesScroll, motLabel);
        }

        return card;
    }

    // Créer une carte pour chaque mot (version simple)
    private VBox createMotCard(String mot, String imageUrl) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(250);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-color: #7B2FF7;" +
                        "-fx-border-width: 3;" +
                        "-fx-padding: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.2), 10, 0, 0, 5);"
        );

        // Effet hover
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: #F0E6FF;" +
                                "-fx-background-radius: 30;" +
                                "-fx-border-radius: 30;" +
                                "-fx-border-color: #7B2FF7;" +
                                "-fx-border-width: 4;" +
                                "-fx-padding: 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.4), 20, 0, 0, 10);"
                )
        );

        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 30;" +
                                "-fx-border-radius: 30;" +
                                "-fx-border-color: #7B2FF7;" +
                                "-fx-border-width: 3;" +
                                "-fx-padding: 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.2), 10, 0, 0, 5);"
                )
        );

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(120);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                String imagePath = "/images/" + imageUrl;
                URL imageResource = getClass().getResource(imagePath);

                if (imageResource != null) {
                    Image img = new Image(imageResource.toExternalForm());
                    ImageView imageView = new ImageView(img);
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    imageContainer.getChildren().add(imageView);
                } else {
                    Label emojiLabel = new Label(getEmojiForMot(mot));
                    emojiLabel.setStyle("-fx-font-size: 60px;");
                    imageContainer.getChildren().add(emojiLabel);
                }
            } catch (Exception e) {
                Label emojiLabel = new Label(getEmojiForMot(mot));
                emojiLabel.setStyle("-fx-font-size: 60px;");
                imageContainer.getChildren().add(emojiLabel);
            }
        } else {
            Label emojiLabel = new Label(getEmojiForMot(mot));
            emojiLabel.setStyle("-fx-font-size: 60px;");
            imageContainer.getChildren().add(emojiLabel);
        }

        // Texte du mot
        Label motLabel = new Label(mot);
        motLabel.setWrapText(true);
        motLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-text-alignment: center;"
        );

        card.getChildren().addAll(imageContainer, motLabel);
        return card;
    }

    // Emoji pour les mots
    private String getEmojiForMot(String mot) {
        mot = mot.toLowerCase();
        if (mot.contains("chocolat")) return "🍫";
        if (mot.contains("toilette")) return "🚽";
        if (mot.contains("peluche")) return "🧸";
        if (mot.contains("ballon")) return "⚽";
        if (mot.contains("verre")) return "🥛";
        if (mot.contains("eau")) return "💧";
        if (mot.contains("école")) return "🏫";
        if (mot.contains("télévision") || mot.contains("tv")) return "📺";
        if (mot.contains("livre")) return "📚";
        return "📌";
    }

    private String getEmojiForCours(Cours cours) {
        String titre = cours.getTitre().toLowerCase();
        if (titre.contains("math") || titre.contains("chiffre")) return "🧮";
        else if (titre.contains("social") || titre.contains("interaction")) return "💬";
        else if (titre.contains("francais") || titre.contains("langue")) return "📚";
        else if (titre.contains("nature")) return "🌿";
        else if (titre.contains("animal")) return "🐶";
        else if (titre.contains("art") || titre.contains("dessin")) return "🎨";
        else return "📘";
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCours(newValue, null);
        });
    }

    private void setupFilters() {
        academicButton.setOnAction(e -> filterCours(null, "Académique"));
        socialButton.setOnAction(e -> filterCours(null, "Social"));
        autonomieButton.setOnAction(e -> filterCours(null, "Autonomie"));
        creativiteButton.setOnAction(e -> filterCours(null, "Créativité"));
    }

    private void filterCours(String searchText, String category) {
        List<Cours> filtered = allCours;

        if (searchText != null && !searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(c -> c.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            c.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (category != null) {
            filtered = filtered.stream()
                    .filter(c -> c.getType_cours().equals(category) ||
                            c.getNiveau().contains(category))
                    .collect(java.util.stream.Collectors.toList());
        }

        coursCountLabel.setText(filtered.size() + " Cours disponibles");
        displayCours(filtered);
    }
}