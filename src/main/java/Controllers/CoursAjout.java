package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Utils.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Optional;

public class CoursAjout implements Initializable {

    @FXML
    private TextField titreField;
    @FXML
    private ComboBox<String> typeCoursCombo;
    @FXML
    private ComboBox<String> niveauCombo;
    @FXML
    private TextField dureeField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField imageField;

    @FXML
    private Button parcourirImageButton;

    @FXML
    private TextArea motsField;
    @FXML
    private VBox imagesMotsContainer;
    @FXML
    private Button ajouterLigneImageButton;

    @FXML
    private TableView<Cours> coursTable;
    @FXML
    private TableColumn<Cours, Integer> idColumn;
    @FXML
    private TableColumn<Cours, String> titreColumn;
    @FXML
    private TableColumn<Cours, String> typeColumn;
    @FXML
    private TableColumn<Cours, String> niveauColumn;
    @FXML
    private TableColumn<Cours, Integer> dureeColumn;
    @FXML
    private TableColumn<Cours, String> descriptionColumn;
    @FXML
    private TableColumn<Cours, String> imageColumn;
    @FXML
    private TableColumn<Cours, Void> actionsColumn;

    @FXML
    private Button ajouterButton;
    @FXML
    private Button annulerButton;
    @FXML
    private Button voirCoursButton;

    // Map pour stocker les labels d'erreur associés à chaque champ
    private Map<Control, Label> errorLabels = new HashMap<>();

    private CoursServices coursServices;
    private ObservableList<Cours> coursList;

    private List<ImageMotLigne> imagesMotLignes = new ArrayList<>();

    // Variable pour suivre si on est en mode modification
    private Cours coursEnModification = null;

    // Styles pour la validation
    private static final String STYLE_VALIDE = "-fx-border-color: #00C853; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_INVALIDE = "-fx-border-color: #D32F2F; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();

        initializeComboBoxes();
        initializeTable();
        loadCoursData();
        configureActions();
        setupValidation();

        ajouterLigneImageButton.setOnAction(e -> ajouterNouvelleLigneImage());

        motsField.textProperty().addListener((observable, oldValue, newValue) -> {
            mettreAJourLignesImages();
        });

        // Ajouter les labels d'erreur après l'initialisation de la scène
        javafx.application.Platform.runLater(this::addErrorLabels);

