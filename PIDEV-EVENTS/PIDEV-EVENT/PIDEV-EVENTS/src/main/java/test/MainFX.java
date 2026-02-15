package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🔍 Looking for: /AjouterEvent.fxml");

            // THIS IS THE CRITICAL LINE
            java.net.URL fxmlUrl = getClass().getResource("/ShowEvent.fxml");
            System.out.println("📁 FXML URL: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("❌ CRITICAL: FXML file NOT FOUND!");
                System.err.println("   Place file at: src/main/resources/BackEventEvent.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            stage.setTitle("AutiCare - Événements");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ ShowEvent loaded successfully!");

        } catch (Exception e) {
            System.err.println("❌ ERROR LOADING FXML:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}