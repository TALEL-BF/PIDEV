package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainDashboardController {

    @FXML private Button eventsParentBtn;
    @FXML private VBox eventsSubmenu;
    @FXML private Button eventsBtn;
    @FXML private Button sponsorsBtn;
    @FXML private StackPane contentArea;

    private boolean isSubmenuVisible = true;

    @FXML
    public void initialize() {
        // Configuration du sous-menu
        eventsSubmenu.setVisible(true);
        eventsSubmenu.setManaged(true);

        // Action pour afficher/masquer le sous-menu
        eventsParentBtn.setOnAction(e -> {
            isSubmenuVisible = !isSubmenuVisible;
            eventsSubmenu.setVisible(isSubmenuVisible);
            eventsSubmenu.setManaged(isSubmenuVisible);
            eventsParentBtn.setText(isSubmenuVisible ?
                    "🎪 Gestion des événements ▾" : "🎪 Gestion des événements ▸");
        });

        // Charger la table des événements par défaut
        loadEventsTable();

        // Actions des boutons
        eventsBtn.setOnAction(e -> loadEventsTable());
        sponsorsBtn.setOnAction(e -> loadSponsorsTable());

        // Style actif
        setActiveButton(eventsBtn);
    }

    public void loadEventsTable() {
        try {
            System.out.println("Chargement de EventTable.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventTable.fxml"));
            Parent table = loader.load();

            // Lier le contrôleur au dashboard
            EventTableController controller = loader.getController();
            controller.setDashboardController(this);

            // Afficher dans contentArea
            contentArea.getChildren().setAll(table);

            setActiveButton(eventsBtn);
            setInactiveButton(sponsorsBtn);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERREUR: Impossible de charger EventTable.fxml");
        }
    }
    public void loadAddSponsorForm() {
        try {
            System.out.println("Chargement du formulaire d'ajout de sponsor...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddSponsorForm.fxml"));
            Parent form = loader.load();

            // Passer la référence du dashboard au formulaire
            AddSponsorController controller = loader.getController();
            controller.setDashboardController(this);

            // Afficher dans contentArea (la sidebar reste visible)
            contentArea.getChildren().setAll(form);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERREUR: Impossible de charger AddSponsorForm.fxml");
        }
    }

    public void loadSponsorsTable() {
        try {
            System.out.println("Chargement de SponsorTable.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SponsorTable.fxml"));
            Parent table = loader.load();

            // Lier le contrôleur au dashboard
            SponsorTableController controller = loader.getController();
            controller.setDashboardController(this);

            // Afficher dans contentArea
            contentArea.getChildren().setAll(table);

            setActiveButton(sponsorsBtn);
            setInactiveButton(eventsBtn);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERREUR: Impossible de charger SponsorTable.fxml");
        }
    }

    public void loadAddEventForm() {
        try {
            System.out.println("Chargement du formulaire d'ajout...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
            Parent form = loader.load();

            // Passer la référence du dashboard au formulaire
            AddEventController controller = loader.getController();
            controller.setDashboardController(this);

            // Afficher dans contentArea
            contentArea.getChildren().setAll(form);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERREUR: Impossible de charger AjouterEvent.fxml");
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    private void setActiveButton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white;");
    }

}