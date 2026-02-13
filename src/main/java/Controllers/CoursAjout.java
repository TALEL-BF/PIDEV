package Controllers;

import Entites.Cours;
import Services.CoursServices;
import Utils.Navigation;  // ← AJOUTER CET IMPORT
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
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

    // NOUVEAUX CHAMPS
    @FXML
    private TextArea motsField;
    @FXML
    private TextArea imagesMotsField;
    @FXML
    private Button parcourirImageButton;

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

    // NOUVEAU BOUTON
    @FXML
    private Button voirCoursButton;  // ← AJOUTER CETTE LIGNE

    private CoursServices coursServices;
    private ObservableList<Cours> coursList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();

        initializeComboBoxes();
        initializeTable();
        loadCoursData();
        configureActions();  // Cette méthode sera modifiée
        setupParcourirButton();
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

    // 🔴 MODIFICATION ICI : Ajouter l'action pour le bouton "Voir les cours"
    private void configureActions() {
        ajouterButton.setOnAction(event -> ajouterCours());
        annulerButton.setOnAction(event -> annuler());

        // Action pour le bouton "Voir les cours"
        if (voirCoursButton != null) {
            voirCoursButton.setOnAction(event -> {
                System.out.println("👁 Navigation vers l'affichage des cours...");
                Navigation.navigateTo("coursaffichage.fxml", "Liste des cours");
            });
        }
    }

    private void setupParcourirButton() {
        if (parcourirImageButton != null) {
            parcourirImageButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choisir une image");

                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
                fileChooser.getExtensionFilters().add(extFilter);

                File selectedFile = fileChooser.showOpenDialog(null);

                if (selectedFile != null) {
                    imageField.setText(selectedFile.getName());
                }
            });
        }
    }

    @FXML
    private void ajouterCours() {
        if (!validateFields()) {
            return;
        }

        try {
            // Créer un nouveau cours avec les 2 nouveaux champs
            Cours nouveauCours = new Cours(
                    titreField.getText(),
                    descriptionArea.getText(),
                    typeCoursCombo.getValue(),
                    niveauCombo.getValue(),
                    Integer.parseInt(dureeField.getText()),
                    imageField.getText(),
                    motsField.getText(),
                    imagesMotsField.getText()
            );

            boolean success = coursServices.ajouter(nouveauCours);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté avec succès!");
                clearFields();
                loadCoursData();
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
        imagesMotsField.clear();
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
        imagesMotsField.setText(cours.getImages_mots());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}