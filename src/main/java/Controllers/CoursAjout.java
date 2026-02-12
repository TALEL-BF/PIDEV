package Controllers;

import Entites.Cours;
import Services.CoursServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    private CoursServices coursServices;
    private ObservableList<Cours> coursList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursServices = new CoursServices();

        // Initialiser les ComboBox
        initializeComboBoxes();

        // Initialiser la table
        initializeTable();

        // Charger les données
        loadCoursData();

        // Configurer les actions
        configureActions();
    }

    private void initializeComboBoxes() {
        // Types de cours
        ObservableList<String> types = FXCollections.observableArrayList(
                "Présentiel", "En ligne", "Hybride", "Vidéo", "PDF"
        );
        typeCoursCombo.setItems(types);

        // Niveaux
        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "Débutant", "Intermédiaire", "Avancé", "Expert"
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
    }

    private void loadCoursData() {
        coursList = FXCollections.observableArrayList(coursServices.getAll());
        coursTable.setItems(coursList);
    }

    private void configureActions() {
        ajouterButton.setOnAction(event -> ajouterCours());
        annulerButton.setOnAction(event -> annuler());
    }

    @FXML
    private void ajouterCours() {
        // Validation des champs
        if (!validateFields()) {
            return;
        }

        try {
            // Créer un nouveau cours
            Cours nouveauCours = new Cours(
                    titreField.getText(),
                    descriptionArea.getText(),
                    typeCoursCombo.getValue(),
                    niveauCombo.getValue(),
                    Integer.parseInt(dureeField.getText()),
                    imageField.getText()
            );

            // Ajouter à la base de données
            boolean success = coursServices.ajouter(nouveauCours);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté avec succès!");
                clearFields();
                loadCoursData(); // Rafraîchir la table
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
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}