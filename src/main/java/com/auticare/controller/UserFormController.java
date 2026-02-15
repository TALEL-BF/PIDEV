package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@Scope("prototype")
public class UserFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private CheckBox changePasswordToggle;
    @FXML
    private VBox passwordContainer;
    @FXML
    private ImageView photoPreview;
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnCancel;

    @Autowired
    private UserService userService;
    @Autowired
    private AdminDashboardController dashboardController;

    private User user;
    private boolean editMode = false;
    private String photoUrl;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("Apprenant", "Parent", "Thérapeute", "Éducateur", "ADMIN");
        statusCombo.getItems().addAll("Actif", "En attente", "Inactif");

        if (changePasswordToggle != null && passwordContainer != null) {
            passwordContainer.managedProperty().bind(changePasswordToggle.selectedProperty());
            passwordContainer.visibleProperty().bind(changePasswordToggle.selectedProperty());
        }
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhoneNumber());
            roleCombo.setValue(user.getRole());
            statusCombo.setValue(user.getStatus());
            this.photoUrl = user.getPhotoUrl();

            // Afficher la photo si elle existe
            if (photoUrl != null && !photoUrl.isEmpty()) {
                loadPhotoPreview(photoUrl);
            }
        } else {
            this.user = new User();
        }
    }

    // Ajoute cette méthode pour charger la photo
    private void loadPhotoPreview(String photoPath) {
        try {
            String cleanPath = photoPath.replaceFirst("^/", "");
            File photoFile = new File(cleanPath);
            if (photoFile.exists()) {
                Image image = new Image(photoFile.toURI().toString());
                photoPreview.setImage(image);
                System.out.println("✅ Photo chargée: " + cleanPath);
            } else {
                System.out.println("⚠️ Fichier photo non trouvé: " + cleanPath);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement photo: " + e.getMessage());
        }
    }
    @FXML
    private void save() {
        if (isValid()) {
            // Vérifier si l'email existe déjà (sauf pour l'utilisateur en cours d'édition)
            if (!editMode && userService.existsByEmail(emailField.getText())) {
                errorLabel.setText("Cet email est déjà utilisé");
                return;
            }

            // Pour l'édition, vérifier si l'email a changé et existe déjà
            if (editMode && user != null && !user.getEmail().equals(emailField.getText())
                    && userService.existsByEmail(emailField.getText())) {
                errorLabel.setText("Cet email est déjà utilisé par un autre utilisateur");
                return;
            }

            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhoneNumber(phoneField.getText());
            user.setRole(roleCombo.getValue());
            user.setStatus(statusCombo.getValue());
            user.setPhotoUrl(photoUrl);

            // Gestion du mot de passe
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                user.setPassword(passwordField.getText());
            } else if (!editMode && user.getId() == null) {
                errorLabel.setText("Mot de passe requis pour un nouvel utilisateur");
                return;
            }

            try {
                User savedUser = userService.saveUser(user);
                if (savedUser != null) {
                    // Message de succès
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText(editMode ? "Utilisateur modifié avec succès!" : "Utilisateur ajouté avec succès!");
                    alert.showAndWait();

                    close();
                } else {
                    errorLabel.setText("Erreur lors de la sauvegarde");
                }
            } catch (Exception e) {
                errorLabel.setText("Erreur lors de la sauvegarde: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnCancel.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File uploadDir = new File("uploads/photos");
                if (!uploadDir.exists())
                    uploadDir.mkdirs();

                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                File destFile = new File(uploadDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.photoUrl = "/uploads/photos/" + fileName;
                photoPreview.setImage(new Image(selectedFile.toURI().toString()));
            } catch (IOException e) {
                errorLabel.setText("Erreur lors de la copie de l'image");
            }
        }
    }

    @FXML
    private void close() {
        // Important: Rafraîchir la liste des utilisateurs avant de recharger la vue
        dashboardController.refreshUserList();

        // Recharger la vue UserManagement
        dashboardController.loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs",
                "Gérez les membres de la plateforme");
    }

    private boolean isValid() {
        errorLabel.setText("");
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String role = roleCombo.getValue();

        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("Le nom complet est requis.");
            return false;
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Format d'email invalide.");
            return false;
        }

        if (phone != null && !phone.isEmpty() && !phone.matches("^\\d{8,15}$")) {
            errorLabel.setText("Le numéro de téléphone doit contenir entre 8 et 15 chiffres.");
            return false;
        }

        if (role == null) {
            errorLabel.setText("Veuillez sélectionner un rôle.");
            return false;
        }

        if (!editMode && (passwordField.getText() == null || passwordField.getText().length() < 6)) {
            errorLabel.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        return true;
    }

}