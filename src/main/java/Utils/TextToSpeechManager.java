package Utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class TextToSpeechManager {

    private static boolean isEnabled = true;
    private static boolean isLoading = false;

    public static void speak(String text) {
        if (!isEnabled || text == null || text.trim().isEmpty()) {
            return;
        }

        if (isLoading) {
            System.out.println("⏳ Génération audio déjà en cours...");
            return;
        }

        isLoading = true;
        final String textToSpeak = text.trim();

        CompletableFuture.supplyAsync(() -> {
            try {
                return OpenAITTS.generateSpeech(textToSpeak);
            } catch (Exception e) {
                System.err.println("❌ Erreur génération TTS: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).thenAccept(audioFile -> {
            Platform.runLater(() -> {
                isLoading = false;
                if (audioFile != null) {
                    AudioPlayer.play(audioFile);
                } else {
                    showError("Impossible de générer l'audio pour: " + textToSpeak);
                }
            });
        });
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
        System.out.println("🔊 TTS " + (enabled ? "activé" : "désactivé"));
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    private static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("TTS - Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message + "\n\nVérifiez votre clé API OpenAI dans config.properties");
            alert.showAndWait();
        });
    }
}