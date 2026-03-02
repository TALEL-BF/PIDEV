package com.auticare.Controller;

import com.auticare.Entites.Conseil;
import com.auticare.Services.ConseilServices;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionArticlesBackController {

    @FXML private BorderPane root;               // nécessaire pour switchTo
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;
    @FXML private Button btnAjouterFab;
    @FXML private Button btnFrontOfficeFab;

    private final ObservableList<Conseil> master = FXCollections.observableArrayList();
    private ConseilServices conseilService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final long MAX_IMG_BYTES = 2L * 1024 * 1024; // 2 Mo

    @FXML
    public void initialize() {
        conseilService = new ConseilServices();

        // Recherche
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> refreshCards());
        }

        // Boutons d'ajout
        if (btnAjouter != null) btnAjouter.setOnAction(e -> onAjouter());
        if (btnAjouterFab != null) btnAjouterFab.setOnAction(e -> onAjouter());

        // Bouton front office
        if (btnFrontOfficeFab != null) btnFrontOfficeFab.setOnAction(e -> switchTo("/MainArticles.fxml"));

        // Ajustement responsive du FlowPane
        if (cardsPane != null) {
            cardsPane.widthProperty().addListener((obs, oldV, newV) -> {
                double w = newV.doubleValue();
                cardsPane.setPrefWrapLength(Math.max(500, w - 40));
            });
        }

        reloadFromDb();
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

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

    // ==================== CARTE D'UN ARTICLE ====================

    private Node buildCard(Conseil c) {
        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");

        // Bandeau supérieur avec avatar
        HBox band = new HBox(12);
        band.setAlignment(Pos.CENTER_LEFT);
        band.getStyleClass().add("suivie-band");

        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        Image img = loadLocalImage(c.getAuteurImage());
        if (img != null) avatar.setImage(img);
        // Rendre l'avatar rond
        double r = 20;
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(r, r, r);
        avatar.setClip(clip);
        avatar.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.2, 0, 2);");
        avatar.getStyleClass().add("avatarRound");

        Label title = new Label(safe(c.getTitre()));
        title.getStyleClass().add("card-title");

        Label sub = new Label(safe(c.getCategorie()));
        sub.getStyleClass().add("card-subtitle");

        VBox nameBox = new VBox(2, title, sub);
        nameBox.getStyleClass().add("name-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        band.getChildren().addAll(avatar, nameBox, spacer);

        // Ligne de séparation
        Region headerLine = new Region();
        headerLine.getStyleClass().add("card-header-line");

        // Corps de la carte
        VBox body = new VBox(12);
        body.getStyleClass().add("suivie-body");

        String metaTxt = "AUTEUR\n" + safe(c.getAuteur())
                + "\n\nDATE\n" + formatDate(c.getDateCreation())
                + "\n\nCONTENU\n" + previewText(c.getContenu(), 160);

        Label info = new Label(metaTxt);
        info.setWrapText(true);
        info.getStyleClass().add("suivie-info");

        // Boutons d'action
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

    // ==================== ACTIONS CRUD ====================

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

    // ==================== FENÊTRE AJOUT/MODIFICATION ====================

    private Optional<Conseil> showConseilWindow(Conseil initial, String title) {
        Stage stage = createCustomStage(title, 980, 560);
        VBox content = makeContentBox();

        Label lblErrors = new Label("");
        lblErrors.getStyleClass().add("form-errors");
        lblErrors.setWrapText(true);

        TextField tfTitre  = new TextField(initial == null ? "" : safe(initial.getTitre()));
        TextField tfAuteur = new TextField(initial == null ? "" : safe(initial.getAuteur()));

        // Image auteur
        final String[] imagePath = { initial == null ? null : initial.getAuteurImage() };

        ImageView avatarPreview = new ImageView();
        avatarPreview.setFitWidth(64);
        avatarPreview.setFitHeight(64);
        avatarPreview.setPreserveRatio(true);
        avatarPreview.getStyleClass().add("avatarPreview");

        Image initImg = loadLocalImage(imagePath[0]);
        if (initImg != null) avatarPreview.setImage(initImg);

        Button btnChooseImg = new Button("Choisir image");
        btnChooseImg.getStyleClass().addAll("btn-soft", "btn-hover");

        Label imgInfo = new Label(imagePath[0] == null ? "Aucune image" : imagePath[0]);
        imgInfo.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: 600;");

        VBox imgBox = new VBox(6, btnChooseImg, imgInfo);
        HBox imgRow = new HBox(12, avatarPreview, imgBox);
        imgRow.setAlignment(Pos.CENTER_LEFT);
        imgRow.getStyleClass().add("img-row");

        // Catégorie
        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("Général", "Autisme", "Sommeil", "Comportement");
        if (initial == null) {
            cbCat.setPromptText("Choisir...");
            cbCat.setValue(null);
        } else {
            cbCat.setValue(safe(initial.getCategorie()));
        }

        // Contenu
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

        gp.add(new Label("Image auteur *"), 0, 3);
        gp.add(imgRow, 1, 3);

        gp.add(new Label("Contenu *"), 0, 4);
        gp.add(taContenu, 1, 4);

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
        final boolean[] catTouched = { initial != null };

        // Validation
        Runnable validate = () -> {
            lblErrors.setText("");
            tfTitre.getStyleClass().removeAll("field-error", "field-ok");
            tfAuteur.getStyleClass().removeAll("field-error", "field-ok");
            taContenu.getStyleClass().removeAll("field-error", "field-ok");
            cbCat.getStyleClass().removeAll("field-error", "field-ok");
            imgRow.getStyleClass().removeAll("field-error", "field-ok");

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

            // Catégorie
            boolean catOk = cbCat.getValue() != null && !cbCat.getValue().trim().isEmpty();
            if (!catTouched[0] || !catOk) {
                ok = false;
                sb.append("- Catégorie (obligatoire)\n");
                cbCat.getStyleClass().add("field-error");
            } else {
                cbCat.getStyleClass().add("field-ok");
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

            // Image obligatoire
            boolean imgOk = (imagePath[0] != null && !imagePath[0].trim().isEmpty());
            if (!imgOk) {
                ok = false;
                sb.append("- Image auteur (obligatoire)\n");
                imgRow.getStyleClass().add("field-error");
            } else {
                imgRow.getStyleClass().add("field-ok");
            }

            btnSave.setDisable(!ok);
            if (!ok) lblErrors.setText(sb.toString());
        };

        // Choix image
        btnChooseImg.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image");
            fc.getExtensionFilters().setAll(
                    new FileChooser.ExtensionFilter("Images (PNG/JPG)", "*.png", "*.jpg", "*.jpeg")
            );

            File chosen = fc.showOpenDialog(stage);
            if (chosen == null) return;

            String name = chosen.getName().toLowerCase();
            boolean extOk = name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
            if (!extOk) {
                lblErrors.setText("Champs à corriger :\n- Image auteur (formats acceptés: png, jpg, jpeg)\n");
                imagePath[0] = null;
                imgInfo.setText("Aucune image");
                avatarPreview.setImage(null);
                validate.run();
                return;
            }

            if (chosen.length() > MAX_IMG_BYTES) {
                lblErrors.setText("Champs à corriger :\n- Image auteur (max 2 Mo)\n");
                imagePath[0] = null;
                imgInfo.setText("Aucune image");
                avatarPreview.setImage(null);
                validate.run();
                return;
            }

            String saved = saveImageToUploads(chosen);
            imagePath[0] = saved;
            imgInfo.setText(saved);

            Image img = loadLocalImage(saved);
            if (img != null) avatarPreview.setImage(img);

            validate.run();
        });

        // Listeners
        tfTitre.textProperty().addListener((o,a,b) -> validate.run());
        tfAuteur.textProperty().addListener((o,a,b) -> validate.run());
        taContenu.textProperty().addListener((o,a,b) -> validate.run());
        cbCat.valueProperty().addListener((o, a, b) -> {
            catTouched[0] = true;
            validate.run();
        });

        validate.run();

        btnSave.setOnAction(e -> {
            validate.run();
            if (btnSave.isDisable()) return;

            result[0] = new Conseil(
                    tfTitre.getText().trim(),
                    taContenu.getText().trim(),
                    cbCat.getValue(),
                    tfAuteur.getText().trim(),
                    imagePath[0]
            );
            stage.close();
        });

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // ==================== MÉTHODES DE NAVIGATION (AJOUTÉES) ====================

    @FXML
    private void showDashboard() {
        switchTo("/views/PsychologueDashboard.fxml");
    }

    @FXML
    private void goToProfile() {
        switchTo("/views/PsychologueProfile.fxml");
    }

    @FXML
    private void showSchedule() {
        showInfo("Emploi du temps", "Fonctionnalité à venir");
    }

    @FXML
    private void showAppointments() {
        showInfo("Rendez-vous", "Fonctionnalité à venir");
    }

    @FXML
    private void showTherapeuticFollowUp() {
        switchTo("/AjouterSuivie.fxml");
    }

    @FXML
    private void showTherapies() {
        switchTo("/AjouterTherapie.fxml");
    }

    @FXML
    private void showArticles() {
        // déjà sur la page
    }

    @FXML
    private void showEvents() {
        showInfo("Événements", "Fonctionnalité à venir");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        com.auticare.utils.SessionManager.getInstance().endSession();
        switchTo("/Login.fxml");
    }

    // ==================== UTILITAIRES ====================

    private void switchTo(String fxmlPath) {
        try {
            java.net.URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IllegalArgumentException("FXML introuvable: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(newRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Navigation", "Impossible de charger : " + fxmlPath + "\n" + ex.getMessage());
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

    private String previewText(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String formatDate(Timestamp ts) {
        if (ts == null) return "";
        return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DF);
    }

    private String saveImageToUploads(File chosenFile) {
        if (chosenFile == null) return null;
        try {
            Path uploadsDir = Path.of("uploads", "articles");
            Files.createDirectories(uploadsDir);

            String name = chosenFile.getName();
            String ext = "";
            int dot = name.lastIndexOf('.');
            if (dot >= 0) ext = name.substring(dot);

            String newName = System.currentTimeMillis() + "_avatar" + ext;
            Path target = uploadsDir.resolve(newName);

            Files.copy(chosenFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "uploads/articles/" + newName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde image: " + e.getMessage(), e);
        }
    }

    private Image loadLocalImage(String relativeOrAbsolutePath) {
        if (relativeOrAbsolutePath == null || relativeOrAbsolutePath.trim().isEmpty()) return null;
        try {
            if (relativeOrAbsolutePath.startsWith("file:") || relativeOrAbsolutePath.startsWith("http")) {
                return new Image(relativeOrAbsolutePath, true);
            }
            String lower = relativeOrAbsolutePath.toLowerCase();
            if (lower.endsWith(".webp")) return null;

            File f = new File(relativeOrAbsolutePath);
            if (!f.exists()) {
                f = new File(System.getProperty("user.dir"), relativeOrAbsolutePath);
            }
            if (!f.exists()) return null;
            return new Image(f.toURI().toString(), true);
        } catch (Exception e) {
            return null;
        }
    }
}