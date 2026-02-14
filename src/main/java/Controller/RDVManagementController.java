package Controller;

import Entites.RDV;
import Services.RDVServices;
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

public class RDVManagementController {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton filterTous;
    @FXML private ToggleButton filterPlanifiee;
    @FXML private ToggleButton filterConfirme;
    @FXML private ToggleButton filterTermine;
    @FXML private ToggleButton filterAnnule;

    private RDVServices rdvServices = new RDVServices();
    private List<RDV> allRDVs;
    private ToggleGroup filterGroup;

    @FXML
    public void initialize() {
        setupFilters();
        loadRDVs();
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
            if (newVal != null) filterRDVs();
        });

        filterTous.setSelected(true);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterRDVs());
    }

    private void loadRDVs() {
        try {
            allRDVs = rdvServices.afficherRDV();
            displayRDVs(allRDVs);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les RDV: " + e.getMessage());
        }
    }

    private void filterRDVs() {
        String searchText = searchField.getText().toLowerCase();
        Toggle selectedFilter = filterGroup.getSelectedToggle();
        String statut = "";

        if (selectedFilter == filterPlanifiee) statut = "planifiee";
        else if (selectedFilter == filterConfirme) statut = "confirme";
        else if (selectedFilter == filterTermine) statut = "termine";
        else if (selectedFilter == filterAnnule) statut = "annule";

        List<RDV> filtered = statut.isEmpty() ? allRDVs : rdvServices.afficherRDVByStatut(statut);

        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(r -> r.getTypeConsultation().toLowerCase().contains(searchText))
                    .toList();
        }

        displayRDVs(filtered);
    }

    private void displayRDVs(List<RDV> rdvs) {
        cardsPane.getChildren().clear();

        for (RDV rdv : rdvs) {
            VBox card = createRDVCard(rdv);
            cardsPane.getChildren().add(card);
            animateCard(card, cardsPane.getChildren().indexOf(card));
        }

        countLabel.setText(rdvs.size() + " Rendez-vous");
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

    private VBox createRDVCard(RDV rdv) {
        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");
        card.setPrefWidth(360);

        // Header
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(rdv.getStatutRdv()));
        band.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(getTypeLabel(rdv.getTypeConsultation()));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14;");

        Label typeLabel = new Label("Rendez-vous");
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11;");

        VBox titleBox = new VBox(2, title, typeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(getStatutLabel(rdv.getStatutRdv()));
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
                "DATE & HEURE\n" + rdv.getDateHeureRdv().format(formatter) +
                "\n\nDURÉE\n" + rdv.getDureeRdvMinutes() + " minutes" +
                "\n\nPSYCHOLOGUE\nID: " + rdv.getIdPsychologue() +
                "\n\nAUTISTE\nID: " + rdv.getIdAutiste()
        );
        info.getStyleClass().add("suivie-info");

        // Actions
        HBox actions = new HBox(10);

        Button btnVoir = new Button("Voir");
        btnVoir.getStyleClass().add("btn-voir");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        btnVoir.setOnAction(e -> viewRDV(rdv));
        HBox.setHgrow(btnVoir, Priority.ALWAYS);

        Button btnEdit = new Button("Éditer");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setOnAction(e -> editRDV(rdv));
        HBox.setHgrow(btnEdit, Priority.ALWAYS);

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("button", "btn-delete");
        btnDelete.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 10;");
        btnDelete.setOnAction(e -> deleteRDV(rdv));

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

    private String getTypeLabel(String type) {
        return switch (type.toLowerCase()) {
            case "premiere_consultation" -> "Première Consultation";
            case "suivi" -> "Suivi";
            case "urgence" -> "Urgence";
            case "familiale" -> "Consultation Familiale";
            case "bilan" -> "Bilan";
            default -> type;
        };
    }

    @FXML
    private void addNewRDV() {
        openRDVForm(null);
    }

    private void viewRDV(RDV rdv) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du RDV");
        alert.setHeaderText(getTypeLabel(rdv.getTypeConsultation()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String content = String.format("""
                Date et heure: %s
                Durée: %d minutes
                Statut: %s
                ID Psychologue: %d
                ID Autiste: %d
                """,
                rdv.getDateHeureRdv().format(formatter),
                rdv.getDureeRdvMinutes(),
                getStatutLabel(rdv.getStatutRdv()),
                rdv.getIdPsychologue(),
                rdv.getIdAutiste()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void editRDV(RDV rdv) {
        openRDVForm(rdv);
    }

    private void deleteRDV(RDV rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le RDV");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    rdvServices.supprimerRDV(rdv.getIdRdv());
                    loadRDVs();
                    showSuccess("RDV supprimé avec succès!");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer le RDV: " + e.getMessage());
                }
            }
        });
    }

    private void openRDVForm(RDV rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RDVForm.fxml"));
            Parent root = loader.load();

            RDVFormController controller = loader.getController();
            controller.setRDVToEdit(rdv);
            controller.setOnSaveCallback(() -> loadRDVs());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(rdv == null ? "Nouveau RDV" : "Modifier RDV");
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

