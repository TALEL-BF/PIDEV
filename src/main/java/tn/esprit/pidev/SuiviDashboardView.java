package tn.esprit.pidev;

import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SuiviDashboardView {

    public Parent getRoot() {

        TabPane tabs = new TabPane();

        Tab tabNiveaux = new Tab("Niveaux de jeu");
        tabNiveaux.setClosable(false);
        tabNiveaux.setContent(new NiveauxPane().getRoot());

        Tab tabSuivi = new Tab("Suivi Total");
        tabSuivi.setClosable(false);
        tabSuivi.setContent(new SuiviTotalPane().getRoot()); // ✅ ici

        tabs.getTabs().addAll(tabNiveaux, tabSuivi);

        return tabs;
    }
}
