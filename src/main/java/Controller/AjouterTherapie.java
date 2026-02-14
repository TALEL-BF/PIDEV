package Controller;

import Entites.Therapie;
import Services.TherapieServices;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AjouterTherapie {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;

    // Sidebar dropdown
    @FXML private ToggleButton tbConsultations;
    @FXML private VBox consultationsSubMenu;
    @FXML private Button btnGestionConsultations;

    // Submenu navigation
    @FXML private Button btnMenuSuivie;
    @FXML private Button btnMenuTherapie;

    private final TherapieServices therapieService = new TherapieServices();
    private final ObservableList<Therapie> master = FXCollections.observableArrayList();

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        reloadFromDb();

        ChangeListener<Object> refreshListener = (obs, o, n) -> refreshCards();
        if (searchField != null) searchField.textProperty().addListener(refreshListener);

        if (btnAjouter != null) btnAjouter.setOnAction(e -> onAjouter());

        if (cardsPane != null) {
            cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
                double w = newV.doubleValue();
                cardsPane.setPrefWrapLength(Math.max(360, w - 40));
            });
        }

        // dropdown consultations
        if (tbConsultations != null && consultationsSubMenu != null) {
            tbConsultations.setSelected(false);
            tbConsultations.setText("▸");

            consultationsSubMenu.setVisible(false);
            consultationsSubMenu.setManaged(false);

            tbConsultations.setOnAction(e -> {
                boolean open = tbConsultations.isSelected();
                consultationsSubMenu.setVisible(open);
                consultationsSubMenu.setManaged(open);
                tbConsultations.setText(open ? "▾" : "▸");
            });

            if (btnGestionConsultations != null) {
                btnGestionConsultations.setOnAction(e -> tbConsultations.fire());
            }
        }

        // navigation
        if (btnMenuSuivie != null) btnMenuSuivie.setOnAction(e -> switchTo("/AjouterSuivie.fxml"));
        if (btnMenuTherapie != null) btnMenuTherapie.setOnAction(e -> switchTo("/AjouterTherapie.fxml"));
    }

    private void switchTo(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reloadFromDb() {
        List<Therapie> list = therapieService.afficherTherapie();
        master.setAll(list);
        refreshCards();
    }

    private void refreshCards() {
        cardsPane.getChildren().clear();

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Therapie> filtered = master.stream()
                .filter(t -> matchesSearch(t, q))
                .collect(Collectors.toList());

        for (Therapie t : filtered) {
            cardsPane.getChildren().add(buildCard(t));
        }

        countLabel.setText(filtered.size() + " Thérapies");

        boolean empty = filtered.isEmpty();
        emptyBox.setVisible(empty);
        emptyBox.setManaged(empty);
    }

    private boolean matchesSearch(Therapie t, String q) {
        if (q.isEmpty()) return true;
        return safe(t.getNomExercice()).toLowerCase().contains(q)
                || safe(t.getTypeExercice()).toLowerCase().contains(q)
                || safe(t.getObjectif()).toLowerCase().contains(q)
                || safe(t.getDescription()).toLowerCase().contains(q)
                || safe(t.getMateriel()).toLowerCase().contains(q)
                || safe(t.getAdaptationTsa()).toLowerCase().contains(q);
    }

    // =========================
    // CARD (same design as suivie)
    // =========================
    private Node buildCard(Therapie t) {

        VBox card = new VBox();
        card.getStyleClass().add("suivie-card"); // نفس style card

        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band"); // نفس gradient header

        Label title = new Label(safe(t.getNomExercice()));
        title.getStyleClass().add("card-title");

        Label type = new Label(safe(t.getTypeExercice()).toUpperCase());
        type.getStyleClass().add("card-subtitle");

        VBox nameBox = new VBox(2, title, type);
        nameBox.getStyleClass().add("name-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        band.getChildren().addAll(nameBox, spacer);

        Region headerLine = new Region();
        headerLine.getStyleClass().add("card-header-line");

        VBox body = new VBox(12);
        body.getStyleClass().add("suivie-body");

        Label info = new Label(
                "OBJECTIF\n" + safe(t.getObjectif()) +
                        "\n\nDURÉE (min)\n" + t.getDureeMin() +
                        "\n\nNIVEAU\n" + t.getNiveau() +
                        "\n\nMATÉRIEL\n" + safe(t.getMateriel())
        );
        info.getStyleClass().add("suivie-info");

        HBox actions = new HBox(12);
        actions.getStyleClass().add("card-actions");

        Button voir = new Button("Voir");
        voir.getStyleClass().addAll("btn-card", "btn-voir");
        voir.setOnAction(e -> onVoir(t));

        Button edit = new Button("Éditer");
        edit.getStyleClass().addAll("btn-card", "btn-edit");
        edit.setOnAction(e -> onEdit(t));

        Button del = new Button("Supprimer");
        del.getStyleClass().addAll("btn-card", "btn-delete");
        del.setOnAction(e -> onSupprimer(t));

        HBox.setHgrow(voir, Priority.ALWAYS);
        HBox.setHgrow(edit, Priority.ALWAYS);
        HBox.setHgrow(del, Priority.ALWAYS);

        voir.setMaxWidth(Double.MAX_VALUE);
        edit.setMaxWidth(Double.MAX_VALUE);
        del.setMaxWidth(Double.MAX_VALUE);

        actions.getChildren().addAll(voir, edit, del);
        body.getChildren().addAll(info, actions);

        card.getChildren().addAll(band, headerLine, body);
        return card;
    }

    // =========================
    // CRUD
    // =========================
    private void onAjouter() {
        Optional<Therapie> res = showTherapieWindow(null, "Ajouter une thérapie");
        res.ifPresent(t -> {
            therapieService.ajouterTherapie(t);
            reloadFromDb();
        });
    }

    private void onEdit(Therapie exist) {
        Optional<Therapie> res = showTherapieWindow(exist, "Modifier la thérapie");
        res.ifPresent(updated -> {
            updated.setIdTherapie(exist.getIdTherapie());
            therapieService.modifierTherapie(updated);
            reloadFromDb();
        });
    }

    private void onVoir(Therapie t) {
        Stage stage = createCustomStage("Détails de la thérapie", 760, 560);

        VBox content = new VBox(14);
        content.getStyleClass().add("custom-content");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.getStyleClass().add("details-grid");

        int r = 0;
        addRow(grid, r++, "Nom exercice", safe(t.getNomExercice()));
        addRow(grid, r++, "Type", safe(t.getTypeExercice()));
        addRow(grid, r++, "Objectif", safe(t.getObjectif()));
        addRow(grid, r++, "Durée (min)", String.valueOf(t.getDureeMin()));
        addRow(grid, r++, "Niveau", String.valueOf(t.getNiveau()));
        addRow(grid, r++, "Matériel", safe(t.getMateriel()));

        Label adapLabel = new Label("Adaptation TSA");
        adapLabel.getStyleClass().add("details-label");

        Label adap = new Label(safe(t.getAdaptationTsa()));
        adap.setWrapText(true);
        adap.getStyleClass().add("details-box");

        Label descLabel = new Label("Description");
        descLabel.getStyleClass().add("details-label");

        Label desc = new Label(safe(t.getDescription()));
        desc.setWrapText(true);
        desc.getStyleClass().add("details-box");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button close = new Button("Fermer");
        close.getStyleClass().addAll("btn-solid-dark", "btn-hover");
        close.setOnAction(e -> stage.close());

        actions.getChildren().add(close);

        content.getChildren().addAll(grid, descLabel, desc, adapLabel, adap, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

    private void onSupprimer(Therapie t) {
        Stage stage = createCustomStage("Suppression", 640, 320);

        VBox content = new VBox(12);
        content.getStyleClass().add("custom-content");

        Label title = new Label("Supprimer cette thérapie ?");
        title.getStyleClass().add("dialog-title");

        Label sub = new Label("Cette action est irréversible.");
        sub.getStyleClass().add("dialog-sub");

        VBox chip = new VBox(4,
                new Label("Exercice : " + safe(t.getNomExercice())),
                new Label("Type : " + safe(t.getTypeExercice()))
        );
        chip.getStyleClass().add("dialog-chip");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("btn-soft", "btn-hover");
        cancel.setOnAction(e -> stage.close());

        Button del = new Button("Supprimer");
        del.getStyleClass().addAll("btn-danger", "btn-hover");
        del.setOnAction(e -> {
            therapieService.supprimerTherapie(t.getIdTherapie());
            stage.close();
            reloadFromDb();
        });

        actions.getChildren().addAll(cancel, del);

        content.getChildren().addAll(title, sub, chip, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

    // =========================
    // ADD / EDIT WINDOW (custom)
    // =========================
    private Optional<Therapie> showTherapieWindow(Therapie t, String title) {

        Stage stage = createCustomStage(title, 980, 660);

        VBox content = new VBox(14);
        content.getStyleClass().add("custom-content");

        TextField tfNom = new TextField(t == null ? "" : safe(t.getNomExercice()));
        TextField tfType = new TextField(t == null ? "" : safe(t.getTypeExercice()));
        TextField tfObj = new TextField(t == null ? "" : safe(t.getObjectif()));

        Spinner<Integer> spDuree = new Spinner<>(1, 300, t == null ? 15 : t.getDureeMin());
        Spinner<Integer> spNiveau = new Spinner<>(1, 10, t == null ? 1 : t.getNiveau());

        TextField tfMateriel = new TextField(t == null ? "" : safe(t.getMateriel()));

        TextArea taDesc = new TextArea(t == null ? "" : safe(t.getDescription()));
        taDesc.setPrefRowCount(4);

        TextArea taAdap = new TextArea(t == null ? "" : safe(t.getAdaptationTsa()));
        taAdap.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(18);
        gp.setVgap(14);
        gp.getStyleClass().add("form-grid");

        int r = 0;
        gp.add(label("Nom exercice"), 0, r); gp.add(tfNom, 1, r);
        gp.add(label("Type exercice"), 2, r); gp.add(tfType, 3, r); r++;

        gp.add(label("Objectif"), 0, r); gp.add(tfObj, 1, r, 3, 1); r++;

        gp.add(label("Durée (min)"), 0, r); gp.add(spDuree, 1, r);
        gp.add(label("Niveau"), 2, r); gp.add(spNiveau, 3, r); r++;

        gp.add(label("Matériel"), 0, r); gp.add(tfMateriel, 1, r, 3, 1); r++;

        Label descLabel = new Label("Description");
        descLabel.getStyleClass().add("details-label");
        taDesc.getStyleClass().add("details-box");

        Label adapLabel = new Label("Adaptation TSA");
        adapLabel.getStyleClass().add("details-label");
        taAdap.getStyleClass().add("details-box");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("btn-soft", "btn-hover");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Sauvegarder");
        save.getStyleClass().addAll("btn-solid-dark", "btn-hover");

        actions.getChildren().addAll(cancel, save);

        content.getChildren().addAll(gp, descLabel, taDesc, adapLabel, taAdap, actions);
        setCenter(stage, content);

        final Therapie[] result = {null};

        // Validation
        save.setDisable(true);
        Runnable validate = () -> {
            boolean valid = !tfNom.getText().trim().isEmpty()
                    && !tfType.getText().trim().isEmpty()
                    && !tfObj.getText().trim().isEmpty();
            save.setDisable(!valid);
        };
        tfNom.textProperty().addListener((o,a,b)->validate.run());
        tfType.textProperty().addListener((o,a,b)->validate.run());
        tfObj.textProperty().addListener((o,a,b)->validate.run());
        validate.run();

        save.setOnAction(e -> {
            result[0] = new Therapie(
                    tfNom.getText().trim(),
                    tfType.getText().trim(),
                    tfObj.getText().trim(),
                    taDesc.getText().trim(),
                    spDuree.getValue(),
                    spNiveau.getValue(),
                    tfMateriel.getText().trim(),
                    taAdap.getText().trim()
            );
            stage.close();
        });

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // =========================
    // CUSTOM WINDOW (same as suivie)
    // =========================
    private Stage createCustomStage(String title, double w, double h) {

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        StackPane root = new StackPane();
        root.setPadding(new Insets(18)); // marge externe
        root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        root.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("custom-window-shell");

        HBox header = new HBox(10);
        header.getStyleClass().add("custom-header");
        header.setPadding(new Insets(14));
        header.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label(title);
        t.getStyleClass().add("custom-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().addAll("custom-close", "btn-hover");
        close.setOnAction(e -> stage.close());

        header.getChildren().addAll(t, spacer, close);
        shell.setTop(header);
        shell.setCenter(new Pane());

        root.getChildren().add(shell);

        // Drag window
        final double[] dx = {0};
        final double[] dy = {0};
        header.setOnMousePressed(e -> {
            dx[0] = e.getSceneX();
            dy[0] = e.getSceneY();
        });
        header.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dx[0]);
            stage.setY(e.getScreenY() - dy[0]);
        });

        Scene scene = new Scene(root, w, h);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        return stage;
    }

    private void setCenter(Stage stage, Node content) {
        BorderPane shell = (BorderPane) ((StackPane) stage.getScene().getRoot()).getChildren().get(0);
        shell.setCenter(content);
    }

    // =========================
    // HELPERS
    // =========================
    private Label label(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("details-key");
        return l;
    }

    private void addRow(GridPane g, int row, String k, String v) {
        Label lk = new Label(k);
        lk.getStyleClass().add("details-key");

        Label lv = new Label(v);
        lv.getStyleClass().add("details-val");
        lv.setWrapText(true);

        g.add(lk, 0, row);
        g.add(lv, 1, row);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
