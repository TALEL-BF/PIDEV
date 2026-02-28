package Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Navigation {

    private static Stage primaryStage;
    private static Map<String, String> parameters = new HashMap<>();


    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }


    public static void setParameters(Map<String, String> params) {
        parameters = params;
    }


    public static Map<String, String> getParameters() {
        return parameters;
    }


    public static void navigateTo(String fxmlFile, String title) {
        try {
            // Extraire les paramètres de l'URL si présents
            String fxmlPath = fxmlFile;
            Map<String, String> params = new HashMap<>();

            if (fxmlFile.contains("?")) {
                String[] parts = fxmlFile.split("\\?");
                fxmlPath = parts[0];

                if (parts.length > 1) {
                    String[] paramPairs = parts[1].split("&");
                    for (String pair : paramPairs) {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            params.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            }


            setParameters(params);

            String path = "/" + fxmlPath;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            System.out.println("🔍 Chargement de : " + path);
            System.out.println("📋 Paramètres : " + params);

            if (Navigation.class.getResource(path) == null) {
                System.err.println("❌ Fichier FXML introuvable : " + path);
                System.err.println("📁 Chemins recherchés :");
                System.err.println("   - " + path);
                return;
            }


            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(path));
            Parent root = loader.load();


            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutiCare - " + title);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Navigation vers : " + fxmlPath + " (" + title + ")");

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation vers " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de navigation",
                    "Impossible de charger la page : " + fxmlFile + "\n" + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("❌ Stage principal non initialisé !");
            System.err.println("Appelez Navigation.setPrimaryStage(stage) au démarrage.");
            e.printStackTrace();
        }
    }


    public static void openNewWindow(String fxmlFile, String title) {
        try {

            if (primaryStage == null) {
                System.err.println("❌ Stage principal non initialisé !");
                return;
            }


            String path = "/" + fxmlFile;
            if (!path.endsWith(".fxml")) {
                path += ".fxml";
            }

            System.out.println("🔍 Ouverture nouvelle fenêtre : " + path);

            if (Navigation.class.getResource(path) == null) {
                System.err.println("❌ Fichier FXML introuvable : " + path);
                return;
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

            System.out.println("✅ Nouvelle fenêtre ouverte : " + fxmlFile);

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture fenêtre " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir la fenêtre : " + fxmlFile + "\n" + e.getMessage());
        }
    }


    public static void goBack() {
        System.out.println("⏪ Retour à la page précédente");

    }


    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static boolean isPrimaryStageSet() {
        return primaryStage != null;
    }

    private static void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public static void refreshCurrentPage() {
        if (primaryStage != null) {
            String title = primaryStage.getTitle();
            String currentFxml = title.replace("AutiCare - ", "") + ".fxml";
            navigateTo(currentFxml, title);
        }
    }
}