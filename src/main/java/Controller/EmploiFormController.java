package Controller;

import Entites.EmploiDuTemps;
import Services.EmploiDuTempsServices;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EmploiFormController {

    @FXML private TextField txtAnneeScolaire;
    @FXML private ComboBox<String> comboJour;
    @FXML private ComboBox<String> comboTranche;
    @FXML private TextField txtIdRdv;
    @FXML private TextField txtIdSeance;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Error labels
    @FXML private Label errorAnneeScolaire;
    @FXML private Label errorJour;
    @FXML private Label errorTranche;
    @FXML private Label errorActivite;

    private EmploiDuTempsServices emploiServices = new EmploiDuTempsServices();
    private EmploiDuTemps emploiToEdit;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupValidation();
    }

    private void setupComboBoxes() {
        comboJour.getItems().addAll("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche");
        comboTranche.getItems().addAll("matin", "apres_midi", "soir", "journee");
    }

    private void setupValidation() {
        // Real-time validation only for numeric fields and mutual exclusivity
        txtIdRdv.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdRdv);
            // Clear the other field if this one is filled
            if (!newVal.trim().isEmpty() && !txtIdSeance.getText().trim().isEmpty()) {
                txtIdSeance.clear();
            }
        });
        txtIdSeance.textProperty().addListener((obs, old, newVal) -> {
            validateNumericField(txtIdSeance);
            // Clear the other field if this one is filled
            if (!newVal.trim().isEmpty() && !txtIdRdv.getText().trim().isEmpty()) {
                txtIdRdv.clear();
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

    private boolean isValidAnneeScolaire(String annee) {
        // Format: YYYY-YYYY (e.g., 2024-2025)
        if (!annee.matches("\\d{4}-\\d{4}")) {
            return false;
        }

        String[] parts = annee.split("-");
        try {
            int firstYear = Integer.parseInt(parts[0]);
            int secondYear = Integer.parseInt(parts[1]);

            // Second year should be first year + 1
            if (secondYear != firstYear + 1) {
                return false;
            }

            // Years should be reasonable (e.g., between 2025 and 2100)
            if (firstYear < 2025 || firstYear > 2100) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateWithErrors() {
        boolean isValid = true;

        // Clear all errors first
        hideError(errorAnneeScolaire);
        hideError(errorJour);
        hideError(errorTranche);
        hideError(errorActivite);


        if (txtAnneeScolaire.getText().trim().isEmpty()) {
            showError(errorAnneeScolaire, "L'année scolaire est requise (format: 2025-2026)", txtAnneeScolaire);
            isValid = false;
        } else if (!isValidAnneeScolaire(txtAnneeScolaire.getText().trim())) {
            showError(errorAnneeScolaire, "Format invalide. Utilisez: ANNÉE-ANNÉE+1 (ex: 2025-2026)", txtAnneeScolaire);
            isValid = false;
        }

        if (comboJour.getValue() == null) {
            showError(errorJour, "Le jour de la semaine est requis", comboJour);
            isValid = false;
        }

        if (comboTranche.getValue() == null) {
            showError(errorTranche, "La tranche horaire est requise", comboTranche);
            isValid = false;
        }

        // Check mutual exclusivity and at least one activity
        boolean hasRdv = !txtIdRdv.getText().trim().isEmpty();
        boolean hasSeance = !txtIdSeance.getText().trim().isEmpty();

        if (!hasRdv && !hasSeance) {
            showError(errorActivite, "Au moins une activité (RDV ou Séance) est requise", txtIdRdv);
            isValid = false;
        } else if (hasRdv && hasSeance) {
            showError(errorActivite, "Vous ne pouvez pas spécifier à la fois un RDV ET une Séance. Choisissez-en un seul.", txtIdRdv);
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

    public void setEmploiToEdit(EmploiDuTemps emploi) {
        this.emploiToEdit = emploi;
        if (emploi != null) {
            txtAnneeScolaire.setText(emploi.getAnneeScolaire());
            comboJour.setValue(emploi.getJourSemaine());
            comboTranche.setValue(emploi.getTrancheHoraire());
            if (emploi.getIdRdv() != null) {
                txtIdRdv.setText(String.valueOf(emploi.getIdRdv()));
            }
            if (emploi.getIdSeance() != null) {
                txtIdSeance.setText(String.valueOf(emploi.getIdSeance()));
            }
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
            Integer idRdv = txtIdRdv.getText().trim().isEmpty() ? null : Integer.parseInt(txtIdRdv.getText().trim());
            Integer idSeance = txtIdSeance.getText().trim().isEmpty() ? null : Integer.parseInt(txtIdSeance.getText().trim());

            if (emploiToEdit == null) {
                EmploiDuTemps newEmploi = new EmploiDuTemps(
                        txtAnneeScolaire.getText().trim(),
                        comboJour.getValue(),
                        comboTranche.getValue(),
                        idRdv,
                        idSeance
                );
                emploiServices.ajouterEmploi(newEmploi);
                showSuccess("Emploi du temps ajouté avec succès!");
            } else {
                emploiToEdit.setAnneeScolaire(txtAnneeScolaire.getText().trim());
                emploiToEdit.setJourSemaine(comboJour.getValue());
                emploiToEdit.setTrancheHoraire(comboTranche.getValue());
                emploiToEdit.setIdRdv(idRdv);
                emploiToEdit.setIdSeance(idSeance);

                emploiServices.modifierEmploi(emploiToEdit);
                showSuccess("Emploi du temps modifié avec succès!");
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

