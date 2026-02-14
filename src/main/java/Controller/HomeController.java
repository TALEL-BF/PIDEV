package Controller;

import Entites.Seance;
import Services.SeanceServices;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomeController {

    @FXML private BorderPane mainContainer;
    @FXML private VBox contentArea;
    @FXML private Button btnSeances;
    @FXML private Button btnRDV;
    @FXML private Button btnEmploi;
    @FXML private Label welcomeLabel;
    @FXML private Label statsSeances;
    @FXML private Label statsRDV;
    @FXML private Label statsEmploi;

    private SeanceServices seanceServices = new SeanceServices();
    private String currentModule = "home";

    @FXML
    public void initialize() {
        loadDashboardStats();
        animateWelcome();
    }

    private void loadDashboardStats() {
        // Load statistics for dashboard
        try {
            int seanceCount = seanceServices.afficherSeances().size();
            statsSeances.setText(seanceCount + " Séances");
        } catch (Exception e) {
            statsSeances.setText("0 Séances");
        }
    }

    private void animateWelcome() {
        FadeTransition fade = new FadeTransition(Duration.millis(1000), welcomeLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void showSeancesModule() {
        loadModule("/SeanceManagement.fxml", "seances");
        highlightActiveButton(btnSeances);
    }

    @FXML
    private void showRDVModule() {
        loadModule("/RDVManagement.fxml", "rdv");
        highlightActiveButton(btnRDV);
    }

    @FXML
    private void showEmploiModule() {
        loadModule("/EmploiManagement.fxml", "emploi");
        highlightActiveButton(btnEmploi);
    }

    private void loadModule(String fxmlPath, String moduleName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent moduleContent = loader.load();

            // Fade out current content
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(moduleContent);

                // Fade in new content
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();

            currentModule = moduleName;
        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger le module: " + e.getMessage());
        }
    }

    private void highlightActiveButton(Button activeBtn) {
        btnSeances.getStyleClass().remove("active");
        btnRDV.getStyleClass().remove("active");
        btnEmploi.getStyleClass().remove("active");
        activeBtn.getStyleClass().add("active");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

