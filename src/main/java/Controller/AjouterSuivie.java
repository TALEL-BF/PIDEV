package Controller;

import Entites.Suivie;
import Services.SuivieServices;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AjouterSuivie {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;

    // Sidebar dropdown
    @FXML private ToggleButton tbConsultations;
    @FXML private VBox consultationsSubMenu;
    @FXML private Button btnGestionConsultations;

    // ✅ Sous-menu buttons (il manquaient chez toi)
    @FXML private Button btnMenuSuivie;
    @FXML private Button btnMenuTherapie;

    private final SuivieServices suivieService = new SuivieServices();
    private final ObservableList<Suivie> master = FXCollections.observableArrayList();

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        reloadFromDb();

        ChangeListener<Object> refreshListener = (obs, o, n) -> refreshCards();
        searchField.textProperty().addListener(refreshListener);

        btnAjouter.setOnAction(e -> onAjouter());

        cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
            double w = newV.doubleValue();
            cardsPane.setPrefWrapLength(Math.max(360, w - 40));
        });

        // ✅ Dropdown (Consultations -> Suivie / Thérapie)
        if (tbConsultations != null && consultationsSubMenu != null) {

            tbConsultations.setSelected(false);
            tbConsultations.setText("▸"); // fermé au début

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

        // ✅ Navigation : Suivie / Thérapie
        if (btnMenuSuivie != null) {
            btnMenuSuivie.setOnAction(e -> switchTo("/AjouterSuivie.fxml"));
        }
        if (btnMenuTherapie != null) {
            btnMenuTherapie.setOnAction(e -> switchTo("/AjouterTherapie.fxml"));
        }
    }

    private void reloadFromDb() {
        List<Suivie> list = suivieService.afficherSuivie();
        master.setAll(list);
        refreshCards();
    }

    private void refreshCards() {
        cardsPane.getChildren().clear();

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Suivie> filtered = master.stream()
                .filter(s -> matchesSearch(s, q))
                .collect(Collectors.toList());

        for (Suivie s : filtered) {
            cardsPane.getChildren().add(buildCard(s));
        }

        countLabel.setText(filtered.size() + " Suivies");

        boolean empty = filtered.isEmpty();
        emptyBox.setVisible(empty);
        emptyBox.setManaged(empty);
    }

    private boolean matchesSearch(Suivie s, String q) {
        if (q.isEmpty()) return true;
        return safe(s.getNomEnfant()).toLowerCase().contains(q)
                || safe(s.getNomPsy()).toLowerCase().contains(q)
                || safe(s.getObservation()).toLowerCase().contains(q)
                || safe(s.getComportement()).toLowerCase().contains(q)
                || safe(s.getInteractionSociale()).toLowerCase().contains(q);
    }

    // =========================
    // CARD
    // =========================
    private Node buildCard(Suivie s) {

        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");

        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");

        Label name = new Label(safe(s.getNomEnfant()));
        name.getStyleClass().add("card-title");

        Label psy = new Label("Dr " + safe(s.getNomPsy()));
        psy.getStyleClass().add("card-subtitle");

        VBox nameBox = new VBox(2, name, psy);
        nameBox.getStyleClass().add("name-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        band.getChildren().addAll(nameBox, spacer);

        Region headerLine = new Region();
        headerLine.getStyleClass().add("card-header-line");

        VBox body = new VBox(12);
        body.getStyleClass().add("suivie-body");

        String date = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalDate().toString();
        String heure = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalTime().toString();

        Label info = new Label(
                "PSY\n" + safe(s.getNomPsy()) +
                        "\n\nDATE\n" + date +
                        "\n\nHEURE\n" + heure +
                        "\n\nSCORES (H/S/A)\n" + s.getScoreHumeur() + " / " + s.getScoreStress() + " / " + s.getScoreAttention() +
                        "\n\nREMARQUE\n" + safe(s.getObservation())
        );
        info.getStyleClass().add("suivie-info");

        HBox actions = new HBox(12);
        actions.getStyleClass().add("card-actions");

        Button voir = new Button("Voir");
        voir.getStyleClass().addAll("btn-card", "btn-voir");
        voir.setOnAction(e -> onVoir(s));

        Button edit = new Button("Éditer");
        edit.getStyleClass().addAll("btn-card", "btn-edit");
        edit.setOnAction(e -> onEdit(s));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().addAll("btn-card", "btn-delete");
        supprimer.setOnAction(e -> onSupprimer(s));

        HBox.setHgrow(voir, Priority.ALWAYS);
        HBox.setHgrow(edit, Priority.ALWAYS);
        HBox.setHgrow(supprimer, Priority.ALWAYS);

        voir.setMaxWidth(Double.MAX_VALUE);
        edit.setMaxWidth(Double.MAX_VALUE);
        supprimer.setMaxWidth(Double.MAX_VALUE);

        actions.getChildren().addAll(voir, edit, supprimer);

        body.getChildren().addAll(info, actions);
        card.getChildren().addAll(band, headerLine, body);
        return card;
    }

    // =========================
    // CRUD
    // =========================
    private void onAjouter() {
        Optional<Suivie> res = showSuivieWindow(null, "Ajouter un suivi");
        res.ifPresent(s -> {
            suivieService.ajouterSuivie(s);
            reloadFromDb();
        });
    }

    private void onEdit(Suivie exist) {
        Optional<Suivie> res = showSuivieWindow(exist, "Modifier le suivi");
        res.ifPresent(updated -> {
            updated.setIdSuivie(exist.getIdSuivie());
            suivieService.modifierSuivie(updated);
            reloadFromDb();
        });
    }

    // ✅ DETAILS
    private void onVoir(Suivie s) {
        Stage stage = createCustomStage("Détails du suivi", 740, 520);

        VBox content = new VBox(14);
        content.getStyleClass().add("custom-content");

        String date = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalDate().toString();
        String heure = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalTime().toString();

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.getStyleClass().add("details-grid");

        int r = 0;
        addRow(grid, r++, "Enfant", safe(s.getNomEnfant()));
        addRow(grid, r++, "Âge", String.valueOf(s.getAge()));
        addRow(grid, r++, "Psychologue", safe(s.getNomPsy()));
        addRow(grid, r++, "Date", date);
        addRow(grid, r++, "Heure", heure);
        addRow(grid, r++, "Scores (H/S/A)", s.getScoreHumeur()+" / "+s.getScoreStress()+" / "+s.getScoreAttention());
        addRow(grid, r++, "Comportement", safe(s.getComportement()));
        addRow(grid, r++, "Interaction", safe(s.getInteractionSociale()));

        Label obsLabel = new Label("Observation");
        obsLabel.getStyleClass().add("details-label");

        Label obs = new Label(safe(s.getObservation()));
        obs.setWrapText(true);
        obs.getStyleClass().add("details-box");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button close = new Button("Fermer");
        close.getStyleClass().addAll("btn-solid-dark", "btn-hover");
        close.setOnAction(e -> stage.close());
        actions.getChildren().add(close);

        content.getChildren().addAll(grid, obsLabel, obs, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

    // ✅ SUPPRESSION
    private void onSupprimer(Suivie s) {
        Stage stage = createCustomStage("Suppression", 640, 320);

        VBox content = new VBox(12);
        content.getStyleClass().add("custom-content");

        Label title = new Label("Supprimer ce suivi ?");
        title.getStyleClass().add("dialog-title");

        Label sub = new Label("Cette action est irréversible.");
        sub.getStyleClass().add("dialog-sub");

        VBox chip = new VBox(4,
                new Label("Enfant : " + safe(s.getNomEnfant())),
                new Label("Psy : " + safe(s.getNomPsy()))
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
            suivieService.supprimerSuivie(s.getIdSuivie());
            stage.close();
            reloadFromDb();
        });

        actions.getChildren().addAll(cancel, del);

        content.getChildren().addAll(title, sub, chip, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

    // ✅ AJOUT / MODIF
    private Optional<Suivie> showSuivieWindow(Suivie s, String title) {

        Stage stage = createCustomStage(title, 980, 620);

        VBox content = new VBox(14);
        content.getStyleClass().add("custom-content");

        TextField tfNom = new TextField(s == null ? "" : safe(s.getNomEnfant()));
        TextField tfAge = new TextField(s == null ? "" : String.valueOf(s.getAge()));
        TextField tfPsy = new TextField(s == null ? "" : safe(s.getNomPsy()));

        DatePicker dpDate = new DatePicker(
                (s == null || s.getDateSuivie() == null) ? LocalDate.now()
                        : s.getDateSuivie().toLocalDateTime().toLocalDate()
        );

        TextField tfHeure = new TextField(
                (s == null || s.getDateSuivie() == null) ? "10:00"
                        : s.getDateSuivie().toLocalDateTime().toLocalTime().toString()
        );

        Spinner<Integer> spH = new Spinner<>(0, 10, s == null ? 5 : s.getScoreHumeur());
        Spinner<Integer> spS = new Spinner<>(0, 10, s == null ? 5 : s.getScoreStress());
        Spinner<Integer> spA = new Spinner<>(0, 10, s == null ? 5 : s.getScoreAttention());

        TextField tfComp = new TextField(s == null ? "" : safe(s.getComportement()));
        TextField tfInter = new TextField(s == null ? "" : safe(s.getInteractionSociale()));

        TextArea taObs = new TextArea(s == null ? "" : safe(s.getObservation()));
        taObs.setPrefRowCount(6);

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("Actif", "Terminé", "Annulé", "EFFECTUE");
        cbStatut.setValue(s == null ? "Actif" : safe(s.getStatut()));

        GridPane gp = new GridPane();
        gp.setHgap(18);
        gp.setVgap(14);
        gp.getStyleClass().add("form-grid");

        int r = 0;
        gp.add(label("Nom enfant"), 0, r); gp.add(tfNom, 1, r);
        gp.add(label("Âge"), 2, r); gp.add(tfAge, 3, r); r++;

        gp.add(label("Nom psy"), 0, r); gp.add(tfPsy, 1, r);
        gp.add(label("Statut"), 2, r); gp.add(cbStatut, 3, r); r++;

        gp.add(label("Date"), 0, r); gp.add(dpDate, 1, r);
        gp.add(label("Heure (HH:mm)"), 2, r); gp.add(tfHeure, 3, r); r++;

        gp.add(label("Score humeur"), 0, r); gp.add(spH, 1, r);
        gp.add(label("Score stress"), 2, r); gp.add(spS, 3, r); r++;

        gp.add(label("Score attention"), 0, r); gp.add(spA, 1, r);
        gp.add(label("Comportement"), 2, r); gp.add(tfComp, 3, r); r++;

        gp.add(label("Interaction sociale"), 0, r);
        gp.add(tfInter, 1, r, 3, 1); r++;

        Label obsLabel = new Label("Observation");
        obsLabel.getStyleClass().add("details-label");

        taObs.getStyleClass().add("details-box");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("btn-soft", "btn-hover");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Sauvegarder");
        save.getStyleClass().addAll("btn-solid-dark", "btn-hover");

        actions.getChildren().addAll(cancel, save);

        content.getChildren().addAll(gp, obsLabel, taObs, actions);
        setCenter(stage, content);

        final Suivie[] result = {null};

        // Validation
        save.setDisable(true);
        Runnable validate = () -> {
            boolean valid = !tfNom.getText().trim().isEmpty() && !tfAge.getText().trim().isEmpty();
            if (valid) {
                try {
                    int a = Integer.parseInt(tfAge.getText().trim());
                    valid = a >= 1 && a <= 120;
                } catch (Exception ex) { valid = false; }
            }
            save.setDisable(!valid);
        };
        tfNom.textProperty().addListener((o,a,b)->validate.run());
        tfAge.textProperty().addListener((o,a,b)->validate.run());
        validate.run();

        save.setOnAction(e -> {
            int age = Integer.parseInt(tfAge.getText().trim());
            Timestamp ts = buildTimestamp(dpDate.getValue(), tfHeure.getText().trim());

            result[0] = new Suivie(
                    tfNom.getText().trim(),
                    age,
                    tfPsy.getText().trim(),
                    ts,
                    spH.getValue(),
                    spS.getValue(),
                    spA.getValue(),
                    tfComp.getText().trim(),
                    tfInter.getText().trim(),
                    taObs.getText().trim(),
                    cbStatut.getValue()
            );
            stage.close();
        });

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // =========================
    // CUSTOM WINDOW
    // =========================
    private Stage createCustomStage(String title, double w, double h) {

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        StackPane root = new StackPane();
        root.setPadding(new Insets(18));
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
    // ✅ NAVIGATION (AJOUTÉE)
    // =========================
    private void switchTo(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) cardsPane.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private Timestamp buildTimestamp(LocalDate d, String hhmm) {
        LocalTime t;
        try { t = LocalTime.parse(hhmm.length() == 5 ? hhmm : "10:00"); }
        catch (Exception e) { t = LocalTime.of(10,0); }
        return Timestamp.valueOf(d.atTime(t));
    }

    private String safe(String s) { return s == null ? "" : s; }
}
