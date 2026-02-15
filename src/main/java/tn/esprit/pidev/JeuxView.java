package tn.esprit.pidev;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class JeuxView {

    public Parent getRoot() {

        Label h1 = new Label("JEUX");
        h1.getStyleClass().add("page-title");

        Label sub = new Label("Gérez les niveaux du jeu et le suivi total.");
        sub.getStyleClass().add("page-subtitle");

        Button btnNew = new Button("+ Nouvelle entrée");
        btnNew.getStyleClass().add("btn-primary");

        BorderPane header = new BorderPane();
        header.getStyleClass().add("page-top");

        VBox left = new VBox(4, h1, sub);
        header.setLeft(left);
        header.setRight(btnNew);
        BorderPane.setMargin(left, new Insets(0, 0, 10, 0));

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tabs");

        Tab tabNiveaux = new Tab("Niveaux de jeu");
        tabNiveaux.setClosable(false);
        tabNiveaux.setContent(new NiveauxPane().getRoot());

        Tab tabSuivi = new Tab("Suivi Total");
        tabSuivi.setClosable(false);
        tabSuivi.setContent(new SuiviTotalPane().getRoot());

        tabs.getTabs().addAll(tabNiveaux, tabSuivi);

        VBox card = new VBox(12, tabs);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));

        VBox page = new VBox(14, header, card);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(18));

        btnNew.setOnAction(e -> {
            // Optionnel: si tu veux “clear form” selon l’onglet actif, on peut l’ajouter après.
        });

        return page;
    }
}
