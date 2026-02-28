package Controller;

import Entites.Conseil;
import Services.ConseilServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

//import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    @FXML private ToggleButton tbPsychologie;
    @FXML private VBox psychologieSubMenu;

    @FXML private Button btnArticlesConseils;
    @FXML private Button btnSuivieTherapeutique;

    private Button activeSubBtn;
    // ---------- Data ----------
    private final List<Conseil> allArticles = new ArrayList<>();
    private ConseilServices conseilService;
    private ToggleGroup chipsGroup;

    private final Set<Integer> likedSession = new HashSet<>();

    @FXML
    private void togglePsychologie() {
        boolean show = tbPsychologie.isSelected();
        psychologieSubMenu.setVisible(show);
        psychologieSubMenu.setManaged(show);

        // style actif pour Psychologie quand ouvert
        if (show) {
            if (!tbPsychologie.getStyleClass().contains("sideBtnActive")) {
                tbPsychologie.getStyleClass().add("sideBtnActive");
            }
        } else {
            // si aucun sous-menu n'est actif, on enlève l'état actif
            if (activeSubBtn == null) {
                tbPsychologie.getStyleClass().remove("sideBtnActive");
            }
        }
    }

    @FXML
    public void initialize() {
        // Ouvrir Psychologie par défaut
        tbPsychologie.setSelected(true);
        psychologieSubMenu.setVisible(true);
        psychologieSubMenu.setManaged(true);
        if (!tbPsychologie.getStyleClass().contains("sideBtnActive")) {
            tbPsychologie.getStyleClass().add("sideBtnActive");
        }

        // Activer Articles & Conseils par défaut
        setActiveSubMenu(btnArticlesConseils);

        // Ton init actuel (search, cards, etc.) reste pareil
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

        // 1) filtrer
        List<Conseil> filtered = allArticles.stream()
                .filter(a -> cat.isBlank() || safe(a.getCategorie()).equalsIgnoreCase(cat))
                .filter(a -> q.isBlank()
                        || safe(a.getTitre()).toLowerCase().contains(q)
                        || safe(a.getContenu()).toLowerCase().contains(q)
                        || safe(a.getAuteur()).toLowerCase().contains(q))
                // 2) ✅ TRIER: plus de likes = en premier
                .sorted((a1, a2) -> {
                    int c = Integer.compare(a2.getLikesCount(), a1.getLikesCount()); // desc
                    if (c != 0) return c;

                    // tie-breaker (optionnel) : titre
                    return safe(a1.getTitre()).compareToIgnoreCase(safe(a2.getTitre()));
                })
                .collect(Collectors.toList());

        // ✅ L'article #1 devient "Tendance"
        int topLikes = filtered.isEmpty() ? 0 : filtered.get(0).getLikesCount();

        for (int i = 0; i < filtered.size(); i++) {
            Conseil a = filtered.get(i);

            boolean trending = (i == 0 && topLikes > 0); // top 1
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

        // ===== HEADER AMÉLIORÉ =====
        StackPane thumbWrap = new StackPane();
        thumbWrap.getStyleClass().add("articleThumb");
        thumbWrap.setMinHeight(HEADER_H);
        thumbWrap.setPrefHeight(HEADER_H);
        thumbWrap.setMaxHeight(HEADER_H);

        // Clip arrondi
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(22);
        clip.setArcHeight(22);
        clip.widthProperty().bind(card.widthProperty());
        clip.setHeight(HEADER_H);
        thumbWrap.setClip(clip);

        // Image de fond avec gestion améliorée
        ImageView thumb = new ImageView();
        thumb.setPreserveRatio(true);
        thumb.setSmooth(true);

        // Charger l'image de l'auteur
        Image authorImage = loadAuthorImage(a.getAuteurImage());

        if (authorImage != null) {
            thumb.setImage(authorImage);

            // Ajuster pour couvrir tout l'espace
            thumb.fitWidthProperty().bind(thumbWrap.widthProperty());
            thumb.fitHeightProperty().bind(thumbWrap.heightProperty());

            // S'assurer que l'image couvre tout
            thumb.setPreserveRatio(false);

        } else {
            // Fallback avec gradient si pas d'image
            thumb.setVisible(false);
            thumb.setManaged(false);
        }

        // Overlay sombre pour meilleure lisibilité du texte
        Region darkOverlay = new Region();
        darkOverlay.setStyle("-fx-background-color: rgba(0,0,0,0);");
        darkOverlay.setMinHeight(HEADER_H);
        darkOverlay.prefWidthProperty().bind(thumbWrap.widthProperty());

        // Badge catégorie
        Label tag = new Label(safe(a.getCategorie()));
        tag.getStyleClass().add("articleTag");
        StackPane.setAlignment(tag, Pos.TOP_RIGHT);
        StackPane.setMargin(tag, new Insets(12));

        // Badge tendance
        Label trend = new Label("🔥 Tendance");
        trend.getStyleClass().add("trendBadge");
        trend.setVisible(trending);
        trend.setManaged(trending);
        StackPane.setAlignment(trend, Pos.TOP_LEFT);
        StackPane.setMargin(trend, new Insets(12));

        // Titre de l'article dans le header (optionnel)
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

        // ===== CONTENU =====
        VBox content = new VBox(10);
        content.setPadding(new Insets(14, 16, 16, 16));

        // Informations auteur avec avatar mini
        HBox authorBox = new HBox(8);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        // Mini avatar de l'auteur
        StackPane miniAvatar = new StackPane();
        miniAvatar.setMinSize(30, 30);
        miniAvatar.setPrefSize(30, 30);
        miniAvatar.setMaxSize(30, 30);
        miniAvatar.setStyle("-fx-background-color: linear-gradient(to bottom, #7C3AED, #EC4899); -fx-background-radius: 15;");

        Label authorInitial = new Label(safe(a.getAuteur()).substring(0, 1).toUpperCase());
        authorInitial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        miniAvatar.getChildren().add(authorInitial);

        VBox authorInfo = new VBox(2);
        Label authorName = new Label(safe(a.getAuteur()));
        authorName.getStyleClass().add("articleMeta");
        authorName.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label("Article récent");
        dateLabel.getStyleClass().add("articleMeta");
        dateLabel.setStyle("-fx-font-size: 11px;");

        authorInfo.getChildren().addAll(authorName, dateLabel);
        authorBox.getChildren().addAll(miniAvatar, authorInfo);



        // Résumé
        Label summary = new Label(preview(safe(a.getContenu()), 100));
        summary.getStyleClass().add("articleSummary");
        summary.setWrapText(true);
        summary.setMaxHeight(50);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ===== LIGNE DES LIKES ET BOUTON LIRE =====
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_LEFT);

        // Coeur et compteur
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

        // Bouton Lire
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

    private void applyCover(ImageView iv, double targetW, double targetH) {
        Image img = iv.getImage();
        if (img == null || targetW <= 0 || targetH <= 0) return;

        double iw = img.getWidth();
        double ih = img.getHeight();
        if (iw <= 0 || ih <= 0) return;

        double scale = Math.max(targetW / iw, targetH / ih);
        double vw = targetW / scale;
        double vh = targetH / scale;

        double x = (iw - vw) / 2.0;
        double y = (ih - vh) / 2.0;

        iv.setViewport(new Rectangle2D(x, y, vw, vh));
        iv.setFitWidth(targetW);
        iv.setFitHeight(targetH);
    }
    private void openArticle(Conseil a) {

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        // ===================== ROOT =====================
        StackPane root = new StackPane();
        root.getStyleClass().add("articleModalRoot");

        // ===================== BACKGROUND (image floue) =====================
        ImageView bgImg = new ImageView();
        bgImg.getStyleClass().add("articleModalBg");
        bgImg.setPreserveRatio(false);
        bgImg.fitWidthProperty().bind(root.widthProperty());
        bgImg.fitHeightProperty().bind(root.heightProperty());

        Image img = loadLocalImage(a.getAuteurImage());
        if (img != null) {
            bgImg.setImage(img);
            // Flou (Gaussian)
            bgImg.setEffect(new javafx.scene.effect.GaussianBlur(28));
            bgImg.setOpacity(0.18); // pâle
        } else {
            bgImg.setVisible(false);
            bgImg.setManaged(false);
        }

        // ===================== OVERLAY (assombrit un peu) =====================
        Region overlay = new Region();
        overlay.getStyleClass().add("articleModalOverlay");

        // ===================== MODAL CARD =====================
        VBox modal = new VBox();
        modal.getStyleClass().add("articleModalCard");
        modal.setMaxWidth(920);
        modal.setPrefWidth(920);

        // ===================== HEADER =====================
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

        // ===================== META =====================
        Label meta = new Label("Catégorie : " + safe(a.getCategorie()) + "   •   Auteur : " + safe(a.getAuteur()));
        meta.getStyleClass().add("articleModalMeta");

        // ===================== CONTENT BOX (scroll propre) =====================
        Label contentLabel = new Label(safe(a.getContenu()));
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("articleModalContentText");

        VBox contentBox = new VBox(contentLabel);
        contentBox.getStyleClass().add("articleModalContentBox");

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("articleModalScroll");

        // ===================== FOOTER =====================
        Button btnFermer = new Button("Fermer");
        btnFermer.getStyleClass().add("articleModalCloseRed");
        btnFermer.setOnAction(e -> stage.close());

        HBox footer = new HBox(btnFermer);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("articleModalFooter");

        // assemble
        VBox body = new VBox(14, meta, scroll, footer);
        body.getStyleClass().add("articleModalBody");

        modal.getChildren().addAll(header, body);

        root.getChildren().addAll(bgImg, overlay, modal);

        Scene scene = new Scene(root, 980, 560);
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

    private void setActiveSubMenu(Button btn) {
        // retirer l'ancien actif
        if (activeSubBtn != null) {
            activeSubBtn.getStyleClass().remove("subMenuBtnActive");
        }

        // mettre le nouveau actif
        activeSubBtn = btn;
        if (!activeSubBtn.getStyleClass().contains("subMenuBtnActive")) {
            activeSubBtn.getStyleClass().add("subMenuBtnActive");
        }

        // garder Psychologie ouvert + actif
        tbPsychologie.setSelected(true);
        psychologieSubMenu.setVisible(true);
        psychologieSubMenu.setManaged(true);

        if (!tbPsychologie.getStyleClass().contains("sideBtnActive")) {
            tbPsychologie.getStyleClass().add("sideBtnActive");
        }
    }
    @FXML
    private void goArticlesConseils() {
        setActiveSubMenu(btnArticlesConseils);
        // أنت ديجا في الصفحة → ما تعمل حتى load
        refresh();
    }
    @FXML
    private void goSuivieTherapeutique() {
        setActiveSubMenu(btnSuivieTherapeutique);
        switchTo("/ParentSuivi.fxml");   // ✅ بدل root.setCenter
    }
    private Image loadLocalImage(String relativeOrAbsolutePath) {
        if (relativeOrAbsolutePath == null || relativeOrAbsolutePath.trim().isEmpty()) return null;

        try {
            String p = relativeOrAbsolutePath.trim();

            // JavaFX ne lit pas webp sans lib externe
            String lower = p.toLowerCase();
            if (lower.endsWith(".webp")) return null;

            // Si déjà URL
            if (p.startsWith("file:") || p.startsWith("http")) {
                return new Image(p, true);
            }

            File f = new File(p);

            // Si chemin relatif => essayer depuis le dossier du projet (user.dir)
            if (!f.exists()) {
                f = new File(System.getProperty("user.dir"), p);
            }

            if (!f.exists()) return null;

            return new Image(f.toURI().toString(), true);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateHeartVisual(ToggleButton btn, boolean liked) {
        btn.setText(liked ? "❤" : "♡");
        btn.getStyleClass().removeAll("heartOn", "heartOff");
        btn.getStyleClass().add(liked ? "heartOn" : "heartOff");
    }


    private Image loadLocalImageSmart(String path) {
        try {
            if (path == null || path.trim().isEmpty()) return null;

            String p = path.trim().replace("\\", "/");

            // JavaFX ne lit pas webp sans lib externe
            if (p.toLowerCase().endsWith(".webp")) return null;

            // 1) URL directe
            if (p.startsWith("file:") || p.startsWith("http")) {
                return new Image(p, true);
            }

            // 2) Chemin absolu ou relatif direct
            File f = new File(p);
            if (f.exists()) {
                return new Image(f.toURI().toString(), true);
            }

            // 3) user.dir + relatif
            f = new File(System.getProperty("user.dir"), p);
            if (f.exists()) {
                return new Image(f.toURI().toString(), true);
            }

            // 4) Dans resources
            String resPath = p.startsWith("/") ? p : "/" + p;
            var url = getClass().getResource(resPath);
            if (url != null) {
                return new Image(url.toExternalForm(), true);
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    // ===================== GESTION AMÉLIORÉE DES IMAGES =====================

    private Image loadAuthorImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return loadDefaultAuthorImage();
        }

        try {
            String path = imagePath.trim().replace("\\", "/");

            // 1. Vérifier si c'est une URL
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return new Image(path, true);
            }

            // 2. Vérifier dans les ressources
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            var resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm(), true);
            }

            // 3. Vérifier dans le dossier assets
            String assetsPath = "/assets/" + path;
            resourceUrl = getClass().getResource(assetsPath);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm(), true);
            }

            // 4. Vérifier dans le système de fichiers
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }

            // 5. Essayer dans le répertoire courant
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
            // Si pas d'image par défaut, créer une image colorée
            return createDefaultAvatar();
        } catch (Exception e) {
            return null;
        }
    }

    private Image createDefaultAvatar() {
        // Crée un avatar par défaut avec la première lettre du nom
        WritableImage img = new WritableImage(140, 140);
        // Tu peux laisser vide, l'image par défaut sera le gradient
        return null;
    }


}
