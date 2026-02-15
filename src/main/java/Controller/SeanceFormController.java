package Controller;

import Entites.Seance;
import Services.SeanceServices;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SeanceFormController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> spinnerHeure;
    @FXML private Spinner<Integer> spinnerMinute;
    @FXML private ComboBox<String> comboJour;
    @FXML private Spinner<Integer> spinnerDuree;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextField txtIdAutiste;
    @FXML private TextField txtIdProfesseur;
    @FXML private TextField txtIdCours;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Individual error labels
    @FXML private Label errorTitre;
    @FXML private Label errorDescription;
    @FXML private Label errorDate;
    @FXML private Label errorJour;
    @FXML private Label errorIdAutiste;
    @FXML private Label errorIdProfesseur;
    @FXML private Label errorIdCours;

    private SeanceServices seanceServices = new SeanceServices();
    private Seance seanceToEdit;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupSpinners();
        setupComboBoxes();
        setupValidation();
    }

    private void setupSpinners() {
        spinnerHeure.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        spinnerMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        spinnerDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 300, 60, 15));
    }

    private void setupComboBoxes() {
        comboJour.getItems().addAll("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche");
        comboStatut.getItems().addAll("planifiee", "confirme", "annule", "reporte", "termine");
        comboStatut.getSelectionModel().selectFirst();
    }

    private void setupValidation() {
        // Real-time validation only for numeric fields
        txtIdAutiste.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdAutiste);
        });
        txtIdProfesseur.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdProfesseur);
        });
        txtIdCours.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdCours);
        });

        // Validation en temps réel pour la date
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                LocalDate today = LocalDate.now();
                if (newDate.isBefore(today)) {
                    showError(errorDate, "La date doit être aujourd'hui ou une date future", datePicker);
                } else {
                    hideError(errorDate);
                }
            }
        });

        // Enable save button always
        btnSave.setDisable(false);
    }

    private void validateNumericField(TextField field) {
        String text = field.getText();
        if (!text.isEmpty() && !text.matches("\\d+")) {
            field.setText(text.replaceAll("[^\\d]", ""));
        }
    }

    private boolean validateWithErrors() {
        boolean isValid = true;

        // Clear all errors first
        hideError(errorTitre);
        hideError(errorDescription);
        hideError(errorDate);
        hideError(errorJour);
        hideError(errorIdAutiste);
        hideError(errorIdProfesseur);
        hideError(errorIdCours);


        if (txtTitre.getText().trim().isEmpty()) {
            showError(errorTitre, "Le titre est requis", txtTitre);
            isValid = false;
        } else if (txtTitre.getText().trim().length() < 3) {
            showError(errorTitre, "Le titre doit contenir au moins 3 caractères", txtTitre);
            isValid = false;
        }

        if (txtDescription.getText().trim().isEmpty()) {
            showError(errorDescription, "La description est requise", txtDescription);
            isValid = false;
        }

        if (datePicker.getValue() == null) {
            showError(errorDate, "La date est requise", datePicker);
            isValid = false;
        } else {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();

            if (selectedDate.isBefore(today)) {
                showError(errorDate, "La date doit être aujourd'hui ou une date future", datePicker);
                isValid = false;
            }
        }

        if (comboJour.getValue() == null) {
            showError(errorJour, "Le jour est requis", comboJour);
            isValid = false;
        }

        if (txtIdAutiste.getText().trim().isEmpty()) {
            showError(errorIdAutiste, "L'ID autiste est requis", txtIdAutiste);
            isValid = false;
        }

        if (txtIdProfesseur.getText().trim().isEmpty()) {
            showError(errorIdProfesseur, "L'ID professeur est requis", txtIdProfesseur);
            isValid = false;
        }

        if (txtIdCours.getText().trim().isEmpty()) {
            showError(errorIdCours, "L'ID cours est requis", txtIdCours);
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message, javafx.scene.Node field) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);

        // Add error style to field
        field.getStyleClass().add("error-shake");

        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), field);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> {
            field.setTranslateX(0);
            field.getStyleClass().remove("error-shake");
        });
        shake.play();
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
    }

    public void setSeanceToEdit(Seance seance) {
        this.seanceToEdit = seance;
        if (seance != null) {
            txtTitre.setText(seance.getTitreSeance());
            txtDescription.setText(seance.getDescription());
            datePicker.setValue(seance.getDateSeance().toLocalDate());
            spinnerHeure.getValueFactory().setValue(seance.getDateSeance().getHour());
            spinnerMinute.getValueFactory().setValue(seance.getDateSeance().getMinute());
            comboJour.setValue(seance.getJoursSemaine());
            spinnerDuree.getValueFactory().setValue(seance.getDuree());
            comboStatut.setValue(seance.getStatutSeance());
            txtIdAutiste.setText(String.valueOf(seance.getIdAutiste()));
            txtIdProfesseur.setText(String.valueOf(seance.getIdProfesseur()));
            txtIdCours.setText(String.valueOf(seance.getIdCours()));
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        if (!validateWithErrors()) {
            return;
        }

        try {
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.of(spinnerHeure.getValue(), spinnerMinute.getValue());
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            if (seanceToEdit == null) {
                // Add new seance
                Seance newSeance = new Seance(
                        txtTitre.getText().trim(),
                        txtDescription.getText().trim(),
                        dateTime,
                        comboJour.getValue(),
                        spinnerDuree.getValue(),
                        comboStatut.getValue(),
                        Integer.parseInt(txtIdAutiste.getText().trim()),
                        Integer.parseInt(txtIdProfesseur.getText().trim()),
                        Integer.parseInt(txtIdCours.getText().trim())
                );
                seanceServices.ajouterSeance(newSeance);
                showSuccess("Séance ajoutée avec succès!");
            } else {
                // Update existing seance
                seanceToEdit.setTitreSeance(txtTitre.getText().trim());
                seanceToEdit.setDescription(txtDescription.getText().trim());
                seanceToEdit.setDateSeance(dateTime);
                seanceToEdit.setJoursSemaine(comboJour.getValue());
                seanceToEdit.setDuree(spinnerDuree.getValue());
                seanceToEdit.setStatutSeance(comboStatut.getValue());
                seanceToEdit.setIdAutiste(Integer.parseInt(txtIdAutiste.getText().trim()));
                seanceToEdit.setIdProfesseur(Integer.parseInt(txtIdProfesseur.getText().trim()));
                seanceToEdit.setIdCours(Integer.parseInt(txtIdCours.getText().trim()));

                seanceServices.modifierSeance(seanceToEdit);
                showSuccess("Séance modifiée avec succès!");
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeWindow();
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

