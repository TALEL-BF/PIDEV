package com.auticare.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardGridController {

    @Autowired
    private AdminDashboardController mainController;

    @FXML
    private VBox tileUsers;
    @FXML
    private VBox tileCourses;
    @FXML
    private VBox tileEvals;
    @FXML
    private VBox tileConsults;
    @FXML
    private VBox tileEvents;
    @FXML
    private VBox tileClaims;

    @FXML
    private void handleUsersClick(MouseEvent event) {
        mainController.loadView("/fxml/UserManagement.fxml", "Gestion des Utilisateurs",
                "Gérez les membres de la plateforme");
    }

    @FXML
    private void handleCoursesClick(MouseEvent event) {
        mainController.loadPlaceholder("Gestion des Cours");
    }

    @FXML
    private void handleEvalsClick(MouseEvent event) {
        mainController.loadPlaceholder("Gestion des Évaluations");
    }

    @FXML
    private void handleConsultsClick(MouseEvent event) {
        mainController.loadPlaceholder("Gestion des Consultations");
    }

    @FXML
    private void handleEventsClick(MouseEvent event) {
        mainController.loadPlaceholder("Gestion des Événements");
    }

    @FXML
    private void handleClaimsClick(MouseEvent event) {
        mainController.loadPlaceholder("Gestion des Réclamations");
    }
}
