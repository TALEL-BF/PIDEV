package Controller;

import Entites.Sponsor;
import Services.SponsorServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Entites.Event;
import Services.EventServices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddSponsorController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField emailField;

    @FXML private TextField telephoneField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imagePathField;
    @FXML private ImageView sponsorImageView;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backButton;

    private SponsorServices sponsorServices;
    private Sponsor sponsorToEdit;
    private Runnable onSponsorAdded;
    private String imagePath;
    private EventServices eventServices;

    // Référence au dashboard controller
    private MainDashboardController dashboardController;

    // Map pour stocker les labels d'erreur
    private Map<Control, Label> errorLabels = new HashMap<>();

    // Styles pour la validation
    private static final String STYLE_TEXTE_VALIDE = "-fx-text-fill: #00C853; -fx-font-size: 11px; -fx-font-weight: bold;";
    private static final String STYLE_TEXTE_INVALIDE = "-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-font-weight: bold;";
    private static final String STYLE_TEXTE_NEUTRE = "-fx-text-fill: #6B7280; -fx-font-size: 11px;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sponsorServices = new SponsorServices();
        initializeComboBoxes();
        configureActions();
        setupValidation();

        // Ajouter les labels de validation
        javafx.application.Platform.runLater(this::addErrorLabels);
    }

    public void setDashboardController(MainDashboardController controller) {
        this.dashboardController = controller;
    }

    // ===================== VALIDATION =====================

    private void addErrorLabels() {
        addErrorLabelAfter(nomField, "Nom");
        addErrorLabelAfter(typeBox, "Type");
        addErrorLabelAfter(emailField, "Email");
        addErrorLabelAfter(telephoneField, "Téléphone");
        addErrorLabelAfter(descriptionField, "Description");
        addErrorLabelAfter(imagePathField, "Image");
    }

    private void addErrorLabelAfter(Control field, String fieldName) {
        Label errorLabel = new Label();
        errorLabel.setStyle(STYLE_TEXTE_NEUTRE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        javafx.scene.Node parent = field.getParent();
        if (parent instanceof javafx.scene.layout.VBox) {
            javafx.scene.layout.VBox vbox = (javafx.scene.layout.VBox) parent;
            int index = vbox.getChildren().indexOf(field);
            if (index >= 0 && index + 1 < vbox.getChildren().size()) {
                vbox.getChildren().add(index + 1, errorLabel);
            } else {
                vbox.getChildren().add(errorLabel);
            }
        }

        errorLabels.put(field, errorLabel);
    }

    private void updateValidationLabel(Control field, String message, boolean isValid) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            if (message == null || message.isEmpty()) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            } else {
                errorLabel.setText(message);
                if (isValid) {
                    errorLabel.setStyle(STYLE_TEXTE_VALIDE);
                } else {
                    errorLabel.setStyle(STYLE_TEXTE_INVALIDE);
                }
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        }
    }

    private void showError(Control field, String message) {
        updateValidationLabel(field, "❌ " + message, false);
    }

    private void showSuccess(Control field, String message) {
        updateValidationLabel(field, "✓ " + message, true);
    }

    private void hideValidation(Control field) {
        updateValidationLabel(field, null, false);
    }

    private void setupValidation() {
        // Validation du nom
        nomField.textProperty().addListener((obs, old, newValue) -> validerNom());
        nomField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) validerNom();
        });

        // Validation du type
        typeBox.valueProperty().addListener((obs, old, newValue) -> validerType());

        // Validation de l'email
        emailField.textProperty().addListener((obs, old, newValue) -> validerEmail());
        emailField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) validerEmail();
        });

        // Validation du téléphone
        telephoneField.textProperty().addListener((obs, old, newValue) -> validerTelephone());
        telephoneField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) validerTelephone();
        });

        // Validation de la description
        descriptionField.textProperty().addListener((obs, old, newValue) -> validerDescription());
        descriptionField.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) validerDescription();
        });

        // Validation de l'image
        imagePathField.textProperty().addListener((obs, old, newValue) -> validerImage());
    }

    private boolean validerNom() {
        String nom = nomField.getText();
        if (nom == null || nom.trim().isEmpty()) {
            showError(nomField, "Le nom est obligatoire");
            return false;
        }

        String trimmed = nom.trim();

        if (trimmed.length() < 2) {
            showError(nomField, "Le nom doit contenir au moins 2 caractères");
            return false;
        }

        // Vérifier qu'il y a au moins une lettre
        boolean hasLetter = false;
        for (char c : trimmed.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                break;
            }
        }

        if (!hasLetter) {
            showError(nomField, "Le nom doit contenir au moins une lettre");
            return false;
        }

        showSuccess(nomField, "Nom valide");
        return true;
    }

    private boolean validerType() {
        String type = typeBox.getValue();
        if (type == null || type.isEmpty()) {
            showError(typeBox, "Le type est obligatoire");
            return false;
        }
        showSuccess(typeBox, "Type valide");
        return true;
    }

    private boolean validerEmail() {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            showError(emailField, "L'email est obligatoire");
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            showError(emailField, "Format d'email invalide");
            return false;
        }
        showSuccess(emailField, "Email valide");
        return true;
    }

    private boolean validerTelephone() {
        String telephone = telephoneField.getText();
        if (telephone == null || telephone.trim().isEmpty()) {
            showError(telephoneField, "Le téléphone est obligatoire");
            return false;
        }
        if (!telephone.matches("\\d+")) {
            showError(telephoneField, "Le téléphone ne doit contenir que des chiffres");
            return false;
        }
        if (telephone.length() < 8 || telephone.length() > 15) {
            showError(telephoneField, "Le téléphone doit contenir entre 8 et 15 chiffres");
            return false;
        }
        showSuccess(telephoneField, "Téléphone valide");
        return true;
    }

    private boolean validerDescription() {
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            showError(descriptionField, "La description est obligatoire");
            return false;
        }
        if (description.trim().length() < 10) {
            showError(descriptionField, "La description doit contenir au moins 10 caractères");
            return false;
        }
        showSuccess(descriptionField, "Description valide");
        return true;
    }

    private boolean validerImage() {
        String image = imagePathField.getText();
        if (image == null || image.trim().isEmpty()) {
            showError(imagePathField, "L'image est obligatoire");
            return false;
        }

        File file = new File(image);
        if (!file.exists()) {
            showError(imagePathField, "Le fichier image n'existe pas");
            return false;
        }

        String lowercase = image.toLowerCase();
        if (lowercase.endsWith(".png") || lowercase.endsWith(".jpg") ||
                lowercase.endsWith(".jpeg") || lowercase.endsWith(".gif")) {
            showSuccess(imagePathField, "Image valide");
            return true;
        } else {
            showError(imagePathField, "Format non supporté (PNG, JPG, JPEG, GIF)");
            return false;
        }
    }

    private boolean validateAllFields() {
        boolean nomValide = validerNom();
        boolean typeValide = validerType();
        boolean emailValide = validerEmail();
        boolean telephoneValide = validerTelephone();
        boolean descriptionValide = validerDescription();
        boolean imageValide = validerImage();

        return nomValide && typeValide && emailValide &&
                telephoneValide && descriptionValide && imageValide;
    }

    // ===================== INITIALISATION =====================

    private void initializeComboBoxes() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Or", "Argent", "Bronze"

        );
        typeBox.setItems(types);
    }

    private void configureActions() {
        saveBtn.setOnAction(event -> handleSaveSponsor());
        cancelBtn.setOnAction(event -> handleCancel());
    }

    public void setOnSponsorAdded(Runnable callback) {
        this.onSponsorAdded = callback;
    }

    // ===================== GESTION IMAGE =====================

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
        if (file != null) {
            imagePath = file.getAbsolutePath();
            imagePathField.setText(imagePath);
            Image image = new Image(file.toURI().toString());
            sponsorImageView.setImage(image);
            sponsorImageView.setFitWidth(150);
            sponsorImageView.setFitHeight(150);
            sponsorImageView.setPreserveRatio(false);
            validerImage(); // Valider après sélection
        }
    }

    // ===================== SAUVEGARDE =====================

    @FXML
    private void handleSaveSponsor() {
        // Vérifier que TOUS les champs sont remplis et valides
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez remplir tous les champs correctement:\n" +
                            "- Nom (avec au moins une lettre)\n" +
                            "- Type\n" +
                            "- Email valide\n" +
                            "- Téléphone (8-15 chiffres)\n" +
                            "- Description (min 10 caractères)\n" +
                            "- Image (format supporté)");
            return;
        }

        try {
            String nom = nomField.getText().trim();
            String type = typeBox.getValue();
            String email = emailField.getText().trim();
            String telephoneText = telephoneField.getText().trim();
            String description = descriptionField.getText().trim();
            String image = imagePathField.getText().trim();

            int telephone = Integer.parseInt(telephoneText);

            if (sponsorToEdit != null) {
                updateSponsor(sponsorToEdit, nom, type, email, telephone, description, image);
            } else {
                addNewSponsor(nom, type, email, telephone, description, image);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le téléphone doit être un nombre entier!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNewSponsor(String nom, String type, String email, int telephone, String description, String image) {
        try {
            Sponsor newSponsor = new Sponsor(nom, type, email, telephone, description, image);
            sponsorServices.ajoutSponsor(newSponsor);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sponsor ajouté avec succès!");

            if (onSponsorAdded != null) {
                onSponsorAdded.run();
            }

            // Retour à la table des sponsors
            returnToSponsorTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du sponsor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSponsor(Sponsor sponsor, String nom, String type, String email, int telephone, String description, String image) {
        try {
            sponsor.setNom(nom);
            sponsor.setTypeSponsor(type);
            sponsor.setEmail(email);
            sponsor.setTelephone(telephone);
            sponsor.setDescription(description);
            sponsor.setImage(image);

            sponsorServices.modifierSponsor(sponsor);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sponsor modifié avec succès!");

            // Retour à la table des sponsors
            returnToSponsorTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification du sponsor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSponsorToEdit(Sponsor sponsor) {
        this.sponsorToEdit = sponsor;

        if (titleLabel != null) {
            titleLabel.setText("Modifier le sponsor");
        }

        nomField.setText(sponsor.getNom());
        typeBox.setValue(sponsor.getTypeSponsor());
        emailField.setText(sponsor.getEmail());
        telephoneField.setText(String.valueOf(sponsor.getTelephone()));
        descriptionField.setText(sponsor.getDescription());

        if (sponsor.getImage() != null && !sponsor.getImage().isEmpty()) {
            imagePathField.setText(sponsor.getImage());
            try {
                File imgFile = new File(sponsor.getImage());
                if (imgFile.exists()) {
                    Image image = new Image(imgFile.toURI().toString());
                    sponsorImageView.setImage(image);
                    sponsorImageView.setFitWidth(150);
                    sponsorImageView.setFitHeight(150);
                    sponsorImageView.setPreserveRatio(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        saveBtn.setText("Modifier");

        // Valider les champs après chargement
        validateAllFields();
    }

    // ===================== NAVIGATION =====================

    @FXML
    private void goBackToSponsorTable() {
        returnToSponsorTable();
    }

    private void returnToSponsorTable() {
        System.out.println("=== RETOUR VERS LA TABLE DES SPONSORS ===");

        try {
            if (dashboardController != null) {
                dashboardController.loadSponsorsTable();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/SponsorTable.fxml"));
                Parent root = loader.load();

                SponsorTableController controller = loader.getController();
                controller.setDashboardController(null);

                Stage stage = (Stage) (backButton != null ? backButton.getScene().getWindow() : saveBtn.getScene().getWindow());
                stage.setScene(new Scene(root));
                stage.setTitle("Gestion des sponsors");
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la liste des sponsors");
        }
    }

    @FXML
    private void handleCancel() {
        if (sponsorToEdit != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Annuler la modification");
            alert.setContentText("Voulez-vous vraiment annuler la modification en cours ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                returnToSponsorTable();
            }
        } else {
            returnToSponsorTable();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        typeBox.setValue(null);
        emailField.clear();
        telephoneField.clear();
        descriptionField.clear();
        imagePathField.clear();
        sponsorImageView.setImage(null);

        // Cacher les validations
        hideValidation(nomField);
        hideValidation(typeBox);
        hideValidation(emailField);
        hideValidation(telephoneField);
        hideValidation(descriptionField);
        hideValidation(imagePathField);
    }

}