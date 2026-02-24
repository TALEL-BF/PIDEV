package Controller;

import Entites.EmploiDuTemps;
import Services.EmploiDuTempsServices;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

public class EmploiCardsController {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private ComboBox<String> filterJour;
    @FXML private ComboBox<String> filterTranche;

    private EmploiDuTempsServices emploiServices = new EmploiDuTempsServices();
    private List<EmploiDuTemps> allEmplois;

    @FXML
    public void initialize() {
        setupFilters();
        loadEmplois();
    }

    private void setupFilters() {
        filterJour.getItems().addAll("Tous", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche");
        filterTranche.getItems().addAll("Tous", "matin", "apres_midi", "soir", "journee");

        filterJour.getSelectionModel().selectFirst();
        filterTranche.getSelectionModel().selectFirst();

        filterJour.valueProperty().addListener((obs, old, newVal) -> filterEmplois());
        filterTranche.valueProperty().addListener((obs, old, newVal) -> filterEmplois());
    }

    private void loadEmplois() {
        try {
            allEmplois = emploiServices.afficherEmplois();
            displayEmplois(allEmplois);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les emplois: " + e.getMessage());
        }
    }

    private void filterEmplois() {
        String jour = filterJour.getValue();
        String tranche = filterTranche.getValue();

        List<EmploiDuTemps> filtered = allEmplois;

        if (jour != null && !jour.equals("Tous")) {
            filtered = filtered.stream()
                    .filter(e -> e.getJourSemaine().equalsIgnoreCase(jour))
                    .toList();
        }

        if (tranche != null && !tranche.equals("Tous")) {
            String finalTranche = tranche;
            filtered = filtered.stream()
                    .filter(e -> e.getTrancheHoraire().equalsIgnoreCase(finalTranche))
                    .toList();
        }

        displayEmplois(filtered);
    }

    private void displayEmplois(List<EmploiDuTemps> emplois) {
        cardsPane.getChildren().clear();

        for (EmploiDuTemps emploi : emplois) {
            VBox card = createEmploiCard(emploi);
            cardsPane.getChildren().add(card);
            animateCard(card, cardsPane.getChildren().indexOf(card));
        }

        countLabel.setText(emplois.size() + " Emploi" + (emplois.size() > 1 ? "s" : ""));
    }

    private void animateCard(VBox card, int index) {
        card.setOpacity(0);
        card.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
        translate.setFromY(20);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.setDelay(Duration.millis(index * 50));
        parallel.play();
    }

    private VBox createEmploiCard(EmploiDuTemps emploi) {
        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 0 0 15 0;");

        // Header
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(emploi.getTrancheHoraire()));
        band.setAlignment(Pos.CENTER_LEFT);
        band.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));

        Label title = new Label(capitalizeFirst(emploi.getJourSemaine()));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(getTrancheLabel(emploi.getTrancheHoraire()));
        badge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-weight: 700; -fx-font-size: 11;");

        band.getChildren().addAll(title, spacer, badge);

        // Body
        VBox body = new VBox(10);
        body.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));

        String typeActivite = "";
        if (emploi.getIdSeance() != null) {
            typeActivite = "📚 Séance";
        } else if (emploi.getIdRdv() != null) {
            typeActivite = "📅 RDV";
        } else {
            typeActivite = "Aucune activité";
        }

        Label info = new Label(
                "ANNÉE: " + emploi.getAnneeScolaire() + "\n" +
                "ACTIVITÉ: " + typeActivite
        );
        info.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13; -fx-line-spacing: 4;");
        info.setWrapText(true);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnVoir = new Button("Voir");
        btnVoir.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: 700; -fx-cursor: hand;");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        btnVoir.setOnAction(e -> viewEmploi(emploi));
        HBox.setHgrow(btnVoir, Priority.ALWAYS);

        actions.getChildren().addAll(btnVoir); // Only btnVoir

        body.getChildren().addAll(info, actions);
        card.getChildren().addAll(band, body);

        addHoverEffect(card);

        return card;
    }

    private void addHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    private String getBandStyle(String tranche) {
        return switch (tranche.toLowerCase()) {
            case "matin" -> "-fx-background-color: linear-gradient(to right, #fbbf24, #f59e0b); -fx-background-radius: 15 15 0 0;";
            case "apres_midi" -> "-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-background-radius: 15 15 0 0;";
            case "soir" -> "-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-background-radius: 15 15 0 0;";
            case "journee" -> "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2); -fx-background-radius: 15 15 0 0;";
            default -> "-fx-background-color: linear-gradient(to right, #6cda95, #27AE60); -fx-background-radius: 15 15 0 0;";
        };
    }

    private String getTrancheLabel(String tranche) {
        return switch (tranche.toLowerCase()) {
            case "matin" -> "☀️ Matin";
            case "apres_midi" -> "🌤️ Après-midi";
            case "soir" -> "🌙 Soir";
            case "journee" -> "📆 Journée";
            default -> tranche;
        };
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void viewEmploi(EmploiDuTemps emploi) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails Emploi du Temps");
        alert.setHeaderText(capitalizeFirst(emploi.getJourSemaine()) + " - " + getTrancheLabel(emploi.getTrancheHoraire()));

        String content = String.format("""
                Année scolaire: %s
                Jour: %s
                Tranche horaire: %s
                ID RDV: %s
                ID Séance: %s
                """,
                emploi.getAnneeScolaire(),
                capitalizeFirst(emploi.getJourSemaine()),
                getTrancheLabel(emploi.getTrancheHoraire()),
                emploi.getIdRdv() != null ? emploi.getIdRdv() : "Non défini",
                emploi.getIdSeance() != null ? emploi.getIdSeance() : "Non défini"
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
