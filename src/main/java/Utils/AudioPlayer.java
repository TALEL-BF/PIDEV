package Utils;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class AudioPlayer {

    private static Player currentPlayer;
    private static Thread playerThread;

    public static void play(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            System.err.println("❌ Fichier audio non trouvé: " + audioFile);
            return;
        }

        // Arrêter la lecture en cours
        stop();

        // Lire dans un thread séparé
        playerThread = new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(audioFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                currentPlayer = new Player(bis);
                System.out.println("🔊 Lecture: " + audioFile.getName());
                currentPlayer.play();
            } catch (Exception e) {
                System.err.println("❌ Erreur lecture audio: " + e.getMessage());
            }
        });

        playerThread.setDaemon(true); // Permet à l'application de se fermer même si le thread tourne
        playerThread.start();
    }

    public static void stop() {
        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
            playerThread = null;
        }
    }
}