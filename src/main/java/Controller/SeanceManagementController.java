package Controller;

import Entites.Seance;
import Services.SeanceServices;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeanceManagementController {

    @FXML private TableView<Seance> seanceTable;
    @FXML private TableColumn<Seance, String> colTitre;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colJours;
    @FXML private TableColumn<Seance, Integer> colDuree;
    @FXML private TableColumn<Seance, String> colStatut;
    @FXML private TableColumn<Seance, Void> colActions;

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
        setupTable();
        setupFilters();
        loadSeances();
        setupSearch();
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreSeance"));
        colJours.setCellValueFactory(new PropertyValueFactory<>("joursSemaine"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutSeance"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        colDate.setCellFactory(column -> new TableCell<Seance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(((Seance) getTableRow().getItem()).getDateSeance().format(formatter));
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 5;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5;");

                btnEdit.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    editSeance(seance);
                });

                btnDelete.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    deleteSeance(seance);
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
            filterSeances();
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

        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(s -> s.getTitreSeance().toLowerCase().contains(searchText) ||
                                 s.getDescription().toLowerCase().contains(searchText))
                    .toList();
        }

        displaySeances(filtered);
    }

    private void displaySeances(List<Seance> seances) {
        ObservableList<Seance> data = FXCollections.observableArrayList(seances);
        seanceTable.setItems(data);
        countLabel.setText(seances.size() + " Séance" + (seances.size() > 1 ? "s" : ""));
    }

    @FXML
    private void addNewSeance() {
        openSeanceForm(null);
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
