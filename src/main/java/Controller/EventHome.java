package com.auticare.controllers;

import com.auticare.entities.Event;
import com.auticare.services.EventServices;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class EventHome {

    @FXML private BorderPane mainContainer;
    @FXML private StackPane contentArea;
    @FXML private Button eventsParentBtn;
    @FXML private VBox eventsSubmenu;
    @FXML private Button btnListEvents;
    @FXML private Button btnSponsors;
    @FXML private Label welcomeLabel;

    private EventServices eventServices = new EventServices();
    private boolean isSubmenuVisible = true;

    @FXML
    public void initialize() {
        showEventList();
        highlightActiveButton(btnListEvents);

        // Configuration du sous-menu
        eventsSubmenu.setVisible(true);
        eventsSubmenu.setManaged(true);

        // Action pour afficher/masquer le sous-menu
        eventsParentBtn.setOnAction(e -> {
            isSubmenuVisible = !isSubmenuVisible;
            eventsSubmenu.setVisible(isSubmenuVisible);
            eventsSubmenu.setManaged(isSubmenuVisible);
            eventsParentBtn.setText(isSubmenuVisible ?
                    "🎉 Gestion des événements ▾" : "🎉 Gestion des événements ▸");
        });

        // Actions des boutons
        btnListEvents.setOnAction(e -> showEventList());
        btnSponsors.setOnAction(e -> showSponsors());

        // Charger la liste par défaut
        showEventList();
        highlightActiveButton(btnListEvents);
    }

    @FXML
    private void showEventList() {
        loadEventsTable();
        highlightActiveButton(btnListEvents);
    }

    @FXML
    private void showSponsors() {
        loadSponsorsTable();
        highlightActiveButton(btnSponsors);
    }

    public void loadEventsTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventTable.fxml"));
            Parent table = loader.load();

            EventTableController controller = loader.getController();
            controller.setEventHome(this);

            contentArea.getChildren().setAll(table);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la table des événements");
        }
    }

    private void highlightActiveButton(Button activeBtn) {
        btnListEvents.getStyleClass().remove("active");
        btnSponsors.getStyleClass().remove("active");
        activeBtn.getStyleClass().add("active");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void loadSponsorsTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SponsorTable.fxml"));
            Parent table = loader.load();

            SponsorTableController controller = loader.getController();
            controller.setEventHome(this);

            contentArea.getChildren().setAll(table);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la table des sponsors");
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }
}