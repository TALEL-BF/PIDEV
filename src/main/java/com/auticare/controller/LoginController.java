package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.repository.UserRepository;
import com.auticare.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Scope("prototype")
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez saisir votre email et votre mot de passe.");
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            SessionManager.getInstance().setCurrentUser(user);

            String role = user.getRole() != null ? user.getRole().toUpperCase().trim() : "USER";

            switch (role) {
                case "ADMIN":
                    loadDashboard("/fxml/AdminDashboard.fxml", "AutiCare - Admin Dashboard");
                    break;
                case "PARENT":
                case "EDUCATEUR":
                case "THERAPEUTE":
                    loadDashboard("/fxml/UserDashboard.fxml", "AutiCare - Espace Professionnel & Parent");
                    break;
                default:
                    loadDashboard("/fxml/UserDashboard.fxml", "AutiCare - Espace Utilisateur");
                    break;
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec de connexion", "Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            System.out.println("🔄 Redirection vers inscription...");

            // Note: Si tu n'as pas Register.fxml, commente cette méthode ou crée le fichier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Créer un compte");
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur redirection inscription: " + e.getMessage());
            e.printStackTrace();

            // Afficher une alerte pour informer l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Fichier manquant");
            alert.setContentText("La page d'inscription (Register.fxml) n'est pas disponible.\nVeuillez contacter l'administrateur.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText(null);
        alert.setContentText("Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadDashboard(String fxmlPath, String title) {
        try {
            System.out.println("Chargement de: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            String detail = (e.getMessage() != null) ? e.getMessage() : e.toString();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le tableau de bord : " + detail);
        }
    }
}