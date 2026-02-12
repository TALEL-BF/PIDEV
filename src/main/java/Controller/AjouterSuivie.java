package Controller;

import Entites.Suivie;
import Services.SuivieServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AjouterSuivie {

    @FXML private ScrollPane mainScroll;     // ✅ IMPORTANT: fx:id="mainScroll" dans FXML
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton tbTous, tbActif, tbTermine, tbAnnule;
    @FXML private Button btnAjouter;
    @FXML private VBox emptyBox;

    private final ToggleGroup statusGroup = new ToggleGroup();
    private final SuivieServices suivieService = new SuivieServices();
    private final ObservableList<Suivie> master = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // Toggle group
        tbTous.setToggleGroup(statusGroup);
        tbActif.setToggleGroup(statusGroup);
        tbTermine.setToggleGroup(statusGroup);
        tbAnnule.setToggleGroup(statusGroup);
        tbTous.setSelected(true);

        // ✅ Wrap responsive (PROPRE): NE PAS binder dans un listener
        // On se base sur la largeur visible du ScrollPane
        cardsPane.prefWrapLengthProperty().bind(
                mainScroll.widthProperty().subtract(90) // marge pour padding + scrollbar
        );

        // Load DB
        reloadFromDb();

        // Listeners (recherche + filtres)
        ChangeListener<Object> refreshListener = (obs, o, n) -> refreshCards();
        searchField.textProperty().addListener(refreshListener);
        statusGroup.selectedToggleProperty().addListener(refreshListener);

        // Bouton Ajouter
        btnAjouter.setOnAction(e -> onAjouter());

        // ✅ Se placer en haut au premier affichage (optionnel mais propre)
        Platform.runLater(() -> mainScroll.setVvalue(0));
    }

    private void reloadFromDb() {
        List<Suivie> list = suivieService.afficherSuivie();
        master.setAll(list);
        refreshCards();
    }

    private void refreshCards() {
        cardsPane.getChildren().clear();

        String q = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        String status = selectedStatus(); // null = Tous

        List<Suivie> filtered = master.stream()
                .filter(s -> matchesSearch(s, q))
                .filter(s -> status == null || safe(s.getStatut()).equalsIgnoreCase(status))
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

    private String selectedStatus() {
        Toggle t = statusGroup.getSelectedToggle();
        if (t == null || t == tbTous) return null;
        if (t == tbActif) return "Actif";
        if (t == tbTermine) return "Terminé";
        if (t == tbAnnule) return "Annulé";
        return null;
    }

    private Node buildCard(Suivie s) {

        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");

        // Band
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(s.getStatut()));

        Label name = new Label(safe(s.getNomEnfant()));
        name.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14;");

        Label role = new Label("Suivi");
        role.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11;");

        VBox nameBox = new VBox(2, name, role);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(safe(s.getStatut()));
        badge.getStyleClass().add("status-badge");

        band.getChildren().addAll(nameBox, spacer, badge);

        // Body
        VBox body = new VBox(10);
        body.getStyleClass().add("suivie-body");

        String date = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalDate().toString();
        String heure = (s.getDateSuivie() == null) ? "-" : s.getDateSuivie().toLocalDateTime().toLocalTime().toString();

        Label info = new Label(
                "PSY\n" + safe(s.getNomPsy()) +
                        "\n\nDATE\n" + date +
                        "\n\nHEURE\n" + heure +
                        "\n\nSCORES (H/S/A)\n" +
                        s.getScoreHumeur() + " / " + s.getScoreStress() + " / " + s.getScoreAttention() +
                        "\n\nREMARQUE\n" + safe(s.getObservation())
        );
        info.getStyleClass().add("suivie-info");

        // Actions
        HBox actions = new HBox(10);

        Button voir = new Button("Voir");
        voir.getStyleClass().add("btn-voir");
        voir.setMaxWidth(Double.MAX_VALUE);
        voir.setOnAction(e -> onVoir(s));

        Button edit = new Button("Éditer");
        edit.getStyleClass().add("btn-edit");
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setOnAction(e -> onEdit(s));

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().add("btn-delete");
        supprimer.setOnAction(e -> onSupprimer(s));

        HBox.setHgrow(voir, Priority.ALWAYS);
        HBox.setHgrow(edit, Priority.ALWAYS);

        actions.getChildren().addAll(voir, edit, supprimer);

        body.getChildren().addAll(info, actions);
        card.getChildren().addAll(band, body);

        return card;
    }

    // ======================
    // CRUD (à garder: tes méthodes existantes)
    // ======================
    private void onAjouter() {
        // TODO: ta logique existante
    }

    private void onEdit(Suivie s) {
        // TODO: ta logique existante
    }

    private void onVoir(Suivie s) {
        // TODO: ta logique existante
    }

    private void onSupprimer(Suivie s) {
        // TODO: ta logique existante
    }

    private String getBandStyle(String statut) {
        if (statut == null) return "-fx-background-color: linear-gradient(to right, #a78bfa, #f472b6);";
        String st = statut.trim().toLowerCase();
        return switch (st) {
            case "actif" -> "-fx-background-color: linear-gradient(to right, #60a5fa, #38bdf8);";
            case "terminé", "termine" -> "-fx-background-color: linear-gradient(to right, #34d399, #2dd4bf);";
            case "annulé", "annule" -> "-fx-background-color: linear-gradient(to right, #fb7185, #f97316);";
            default -> "-fx-background-color: linear-gradient(to right, #a78bfa, #f472b6);";
        };
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