        // Ajouter un listener pour rafraîchir la table quand elle devient visible
        coursTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                refreshTable();
            }
        });
    }

    /**
     * Rafraîchit la table des cours
     */
    private void refreshTable() {
        try {
            List<Cours> cours = coursServices.getAll();
            if (cours != null) {
                coursList = FXCollections.observableArrayList(cours);
                coursTable.setItems(coursList);
                coursTable.refresh();
                System.out.println("Table rafraîchie avec " + cours.size() + " cours");
            } else {
                System.out.println("Aucun cours trouvé");
                coursList = FXCollections.observableArrayList();
                coursTable.setItems(coursList);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement de la table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute les labels d'erreur après chaque champ
     */
    private void addErrorLabels() {
        addErrorLabelAfter(titreField, "Titre du cours");
        addErrorLabelAfter(typeCoursCombo, "Type de cours");
        addErrorLabelAfter(niveauCombo, "Niveau");
        addErrorLabelAfter(dureeField, "Durée (en minutes)");
        addErrorLabelAfter(descriptionArea, "Description");
        addErrorLabelAfter(imageField, "Image du cours");
        addErrorLabelAfter(motsField, "Mots du cours");
    }

    /**
     * Ajoute un label d'erreur après un champ spécifique
     */
    private void addErrorLabelAfter(Control field, String fieldName) {
        // Créer le label d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-wrap-text: true;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Trouver le parent du champ
        Node parent = field.getParent();
        if (parent instanceof VBox) {
            VBox vbox = (VBox) parent;
            int index = vbox.getChildren().indexOf(field);
            if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                vbox.getChildren().add(index + 1, errorLabel);
            } else {
                vbox.getChildren().add(errorLabel);
            }
        } else if (parent instanceof HBox) {
            // Si le champ est dans un HBox, remonter au VBox parent
            Node grandParent = parent.getParent();
            if (grandParent instanceof VBox) {
                VBox vbox = (VBox) grandParent;
                int index = vbox.getChildren().indexOf(parent);
                if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                    vbox.getChildren().add(index + 1, errorLabel);
                } else {
                    vbox.getChildren().add(errorLabel);
                }
            }
        }

        // Stocker le label dans la map
        errorLabels.put(field, errorLabel);
    }

    /**
     * Affiche un message d'erreur pour un champ
     */
    private void showError(Control field, String message) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Cache le message d'erreur pour un champ
     */
    private void hideError(Control field) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Classe interne pour gérer une ligne de sélection d'images pour un mot
     */
    private class ImageMotLigne {
        private Label motLabel;
        private TextField imagesField;
        private Button parcourirButton;
        private Label errorLabel;
        private HBox ligne;
        private VBox ligneContainer;

        public ImageMotLigne(String mot) {
            motLabel = new Label(mot);
            motLabel.setPrefWidth(150);
            motLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

            imagesField = new TextField();
            imagesField.setPromptText("Images (séparées par ;)");
            imagesField.setPrefWidth(300);
            imagesField.setStyle(STYLE_INVALIDE);

            errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px;");
            errorLabel.setVisible(false);

            parcourirButton = new Button("Parcourir...");
            parcourirButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 10;");

            Button supprimerButton = new Button("✖");
            supprimerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4444; -fx-font-size: 16px; -fx-cursor: hand;");

            ligne = new HBox(10, motLabel, imagesField, parcourirButton, supprimerButton);
            ligne.setAlignment(Pos.CENTER_LEFT);

            ligneContainer = new VBox(5, ligne, errorLabel);
            ligneContainer.setStyle("-fx-padding: 5 0;");

            parcourirButton.setOnAction(e -> choisirImagesMultiples(imagesField));
            supprimerButton.setOnAction(e -> supprimerLigne(this));

            // Ajouter la validation pour ce champ
            imagesField.textProperty().addListener((obs, old, newValue) -> {
                validerChampImageMot(this);
            });
        }

        public VBox getLigneContainer() { return ligneContainer; }
        public String getMot() { return motLabel.getText(); }
        public String getImages() { return imagesField.getText(); }
        public void setImages(String images) { imagesField.setText(images); }
        public void showError(String message) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
        }
        public void hideError() {
            errorLabel.setVisible(false);
        }
        public TextField getImagesField() { return imagesField; }
    }

    /**
     * Met à jour les lignes d'images en fonction des mots saisis
     */
    private void mettreAJourLignesImages() {
        String motsText = motsField.getText();
        if (motsText == null || motsText.trim().isEmpty()) {
            imagesMotLignes.clear();
            imagesMotsContainer.getChildren().clear();
            return;
        }

        String[] mots = motsText.split(";");
        List<String> motsActuels = new ArrayList<>();
        for (String mot : mots) {
            if (!mot.trim().isEmpty()) {
                motsActuels.add(mot.trim());
            }
        }

        List<ImageMotLigne> aSupprimer = new ArrayList<>();
        for (ImageMotLigne ligne : imagesMotLignes) {
            if (!motsActuels.contains(ligne.getMot())) {
                aSupprimer.add(ligne);
            }
        }

        for (ImageMotLigne ligne : aSupprimer) {
            imagesMotLignes.remove(ligne);
            imagesMotsContainer.getChildren().remove(ligne.getLigneContainer());
        }

        for (String mot : motsActuels) {
            boolean motExiste = imagesMotLignes.stream()
                    .anyMatch(ligne -> ligne.getMot().equals(mot));

            if (!motExiste) {
                ImageMotLigne nouvelleLigne = new ImageMotLigne(mot);
                imagesMotLignes.add(nouvelleLigne);
                imagesMotsContainer.getChildren().add(nouvelleLigne.getLigneContainer());
            }
        }
    }

    /**
     * Ajoute une nouvelle ligne d'image pour un mot
     */
    private void ajouterNouvelleLigneImage() {
        String motsText = motsField.getText();
        if (motsText == null || motsText.trim().isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ajouter un mot");
            dialog.setHeaderText("Saisissez le mot pour lequel vous voulez ajouter des images");
            dialog.setContentText("Mot :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String mot = result.get().trim();
                ImageMotLigne nouvelleLigne = new ImageMotLigne(mot);
                imagesMotLignes.add(nouvelleLigne);
                imagesMotsContainer.getChildren().add(nouvelleLigne.getLigneContainer());

                // Mettre à jour le champ mots
                if (motsField.getText().isEmpty()) {
                    motsField.setText(mot);
                } else {
                    motsField.setText(motsField.getText() + ";" + mot);
                }
            }
        } else {
            String[] mots = motsText.split(";");
            for (String mot : mots) {
                if (!mot.trim().isEmpty()) {
                    boolean motExiste = imagesMotLignes.stream()
                            .anyMatch(ligne -> ligne.getMot().equals(mot.trim()));

                    if (!motExiste) {
                        ImageMotLigne nouvelleLigne = new ImageMotLigne(mot.trim());
                        imagesMotLignes.add(nouvelleLigne);
                        imagesMotsContainer.getChildren().add(nouvelleLigne.getLigneContainer());
                    }
                }
            }
        }
    }

    /**
     * Ouvre un FileChooser pour sélectionner une image pour le cours
     */
    private void choisirImageCours() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le cours");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imageField.setText(selectedFile.getName());
            validerImage();
        }
    }

    /**
     * Ouvre un FileChooser pour sélectionner plusieurs images
     */
    private void choisirImagesMultiples(TextField imagesField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des images");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedFiles.size(); i++) {
                if (i > 0) sb.append(";");
                sb.append(selectedFiles.get(i).getName());
            }

            String existingImages = imagesField.getText();
            if (existingImages != null && !existingImages.isEmpty()) {
                imagesField.setText(existingImages + ";" + sb.toString());
            } else {
                imagesField.setText(sb.toString());
            }
        }
    }

    /**
     * Supprime une ligne d'image
     */
    private void supprimerLigne(ImageMotLigne ligne) {
        imagesMotLignes.remove(ligne);
        imagesMotsContainer.getChildren().remove(ligne.getLigneContainer());

        // Mettre à jour le champ mots
        String[] mots = motsField.getText().split(";");
        StringBuilder newMots = new StringBuilder();
        for (String mot : mots) {
            if (!mot.trim().equals(ligne.getMot()) && !mot.trim().isEmpty()) {
                if (newMots.length() > 0) newMots.append(";");
                newMots.append(mot.trim());
            }
        }
        motsField.setText(newMots.toString());
    }

    /**
     * Initialise les ComboBox avec leurs valeurs
     */
    private void initializeComboBoxes() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Académique", "Social", "Autonomie", "Créativité"
        );
        typeCoursCombo.setItems(types);
        typeCoursCombo.setStyle(STYLE_INVALIDE);

        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "Débutant", "Intermédiaire", "Avancé"
        );
        niveauCombo.setItems(niveaux);
        niveauCombo.setStyle(STYLE_INVALIDE);
    }

    /**
     * Initialise la table des cours
     */
    private void initializeTable() {
        // Configuration des colonnes
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_cours"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Masquer la colonne ID
        idColumn.setVisible(false);

        ajouterBoutonsActions();

        // Ajouter un listener pour les changements de sélection
        coursTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                System.out.println("Cours sélectionné: " + newSelection.getTitre());
            }
        });
    }

    /**
     * Ajoute les boutons d'action dans la table
     */
    private void ajouterBoutonsActions() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifierButton = new Button("Modifier");
            private final Button supprimerButton = new Button("Supprimer");
            private final HBox pane = new HBox(10, modifierButton, supprimerButton);

            {
                modifierButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #FFA500;" +
                                "-fx-text-fill: #FFA500;" +
                                "-fx-border-radius: 20;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 5 15;" +
                                "-fx-cursor: hand;"
                );

                supprimerButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #FF4444;" +
                                "-fx-text-fill: #FF4444;" +
                                "-fx-border-radius: 20;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 5 15;" +
                                "-fx-cursor: hand;"
                );

                pane.setAlignment(Pos.CENTER);

                modifierButton.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    modifierCours(cours);
                });

                supprimerButton.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    supprimerCours(cours);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    /**
     * Charge les données des cours dans la table
     */
    private void loadCoursData() {
        try {
            List<Cours> cours = coursServices.getAll();
            if (cours != null) {
                coursList = FXCollections.observableArrayList(cours);
                coursTable.setItems(coursList);
                System.out.println("Données chargées: " + cours.size() + " cours");
            } else {
                System.err.println("Aucune donnée reçue du service");
                coursList = FXCollections.observableArrayList();
                coursTable.setItems(coursList);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
            coursList = FXCollections.observableArrayList();
            coursTable.setItems(coursList);
        }
    }

    /**
     * Configure les actions des boutons
     */
    private void configureActions() {
        ajouterButton.setOnAction(event -> {
            if (coursEnModification != null) {
                modifierCoursExistant();
            } else {
                ajouterCours();
            }
        });

        annulerButton.setOnAction(event -> annuler());

        if (parcourirImageButton != null) {
            parcourirImageButton.setOnAction(event -> choisirImageCours());
        }

        if (voirCoursButton != null) {
            voirCoursButton.setOnAction(event -> {
                System.out.println("👁 Navigation vers l'affichage des cours...");
                Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            });
        }
    }

    /**
     * Configure la validation en temps réel pour tous les champs
     */
    private void setupValidation() {
        // Validation du titre
        titreField.textProperty().addListener((obs, old, newValue) -> {
            validerTitre();
        });
        titreField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerTitre();
            }
        });

        // Validation du type de cours
        typeCoursCombo.valueProperty().addListener((obs, old, newValue) -> {
            validerTypeCours();
        });

        // Validation du niveau
        niveauCombo.valueProperty().addListener((obs, old, newValue) -> {
            validerNiveau();
        });

        // Validation de la durée
        dureeField.textProperty().addListener((obs, old, newValue) -> {
            validerDuree();
        });
        dureeField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerDuree();
            }
        });

        // Validation de la description
        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            validerDescription();
        });
        descriptionArea.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                validerDescription();
            }
        });

        // Validation de l'image
        imageField.textProperty().addListener((obs, old, newValue) -> {
            validerImage();
        });

        // Validation des mots
        motsField.textProperty().addListener((obs, old, newValue) -> {
            validerMots();
        });
    }

    /**
     * Valide le champ titre
     */
    private boolean validerTitre() {
        String titre = titreField.getText();
        if (titre == null || titre.trim().isEmpty()) {
            titreField.setStyle(STYLE_INVALIDE);
            showError(titreField, "Le titre est obligatoire");
            return false;
        } else if (titre.length() < 3) {
            titreField.setStyle(STYLE_INVALIDE);
            showError(titreField, "Le titre doit contenir au moins 3 caractères");
            return false;
        } else if (titre.length() > 100) {
            titreField.setStyle(STYLE_INVALIDE);
            showError(titreField, "Le titre ne doit pas dépasser 100 caractères");
            return false;
        } else {
            titreField.setStyle(STYLE_VALIDE);
            hideError(titreField);
            return true;
        }
    }

    /**
     * Valide le type de cours
     */
    private boolean validerTypeCours() {
        String type = typeCoursCombo.getValue();
        if (type == null || type.isEmpty()) {
            typeCoursCombo.setStyle(STYLE_INVALIDE);
            showError(typeCoursCombo, "Veuillez sélectionner un type de cours");
            return false;
        } else {
            typeCoursCombo.setStyle(STYLE_VALIDE);
            hideError(typeCoursCombo);
            return true;
        }
    }

    /**
     * Valide le niveau
     */
    private boolean validerNiveau() {
        String niveau = niveauCombo.getValue();
        if (niveau == null || niveau.isEmpty()) {
            niveauCombo.setStyle(STYLE_INVALIDE);
            showError(niveauCombo, "Veuillez sélectionner un niveau");
            return false;
        } else {
            niveauCombo.setStyle(STYLE_VALIDE);
            hideError(niveauCombo);
            return true;
        }
    }

    /**
     * Valide la durée
     */
    private boolean validerDuree() {
        String dureeText = dureeField.getText();
        if (dureeText == null || dureeText.trim().isEmpty()) {
            dureeField.setStyle(STYLE_INVALIDE);
            showError(dureeField, "La durée est obligatoire");
            return false;
        }

        try {
            int duree = Integer.parseInt(dureeText.trim());
            if (duree <= 0) {
                dureeField.setStyle(STYLE_INVALIDE);
                showError(dureeField, "La durée doit être supérieure à 0");
                return false;
            } else if (duree > 480) {
                dureeField.setStyle(STYLE_INVALIDE);
                showError(dureeField, "La durée ne doit pas dépasser 480 minutes (8 heures)");
                return false;
            } else {
                dureeField.setStyle(STYLE_VALIDE);
                hideError(dureeField);
                return true;
            }
        } catch (NumberFormatException e) {
            dureeField.setStyle(STYLE_INVALIDE);
            showError(dureeField, "La durée doit être un nombre entier");
            return false;
        }
    }

    /**
     * Valide la description
     */
    private boolean validerDescription() {
        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            descriptionArea.setStyle(STYLE_INVALIDE);
            showError(descriptionArea, "La description est obligatoire");
            return false;
        } else if (description.length() < 10) {
            descriptionArea.setStyle(STYLE_INVALIDE);
            showError(descriptionArea, "La description doit contenir au moins 10 caractères");
            return false;
        } else if (description.length() > 1000) {
            descriptionArea.setStyle(STYLE_INVALIDE);
            showError(descriptionArea, "La description ne doit pas dépasser 1000 caractères");
            return false;
        } else {
            descriptionArea.setStyle(STYLE_VALIDE);
            hideError(descriptionArea);
            return true;
        }
    }

    /**
     * Valide l'image (obligatoire)
     */
    private boolean validerImage() {
        String image = imageField.getText();
        if (image == null || image.trim().isEmpty()) {
            imageField.setStyle(STYLE_INVALIDE);
            showError(imageField, "L'image est obligatoire");
            return false;
        }

        String lowercase = image.toLowerCase();
        if (lowercase.endsWith(".png") || lowercase.endsWith(".jpg") ||
                lowercase.endsWith(".jpeg") || lowercase.endsWith(".gif") ||
                lowercase.endsWith(".bmp")) {
            imageField.setStyle(STYLE_VALIDE);
            hideError(imageField);
            return true;
        } else {
            imageField.setStyle(STYLE_INVALIDE);
            showError(imageField, "Format d'image non supporté (utilisez PNG, JPG, JPEG, GIF ou BMP)");
            return false;
        }
    }

    /**
     * Valide les mots du cours
     */
    private boolean validerMots() {
        String mots = motsField.getText();
        if (mots == null || mots.trim().isEmpty()) {
            motsField.setStyle(STYLE_INVALIDE);
            showError(motsField, "Les mots sont obligatoires");
            return false;
        }

        String[] motsArray = mots.split(";");
        boolean valide = true;
        StringBuilder erreurs = new StringBuilder();

        for (String mot : motsArray) {
            String motTrim = mot.trim();
            if (!motTrim.isEmpty()) {
                if (motTrim.length() < 2) {
                    valide = false;
                    erreurs.append("Le mot '").append(motTrim).append("' est trop court (min 2 caractères). ");
                } else if (motTrim.length() > 50) {
                    valide = false;
                    erreurs.append("Le mot '").append(motTrim).append("' est trop long (max 50 caractères). ");
                }
            }
        }

        if (valide) {
            motsField.setStyle(STYLE_VALIDE);
            hideError(motsField);
        } else {
            motsField.setStyle(STYLE_INVALIDE);
            showError(motsField, erreurs.toString());
        }

        return valide;
    }

    /**
     * Valide toutes les lignes d'images pour les mots
     */
    private boolean validerImagesMots() {
        boolean toutesValides = true;

        if (imagesMotLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez ajouter des images pour chaque mot en cliquant sur 'Ajouter des images pour un mot'");
            return false;
        }

        for (ImageMotLigne ligne : imagesMotLignes) {
            String images = ligne.getImages();
            if (images == null || images.trim().isEmpty()) {
                toutesValides = false;
                ligne.getImagesField().setStyle(STYLE_INVALIDE);
                ligne.showError("Au moins une image est requise pour ce mot");
            } else {
                String[] imagesArray = images.split(";");
                boolean imagesValides = true;
                for (String img : imagesArray) {
                    String imgTrim = img.trim();
                    if (!imgTrim.isEmpty()) {
                        String lowercase = imgTrim.toLowerCase();
                        if (!(lowercase.endsWith(".png") || lowercase.endsWith(".jpg") ||
                                lowercase.endsWith(".jpeg") || lowercase.endsWith(".gif"))) {
                            imagesValides = false;
                            break;
                        }
                    }
                }

                if (imagesValides && imagesArray.length > 0) {
                    ligne.getImagesField().setStyle(STYLE_VALIDE);
                    ligne.hideError();
                } else {
                    toutesValides = false;
                    ligne.getImagesField().setStyle(STYLE_INVALIDE);
                    ligne.showError("Formats d'image non supportés (PNG, JPG, JPEG, GIF uniquement)");
                }
            }
        }

        return toutesValides;
    }

    /**
     * Valide un champ d'image pour un mot spécifique
     */
    private void validerChampImageMot(ImageMotLigne ligne) {
        String images = ligne.getImages();
        if (images == null || images.trim().isEmpty()) {
            ligne.getImagesField().setStyle(STYLE_INVALIDE);
            ligne.showError("Au moins une image est requise pour ce mot");
        } else {
            String[] imagesArray = images.split(";");
            boolean valide = true;
            for (String img : imagesArray) {
                String imgTrim = img.trim();
                if (!imgTrim.isEmpty()) {
                    String lowercase = imgTrim.toLowerCase();
                    if (!(lowercase.endsWith(".png") || lowercase.endsWith(".jpg") ||
                            lowercase.endsWith(".jpeg") || lowercase.endsWith(".gif"))) {
                        valide = false;
                        break;
                    }
                }
            }

            if (valide && imagesArray.length > 0) {
                ligne.getImagesField().setStyle(STYLE_VALIDE);
                ligne.hideError();
            } else {
                ligne.getImagesField().setStyle(STYLE_INVALIDE);
                ligne.showError("Formats d'image non supportés (PNG, JPG, JPEG, GIF uniquement)");
            }
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateAllFields() {
        boolean titreValide = validerTitre();
        boolean typeValide = validerTypeCours();
        boolean niveauValide = validerNiveau();
        boolean dureeValide = validerDuree();
        boolean descriptionValide = validerDescription();
        boolean imageValide = validerImage();
        boolean motsValide = validerMots();
        boolean imagesMotsValide = validerImagesMots();

        return titreValide && typeValide && niveauValide && dureeValide &&
                descriptionValide && imageValide && motsValide && imagesMotsValide;
    }

    /**
     * Ajoute un nouveau cours
     */
    @FXML
    private void ajouterCours() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire (champs en rouge)");
            return;
        }

        try {
            String imagesMotsString = construireImagesMotsString();

            Cours nouveauCours = new Cours(
                    titreField.getText(),
                    descriptionArea.getText(),
                    typeCoursCombo.getValue(),
                    niveauCombo.getValue(),
                    Integer.parseInt(dureeField.getText()),
                    imageField.getText(),
                    motsField.getText(),
                    imagesMotsString
            );

            boolean success = coursServices.ajouter(nouveauCours);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté avec succès!");
                clearFields();
                refreshTable(); // Rafraîchir la table après ajout

                // Navigation optionnelle (vous pouvez commenter si vous voulez rester sur la même page)
                // Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du cours.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La durée doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Modifie un cours existant
     */
    private void modifierCoursExistant() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire (champs en rouge)");
            return;
        }

        try {
            String imagesMotsString = construireImagesMotsString();

            coursEnModification.setTitre(titreField.getText());
            coursEnModification.setDescription(descriptionArea.getText());
            coursEnModification.setType_cours(typeCoursCombo.getValue());
            coursEnModification.setNiveau(niveauCombo.getValue());
            coursEnModification.setDuree(Integer.parseInt(dureeField.getText()));
            coursEnModification.setImage(imageField.getText());
            coursEnModification.setMots(motsField.getText());
            coursEnModification.setImages_mots(imagesMotsString);

            boolean success = coursServices.modifier(coursEnModification);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours modifié avec succès!");

                coursEnModification = null;
                ajouterButton.setText("Ajouter le cours");

                clearFields();
                refreshTable(); // Rafraîchir la table après modification

                System.out.println("Modification réussie, table rafraîchie");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification du cours.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La durée doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Construit la chaîne des images de mots
     */
    private String construireImagesMotsString() {
        StringBuilder imagesMotsBuilder = new StringBuilder();
        List<String> motsList = new ArrayList<>();

        String[] mots = motsField.getText().split(";");
        for (String mot : mots) {
            if (!mot.trim().isEmpty()) {
                motsList.add(mot.trim());
            }
        }

        for (String mot : motsList) {
            boolean imageTrouvee = false;
            for (ImageMotLigne ligne : imagesMotLignes) {
                if (ligne.getMot().equals(mot)) {
                    if (imagesMotsBuilder.length() > 0) {
                        imagesMotsBuilder.append(";");
                    }
                    imagesMotsBuilder.append(ligne.getImages());
                    imageTrouvee = true;
                    break;
                }
            }
            if (!imageTrouvee) {
                if (imagesMotsBuilder.length() > 0) {
                    imagesMotsBuilder.append(";");
                }
                imagesMotsBuilder.append("");
            }
        }

        return imagesMotsBuilder.toString();
    }

    /**
     * Annule la saisie en cours
     */
    @FXML
    private void annuler() {
        if (coursEnModification != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Annuler la modification");
            alert.setContentText("Voulez-vous vraiment annuler la modification en cours ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                clearFields();
                coursEnModification = null;
                ajouterButton.setText("Ajouter le cours");
            }
        } else {
            clearFields();
        }
    }

    /**
     * Vide tous les champs du formulaire
     */
    private void clearFields() {
        titreField.clear();
        titreField.setStyle(STYLE_INVALIDE);
        hideError(titreField);

        typeCoursCombo.setValue(null);
        typeCoursCombo.setStyle(STYLE_INVALIDE);
        hideError(typeCoursCombo);

        niveauCombo.setValue(null);
        niveauCombo.setStyle(STYLE_INVALIDE);
        hideError(niveauCombo);

        dureeField.clear();
        dureeField.setStyle(STYLE_INVALIDE);
        hideError(dureeField);

        descriptionArea.clear();
        descriptionArea.setStyle(STYLE_INVALIDE);
        hideError(descriptionArea);

        imageField.clear();
        imageField.setStyle(STYLE_INVALIDE);
        hideError(imageField);

        motsField.clear();
        motsField.setStyle(STYLE_INVALIDE);
        hideError(motsField);

        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();
    }

    /**
     * Prépare le formulaire pour la modification d'un cours
     */
    private void modifierCours(Cours cours) {
        remplirFormulairePourModification(cours);
        ajouterButton.setText("Modifier le cours");
        coursEnModification = cours;
    }

    /**
     * Supprime un cours
     */
    private void supprimerCours(Cours cours) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le cours");
        alert.setContentText("Voulez-vous vraiment supprimer le cours \"" + cours.getTitre() + "\" ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = coursServices.supprimer(cours.getId_cours());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé avec succès!");
                    refreshTable(); // Rafraîchir la table après suppression

                    if (coursEnModification != null && coursEnModification.getId_cours() == cours.getId_cours()) {
                        clearFields();
                        coursEnModification = null;
                        ajouterButton.setText("Ajouter le cours");
                    }

                    System.out.println("Suppression réussie, table rafraîchie");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression du cours.");
                }
            }
        });
    }

    /**
     * Remplit le formulaire avec les données d'un cours pour modification
     */
    private void remplirFormulairePourModification(Cours cours) {
        titreField.setText(cours.getTitre());
        validerTitre();

        typeCoursCombo.setValue(cours.getType_cours());
        validerTypeCours();

        niveauCombo.setValue(cours.getNiveau());
        validerNiveau();

        dureeField.setText(String.valueOf(cours.getDuree()));
        validerDuree();

        descriptionArea.setText(cours.getDescription());
        validerDescription();

        imageField.setText(cours.getImage());
        validerImage();

        motsField.setText(cours.getMots());
        validerMots();

        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();

        if (cours.getMots() != null && cours.getImages_mots() != null) {
            String[] mots = cours.getMots().split(";");
            String[] images = cours.getImages_mots().split(";");

            for (int i = 0; i < mots.length; i++) {
                String mot = mots[i].trim();
                if (!mot.isEmpty()) {
                    ImageMotLigne ligne = new ImageMotLigne(mot);
                    if (i < images.length) {
                        ligne.setImages(images[i].trim());
                    }
                    imagesMotLignes.add(ligne);
                    imagesMotsContainer.getChildren().add(ligne.getLigneContainer());

                    validerChampImageMot(ligne);
                }
            }
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}