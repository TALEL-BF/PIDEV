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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
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

    @FXML
    public void initialize() {
        // Load User Management as default for Admin
        loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
        updateSidebarStyle(btnUsers);
        setupAdminProfile();
    }

    private void setupAdminProfile() {
        com.auticare.entity.User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String roleStr = user.getRole() != null ? user.getRole() : "ADMINISTRATEUR";
            adminNameLabel.setText(user.getName());
            adminRoleLabel.setText(roleStr);
            userRoleSidebar.setText(roleStr);
        }
    }

    @FXML
    private void switchView(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String text = btn.getText();

        // Update active class
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
                // Reset to base class based on ID or current state if needed
                // But for now, just clearing the active ones is enough if we rely on FXML base
                // classes
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Node view = loader.load();
            setContent(view, title, subtitle);
        } catch (IOException e) {
            e.printStackTrace();
            loadPlaceholder(title + " (Error loading FXML)");
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
