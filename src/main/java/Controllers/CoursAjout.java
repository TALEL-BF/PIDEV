package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Utils.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    private CoursServices coursServices;
    private ObservableList<Cours> coursList;

    private List<ImageMotLigne> imagesMotLignes = new ArrayList<>();

    // Variable pour suivre si on est en mode modification
    private Cours coursEnModification = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();

        initializeComboBoxes();
        initializeTable();
        loadCoursData();
        configureActions();

        ajouterLigneImageButton.setOnAction(e -> ajouterNouvelleLigneImage());

        motsField.textProperty().addListener((observable, oldValue, newValue) -> {
            mettreAJourLignesImages();
        });
    }

    // Classe interne pour gérer une ligne de sélection d'images pour un mot
    private class ImageMotLigne {
        private Label motLabel;
        private TextField imagesField;
        private Button parcourirButton;
        private HBox ligne;

        public ImageMotLigne(String mot) {
            motLabel = new Label(mot);
            motLabel.setPrefWidth(150);
            motLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7B2FF7;");

            imagesField = new TextField();
            imagesField.setPromptText("Images (séparées par ;)");
            imagesField.setPrefWidth(300);

            parcourirButton = new Button("Parcourir...");
            parcourirButton.setStyle("-fx-background-color: #7B2FF7; -fx-text-fill: white; -fx-background-radius: 10;");

            Button supprimerButton = new Button("✖");
            supprimerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4444; -fx-font-size: 16px; -fx-cursor: hand;");

            ligne = new HBox(10, motLabel, imagesField, parcourirButton, supprimerButton);
            ligne.setAlignment(Pos.CENTER_LEFT);

            parcourirButton.setOnAction(e -> choisirImagesMultiples(imagesField));
            supprimerButton.setOnAction(e -> supprimerLigne(this));
        }

        public HBox getLigne() { return ligne; }
        public String getMot() { return motLabel.getText(); }
        public String getImages() { return imagesField.getText(); }
        public void setImages(String images) { imagesField.setText(images); }
    }

    private void mettreAJourLignesImages() {
        String motsText = motsField.getText();
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
            imagesMotsContainer.getChildren().remove(ligne.getLigne());
        }

        for (String mot : motsActuels) {
            boolean motExiste = imagesMotLignes.stream()
                    .anyMatch(ligne -> ligne.getMot().equals(mot));

            if (!motExiste) {
                ImageMotLigne nouvelleLigne = new ImageMotLigne(mot);
                imagesMotLignes.add(nouvelleLigne);
                imagesMotsContainer.getChildren().add(nouvelleLigne.getLigne());
            }
        }
    }

    private void ajouterNouvelleLigneImage() {
        String motsText = motsField.getText();
        if (motsText == null || motsText.trim().isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ajouter un mot");
            dialog.setHeaderText("Saisissez le mot pour lequel vous voulez ajouter des images");
            dialog.setContentText("Mot :");

            dialog.showAndWait().ifPresent(mot -> {
                if (!mot.trim().isEmpty()) {
                    ImageMotLigne nouvelleLigne = new ImageMotLigne(mot.trim());
                    imagesMotLignes.add(nouvelleLigne);
                    imagesMotsContainer.getChildren().add(nouvelleLigne.getLigne());
                }
            });
        } else {
            String[] mots = motsText.split(";");
            for (String mot : mots) {
                if (!mot.trim().isEmpty()) {
                    boolean motExiste = imagesMotLignes.stream()
                            .anyMatch(ligne -> ligne.getMot().equals(mot.trim()));

                    if (!motExiste) {
                        ImageMotLigne nouvelleLigne = new ImageMotLigne(mot.trim());
                        imagesMotLignes.add(nouvelleLigne);
                        imagesMotsContainer.getChildren().add(nouvelleLigne.getLigne());
                    }
                }
            }
        }
    }

    private void choisirImageCours() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le cours");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imageField.setText(selectedFile.getName());
        }
    }

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

    private void supprimerLigne(ImageMotLigne ligne) {
        imagesMotLignes.remove(ligne);
        imagesMotsContainer.getChildren().remove(ligne.getLigne());
    }

    private void initializeComboBoxes() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Académique", "Social", "Autonomie", "Créativité"
        );
        typeCoursCombo.setItems(types);

        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "Débutant", "Intermédiaire", "Avancé"
        );
        niveauCombo.setItems(niveaux);
    }

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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadCoursData() {
        coursList = FXCollections.observableArrayList(coursServices.getAll());
        coursTable.setItems(coursList);
    }

    private void configureActions() {
        ajouterButton.setOnAction(event -> {
            if (coursEnModification != null) {
                // Mode modification
                modifierCoursExistant();
            } else {
                // Mode ajout
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

    @FXML
    private void ajouterCours() {
        if (!validateFields()) {
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
                loadCoursData();

                // Navigation automatique vers l'affichage après ajout réussi
                Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du cours.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La durée doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    // Nouvelle méthode pour la modification
    private void modifierCoursExistant() {
        if (!validateFields()) {
            return;
        }

        try {
            String imagesMotsString = construireImagesMotsString();

            // Mettre à jour l'objet cours existant
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

                // Réinitialiser le mode modification
                coursEnModification = null;
                ajouterButton.setText("Ajouter le cours");

                // Recharger les données et vider le formulaire
                clearFields();
                loadCoursData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification du cours.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La durée doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour construire la chaîne des images de mots
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

    private boolean validateFields() {
        if (titreField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un titre.");
            return false;
        }
        if (typeCoursCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type de cours.");
            return false;
        }
        if (niveauCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un niveau.");
            return false;
        }
        if (dureeField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir une durée.");
            return false;
        }
        if (descriptionArea.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir une description.");
            return false;
        }
        return true;
    }

    @FXML
    private void annuler() {
        if (coursEnModification != null) {
            // Demander confirmation avant d'annuler une modification en cours
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
        typeCoursCombo.setValue(null);
        niveauCombo.setValue(null);
        dureeField.clear();
        descriptionArea.clear();
        imageField.clear();
        motsField.clear();

        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();
    }

    private void modifierCours(Cours cours) {
        // Remplir le formulaire avec les données du cours sélectionné
        remplirFormulairePourModification(cours);

        // Changer le texte du bouton
        ajouterButton.setText("Modifier le cours");

        // Stocker le cours en cours de modification
        coursEnModification = cours;

        // Faire défiler jusqu'au formulaire
        // (Optionnel : vous pouvez ajouter ici du code pour faire défiler la vue)
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
                    loadCoursData();

                    // Si on était en train de modifier ce cours, réinitialiser
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

    private void remplirFormulairePourModification(Cours cours) {
        titreField.setText(cours.getTitre());
        typeCoursCombo.setValue(cours.getType_cours());
        niveauCombo.setValue(cours.getNiveau());
        dureeField.setText(String.valueOf(cours.getDuree()));
        descriptionArea.setText(cours.getDescription());
        imageField.setText(cours.getImage());
        motsField.setText(cours.getMots());

        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();

        if (cours.getMots() != null && cours.getImages_mots() != null) {
            String[] mots = cours.getMots().split(";");
            String[] images = cours.getImages_mots().split(";");

            for (int i = 0; i < mots.length; i++) {
                if (i < mots.length) {
                    String mot = mots[i].trim();
                    if (!mot.isEmpty()) {
                        ImageMotLigne ligne = new ImageMotLigne(mot);
                        if (i < images.length) {
                            ligne.setImages(images[i].trim());
                        }
                        imagesMotLignes.add(ligne);
                        imagesMotsContainer.getChildren().add(ligne.getLigne());
                    }
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}