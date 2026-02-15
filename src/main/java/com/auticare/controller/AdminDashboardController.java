package com.auticare.controller;

import com.auticare.session.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class AdminDashboardController implements Initializable {

    @FXML
    private Button btnUsers;
    @FXML
    private Button btnCourses;
    @FXML
    private Button btnEmploi;
    @FXML
    private Button btnConsults;
    @FXML
    private Button btnSuivie;
    @FXML
    private Button btnTherapie;
    @FXML
    private Button btnEvents;
    @FXML
    private Button btnJeux;
    @FXML
    private Label pageTitle;
    @FXML
    private Label pageSubtitle;
    @FXML
    private Label adminNameLabel;
    @FXML
    private Label adminRoleLabel;
    @FXML
    private javafx.scene.shape.Circle adminAvatar;
    @FXML
    private StackPane contentArea;
    @FXML
    private BorderPane mainContainer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    // Référence au contrôleur UserFxController pour rafraîchir la liste
    private UserFxController currentUserFxController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== AdminDashboardController.initialize() ===");

        try {
            // Vérifier que les FXML sont bien injectés
            if (pageTitle == null) {
                System.err.println("❌ pageTitle est null - Vérifie le fx:id dans le FXML");
            }
            if (pageSubtitle == null) {
                System.err.println("❌ pageSubtitle est null - Vérifie le fx:id dans le FXML");
            }
            if (contentArea == null) {
                System.err.println("❌ contentArea est null - Vérifie le fx:id dans le FXML");
            }

            // Mettre à jour le profil admin
            setupAdminProfile();

            // Charger UserManagement par défaut
            loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");

            // Mettre à jour le style du bouton actif
            updateSidebarStyle(btnUsers);

            System.out.println("✅ Initialisation terminée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur dans initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAdminProfile() {
        try {
            com.auticare.entity.User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                if (adminNameLabel != null) {
                    adminNameLabel.setText(user.getName());
                }
                if (adminRoleLabel != null) {
                    adminRoleLabel.setText(user.getRole() != null ? user.getRole() : "ADMINISTRATEUR");
                }
                System.out.println("✅ Profil admin chargé: " + user.getName());
            } else {
                System.out.println("⚠️ Aucun utilisateur connecté");
                if (adminNameLabel != null) {
                    adminNameLabel.setText("Admin");
                }
                if (adminRoleLabel != null) {
                    adminRoleLabel.setText("Administrateur");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans setupAdminProfile: " + e.getMessage());
        }
    }

    @FXML
    private void switchView(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String text = btn.getText();
        System.out.println("switchView: " + text);

        updateSidebarStyle(btn);

        if (text.contains("Utilisateur")) {
            loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
        } else if (text.contains("Cours")) {
            loadView("/fxml/CourseManagement.fxml", "Gestion Cours & Évaluations", "Gérez le contenu éducatif");
        } else if (text.contains("Suivie")) {
            loadView("/fxml/FollowUpManagement.fxml", "Gestion des Suivies", "Gérez les suivies");
        } else if (text.contains("Thérapie")) {
            loadView("/fxml/TherapieManagement.fxml", "Gestion des Thérapies", "Gérez les thérapies");
        } else {
            loadPlaceholder(text);
        }
    }

    private void updateSidebarStyle(Button activeBtn) {
        Button[] buttons = { btnUsers, btnCourses, btnEmploi, btnConsults, btnSuivie, btnTherapie, btnEvents, btnJeux };
        for (Button b : buttons) {
            if (b != null) {
                b.getStyleClass().removeAll("sidebar-btn-active");
                b.getStyleClass().add("sidebar-btn");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("sidebar-btn");
            activeBtn.getStyleClass().add("sidebar-btn-active");
        }
    }

    public void loadPlaceholder(String title) {
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText("Module en cours de développement");
        }
        if (contentArea != null) {
            contentArea.getChildren().clear();
            Label placeholder = new Label("Module " + title + " en cours de développement");
            placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #636e72; -fx-alignment: center;");
            contentArea.getChildren().add(placeholder);
        }
    }

    public void setContent(Node view, String title, String subtitle) {
        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        }
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText(subtitle);
        }
    }

    public void loadView(String fxmlPath, String title, String subtitle) {
        try {
            System.out.println("🔄 Chargement de: " + fxmlPath);

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Fichier non trouvé: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            // Si c'est UserManagement.fxml, garder une référence
            if (fxmlPath.contains("UserManagement")) {
                currentUserFxController = loader.getController();
                System.out.println("✅ Référence à UserFxController sauvegardée");
            }

            setContent(view, title, subtitle);
            System.out.println("✅ Chargé avec succès: " + fxmlPath);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            loadPlaceholder(title);
        }
    }

    public void refreshUserList() {
        System.out.println("🔄 Rafraîchissement de la liste...");
        if (currentUserFxController != null) {
            currentUserFxController.refreshList();
        } else {
            loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Déconnexion");
            alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter?");
            alert.setContentText("Vous serez redirigé vers la page de connexion.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                SessionManager.getInstance().clearSession();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("AutiCare - Connexion");
                stage.show();

                System.out.println("✅ Déconnexion réussie");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de la déconnexion");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}