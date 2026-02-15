package com.auticare.controller;

import com.auticare.entity.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
public class UserProfileController {

    @FXML private ImageView profileImage;
    @FXML private Label userName;
    @FXML private Label roleBadge;
    @FXML private Label statusBadge;
    @FXML private Label regDate;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;

    @Autowired
    private AdminDashboardController dashboardController;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            displayUserInfo();
        }
    }

    private void displayUserInfo() {
        userName.setText(user.getName());
        emailLabel.setText(user.getEmail());
        phoneLabel.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Non renseigné");

        // Badge rôle
        roleBadge.setText(user.getRole());
        roleBadge.getStyleClass().addAll("badge-role", getRoleStyle(user.getRole()));

        // Badge statut
        statusBadge.setText(user.getStatus());
        statusBadge.getStyleClass().addAll("badge-role", getStatusStyle(user.getStatus()));

        // Date d'inscription
        if (user.getRegistrationDate() != null) {
            regDate.setText(user.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        // Charger la photo
        loadProfilePhoto();
    }

    private String getRoleStyle(String role) {
        if (role == null) return "badge-gray";
        switch (role.toUpperCase()) {
            case "ADMIN": return "badge-admin";
            case "PARENT": return "badge-parent";
            case "ÉDUCATEUR":
            case "EDUCATEUR": return "badge-educateur";
            case "THÉRAPEUTE":
            case "THERAPEUTE": return "badge-therapeute";
            case "APPRENANT": return "badge-apprenant";
            default: return "badge-gray";
        }
    }

    private String getStatusStyle(String status) {
        if (status == null) return "badge-gray";
        switch (status.toLowerCase()) {
            case "actif": return "badge-actif";
            case "en attente": return "badge-en-attente";
            case "inactif": return "badge-inactif";
            default: return "badge-gray";
        }
    }

    private void loadProfilePhoto() {
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            try {
                String photoPath = user.getPhotoUrl().replaceFirst("^/", "");
                File photoFile = new File(photoPath);
                if (photoFile.exists()) {
                    Image image = new Image(photoFile.toURI().toString());
                    profileImage.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement photo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEdit() {
        if (user != null && dashboardController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditUser.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Node view = loader.load();

                UserFormController controller = loader.getController();
                controller.setUser(user);
                controller.setEditMode(true);

                dashboardController.setContent(view, "Modifier l'Utilisateur", "Mise à jour des infos");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack() {
        dashboardController.loadView("/fxml/UserManagement.fxml",
                "Gestion des Utilisateurs", "Gérez les membres de la plateforme");
    }
}