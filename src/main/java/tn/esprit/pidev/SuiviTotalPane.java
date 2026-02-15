package tn.esprit.pidev;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.UnaryOperator;

public class SuiviTotalPane {

    private final SuiviTotalService service = new SuiviTotalService();
    private final ObservableList<SuiviTotal> data = FXCollections.observableArrayList();

    private TableView<SuiviTotal> table;
    private TextField tfEnfantId, tfEval, tfConsult, tfSearch;
    private Label lblSelectedId;

    public Parent getRoot() {

        tfEnfantId = new TextField();
        tfEnfantId.setPromptText("Enfant ID (ex: 12)");
        tfEnfantId.getStyleClass().add("input");

        tfEval = new TextField();
        tfEval.setPromptText("Note évaluation (ex: 12.5)");
        tfEval.getStyleClass().add("input");

        tfConsult = new TextField();
        tfConsult.setPromptText("Note consultation (ex: 14)");
        tfConsult.getStyleClass().add("input");

        tfSearch = new TextField();
        tfSearch.setPromptText("Recherche ID / Enfant / Niveau");
        tfSearch.getStyleClass().add("search");

        lblSelectedId = new Label("ID: (aucun)");
        lblSelectedId.getStyleClass().add("counter-label");

        Button btnAjouter = new Button("Ajouter");
        btnAjouter.getStyleClass().add("btn-primary");

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().add("btn-soft");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-soft");

        // Contrôle de saisie
        applyIntFilter(tfEnfantId);
        applyDoubleFilter(tfEval);
        applyDoubleFilter(tfConsult);

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SuiviTotal, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<SuiviTotal, Integer> colEnfant = new TableColumn<>("Enfant");
        colEnfant.setCellValueFactory(new PropertyValueFactory<>("enfantId"));

        TableColumn<SuiviTotal, Double> colEval = new TableColumn<>("Évaluation");
        colEval.setCellValueFactory(new PropertyValueFactory<>("noteEvaluation"));

        TableColumn<SuiviTotal, Double> colCons = new TableColumn<>("Consultation");
        colCons.setCellValueFactory(new PropertyValueFactory<>("noteConsultation"));

        TableColumn<SuiviTotal, Double> colMoy = new TableColumn<>("Moyenne");
        colMoy.setCellValueFactory(new PropertyValueFactory<>("moyenne"));

        TableColumn<SuiviTotal, String> colNiv = new TableColumn<>("Niveau");
        colNiv.setCellValueFactory(new PropertyValueFactory<>("niveauLibelle"));

        table.getColumns().addAll(colId, colEnfant, colEval, colCons, colMoy, colNiv);
        table.setItems(data);

        refreshAll();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblSelectedId.setText("ID: " + selected.getId());
                tfEnfantId.setText(String.valueOf(selected.getEnfantId()));
                tfEval.setText(String.valueOf(selected.getNoteEvaluation()));
                tfConsult.setText(String.valueOf(selected.getNoteConsultation()));
                validateLive();
            }
        });

        tfSearch.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) refreshAll();
            else data.setAll(service.search(val));
        });

        // live validation
        tfEnfantId.textProperty().addListener((o,a,b)-> validateLive());
        tfEval.textProperty().addListener((o,a,b)-> validateLive());
        tfConsult.textProperty().addListener((o,a,b)-> validateLive());

        btnAjouter.setOnAction(e -> {
            if (!validateSubmit()) return;

            int enfantId = Integer.parseInt(tfEnfantId.getText().trim());
            double eval = parseDouble(tfEval);
            double cons = parseDouble(tfConsult);

            boolean ok = service.add(enfantId, eval, cons);
            if (ok) { clear(); refreshAll(); }
            else alert("Erreur", "Insertion échouée (DB). Vérifie que les niveaux existent et couvrent la moyenne.");
        });

        btnModifier.setOnAction(e -> {
            SuiviTotal selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Erreur", "Sélectionne une ligne."); return; }
            if (!validateSubmit()) return;

            int enfantId = Integer.parseInt(tfEnfantId.getText().trim());
            double eval = parseDouble(tfEval);
            double cons = parseDouble(tfConsult);

            boolean ok = service.update(selected.getId(), enfantId, eval, cons);
            if (ok) { clear(); refreshAll(); }
            else alert("Erreur", "Update échoué (DB).");
        });

        btnSupprimer.setOnAction(e -> {
            SuiviTotal selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Erreur", "Sélectionne une ligne."); return; }

            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Confirmation");
            c.setHeaderText("Supprimer Suivi ID " + selected.getId() + " ?");
            c.setContentText("Enfant: " + selected.getEnfantId());

            c.showAndWait().ifPresent(b -> {
                if (b == ButtonType.OK) {
                    boolean ok = service.delete(selected.getId());
                    if (ok) { clear(); refreshAll(); }
                    else alert("Erreur", "Delete échoué.");
                }
            });
        });

        btnClear.setOnAction(e -> clear());

        HBox row1 = new HBox(10, tfEnfantId, tfEval, tfConsult, btnAjouter, btnModifier, btnSupprimer, btnClear);
        row1.setPadding(new Insets(10));

        HBox row2 = new HBox(10, new Label("Recherche:"), tfSearch, lblSelectedId);
        row2.setPadding(new Insets(0, 10, 10, 10));

        VBox root = new VBox(10, row1, row2, table);
        root.setPadding(new Insets(10));
        return root;
    }

    private void refreshAll() { data.setAll(service.getAll()); }

    private void clear() {
        table.getSelectionModel().clearSelection();
        lblSelectedId.setText("ID: (aucun)");
        tfEnfantId.clear();
        tfEval.clear();
        tfConsult.clear();

        clearFieldState(tfEnfantId);
        clearFieldState(tfEval);
        clearFieldState(tfConsult);
    }

    // ---------------- VALIDATION ----------------

    private void validateLive() {
        Integer enfant = tryParseInt(tfEnfantId);
        Double eval = tryParseDouble(tfEval);
        Double cons = tryParseDouble(tfConsult);

        // enfantId > 0
        mark(tfEnfantId, enfant != null && enfant > 0);

        // notes entre 0 et 20
        mark(tfEval, eval != null && eval >= 0 && eval <= 20);
        mark(tfConsult, cons != null && cons >= 0 && cons <= 20);
    }

    private boolean validateSubmit() {
        validateLive();

        Integer enfant = tryParseInt(tfEnfantId);
        if (enfant == null || enfant <= 0) {
            alert("Erreur", "Enfant ID doit être un entier > 0.");
            return false;
        }

        Double eval = tryParseDouble(tfEval);
        Double cons = tryParseDouble(tfConsult);

        if (eval == null || cons == null) {
            alert("Erreur", "Notes doivent être des nombres.");
            return false;
        }
        if (eval < 0 || eval > 20 || cons < 0 || cons > 20) {
            alert("Erreur", "Notes doivent être entre 0 et 20.");
            return false;
        }
        return true;
    }

    private void mark(TextField f, boolean ok) {
        f.getStyleClass().removeAll("field-ok", "field-bad");
        if (f.getText() == null || f.getText().isBlank()) return;
        f.getStyleClass().add(ok ? "field-ok" : "field-bad");
    }

    private void clearFieldState(TextField f) {
        f.getStyleClass().removeAll("field-ok", "field-bad");
    }

    private void applyIntFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isEmpty()) return change;
            if (text.matches("\\d{0,9}")) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private void applyDoubleFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isEmpty()) return change;
            if (text.matches("\\d*(?:[\\.,]\\d*)?")) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private Integer tryParseInt(TextField f) {
        String t = f.getText();
        if (t == null || t.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(t.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double tryParseDouble(TextField f) {
        String t = f.getText();
        if (t == null || t.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(t.trim().replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    private double parseDouble(TextField f) {
        return Double.parseDouble(f.getText().trim().replace(',', '.'));
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
