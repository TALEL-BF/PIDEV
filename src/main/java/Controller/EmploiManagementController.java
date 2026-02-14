package Controller;

import Entites.EmploiDuTemps;
import Services.EmploiDuTempsServices;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class EmploiManagementController {

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
            filtered = emploiServices.afficherEmploisByJour(jour);
        }

        if (tranche != null && !tranche.equals("Tous")) {
            String finalTranche = tranche;
            filtered = filtered.stream()
                    .filter(e -> e.getTrancheHoraire().equals(finalTranche))
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
        card.setPrefWidth(360);

        // Header
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(emploi.getTrancheHoraire()));
        band.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(capitalizeFirst(emploi.getJourSemaine()));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14;");

        Label typeLabel = new Label("Emploi du Temps");
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11;");

        VBox titleBox = new VBox(2, title, typeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(getTrancheLabel(emploi.getTrancheHoraire()));
        badge.setStyle("""
            -fx-background-color: rgba(255,255,255,0.22);
            -fx-text-fill: white;
            -fx-padding: 4 10;
            -fx-background-radius: 999;
            -fx-font-weight: 700;
            -fx-font-size: 11;
        """);

        band.getChildren().addAll(titleBox, spacer, badge);

        // Body
        VBox body = new VBox(10);
        body.getStyleClass().add("suivie-body");

        String typeActivite = "";
        if (emploi.getIdSeance() != null) {
            typeActivite = "📚 Séance (ID: " + emploi.getIdSeance() + ")";
        } else if (emploi.getIdRdv() != null) {
            typeActivite = "📅 RDV (ID: " + emploi.getIdRdv() + ")";
        } else {
            typeActivite = "Aucune activité liée";
        }

        Label info = new Label(
                "ANNÉE SCOLAIRE\n" + emploi.getAnneeScolaire() +
                "\n\nTRANCHE HORAIRE\n" + getTrancheLabel(emploi.getTrancheHoraire()) +
                "\n\nACTIVITÉ\n" + typeActivite
        );
        info.getStyleClass().add("suivie-info");

        // Actions
        HBox actions = new HBox(10);

        Button btnVoir = new Button("Voir");
        btnVoir.getStyleClass().add("btn-voir");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        btnVoir.setOnAction(e -> viewEmploi(emploi));
        HBox.setHgrow(btnVoir, Priority.ALWAYS);

        Button btnEdit = new Button("Éditer");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setOnAction(e -> editEmploi(emploi));
        HBox.setHgrow(btnEdit, Priority.ALWAYS);

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("button", "btn-delete");
        btnDelete.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 10;");
        btnDelete.setOnAction(e -> deleteEmploi(emploi));

        actions.getChildren().addAll(btnVoir, btnEdit, btnDelete);

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
            case "matin" -> "-fx-background-color: linear-gradient(to right, #fbbf24, #f59e0b);";
            case "apres_midi" -> "-fx-background-color: linear-gradient(to right, #10b981, #059669);";
            case "soir" -> "-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed);";
            case "journee" -> "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2);";
            default -> "-fx-background-color: linear-gradient(to right, #6cda95, #27AE60);";
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

    @FXML
    private void addNewEmploi() {
        openEmploiForm(null);
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

    private void editEmploi(EmploiDuTemps emploi) {
        openEmploiForm(emploi);
    }

    private void deleteEmploi(EmploiDuTemps emploi) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'emploi du temps");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet emploi du temps ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    emploiServices.supprimerEmploi(emploi.getIdEmploi());
                    loadEmplois();
                    showSuccess("Emploi supprimé avec succès!");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'emploi: " + e.getMessage());
                }
            }
        });
    }

    private void openEmploiForm(EmploiDuTemps emploi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmploiForm.fxml"));
            Parent root = loader.load();

            EmploiFormController controller = loader.getController();
            controller.setEmploiToEdit(emploi);
            controller.setOnSaveCallback(() -> loadEmplois());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(emploi == null ? "Nouvel Emploi du Temps" : "Modifier Emploi du Temps");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
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

