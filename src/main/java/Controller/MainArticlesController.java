package Controller;

import Entites.Conseil;
import Services.ConseilServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainArticlesController {

    // ---------- UI ----------
    @FXML private BorderPane root;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label countLabel;
    @FXML private TilePane cardsPane;

    @FXML private ToggleButton chipGeneral, chipAutisme, chipSommeil, chipComportement;

    // Sidebar (FrontOffice)
    @FXML private Button btnCours, btnEmploi, btnArticles, btnEvents, btnJeux, btnLogout;

    // ---------- Data ----------
    private final List<Conseil> allArticles = new ArrayList<>();
    private ConseilServices conseilService;
    private ToggleGroup chipsGroup;

    @FXML
    public void initialize() {

        conseilService = new ConseilServices();

        setupChips();
        setupActions();
        setupSidebarNavigation();

        loadFromDb();
        refresh();
    }

    // ===================== SIDEBAR NAV (FrontOffice) =====================

    private void setupSidebarNavigation() {

        // Ici tu mets les bons fxml de ton FrontOffice
        if (btnCours != null)   btnCours.setOnAction(e -> switchTo("/MainCours.fxml"));
        if (btnEmploi != null)  btnEmploi.setOnAction(e -> switchTo("/MainEmploi.fxml"));
        if (btnArticles != null)btnArticles.setOnAction(e -> switchTo("/MainArticles.fxml"));
        if (btnEvents != null)  btnEvents.setOnAction(e -> switchTo("/MainEvents.fxml"));
        if (btnJeux != null)    btnJeux.setOnAction(e -> switchTo("/MainJeux.fxml"));

        // Déconnexion -> tu veux basculer vers AjouterSuivie.fxml
        if (btnLogout != null)  btnLogout.setOnAction(e -> switchTo("/GestionArticlesBack.fxml"));
    }

    // ===================== CHIPS =====================

    private void setupChips() {
        chipsGroup = new ToggleGroup();

        chipGeneral.setToggleGroup(chipsGroup);
        chipAutisme.setToggleGroup(chipsGroup);
        chipSommeil.setToggleGroup(chipsGroup);
        chipComportement.setToggleGroup(chipsGroup);

        chipGeneral.setSelected(true);

        chipsGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            updateChipStyles();
            refresh();
        });

        updateChipStyles();
    }

    private void updateChipStyles() {
        ToggleButton[] chips = { chipGeneral, chipAutisme, chipSommeil, chipComportement };
        for (ToggleButton b : chips) {
            b.getStyleClass().remove("chipActive");
            if (b.isSelected()) b.getStyleClass().add("chipActive");
        }
    }

    private String currentCategory() {
        Toggle t = chipsGroup.getSelectedToggle();
        if (t == chipGeneral) return "Général";
        if (t == chipAutisme) return "Autisme";
        if (t == chipSommeil) return "Sommeil";
        if (t == chipComportement) return "Comportement";
        return "";
    }

    // ===================== ACTIONS =====================

    private void setupActions() {
        if (btnSearch != null) btnSearch.setOnAction(e -> refresh());
        if (searchField != null) searchField.textProperty().addListener((o, a, b) -> refresh());
    }

    // ===================== DATA =====================

    private void loadFromDb() {
        allArticles.clear();
        allArticles.addAll(conseilService.afficherConseils());
    }

    private void refresh() {
        cardsPane.getChildren().clear();

        String q = safe(searchField.getText()).trim().toLowerCase();
        String cat = currentCategory();

        List<Conseil> filtered = allArticles.stream()
                .filter(a -> cat.isBlank() || safe(a.getCategorie()).equalsIgnoreCase(cat))
                .filter(a -> q.isBlank()
                        || safe(a.getTitre()).toLowerCase().contains(q)
                        || safe(a.getContenu()).toLowerCase().contains(q)
                        || safe(a.getAuteur()).toLowerCase().contains(q))
                .collect(Collectors.toList());

        for (Conseil a : filtered) {
            cardsPane.getChildren().add(buildCard(a));
        }

        if (countLabel != null) {
            countLabel.setText(filtered.size() + " Articles disponibles");
        }
    }

    // ===================== CARD UI =====================

    private Pane buildCard(Conseil a) {

        VBox card = new VBox();
        card.getStyleClass().add("articleCard");
        card.setPrefWidth(280);
        card.setMinHeight(320);
        card.setMaxHeight(320);

        StackPane thumbWrap = new StackPane();
        thumbWrap.getStyleClass().add("articleThumb");

        Label tag = new Label(safe(a.getCategorie()));
        tag.getStyleClass().add("articleTag");
        StackPane.setAlignment(tag, Pos.TOP_RIGHT);
        StackPane.setMargin(tag, new Insets(10));
        thumbWrap.getChildren().add(tag);

        VBox content = new VBox(8);
        content.setPadding(new Insets(14, 16, 14, 16));

        Label title = new Label(safe(a.getTitre()));
        title.getStyleClass().add("articleTitle");
        title.setWrapText(true);
        title.setMaxHeight(44);

        Label author = new Label("Par: " + safe(a.getAuteur()));
        author.getStyleClass().add("articleMeta");

        Label summary = new Label(preview(safe(a.getContenu()), 140));
        summary.getStyleClass().add("articleSummary");
        summary.setWrapText(true);
        summary.setMaxHeight(70);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLire = new Button("Lire");
        btnLire.getStyleClass().add("articleBtn");
        btnLire.setOnAction(e -> openArticle(a));

        HBox actions = new HBox(btnLire);
        actions.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(title, author, summary, spacer, actions);
        card.getChildren().addAll(thumbWrap, content);

        return card;
    }

    // ===================== POPUP ARTICLE (custom, sans entête Windows) =====================

    private void openArticle(Conseil a) {

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox shell = new VBox();
        shell.getStyleClass().add("article-shell");

        HBox header = new HBox();
        header.getStyleClass().add("article-header");

        Label title = new Label(safe(a.getTitre()));
        title.getStyleClass().add("article-header-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("article-close");
        btnClose.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, sp, btnClose);

        Label meta = new Label("Catégorie : " + safe(a.getCategorie()) + "   •   Auteur : " + safe(a.getAuteur()));
        meta.getStyleClass().add("article-meta");

        TextArea ta = new TextArea(safe(a.getContenu()));
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.getStyleClass().add("article-area");

        Button btnOk = new Button("Fermer");
        btnOk.getStyleClass().add("article-btn");
        btnOk.setOnAction(e -> stage.close());

        HBox footer = new HBox(btnOk);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("article-footer");

        VBox body = new VBox(10, meta, ta, footer);
        body.getStyleClass().add("article-body");

        shell.getChildren().addAll(header, body);

        Scene scene = new Scene(shell, 760, 460);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setScene(scene);
        stage.showAndWait();
    }

    // ===================== NAV HELPERS =====================

    private void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent rootNode = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(new Scene(rootNode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String preview(String s, int max) {
        String t = safe(s).trim();
        if (t.isEmpty()) return "";
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }
}
