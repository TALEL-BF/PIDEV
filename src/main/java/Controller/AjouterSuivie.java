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
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class AjouterSuivie {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;

    @FXML private ToggleButton tbConsultations;
    @FXML private VBox consultationsSubMenu;
    @FXML private Button btnGestionConsultations;

    @FXML private Button btnMenuSuivie;
    @FXML private Button btnMenuTherapie;

    // Stats UI
    @FXML private ComboBox<String> cbEnfantStats;
    @FXML private Button btnRefreshStats;
    @FXML private Label lblDeltaHumeur, lblDeltaStress, lblDeltaAttention;
    @FXML private javafx.scene.chart.LineChart<String, Number> monthlyChart;
    @FXML private VBox statsBox;
    @FXML private Label lblToggleStats;

    private SuivieServices suivieService;
    private Services.StatsServices statsService;

    private final ObservableList<Suivie> master = FXCollections.observableArrayList();

    // Styles bordures
    private static final String STYLE_OK   = "-fx-border-color: #22c55e; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_BAD  = "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_NONE = "-fx-border-color: transparent; -fx-border-width: 0;";

    @FXML
    public void initialize() {
        initSidebarDropdown();
        initNavigation();
        initUiListeners();
        initStatsToggle();

        try {
            suivieService = new SuivieServices();
            reloadFromDb();
        } catch (Exception ex) {
            showDbError(ex);
        }

        try {
            statsService = new Services.StatsServices();
            loadStatsUI();
        } catch (Exception ex) {
            disableStatsUI();
        }
    }

    private void initUiListeners() {
        if (searchField != null) {
            ChangeListener<Object> refreshListener = (obs, o, n) -> refreshCards();
            searchField.textProperty().addListener(refreshListener);
        }
        if (btnAjouter != null) {
            btnAjouter.setOnAction(e -> onAjouter());
        }
        if (cardsPane != null) {
            cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
                double w = newV.doubleValue();
                cardsPane.setPrefWrapLength(Math.max(360, w - 40));
            });
        }
    }

    private void initSidebarDropdown() {
        if (tbConsultations == null || consultationsSubMenu == null) return;

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

    private void initNavigation() {
        if (btnMenuSuivie != null) btnMenuSuivie.setOnAction(e -> switchTo("/AjouterSuivie.fxml"));
        if (btnMenuTherapie != null) btnMenuTherapie.setOnAction(e -> switchTo("/AjouterTherapie.fxml"));
    }

    // -----------------------------
    // DB load + cards
    // -----------------------------
    private void reloadFromDb() {
        if (suivieService == null) return;
        List<Suivie> list = suivieService.afficherSuivie();
        master.setAll(list);
        refreshCards();
    }

    private void refreshCards() {
        if (cardsPane == null) return;

        cardsPane.getChildren().clear();
        String q = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();

        List<Suivie> filtered = master.stream()
                .filter(s -> matchesSearch(s, q))
                .collect(Collectors.toList());

        for (Suivie s : filtered) cardsPane.getChildren().add(buildCard(s));

        if (countLabel != null) countLabel.setText(filtered.size() + " Suivies");

        boolean empty = filtered.isEmpty();
        if (emptyBox != null) {
            emptyBox.setVisible(empty);
            emptyBox.setManaged(empty);
        }
    }

    private boolean matchesSearch(Suivie s, String q) {
        if (q.isEmpty()) return true;
        return safe(s.getNomEnfant()).toLowerCase().contains(q)
                || safe(s.getNomPsy()).toLowerCase().contains(q)
                || safe(s.getObservation()).toLowerCase().contains(q)
                || safe(s.getComportement()).toLowerCase().contains(q)
                || safe(s.getInteractionSociale()).toLowerCase().contains(q)
                || safe(s.getStatut()).toLowerCase().contains(q);
    }

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
                        "\n\nÉTAT\n" + safe(s.getStatut()) +
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

    // -----------------------------
    // CRUD
    // -----------------------------
    private void onAjouter() {
        if (!ensureDbReady()) return;

        Optional<Suivie> res = showSuivieWindow(null, "Ajouter un suivi");
        res.ifPresent(s -> {
            suivieService.ajouterSuivie(s);
            reloadFromDb();
            loadStatsUI();
        });
    }

    private void onEdit(Suivie exist) {
        if (!ensureDbReady()) return;

        Optional<Suivie> res = showSuivieWindow(exist, "Modifier le suivi");
        res.ifPresent(updated -> {
            updated.setIdSuivie(exist.getIdSuivie());
            suivieService.modifierSuivie(updated);
            reloadFromDb();
            loadStatsUI();
        });
    }

    private void onSupprimer(Suivie s) {
        if (!ensureDbReady()) return;

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
            loadStatsUI();
        });

        actions.getChildren().addAll(cancel, del);

        content.getChildren().addAll(title, sub, chip, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

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
        addRow(grid, r++, "Email parent", safe(s.getEmailParent()));
        addRow(grid, r++, "Niveau séance", String.valueOf(s.getNiveauSeance()));
        addRow(grid, r++, "Psychologue", safe(s.getNomPsy()));
        addRow(grid, r++, "État", safe(s.getStatut()));
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

    // ==========================================================
    // Fenêtre Ajouter / Modifier (PRO: rouge/vert + erreurs + date future)
    // Niveau séance + scores: 0 rouge, >=1 vert, retour à 0 rouge
    // ==========================================================
    private Optional<Suivie> showSuivieWindow(Suivie s, String title) {

        Stage stage = createCustomStage(title, 1000, 700);

        VBox content = new VBox(12);
        content.getStyleClass().add("custom-content");

        // Message erreurs global
        Label lblErrors = new Label("");
        lblErrors.setWrapText(true);
        lblErrors.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 800;");

        TextField tfNom = new TextField(s == null ? "" : safe(s.getNomEnfant()));
        TextField tfEmail = new TextField(s == null ? "" : safe(s.getEmailParent()));
        tfEmail.setPromptText("parent@email.com");

        TextField tfAge = new TextField(s == null ? "" : String.valueOf(s.getAge()));
        TextField tfPsy = new TextField(s == null ? "" : safe(s.getNomPsy()));

        DatePicker dpDate = new DatePicker(
                (s == null || s.getDateSuivie() == null)
                        ? LocalDate.now()
                        : s.getDateSuivie().toLocalDateTime().toLocalDate()
        );

        // ✅ BLOQUER dates passées
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fecaca;");
                }
            }
        });

        TextField tfHeure = new TextField(
                (s == null || s.getDateSuivie() == null)
                        ? ""
                        : s.getDateSuivie().toLocalDateTime().toLocalTime().withSecond(0).withNano(0).toString()
        );
        tfHeure.setPromptText("HH:mm (ex: 11:00)");

        // ✅ Spinners : 0 au départ => ROUGE ; >=1 => VERT ; retour à 0 => ROUGE
        Spinner<Integer> spH = new Spinner<>();
        Spinner<Integer> spS = new Spinner<>();
        Spinner<Integer> spA = new Spinner<>();
        Spinner<Integer> spNiveau = new Spinner<>();

        spH.setValueFactory(new IntegerSpinnerValueFactory(0, 10, 0));
        spS.setValueFactory(new IntegerSpinnerValueFactory(0, 10, 0));
        spA.setValueFactory(new IntegerSpinnerValueFactory(0, 10, 0));
        spNiveau.setValueFactory(new IntegerSpinnerValueFactory(0, 10, 0));

        spH.setEditable(true);
        spS.setEditable(true);
        spA.setEditable(true);
        spNiveau.setEditable(true);

        if (s == null) {
            spNiveau.getValueFactory().setValue(0);
            spH.getValueFactory().setValue(0);
            spS.getValueFactory().setValue(0);
            spA.getValueFactory().setValue(0);
        } else {
            spNiveau.getValueFactory().setValue(s.getNiveauSeance() == null ? 0 : s.getNiveauSeance());
            spH.getValueFactory().setValue(s.getScoreHumeur());
            spS.getValueFactory().setValue(s.getScoreStress());
            spA.getValueFactory().setValue(s.getScoreAttention());
        }

        // ✅ État (+ Normal)
        ComboBox<String> cbEtat = new ComboBox<>();
        cbEtat.getItems().addAll("Normal", "Immobile", "Il ne bouge pas", "Il n'entend pas", "Il ne voit pas");
        String etatInit = (s == null) ? "" : safe(s.getStatut());
        cbEtat.setValue(etatInit.isBlank() ? null : etatInit);

        ComboBox<String> cbComportement = new ComboBox<>();
        cbComportement.getItems().addAll("Calme", "Agité");
        String compInit = (s == null) ? "" : safe(s.getComportement());
        cbComportement.setValue(compInit.isBlank() ? null : compInit);

        ComboBox<String> cbInteraction = new ComboBox<>();
        cbInteraction.getItems().addAll("Interaction totale", "Moyenne", "Faible");
        String interInit = (s == null) ? "" : safe(s.getInteractionSociale());
        cbInteraction.setValue(interInit.isBlank() ? null : interInit);

        TextArea taObs = new TextArea(s == null ? "" : safe(s.getObservation()));
        taObs.setPrefRowCount(5);

        // -------- Filtrage strict
        tfNom.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("[^a-zA-ZÀ-ÿ\\s]", "");
            if (!cleaned.equals(newV)) tfNom.setText(cleaned);
            if (cleaned.length() > 40) tfNom.setText(cleaned.substring(0, 40));
        });

        tfPsy.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("[^a-zA-ZÀ-ÿ\\s\\.\\-]", "");
            if (!cleaned.equals(newV)) tfPsy.setText(cleaned);
            if (cleaned.length() > 40) tfPsy.setText(cleaned.substring(0, 40));
        });

        tfAge.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("[^0-9]", "");
            if (!cleaned.equals(newV)) tfAge.setText(cleaned);
            if (cleaned.length() > 3) tfAge.setText(cleaned.substring(0, 3));
        });

        tfHeure.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("[^0-9:]", "");
            if (!cleaned.equals(newV)) tfHeure.setText(cleaned);
            if (cleaned.length() > 5) tfHeure.setText(cleaned.substring(0, 5));
        });

        tfEmail.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("\\s+", "");
            if (!cleaned.equals(newV)) tfEmail.setText(cleaned);
            if (cleaned.length() > 80) tfEmail.setText(cleaned.substring(0, 80));
        });

        taObs.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > 500) taObs.setText(newV.substring(0, 500));
        });

        // -------- Layout
        GridPane gp = new GridPane();
        gp.setHgap(18);
        gp.setVgap(14);
        gp.getStyleClass().add("form-grid");

        int r = 0;
        gp.add(label("Nom enfant *"), 0, r); gp.add(tfNom, 1, r);
        gp.add(label("Âge *"), 2, r); gp.add(tfAge, 3, r); r++;

        gp.add(label("Email parent *"), 0, r); gp.add(tfEmail, 1, r);
        gp.add(label("Niveau séance *"), 2, r); gp.add(spNiveau, 3, r); r++;

        gp.add(label("Nom psy *"), 0, r); gp.add(tfPsy, 1, r);
        gp.add(label("État *"), 2, r); gp.add(cbEtat, 3, r); r++;

        gp.add(label("Date *"), 0, r); gp.add(dpDate, 1, r);
        gp.add(label("Heure (HH:mm) *"), 2, r); gp.add(tfHeure, 3, r); r++;

        gp.add(label("Score humeur *"), 0, r); gp.add(spH, 1, r);
        gp.add(label("Score stress *"), 2, r); gp.add(spS, 3, r); r++;

        gp.add(label("Score attention *"), 0, r); gp.add(spA, 1, r);
        gp.add(label("Comportement *"), 2, r); gp.add(cbComportement, 3, r); r++;

        gp.add(label("Interaction sociale *"), 0, r);
        gp.add(cbInteraction, 1, r, 3, 1); r++;

        Label obsLabel = new Label("Observation (max 500)");
        obsLabel.getStyleClass().add("details-label");
        taObs.getStyleClass().add("details-box");

        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("btn-soft", "btn-hover");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Sauvegarder");
        save.getStyleClass().addAll("btn-solid-dark", "btn-hover");

        HBox actions = new HBox(12, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(lblErrors, gp, obsLabel, taObs, actions);
        setCenter(stage, content);

        final Suivie[] result = {null};

        // =====================
        // VALIDATION PRO TOTALE
        // =====================
        save.setDisable(true);

        Runnable validateAll = () -> {
            List<String> errors = new ArrayList<>();

            boolean okNom   = validateNom(tfNom, errors);
            boolean okAge   = validateAge(tfAge, errors);
            boolean okEmail = validateEmail(tfEmail, errors);
            boolean okPsy   = validatePsy(tfPsy, errors);
            boolean okDate  = validateDateNotPast(dpDate, errors);
            boolean okHeure = validateHeure(tfHeure, errors);

            boolean okEtat  = validateCombo(cbEtat, "État", errors);
            boolean okComp  = validateCombo(cbComportement, "Comportement", errors);
            boolean okInter = validateCombo(cbInteraction, "Interaction sociale", errors);

            // ✅ Niveau + Scores : 0 = ROUGE, >=1 = VERT, retour 0 = ROUGE
            boolean okNiv = validateSpinnerMin1(spNiveau, "Niveau séance", errors);
            boolean okSH  = validateSpinnerMin1(spH, "Score humeur", errors);
            boolean okSS  = validateSpinnerMin1(spS, "Score stress", errors);
            boolean okSA  = validateSpinnerMin1(spA, "Score attention", errors);

            if (errors.isEmpty()) {
                lblErrors.setText("");
                save.setDisable(false);
            } else {
                lblErrors.setText("Champs à corriger :\n- " + String.join("\n- ", errors));
                save.setDisable(true);
            }

            taObs.setStyle(STYLE_NONE);
        };

        // listeners
        tfNom.textProperty().addListener((o, a, b) -> validateAll.run());
        tfAge.textProperty().addListener((o, a, b) -> validateAll.run());
        tfEmail.textProperty().addListener((o, a, b) -> validateAll.run());
        tfPsy.textProperty().addListener((o, a, b) -> validateAll.run());
        tfHeure.textProperty().addListener((o, a, b) -> validateAll.run());
        dpDate.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbEtat.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbComportement.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbInteraction.valueProperty().addListener((o, a, b) -> validateAll.run());

        // ✅ important: écouter les flèches + saisie dans les spinners
        spNiveau.valueProperty().addListener((o,a,b) -> validateAll.run());
        spH.valueProperty().addListener((o,a,b) -> validateAll.run());
        spS.valueProperty().addListener((o,a,b) -> validateAll.run());
        spA.valueProperty().addListener((o,a,b) -> validateAll.run());

        spNiveau.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spH.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spS.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spA.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());

        // ✅ Au démarrage : 0 => rouges
        validateAll.run();

        // -------- Save
        save.setOnAction(e -> {
            validateAll.run();
            if (save.isDisable()) return;

            try {
                int age = Integer.parseInt(tfAge.getText().trim());
                Timestamp ts = buildTimestamp(dpDate.getValue(), tfHeure.getText().trim());

                int niveau = Integer.parseInt(spNiveau.getEditor().getText().trim());
                int scoreH = Integer.parseInt(spH.getEditor().getText().trim());
                int scoreS = Integer.parseInt(spS.getEditor().getText().trim());
                int scoreA = Integer.parseInt(spA.getEditor().getText().trim());

                Suivie created = new Suivie(
                        tfNom.getText().trim(),
                        age,
                        tfPsy.getText().trim(),
                        ts,
                        scoreH,
                        scoreS,
                        scoreA,
                        cbComportement.getValue(),
                        cbInteraction.getValue(),
                        taObs.getText().trim(),
                        cbEtat.getValue(),
                        tfEmail.getText().trim(),
                        niveau,
                        null,
                        null,
                        null
                );

                // sécurité setters
                created.setEmailParent(tfEmail.getText().trim());
                created.setNiveauSeance(niveau);
                created.setStatut(cbEtat.getValue());
                created.setComportement(cbComportement.getValue());
                created.setInteractionSociale(cbInteraction.getValue());

                result[0] = created;
                stage.close();

            } catch (Exception ex) {
                showDbError(ex);
            }
        });

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // -----------------------------
    // VALIDATORS (avec bordure)
    // -----------------------------
    private boolean validateNom(TextField tf, List<String> errors) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        boolean ok = v.length() >= 3 && v.length() <= 40 && v.matches("^[A-Za-zÀ-ÿ\\s]+$");
        tf.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Nom enfant (min 3 lettres, lettres/espaces seulement)");
        return ok;
    }

    private boolean validatePsy(TextField tf, List<String> errors) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        boolean ok = v.length() >= 3 && v.length() <= 40 && v.matches("^[A-Za-zÀ-ÿ\\s\\.\\-]+$");
        tf.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Nom psy (min 3, lettres + . - autorisés)");
        return ok;
    }

    private boolean validateAge(TextField tf, List<String> errors) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        boolean ok;
        try {
            int age = Integer.parseInt(v);
            ok = age >= 1 && age <= 120;
        } catch (Exception e) {
            ok = false;
        }
        tf.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Âge (nombre entre 1 et 120)");
        return ok;
    }

    private boolean validateEmail(TextField tf, List<String> errors) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        boolean ok = v.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        tf.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Email parent (ex: parent@email.com)");
        return ok;
    }

    private boolean validateHeure(TextField tf, List<String> errors) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        boolean ok;
        try {
            ok = v.matches("^\\d{2}:\\d{2}$");
            if (ok) LocalTime.parse(v);
        } catch (Exception e) {
            ok = false;
        }
        tf.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Heure (format HH:mm, ex: 11:00)");
        return ok;
    }

    private boolean validateDateNotPast(DatePicker dp, List<String> errors) {
        LocalDate d = dp.getValue();
        boolean ok = d != null && !d.isBefore(LocalDate.now());
        dp.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add("Date (obligatoire, pas de date passée)");
        return ok;
    }

    private boolean validateCombo(ComboBox<String> cb, String label, List<String> errors) {
        boolean ok = cb.getValue() != null && !cb.getValue().trim().isEmpty();
        cb.setStyle(ok ? STYLE_OK : STYLE_BAD);
        if (!ok) errors.add(label + " (obligatoire)");
        return ok;
    }

    // ✅ appliquer style sur le spinner (éditeur)
    private void applySpinnerBorder(Spinner<Integer> sp, boolean ok) {
        if (sp == null) return;
        sp.getEditor().setStyle(ok ? STYLE_OK : STYLE_BAD);
    }

    // ✅ 0 => ROUGE ; 1..10 => VERT ; retour 0 => ROUGE ; texte => ROUGE
    private boolean validateSpinnerMin1(Spinner<Integer> sp, String label, List<String> errors) {
        String txt = sp.getEditor().getText() == null ? "" : sp.getEditor().getText().trim();
        boolean ok = false;

        try {
            int v = Integer.parseInt(txt);
            ok = (v >= 1 && v <= 10);
            if (ok && sp.getValueFactory() instanceof IntegerSpinnerValueFactory) {
                ((IntegerSpinnerValueFactory) sp.getValueFactory()).setValue(v);
            }
        } catch (Exception e) {
            ok = false;
        }

        applySpinnerBorder(sp, ok);
        if (!ok) errors.add(label + " (obligatoire, min 1)");
        return ok;
    }

    // -----------------------------
    // Window helpers
    // -----------------------------
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
        header.setOnMousePressed(e -> { dx[0] = e.getSceneX(); dy[0] = e.getSceneY(); });
        header.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - dx[0]); stage.setY(e.getScreenY() - dy[0]); });

        Scene scene = new Scene(root, w, h);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        return stage;
    }

    private void setCenter(Stage stage, Node content) {
        BorderPane shell = (BorderPane) ((StackPane) stage.getScene().getRoot()).getChildren().get(0);
        shell.setCenter(content);
    }

    private void switchTo(String fxmlPath) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Navigation");
                a.setHeaderText("FXML introuvable");
                a.setContentText("Chemin : " + fxmlPath);
                a.showAndWait();
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
            javafx.scene.Parent root = loader.load();

            // Prend n'importe quel node sûr (btnMenuTherapie ici)
            Stage stage = (Stage) btnMenuTherapie.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();

            // Remonter la cause réelle
            Throwable cause = ex;
            while (cause.getCause() != null) cause = cause.getCause();

            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Navigation");
            a.setHeaderText("Erreur lors du chargement");
            a.setContentText(
                    "FXML: " + fxmlPath + "\n\n" +
                            "Erreur: " + ex.getClass().getSimpleName() + " - " + String.valueOf(ex.getMessage()) + "\n\n" +
                            "Cause: " + cause.getClass().getSimpleName() + " - " + String.valueOf(cause.getMessage())
            );
            a.showAndWait();
        }
    }


    // -----------------------------
    // Stats
    // -----------------------------
    private void loadStatsUI() {
        if (statsService == null) return;
        if (cbEnfantStats == null || btnRefreshStats == null || monthlyChart == null
                || lblDeltaHumeur == null || lblDeltaStress == null || lblDeltaAttention == null) return;

        cbEnfantStats.getItems().setAll(statsService.getAllEnfants());

        btnRefreshStats.setOnAction(e -> refreshStats());
        cbEnfantStats.setOnAction(e -> refreshStats());

        if (!cbEnfantStats.getItems().isEmpty()) {
            cbEnfantStats.getSelectionModel().select(0);
            refreshStats();
        }
    }

    private void refreshStats() {
        if (statsService == null) return;

        String enfant = cbEnfantStats.getValue();
        if (enfant == null || enfant.isBlank()) return;

        Entites.StatsSeanceDelta d = statsService.getLastDelta(enfant);

        if (d == null || d.prevHumeur == null) {
            lblDeltaHumeur.setText("-");
            lblDeltaStress.setText("-");
            lblDeltaAttention.setText("-");
        } else {
            lblDeltaHumeur.setText(String.valueOf(d.deltaH()));
            lblDeltaStress.setText(String.valueOf(d.progressStress()));
            lblDeltaAttention.setText(String.valueOf(d.deltaA()));
        }

        monthlyChart.getData().clear();

        var sH = new javafx.scene.chart.XYChart.Series<String, Number>(); sH.setName("Humeur");
        var sS = new javafx.scene.chart.XYChart.Series<String, Number>(); sS.setName("Stress");
        var sA = new javafx.scene.chart.XYChart.Series<String, Number>(); sA.setName("Attention");

        List<Entites.StatsMensuelle> list = statsService.getMonthlyStats(enfant);
        for (Entites.StatsMensuelle m : list) {
            String label = String.format("%02d/%d", m.month, m.year);
            sH.getData().add(new javafx.scene.chart.XYChart.Data<>(label, m.avgHumeur));
            sS.getData().add(new javafx.scene.chart.XYChart.Data<>(label, m.avgStress));
            sA.getData().add(new javafx.scene.chart.XYChart.Data<>(label, m.avgAttention));
        }

        monthlyChart.getData().addAll(sH, sS, sA);
    }

    private void disableStatsUI() {
        if (cbEnfantStats != null) cbEnfantStats.setDisable(true);
        if (btnRefreshStats != null) btnRefreshStats.setDisable(true);
        if (lblDeltaHumeur != null) lblDeltaHumeur.setText("-");
        if (lblDeltaStress != null) lblDeltaStress.setText("-");
        if (lblDeltaAttention != null) lblDeltaAttention.setText("-");
        if (monthlyChart != null) monthlyChart.setVisible(false);
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private Label label(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("details-key");
        return l;
    }

    private void addRow(GridPane g, int row, String k, String v) {
        Label lk = new Label(k); lk.getStyleClass().add("details-key");
        Label lv = new Label(v); lv.getStyleClass().add("details-val"); lv.setWrapText(true);
        g.add(lk, 0, row);
        g.add(lv, 1, row);
    }

    private Timestamp buildTimestamp(LocalDate d, String hhmm) {
        LocalTime t;
        try {
            t = LocalTime.parse(hhmm.length() == 5 ? hhmm : "10:00");
        } catch (Exception e) {
            t = LocalTime.of(10, 0);
        }
        return Timestamp.valueOf(d.atTime(t));
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void showDbError(Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText("Problème lors de l'opération");
        a.setContentText(ex == null ? "" : ex.getMessage());
        a.showAndWait();
    }

    private boolean ensureDbReady() {
        try {
            if (suivieService == null) suivieService = new SuivieServices();
            if (statsService == null)  statsService = new Services.StatsServices();
            return true;
        } catch (Exception ex) {
            showDbError(ex);
            return false;
        }
    }

    private void initStatsToggle() {
        if (statsBox == null) return;
        setStatsVisible(false);
        if (lblToggleStats != null) {
            lblToggleStats.setOnMouseClicked(e -> setStatsVisible(!statsBox.isVisible()));
        }
    }

    private void setStatsVisible(boolean show) {
        statsBox.setVisible(show);
        statsBox.setManaged(show);

        if (lblToggleStats != null) {
            lblToggleStats.setText(show ? "▾ Statistiques" : "▸ Statistiques");
        }

        if (show && ensureDbReady()) {
            loadStatsUI();
            refreshStats();
        }
    }
}
