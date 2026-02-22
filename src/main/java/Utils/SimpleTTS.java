package Utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class SimpleTTS {

    public static void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;


        java.awt.Toolkit.getDefaultToolkit().beep();


        System.out.println("🔊 [SimpleTTS] " + text);


        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("🔊 Message vocal");
            alert.setHeaderText("Texte prononcé :");
            alert.setContentText(text);

            alert.getDialogPane().setStyle(
                    "-fx-background-color: #F0E6FF;" +
                            "-fx-border-color: #7B2FF7;" +
                            "-fx-border-width: 3;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;"
            );


            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        if (alert.isShowing()) {
                            alert.close();
                        }
                    });
                } catch (InterruptedException e) {}
            }).start();

            alert.show();
        });
    }
}