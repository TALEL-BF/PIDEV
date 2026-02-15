package tn.esprit.pidev;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFX extends Application {

    private BorderPane root;
    private VBox sidebar;

    private final Map<Button, Runnable> nav = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {

        root = new BorderPane();
        root.getStyleClass().add("root");

        // -------- Sidebar --------
        sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(280);

        Label brand = new Label("AutiCare");
        brand.getStyleClass().add("brand");

        Label sub = new Label("Plateforme éducative");
        sub.getStyleClass().add("brand-sub");

        VBox header = new VBox(2, brand, sub);
        header.getStyleClass().add("sidebar-header");
        header.setPadding(new Insets(8, 0, 10, 0));

        Button btnUsers   = menuBtn("👥  Gestion des utilisateurs");
        Button btnCours   = menuBtn("📘  Gestion des cours");
        Button btnEval    = menuBtn("📝  Gestion des évaluations");
        Button btnConsult = menuBtn("💬  Gestion des consultations");
        Button btnEvents  = menuBtn("📅  Gestion des événements");
        Button btnReclam  = menuBtn("⚠️  Gestion des réclamations");
        Button btnJeux    = menuBtn("🎮  JEUX");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                header,
                new Separator(),
                btnUsers, btnCours, btnEval, btnConsult, btnEvents, btnReclam,
                new Separator(),
                btnJeux,
                spacer
        );

        root.setLeft(sidebar);

        // -------- Pages --------
        nav.put(btnUsers,   () -> root.setCenter(staticPage("Gestion des utilisateurs")));
        nav.put(btnCours,   () -> root.setCenter(staticPage("Gestion des cours")));
        nav.put(btnEval,    () -> root.setCenter(staticPage("Gestion des évaluations")));
        nav.put(btnConsult, () -> root.setCenter(staticPage("Gestion des consultations")));
        nav.put(btnEvents,  () -> root.setCenter(staticPage("Gestion des événements")));
        nav.put(btnReclam,  () -> root.setCenter(staticPage("Gestion des réclamations")));
        nav.put(btnJeux,    () -> root.setCenter(new JeuxView().getRoot()));

        nav.forEach((btn, action) -> btn.setOnAction(e -> {
            setActive(btn);
            action.run();
        }));

        // Default
        setActive(btnJeux);
        root.setCenter(new JeuxView().getRoot());

        Scene scene = new Scene(root, 1300, 740);

        URL css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("⚠️ app.css introuvable. Mets-le dans: src/main/resources/app.css");
        }

        stage.setTitle("Dashboard - PIDEV Desktop");
        stage.setScene(scene);
        stage.show();
    }

    private Button menuBtn(String text) {
        Button b = new Button(text);
        // IMPORTANT: on utilise le naming du CSS de ton ami
        b.getStyleClass().add("menu-item");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    private void setActive(Button active) {
        for (var n : sidebar.getChildren()) {
            if (n instanceof Button b) {
                b.getStyleClass().remove("active");
            }
        }
        active.getStyleClass().add("active");
    }

    private Pane staticPage(String title) {
        Label h1 = new Label(title);
        h1.getStyleClass().add("h1");

        Label s = new Label("Page statique pour l’instant.");
        s.getStyleClass().add("sub");

        VBox box = new VBox(10, h1, s);
        box.getStyleClass().add("content");
        box.setPadding(new Insets(22));
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
