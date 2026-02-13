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
            // Construire le chemin du fichier FXML
            String path = "/" + fxmlFile;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            System.out.println("🔍 Chargement de : " + path);

            // Vérifier si le fichier existe
            if (Navigation.class.getResource(path) == null) {
                System.err.println("❌ Fichier FXML introuvable : " + path);
                System.err.println("📁 Chemins recherchés :");
                System.err.println("   - " + path);
                System.err.println("   - /resources" + path);
                return;
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(path));
            Parent root = loader.load();

            // Créer et afficher la scène
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutiCare - " + title);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Navigation vers : " + fxmlFile + " (" + title + ")");

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation vers " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();

            // Afficher une alerte en cas d'erreur
            showErrorAlert("Erreur de navigation",
                    "Impossible de charger la page : " + fxmlFile + "\n" + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("❌ Stage principal non initialisé !");
            System.err.println("Appelez Navigation.setPrimaryStage(stage) au démarrage.");
            e.printStackTrace();
        }
    }

    /**
     * Ouvrir une nouvelle fenêtre (modale)
     */
    public static void openNewWindow(String fxmlFile, String title) {
        try {
            // Vérifier que le stage principal est initialisé
            if (primaryStage == null) {
                System.err.println("❌ Stage principal non initialisé !");
                return;
            }

            // Construire le chemin du fichier FXML
            String path = "/" + fxmlFile;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            System.out.println("🔍 Ouverture nouvelle fenêtre : " + path);

            // Vérifier si le fichier existe
            if (Navigation.class.getResource(path) == null) {
                System.err.println("❌ Fichier FXML introuvable : " + path);
                return;
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(path));
            Parent root = loader.load();

            // Créer et configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AutiCare - " + title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Nouvelle fenêtre ouverte : " + fxmlFile);

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture fenêtre " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();

            // Afficher une alerte en cas d'erreur
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir la fenêtre : " + fxmlFile + "\n" + e.getMessage());
        }
    }

    /**
     * Retour à la page précédente (optionnel - à implémenter avec un historique)
     */
    public static void goBack() {
        System.out.println("⏪ Retour à la page précédente");
        // TODO: Implémenter un historique de navigation
    }

    /**
     * Obtenir le stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Vérifier si le stage principal est initialisé
     */
    public static boolean isPrimaryStageSet() {
        return primaryStage != null;
    }

    /**
     * Afficher une alerte d'erreur
     */
    private static void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Rafraîchir la page actuelle
     */
    public static void refreshCurrentPage() {
        if (primaryStage != null) {
            String title = primaryStage.getTitle();
            String currentFxml = title.replace("AutiCare - ", "") + ".fxml";
            navigateTo(currentFxml, title);
        }
    }
}