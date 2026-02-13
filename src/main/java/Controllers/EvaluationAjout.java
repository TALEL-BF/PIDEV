package Controllers;

import Entites.Cours;
import Entites.Evaluation;
import Services.CoursServices;
import Services.EvaluationServices;
import Utils.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EvaluationAjout implements Initializable {

    @FXML private ComboBox<String> typeEvaluationCombo;
    @FXML private TextField scoreField;
    @FXML private ComboBox<String> niveauComprehensionCombo;
    @FXML private DatePicker dateEvaluationPicker;
    @FXML private ComboBox<Cours> coursCombo;
    @FXML private Button ajouterButton;
    @FXML private Button annulerButton;

    private EvaluationServices evaluationServices;
    private CoursServices coursServices;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evaluationServices = new EvaluationServices();
        coursServices = new CoursServices();

        initializeComboBoxes();
        loadCours();
        setupDatePicker();
    }

    private void initializeComboBoxes() {
        // Types d'évaluation
        ObservableList<String> types = FXCollections.observableArrayList(
                "Diagnostique", "Formative", "Sommative", "Continue"
        );
        typeEvaluationCombo.setItems(types);

        // Niveaux de compréhension
        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "Très faible", "Faible", "Moyen", "Bon", "Excellent"
        );
        niveauComprehensionCombo.setItems(niveaux);
    }

    private void loadCours() {
        List<Cours> coursList = coursServices.getAll();
        ObservableList<Cours> observableCours = FXCollections.observableArrayList(coursList);
        coursCombo.setItems(observableCours);

        // Comment afficher un Cours dans le ComboBox
        coursCombo.setConverter(new StringConverter<Cours>() {
            @Override
            public String toString(Cours cours) {
                return cours == null ? "" : cours.getTitre() + " (" + cours.getNiveau() + ")";
            }

            @Override
            public Cours fromString(String string) {
                return null; // Pas nécessaire
            }
        });
    }

    private void setupDatePicker() {
        dateEvaluationPicker.setValue(LocalDate.now());
        dateEvaluationPicker.setPromptText("AAAA-MM-JJ");

        // Format personnalisé
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateEvaluationPicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ?
                        LocalDate.parse(string, formatter) : null;
            }
        });
    }

    @FXML
    private void ajouterEvaluation() {
        if (!validateFields()) {
            return;
        }

        try {
            float score = Float.parseFloat(scoreField.getText());
            if (score < 0 || score > 20) {
                showAlert(Alert.AlertType.WARNING, "Validation",
                        "Le score doit être entre 0 et 20");
                return;
            }

            Evaluation evaluation = new Evaluation();
            evaluation.setType_evaluation(typeEvaluationCombo.getValue());
            evaluation.setScore(score);
            evaluation.setNiveau_comprehension(niveauComprehensionCombo.getValue());
            evaluation.setDate_evaluation(dateEvaluationPicker.getValue());
            evaluation.setCours(coursCombo.getValue());

            boolean success = evaluationServices.ajouter(evaluation);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Évaluation ajoutée avec succès !");
                Navigation.navigateTo("evaluationaffichage.fxml", "Liste des évaluations");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Échec de l'ajout de l'évaluation");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Le score doit être un nombre valide (ex: 15.5)");
        }
    }

    private boolean validateFields() {
        if (typeEvaluationCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner un type d'évaluation");
            return false;
        }
        if (scoreField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez saisir un score");
            return false;
        }
        if (niveauComprehensionCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner un niveau de compréhension");
            return false;
        }
        if (dateEvaluationPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner une date");
            return false;
        }
        if (coursCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner un cours");
            return false;
        }
        return true;
    }

    @FXML
    private void annuler() {
        Navigation.navigateTo("evaluationaffichage.fxml", "Liste des évaluations");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}