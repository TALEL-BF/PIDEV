package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/AjouterSuivie.fxml"),
                        "❌ FXML introuvable : /AjouterSuivie.fxml (vérifie src/main/resources)")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        // ✅ CSS (sécurité, même si déjà dans FXML)
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/app.css"),
                        "❌ CSS introuvable : /styles/app.css").toExternalForm()
        );

        stage.setTitle("Gestion des Suivies");
        stage.setWidth(1365);
        stage.setHeight(768);
        stage.setResizable(true);
        stage.centerOnScreen();

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

