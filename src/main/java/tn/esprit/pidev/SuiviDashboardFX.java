package tn.esprit.pidev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class SuiviDashboardFX extends Application {

    @Override
    public void start(Stage stage) {

        NiveauxPane niveauxPane = new NiveauxPane();
        SuiviTotalPane suiviPane = new SuiviTotalPane();

        TabPane tabs = new TabPane();

        Tab tabNiveaux = new Tab("Niveaux de jeu");
        tabNiveaux.setClosable(false);
        tabNiveaux.setContent(niveauxPane.getRoot());

        Tab tabSuivi = new Tab("Suivi Total");
        tabSuivi.setClosable(false);
        tabSuivi.setContent(suiviPane.getRoot());

        tabs.getTabs().addAll(tabNiveaux, tabSuivi);

        Scene scene = new Scene(tabs, 1000, 650);
        stage.setTitle("Suivi Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
