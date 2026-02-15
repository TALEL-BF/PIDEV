package com.auticare.controller;

import com.auticare.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserProfileController {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label userName;
    @FXML
    private Label roleBadge;
    @FXML
    private Label statusBadge;
    @FXML
    private Label regDate;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @Autowired
    private AdminDashboardController dashboardController;

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            userName.setText(user.getName());
            emailLabel.setText(user.getEmail());
            phoneLabel.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A");
            roleBadge.setText(user.getRole());
            statusBadge.setText(user.getStatus());

            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                profileImage.setImage(new Image("file:" + user.getPhotoUrl()));
            }
        }
    }

    @FXML
    private void handleEdit() {
        // Implementation for showing edit view in dashboard center
    }

    @FXML
    private void handleBack() {
        dashboardController.loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs",
                "Gérez les membres de la plateforme");
    }
}
