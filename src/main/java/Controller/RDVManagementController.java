package Controller;

import Entites.RDV;
import Services.RDVServices;
import Services.RdvNotificationService;
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

public class RDVManagementController {

    @FXML private TableView<RDV> rdvTable;
    @FXML private TableColumn<RDV, String> colType;
    @FXML private TableColumn<RDV, String> colDate;
    @FXML private TableColumn<RDV, Integer> colDuree;
    @FXML private TableColumn<RDV, String> colStatut;
    @FXML private TableColumn<RDV, Void> colActions;

    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton filterTous;
    @FXML private ToggleButton filterPlanifiee;
    @FXML private ToggleButton filterConfirme;
    @FXML private ToggleButton filterTermine;
    @FXML private ToggleButton filterAnnule;

    private RDVServices rdvServices = new RDVServices();
    private RdvNotificationService notificationService = new RdvNotificationService();
    private List<RDV> allRDVs;
    private ToggleGroup filterGroup;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadRDVs();
        setupSearch();
    }

    private void setupTable() {
        colType.setCellValueFactory(new PropertyValueFactory<>("typeConsultation"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeRdvMinutes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutRdv"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        colDate.setCellFactory(column -> new TableCell<RDV, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(((RDV) getTableRow().getItem()).getDateHeureRdv().format(formatter));
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
                    RDV rdv = getTableView().getItems().get(getIndex());
                    editRDV(rdv);
                });

                btnDelete.setOnAction(event -> {
                    RDV rdv = getTableView().getItems().get(getIndex());
                    deleteRDV(rdv);
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
            if (allRDVs != null && !allRDVs.isEmpty()) {
                notificationService.checkAndSendNotifications(allRDVs);
            }
            filterRDVs();
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
        ObservableList<RDV> data = FXCollections.observableArrayList(rdvs);
        rdvTable.setItems(data);
        countLabel.setText(rdvs.size() + " Rendez-vous");
    }

    @FXML
    private void addNewRDV() {
        openRDVForm(null);
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
