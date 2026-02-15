package tn.esprit.pidev;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainFX extends Application {

    private final NiveauJeuService service = new NiveauJeuService();
    private final ObservableList<NiveauJeu> data = FXCollections.observableArrayList();

    private TableView<NiveauJeu> table;
    private TextField tfLibelle, tfMin, tfMax, tfDesc, tfSearch;
    private Label lblSelectedId;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gestion des Niveaux");

        // Champs
        tfLibelle = new TextField();
        tfLibelle.setPromptText("Libellé (ex: FACILE)");

        tfMin = new TextField();
        tfMin.setPromptText("Min moyenne (ex: 0)");

        tfMax = new TextField();
        tfMax.setPromptText("Max moyenne (ex: 10)");

        tfDesc = new TextField();
        tfDesc.setPromptText("Description (optionnel)");

        tfSearch = new TextField();
        tfSearch.setPromptText("Rechercher par ID ou Libellé...");

        lblSelectedId = new Label("ID: (aucun)");
        lblSelectedId.setStyle("-fx-font-weight: bold;");

        // Boutons
        Button btnAjouter = new Button("Ajouter");
        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");
        Button btnClear = new Button("Clear");

        // Table
        table = new TableView<>();

        TableColumn<NiveauJeu, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<NiveauJeu, String> colLib = new TableColumn<>("Libellé");
        colLib.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colLib.setPrefWidth(170);

        // ✅ propriétés minMoyenne / maxMoyenne (et non min/max)
        TableColumn<NiveauJeu, Double> colMin = new TableColumn<>("Min");
        colMin.setCellValueFactory(new PropertyValueFactory<>("minMoyenne"));
        colMin.setPrefWidth(110);

        TableColumn<NiveauJeu, Double> colMax = new TableColumn<>("Max");
        colMax.setCellValueFactory(new PropertyValueFactory<>("maxMoyenne"));
        colMax.setPrefWidth(110);

        TableColumn<NiveauJeu, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(300);

        table.getColumns().addAll(colId, colLib, colMin, colMax, colDesc);
        table.setItems(data);

        // Charger données
        refreshAll();

        // Remplir les champs quand on sélectionne
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblSelectedId.setText("ID: " + selected.getId());
                tfLibelle.setText(selected.getLibelle());
                tfMin.setText(String.valueOf(selected.getMinMoyenne()));
                tfMax.setText(String.valueOf(selected.getMaxMoyenne()));
                tfDesc.setText(selected.getDescription() == null ? "" : selected.getDescription());
            }
        });

        // Recherche live
        tfSearch.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                refreshAll();
            } else {
                data.setAll(service.search(val));
            }
        });

        // Ajouter
        btnAjouter.setOnAction(e -> {
            try {
                String lib = tfLibelle.getText().trim();
                double min = Double.parseDouble(tfMin.getText().trim());
                double max = Double.parseDouble(tfMax.getText().trim());
                String desc = tfDesc.getText().trim();
                if (desc.isEmpty()) desc = null;

                if (lib.isEmpty()) {
                    alert("Erreur", "Libellé est obligatoire.");
                    return;
                }
                if (min > max) {
                    alert("Erreur", "Min ne peut pas être > Max.");
                    return;
                }

                lib = lib.toUpperCase();

                boolean ok = service.add(new NiveauJeu(0, lib, min, max, desc));
                if (ok) {
                    System.out.println("Niveau ajouté !");
                    clearFields();
                    refreshAll();
                } else {
                    alert("Erreur", "Insertion échouée (vérifie la connexion DB).");
                }
            } catch (NumberFormatException ex) {
                alert("Erreur", "Min/Max doivent être des nombres (ex: 10 ou 10.5).");
            }
        });

        // Modifier
        btnModifier.setOnAction(e -> {
            NiveauJeu selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Erreur", "Sélectionne une ligne à modifier.");
                return;
            }

            try {
                String lib = tfLibelle.getText().trim();
                double min = Double.parseDouble(tfMin.getText().trim());
                double max = Double.parseDouble(tfMax.getText().trim());
                String desc = tfDesc.getText().trim();
                if (desc.isEmpty()) desc = null;

                if (lib.isEmpty()) {
                    alert("Erreur", "Libellé est obligatoire.");
                    return;
                }
                if (min > max) {
                    alert("Erreur", "Min ne peut pas être > Max.");
                    return;
                }

                lib = lib.toUpperCase();

                NiveauJeu updated = new NiveauJeu(selected.getId(), lib, min, max, desc);
                boolean ok = service.update(updated);
                if (ok) {
                    System.out.println("Niveau modifié !");
                    clearFields();
                    refreshAll();
                } else {
                    alert("Erreur", "Update échoué.");
                }

            } catch (NumberFormatException ex) {
                alert("Erreur", "Min/Max doivent être des nombres (ex: 10 ou 10.5).");
            }
        });

        // Supprimer
        btnSupprimer.setOnAction(e -> {
            NiveauJeu selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Erreur", "Sélectionne une ligne à supprimer.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer le niveau ID " + selected.getId() + " ?");
            confirm.setContentText("Libellé: " + selected.getLibelle());

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    boolean ok = service.delete(selected.getId());
                    if (ok) {
                        System.out.println("Niveau supprimé !");
                        clearFields();
                        refreshAll();
                    } else {
                        alert("Erreur", "Delete échoué.");
                    }
                }
            });
        });

        btnClear.setOnAction(e -> clearFields());

        // Layout
        HBox topRow = new HBox(10, tfLibelle, tfMin, tfMax, tfDesc, btnAjouter, btnModifier, btnSupprimer, btnClear);
        topRow.setPadding(new Insets(10));

        HBox searchRow = new HBox(10, new Label("Recherche:"), tfSearch, lblSelectedId);
        searchRow.setPadding(new Insets(0, 10, 10, 10));

        VBox root = new VBox(10, topRow, searchRow, table);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1100, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshAll() {
        data.setAll(service.getAll());
    }

    private void clearFields() {
        table.getSelectionModel().clearSelection();
        lblSelectedId.setText("ID: (aucun)");
        tfLibelle.clear();
        tfMin.clear();
        tfMax.clear();
        tfDesc.clear();
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
