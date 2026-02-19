package Controller;

import Entites.Conseil;
import Services.ConseilServices;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionArticlesBackController {

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;

    @FXML private ToggleButton tbConsultations;
    @FXML private VBox consultationsSubMenu;
    @FXML private Button btnGestionConsultations;
    @FXML private Button btnAjouterFab;
    @FXML private Button btnFrontOfficeFab;
    @FXML private VBox fabGroup;
    @FXML private StackPane pageBg; // si ton StackPane a ce styleClass, donne-lui fx:id="pageBg" dans FXML

    @FXML
    private void openFrontOffice() { switchTo("/MainArticles.fxml"); }


    @FXML private Button btnSubSuivie;
    @FXML private Button btnSubTherapie;
    @FXML private Button btnSubArticles;


    @FXML private Button btnMenuSuivie;
    @FXML private Button btnMenuTherapie;
    @FXML private Button btnMenuArticles;

    @FXML private Button btnFrontOffice;



    private final ObservableList<Conseil> master = FXCollections.observableArrayList();
    private ConseilServices conseilService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {

        conseilService = new ConseilServices();

        // Sidebar dropdown (فتح submenu لأننا في Articles)
        initSidebarDropdownOpenByDefault(true);

        setParentConsultationsActive(true);
        setSubActive(btnSubArticles);

        // Navigation submenu
        if (btnSubSuivie != null) btnSubSuivie.setOnAction(e -> { setSubActive(btnSubSuivie); switchTo("/AjouterSuivie.fxml"); });
        if (btnSubTherapie != null) btnSubTherapie.setOnAction(e -> { setSubActive(btnSubTherapie); switchTo("/AjouterTherapie.fxml"); });
        if (btnSubArticles != null) btnSubArticles.setOnAction(e -> { setSubActive(btnSubArticles); switchTo("/GestionArticlesBack.fxml"); });

        // Actions FAB
        if (btnAjouterFab != null) btnAjouterFab.setOnAction(e -> onAjouter());
        if (btnFrontOfficeFab != null) btnFrontOfficeFab.setOnAction(e -> switchTo("/MainArticles.fxml"));

        // Search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> refreshCards());
        }

        // Wrap responsive
        if (cardsPane != null) {
            cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
                double w = newV.doubleValue();
                cardsPane.setPrefWrapLength(Math.max(500, w - 40));
            });
        }

        reloadFromDb();
    }



    private void initNavigation() {
        if (btnMenuSuivie != null) btnMenuSuivie.setOnAction(e -> switchTo("/AjouterSuivie.fxml"));
        if (btnMenuTherapie != null) btnMenuTherapie.setOnAction(e -> switchTo("/AjouterTherapie.fxml"));
        if (btnMenuArticles != null) btnMenuArticles.setOnAction(e -> switchTo("/GestionArticlesBack.fxml"));

        if (btnFrontOffice != null) btnFrontOffice.setOnAction(e -> switchTo("/MainArticles.fxml"));
    }

    private void initUiListeners() {
        if (searchField != null) {
            ChangeListener<String> l = (obs, o, n) -> refreshCards();
            searchField.textProperty().addListener(l);
        }

        if (btnAjouter != null) {
            btnAjouter.setOnAction(e -> onAjouter());
        }

        if (cardsPane != null) {
            cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
                double w = newV.doubleValue();
                cardsPane.setPrefWrapLength(Math.max(500, w - 40));
            });
        }


    }

    private void reloadFromDb() {
        List<Conseil> list = conseilService.afficherConseils();
        master.setAll(list);
        refreshCards();
    }

    private void refreshCards() {
        cardsPane.getChildren().clear();

        String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        List<Conseil> filtered = conseilService.rechercher(q);

        for (Conseil c : filtered) cardsPane.getChildren().add(buildCard(c));

        if (countLabel != null) countLabel.setText(filtered.size() + " Articles");

        boolean empty = filtered.isEmpty();
        if (emptyBox != null) {
            emptyBox.setVisible(empty);
            emptyBox.setManaged(empty);
        }
    }

    private Node buildCard(Conseil c) {

        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");

        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");

        Label title = new Label(safe(c.getTitre()));
        title.getStyleClass().add("card-title");

        Label sub = new Label(safe(c.getCategorie()));
        sub.getStyleClass().add("card-subtitle");

        VBox nameBox = new VBox(2, title, sub);
        nameBox.getStyleClass().add("name-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        band.getChildren().addAll(nameBox, spacer);

        Region headerLine = new Region();
        headerLine.getStyleClass().add("card-header-line");

        VBox body = new VBox(12);
        body.getStyleClass().add("suivie-body");

        String metaTxt = "AUTEUR\n" + safe(c.getAuteur())
                + "\n\nDATE\n" + formatDate(c.getDateCreation())
                + "\n\nCONTENU\n" + previewText(c.getContenu(), 160);

        Label info = new Label(metaTxt);
        info.setWrapText(true);
        info.getStyleClass().add("suivie-info");

        HBox actions = new HBox(12);
        actions.getStyleClass().add("card-actions");

        Button lire = new Button("Lire");
        lire.getStyleClass().addAll("btn-card", "btn-voir");
        lire.setOnAction(e -> onLire(c));

        Button modif = new Button("Éditer");
        modif.getStyleClass().addAll("btn-card", "btn-edit");
        modif.setOnAction(e -> onModifier(c));

        Button suppr = new Button("Supprimer");
        suppr.getStyleClass().addAll("btn-card", "btn-delete");
        suppr.setOnAction(e -> onSupprimer(c));

        HBox.setHgrow(lire, Priority.ALWAYS);
        HBox.setHgrow(modif, Priority.ALWAYS);
        HBox.setHgrow(suppr, Priority.ALWAYS);
        lire.setMaxWidth(Double.MAX_VALUE);
        modif.setMaxWidth(Double.MAX_VALUE);
        suppr.setMaxWidth(Double.MAX_VALUE);

        actions.getChildren().addAll(lire, modif, suppr);

        body.getChildren().addAll(info, actions);

        card.getChildren().addAll(band, headerLine, body);
        return card;
    }

    private void onLire(Conseil c) {
        Stage stage = createCustomStage("Article", 920, 520);
        VBox content = makeContentBox();

        Label title = new Label(safe(c.getTitre()));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #111827;");

        Label meta = new Label("Catégorie: " + safe(c.getCategorie()) +
                "   •   Auteur: " + safe(c.getAuteur()) +
                "   •   " + formatDate(c.getDateCreation()));
        meta.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: 700;");

        TextArea ta = new TextArea(safe(c.getContenu()));
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.getStyleClass().add("inputModernArea");
        ta.setPrefHeight(320);

        Button btnClose = new Button("Fermer");
        btnClose.getStyleClass().addAll("btn-soft", "btn-hover");
        btnClose.setOnAction(e -> stage.close());

        HBox actions = new HBox(btnClose);
        actions.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(title, meta, ta, actions);
        ((VBox) stage.getScene().getRoot()).getChildren().add(content);

        stage.showAndWait();
    }

    @FXML
    private void onAjouter() {
        Optional<Conseil> res = showConseilWindow(null, "Ajouter un article");
        res.ifPresent(c -> {
            conseilService.ajouterConseil(c);
            reloadFromDb();
        });
    }

    private void onModifier(Conseil existing) {
        Optional<Conseil> res = showConseilWindow(existing, "Modifier l'article");
        res.ifPresent(updated -> {
            updated.setIdArticle(existing.getIdArticle());
            conseilService.modifierConseil(updated);
            reloadFromDb();
        });
    }

    private void onSupprimer(Conseil c) {
        Stage stage = createCustomStage("Suppression", 700, 260);
        VBox content = makeContentBox();

        Label h1 = new Label("Supprimer cet article ?");
        h1.getStyleClass().add("dialog-h1");

        Label sub = new Label("Cette action est irréversible.");
        sub.getStyleClass().add("dialog-sub");

        Label chip = new Label("Titre : " + safe(c.getTitre()));
        chip.getStyleClass().add("dialog-chip");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().addAll("btn-soft", "btn-hover");
        btnCancel.setOnAction(e -> stage.close());

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("btn-danger", "btn-hover");
        btnDelete.setOnAction(e -> {
            conseilService.supprimerConseil(c.getIdArticle());
            stage.close();
            reloadFromDb();
        });

        actions.getChildren().addAll(btnCancel, btnDelete);

        content.getChildren().addAll(h1, sub, chip, actions);
        ((VBox) stage.getScene().getRoot()).getChildren().add(content);

        stage.showAndWait();
    }

    // =================== Fenêtre Ajouter/Modifier (comme Suivie) ===================

    private Optional<Conseil> showConseilWindow(Conseil initial, String title) {

        Stage stage = createCustomStage(title, 980, 560);
        VBox content = makeContentBox();

        Label lblErrors = new Label("");
        lblErrors.getStyleClass().add("form-errors");
        lblErrors.setWrapText(true);

        TextField tfTitre  = new TextField(initial == null ? "" : safe(initial.getTitre()));
        TextField tfAuteur = new TextField(initial == null ? "" : safe(initial.getAuteur()));

        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("Général", "Autisme", "Sommeil", "Comportement");

        // ✅ مهم: مثل "État" في Suivie
        // - Ajouter: تبدأ فارغة => Rouge
        // - Modifier: تكون معبّية => Vert مباشرة (اختياري، نعملها كما Suivie في modifier)
        if (initial == null) {
            cbCat.setPromptText("Choisir...");
            cbCat.setValue(null); // ✅ فارغة في البداية
        } else {
            cbCat.setValue(safe(initial.getCategorie()));
        }

        TextArea taContenu = new TextArea(initial == null ? "" : safe(initial.getContenu()));
        taContenu.setWrapText(true);
        taContenu.setPrefRowCount(10);

        tfTitre.getStyleClass().add("inputModern");
        tfAuteur.getStyleClass().add("inputModern");
        cbCat.getStyleClass().add("inputModern");
        taContenu.getStyleClass().add("inputModernArea");

        GridPane gp = new GridPane();
        gp.setHgap(18);
        gp.setVgap(14);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0, c1);

        gp.add(new Label("Titre *"), 0, 0);
        gp.add(tfTitre, 1, 0);

        gp.add(new Label("Catégorie *"), 0, 1);
        gp.add(cbCat, 1, 1);

        gp.add(new Label("Auteur *"), 0, 2);
        gp.add(tfAuteur, 1, 2);

        gp.add(new Label("Contenu *"), 0, 3);
        gp.add(taContenu, 1, 3);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().addAll("btn-soft", "btn-hover");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSave = new Button(initial == null ? "Ajouter" : "Sauvegarder");
        btnSave.getStyleClass().addAll("btn-solid-dark", "btn-hover");

        actions.getChildren().addAll(btnCancel, btnSave);

        content.getChildren().addAll(lblErrors, gp, actions);
        ((VBox) stage.getScene().getRoot()).getChildren().add(content);

        final Conseil[] result = new Conseil[1];

        // ✅ باش Catégorie تولّي خضراء فقط بعد اختيار (في ajout)
        final boolean[] catTouched = { initial != null }; // modifier => تعتبر "touchée"
        final boolean[] submitted  = { false };

        Runnable validate = () -> {

            lblErrors.setText("");

            tfTitre.getStyleClass().removeAll("field-error", "field-ok");
            tfAuteur.getStyleClass().removeAll("field-error", "field-ok");
            taContenu.getStyleClass().removeAll("field-error", "field-ok");
            cbCat.getStyleClass().removeAll("field-error", "field-ok");

            boolean ok = true;
            StringBuilder sb = new StringBuilder("Champs à corriger :\n");

            // Titre
            if (isBlank(tfTitre.getText()) || tfTitre.getText().trim().length() < 3) {
                ok = false;
                sb.append("- Titre (min 3 caractères)\n");
                tfTitre.getStyleClass().add("field-error");
            } else {
                tfTitre.getStyleClass().add("field-ok");
            }

            // ✅ Catégorie: rouge au début (ajout), vert après sélection
            boolean catOk = cbCat.getValue() != null && !cbCat.getValue().trim().isEmpty();

            if (!catTouched[0]) {
                // au démarrage (ajout) => rouge
                ok = false;
                sb.append("- Catégorie (obligatoire)\n");
                cbCat.getStyleClass().add("field-error");
            } else {
                if (!catOk) {
                    ok = false;
                    sb.append("- Catégorie (obligatoire)\n");
                    cbCat.getStyleClass().add("field-error");
                } else {
                    cbCat.getStyleClass().add("field-ok");
                }
            }

            // Auteur
            if (isBlank(tfAuteur.getText()) || tfAuteur.getText().trim().length() < 3) {
                ok = false;
                sb.append("- Auteur (min 3 caractères)\n");
                tfAuteur.getStyleClass().add("field-error");
            } else {
                tfAuteur.getStyleClass().add("field-ok");
            }

            // Contenu
            if (isBlank(taContenu.getText()) || taContenu.getText().trim().length() < 10) {
                ok = false;
                sb.append("- Contenu (min 10 caractères)\n");
                taContenu.getStyleClass().add("field-error");
            } else {
                taContenu.getStyleClass().add("field-ok");
            }

            btnSave.setDisable(!ok);
            if (!ok) lblErrors.setText(sb.toString());
        };

        // listeners
        tfTitre.textProperty().addListener((o,a,b) -> validate.run());
        tfAuteur.textProperty().addListener((o,a,b) -> validate.run());
        taContenu.textProperty().addListener((o,a,b) -> validate.run());

        cbCat.valueProperty().addListener((o, a, b) -> {
            catTouched[0] = true;     // ✅ أول sélection => touchée
            validate.run();
        });

        validate.run();

        btnSave.setOnAction(e -> {
            submitted[0] = true;
            validate.run();
            if (btnSave.isDisable()) return;

            result[0] = new Conseil(
                    tfTitre.getText().trim(),
                    taContenu.getText().trim(),
                    cbCat.getValue(),
                    tfAuteur.getText().trim()
            );
            stage.close();
        });

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // =================== Helpers UI ===================

    private Stage createCustomStage(String title, double w, double h) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(title);

        VBox shell = new VBox();
        shell.getStyleClass().add("custom-window-shell");

        HBox header = new HBox();
        header.getStyleClass().add("custom-header");
        header.setPadding(new Insets(12, 14, 12, 14));
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("custom-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("custom-close");
        btnClose.setOnAction(e -> stage.close());

        header.getChildren().addAll(lblTitle, spacer, btnClose);
        shell.getChildren().add(header);

        Scene scene = new Scene(shell, w, h);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        stage.setScene(scene);

        return stage;
    }

    private VBox makeContentBox() {
        VBox content = new VBox(12);
        content.getStyleClass().add("custom-content");
        return content;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String previewText(String s, int max){
        if (s == null) return "";
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String formatDate(Timestamp ts) {
        if (ts == null) return "";
        return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DF);
    }

    private void switchTo(String fxmlPath) {
        try {
            java.net.URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IllegalArgumentException("FXML introuvable: " + fxmlPath +
                        "\n✅ Vérifie qu’il est bien dans: src/main/resources" +
                        "\n✅ Vérifie majuscules/minuscules du nom.");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur Navigation");
            a.setHeaderText("Erreur lors du chargement");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    private void setParentConsultationsActive(boolean active){
        btnGestionConsultations.getStyleClass().remove("sideBtnActive");
        if(active) btnGestionConsultations.getStyleClass().add("sideBtnActive");
    }

    private void setSubActive(Button selected){
        // reset
        btnSubSuivie.getStyleClass().remove("subBtnActive");
        btnSubTherapie.getStyleClass().remove("subBtnActive");
        btnSubArticles.getStyleClass().remove("subBtnActive");

        // active only the clicked one
        selected.getStyleClass().add("subBtnActive");

        // parent stays active as long as we are in this module
        setParentConsultationsActive(true);
    }

    private void initSidebarDropdown(boolean openByDefault) {
        if (tbConsultations == null || consultationsSubMenu == null) return;

        tbConsultations.setSelected(openByDefault);
        tbConsultations.setText(openByDefault ? "▾" : "▸");

        consultationsSubMenu.setVisible(openByDefault);
        consultationsSubMenu.setManaged(openByDefault);

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
    private void initSidebarDropdownOpenByDefault(boolean open) {
        if (tbConsultations == null || consultationsSubMenu == null) return;

        tbConsultations.setSelected(open);
        tbConsultations.setText(open ? "▾" : "▸");

        consultationsSubMenu.setVisible(open);
        consultationsSubMenu.setManaged(open);

        tbConsultations.setOnAction(e -> {
            boolean isOpen = tbConsultations.isSelected();
            consultationsSubMenu.setVisible(isOpen);
            consultationsSubMenu.setManaged(isOpen);
            tbConsultations.setText(isOpen ? "▾" : "▸");
        });

        if (btnGestionConsultations != null) {
            btnGestionConsultations.setOnAction(e -> tbConsultations.fire());
        }
    }

}
