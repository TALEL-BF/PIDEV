package Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;

public class Navigation {

    private static Stage primaryStage;

    /**
     * Initialiser le stage principal
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Changer de page dans la même fenêtre
     */
    public static void navigateTo(String fxmlFile, String title) {
        try {
            // 🔴 CHANGEMENT ICI : ne pas ajouter "/fxml/" car vos fichiers sont à la racine
            String path = "/" + fxmlFile;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            System.out.println("🔍 Chargement de : " + path);

            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(path));

            // 🔴 VÉRIFICATION : si null, le fichier n'est pas trouvé
            if (Navigation.class.getResource(path) == null) {
                System.err.println("❌ Fichier FXML introuvable : " + path);
                return;
            }

            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutiCare - " + title);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Navigation vers : " + fxmlFile);

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation vers " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvrir une nouvelle fenêtre
     */
    public static void openNewWindow(String fxmlFile, String title) {
        try {
            String path = "/" + fxmlFile;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(path));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - " + title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Nouvelle fenêtre : " + fxmlFile);

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture fenêtre " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retour à la page précédente (optionnel)
     */
    public static void goBack() {
        System.out.println("⏪ Retour à la page précédente");
    }
}