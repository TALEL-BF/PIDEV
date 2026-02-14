package Controller;

import Entites.Seance;
import Services.SeanceServices;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeanceManagementController {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton filterTous;
    @FXML private ToggleButton filterPlanifiee;
    @FXML private ToggleButton filterConfirme;
    @FXML private ToggleButton filterTermine;
    @FXML private ToggleButton filterAnnule;

    private SeanceServices seanceServices = new SeanceServices();
    private List<Seance> allSeances;
    private ToggleGroup filterGroup;

    @FXML
    public void initialize() {
        setupFilters();
        loadSeances();
        setupSearch();
    }

    private void setupFilters() {
        filterGroup = new ToggleGroup();
        filterTous.setToggleGroup(filterGroup);
        filterPlanifiee.setToggleGroup(filterGroup);
        filterConfirme.setToggleGroup(filterGroup);
        filterTermine.setToggleGroup(filterGroup);
        filterAnnule.setToggleGroup(filterGroup);

        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                filterSeances();
            }
        });

        filterTous.setSelected(true);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filterSeances();
        });
    }

    private void loadSeances() {
        try {
            allSeances = seanceServices.afficherSeances();
            displaySeances(allSeances);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les séances: " + e.getMessage());
        }
    }

    private void filterSeances() {
        String searchText = searchField.getText().toLowerCase();
        Toggle selectedFilter = filterGroup.getSelectedToggle();
        String statut = "";

        if (selectedFilter == filterPlanifiee) statut = "planifiee";
        else if (selectedFilter == filterConfirme) statut = "confirme";
        else if (selectedFilter == filterTermine) statut = "termine";
        else if (selectedFilter == filterAnnule) statut = "annule";

        List<Seance> filtered;
        if (statut.isEmpty()) {
            filtered = allSeances;
        } else {
            filtered = seanceServices.afficherSeancesByStatut(statut);
        }

        // Apply search filter
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(s -> s.getTitreSeance().toLowerCase().contains(searchText) ||
                                 s.getDescription().toLowerCase().contains(searchText))
                    .toList();
        }

        displaySeances(filtered);
    }

    private void displaySeances(List<Seance> seances) {
        cardsPane.getChildren().clear();

        for (Seance seance : seances) {
            VBox card = createSeanceCard(seance);
            cardsPane.getChildren().add(card);

            // Entrance animation
            card.setOpacity(0);
            card.setTranslateY(20);
            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
            translate.setFromY(20);
            translate.setToY(0);

            ParallelTransition parallel = new ParallelTransition(fade, translate);
            parallel.setDelay(Duration.millis(cardsPane.getChildren().indexOf(card) * 50));
            parallel.play();
        }

        countLabel.setText(seances.size() + " Séance" + (seances.size() > 1 ? "s" : ""));
    }

    private VBox createSeanceCard(Seance seance) {
        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");
        card.setPrefWidth(360);

        // Header band
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(seance.getStatutSeance()));
        band.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(seance.getTitreSeance());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14;");

        Label typeLabel = new Label("Séance");
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11;");

        VBox titleBox = new VBox(2, title, typeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(getStatutLabel(seance.getStatutSeance()));
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        Label info = new Label(
                "DATE\n" + seance.getDateSeance().format(formatter) +
                "\n\nJOUR\n" + seance.getJoursSemaine() +
                "\n\nDURÉE\n" + seance.getDuree() + " minutes" +
                "\n\nDESCRIPTION\n" + seance.getDescription()
        );
        info.getStyleClass().add("suivie-info");
        info.setWrapText(true);

        // Actions
        HBox actions = new HBox(10);

        Button btnVoir = new Button("Voir");
        btnVoir.getStyleClass().add("btn-voir");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        btnVoir.setOnAction(e -> viewSeance(seance));
        HBox.setHgrow(btnVoir, Priority.ALWAYS);

        Button btnEdit = new Button("Éditer");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setOnAction(e -> editSeance(seance));
        HBox.setHgrow(btnEdit, Priority.ALWAYS);

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("button", "btn-delete");
        btnDelete.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 10;");
        btnDelete.setOnAction(e -> deleteSeance(seance));

        actions.getChildren().addAll(btnVoir, btnEdit, btnDelete);

        body.getChildren().addAll(info, actions);
        card.getChildren().addAll(band, body);

        // Hover effect
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

        return card;
    }

    private String getBandStyle(String statut) {
        return switch (statut.toLowerCase()) {
            case "confirme" -> "-fx-background-color: linear-gradient(to right, #56CCF2, #2F80ED);";
            case "planifiee" -> "-fx-background-color: linear-gradient(to right, #F2C94C, #F2994A);";
            case "termine" -> "-fx-background-color: linear-gradient(to right, #6FCF97, #27AE60);";
            case "annule" -> "-fx-background-color: linear-gradient(to right, #EB5757, #C92A2A);";
            case "reporte" -> "-fx-background-color: linear-gradient(to right, #BB6BD9, #9B51E0);";
            default -> "-fx-background-color: linear-gradient(to right, #8b5cf6, #a78bfa);";
        };
    }

    private String getStatutLabel(String statut) {
        return switch (statut.toLowerCase()) {
            case "planifiee" -> "Planifiée";
            case "confirme" -> "Confirmé";
            case "termine" -> "Terminé";
            case "annule" -> "Annulé";
            case "reporte" -> "Reporté";
            default -> statut;
        };
    }

    @FXML
    private void addNewSeance() {
        openSeanceForm(null);
    }

    private void viewSeance(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SeanceDetails.fxml"));
            Parent root = loader.load();

            SeanceDetailsController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Détails de la séance");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
        }
    }

    private void editSeance(Seance seance) {
        openSeanceForm(seance);
    }

    private void deleteSeance(Seance seance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la séance");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la séance \"" + seance.getTitreSeance() + "\" ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    seanceServices.supprimerSeance(seance.getIdSeance());
                    loadSeances();
                    showSuccess("Séance supprimée avec succès!");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer la séance: " + e.getMessage());
                }
            }
        });
    }

    private void openSeanceForm(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SeanceForm.fxml"));
            Parent root = loader.load();

            SeanceFormController controller = loader.getController();
            controller.setSeanceToEdit(seance);
            controller.setOnSaveCallback(() -> loadSeances());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(seance == null ? "Nouvelle Séance" : "Modifier Séance");
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

