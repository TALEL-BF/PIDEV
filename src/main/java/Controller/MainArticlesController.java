package com.auticare.Controller;

import com.auticare.Entites.Conseil;
import com.auticare.Services.ConseilServices;
import com.auticare.models.User;
import com.auticare.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainArticlesController {

    // ---------- UI (sidebar) ----------
    @FXML private BorderPane root;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;

    // ---------- UI (contenu principal) ----------
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label countLabel;
    @FXML private TilePane cardsPane;

    @FXML private ToggleButton chipGeneral, chipAutisme, chipSommeil, chipComportement;

    // ---------- Data ----------
    private final List<Conseil> allArticles = new ArrayList<>();
    private ConseilServices conseilService;
    private ToggleGroup chipsGroup;
    private final Set<Integer> likedSession = new HashSet<>();
    private User currentUser;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            userEmailLabel.setText(currentUser.getEmail());
        }

        conseilService = new ConseilServices();
        setupChips();
        setupActions();
        loadFromDb();
        refresh();
    }

    // ==================== MÉTHODES DE NAVIGATION (sidebar parent) ====================

    @FXML
    private void showDashboard() {
        switchTo("/views/ParentDashboard.fxml");
    }

    @FXML
    private void goToProfile() {
        switchTo("/views/ParentProfile.fxml");
    }

    @FXML
    private void showSchedule() {
        showInfo("Emploi du temps", "Fonctionnalité à venir");
    }

    @FXML
    private void showCourses() {
        switchTo("/coursaffichage.fxml");
    }

    @FXML
    private void showGames() {
        showInfo("Jeux", "Bientôt disponible");
    }

    @FXML
    private void showTherapeuticFollowUp() {
        switchTo("/ParentSuivi.fxml");
    }

    @FXML
    private void showEvents() {
        showInfo("Événements", "Bientôt disponible");
    }

    @FXML
    private void showArticles() {
        // déjà sur la page
        refresh();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().endSession();
        switchTo("/views/Login.fxml");
    }

    // ==================== UTILITAIRE DE NAVIGATION ====================

    private void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(newRoot);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation", "Impossible de charger : " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
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

    // ==================== CHIPS ====================

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

    // ==================== ACTIONS ====================

    private void setupActions() {
        if (btnSearch != null) btnSearch.setOnAction(e -> refresh());
        if (searchField != null) searchField.textProperty().addListener((o, a, b) -> refresh());
    }

    // ==================== DATA ====================

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
                .sorted((a1, a2) -> {
                    int c = Integer.compare(a2.getLikesCount(), a1.getLikesCount());
                    if (c != 0) return c;
                    return safe(a1.getTitre()).compareToIgnoreCase(safe(a2.getTitre()));
                })
                .collect(Collectors.toList());

        int topLikes = filtered.isEmpty() ? 0 : filtered.get(0).getLikesCount();

        for (int i = 0; i < filtered.size(); i++) {
            Conseil a = filtered.get(i);
            boolean trending = (i == 0 && topLikes > 0);
            cardsPane.getChildren().add(buildCard(a, trending));
        }

        if (countLabel != null) {
            countLabel.setText(filtered.size() + " Articles disponibles");
        }
    }

    // ===================== CARD UI =====================

    private Pane buildCard(Conseil a, boolean trending) {
        VBox card = new VBox();
        card.getStyleClass().add("articleCard");
        card.setPrefWidth(320);
        card.setMinWidth(320);
        card.setMaxWidth(320);
        card.setMinHeight(380);
        card.setPrefHeight(380);
        card.setMaxHeight(380);

        final double HEADER_H = 220;

        StackPane thumbWrap = new StackPane();
        thumbWrap.getStyleClass().add("articleThumb");
        thumbWrap.setMinHeight(HEADER_H);
        thumbWrap.setPrefHeight(HEADER_H);
        thumbWrap.setMaxHeight(HEADER_H);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(22);
        clip.setArcHeight(22);
        clip.widthProperty().bind(card.widthProperty());
        clip.setHeight(HEADER_H);
        thumbWrap.setClip(clip);

        ImageView thumb = new ImageView();
        thumb.setPreserveRatio(true);
        thumb.setSmooth(true);

        Image authorImage = loadAuthorImage(a.getAuteurImage());
        if (authorImage != null) {
            thumb.setImage(authorImage);
            thumb.fitWidthProperty().bind(thumbWrap.widthProperty());
            thumb.fitHeightProperty().bind(thumbWrap.heightProperty());
            thumb.setPreserveRatio(false);
        } else {
            thumb.setVisible(false);
            thumb.setManaged(false);
        }

        Region darkOverlay = new Region();
        darkOverlay.setStyle("-fx-background-color: rgba(0,0,0,0);");
        darkOverlay.setMinHeight(HEADER_H);
        darkOverlay.prefWidthProperty().bind(thumbWrap.widthProperty());

        Label tag = new Label(safe(a.getCategorie()));
        tag.getStyleClass().add("articleTag");
        StackPane.setAlignment(tag, Pos.TOP_RIGHT);
        StackPane.setMargin(tag, new Insets(12));

        Label trend = new Label("🔥 Tendance");
        trend.getStyleClass().add("trendBadge");
        trend.setVisible(trending);
        trend.setManaged(trending);
        StackPane.setAlignment(trend, Pos.TOP_LEFT);
        StackPane.setMargin(trend, new Insets(12));

        Label headerTitle = new Label(safe(a.getTitre()));
        headerTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        headerTitle.setWrapText(true);
        headerTitle.setMaxWidth(280);
        StackPane.setAlignment(headerTitle, Pos.BOTTOM_LEFT);
        StackPane.setMargin(headerTitle, new Insets(0, 12, 12, 12));

        if (trending) {
            thumbWrap.getChildren().addAll(thumb, darkOverlay, tag, trend, headerTitle);
        } else {
            thumbWrap.getChildren().addAll(thumb, darkOverlay, tag, headerTitle);
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(14, 16, 16, 16));

        HBox authorBox = new HBox(8);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        StackPane miniAvatar = new StackPane();
        miniAvatar.setMinSize(30, 30);
        miniAvatar.setPrefSize(30, 30);
        miniAvatar.setMaxSize(30, 30);
        miniAvatar.setStyle("-fx-background-color: linear-gradient(to bottom, #7C3AED, #EC4899); -fx-background-radius: 15;");

        String auteur = safe(a.getAuteur());
        Label authorInitial = new Label(auteur.isEmpty() ? "?" : auteur.substring(0, 1).toUpperCase());
        authorInitial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        miniAvatar.getChildren().add(authorInitial);

        VBox authorInfo = new VBox(2);
        Label authorName = new Label(auteur);
        authorName.getStyleClass().add("articleMeta");
        authorName.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label("Article récent");
        dateLabel.getStyleClass().add("articleMeta");
        dateLabel.setStyle("-fx-font-size: 11px;");

        authorInfo.getChildren().addAll(authorName, dateLabel);
        authorBox.getChildren().addAll(miniAvatar, authorInfo);

        Label summary = new Label(preview(safe(a.getContenu()), 100));
        summary.getStyleClass().add("articleSummary");
        summary.setWrapText(true);
        summary.setMaxHeight(50);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Label likeCount = new Label(String.valueOf(a.getLikesCount()));
        likeCount.getStyleClass().add("likeBubble");

        Button btnLike = new Button("♡");
        btnLike.getStyleClass().addAll("likeFab", "likeOff");

        if (likedSession.contains(a.getIdArticle())) {
            btnLike.setText("♥");
            btnLike.getStyleClass().remove("likeOff");
            btnLike.getStyleClass().add("likeOn");
        }

        btnLike.setOnAction(ev -> {
            int id = a.getIdArticle();
            if (likedSession.contains(id)) {
                likedSession.remove(id);
                int newCount = conseilService.decrementLike(id);
                a.setLikesCount(newCount);
                likeCount.setText(String.valueOf(newCount));
                btnLike.setText("♡");
                btnLike.getStyleClass().remove("likeOn");
                if (!btnLike.getStyleClass().contains("likeOff")) btnLike.getStyleClass().add("likeOff");
            } else {
                likedSession.add(id);
                int newCount = conseilService.incrementLike(id);
                a.setLikesCount(newCount);
                likeCount.setText(String.valueOf(newCount));
                btnLike.setText("♥");
                btnLike.getStyleClass().remove("likeOff");
                if (!btnLike.getStyleClass().contains("likeOn")) btnLike.getStyleClass().add("likeOn");
            }
            refresh();
        });

        HBox likeBox = new HBox(6, btnLike, likeCount);
        likeBox.setAlignment(Pos.CENTER_LEFT);

        Button btnLire = new Button("Lire l'article");
        btnLire.getStyleClass().add("articleBtn");
        btnLire.setOnAction(e -> openArticle(a));

        HBox lireBox = new HBox(btnLire);
        lireBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(lireBox, Priority.ALWAYS);

        actionRow.getChildren().addAll(likeBox, lireBox);

        content.getChildren().addAll(authorBox, summary, spacer, actionRow);
        card.getChildren().addAll(thumbWrap, content);
        return card;
    }

    private void openArticle(Conseil a) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        StackPane rootModal = new StackPane();
        rootModal.getStyleClass().add("articleModalRoot");

        ImageView bgImg = new ImageView();
        bgImg.getStyleClass().add("articleModalBg");
        bgImg.setPreserveRatio(false);
        bgImg.fitWidthProperty().bind(rootModal.widthProperty());
        bgImg.fitHeightProperty().bind(rootModal.heightProperty());

        Image img = loadAuthorImage(a.getAuteurImage());
        if (img != null) {
            bgImg.setImage(img);
            bgImg.setEffect(new GaussianBlur(28));
            bgImg.setOpacity(0.18);
        } else {
            bgImg.setVisible(false);
            bgImg.setManaged(false);
        }

        Region overlay = new Region();
        overlay.getStyleClass().add("articleModalOverlay");

        VBox modal = new VBox();
        modal.getStyleClass().add("articleModalCard");
        modal.setMaxWidth(920);
        modal.setPrefWidth(920);

        HBox header = new HBox();
        header.getStyleClass().add("articleModalHeader");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(safe(a.getTitre()));
        title.getStyleClass().add("articleModalTitle");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("articleModalClose");
        btnClose.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, sp, btnClose);

        Label meta = new Label("Catégorie : " + safe(a.getCategorie()) + "   •   Auteur : " + safe(a.getAuteur()));
        meta.getStyleClass().add("articleModalMeta");

        Label contentLabel = new Label(safe(a.getContenu()));
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("articleModalContentText");

        VBox contentBox = new VBox(contentLabel);
        contentBox.getStyleClass().add("articleModalContentBox");

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("articleModalScroll");

        Button btnFermer = new Button("Fermer");
        btnFermer.getStyleClass().add("articleModalCloseRed");
        btnFermer.setOnAction(e -> stage.close());

        HBox footer = new HBox(btnFermer);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("articleModalFooter");

        VBox body = new VBox(14, meta, scroll, footer);
        body.getStyleClass().add("articleModalBody");

        modal.getChildren().addAll(header, body);
        rootModal.getChildren().addAll(bgImg, overlay, modal);

        Scene scene = new Scene(rootModal, 980, 560);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setScene(scene);
        stage.showAndWait();
    }

    // ===================== GESTION DES IMAGES =====================

    private Image loadAuthorImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return loadDefaultAuthorImage();
        }
        try {
            String path = imagePath.trim().replace("\\", "/");
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return new Image(path, true);
            }
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            var resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm(), true);
            }
            String assetsPath = "/assets/" + path;
            resourceUrl = getClass().getResource(assetsPath);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm(), true);
            }
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
            file = new File(System.getProperty("user.dir"), path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
            return loadDefaultAuthorImage();
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + imagePath + " - " + e.getMessage());
            return loadDefaultAuthorImage();
        }
    }

    private Image loadDefaultAuthorImage() {
        try {
            var url = getClass().getResource("/assets/default-author.jpg");
            if (url != null) {
                return new Image(url.toExternalForm(), true);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String preview(String s, int max) {
        String t = safe(s).trim();
        if (t.isEmpty()) return "";
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userEmailLabel.setText(user.getEmail());
        }
    }
}