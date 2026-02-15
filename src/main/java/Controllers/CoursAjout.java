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
import java.util.*;

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

    private Map<Control, Label> errorLabels = new HashMap<>();
    private CoursServices coursServices;
    private ObservableList<Cours> coursList;
    private List<ImageMotLigne> imagesMotLignes = new ArrayList<>();
    private Cours coursEnModification = null;

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

        javafx.application.Platform.runLater(this::addErrorLabels);

        coursTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                refreshTable();
            }
        });
    }

    private void refreshTable() {
        try {
            List<Cours> cours = coursServices.getAll();
            if (cours != null) {
                coursList = FXCollections.observableArrayList(cours);
                coursTable.setItems(coursList);
                coursTable.refresh();
                System.out.println("✅ Table rafraîchie avec " + cours.size() + " cours");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addErrorLabels() {
        addErrorLabelAfter(titreField, "Titre du cours");
        addErrorLabelAfter(typeCoursCombo, "Type de cours");
        addErrorLabelAfter(niveauCombo, "Niveau");
        addErrorLabelAfter(dureeField, "Durée (en minutes)");
        addErrorLabelAfter(descriptionArea, "Description");
        addErrorLabelAfter(imageField, "Image du cours");
        addErrorLabelAfter(motsField, "Mots du cours");
    }

    private void addErrorLabelAfter(Control field, String fieldName) {
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-wrap-text: true;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

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

        errorLabels.put(field, errorLabel);
    }

    private void showError(Control field, String message) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError(Control field) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * CLASSE INTERNE POUR GÉRER UN MOT ET SON IMAGE
     */
    private class ImageMotLigne {
        private int index;
        private Label motLabel;
        private Label imageLabel;
        private Button parcourirButton;
        private Button supprimerImageButton;
        private Label errorLabel;
        private HBox ligne;
        private VBox ligneContainer;

        private String imagePath = "";

        public ImageMotLigne(String mot, int index) {
            this.index = index;
            this.imagePath = "";

            motLabel = new Label(mot);
            motLabel.setPrefWidth(150);
            motLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

            imageLabel = new Label("Aucune image");
            imageLabel.setPrefWidth(250);
            imageLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");

            errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px;");
            errorLabel.setVisible(false);

            parcourirButton = new Button("Choisir image...");
            parcourirButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");

            supprimerImageButton = new Button("✖");
            supprimerImageButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4444; -fx-font-size: 16px; -fx-cursor: hand;");
            supprimerImageButton.setVisible(false);

            Button supprimerLigneButton = new Button("Supprimer mot");
            supprimerLigneButton.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4444; -fx-text-fill: #FF4444; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");

            ligne = new HBox(10, motLabel, imageLabel, parcourirButton, supprimerImageButton, supprimerLigneButton);
            ligne.setAlignment(Pos.CENTER_LEFT);

            ligneContainer = new VBox(5, ligne, errorLabel);
            ligneContainer.setStyle("-fx-padding: 5 0;");

            parcourirButton.setOnAction(e -> choisirImagePourMot(this));
            supprimerImageButton.setOnAction(e -> supprimerImage(this));
            supprimerLigneButton.setOnAction(e -> supprimerLigne(this));
        }

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public VBox getLigneContainer() { return ligneContainer; }
        public String getMot() { return motLabel.getText(); }
        public void setMot(String mot) { motLabel.setText(mot); }
        public String getImagePath() { return imagePath; }

        /**
         * Met à jour l'affichage de l'image
         */
        public void updateImageDisplay() {
            if (imagePath != null && !imagePath.isEmpty()) {
                imageLabel.setText(imagePath);
                imageLabel.setStyle("-fx-text-fill: #00C853; -fx-font-weight: normal; -fx-font-style: normal;");
                supprimerImageButton.setVisible(true);
                hideError();
            } else {
                imageLabel.setText("Aucune image");
                imageLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
                supprimerImageButton.setVisible(false);
            }
            // Forcer le rafraîchissement immédiat
            ligneContainer.requestLayout();
        }

        public void setImagePath(String path) {
            this.imagePath = path;
            updateImageDisplay();
        }

        public void showError(String message) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
            ligneContainer.requestLayout();
        }

        public void hideError() {
            errorLabel.setVisible(false);
            ligneContainer.requestLayout();
        }

        public boolean hasImage() {
            return imagePath != null && !imagePath.isEmpty();
        }
    }

    /**
     * Choisir une image pour un mot spécifique
     */
    private void choisirImagePourMot(ImageMotLigne ligne) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le mot: " + ligne.getMot());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            ligne.setImagePath(selectedFile.getName());
            // Forcer le rafraîchissement du conteneur parent
            imagesMotsContainer.requestLayout();
            System.out.println("✅ Image '" + selectedFile.getName() + "' associée au mot: " + ligne.getMot());
        }
    }

    /**
     * Supprimer l'image d'un mot
     */
    private void supprimerImage(ImageMotLigne ligne) {
        ligne.setImagePath("");
        imagesMotsContainer.requestLayout();
        System.out.println("🗑️ Image supprimée pour le mot: " + ligne.getMot());
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

        // Sauvegarder les images existantes
        Map<String, String> imagesExistantes = new HashMap<>();
        for (ImageMotLigne ligne : imagesMotLignes) {
            if (ligne.hasImage()) {
                imagesExistantes.put(ligne.getMot(), ligne.getImagePath());
            }
        }

        // Nettoyer
        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();

        // Créer les nouvelles lignes
        for (int i = 0; i < motsActuels.size(); i++) {
            String mot = motsActuels.get(i);

            ImageMotLigne nouvelleLigne = new ImageMotLigne(mot, i);

            if (imagesExistantes.containsKey(mot)) {
                nouvelleLigne.setImagePath(imagesExistantes.get(mot));
                System.out.println("✓ Image restaurée pour le mot: " + mot);
            }

            imagesMotLignes.add(nouvelleLigne);
            imagesMotsContainer.getChildren().add(nouvelleLigne.getLigneContainer());
        }

        // Forcer le rafraîchissement
        imagesMotsContainer.requestLayout();
    }

    /**
     * Ajoute une nouvelle ligne d'image pour un mot
     */
    private void ajouterNouvelleLigneImage() {
        String motsText = motsField.getText();
        if (motsText == null || motsText.trim().isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ajouter un mot");
            dialog.setHeaderText("Saisissez le mot pour lequel vous voulez ajouter une image");
            dialog.setContentText("Mot :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String mot = result.get().trim();
                int nouvelIndex = imagesMotLignes.size();
                ImageMotLigne nouvelleLigne = new ImageMotLigne(mot, nouvelIndex);
                imagesMotLignes.add(nouvelleLigne);
                imagesMotsContainer.getChildren().add(nouvelleLigne.getLigneContainer());

                if (motsField.getText().isEmpty()) {
                    motsField.setText(mot);
                } else {
                    motsField.setText(motsField.getText() + ";" + mot);
                }

                // Forcer le rafraîchissement
                imagesMotsContainer.requestLayout();
                nouvelleLigne.getLigneContainer().requestLayout();

                System.out.println("✚ Nouveau mot ajouté: " + mot);
            }
        }
    }

    /**
     * Supprime une ligne
     */
    private void supprimerLigne(ImageMotLigne ligne) {
        imagesMotLignes.remove(ligne);
        imagesMotsContainer.getChildren().remove(ligne.getLigneContainer());

        String[] mots = motsField.getText().split(";");
        StringBuilder newMots = new StringBuilder();
        for (String mot : mots) {
            if (!mot.trim().equals(ligne.getMot()) && !mot.trim().isEmpty()) {
                if (newMots.length() > 0) newMots.append(";");
                newMots.append(mot.trim());
            }
        }
        motsField.setText(newMots.toString());

        imagesMotsContainer.requestLayout();
        System.out.println("✖ Ligne supprimée pour le mot: " + ligne.getMot());
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

    private void initializeTable() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_cours"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        idColumn.setVisible(false);
        ajouterBoutonsActions();
    }

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
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCoursData() {
        try {
            List<Cours> cours = coursServices.getAll();
            if (cours != null) {
                coursList = FXCollections.observableArrayList(cours);
                coursTable.setItems(coursList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            });
        }
    }

    private void setupValidation() {
        titreField.textProperty().addListener((obs, old, newValue) -> validerTitre());
        typeCoursCombo.valueProperty().addListener((obs, old, newValue) -> validerTypeCours());
        niveauCombo.valueProperty().addListener((obs, old, newValue) -> validerNiveau());
        dureeField.textProperty().addListener((obs, old, newValue) -> validerDuree());
        descriptionArea.textProperty().addListener((obs, old, newValue) -> validerDescription());
        imageField.textProperty().addListener((obs, old, newValue) -> validerImage());
        motsField.textProperty().addListener((obs, old, newValue) -> validerMots());
    }

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
        } else {
            titreField.setStyle(STYLE_VALIDE);
            hideError(titreField);
            return true;
        }
    }

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
        } else {
            descriptionArea.setStyle(STYLE_VALIDE);
            hideError(descriptionArea);
            return true;
        }
    }

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
            showError(imageField, "Format d'image non supporté");
            return false;
        }
    }

    private boolean validerMots() {
        String mots = motsField.getText();
        if (mots == null || mots.trim().isEmpty()) {
            motsField.setStyle(STYLE_INVALIDE);
            showError(motsField, "Les mots sont obligatoires");
            return false;
        }
        motsField.setStyle(STYLE_VALIDE);
        hideError(motsField);
        return true;
    }

    private boolean validerImagesMots() {
        boolean toutesValides = true;

        if (imagesMotLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez ajouter des images pour chaque mot");
            return false;
        }

        for (ImageMotLigne ligne : imagesMotLignes) {
            if (!ligne.hasImage()) {
                toutesValides = false;
                ligne.showError("Une image est requise pour ce mot");
            } else {
                ligne.hideError();
            }
        }

        return toutesValides;
    }

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

    @FXML
    private void ajouterCours() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire");
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
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du cours.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierCoursExistant() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs dans le formulaire");
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
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification du cours.");
            }

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

        for (int i = 0; i < imagesMotLignes.size(); i++) {
            if (i > 0) {
                imagesMotsBuilder.append(";");
            }

            for (ImageMotLigne ligne : imagesMotLignes) {
                if (ligne.getIndex() == i) {
                    if (ligne.hasImage()) {
                        imagesMotsBuilder.append(ligne.getImagePath());
                    }
                    break;
                }
            }
        }

        String result = imagesMotsBuilder.toString();
        System.out.println("✅ Chaîne d'images construite: " + result);
        return result;
    }

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

    private void modifierCours(Cours cours) {
        remplirFormulairePourModification(cours);
        ajouterButton.setText("Modifier le cours");
        coursEnModification = cours;
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

        if (cours.getMots() != null && !cours.getMots().trim().isEmpty()) {
            String[] mots = cours.getMots().split(";");
            String[] images = null;

            if (cours.getImages_mots() != null && !cours.getImages_mots().trim().isEmpty()) {
                images = cours.getImages_mots().split(";");
                System.out.println("📸 Images chargées: " + Arrays.toString(images));
            }

            for (int i = 0; i < mots.length; i++) {
                String mot = mots[i].trim();
                if (!mot.isEmpty()) {
                    ImageMotLigne ligne = new ImageMotLigne(mot, i);

                    if (images != null && i < images.length) {
                        String imageValue = images[i].trim();
                        if (!imageValue.isEmpty() && !imageValue.equals("null")) {
                            ligne.setImagePath(imageValue);
                            System.out.println("✓ Image associée au mot '" + mot + "': " + imageValue);
                        }
                    }

                    imagesMotLignes.add(ligne);
                    imagesMotsContainer.getChildren().add(ligne.getLigneContainer());
                }
            }
        }

        // Forcer le rafraîchissement après le chargement
        javafx.application.Platform.runLater(() -> {
            imagesMotsContainer.requestLayout();
            for (ImageMotLigne ligne : imagesMotLignes) {
                ligne.getLigneContainer().requestLayout();
            }
        });
    }

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
                    refreshTable();

                    if (coursEnModification != null && coursEnModification.getId_cours() == cours.getId_cours()) {
                        clearFields();
                        coursEnModification = null;
                        ajouterButton.setText("Ajouter le cours");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression du cours.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}