package com.auticare.controller;

import com.auticare.entity.User;
import com.auticare.repository.UserRepository;
import com.auticare.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Component
@Scope("prototype")
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleLogin() {
        // Réinitialiser les messages d'erreur
        clearErrors();

        // Valider les champs
        if (!validateInputs()) {
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

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
                    // Rediriger vers CourseManagement pour les parents, éducateurs et thérapeutes
                    loadDashboard("/fxml/CourseManagement.fxml", "AutiCare - Gestion des Cours");
                    break;
                default:
                    loadDashboard("/fxml/UserDashboard.fxml", "AutiCare - Espace Utilisateur");
                    break;
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec de connexion", "Email ou mot de passe incorrect.");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            emailErrorLabel.setText("L'email est requis");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            emailErrorLabel.setText("Format d'email invalide");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else {
            emailErrorLabel.setVisible(false);
        }

        // Validation du mot de passe
        if (password == null || password.trim().isEmpty()) {
            passwordErrorLabel.setText("Le mot de passe est requis");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else {
            passwordErrorLabel.setVisible(false);
        }

        return isValid;
    }

    private void clearErrors() {
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        try {
            System.out.println("🔄 Redirection vers inscription...");

            // Vérifier que le fichier Register.fxml existe
            URL resource = getClass().getResource("/fxml/Register.fxml");
            System.out.println("📁 URL de Register.fxml: " + resource);

            if (resource == null) {
                // Essayer avec un chemin absolu (pour debug)
                File file = new File("src/main/resources/fxml/Register.fxml");
                System.out.println("📁 Chemin absolu: " + file.getAbsolutePath());
                System.out.println("📁 Fichier existe? " + file.exists());

                if (file.exists()) {
                    resource = file.toURI().toURL();
                    System.out.println("✅ Fichier trouvé dans le système de fichiers!");
                } else {
                    throw new IOException("Fichier Register.fxml non trouvé dans /fxml/");
                }
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Créer un compte");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur redirection inscription: " + e.getMessage());
            e.printStackTrace();

            // Afficher une alerte pour informer l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Fichier manquant");
            alert.setContentText("La page d'inscription (Register.fxml) n'est pas disponible.\nDétail: " + e.getMessage());
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
            System.out.println("🔍 Chargement de: " + fxmlPath);

            URL resource = getClass().getResource(fxmlPath);
            System.out.println("📁 URL complète: " + resource);

            if (resource == null) {
                throw new IOException("Fichier non trouvé: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

            System.out.println("✅ Chargement réussi: " + fxmlPath);

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            String detail = (e.getMessage() != null) ? e.getMessage() : e.toString();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le tableau de bord : " + detail);
        }
    }
}