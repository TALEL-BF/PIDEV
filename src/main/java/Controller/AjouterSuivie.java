package Controller;

import Entites.Suivie;
import Entites.Therapie;
import IServices.ISuivieServices;
import Services.CompteRenduPdfService;
import Services.MailService;
import Services.SuivieServices;
import Services.TherapieServices;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.*;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import javafx.scene.control.OverrunStyle;

public class AjouterSuivie {

    // ====== UI (FXML) ======
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;
    @FXML private BorderPane root;

    @FXML private ToggleButton tbConsultations;
    @FXML private VBox consultationsSubMenu;
    @FXML private Button btnGestionConsultations;

    @FXML private Button btnSubSuivie;
    @FXML private Button btnSubTherapie;
    @FXML private Button btnSubArticles;

    @FXML private Button btnMenuSuivie;
    @FXML private Button btnMenuTherapie;
    @FXML private Button btnMenuArticles;

    // Stats UI (dans ton FXML)
    @FXML private ComboBox<String> cbEnfantStats;
    @FXML private Button btnRefreshStats;
    @FXML private Label lblDeltaHumeur, lblDeltaStress, lblDeltaAttention;
    @FXML private VBox statsBox;
    @FXML private Label lblToggleStats;
    @FXML private NumberAxis xAxisStats;
    @FXML private NumberAxis yAxisStats;
    @FXML private LineChart<Number, Number> lineConsultations;

    // Champs qui existent peut-être dans ton projet (si pas dans ton FXML -> restent null, OK)
    @FXML private StackedBarChart<String, Number> stackedMonthly;
    @FXML private LineChart<String, Number> monthlyLine;
    @FXML private LineChart<String, Number> monthlyChart;
    @FXML private BarChart<String, Number> barMonthly;
    @FXML private Button btnAjouterFab;

    // 🔔 Notifications (PDF parent -> psy)
    @FXML private Button btnNotif;
    @FXML private Label lblNotifBadge;
    @FXML private VBox notifPanel;
    @FXML private Button btnNotifRefresh;

    private Popup notifPopup;
    private VBox notifListBox;
    private Label notifHeaderSub;

    private final SuivieServices suivieServices = new SuivieServices();


    // ====== DATA / SERVICE ======
    private final ObservableList<Suivie> master = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
    // ✅ UN SEUL SERVICE : interface + impl (BONNE PRATIQUE)
    private final ISuivieServices suivieService = new SuivieServices();
    // Affichage date sur l'axe X
    private static final DateTimeFormatter AXIS_FMT = DateTimeFormatter.ofPattern("dd/MM");

    // Optionnel: date + heure si tu veux
    private static final DateTimeFormatter AXIS_FMT_DT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private String labelDateForAxis(Suivie s) {
        if (s == null || s.getDateSuivie() == null) return "";
        // choisis 1 des 2 :
        return s.getDateSuivie().toLocalDateTime().format(AXIS_FMT);     // juste date
        // return s.getDateSuivie().toLocalDateTime().format(AXIS_FMT_DT); // date+heure
    }

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
        initNotificationsUI();

        // Chargement DB (catch ici => plus de "Unhandled SQLException")
        reloadFromDb();

        // sidebar active
        setParentConsultationsActive(true);
        if (btnSubSuivie != null) setSubActive(btnSubSuivie);

        if (btnSubSuivie != null) {
            btnSubSuivie.setOnAction(e -> { setSubActive(btnSubSuivie); switchTo("/AjouterSuivie.fxml"); });
        }
        if (btnSubTherapie != null) {
            btnSubTherapie.setOnAction(e -> { setSubActive(btnSubTherapie); switchTo("/AjouterTherapie.fxml"); });
        }
        if (btnSubArticles != null) {
            btnSubArticles.setOnAction(e -> { setSubActive(btnSubArticles); switchTo("/GestionArticlesBack.fxml"); });
        }


