package com.auticare.controller;

import com.auticare.session.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class AdminDashboardController {

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
    private Label userRoleSidebar;
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

    @FXML
    public void initialize() {
        System.out.println("=== AdminDashboardController.initialize() ===");

        try {
            // Vérifier si UserManagement.fxml existe
            URL fxmlUrl = getClass().getResource("/fxml/UserManagement.fxml");
            System.out.println("URL de UserManagement.fxml: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("ERREUR: UserManagement.fxml n'est pas trouvé!");
                pageTitle.setText("ERREUR");
                pageSubtitle.setText("Fichier UserManagement.fxml manquant");
                Label errorLabel = new Label("Le fichier /fxml/UserManagement.fxml est introuvable.\n" +
                        "Vérifiez qu'il existe dans src/main/resources/fxml/");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                contentArea.getChildren().add(errorLabel);
                return;
            }

            // Essayer de charger UserManagement
            loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
            updateSidebarStyle(btnUsers);
            setupAdminProfile();

            System.out.println("Initialisation terminée avec succès!");

        } catch (Exception e) {
            System.err.println("Exception dans initialize(): " + e.getMessage());
            e.printStackTrace();

            // Afficher l'erreur dans l'interface
            pageTitle.setText("Erreur");
            pageSubtitle.setText(e.getMessage());
            Label errorLabel = new Label("Erreur: " + e.toString());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-wrap-text: true;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void setupAdminProfile() {
        try {
            com.auticare.entity.User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                String roleStr = user.getRole() != null ? user.getRole() : "ADMINISTRATEUR";
                adminNameLabel.setText(user.getName());
                adminRoleLabel.setText(roleStr);
                userRoleSidebar.setText(roleStr);
                System.out.println("Profil admin chargé: " + user.getName());
            }
        } catch (Exception e) {
            System.err.println("Erreur dans setupAdminProfile: " + e.getMessage());
        }
    }

    @FXML
    private void switchView(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String text = btn.getText();
        System.out.println("switchView: " + text);

        updateSidebarStyle(btn);

        if (text.equals("Gestion Utilisateur")) {
            loadView("/fxml/UserManagement.fxml", "Gestion Utilisateur", "Gérez les membres de la plateforme");
        } else if (text.contains("Cours")) {
            loadView("/fxml/CourseManagement.fxml", "Gestion Cours & Évaluations", "Gérez le contenu éducatif");
        } else if (text.equals("Suivie")) {
            loadView("/fxml/FollowUpManagement.fxml", "Gestion des Suivies",
                    "Gérez les suivies et les informations associées");
        } else {
            loadPlaceholder(text);
        }
    }

    private void updateSidebarStyle(Button activeBtn) {
        Button[] buttons = { btnUsers, btnCourses, btnEmploi, btnConsults, btnSuivie, btnTherapie, btnEvents, btnJeux };
        for (Button b : buttons) {
            if (b != null) {
                b.getStyleClass().removeAll("sidebar-btn-active", "sidebar-sub-btn-active");
            }
        }
        if (activeBtn != null) {
            if (activeBtn.getStyleClass().contains("sidebar-sub-btn")) {
                activeBtn.getStyleClass().add("sidebar-sub-btn-active");
            } else {
                activeBtn.getStyleClass().add("sidebar-btn-active");
            }
        }
    }

    public void loadPlaceholder(String title) {
        pageTitle.setText(title);
        pageSubtitle.setText("Module en cours de développement");
        contentArea.getChildren().clear();
        Label placeholder = new Label("Architecture Desktop pour " + title + "\nPrêt pour intégration FXML.");
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #636e72; -fx-text-alignment: center;");
        contentArea.getChildren().add(placeholder);
    }

    public void setContent(Node view, String title, String subtitle) {
        contentArea.getChildren().setAll(view);
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
    }

    public void loadView(String fxmlPath, String title, String subtitle) {
        try {
            System.out.println("Tentative de chargement: " + fxmlPath);

            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("URL complète: " + fxmlUrl);

            if (fxmlUrl == null) {
                throw new IOException("Fichier non trouvé: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();

            // Si c'est UserManagement.fxml, garder une référence au contrôleur
            if (fxmlPath.contains("UserManagement")) {
                currentUserFxController = loader.getController();
                System.out.println("✅ Référence à UserFxController sauvegardée");
            }

            setContent(view, title, subtitle);
            System.out.println("✅ Chargé avec succès: " + fxmlPath);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            loadPlaceholder(title + " (Error loading FXML)");
        }
    }

    // Méthode pour rafraîchir la liste des utilisateurs
    public void refreshUserList() {
        System.out.println("🔄 Rafraîchissement de la liste des utilisateurs...");
        if (currentUserFxController != null) {
            currentUserFxController.refreshList();
        } else {
            System.out.println("⚠️ currentUserFxController est null - rechargement de la vue");
            loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}