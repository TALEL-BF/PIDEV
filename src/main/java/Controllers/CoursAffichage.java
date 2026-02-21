package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Services.EvaluationServices;
import Services.StabilityImageService; // AJOUT
import Utils.Navigation;
import Utils.TextToSpeechManager;
import javafx.fxml.FXML;
import javafx.animation.RotateTransition;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.io.ByteArrayInputStream; // AJOUT

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
    @FXML
    private Button drawingSpaceButton;
    @FXML
    private Button iaImageButton;

    private CoursServices coursServices;
    private EvaluationServices evaluationServices;
    private StabilityImageService stabilityImageService; // REMPLACE FalImageService
    private List<Cours> allCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();
        evaluationServices = new EvaluationServices();

        // Initialisation de Stability AI avec votre clé API
        remplacer
        stabilityImageService = new StabilityImageService(stabilityApiKey);

        loadCours();
        setupSearch();
        setupFilters();
        setupNavigation();
        setupDrawingSpace();
        setupIAImageButton();
    }

    private void setupIAImageButton() {
        if (iaImageButton != null) {
            iaImageButton.setOnAction(event -> {
                System.out.println("🤖 Ouverture du générateur d'images IA...");
                ouvrirGenerateurImageIA();
            });

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), iaImageButton);

            iaImageButton.setOnMouseEntered(e -> {
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.play();
                iaImageButton.setStyle("-fx-background-color: #FF6B6B; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255,107,107,0.4), 10, 0, 0, 5);");
            });

            iaImageButton.setOnMouseExited(e -> {
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
                iaImageButton.setStyle("-fx-background-color: #FF4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255,68,68,0.3), 10, 0, 0, 5);");
            });
        }
    }

    private void ouvrirGenerateurImageIA() {
        Stage imageStage = new Stage();
        imageStage.setTitle("🤖 Générateur d'images par IA");
        imageStage.initModality(Modality.APPLICATION_MODAL);

        // ================= CONTENEUR PRINCIPAL =================
        HBox mainContainer = new HBox();
        mainContainer.setPrefWidth(1300);
        mainContainer.setPrefHeight(800);

        // ================= SIDEBAR IDENTIQUE =================
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(260.0);
        sidebar.setMinWidth(260.0);
        sidebar.setMaxWidth(260.0);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #7B2FF7, #5F0FFF); -fx-padding: 25 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Logo
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-padding: 0 0 15 5;");

        ImageView logoImage = new ImageView();
        logoImage.setFitHeight(40.0);
        logoImage.setFitWidth(40.0);
        logoImage.setPreserveRatio(true);
        try {
            Image img = new Image(getClass().getResource("/images/logo.png").toExternalForm());
            logoImage.setImage(img);
        } catch (Exception e) {
            Label logoEmoji = new Label("🎨");
            logoEmoji.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
            logoBox.getChildren().add(logoEmoji);
        }

        VBox logoText = new VBox(2);
        Label autiCareLabel = new Label("AutiCare");
        autiCareLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label espaceLabel = new Label("Espace éducatif");
        espaceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        logoText.getChildren().addAll(autiCareLabel, espaceLabel);

        logoBox.getChildren().addAll(logoImage, logoText);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-pref-height: 1;");

        // Menu de navigation
        VBox menuBox = new VBox(8);
        menuBox.setStyle("-fx-padding: 10 0 20 0;");

        Button coursButton = new Button("📚 Nos cours");
        coursButton.setMaxWidth(Double.MAX_VALUE);
        coursButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 10;");
        coursButton.setOnAction(e -> {
            System.out.println("📚 Retour à l'affichage des cours");
            imageStage.close();
        });

        Button emploisButton = new Button("📅 Emplois du temps");
        emploisButton.setMaxWidth(Double.MAX_VALUE);
        emploisButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        Button evenementsButton = new Button("🎉 Événements");
        evenementsButton.setMaxWidth(Double.MAX_VALUE);
        evenementsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        Button suiviButton = new Button("📞 Suivi consultation");
        suiviButton.setMaxWidth(Double.MAX_VALUE);
        suiviButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10; -fx-wrap-text: true;");

        Button jeuxButton = new Button("🎮 Jeux éducatifs");
        jeuxButton.setMaxWidth(Double.MAX_VALUE);
        jeuxButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        menuBox.getChildren().addAll(coursButton, emploisButton, evenementsButton, suiviButton, jeuxButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button deconnexionButton = new Button("🚪 Déconnexion");
        deconnexionButton.setMaxWidth(Double.MAX_VALUE);
        deconnexionButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");

        sidebar.getChildren().addAll(logoBox, separator, menuBox, spacer, deconnexionButton);

        // ================= CONTENU PRINCIPAL AVEC CENTRAGE =================
        ScrollPane mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setStyle("-fx-background: white; -fx-border-width: 0;");
        HBox.setHgrow(mainScrollPane, Priority.ALWAYS);

        // Conteneur pour centrer le contenu
        StackPane centerWrapper = new StackPane();
        centerWrapper.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F8FF, #E8F0FE);");

        VBox contentContainer = new VBox(25);
        contentContainer.setMaxWidth(900);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(40));

        // ========== EN-TÊTE ==========
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setMaxWidth(900);

        Button backButton = new Button("← Retour aux cours");
        backButton.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        backButton.setOnAction(e -> imageStage.close());

        // Animation du bouton retour
        ScaleTransition backScale = new ScaleTransition(Duration.millis(200), backButton);
        backButton.setOnMouseEntered(e -> {
            backScale.setToX(1.05);
            backScale.setToY(1.05);
            backScale.play();
            backButton.setStyle("-fx-background-color: #6A1FF7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(106,31,247,0.4), 10, 0, 0, 5);");
        });

        backButton.setOnMouseExited(e -> {
            backScale.setToX(1.0);
            backScale.setToY(1.0);
            backScale.play();
            backButton.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        });

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label titleLabel = new Label("🎨 Générateur d'images magique");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        headerBox.getChildren().addAll(backButton, headerSpacer, titleLabel);

        // Sous-titre
        Label subtitleLabel = new Label("Décris ce que tu veux voir, l'IA va le créer pour toi !");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666; -fx-padding: 0 0 20 0;");
        subtitleLabel.setAlignment(Pos.CENTER);

        // ========== CARTE PRINCIPALE CENTRÉE ==========
        VBox mainCard = new VBox(25);
        mainCard.setMaxWidth(800);
        mainCard.setAlignment(Pos.CENTER);
        mainCard.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #7B2FF7; -fx-border-width: 3; -fx-border-radius: 25; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.2), 15, 0, 0, 5);");

        // Zone de texte pour le prompt
        VBox promptBox = new VBox(10);
        promptBox.setAlignment(Pos.CENTER);

        Label promptLabel = new Label("📝 Décris l'image à générer :");
        promptLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        TextArea promptArea = new TextArea();
        promptArea.setPromptText("Exemple: un oiseau bleu qui vole dans le ciel, style dessin animé pour enfants");
        promptArea.setWrapText(true);
        promptArea.setPrefRowCount(3);
        promptArea.setMaxWidth(700);
        promptArea.setStyle("-fx-background-radius: 15; -fx-border-radius: 15; -fx-font-size: 15px; -fx-padding: 12;");

        promptBox.getChildren().addAll(promptLabel, promptArea);

        // Bouton générer centré
        HBox buttonWrapper = new HBox();
        buttonWrapper.setAlignment(Pos.CENTER);

        Button genererButton = new Button("✨ Générer l'image");
        genererButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 15 40; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);");

        // Animation du bouton générer
        ScaleTransition genererScale = new ScaleTransition(Duration.millis(200), genererButton);
        genererButton.setOnMouseEntered(e -> {
            genererScale.setToX(1.05);
            genererScale.setToY(1.05);
            genererScale.play();
            genererButton.setStyle("-fx-background-color: #6A1FF7; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 15 40; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(106,31,247,0.5), 15, 0, 0, 8);");
        });

        genererButton.setOnMouseExited(e -> {
            genererScale.setToX(1.0);
            genererScale.setToY(1.0);
            genererScale.play();
            genererButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 15 40; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);");
        });

        buttonWrapper.getChildren().add(genererButton);

        // Indicateur de chargement centré
        HBox progressWrapper = new HBox();
        progressWrapper.setAlignment(Pos.CENTER);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(60, 60);

        progressWrapper.getChildren().add(progressIndicator);

        // Zone d'affichage de l'image centrée
        VBox imageBox = new VBox(15);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 20; -fx-background-radius: 20; -fx-border-color: #7B2FF7; -fx-border-width: 2; -fx-border-radius: 20;");
        imageBox.setPrefHeight(350);
        imageBox.setMaxWidth(700);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(280);
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);
        imageView.setVisible(false);

        Label placeholderLabel = new Label("L'image générée apparaîtra ici");
        placeholderLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #999999;");

        imageBox.getChildren().addAll(placeholderLabel, imageView);

        // Boutons d'action centrés
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setVisible(false);

        Button saveButton = new Button("💾 Sauvegarder l'image");
        saveButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 30; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;");

        Button newButton = new Button("🔄 Nouvelle image");
        newButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 30; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;");

        actionButtons.getChildren().addAll(saveButton, newButton);

        // Label d'erreur centré
        HBox errorWrapper = new HBox();
        errorWrapper.setAlignment(Pos.CENTER);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 15px; -fx-padding: 10;");
        errorLabel.setVisible(false);

        errorWrapper.getChildren().add(errorLabel);

        // Assemblage de la carte
        mainCard.getChildren().addAll(promptBox, buttonWrapper, progressWrapper, imageBox, actionButtons, errorWrapper);

        // Assemblage du contenu
        contentContainer.getChildren().addAll(headerBox, subtitleLabel, mainCard);
        centerWrapper.getChildren().add(contentContainer);
        mainScrollPane.setContent(centerWrapper);

        // ========== LOGIQUE DE GÉNÉRATION ==========
        genererButton.setOnAction(e -> {
            String prompt = promptArea.getText().trim();
            if (prompt.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez décrire l'image à générer");
                return;
            }

            genererButton.setDisable(true);
            progressIndicator.setVisible(true);
            placeholderLabel.setVisible(false);
            imageView.setVisible(false);
            actionButtons.setVisible(false);
            errorLabel.setVisible(false);

            new Thread(() -> {
                try {
                    System.out.println("🤖 Génération d'image Stability AI pour: " + prompt);
                    byte[] imageBytes = stabilityImageService.genererImage(prompt);

                    javafx.application.Platform.runLater(() -> {
                        if (imageBytes != null && imageBytes.length > 0) {
                            Image image = new Image(new ByteArrayInputStream(imageBytes));
                            imageView.setImage(image);
                            imageView.setVisible(true);
                            actionButtons.setVisible(true);

                            saveButton.setOnAction(saveEvent -> {
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setTitle("Sauvegarder l'image");
                                fileChooser.getExtensionFilters().add(
                                        new FileChooser.ExtensionFilter("Images PNG", "*.png")
                                );
                                File file = fileChooser.showSaveDialog(imageStage);

                                if (file != null) {
                                    try {
                                        java.nio.file.Files.write(file.toPath(), imageBytes);
                                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Image sauvegardée avec succès !");
                                    } catch (IOException ex) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + ex.getMessage());
                                    }
                                }
                            });

                            newButton.setOnAction(newEvent -> {
                                promptArea.clear();
                                imageView.setImage(null);
                                imageView.setVisible(false);
                                placeholderLabel.setVisible(true);
                                actionButtons.setVisible(false);
                            });

                        } else {
                            errorLabel.setText("❌ Échec de la génération. Veuillez réessayer.");
                            errorLabel.setVisible(true);
                            placeholderLabel.setVisible(true);
                        }

                        genererButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        errorLabel.setText("❌ Erreur: " + ex.getMessage());
                        errorLabel.setVisible(true);
                        genererButton.setDisable(false);
                        progressIndicator.setVisible(false);
                        placeholderLabel.setVisible(true);
                    });
                }
            }).start();
        });

        // Assemblage final
        mainContainer.getChildren().addAll(sidebar, mainScrollPane);

        Scene scene = new Scene(mainContainer);
        imageStage.setScene(scene);
        imageStage.show();
    }

    private void setupNavigation() {
        if (ajouterCoursButton != null) {
            ajouterCoursButton.setOnAction(event -> {
                System.out.println("➕ Navigation vers l'ajout de cours...");
                Navigation.navigateTo("coursajout.fxml", "Ajouter un cours");
            });

            ajouterCoursButton.setOnMouseEntered(e ->
                    ajouterCoursButton.setStyle("-fx-background-color: #6A1FF7; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(106,31,247,0.4), 10, 0, 0, 5);")
            );

            ajouterCoursButton.setOnMouseExited(e ->
                    ajouterCoursButton.setStyle("-fx-background-color: #7B2FF7; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 12 25; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(123,47,247,0.3), 10, 0, 0, 5);")
            );
        }

        if (ajouterEvaluationButton != null) {
            ajouterEvaluationButton.setOnAction(event -> {
                System.out.println("📝 Navigation vers l'ajout d'évaluation...");
                Navigation.navigateTo("evaluationajout.fxml", "Ajouter des questions");
            });

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

    private void setupDrawingSpace() {
        if (drawingSpaceButton != null) {
            drawingSpaceButton.setOnAction(event -> {
                System.out.println("🎨 Ouverture de l'espace de dessin...");
                ouvrirEspaceDessinAmeliore();
            });

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), drawingSpaceButton);
            drawingSpaceButton.setOnMouseEntered(e -> {
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.play();
                drawingSpaceButton.setStyle("-fx-background-color: #9B59B6; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(155,89,182,0.4), 10, 0, 0, 5);");
            });

            drawingSpaceButton.setOnMouseExited(e -> {
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
                drawingSpaceButton.setStyle("-fx-background-color: #8E44AD; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 12 25; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(142,68,173,0.3), 10, 0, 0, 5);");
            });
        }
    }

    private void ouvrirEspaceDessinAmeliore() {
        Stage drawingStage = new Stage();
        drawingStage.setTitle("🎨 Espace de dessin magique pour enfants");
        drawingStage.initModality(Modality.APPLICATION_MODAL);

        HBox mainContainer = new HBox();
        mainContainer.setPrefWidth(1400);
        mainContainer.setPrefHeight(850);

        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(260.0);
        sidebar.setMinWidth(260.0);
        sidebar.setMaxWidth(260.0);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #7B2FF7, #5F0FFF); -fx-padding: 25 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-padding: 0 0 15 5;");

        ImageView logoImage = new ImageView();
        logoImage.setFitHeight(40.0);
        logoImage.setFitWidth(40.0);
        logoImage.setPreserveRatio(true);
        try {
            Image img = new Image(getClass().getResource("/images/logo.png").toExternalForm());
            logoImage.setImage(img);
        } catch (Exception e) {
            Label logoEmoji = new Label("🎨");
            logoEmoji.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
            logoBox.getChildren().add(logoEmoji);
        }

        VBox logoText = new VBox(2);
        Label autiCareLabel = new Label("AutiCare");
        autiCareLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label espaceLabel = new Label("Espace éducatif");
        espaceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        logoText.getChildren().addAll(autiCareLabel, espaceLabel);

        logoBox.getChildren().addAll(logoImage, logoText);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-pref-height: 1;");

        VBox menuBox = new VBox(8);
        menuBox.setStyle("-fx-padding: 10 0 20 0;");

        Button coursButton = new Button("📚 Nos cours");
        coursButton.setMaxWidth(Double.MAX_VALUE);
        coursButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 10;");
        coursButton.setOnAction(e -> {
            System.out.println("📚 Retour à l'affichage des cours");
            drawingStage.close();
        });

        Button emploisButton = new Button("📅 Emplois du temps");
        emploisButton.setMaxWidth(Double.MAX_VALUE);
        emploisButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");
        emploisButton.setOnMouseEntered(e ->
                emploisButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );
        emploisButton.setOnMouseExited(e ->
                emploisButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );

        Button evenementsButton = new Button("🎉 Événements");
        evenementsButton.setMaxWidth(Double.MAX_VALUE);
        evenementsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");
        evenementsButton.setOnMouseEntered(e ->
                evenementsButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );
        evenementsButton.setOnMouseExited(e ->
                evenementsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );

        Button suiviButton = new Button("📞 Suivi consultation");
        suiviButton.setMaxWidth(Double.MAX_VALUE);
        suiviButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10; -fx-wrap-text: true;");
        suiviButton.setOnMouseEntered(e ->
                suiviButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10; -fx-wrap-text: true;")
        );
        suiviButton.setOnMouseExited(e ->
                suiviButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10; -fx-wrap-text: true;")
        );

        Button jeuxButton = new Button("🎮 Jeux éducatifs");
        jeuxButton.setMaxWidth(Double.MAX_VALUE);
        jeuxButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");
        jeuxButton.setOnMouseEntered(e ->
                jeuxButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );
        jeuxButton.setOnMouseExited(e ->
                jeuxButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;")
        );

        menuBox.getChildren().addAll(coursButton, emploisButton, evenementsButton, suiviButton, jeuxButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button deconnexionButton = new Button("🚪 Déconnexion");
        deconnexionButton.setMaxWidth(Double.MAX_VALUE);
        deconnexionButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");

        sidebar.getChildren().addAll(logoBox, separator, menuBox, spacer, deconnexionButton);

        BorderPane drawingContent = new BorderPane();
        drawingContent.setStyle("-fx-background-color: linear-gradient(to bottom, #D5B8FF, #E8D5FF);");

        ScrollPane mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-width: 0;");

        VBox scrollContent = new VBox(15);
        scrollContent.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 10 20; -fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 30; -fx-border-color: #8E44AD; -fx-border-width: 2; -fx-border-radius: 30;");

        Button backToCoursesButton = new Button("← Retour aux cours");
        backToCoursesButton.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        backToCoursesButton.setOnAction(e -> {
            System.out.println("📚 Retour à l'affichage des cours");
            drawingStage.close();
        });

        HBox logoTitleBox = new HBox(15);
        logoTitleBox.setAlignment(Pos.CENTER_LEFT);

        Label paintEmoji = new Label("🎨");
        paintEmoji.setStyle("-fx-font-size: 50px; -fx-text-fill: #000000;");

        VBox titleBox = new VBox(5);
        Label mainTitle = new Label("Espace de dessin magique");
        mainTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        Label subTitle = new Label("Laisse libre cours à ton imagination !");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        titleBox.getChildren().addAll(mainTitle, subTitle);
        logoTitleBox.getChildren().addAll(paintEmoji, titleBox);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backToCoursesButton, headerSpacer, logoTitleBox);

        VBox topPanel = new VBox(15);
        topPanel.setStyle("-fx-padding: 20; -fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 30; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.3), 10, 0, 0, 5); -fx-border-color: #8E44AD; -fx-border-width: 2; -fx-border-radius: 30;");

        FlowPane toolBar = new FlowPane();
        toolBar.setHgap(15);
        toolBar.setVgap(15);
        toolBar.setAlignment(Pos.CENTER);
        toolBar.setPadding(new Insets(15));

        ColorPicker colorPicker = new ColorPicker(Color.web("#8E44AD"));
        colorPicker.setStyle("-fx-font-size: 14px; -fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #8E44AD; -fx-border-width: 2;");

        VBox brushBox = new VBox(5);
        brushBox.setAlignment(Pos.CENTER);
        Label brushLabel = new Label("🖌️ Taille du pinceau");
        brushLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");

        Slider brushSizeSlider = new Slider(1, 50, 10);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setPrefWidth(200);
        brushSizeSlider.setStyle("-fx-control-inner-background: #D5B8FF;");

        Label brushSizeLabel = new Label("10");
        brushSizeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");

        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                brushSizeLabel.setText(String.valueOf(newVal.intValue()))
        );

        brushBox.getChildren().addAll(brushLabel, brushSizeSlider, brushSizeLabel);

        ToggleGroup toolsGroup = new ToggleGroup();

        ToggleButton pencilButton = createToolButton("✏️", "Crayon", "#E1BEE7", toolsGroup, "pencil");
        ToggleButton eraserButton = createToolButton("🧽", "Gomme", "#FFCDD2", toolsGroup, "eraser");
        ToggleButton starButton = createToolButton("⭐", "Étoile", "#FFF9C4", toolsGroup, "star");
        ToggleButton heartButton = createToolButton("❤️", "Cœur", "#F8BBD0", toolsGroup, "heart");
        ToggleButton rainbowButton = createToolButton("🌈", "Arc-en-ciel", "#B2EBF2", toolsGroup, "rainbow");

        pencilButton.setSelected(true);

        FlowPane actionFlowPane = new FlowPane();
        actionFlowPane.setHgap(15);
        actionFlowPane.setVgap(15);
        actionFlowPane.setAlignment(Pos.CENTER);

        Button clearButton = createActionButton("🗑️", "Effacer tout", "#FF6B6B");
        Button saveButton = createActionButton("💾", "Sauvegarder", "#4CAF50");
        Button closeButton = createActionButton("❌", "Fermer", "#95A5A6");
        closeButton.setOnAction(e -> drawingStage.close());

        actionFlowPane.getChildren().addAll(clearButton, saveButton, closeButton);

        FlowPane shapeFlowPane = new FlowPane();
        shapeFlowPane.setHgap(15);
        shapeFlowPane.setVgap(15);
        shapeFlowPane.setAlignment(Pos.CENTER);
        shapeFlowPane.setStyle("-fx-padding: 15; -fx-background-color: #F5E6FF; -fx-background-radius: 30; -fx-border-color: #8E44AD; -fx-border-width: 2; -fx-border-radius: 30;");

        Label shapeLabel = new Label("✨ Formes magiques:");
        shapeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");

        Button circleButton = createShapeButton("⭕", "Cercle", "#FFA726");
        Button squareButton = createShapeButton("⬛", "Carré", "#66BB6A");
        Button triangleButton = createShapeButton("🔺", "Triangle", "#42A5F5");
        Button sunButton = createShapeButton("☀️", "Soleil", "#FFD54F");
        Button cloudButton = createShapeButton("☁️", "Nuage", "#90CAF9");
        Button flowerButton = createShapeButton("🌸", "Fleur", "#F06292");
        Button star2Button = createShapeButton("⭐", "Étoile", "#FFB74D");
        Button heart2Button = createShapeButton("❤️", "Cœur", "#F06292");
        Button moonButton = createShapeButton("🌙", "Lune", "#9575CD");
        Button treeButton = createShapeButton("🌲", "Arbre", "#66BB6A");

        shapeFlowPane.getChildren().addAll(
                shapeLabel, circleButton, squareButton, triangleButton,
                sunButton, cloudButton, flowerButton, star2Button,
                heart2Button, moonButton, treeButton
        );

        toolBar.getChildren().addAll(
                createToolLabel("🎨 Couleur:"), colorPicker,
                brushBox
        );

        toolBar.getChildren().add(createToolLabel("🖌️ Outils:"));
        toolBar.getChildren().addAll(pencilButton, eraserButton, starButton, heartButton, rainbowButton);

        topPanel.getChildren().addAll(toolBar, actionFlowPane, shapeFlowPane);

        StackPane canvasContainer = new StackPane();
        canvasContainer.setStyle("-fx-padding: 20;");

        VBox canvasBackground = new VBox();
        canvasBackground.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-border-radius: 30; -fx-border-color: #8E44AD; -fx-border-width: 5; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.3), 15, 0, 0, 5);");

        Canvas canvas = new Canvas(1000, 600);
        canvas.setStyle("-fx-background-color: white; -fx-background-radius: 25;");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(10);
        gc.setStroke(Color.web("#8E44AD"));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        final double[] lastX = {0};
        final double[] lastY = {0};
        final Color[] currentColor = {Color.web("#8E44AD")};
        final double[] currentSize = {10};
        final String[] currentTool = {"pencil"};

        canvas.setOnMousePressed(e -> {
            lastX[0] = e.getX();
            lastY[0] = e.getY();
            gc.beginPath();
            gc.moveTo(lastX[0], lastY[0]);

            if (currentTool[0].equals("star")) {
                dessinerEtoile(gc, e.getX(), e.getY(), currentSize[0] * 2);
            } else if (currentTool[0].equals("heart")) {
                dessinerCoeur(gc, e.getX(), e.getY(), currentSize[0]);
            } else if (currentTool[0].equals("rainbow")) {
                currentColor[0] = Color.color(Math.random(), Math.random(), Math.random());
                gc.setStroke(currentColor[0]);
                gc.setLineWidth(currentSize[0]);
                gc.stroke();
            } else {
                gc.setStroke(currentColor[0]);
                gc.setLineWidth(currentSize[0]);
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(e -> {
            double currentX = e.getX();
            double currentY = e.getY();

            if (toolsGroup.getSelectedToggle() != null) {
                String tool = (String) toolsGroup.getSelectedToggle().getUserData();
                currentTool[0] = tool;

                if ("eraser".equals(tool)) {
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(currentSize[0]);
                } else if ("pencil".equals(tool)) {
                    gc.setStroke(currentColor[0]);
                    gc.setLineWidth(currentSize[0]);
                } else if ("star".equals(tool)) {
                    dessinerEtoile(gc, currentX, currentY, currentSize[0] * 2);
                    lastX[0] = currentX;
                    lastY[0] = currentY;
                    return;
                } else if ("heart".equals(tool)) {
                    dessinerCoeur(gc, currentX, currentY, currentSize[0]);
                    lastX[0] = currentX;
                    lastY[0] = currentY;
                    return;
                } else if ("rainbow".equals(tool)) {
                    gc.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
                    gc.setLineWidth(currentSize[0]);
                }
            }

            gc.strokeLine(lastX[0], lastY[0], currentX, currentY);
            lastX[0] = currentX;
            lastY[0] = currentY;
        });

        colorPicker.setOnAction(e -> {
            currentColor[0] = colorPicker.getValue();
        });

        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentSize[0] = newVal.doubleValue();
        });

        clearButton.setOnAction(e -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            showHappyMessage("✨ Bravo ! Tu as effacé ton dessin !");
        });

        saveButton.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Sauvegarder ton chef-d'œuvre");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images PNG", "*.png")
                );
                File file = fileChooser.showSaveDialog(drawingStage);

                if (file != null) {
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    WritableImage image = canvas.snapshot(params, null);

                    BufferedImage bufferedImage = new BufferedImage(
                            (int) canvas.getWidth(),
                            (int) canvas.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );

                    for (int x = 0; x < canvas.getWidth(); x++) {
                        for (int y = 0; y < canvas.getHeight(); y++) {
                            int argb = image.getPixelReader().getArgb(x, y);
                            bufferedImage.setRGB(x, y, argb);
                        }
                    }

                    ImageIO.write(bufferedImage, "png", file);
                    showHappyMessage("🎉 Super ! Ton dessin est sauvegardé !");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Oups!", "Erreur: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });



        circleButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeOval(400, 200, 100, 100);
            showHappyMessage("⭕ Joli cercle !");
        });

        squareButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeRect(400, 200, 100, 100);
            showHappyMessage("⬛ Beau carré !");
        });

        triangleButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            double[] xPoints = {450, 350, 550};
            double[] yPoints = {150, 350, 350};
            gc.strokePolygon(xPoints, yPoints, 3);
            showHappyMessage("🔺 Super triangle !");
        });

        sunButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeOval(450, 200, 80, 80);
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                double x1 = 490 + 50 * Math.cos(angle);
                double y1 = 240 + 50 * Math.sin(angle);
                double x2 = 490 + 70 * Math.cos(angle);
                double y2 = 240 + 70 * Math.sin(angle);
                gc.strokeLine(x1, y1, x2, y2);
            }
            showHappyMessage("☀️ Il fait soleil !");
        });

        cloudButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeOval(350, 200, 60, 40);
            gc.strokeOval(390, 190, 70, 50);
            gc.strokeOval(440, 200, 60, 40);
            showHappyMessage("☁️ Un joli nuage !");
        });

        flowerButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            for (int i = 0; i < 5; i++) {
                double angle = i * 2 * Math.PI / 5;
                double x = 450 + 40 * Math.cos(angle);
                double y = 250 + 40 * Math.sin(angle);
                gc.strokeOval(x-15, y-15, 30, 30);
            }
            gc.strokeOval(435, 235, 30, 30);
            showHappyMessage("🌸 Une belle fleur !");
        });

        star2Button.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            dessinerEtoile(gc, 450, 250, 50);
            showHappyMessage("⭐ Une jolie étoile !");
        });

        heart2Button.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            dessinerCoeur(gc, 450, 250, 30);
            showHappyMessage("❤️ Un beau cœur !");
        });

        moonButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeOval(450, 200, 70, 70);
            gc.setFill(Color.WHITE);
            gc.fillOval(470, 190, 40, 40);
            showHappyMessage("🌙 Une jolie lune !");
        });

        treeButton.setOnAction(e -> {
            gc.setStroke(currentColor[0]);
            gc.setLineWidth(currentSize[0]);
            gc.strokeRect(440, 250, 20, 80);
            gc.strokeOval(430, 200, 40, 40);
            gc.strokeOval(450, 180, 30, 30);
            gc.strokeOval(410, 220, 30, 30);
            showHappyMessage("🌲 Un bel arbre !");
        });

        canvasBackground.getChildren().add(canvas);
        canvasContainer.getChildren().add(canvasBackground);

        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setStyle("-fx-padding: 15; -fx-background-color: #F5E6FF; -fx-background-radius: 30; -fx-border-color: #8E44AD; -fx-border-width: 3; -fx-border-radius: 30;");

        Label messageIcon = new Label("🎉");
        messageIcon.setStyle("-fx-font-size: 30px;");

        Label messageLabel = new Label("Amuse-toi bien ! Dessine ce que tu veux avec de belles couleurs !");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");

        messageBox.getChildren().addAll(messageIcon, messageLabel);

        scrollContent.getChildren().addAll(headerBox, topPanel, canvasContainer, messageBox);
        mainScrollPane.setContent(scrollContent);

        drawingContent.setCenter(mainScrollPane);

        mainContainer.getChildren().addAll(sidebar, drawingContent);
        HBox.setHgrow(drawingContent, Priority.ALWAYS);

        Scene scene = new Scene(mainContainer, 1400, 850);
        drawingStage.setScene(scene);
        drawingStage.show();
    }

    private ToggleButton createToolButton(String emoji, String tooltip, String color, ToggleGroup group, String userData) {
        ToggleButton button = new ToggleButton(emoji);
        button.setToggleGroup(group);
        button.setUserData(userData);
        button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 20px; -fx-min-width: 60; -fx-min-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");

        Tooltip tp = new Tooltip(tooltip);
        tp.setStyle("-fx-font-size: 14px; -fx-background-color: #8E44AD; -fx-text-fill: white;");
        button.setTooltip(tp);

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 24px; -fx-min-width: 60; -fx-min-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #8E44AD, 10, 0, 0, 5);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 20px; -fx-min-width: 60; -fx-min-height: 60; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");
        });

        return button;
    }

    private Button createActionButton(String emoji, String tooltip, String color) {
        Button button = new Button(emoji + " " + tooltip);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #8E44AD, 10, 0, 0, 5);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");
        });

        return button;
    }

    private Button createShapeButton(String emoji, String tooltip, String color) {
        Button button = new Button(emoji);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 20px; -fx-min-width: 50; -fx-min-height: 50; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");

        Tooltip tp = new Tooltip(tooltip);
        tp.setStyle("-fx-font-size: 14px; -fx-background-color: #8E44AD; -fx-text-fill: white;");
        button.setTooltip(tp);

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 24px; -fx-min-width: 50; -fx-min-height: 50; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #8E44AD, 10, 0, 0, 5);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 20px; -fx-min-width: 50; -fx-min-height: 50; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.2), 5, 0, 0, 2);");
        });

        return button;
    }

    private Label createToolLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");
        return label;
    }

    private void dessinerEtoile(GraphicsContext gc, double x, double y, double taille) {
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        double[] xPoints = new double[10];
        double[] yPoints = new double[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double rayon = (i % 2 == 0) ? taille : taille / 2;
            xPoints[i] = x + rayon * Math.cos(angle);
            yPoints[i] = y - rayon * Math.sin(angle);
        }

        gc.strokePolygon(xPoints, yPoints, 10);
    }

    private void dessinerCoeur(GraphicsContext gc, double x, double y, double taille) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);
        gc.strokeOval(x - taille, y - taille/2, taille, taille);
        gc.strokeOval(x, y - taille/2, taille, taille);
        gc.strokeLine(x - taille, y, x, y + taille);
        gc.strokeLine(x + taille, y, x, y + taille);
    }

    private void showHappyMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Bravo !");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F5E6FF; -fx-background-radius: 20; -fx-border-color: #8E44AD; -fx-border-width: 3; -fx-border-radius: 20;");

        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        VBox card = new VBox(0);
        card.setPrefWidth(280);
        card.setPrefHeight(380);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                        "-fx-transition: all 0.3s ease;"
        );

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 24;" +
                            "-fx-border-radius: 24;" +
                            "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.3), 25, 0, 0, 10);" +
                            "-fx-translate-y: -5;" +
                            "-fx-transition: all 0.3s ease;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 24;" +
                            "-fx-border-radius: 24;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                            "-fx-translate-y: 0;" +
                            "-fx-transition: all 0.3s ease;"
            );
        });

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle(
                "-fx-background-radius: 24 24 0 0;" +
                        "-fx-background-color: linear-gradient(to bottom, #7B2FF7, #9F5FF7);"
        );

        ImageView courseImageView = new ImageView();
        courseImageView.setFitHeight(160);
        courseImageView.setFitWidth(280);
        courseImageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(300, 180);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        courseImageView.setClip(clip);

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

        Region gradientOverlay = new Region();
        gradientOverlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.4), transparent);" +
                        "-fx-background-radius: 24 24 0 0;"
        );
        gradientOverlay.setPrefHeight(70);
        gradientOverlay.setMaxHeight(70);
        StackPane.setAlignment(gradientOverlay, Pos.TOP_CENTER);

        Label typeBadge = new Label(cours.getType_cours());
        String typeColor = getTypeColor(cours.getType_cours());
        typeBadge.setStyle(
                "-fx-background-color: " + typeColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 6 16;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);" +
                        "-fx-border-color: rgba(255,255,255,0.3);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 30;"
        );
        StackPane.setAlignment(typeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeBadge, new Insets(15, 15, 0, 0));

        imageContainer.getChildren().addAll(courseImageView, gradientOverlay, typeBadge);

        VBox contentContainer = new VBox(15);
        contentContainer.setStyle("-fx-padding: 20 20 15 20;");

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleIcon = new Label(getEmojiForCours(cours));
        titleIcon.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2C3E50;" +
                        "-fx-wrap-text: true;"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);

        titleBox.getChildren().addAll(titleIcon, titleLabel);

        String description = cours.getDescription();
        if (description.length() > 80) {
            description = description.substring(0, 77) + "...";
        }

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #7F8C8D;" +
                        "-fx-line-spacing: 2;"
        );

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setStyle("-fx-padding: 5 0;");

        String niveauColor = getNiveauColor(cours.getNiveau());
        Label niveauIcon = new Label(getNiveauIcon(cours.getNiveau()));
        niveauIcon.setStyle("-fx-font-size: 16px;");
        Label niveauLabel = new Label(cours.getNiveau());
        niveauLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: " + niveauColor + ";"
        );
        HBox niveauBox = new HBox(5, niveauIcon, niveauLabel);
        niveauBox.setAlignment(Pos.CENTER_LEFT);

        Label dureeIcon = new Label("⏱️");
        dureeIcon.setStyle("-fx-font-size: 16px;");
        Label dureeLabel = new Label(cours.getDuree() + " min");
        dureeLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #7B2FF7;"
        );
        HBox dureeBox = new HBox(5, dureeIcon, dureeLabel);
        dureeBox.setAlignment(Pos.CENTER_LEFT);

        infoGrid.add(niveauBox, 0, 0);
        infoGrid.add(dureeBox, 1, 0);

        int motCount = cours.getMots() != null ? cours.getMots().split(";").length : 0;
        Label motsIcon = new Label("📝");
        motsIcon.setStyle("-fx-font-size: 14px;");
        Label motsCountLabel = new Label(motCount + " mot" + (motCount > 1 ? "s" : ""));
        motsCountLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #95A5A6;"
        );
        HBox motsBox = new HBox(5, motsIcon, motsCountLabel);
        motsBox.setAlignment(Pos.CENTER_LEFT);

        int evalCount = evaluationServices.compterParCours(cours.getId_cours());
        Label evalIcon = new Label("📋");
        evalIcon.setStyle("-fx-font-size: 14px;");
        Label evalCountLabel = new Label(evalCount + " éval" + (evalCount > 1 ? "s" : ""));
        evalCountLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #95A5A6;"
        );
        HBox evalBox = new HBox(5, evalIcon, evalCountLabel);
        evalBox.setAlignment(Pos.CENTER_LEFT);

        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(motsBox, evalBox);

        Button commencerBtn = new Button("Commencer l'apprentissage");
        commencerBtn.setMaxWidth(Double.MAX_VALUE);
        commencerBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #7B2FF7, #9F5FF7);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.3), 10, 0, 0, 3);" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 30;"
        );

        commencerBtn.setOnMouseEntered(e -> {
            commencerBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6A1FF7, #8A4FF7);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 12 20;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.5), 15, 0, 0, 5);" +
                            "-fx-border-color: rgba(255,255,255,0.3);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 30;" +
                            "-fx-scale-x: 1.02;" +
                            "-fx-scale-y: 1.02;"
            );
        });

        commencerBtn.setOnMouseExited(e -> {
            commencerBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #7B2FF7, #9F5FF7);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 12 20;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(123,47,247,0.3), 10, 0, 0, 3);" +
                            "-fx-border-color: rgba(255,255,255,0.2);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 30;"
            );
        });

        commencerBtn.setOnAction(e -> {
            System.out.println("🎓 Début du cours: " + cours.getTitre());
            afficherContenuSimplifie(cours);
        });

        contentContainer.getChildren().addAll(
                titleBox,
                descriptionLabel,
                infoGrid,
                statsBox
        );

        card.getChildren().addAll(imageContainer, contentContainer, commencerBtn);
        VBox.setMargin(commencerBtn, new Insets(0, 20, 20, 20));

        return card;
    }

    private String getTypeColor(String type) {
        if (type == null) return "#7B2FF7";

        switch (type) {
            case "Académique": return "#3498DB";
            case "Social": return "#E91E63";
            case "Autonomie": return "#2ECC71";
            case "Créativité": return "#F39C12";
            default: return "#7B2FF7";
        }
    }

    private String getNiveauColor(String niveau) {
        if (niveau == null) return "#7B2FF7";

        switch (niveau) {
            case "Débutant": return "#2ECC71";
            case "Intermédiaire": return "#F39C12";
            case "Avancé": return "#E74C3C";
            default: return "#7B2FF7";
        }
    }

    private String getNiveauIcon(String niveau) {
        if (niveau == null) return "📊";

        switch (niveau) {
            case "Débutant": return "🌱";
            case "Intermédiaire": return "📈";
            case "Avancé": return "🚀";
            default: return "📊";
        }
    }

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

        // ================= CONTENEUR PRINCIPAL (MÊME TAILLE QUE L'AFFICHAGE COURS) =================
        HBox mainContainer = new HBox();
        mainContainer.setPrefWidth(1300);
        mainContainer.setPrefHeight(800);

        // ================= SIDEBAR IDENTIQUE À L'AFFICHAGE COURS =================
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(260.0);
        sidebar.setMinWidth(260.0);
        sidebar.setMaxWidth(260.0);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #7B2FF7, #5F0FFF); -fx-padding: 25 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Logo
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-padding: 0 0 15 5;");

        ImageView logoImage = new ImageView();
        logoImage.setFitHeight(40.0);
        logoImage.setFitWidth(40.0);
        logoImage.setPreserveRatio(true);
        try {
            Image img = new Image(getClass().getResource("/images/logo.png").toExternalForm());
            logoImage.setImage(img);
        } catch (Exception e) {
            Label logoEmoji = new Label("🎨");
            logoEmoji.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
            logoBox.getChildren().add(logoEmoji);
        }

        VBox logoText = new VBox(2);
        Label autiCareLabel = new Label("AutiCare");
        autiCareLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label espaceLabel = new Label("Espace éducatif");
        espaceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        logoText.getChildren().addAll(autiCareLabel, espaceLabel);

        logoBox.getChildren().addAll(logoImage, logoText);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-pref-height: 1;");

        // Menu de navigation
        VBox menuBox = new VBox(8);
        menuBox.setStyle("-fx-padding: 10 0 20 0;");

        Button coursButton = new Button("📚 Nos cours");
        coursButton.setMaxWidth(Double.MAX_VALUE);
        coursButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 10;");
        coursButton.setOnAction(e -> {
            System.out.println("📚 Retour à l'affichage des cours");
            contentStage.close();
        });

        Button emploisButton = new Button("📅 Emplois du temps");
        emploisButton.setMaxWidth(Double.MAX_VALUE);
        emploisButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        Button evenementsButton = new Button("🎉 Événements");
        evenementsButton.setMaxWidth(Double.MAX_VALUE);
        evenementsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        Button suiviButton = new Button("📞 Suivi consultation");
        suiviButton.setMaxWidth(Double.MAX_VALUE);
        suiviButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10; -fx-wrap-text: true;");

        Button jeuxButton = new Button("🎮 Jeux éducatifs");
        jeuxButton.setMaxWidth(Double.MAX_VALUE);
        jeuxButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-cursor: hand; -fx-padding: 12 15; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-background-radius: 10;");

        menuBox.getChildren().addAll(coursButton, emploisButton, evenementsButton, suiviButton, jeuxButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button deconnexionButton = new Button("🚪 Déconnexion");
        deconnexionButton.setMaxWidth(Double.MAX_VALUE);
        deconnexionButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");

        sidebar.getChildren().addAll(logoBox, separator, menuBox, spacer, deconnexionButton);

        // ================= CONTENU PRINCIPAL (VOTRE CODE EXISTANT) =================
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F0F8FF; -fx-border-width: 0;");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        // Conteneur pour le contenu (renommé pour éviter le conflit)
        VBox contentContainer = new VBox(30);
        contentContainer.setStyle("-fx-background-color: #F0F8FF; -fx-padding: 40;");
        contentContainer.setAlignment(Pos.TOP_CENTER);

        // En-tête avec bouton retour
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setMaxWidth(1200);

        Button backButton = new Button("← Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 18px; -fx-cursor: hand;");
        backButton.setOnAction(e -> contentStage.close());

        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        headerBox.getChildren().addAll(backButton, spacer2, titleLabel);

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

            int nbImages = Math.min(imagesList.length, motsList.length);

            for (int i = 0; i < motsList.length; i++) {
                String mot = motsList[i].trim();
                if (mot.isEmpty()) continue;

                String image = (i < nbImages) ? imagesList[i].trim() : null;

                if (image != null && (image.equals("null") || image.isEmpty())) {
                    image = null;
                }

                if (image != null && image.contains(";")) {
                    String[] multipleImages = image.split(";");
                    String firstImage = multipleImages[0].trim();
                    VBox motCard = createMotCard(mot, firstImage);
                    motsPane.getChildren().add(motCard);
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

        // SECTION ÉVALUATION
        VBox evaluationSection = new VBox(15);
        evaluationSection.setMaxWidth(1200);
        evaluationSection.setAlignment(Pos.CENTER);
        evaluationSection.setStyle("-fx-padding: 40 0 20 0;");

        Label evaluationTitle = new Label("📋 Évaluation du cours");
        evaluationTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

        int questionCount = evaluationServices.compterParCours(cours.getId_cours());

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

        passerQuizBtn.setOnAction(e -> {
            contentStage.close();
            System.out.println("🎯 Navigation vers le quiz pour le cours ID: " + cours.getId_cours());
            Navigation.navigateTo("passerevaluation.fxml?coursId=" + cours.getId_cours(),
                    "Quiz - " + cours.getTitre());
        });

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

        // Assemblage du contenu
        contentContainer.getChildren().addAll(headerBox, descriptionCard, motsSection, evaluationSection);
        scrollPane.setContent(contentContainer);

        // Assemblage final avec sidebar
        mainContainer.getChildren().addAll(sidebar, scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(mainContainer, 1300, 800);
        contentStage.setScene(scene);
        contentStage.show();
    }

    private VBox createMotCard(String mot, String imageUrl) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(280);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-color: #7B2FF7;" +
                        "-fx-border-width: 3;" +
                        "-fx-padding: 20;" +
                        "-fx-cursor: hand;"
        );

        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: #F0E6FF;" +
                                "-fx-background-radius: 30;" +
                                "-fx-border-radius: 30;" +
                                "-fx-border-color: #7B2FF7;" +
                                "-fx-border-width: 4;" +
                                "-fx-padding: 20;" +
                                "-fx-cursor: hand;"
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
                                "-fx-cursor: hand;"
                )
        );

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(120);

        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
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

        Button speakerButton = new Button("🔊");
        speakerButton.setStyle(
                "-fx-background-color: #7B2FF7;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );

        speakerButton.setOnAction(e -> {
            TextToSpeechManager.speak(mot);
        });

        card.setOnMouseClicked(e -> {
            TextToSpeechManager.speak(mot);
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(speakerButton);

        card.getChildren().addAll(imageContainer, motLabel, buttonBox);
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
            resetFilterButtons();
        }

        coursCountLabel.setText(filtered.size() + " Cours disponibles");
        displayCours(filtered);
    }

    private void updateFilterButtons(String activeCategory) {
        resetFilterButtons();

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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}