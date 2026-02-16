package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

@Component
public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button btnRegister;

    // Labels d'erreur
    @FXML private Label nameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleRegister() {
        System.out.println("🔄 Tentative d'inscription...");

        // Réinitialiser les erreurs
        clearErrors();

        // Valider les champs
        if (!validateInputs()) {
            return;
        }

        String email = emailField.getText().trim().toLowerCase();

        // Vérifier si l'email existe déjà
        if (userService.existsByEmail(email)) {
            emailErrorLabel.setText("❌ Cet email est déjà utilisé");
            emailErrorLabel.setVisible(true);
            return;
        }

        try {
            // Créer nouvel utilisateur
            User user = new User();
            user.setName(nameField.getText().trim());
            user.setEmail(email);
            user.setPhoneNumber(phoneField.getText().trim());
            user.setPassword(passwordField.getText());
            user.setRole("Parent"); // Rôle par défaut
            user.setStatus("En attente"); // Statut par défaut
            user.setRegistrationDate(LocalDate.now());

            System.out.println("📝 Sauvegarde de l'utilisateur: " + email);

            User savedUser = userService.saveUser(user);

            if (savedUser != null) {
                System.out.println("✅ Compte créé avec succès! ID: " + savedUser.getId());
                showSuccess("Compte créé avec succès! Veuillez vous connecter.");

                // Rediriger vers la page de connexion
                handleBackToLogin();
            } else {
                showError("Erreur lors de la création du compte");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation du nom
        if (name == null || name.trim().isEmpty()) {
            nameErrorLabel.setText("❌ Le nom est requis");
            nameErrorLabel.setVisible(true);
            isValid = false;
        } else if (name.trim().length() < 2) {
            nameErrorLabel.setText("❌ Le nom doit contenir au moins 2 caractères");
            nameErrorLabel.setVisible(true);
            isValid = false;
        } else {
            nameErrorLabel.setVisible(false);
        }

        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            emailErrorLabel.setText("❌ L'email est requis");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            emailErrorLabel.setText("❌ Format d'email invalide");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else {
            emailErrorLabel.setVisible(false);
        }

        // Validation du téléphone (optionnel)
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("\\s+", "").replace("+", "").replace("-", "");
            if (!cleanPhone.matches("^[0-9]{8,15}$")) {
                phoneErrorLabel.setText("❌ Le téléphone doit contenir 8 à 15 chiffres");
                phoneErrorLabel.setVisible(true);
                isValid = false;
            } else {
                phoneErrorLabel.setVisible(false);
            }
        } else {
            phoneErrorLabel.setVisible(false);
        }

        // Validation du mot de passe
        if (password == null || password.trim().isEmpty()) {
            passwordErrorLabel.setText("❌ Le mot de passe est requis");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else if (password.length() < 6) {
            passwordErrorLabel.setText("❌ Le mot de passe doit contenir au moins 6 caractères");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else {
            passwordErrorLabel.setVisible(false);
        }

        // Validation de la confirmation
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            confirmPasswordErrorLabel.setText("❌ Veuillez confirmer le mot de passe");
            confirmPasswordErrorLabel.setVisible(true);
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordErrorLabel.setText("❌ Les mots de passe ne correspondent pas");
            confirmPasswordErrorLabel.setVisible(true);
            isValid = false;
        } else {
            confirmPasswordErrorLabel.setVisible(false);
        }

        return isValid;
    }

    private void clearErrors() {
        nameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        phoneErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setVisible(false);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToLogin() {
        try {
            System.out.println("🔄 Retour à la page de connexion");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Connexion");
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur retour connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}