        initStatsUI();
    }

    // -----------------------------
    // UI listeners
    // -----------------------------
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
        if (btnAjouterFab != null) {
            btnAjouterFab.setOnAction(e -> onAjouter());
        }
        if (btnMenuArticles != null) {
            btnMenuArticles.setOnAction(e -> switchTo("/GestionArticlesBack.fxml"));
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
        try {
            List<Suivie> list = suivieService.afficherSuivie();
            master.setAll(list);
            refreshCards();
        } catch (Exception ex) {
            showDbError(ex);
        }
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
        Optional<Suivie> res = showSuivieWindow(null, "Ajouter un suivi");
        res.ifPresent(s -> {
            try {
                suivieService.ajouterSuivie(s);
                reloadFromDb();
            } catch (Exception ex) {
                showDbError(ex);
            }
        });
    }

    private void onEdit(Suivie exist) {
        Optional<Suivie> res = showSuivieWindow(exist, "Modifier le suivi");
        res.ifPresent(updated -> {
            try {
                updated.setIdSuivie(exist.getIdSuivie());
                suivieService.modifierSuivie(updated);
                reloadFromDb();
            } catch (Exception ex) {
                showDbError(ex);
            }
        });
    }

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
            try {
                suivieService.supprimerSuivie(s.getIdSuivie());
                stage.close();
                reloadFromDb();
            } catch (Exception ex) {
                showDbError(ex);
            }
        });

        actions.getChildren().addAll(cancel, del);

        content.getChildren().addAll(title, sub, chip, actions);
        setCenter(stage, content);

        stage.showAndWait();
    }

    private void onVoir(Suivie s) {
        Stage stage = createCustomStage("Détails du suivi", 780, 560);

        VBox content = new VBox(14);
        content.getStyleClass().add("custom-content");

        String date = (s.getDateSuivie() == null) ? "-" :
                s.getDateSuivie().toLocalDateTime().toLocalDate().toString();

        String heure = (s.getDateSuivie() == null) ? "-" :
                s.getDateSuivie().toLocalDateTime().toLocalTime().withSecond(0).withNano(0).toString();

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        grid.getStyleClass().add("details-grid");

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(180);
        c1.setPrefWidth(180);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        grid.getColumnConstraints().setAll(c1, c2);

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

        // Charger nom exercice si déjà présent
        String exerciceNom = "Aucun exercice choisi";
        try {
            if (s.getIdTherapieReco() != null) {
                TherapieServices ts = new TherapieServices();
                Therapie t0 = ts.getTherapieById(s.getIdTherapieReco());
                if (t0 != null && t0.getNomExercice() != null) exerciceNom = t0.getNomExercice();
            }
        } catch (Exception ex) {
            exerciceNom = "Erreur chargement exercice";
        }

        Label lblExercice = addRow(grid, r++, "Exercice recommandé", exerciceNom);

        addRow(grid, r++, "Comportement", safe(s.getComportement()));
        addRow(grid, r++, "Interaction", safe(s.getInteractionSociale()));

        Label obsTitle = new Label("Observation");
        obsTitle.getStyleClass().add("details-key");

        Label obs = new Label(safe(s.getObservation()));
        obs.setWrapText(true);
        obs.getStyleClass().add("details-box");
        obs.setMaxWidth(Double.MAX_VALUE);

        // ===== Actions =====
        HBox actions = new HBox(12);
        actions.getStyleClass().add("dialog-footer");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnExercices = new Button("Exercices adaptés");
        btnExercices.getStyleClass().addAll("btn-solid-dark","btn-hover");

        Button btnEnvoyerPdf = new Button("Envoyer PDF au parent");
        btnEnvoyerPdf.getStyleClass().addAll("btn-solid-dark","btn-hover");
        btnEnvoyerPdf.setDisable(s.getIdTherapieReco() == null);

        // 1) Choisir exercice
        btnExercices.setOnAction(e -> {
            try {
                Optional<Therapie> chosen = showExercicesAdaptesWindow(s);
                chosen.ifPresent(t -> {
                    lblExercice.setText(t.getNomExercice());

                    // Important : mettre à jour l'objet sélectionné
                    s.setIdTherapieReco(t.getIdTherapie());

                    // Mettre à jour la liste master (pour refresh UI ailleurs)
                    for (Suivie x : master) {
                        if (x.getIdSuivie() == s.getIdSuivie()) {
                            x.setIdTherapieReco(t.getIdTherapie());
                            break;
                        }
                    }

                    // activer le bouton PDF
                    btnEnvoyerPdf.setDisable(false);
                });
            } catch (Exception ex) {
                showDbError(ex);
            }
        });

        // 2) Générer PDF + envoyer au parent
        btnEnvoyerPdf.setOnAction(e -> {
            if (s.getEmailParent() == null || s.getEmailParent().isBlank()) {
                showAlert("Email parent manquant", "Impossible d’envoyer : EMAIL_PARENT est vide.");
                return;
            }
            if (s.getIdTherapieReco() == null) {
                showAlert("Exercice manquant", "Choisis un exercice adapté avant l’envoi.");
                return;
            }

            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {

                    // a) charger thérapie complète
                    TherapieServices ts = new TherapieServices();
                    Therapie therapie = ts.getTherapieById(s.getIdTherapieReco());

                    // b) générer PDF pro (suivi + exercice complet)
                    CompteRenduPdfService pdfService = new CompteRenduPdfService();
                    java.io.File pdfFile = pdfService.generate(s, therapie);

                    // c) sauver en base (ID_THERAPIE_RECO + CR_PDF_PATH si tu le gères)
                    s.setCrPdfPath(pdfFile.getAbsolutePath()); // si ton Entité a ce champ
                    SuivieServices ss = new SuivieServices();
                    ss.modifierSuivie(s);

                    // d) envoyer mail + PDF
                    MailService mail = new MailService(
                            System.getenv("GMAIL_USER"),
                            System.getenv("GMAIL_APP_PASSWORD")
                    );

                    mail.sendWithAttachment(
                            s.getEmailParent(),
                            "Compte rendu (PDF) - " + safe(s.getNomEnfant()),
                            "Bonjour,\nVeuillez trouver en pièce jointe le compte rendu du suivi et l'exercice recommandé.\nCordialement.",
                            pdfFile
                    );

                    return null;
                }
            };

            task.setOnSucceeded(ev -> showAlert("Succès", "📧 PDF envoyé au parent : " + s.getEmailParent()));
            task.setOnFailed(ev -> showAlert("Erreur", "❌ Envoi échoué : " + task.getException().getMessage()));

            new Thread(task).start();
        });

        Button close = new Button("Fermer");
        close.getStyleClass().addAll("btn-solid-dark", "btn-hover");
        close.setOnAction(e -> stage.close());

        actions.getChildren().addAll(btnExercices, btnEnvoyerPdf, close);

        VBox inner = new VBox(14, grid, obsTitle, obs, actions);
        inner.setFillWidth(true);

        ScrollPane sp = new ScrollPane(inner);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background-color: transparent;");
        sp.setPadding(new Insets(0));

        content.getChildren().add(sp);
        setCenter(stage, content);
        stage.showAndWait();
    }
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ==========================================================
    // Fenêtre Ajouter / Modifier
    // ==========================================================
    private Optional<Suivie> showSuivieWindow(Suivie s, String title) {

        Stage stage = createCustomStage(title, 1000, 700);

        VBox content = new VBox(12);
        content.getStyleClass().add("custom-content");

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

        save.setDisable(true);

        Runnable validateAll = () -> {
            List<String> errors = new ArrayList<>();

            validateNom(tfNom, errors);
            validateAge(tfAge, errors);
            validateEmail(tfEmail, errors);
            validatePsy(tfPsy, errors);
            validateDateNotPast(dpDate, errors);
            validateHeure(tfHeure, errors);

            validateCombo(cbEtat, "État", errors);
            validateCombo(cbComportement, "Comportement", errors);
            validateCombo(cbInteraction, "Interaction sociale", errors);

            validateSpinnerMin1(spNiveau, "Niveau séance", errors);
            validateSpinnerMin1(spH, "Score humeur", errors);
            validateSpinnerMin1(spS, "Score stress", errors);
            validateSpinnerMin1(spA, "Score attention", errors);

            if (errors.isEmpty()) {
                lblErrors.setText("");
                save.setDisable(false);
            } else {
                lblErrors.setText("Champs à corriger :\n- " + String.join("\n- ", errors));
                save.setDisable(true);
            }

            taObs.setStyle(STYLE_NONE);
        };

        tfNom.textProperty().addListener((o, a, b) -> validateAll.run());
        tfAge.textProperty().addListener((o, a, b) -> validateAll.run());
        tfEmail.textProperty().addListener((o, a, b) -> validateAll.run());
        tfPsy.textProperty().addListener((o, a, b) -> validateAll.run());
        tfHeure.textProperty().addListener((o, a, b) -> validateAll.run());
        dpDate.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbEtat.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbComportement.valueProperty().addListener((o, a, b) -> validateAll.run());
        cbInteraction.valueProperty().addListener((o, a, b) -> validateAll.run());

        spNiveau.valueProperty().addListener((o,a,b) -> validateAll.run());
        spH.valueProperty().addListener((o,a,b) -> validateAll.run());
        spS.valueProperty().addListener((o,a,b) -> validateAll.run());
        spA.valueProperty().addListener((o,a,b) -> validateAll.run());

        spNiveau.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spH.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spS.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());
        spA.getEditor().textProperty().addListener((o,a,b) -> validateAll.run());

        validateAll.run();

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
    // VALIDATORS
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

    private void applySpinnerBorder(Spinner<Integer> sp, boolean ok) {
        if (sp == null) return;
        sp.getEditor().setStyle(ok ? STYLE_OK : STYLE_BAD);
    }

    private boolean validateSpinnerMin1(Spinner<Integer> sp, String label, List<String> errors) {
        String txt = sp.getEditor().getText() == null ? "" : sp.getEditor().getText().trim();
        boolean ok;
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

        StackPane rootPane = new StackPane();
        rootPane.setPadding(new Insets(18));
        rootPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        rootPane.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

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

        rootPane.getChildren().add(shell);

        final double[] dx = {0};
        final double[] dy = {0};
        header.setOnMousePressed(e -> { dx[0] = e.getSceneX(); dy[0] = e.getSceneY(); });
        header.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - dx[0]); stage.setY(e.getScreenY() - dy[0]); });

        Scene scene = new Scene(rootPane, w, h);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(newRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur Navigation");
            a.setHeaderText("Erreur lors du chargement");
            a.setContentText("FXML: " + fxmlPath + "\nErreur: " + ex.getMessage());
            a.showAndWait();
        }
    }

    // -----------------------------
    // Stats (toggle + UI)
    // -----------------------------
    private void initStatsToggle() {
        if (statsBox == null) return;
        setStatsVisible(false);
        if (lblToggleStats != null) {
            lblToggleStats.setOnMouseClicked(e -> setStatsVisible(!statsBox.isVisible()));
        }
    }

    private void setStatsVisible(boolean show) {
        if (statsBox == null) return;
        statsBox.setVisible(show);
        statsBox.setManaged(show);

        if (lblToggleStats != null) {
            lblToggleStats.setText(show ? "▾ Statistiques" : "▸ Statistiques");
        }
    }

    private void disableStatsUI() {
        if (cbEnfantStats != null) cbEnfantStats.setDisable(true);
        if (btnRefreshStats != null) btnRefreshStats.setDisable(true);
        if (lblDeltaHumeur != null) lblDeltaHumeur.setText("-");
        if (lblDeltaStress != null) lblDeltaStress.setText("-");
        if (lblDeltaAttention != null) lblDeltaAttention.setText("-");
        if (monthlyChart != null) monthlyChart.setVisible(false);
    }

    private void initStatsUI() {
        try {
            if (cbEnfantStats != null) {
                cbEnfantStats.getItems().setAll(suivieService.listerNomsEnfants());
            }
            if (btnRefreshStats != null) {
                btnRefreshStats.setOnAction(e -> refreshStats());
            }
            if (lblToggleStats != null) {
                lblToggleStats.setOnMouseClicked(e -> toggleStats());
            }
            if (lineConsultations != null) {
                lineConsultations.setAnimated(false);
                lineConsultations.setCreateSymbols(true);
                lineConsultations.getData().clear();
            }
            cbEnfantStats.getItems().setAll(suivieService.listerNomsEnfants());
            btnRefreshStats.setOnAction(e -> refreshStats());
            lblToggleStats.setOnMouseClicked(e -> toggleStats());

            configureLineChartReadable(lineConsultations);
            lineConsultations.getData().clear();
            configureCartesianChart();
        } catch (Exception ex) {
            showDbError(ex);
            disableStatsUI();
        }
    }

    private void toggleStats() {
        if (statsBox == null) return;
        boolean show = !statsBox.isVisible();
        statsBox.setVisible(show);
        statsBox.setManaged(show);
        if (lblToggleStats != null) {
            lblToggleStats.setText(show ? "▾ Statistiques" : "▸ Statistiques");
        }
    }

    private void refreshStats() {

        String enfant = (cbEnfantStats == null) ? null : cbEnfantStats.getValue();
        if (enfant == null || enfant.isBlank()) return;

        List<Suivie> rows = suivieService.statsParEnfant(enfant);

        if (lineConsultations == null || xAxisStats == null || yAxisStats == null) return;

        lineConsultations.getData().clear();

        if (lblDeltaHumeur != null) lblDeltaHumeur.setText("-");
        if (lblDeltaStress != null) lblDeltaStress.setText("-");
        if (lblDeltaAttention != null) lblDeltaAttention.setText("-");

        if (rows == null || rows.isEmpty()) return;

        // ✅ labels X (date) dans le même ordre que rows
        final List<String> labelsX = new ArrayList<>();
        for (Suivie s : rows) labelsX.add(labelDateForAxis(s));

        // ✅ Formatter: afficher date au lieu de 1..N
        xAxisStats.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number value) {
                int i = value.intValue(); // 1..N
                if (i >= 1 && i <= labelsX.size()) return labelsX.get(i - 1);
                return "";
            }
            @Override
            public Number fromString(String string) {
                return 0; // pas utilisé
            }
        });

        XYChart.Series<Number, Number> serH = new XYChart.Series<>();
        serH.setName("Humeur");

        XYChart.Series<Number, Number> serS = new XYChart.Series<>();
        serS.setName("Stress");

        XYChart.Series<Number, Number> serA = new XYChart.Series<>();
        serA.setName("Attention");

        int index = 1;
        for (Suivie s : rows) {
            serH.getData().add(new XYChart.Data<>(index, s.getScoreHumeur()));
            serS.getData().add(new XYChart.Data<>(index, s.getScoreStress()));
            serA.getData().add(new XYChart.Data<>(index, s.getScoreAttention()));
            index++;
        }

        // ✅ bornes X propres et stables
        xAxisStats.setAutoRanging(false);
        xAxisStats.setLowerBound(0.5);
        xAxisStats.setUpperBound(rows.size() + 0.5);
        xAxisStats.setTickUnit(1);


        // ✅ Y reste 0..10 (déjà fixé), on le re-force au cas où
        yAxisStats.setAutoRanging(false);
        yAxisStats.setLowerBound(0);
        yAxisStats.setUpperBound(15.5);
        yAxisStats.setTickUnit(1);

        lineConsultations.getData().setAll(serH, serS, serA);

        // Δ : dernière - précédente
        if (rows.size() >= 2) {
            Suivie prev = rows.get(rows.size() - 2);
            Suivie last = rows.get(rows.size() - 1);

            if (lblDeltaHumeur != null) lblDeltaHumeur.setText(formatDelta(last.getScoreHumeur() - prev.getScoreHumeur()));
            if (lblDeltaStress != null) lblDeltaStress.setText(formatDelta(last.getScoreStress() - prev.getScoreStress()));
            if (lblDeltaAttention != null) lblDeltaAttention.setText(formatDelta(last.getScoreAttention() - prev.getScoreAttention()));
        } else {
            if (lblDeltaHumeur != null) lblDeltaHumeur.setText("0");
            if (lblDeltaStress != null) lblDeltaStress.setText("0");
            if (lblDeltaAttention != null) lblDeltaAttention.setText("0");
        }

        addValueLabelsSafe(lineConsultations);
    }
    private String formatDelta(int d) {
        return (d > 0 ? "+" : "") + d;
    }

    // -----------------------------
    // Exercices adaptés
    // -----------------------------
    private String niveauFromScore(int score) {
        if (score <= 0) return "faible";
        if (score <= 3) return "faible";
        if (score <= 6) return "moyenne";
        return "elevee";
    }

    private Optional<Therapie> showExercicesAdaptesWindow(Suivie s) {

        String nivH = niveauFromScore(s.getScoreHumeur());
        String nivA = niveauFromScore(s.getScoreAttention());
        String nivS = niveauFromScore(s.getScoreStress());

        TherapieServices ts = new TherapieServices();
        List<Therapie> adaptees = ts.chercherTherapiesAdaptees(nivH, nivA, nivS);

        Stage st = createCustomStage("Exercices adaptés", 700, 500);

        final Therapie[] chosen = {null};

        ListView<Therapie> listView = new ListView<>();
        listView.getItems().addAll(adaptees);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Therapie t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setText(null);
                } else {
                    setText(t.getNomExercice()
                            + " | Durée: " + t.getDureeMin() + " min"
                            + " | Objectif: " + t.getObjectif());
                }
            }
        });

        Button choisir = new Button("Choisir cet exercice");
        choisir.getStyleClass().addAll("btn-solid-dark","btn-hover");

        choisir.setOnAction(ev -> {
            Therapie selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try {
                s.setIdTherapieReco(selected.getIdTherapie());
                suivieService.modifierSuivie(s); // ✅ plus de "if (suivieService == null)" car final

                chosen[0] = selected;
                st.close();

            } catch (Exception ex) {
                showDbError(ex);
            }
        });

        VBox r = new VBox(15,
                new Label("Niveaux détectés : "
                        + "Humeur=" + nivH
                        + ", Attention=" + nivA
                        + ", Stress=" + nivS),
                listView,
                choisir);

        r.setPadding(new Insets(20));

        setCenter(st, r);
        st.showAndWait();

        return Optional.ofNullable(chosen[0]);
    }

    // -----------------------------
    // Sidebar active styles
    // -----------------------------
    private void setParentConsultationsActive(boolean active){
        if (btnGestionConsultations == null) return;
        btnGestionConsultations.getStyleClass().remove("sideBtnActive");
        if(active) btnGestionConsultations.getStyleClass().add("sideBtnActive");
    }

    private void setSubActive(Button selected){
        if (btnSubSuivie != null) btnSubSuivie.getStyleClass().remove("subBtnActive");
        if (btnSubTherapie != null) btnSubTherapie.getStyleClass().remove("subBtnActive");
        if (btnSubArticles != null) btnSubArticles.getStyleClass().remove("subBtnActive");

        if (selected != null) selected.getStyleClass().add("subBtnActive");
        setParentConsultationsActive(true);
    }


    // -----------------------------
    // Helpers
    // -----------------------------
    private Label label(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("details-key");
        return l;
    }

    private Label addRow(GridPane g, int row, String k, String v) {
        Label lk = new Label(k);
        lk.getStyleClass().add("details-key");

        Label lv = new Label(v);
        lv.getStyleClass().add("details-val");
        lv.setWrapText(true);

        g.add(lk, 0, row);
        g.add(lv, 1, row);

        return lv;
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
    private void applyValueLabels(LineChart<String, Number> chart) {
        // attendre que JavaFX crée les nodes des points
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Series<String, Number> s : chart.getData()) {
                for (XYChart.Data<String, Number> d : s.getData()) {
                    Node node = d.getNode();
                    if (node == null) continue;

                    // évite double ajout si refresh
                    if (node.lookup(".value-label") != null) continue;

                    Label lbl = new Label(String.valueOf(d.getYValue()));
                    lbl.getStyleClass().add("value-label");
                    lbl.setMouseTransparent(true);

                    StackPane wrapper = (StackPane) node;
                    wrapper.getChildren().add(lbl);

                    // position au-dessus du point
                    StackPane.setAlignment(lbl, Pos.TOP_CENTER);
                    lbl.setTranslateY(-12);
                }
            }
        });
    }

    private void configureLineChartReadable(LineChart<Number, Number> chart) {

        if (chart == null) return;

        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(true);

        // ✅ X axis = NumberAxis
        if (chart.getXAxis() instanceof NumberAxis x) {
            x.setAutoRanging(true);      // ou false si tu fixes bounds
            x.setForceZeroInRange(false);
            x.setTickUnit(1);            // 1 consultation, 2 consultation...
            x.setMinorTickVisible(false);
            x.setTickLabelRotation(0);
            x.setLabel("Consultation");
        }

        // ✅ Y axis = NumberAxis
        if (chart.getYAxis() instanceof NumberAxis y) {
            y.setAutoRanging(false);
            y.setLowerBound(0);
            y.setUpperBound(10);
            y.setTickUnit(1);
            y.setMinorTickVisible(true);
            y.setLabel("Score (0-10)");
        }

        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);

        chart.applyCss();
        chart.layout();
    }
    private void applyPointLabelsAndTooltips(LineChart<String, Number> chart,
                                             List<String> tooltipTexts) {

        javafx.application.Platform.runLater(() -> {
            int index = 0;

            for (XYChart.Series<String, Number> series : chart.getData()) {
                for (XYChart.Data<String, Number> d : series.getData()) {
                    Node node = d.getNode();
                    if (node == null) continue;

                    // Tooltip (date/heure)
                    String tip = (tooltipTexts != null && index < tooltipTexts.size())
                            ? tooltipTexts.get(index)
                            : d.getXValue();
                    Tooltip.install(node, new Tooltip(series.getName() + " = " + d.getYValue() + "\n" + tip));

                    // Label valeur
                    if (node.lookup(".value-label") == null) {
                        Label lbl = new Label(String.valueOf(d.getYValue()));
                        lbl.getStyleClass().add("value-label");
                        lbl.setMouseTransparent(true);

                        StackPane sp = (StackPane) node;
                        sp.getChildren().add(lbl);
                        StackPane.setAlignment(lbl, Pos.TOP_CENTER);
                        lbl.setTranslateY(-16);
                    }
                    index++;
                }
                // reset index par série ? non : car tooltips list est par consultation
                index = 0;
            }
        });
    }
    private void configureCartesianChart() {

        if (lineConsultations == null || xAxisStats == null || yAxisStats == null) return;

        lineConsultations.setAnimated(false);
        lineConsultations.setCreateSymbols(true);
        lineConsultations.setLegendVisible(true);

        // X (index des consultations) -> labels seront remplacés par date via formatter
        // X: consultations 1..N (on laisse une marge)
        xAxisStats.setAutoRanging(false);
        xAxisStats.setLowerBound(0);     // ✅ marge à gauche
        xAxisStats.setUpperBound(5);     // ✅ marge à droite (sera recalculé)
        xAxisStats.setTickUnit(1);
        xAxisStats.setMinorTickVisible(false);
        xAxisStats.setForceZeroInRange(false);

// Y: score 0..10 (marge haut pour les labels)
        yAxisStats.setAutoRanging(false);
        yAxisStats.setLowerBound(0);
        yAxisStats.setUpperBound(0);    // ✅ marge en haut
        yAxisStats.setTickUnit(1);
        yAxisStats.setMinorTickVisible(false);

        lineConsultations.setHorizontalGridLinesVisible(true);
        lineConsultations.setVerticalGridLinesVisible(true);
        lineConsultations.setPadding(new Insets(10, 10, 10, 10));
    }
    private void addValueLabels(LineChart<Number, Number> chart) {

        javafx.application.Platform.runLater(() -> {

            for (XYChart.Series<Number, Number> s : chart.getData()) {
                for (XYChart.Data<Number, Number> d : s.getData()) {

                    Node node = d.getNode();
                    if (node == null) continue;

                    Label lbl = new Label(String.valueOf(d.getYValue()));
                    lbl.setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-padding: 1 1 1 1;" +
                                    "-fx-background-radius: 1;" +
                                    "-fx-font-size: 50px;" +   // plus petit
                                    "-fx-font-weight: bold;" +
                                    "-fx-border-color: #e5e7eb;" +
                                    "-fx-border-radius: 1;"
                    );

                    StackPane sp = (StackPane) node;
                    sp.getChildren().add(lbl);
                    StackPane.setAlignment(lbl, Pos.TOP_CENTER);
                    lbl.setTranslateY(-12);
                }
            }
        });
    }
    private void addValueLabelsSafe(LineChart<Number, Number> chart) {

        javafx.application.Platform.runLater(() -> {
            for (XYChart.Series<Number, Number> s : chart.getData()) {
                for (XYChart.Data<Number, Number> d : s.getData()) {

                    Node node = d.getNode();
                    if (node == null) continue;

                    // ✅ évite doublons
                    if (node.lookup(".value-label") != null) continue;

                    Label lbl = new Label(String.valueOf(d.getYValue()));
                    lbl.getStyleClass().add("value-label");
                    lbl.setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-padding: 4 8 4 8;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-border-color: #e5e7eb;" +
                                    "-fx-border-radius: 8;"
                    );

                    StackPane sp = (StackPane) node;
                    sp.getChildren().add(lbl);
                    StackPane.setAlignment(lbl, Pos.TOP_CENTER);
                    lbl.setTranslateY(-18);
                }
            }
        });
    }



    /* =========================================================
   🔔 Notifications : Upload PDF parent -> psy (BackOffice)
   ========================================================= */

    private void initNotificationsUI() {
        if (btnNotif == null) return;

        notifPopup = new Popup();
        notifPopup.setAutoHide(true);
        notifPopup.setHideOnEscape(true);

        VBox rootBox = new VBox(12);
        rootBox.getStyleClass().add("notifRoot");
        rootBox.setPrefWidth(460);
        rootBox.setMaxWidth(460);

        HBox header = new HBox(10);
        header.getStyleClass().add("notifHeader");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Boîte de notifications");
        title.getStyleClass().add("notifHeaderTitle");

        notifHeaderSub = new Label("Documents envoyés par les parents");
        notifHeaderSub.getStyleClass().add("notifHeaderSub");

        VBox titleBox = new VBox(2, title, notifHeaderSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refresh = new Button("Actualiser");
        refresh.getStyleClass().add("notifRefreshBtn");
        refresh.setOnAction(e -> refreshNotifications());

        header.getChildren().addAll(titleBox, spacer, refresh);

        notifListBox = new VBox(10);
        ScrollPane sp = new ScrollPane(notifListBox);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("notifScroll");
        sp.setPrefHeight(420);

        rootBox.getChildren().addAll(header, sp);
        rootBox.setMinWidth(460);
        rootBox.setMaxWidth(460);

// ✅ IMPORTANT: forcer la feuille CSS dans le Popup
        rootBox.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        notifPopup.getContent().add(rootBox);

        btnNotif.setOnAction(e -> {
            refreshNotifications();
            toggleNotifPopup();
        });

        refreshNotifBadge();
    }

    private void toggleNotifPopup() {
        if (notifPopup == null || btnNotif == null) return;

        if (notifPopup.isShowing()) {
            notifPopup.hide();
            return;
        }

        var b = btnNotif.localToScreen(btnNotif.getBoundsInLocal());
        if (b == null) return;

        double x = b.getMaxX() - 460;
        double y = b.getMaxY() + 8;

        notifPopup.show(btnNotif, x, y);
        notifPopup.show(root.getScene().getWindow(), x, y);
    }

    private void refreshNotifBadge() {
        if (lblNotifBadge == null) return;

        try {
            int n = new SuivieServices().countNewParentUploads();
            lblNotifBadge.setText(String.valueOf(n));
            lblNotifBadge.setVisible(n > 0);
            lblNotifBadge.setManaged(n > 0);
        } catch (Exception e) {
            lblNotifBadge.setVisible(false);
            lblNotifBadge.setManaged(false);
        }
    }

    private void refreshNotifications() {
        if (notifListBox == null) return;

        notifListBox.getChildren().clear();

        try {
            SuivieServices ss = new SuivieServices();
            List<SuivieServices.ParentUploadNotif> list = ss.listParentUploads();

            if (list == null || list.isEmpty()) {
                VBox empty = new VBox(6);
                empty.getStyleClass().add("notifEmpty");

                Label t = new Label("Aucune notification");
                t.getStyleClass().add("notifEmptyTitle");

                Label s = new Label("Quand un parent envoie un PDF, il apparaîtra ici.");
                s.getStyleClass().add("notifEmptySub");
                s.setWrapText(true);

                empty.getChildren().addAll(t, s);
                notifListBox.getChildren().add(empty);

                refreshNotifBadge();
                notifListBox.applyCss();
                notifListBox.layout();
                return;
            }

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (SuivieServices.ParentUploadNotif n : list) {
                notifListBox.getChildren().add(buildNotifCard(n, df));
            }

            refreshNotifBadge();

            // ✅ force CSS/layout après ajout dynamique
            notifListBox.applyCss();
            notifListBox.layout();

        } catch (Exception e) {
            VBox err = new VBox(6);
            err.getStyleClass().add("notifEmpty");

            Label t = new Label("Erreur notifications");
            t.getStyleClass().add("notifEmptyTitle");

            Label s = new Label(safe(e.getMessage()));
            s.getStyleClass().add("notifEmptySub");
            s.setWrapText(true);

            err.getChildren().addAll(t, s);
            notifListBox.getChildren().add(err);

            refreshNotifBadge();
            notifListBox.applyCss();
            notifListBox.layout();
        }
    }

    private void downloadParentPdf(SuivieServices.ParentUploadNotif n) {
        try {
            if (n == null || n.filePath == null || n.filePath.isBlank()) {
                showAlert("Téléchargement", "Chemin du PDF introuvable.");
                return;
            }

            java.io.File src = new java.io.File(n.filePath);
            if (!src.exists()) {
                showAlert("Téléchargement", "Le fichier n'existe pas sur le disque :\n" + n.filePath);
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            String initialName = (n.fileName != null && !n.fileName.isBlank()) ? n.fileName : src.getName();
            fc.setInitialFileName(initialName);

            Stage owner = (Stage) root.getScene().getWindow();
            java.io.File dest = fc.showSaveDialog(owner);

            if (dest == null) return;

            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showAlert("Téléchargement", "PDF enregistré :\n" + dest.getAbsolutePath());

        } catch (Exception e) {
            showAlert("Téléchargement", "Erreur : " + safe(e.getMessage()));
        }
    }


    private VBox buildNotifCard(SuivieServices.ParentUploadNotif n, DateTimeFormatter df) {

        VBox card = new VBox(8);
        card.getStyleClass().add("notifCard");
        if (!n.seen) card.getStyleClass().add("notifCardNew");

        // ✅ pour éviter que la card devienne minuscule / bizarre
        card.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        String subject = (n.subject == null || n.subject.isBlank()) ? "Document (sans sujet)" : n.subject;

        Label title = new Label(subject);
        title.getStyleClass().add("notifTitle");

        // ✅ CRUCIAL: ellipsis + ne prend pas toute la place
        title.setWrapText(false);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label status = new Label(n.seen ? "VU" : "NOUVEAU");
        status.getStyleClass().add(n.seen ? "notifBadgeSeen" : "notifBadgeNew");
        status.setMinWidth(Region.USE_PREF_SIZE);

        top.getChildren().addAll(title, status);

        String when = (n.uploadedAt == null) ? "-" : df.format(n.uploadedAt.toLocalDateTime());

        Label meta = new Label("Parent : " + safe(n.email) + " • Enfant : " + safe(n.enfant) + " • " + when);
        meta.getStyleClass().add("notifMeta");
        meta.setWrapText(true);

        Label file = new Label("Fichier : " + safe(n.fileName));
        file.getStyleClass().add("notifFile");
        file.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnDl = new Button("Télécharger");
        btnDl.getStyleClass().add("notifDownloadBtn");

        Button btnSeen = new Button("Marquer vu");
        btnSeen.getStyleClass().add("notifSecondaryBtn");
        btnSeen.setDisable(n.seen);

        btnDl.setOnAction(ev -> {
            downloadParentPdf(n);
            if (!n.seen) {
                markNotifSeen(n.idUpload);   // ✅ ici
                refreshNotifications();
            }
        });

        btnSeen.setOnAction(ev -> {
            markNotifSeen(n.idUpload);       // ✅ ici
            refreshNotifications();
        });

        actions.getChildren().addAll(btnDl, btnSeen);

        card.getChildren().addAll(top, meta, file, actions);
        return card;
    }
    private void markNotifSeen(int idUpload) {
        try {
            new SuivieServices().markParentUploadSeen(idUpload);
            refreshNotifBadge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}