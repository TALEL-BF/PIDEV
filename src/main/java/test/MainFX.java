package test;

import Utils.Navigation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Afficher le chemin pour déboguer
            System.out.println("=== INFORMATION DE DÉBOGAGE ===");
            System.out.println("Répertoire de travail : " + System.getProperty("user.dir"));
            System.out.println("Resource coursaffichage.fxml : " + getClass().getResource("/coursaffichage.fxml"));
            System.out.println("Resource coursajout.fxml : " + getClass().getResource("/coursajout.fxml"));
            System.out.println("================================");


            Navigation.setPrimaryStage(stage);


            Parent root = FXMLLoader.load(getClass().getResource("/coursaffichage.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AutiCare - Gestion des Cours");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();


            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de démarrer l'application");
            alert.setContentText("Vérifiez que les fichiers FXML sont dans src/main/resources/\n\n" +
                    "Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}