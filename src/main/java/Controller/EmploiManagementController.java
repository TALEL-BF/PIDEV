package Controller;

import Entites.EmploiDuTemps;
import Entites.RDV;
import Entites.Seance;
import Services.EmploiDuTempsServices;
import Services.RDVServices;
import Services.SeanceServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EmploiManagementController {

    @FXML private TableView<EmploiDuTemps> emploiTable;
    @FXML private TableColumn<EmploiDuTemps, String> colAnnee;
    @FXML private TableColumn<EmploiDuTemps, String> colJour;
    @FXML private TableColumn<EmploiDuTemps, String> colTranche;
    @FXML private TableColumn<EmploiDuTemps, String> colActivite;
    @FXML private TableColumn<EmploiDuTemps, Void> colActions;

    @FXML private Label countLabel;
    @FXML private ComboBox<String> filterJour;
    @FXML private ComboBox<String> filterTranche;

    private EmploiDuTempsServices emploiServices = new EmploiDuTempsServices();
    private RDVServices rdvServices = new RDVServices();
    private SeanceServices seanceServices = new SeanceServices();

    private List<EmploiDuTemps> allEmplois;
    private Map<Integer, RDV> rdvMap;
    private Map<Integer, Seance> seanceMap;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("anneeScolaire"));
        colJour.setCellValueFactory(new PropertyValueFactory<>("jourSemaine"));
        colTranche.setCellValueFactory(new PropertyValueFactory<>("trancheHoraire"));

        // Custom factory for Activity column
        colActivite.setCellValueFactory(cellData -> {
            EmploiDuTemps emploi = cellData.getValue();
            String title = "Aucune";

            if (emploi.getIdRdv() != null && rdvMap != null && rdvMap.containsKey(emploi.getIdRdv())) {
                RDV rdv = rdvMap.get(emploi.getIdRdv());
                title = "📅 RDV: " + rdv.getTypeConsultation();
            } else if (emploi.getIdSeance() != null && seanceMap != null && seanceMap.containsKey(emploi.getIdSeance())) {
                Seance seance = seanceMap.get(emploi.getIdSeance());
                title = "📚 Séance: " + seance.getTitreSeance();
            }

            return new SimpleStringProperty(title);
        });

        // Custom factory for Actions column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 5;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5;");

                btnEdit.setOnAction(event -> {
                    EmploiDuTemps emploi = getTableView().getItems().get(getIndex());
                    openEmploiForm(emploi);
                });

                btnDelete.setOnAction(event -> {
                    EmploiDuTemps emploi = getTableView().getItems().get(getIndex());
                    deleteEmploi(emploi);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupFilters() {
        filterJour.getItems().addAll("Tous", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche");
        filterTranche.getItems().addAll("Tous", "matin", "apres_midi", "soir", "journee");

        filterJour.getSelectionModel().selectFirst();
        filterTranche.getSelectionModel().selectFirst();

        filterJour.valueProperty().addListener((obs, old, newVal) -> filterEmplois());
        filterTranche.valueProperty().addListener((obs, old, newVal) -> filterEmplois());
    }

    private void loadData() {
        try {
            // Pre-load reference data for lookups
            List<RDV> rdvs = rdvServices.afficherRDV();
            rdvMap = rdvs.stream().collect(Collectors.toMap(RDV::getIdRdv, Function.identity()));

            List<Seance> seances = seanceServices.afficherSeances();
            seanceMap = seances.stream().collect(Collectors.toMap(Seance::getIdSeance, Function.identity()));

            loadEmplois();

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void loadEmplois() {
        try {
            allEmplois = emploiServices.afficherEmplois();
            filterEmplois(); // Apply filters and display
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
                    .filter(e -> e.getJourSemaine().equalsIgnoreCase(jour)) // Use equalsIgnoreCase for safety
                    .toList(); // Java 16+
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
        ObservableList<EmploiDuTemps> data = FXCollections.observableArrayList(emplois);
        emploiTable.setItems(data);
        countLabel.setText(emplois.size() + " Emploi" + (emplois.size() > 1 ? "s" : ""));
    }

    @FXML
    private void addNewEmploi() {
        openEmploiForm(null);
    }

    private void openEmploiForm(EmploiDuTemps emploi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmploiForm.fxml"));
            Parent root = loader.load();

            EmploiFormController controller = loader.getController();
            controller.setEmploiToEdit(emploi);
            controller.setOnSaveCallback(this::loadEmplois);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(emploi == null ? "Nouvel Emploi" : "Modifier Emploi");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
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
                    loadEmplois(); // Refresh table
                } catch (Exception e) {
                    showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void switchToCalendar() {
        // Implementation similar to previous logic, simplified
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmploiCalendar.fxml"));
            Parent root = loader.load();

            // Try to find the nearest BorderPane (likely in Home.fxml)
             if (emploiTable.getScene() != null && emploiTable.getScene().getRoot() instanceof javafx.scene.layout.BorderPane) { // Check scene from table
                 ((javafx.scene.layout.BorderPane) emploiTable.getScene().getRoot()).setCenter(root);
             } else {
                 // Fallback: If not in a BorderPane, maybe just replace the root scene?
                 // Or open a new window if that's safer.
                 // Let's assume the BorderPane strategy works as in other controllers.
                 if (countLabel.getScene() != null) {
                      countLabel.getScene().setRoot(root);
                 }
             }

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
        }
    }



    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
