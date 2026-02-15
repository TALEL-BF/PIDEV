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
    @FXML private Label errorLabel;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleRegister() {
        if (validateInput()) {
            // Vérifier si l'email existe déjà
            if (userService.existsByEmail(emailField.getText())) {
                showError("Cet email est déjà utilisé");
                return;
            }

            // Créer nouvel utilisateur
            User user = new User();
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhoneNumber(phoneField.getText());
            user.setPassword(passwordField.getText());
            user.setRole("Parent"); // Rôle par défaut
            user.setStatus("En attente"); // Statut par défaut
            user.setRegistrationDate(LocalDate.now());

            try {
                userService.saveUser(user);
                showSuccess("Compte créé avec succès! Veuillez vous connecter.");
                handleBackToLogin();
            } catch (Exception e) {
                showError("Erreur lors de la création du compte: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty()) {
            showError("Le nom est requis");
            return false;
        }
        if (emailField.getText().isEmpty()) {
            showError("L'email est requis");
            return false;
        }
        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est requis");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
            return false;
        }
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}