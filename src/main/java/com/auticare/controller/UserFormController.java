package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
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
    @FXML
    private Button btnSave;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminDashboardController dashboardController;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private User user;
    private boolean editMode = false;
    private String photoUrl;

    @FXML
    public void initialize() {
        System.out.println("🔄 Initialisation de UserFormController");

        // Initialiser les combobox
        roleCombo.getItems().addAll("Apprenant", "Parent", "Thérapeute", "Éducateur", "ADMIN");
        statusCombo.getItems().addAll("Actif", "En attente", "Inactif");

        // Gérer l'affichage du champ mot de passe en mode édition
        if (changePasswordToggle != null && passwordContainer != null) {
            passwordContainer.managedProperty().bind(changePasswordToggle.selectedProperty());
            passwordContainer.visibleProperty().bind(changePasswordToggle.selectedProperty());
            changePasswordToggle.setSelected(false);
        }

        // Valeurs par défaut pour un nouvel utilisateur
        if (!editMode) {
            roleCombo.setValue("Parent");
            statusCombo.setValue("En attente");
        }
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (editMode && changePasswordToggle != null) {
            changePasswordToggle.setSelected(false);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            System.out.println("📋 Chargement de l'utilisateur: " + user.getEmail() + " (ID: " + user.getId() + ")");

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
                // Essayer avec le chemin complet
                File fullPathFile = new File(System.getProperty("user.dir") + "/" + cleanPath);
                if (fullPathFile.exists()) {
                    Image image = new Image(fullPathFile.toURI().toString());
                    photoPreview.setImage(image);
                    System.out.println("✅ Photo chargée (chemin complet): " + fullPathFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement photo: " + e.getMessage());
        }
    }

    @FXML
    private void save() {
        System.out.println("🔄 Tentative de sauvegarde...");
        errorLabel.setText("");
        errorLabel.setStyle("-fx-text-fill: #EF4444;");

        if (!isValid()) {
            return;
        }

        try {
            String newEmail = emailField.getText().trim().toLowerCase();

            // Vérification d'unicité de l'email
            if (!editMode) {
                // Mode ajout : vérifier si l'email existe déjà
                if (userService.existsByEmail(newEmail)) {
                    errorLabel.setText("❌ Cet email est déjà utilisé par un autre compte");
                    System.out.println("❌ Email déjà existant: " + newEmail);
                    return;
                }
            } else {
                // Mode édition : vérifier si l'email a changé ET s'il existe déjà
                if (user != null && !user.getEmail().equalsIgnoreCase(newEmail)) {
                    System.out.println("🔍 L'email change de " + user.getEmail() + " à " + newEmail);

                    if (userService.existsByEmail(newEmail)) {
                        errorLabel.setText("❌ Cet email est déjà utilisé par un autre utilisateur");
                        System.out.println("❌ Nouvel email déjà pris: " + newEmail);
                        return;
                    } else {
                        System.out.println("✅ Nouvel email disponible: " + newEmail);
                    }
                } else {
                    // L'email n'a pas changé - c'est le cas pour atef@gmail.com
                    System.out.println("✅ L'email n'a pas changé: " + newEmail);
                    // Pas besoin de vérifier, c'est son propre email
                }
            }

            // Mettre à jour les champs
            user.setName(nameField.getText().trim());
            user.setEmail(newEmail);
            user.setPhoneNumber(phoneField.getText().trim());
            user.setRole(roleCombo.getValue());
            user.setStatus(statusCombo.getValue());
            user.setPhotoUrl(photoUrl);

            // Gestion du mot de passe
            if (editMode) {
                // En mode édition
                if (changePasswordToggle != null && changePasswordToggle.isSelected()) {
                    // L'utilisateur veut changer le mot de passe
                    String newPassword = passwordField.getText();
                    if (newPassword != null && !newPassword.isEmpty()) {
                        user.setPassword(newPassword);
                        System.out.println("🔑 Nouveau mot de passe défini");
                    } else {
                        errorLabel.setText("❌ Veuillez entrer un nouveau mot de passe");
                        return;
                    }
                } else {
                    // Conserver l'ancien mot de passe
                    System.out.println("🔑 Conservation de l'ancien mot de passe");
                    // Important: on ne change pas le mot de passe
                }
            } else {
                // En mode ajout, le mot de passe est obligatoire
                user.setPassword(passwordField.getText());
            }

            System.out.println("📝 Sauvegarde de l'utilisateur: " + user.getEmail() + " (ID: " + user.getId() + ")");

            User savedUser = userService.saveUser(user);

            if (savedUser != null) {
                System.out.println("✅ Utilisateur sauvegardé avec ID: " + savedUser.getId());

                // Message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText(editMode ? "✅ Utilisateur modifié avec succès!" : "✅ Utilisateur ajouté avec succès!");
                alert.showAndWait();

                // Rafraîchir la liste et fermer
                dashboardController.refreshUserList();
                close();

            } else {
                errorLabel.setText("❌ Erreur lors de la sauvegarde");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();

            // Message d'erreur plus explicite
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("Duplicate entry") || errorMessage.contains("ConstraintViolationException")) {
                    errorLabel.setText("❌ Cet email est déjà utilisé par un autre compte");
                } else if (errorMessage.contains("commit")) {
                    errorLabel.setText("❌ Erreur de base de données - Vérifie que l'email n'est pas déjà utilisé");
                } else if (errorMessage.contains("already used")) {
                    errorLabel.setText("❌ " + errorMessage);
                } else {
                    errorLabel.setText("❌ Erreur: " + errorMessage);
                }
            } else {
                errorLabel.setText("❌ Erreur inconnue lors de la sauvegarde");
            }
        }
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(btnCancel.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Créer le dossier uploads/photos s'il n'existe pas
                File uploadDir = new File("uploads/photos");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Générer un nom de fichier unique
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                File destFile = new File(uploadDir, fileName);

                // Copier le fichier
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Sauvegarder le chemin relatif
                this.photoUrl = "uploads/photos/" + fileName;

                // Afficher la photo dans l'aperçu
                Image image = new Image(destFile.toURI().toString());
                photoPreview.setImage(image);

                System.out.println("✅ Photo sauvegardée: " + this.photoUrl);

            } catch (IOException e) {
                errorLabel.setText("❌ Erreur lors de la copie de l'image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void close() {
        System.out.println("🔄 Fermeture du formulaire");
        // Rafraîchir la liste avant de fermer
        dashboardController.refreshUserList();
        // Recharger la vue UserManagement
        dashboardController.loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs",
                "Gérez les membres de la plateforme");
    }

    private boolean isValid() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String role = roleCombo.getValue();
        String password = passwordField.getText();

        // Validation du nom
        if (name == null || name.trim().isEmpty()) {
            errors.append("• Le nom complet est requis\n");
            isValid = false;
        } else if (name.trim().length() < 2) {
            errors.append("• Le nom doit contenir au moins 2 caractères\n");
            isValid = false;
        }

        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            errors.append("• L'email est requis\n");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("• Format d'email invalide (exemple: nom@domaine.com)\n");
            isValid = false;
        }

        // Validation du téléphone (optionnel mais doit être valide si présent)
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("\\s+", "").replace("+", "").replace("-", "");
            if (!cleanPhone.matches("^[0-9]{8,15}$")) {
                errors.append("• Le téléphone doit contenir entre 8 et 15 chiffres\n");
                isValid = false;
            }
        }

        // Validation du rôle
        if (role == null || role.isEmpty()) {
            errors.append("• Veuillez sélectionner un rôle\n");
            isValid = false;
        }

        // Validation du mot de passe
        if (!editMode) {
            // Mode ajout : mot de passe obligatoire
            if (password == null || password.trim().isEmpty()) {
                errors.append("• Le mot de passe est requis\n");
                isValid = false;
            } else if (password.length() < 6) {
                errors.append("• Le mot de passe doit contenir au moins 6 caractères\n");
                isValid = false;
            }
        } else {
            // Mode édition : mot de passe optionnel
            if (changePasswordToggle != null && changePasswordToggle.isSelected()) {
                if (password == null || password.trim().isEmpty()) {
                    errors.append("• Veuillez entrer un nouveau mot de passe\n");
                    isValid = false;
                } else if (password.length() < 6) {
                    errors.append("• Le nouveau mot de passe doit contenir au moins 6 caractères\n");
                    isValid = false;
                }
            }
        }

        if (!isValid) {
            errorLabel.setText(errors.toString());
            errorLabel.setVisible(true);
        }

        return isValid;
    }
}