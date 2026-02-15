package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Format Email invalide", "Veuillez saisir un email valide.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Mots de passe non identiques",
                    "Les deux mots de passe ne correspondent pas.");
            return;
        }

        if (userService.existsByEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Email déjà utilisé", "Cet email est déjà associé à un compte.");
            return;
        }

        try {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhoneNumber(phone);
            newUser.setPassword(password); // Note: Should hash password in production
            newUser.setRole("USER");
            newUser.setStatus("Actif");

            userService.saveUser(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            handleBackToLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la création du compte : " + e.getMessage());
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
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
