package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AjouterSuivie {

    @FXML
    private FlowPane cardsPane;

    @FXML
    private Label countLabel;

    @FXML
    public void initialize() {
        addSuivieCard("Talel Ben Fradj", "03-05-2025", "20:00", "Actif", 8, "Calme aujourd’hui");
        addSuivieCard("Sahrine Jhan", "05-05-2025", "10:00", "Terminé", 5, "Stress léger");

        countLabel.setText(cardsPane.getChildren().size() + " Suivies");
    }

    private void addSuivieCard(
            String nom,
            String date,
            String heure,
            String statut,
            int score,
            String remarque
    ) {

        // Carte principale (sans spacing pour coller bande + body)
        VBox card = new VBox();
        card.getStyleClass().add("suivie-card");

        // ===== Bande colorée (comme le template) =====
        HBox band = new HBox(10);
        band.getStyleClass().add("suivie-band");
        band.setStyle(getBandStyle(statut)); // dégradé selon statut

        Label name = new Label(nom);
        name.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14;");

        Label role = new Label("Suivie"); // petit texte comme “Etudiant/Parent” dans le template
        role.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11;");

        VBox nameBox = new VBox(2, name, role);

        // Petit “badge” statut à droite (Actif/Terminé/Annulé)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(statut);
        badge.setStyle("""
            -fx-background-color: rgba(255,255,255,0.22);
            -fx-text-fill: white;
            -fx-padding: 4 10;
            -fx-background-radius: 999;
            -fx-font-weight: 700;
            -fx-font-size: 11;
        """);

        band.getChildren().addAll(nameBox, spacer, badge);

        // ===== Corps de la carte =====
        VBox body = new VBox(10);
        body.getStyleClass().add("suivie-body");

        Label info = new Label(
                "DATE\n" + date +
                        "\n\nHEURE\n" + heure +
                        "\n\nSCORE HUMEUR\n" + score +
                        "\n\nREMARQUE\n" + remarque
        );
        info.getStyleClass().add("suivie-info");

        // ===== Boutons bas (style template) =====
        HBox actions = new HBox(10);

        Button voir = new Button("Voir");
        voir.getStyleClass().add("btn-voir");
        voir.setMaxWidth(Double.MAX_VALUE);

        Button edit = new Button("Éditer");
        edit.getStyleClass().add("btn-edit");
        edit.setMaxWidth(Double.MAX_VALUE);

        // Si tu veux garder supprimer, on le met à droite, sinon tu peux le retirer
        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().add("btn-delete");

        HBox.setHgrow(voir, Priority.ALWAYS);
        HBox.setHgrow(edit, Priority.ALWAYS);

        actions.getChildren().addAll(voir, edit, supprimer);

        body.getChildren().addAll(info, actions);

        // Assemble
        card.getChildren().addAll(band, body);
        cardsPane.getChildren().add(card);
    }

    private String getBandStyle(String statut) {
        if (statut == null) return "-fx-background-color: linear-gradient(to right, #a78bfa, #f472b6);";

        String s = statut.trim().toLowerCase();
        return switch (s) {
            case "actif" ->
                    "-fx-background-color: linear-gradient(to right, #60a5fa, #38bdf8);";
            case "terminé", "termine" ->
                    "-fx-background-color: linear-gradient(to right, #34d399, #2dd4bf);";
            case "annulé", "annule" ->
                    "-fx-background-color: linear-gradient(to right, #fb7185, #f97316);";
            default ->
                    "-fx-background-color: linear-gradient(to right, #a78bfa, #f472b6);";
        };
    }
}
