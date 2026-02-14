package Controller;

import Entites.RDV;
import Services.RDVServices;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RDVFormController {

    @FXML private ComboBox<String> comboType;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> spinnerHeure;
    @FXML private Spinner<Integer> spinnerMinute;
    @FXML private Spinner<Integer> spinnerDuree;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextField txtIdPsychologue;
    @FXML private TextField txtIdAutiste;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Individual error labels
    @FXML private Label errorType;
    @FXML private Label errorDate;
    @FXML private Label errorIdPsychologue;
    @FXML private Label errorIdAutiste;

    private RDVServices rdvServices = new RDVServices();
    private RDV rdvToEdit;
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
        spinnerDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 180, 45, 15));
    }

    private void setupComboBoxes() {
        comboType.getItems().addAll("premiere_consultation", "suivi", "urgence", "familiale", "bilan");
        comboStatut.getItems().addAll("planifiee", "confirme", "annule", "reporte", "termine");
        comboStatut.getSelectionModel().selectFirst();
    }

    private void setupValidation() {
        // Real-time validation only for numeric fields
        txtIdPsychologue.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdPsychologue);
        });
        txtIdAutiste.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdAutiste);
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
        hideError(errorType);
        hideError(errorDate);
        hideError(errorIdPsychologue);
        hideError(errorIdAutiste);


        if (comboType.getValue() == null) {
            showError(errorType, "Le type de consultation est requis", comboType);
            isValid = false;
        }

        if (datePicker.getValue() == null) {
            showError(errorDate, "La date est requise", datePicker);
            isValid = false;
        }

        if (txtIdPsychologue.getText().trim().isEmpty()) {
            showError(errorIdPsychologue, "L'ID psychologue est requis", txtIdPsychologue);
            isValid = false;
        }

        if (txtIdAutiste.getText().trim().isEmpty()) {
            showError(errorIdAutiste, "L'ID autiste est requis", txtIdAutiste);
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message, javafx.scene.Node field) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);

        field.getStyleClass().add("error-shake");

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

    public void setRDVToEdit(RDV rdv) {
        this.rdvToEdit = rdv;
        if (rdv != null) {
            comboType.setValue(rdv.getTypeConsultation());
            datePicker.setValue(rdv.getDateHeureRdv().toLocalDate());
            spinnerHeure.getValueFactory().setValue(rdv.getDateHeureRdv().getHour());
            spinnerMinute.getValueFactory().setValue(rdv.getDateHeureRdv().getMinute());
            spinnerDuree.getValueFactory().setValue(rdv.getDureeRdvMinutes());
            comboStatut.setValue(rdv.getStatutRdv());
            txtIdPsychologue.setText(String.valueOf(rdv.getIdPsychologue()));
            txtIdAutiste.setText(String.valueOf(rdv.getIdAutiste()));
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

            if (rdvToEdit == null) {
                RDV newRDV = new RDV(
                        comboType.getValue(),
                        dateTime,
                        comboStatut.getValue(),
                        spinnerDuree.getValue(),
                        Integer.parseInt(txtIdPsychologue.getText().trim()),
                        Integer.parseInt(txtIdAutiste.getText().trim())
                );
                rdvServices.ajouterRDV(newRDV);
                showSuccess("RDV ajouté avec succès!");
            } else {
                rdvToEdit.setTypeConsultation(comboType.getValue());
                rdvToEdit.setDateHeureRdv(dateTime);
                rdvToEdit.setStatutRdv(comboStatut.getValue());
                rdvToEdit.setDureeRdvMinutes(spinnerDuree.getValue());
                rdvToEdit.setIdPsychologue(Integer.parseInt(txtIdPsychologue.getText().trim()));
                rdvToEdit.setIdAutiste(Integer.parseInt(txtIdAutiste.getText().trim()));

                rdvServices.modifierRDV(rdvToEdit);
                showSuccess("RDV modifié avec succès!");
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

