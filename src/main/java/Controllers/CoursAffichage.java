package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Services.EvaluationServices;
import Utils.Navigation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

    @FXML
    private Label coursCountLabel;
    @FXML
    private FlowPane coursFlowPane;
    @FXML
    private TextField searchField;
    @FXML
    private Button academicButton, socialButton, autonomieButton, creativiteButton;
    @FXML
    private Button ajouterCoursButton;
    @FXML
    private Button ajouterEvaluationButton;

    private CoursServices coursServices;
    private EvaluationServices evaluationServices;
    private List<Cours> allCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();
        evaluationServices = new EvaluationServices();
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

        // Navigation pour le bouton Ajouter évaluation
        if (ajouterEvaluationButton != null) {
            ajouterEvaluationButton.setOnAction(event -> {
                System.out.println("📝 Navigation vers l'ajout d'évaluation...");
                Navigation.navigateTo("evaluationajout.fxml", "Ajouter des questions");
            });

            // Effet hover pour le bouton vert
            ajouterEvaluationButton.setOnMouseEntered(e ->
                    ajouterEvaluationButton.setStyle("-fx-background-color: #218838; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(33,136,56,0.4), 10, 0, 0, 5);")
            );

            ajouterEvaluationButton.setOnMouseExited(e ->
                    ajouterEvaluationButton.setStyle("-fx-background-color: #28A745; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(40,167,69,0.3), 10, 0, 0, 5);")
            );
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
        card.setPrefHeight(380);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: #EEEEEE; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 0 0 20 0; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-padding: 0 0 20 0; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.1), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #EEEEEE; -fx-border-width: 1; -fx-padding: 0 0 20 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"));

        // Image du cours
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #F5F0FF, #FFFFFF); " +
                "-fx-background-radius: 15 15 0 0;");

        ImageView courseImageView = new ImageView();
        courseImageView.setFitHeight(100);
        courseImageView.setFitWidth(200);
        courseImageView.setPreserveRatio(true);

        // Charger l'image du cours si disponible
        if (cours.getImage() != null && !cours.getImage().isEmpty()) {
            try {
                String imagePath = "/images/" + cours.getImage();
                URL imageResource = getClass().getResource(imagePath);
                if (imageResource != null) {
                    Image img = new Image(imageResource.toExternalForm());
                    courseImageView.setImage(img);
                } else {
                    setDefaultCourseImage(courseImageView, cours);
                }
            } catch (Exception e) {
                setDefaultCourseImage(courseImageView, cours);
            }
        } else {
            setDefaultCourseImage(courseImageView, cours);
        }

        imageContainer.getChildren().add(courseImageView);

        // Conteneur pour le contenu texte (avec padding)
        VBox contentContainer = new VBox(8);
        contentContainer.setStyle("-fx-padding: 0 20 0 20;");

        // Titre du cours
        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");
        titleLabel.setWrapText(true);

        // Description
        Label descriptionLabel = new Label(cours.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        descriptionLabel.setPrefHeight(40);
        descriptionLabel.setMaxHeight(40);

        // BADGES POUR TYPE ET NIVEAU
        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        // Badge pour le type de cours
        Label typeBadge = new Label(cours.getType_cours());
        typeBadge.setStyle(getTypeStyle(cours.getType_cours()));
        typeBadge.setPadding(new Insets(4, 12, 4, 12));

        // Badge pour le niveau
        Label niveauBadge = new Label(cours.getNiveau());
        niveauBadge.setStyle(getNiveauStyle(cours.getNiveau()));
        niveauBadge.setPadding(new Insets(4, 12, 4, 12));

        badgesBox.getChildren().addAll(typeBadge, niveauBadge);

        // Footer avec évaluations
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label starLabel = new Label("⭐");
        Label ratingLabel = new Label("4.8");
        ratingLabel.setStyle("-fx-text-fill: #7B2FF7; -fx-font-weight: bold;");
        ratingBox.getChildren().addAll(starLabel, ratingLabel);

        HBox participantsBox = new HBox(5);
        participantsBox.setAlignment(Pos.CENTER_LEFT);
        Label usersLabel = new Label("👥");
        Label participantsLabel = new Label("124");
        participantsLabel.setStyle("-fx-text-fill: #7B2FF7; -fx-font-weight: bold;");
        participantsBox.getChildren().addAll(usersLabel, participantsLabel);

        footer.getChildren().addAll(ratingBox, participantsBox);

        // Bouton Commencer
        Button commencerBtn = new Button("Commencer");
        commencerBtn.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; " +
                "-fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 5, 0, 0, 2);");

        commencerBtn.setOnMouseEntered(e ->
                commencerBtn.setStyle("-fx-background-color: #6A1FF7; -fx-text-fill: white; " +
                        "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.5), 8, 0, 0, 3);")
        );

        commencerBtn.setOnMouseExited(e ->
                commencerBtn.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; " +
                        "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; " +
                        "-fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 5, 0, 0, 2);")
        );

        commencerBtn.setOnAction(e -> {
            System.out.println("🎓 Début du cours: " + cours.getTitre());
            afficherContenuSimplifie(cours);
        });

        // Ajouter tous les éléments au contentContainer
        contentContainer.getChildren().addAll(titleLabel, descriptionLabel, badgesBox, footer);
        card.getChildren().addAll(imageContainer, contentContainer, commencerBtn);

        // Aligner le bouton au centre
        VBox.setMargin(commencerBtn, new Insets(0, 20, 10, 20));

        return card;
    }

    // Styles pour les badges de type
    private String getTypeStyle(String type) {
        if (type == null) return getDefaultBadgeStyle();

        switch (type) {
            case "Académique":
                return "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Social":
                return "-fx-background-color: #FCE4EC; -fx-text-fill: #C2185B; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Autonomie":
                return "-fx-background-color: #E8F5E8; -fx-text-fill: #2E7D32; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Créativité":
                return "-fx-background-color: #FFF3E0; -fx-text-fill: #F57C00; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            default:
                return getDefaultBadgeStyle();
        }
    }

    // Styles pour les badges de niveau
    private String getNiveauStyle(String niveau) {
        if (niveau == null) return getDefaultBadgeStyle();

        switch (niveau) {
            case "Débutant":
                return "-fx-background-color: #E8F5E8; -fx-text-fill: #2E7D32; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Intermédiaire":
                return "-fx-background-color: #FFF8E1; -fx-text-fill: #F57C00; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Avancé":
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
            default:
                return getDefaultBadgeStyle();
        }
    }

    private String getDefaultBadgeStyle() {
        return "-fx-background-color: #F5F5F5; -fx-text-fill: #666666; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;";
    }

    // Méthode utilitaire pour définir une image par défaut
    private void setDefaultCourseImage(ImageView imageView, Cours cours) {
        String titre = cours.getTitre().toLowerCase();
        String defaultImage;

        if (titre.contains("math") || titre.contains("chiffre")) {
            defaultImage = "math_default.png";
        } else if (titre.contains("social") || titre.contains("interaction")) {
            defaultImage = "social_default.png";
        } else if (titre.contains("francais") || titre.contains("langue")) {
            defaultImage = "language_default.png";
        } else if (titre.contains("nature")) {
            defaultImage = "nature_default.png";
        } else if (titre.contains("animal")) {
            defaultImage = "animal_default.png";
        } else if (titre.contains("art") || titre.contains("dessin")) {
            defaultImage = "art_default.png";
        } else {
            defaultImage = "course_default.png";
        }

        try {
            String imagePath = "/images/" + defaultImage;
            URL imageResource = getClass().getResource(imagePath);
            if (imageResource != null) {
                Image img = new Image(imageResource.toExternalForm());
                imageView.setImage(img);
            } else {
                imageView.setImage(null);
                StackPane parent = (StackPane) imageView.getParent();
                if (parent != null) {
                    Label emojiLabel = new Label(getEmojiForCours(cours));
                    emojiLabel.setStyle("-fx-font-size: 50px;");
                    parent.getChildren().clear();
                    parent.getChildren().add(emojiLabel);
                }
            }
        } catch (Exception e) {
            imageView.setImage(null);
        }
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
        headerBox.setAlignment(Pos.CENTER);
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
            Label emptyLabel = new Label("Aucun contenu");
            emptyLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #999;");
            motsPane.getChildren().add(emptyLabel);
        }

        motsSection.getChildren().add(motsPane);

        // SECTION ÉVALUATION - UNIQUEMENT LE BOUTON PASSER LE QUIZ
        VBox evaluationSection = new VBox(15);
        evaluationSection.setMaxWidth(1200);
        evaluationSection.setAlignment(Pos.CENTER);
        evaluationSection.setStyle("-fx-padding: 40 0 20 0;");

        Label evaluationTitle = new Label("📋 Évaluation du cours");
        evaluationTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        // Compter les questions disponibles
        int questionCount = evaluationServices.compterParCours(cours.getId_cours());

        // Bouton Passer le quiz
        Button passerQuizBtn = new Button("🎯 Passer le quiz");
        passerQuizBtn.setStyle(
                "-fx-background-color: #7B2FF7;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 20 50;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);"
        );

        // Effet hover
        passerQuizBtn.setOnMouseEntered(e ->
                passerQuizBtn.setStyle(
                        "-fx-background-color: #6A1FF7;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 30;" +
                                "-fx-padding: 20 50;" +
                                "-fx-font-size: 20px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.5), 15, 0, 0, 8);"
                )
        );

        passerQuizBtn.setOnMouseExited(e ->
                passerQuizBtn.setStyle(
                        "-fx-background-color: #7B2FF7;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 30;" +
                                "-fx-padding: 20 50;" +
                                "-fx-font-size: 20px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);"
                )
        );

        // Action pour le quiz - CORRIGÉ
        passerQuizBtn.setOnAction(e -> {
            contentStage.close();
            System.out.println("🎯 Navigation vers le quiz pour le cours ID: " + cours.getId_cours());
            Navigation.navigateTo("passerevaluation.fxml?coursId=" + cours.getId_cours(),
                    "Quiz - " + cours.getTitre());
        });

        // Texte descriptif
        String questionText = questionCount + " question(s) disponible(s)";
        if (questionCount > 0) {
            int totalScore = evaluationServices.getScoreTotalParCours(cours.getId_cours());
            questionText += " - Score total: " + totalScore + " points";
        }

        Label evaluationHint = new Label(questionText);
        evaluationHint.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-padding: 10 0 0;");

        if (questionCount == 0) {
            Label noQuestionsLabel = new Label("Aucune question disponible pour ce cours pour le moment.");
            noQuestionsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999999; -fx-padding: 20;");
            evaluationSection.getChildren().addAll(evaluationTitle, noQuestionsLabel);
        } else {
            evaluationSection.getChildren().addAll(evaluationTitle, passerQuizBtn, evaluationHint);
        }

        // Ajouter toutes les sections au conteneur principal
        mainContainer.getChildren().addAll(headerBox, descriptionCard, motsSection, evaluationSection);
        scrollPane.setContent(mainContainer);

        Scene scene = new Scene(scrollPane, 1300, 800);
        contentStage.setScene(scene);
        contentStage.show();
    }

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

                            imageView.setOnMouseEntered(ev ->
                                    imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.5), 10, 0, 0, 5);")
                            );
                            imageView.setOnMouseExited(ev ->
                                    imageView.setStyle("")
                            );

                            imagesContainer.getChildren().add(imageView);
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs
                    }
                }
            }
        }

        if (imagesContainer.getChildren().isEmpty()) {
            Label emojiLabel = new Label(getEmojiForMot(mot));
            emojiLabel.setStyle("-fx-font-size: 80px;");
            imagesContainer.getChildren().add(emojiLabel);
        }

        imagesScroll.setContent(imagesContainer);

        Label motLabel = new Label(mot);
        motLabel.setWrapText(true);
        motLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-text-alignment: center;"
        );

        if (imageUrls != null && imageUrls.length > 1) {
            Label countLabel = new Label("📸 " + imageUrls.length + " images");
            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7B2FF7; -fx-font-weight: bold;");
            card.getChildren().addAll(imagesScroll, motLabel, countLabel);
        } else {
            card.getChildren().addAll(imagesScroll, motLabel);
        }

        return card;
    }

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
        academicButton.setOnAction(e -> {
            if (isButtonActive(academicButton)) {
                resetFilters();
            } else {
                filterCours(null, "Académique");
            }
        });

        socialButton.setOnAction(e -> {
            if (isButtonActive(socialButton)) {
                resetFilters();
            } else {
                filterCours(null, "Social");
            }
        });

        autonomieButton.setOnAction(e -> {
            if (isButtonActive(autonomieButton)) {
                resetFilters();
            } else {
                filterCours(null, "Autonomie");
            }
        });

        creativiteButton.setOnAction(e -> {
            if (isButtonActive(creativiteButton)) {
                resetFilters();
            } else {
                filterCours(null, "Créativité");
            }
        });
    }

    private boolean isButtonActive(Button button) {
        return button.getStyle().contains("#7B2FF7") && !button.getStyle().contains("transparent");
    }

    private void filterCours(String searchText, String category) {
        List<Cours> filtered = allCours;

        if (searchText != null && !searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(c -> c.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            c.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            final String categoryFilter = category;
            filtered = filtered.stream()
                    .filter(c -> {
                        String typeCours = c.getType_cours();
                        return typeCours != null && typeCours.equals(categoryFilter);
                    })
                    .collect(java.util.stream.Collectors.toList());

            updateFilterButtons(category);
        } else {
            // Réinitialiser les boutons si pas de catégorie
            resetFilterButtons();
        }

        coursCountLabel.setText(filtered.size() + " Cours disponibles");
        displayCours(filtered);
    }

    private void updateFilterButtons(String activeCategory) {
        // Réinitialiser tous les boutons au style inactif d'abord
        resetFilterButtons();

        // Mettre en évidence le bouton actif
        switch (activeCategory) {
            case "Académique":
                setButtonActive(academicButton);
                break;
            case "Social":
                setButtonActive(socialButton);
                break;
            case "Autonomie":
                setButtonActive(autonomieButton);
                break;
            case "Créativité":
                setButtonActive(creativiteButton);
                break;
        }
    }

    private void setButtonActive(Button button) {
        button.setStyle("-fx-background-color: #7B2FF7; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;");
    }

    private void resetFilterButtons() {
        Button[] buttons = {academicButton, socialButton, autonomieButton, creativiteButton};
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: #7B2FF7; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-text-fill: #7B2FF7; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-padding: 8 20; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;");
        }
    }

    private void resetFilters() {
        displayCours(allCours);
        coursCountLabel.setText(allCours.size() + " Cours disponibles");
        searchField.clear();
        resetFilterButtons();
    }
}