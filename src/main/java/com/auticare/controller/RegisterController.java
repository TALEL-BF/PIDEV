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

    // Labels d'erreur (obligatoires)
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
        // Réinitialiser les erreurs
        clearErrors();

        // Valider les champs
        if (!validateInputs()) {
            return;
        }

        // Vérifier si l'email existe déjà
        if (userService.existsByEmail(emailField.getText())) {
            emailErrorLabel.setText("Cet email est déjà utilisé");
            emailErrorLabel.setVisible(true);
            return;
        }

        try {
            // Créer nouvel utilisateur
            User user = new User();
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhoneNumber(phoneField.getText());
            user.setPassword(passwordField.getText());
            user.setRole("Parent");
            user.setStatus("En attente");
            user.setRegistrationDate(LocalDate.now());

            User savedUser = userService.saveUser(user);

            if (savedUser != null) {
                showSuccess("Compte créé avec succès! Veuillez vous connecter.");
                handleBackToLogin();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la création du compte");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du nom
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            nameErrorLabel.setText("Le nom est requis");
            nameErrorLabel.setVisible(true);
            isValid = false;
        } else if (name.length() < 3) {
            nameErrorLabel.setText("Le nom doit contenir au moins 3 caractères");
            nameErrorLabel.setVisible(true);
            isValid = false;
        } else {
            nameErrorLabel.setVisible(false);
        }

        // Validation de l'email
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            emailErrorLabel.setText("L'email est requis");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailErrorLabel.setText("Format d'email invalide");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else {
            emailErrorLabel.setVisible(false);
        }

        // Validation du téléphone (optionnel)
        String phone = phoneField.getText();
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^[0-9]{8,15}$")) {
                phoneErrorLabel.setText("Le téléphone doit contenir 8 à 15 chiffres");
                phoneErrorLabel.setVisible(true);
                isValid = false;
            } else {
                phoneErrorLabel.setVisible(false);
            }
        } else {
            phoneErrorLabel.setVisible(false);
        }

        // Validation du mot de passe
        String password = passwordField.getText();
        if (password == null || password.isEmpty()) {
            passwordErrorLabel.setText("Le mot de passe est requis");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else if (password.length() < 6) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins 6 caractères");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else {
            passwordErrorLabel.setVisible(false);
        }

        // Validation de la confirmation
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            confirmPasswordErrorLabel.setText("Les mots de passe ne correspondent pas");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}