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

    // NOUVEAUX CHAMPS POUR LES MOTS AVEC IMAGES MULTIPLES
    @FXML
    private TextArea motsField;
    @FXML
    private VBox imagesMotsContainer;  // Conteneur pour les lignes d'images
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

    // Liste pour stocker les lignes d'images
    private List<ImageMotLigne> imagesMotLignes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();

        initializeComboBoxes();
        initializeTable();
        loadCoursData();
        configureActions();

        // NE PAS ajouter de ligne par défaut
        // Les lignes seront ajoutées uniquement quand l'utilisateur clique sur le bouton

        // Configurer le bouton pour ajouter des lignes
        ajouterLigneImageButton.setOnAction(e -> ajouterNouvelleLigneImage());

        // Ajouter un listener pour mettre à jour les lignes quand les mots changent
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

    // Nouvelle méthode pour mettre à jour les lignes d'images quand les mots changent
    private void mettreAJourLignesImages() {
        String motsText = motsField.getText();
        String[] mots = motsText.split(";");

        // Créer une liste des mots actuels
        List<String> motsActuels = new ArrayList<>();
        for (String mot : mots) {
            if (!mot.trim().isEmpty()) {
                motsActuels.add(mot.trim());
            }
        }

        // Supprimer les lignes pour les mots qui n'existent plus
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

        // Ajouter des lignes pour les nouveaux mots
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
            // Au lieu d'afficher une alerte, on crée une ligne avec un champ vide
            // que l'utilisateur pourra remplir manuellement
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
            // Utiliser les mots existants
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
                "Présentiel", "En ligne", "Hybride", "Vidéo", "PDF"
        );
        typeCoursCombo.setItems(types);

        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "Débutant", "Intermédiaire", "Avancé"
        );
        niveauCombo.setItems(niveaux);
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_cours"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_cours"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

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
                                "-fx-padding: 5 15;"
                );

                supprimerButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #FF4444;" +
                                "-fx-text-fill: #FF4444;" +
                                "-fx-border-radius: 20;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 5 15;"
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
        ajouterButton.setOnAction(event -> ajouterCours());
        annulerButton.setOnAction(event -> annuler());

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
            // Construire la chaîne des images de mots à partir des lignes
            StringBuilder imagesMotsBuilder = new StringBuilder();
            List<String> motsList = new ArrayList<>();

            // Récupérer les mots du champ motsField
            String[] mots = motsField.getText().split(";");
            for (String mot : mots) {
                if (!mot.trim().isEmpty()) {
                    motsList.add(mot.trim());
                }
            }

            // Construire la chaîne d'images dans l'ordre des mots
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

            Cours nouveauCours = new Cours(
                    titreField.getText(),
                    descriptionArea.getText(),
                    typeCoursCombo.getValue(),
                    niveauCombo.getValue(),
                    Integer.parseInt(dureeField.getText()),
                    imageField.getText(),
                    motsField.getText(),
                    imagesMotsBuilder.toString()
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
        // Les mots ne sont plus obligatoires
        return true;
    }

    @FXML
    private void annuler() {
        clearFields();
    }

    private void clearFields() {
        titreField.clear();
        typeCoursCombo.setValue(null);
        niveauCombo.setValue(null);
        dureeField.clear();
        descriptionArea.clear();
        imageField.clear();
        motsField.clear();

        // Effacer les lignes d'images
        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();
    }

    private void modifierCours(Cours cours) {
        remplirFormulairePourModification(cours);
        ajouterButton.setText("Modifier");
        showAlert(Alert.AlertType.INFORMATION, "Modification",
                "Fonctionnalité de modification en cours de développement");
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

        // Reconstruire les lignes d'images
        imagesMotLignes.clear();
        imagesMotsContainer.getChildren().clear();

        if (cours.getMots() != null && cours.getImages_mots() != null) {
            String[] mots = cours.getMots().split(";");
            String[] images = cours.getImages_mots().split(";");

            for (int i = 0; i < mots.length; i++) {
                if (i < mots.length) {
                    ImageMotLigne ligne = new ImageMotLigne(mots[i].trim());
                    if (i < images.length) {
                        ligne.setImages(images[i].trim());
                    }
                    imagesMotLignes.add(ligne);
                    imagesMotsContainer.getChildren().add(ligne.getLigne());
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