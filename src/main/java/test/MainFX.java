package test;

import Utils.Navigation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        Navigation.setPrimaryStage(stage);

        // 🔴 CHANGER ICI : démarrer sur l'affichage, pas sur l'ajout
        Parent root = FXMLLoader.load(getClass().getResource("/coursaffichage.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("AutiCare - Gestion des Cours");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}