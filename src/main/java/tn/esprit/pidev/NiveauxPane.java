package tn.esprit.pidev;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NiveauxPane {

    private final NiveauJeuService service = new NiveauJeuService();
    private final ObservableList<NiveauJeu> data = FXCollections.observableArrayList();

    private TableView<NiveauJeu> table;
    private TextField tfLibelle, tfMin, tfMax, tfDesc, tfSearch;
    private Label lblSelectedId;

    public Parent getRoot() {

        tfLibelle = new TextField();
        tfLibelle.setPromptText("Libellé (FACILE/MOYEN/DIFFICILE)");

        tfMin = new TextField();
        tfMin.setPromptText("Min");

        tfMax = new TextField();
        tfMax.setPromptText("Max");

        tfDesc = new TextField();
        tfDesc.setPromptText("Description (optionnel)");

        tfSearch = new TextField();
        tfSearch.setPromptText("Recherche ID / Libellé");

        lblSelectedId = new Label("ID: (aucun)");
        lblSelectedId.setStyle("-fx-font-weight: bold;");

        Button btnAjouter = new Button("Ajouter");
        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");
        Button btnClear = new Button("Clear");

        table = new TableView<>();

        TableColumn<NiveauJeu, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(70);

        TableColumn<NiveauJeu, String> colLib = new TableColumn<>("Libellé");
        colLib.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colLib.setPrefWidth(160);

        TableColumn<NiveauJeu, Double> colMin = new TableColumn<>("Min");
        colMin.setCellValueFactory(new PropertyValueFactory<>("minMoyenne"));
        colMin.setPrefWidth(120);

        TableColumn<NiveauJeu, Double> colMax = new TableColumn<>("Max");
        colMax.setCellValueFactory(new PropertyValueFactory<>("maxMoyenne"));
        colMax.setPrefWidth(120);

        TableColumn<NiveauJeu, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(360);

        table.getColumns().addAll(colId, colLib, colMin, colMax, colDesc);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        refreshAll();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblSelectedId.setText("ID: " + selected.getId());
                tfLibelle.setText(selected.getLibelle());
                tfMin.setText(String.valueOf(selected.getMinMoyenne()));
                tfMax.setText(String.valueOf(selected.getMaxMoyenne()));
                tfDesc.setText(selected.getDescription() == null ? "" : selected.getDescription());
            }
        });

        tfSearch.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) refreshAll();
            else data.setAll(service.search(val));
        });

        btnAjouter.setOnAction(e -> {
            String lib = safe(tfLibelle.getText()).toUpperCase();
            String sMin = safe(tfMin.getText());
            String sMax = safe(tfMax.getText());
            String desc = safe(tfDesc.getText());
            if (desc.isEmpty()) desc = null;

            if (lib.isEmpty()) { alert("Erreur", "Libellé obligatoire."); return; }
            if (!isDouble(sMin) || !isDouble(sMax)) { alert("Erreur", "Min/Max doivent être des nombres."); return; }

            double min = Double.parseDouble(sMin);
            double max = Double.parseDouble(sMax);

            if (min > max) { alert("Erreur", "Min doit être <= Max."); return; }

            boolean ok = service.add(new NiveauJeu(0, lib, min, max, desc));
            if (ok) { clear(); refreshAll(); }
            else alert("Erreur", "Insertion échouée (DB).");
        });

        btnModifier.setOnAction(e -> {
            NiveauJeu selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Erreur", "Sélectionne une ligne."); return; }

            String lib = safe(tfLibelle.getText()).toUpperCase();
            String sMin = safe(tfMin.getText());
            String sMax = safe(tfMax.getText());
            String desc = safe(tfDesc.getText());
            if (desc.isEmpty()) desc = null;

            if (lib.isEmpty()) { alert("Erreur", "Libellé obligatoire."); return; }
            if (!isDouble(sMin) || !isDouble(sMax)) { alert("Erreur", "Min/Max doivent être des nombres."); return; }

            double min = Double.parseDouble(sMin);
            double max = Double.parseDouble(sMax);

            if (min > max) { alert("Erreur", "Min doit être <= Max."); return; }

            boolean ok = service.update(new NiveauJeu(selected.getId(), lib, min, max, desc));
            if (ok) { clear(); refreshAll(); }
            else alert("Erreur", "Update échoué.");
        });

        btnSupprimer.setOnAction(e -> {
            NiveauJeu selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Erreur", "Sélectionne une ligne."); return; }

            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Confirmation");
            c.setHeaderText("Supprimer ID " + selected.getId() + " ?");
            c.setContentText(selected.getLibelle());

            c.showAndWait().ifPresent(b -> {
                if (b == ButtonType.OK) {
                    boolean ok = service.delete(selected.getId());
                    if (ok) { clear(); refreshAll(); }
                    else alert("Erreur", "Delete échoué.");
                }
            });
        });

        btnClear.setOnAction(e -> clear());

        HBox row1 = new HBox(10, tfLibelle, tfMin, tfMax, tfDesc, btnAjouter, btnModifier, btnSupprimer, btnClear);
        row1.setPadding(new Insets(10));

        HBox row2 = new HBox(10, new Label("Recherche:"), tfSearch, lblSelectedId);
        row2.setPadding(new Insets(0, 10, 10, 10));

        VBox root = new VBox(10, row1, row2, table);
        root.setPadding(new Insets(10));
        return root;
    }

    public void clearForm() { clear(); }

    private void refreshAll() { data.setAll(service.getAll()); }

    private void clear() {
        table.getSelectionModel().clearSelection();
        lblSelectedId.setText("ID: (aucun)");
        tfLibelle.clear();
        tfMin.clear();
        tfMax.clear();
        tfDesc.clear();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static boolean isDouble(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        try { Double.parseDouble(s.trim()); return true; }
        catch (Exception e) { return false; }
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
