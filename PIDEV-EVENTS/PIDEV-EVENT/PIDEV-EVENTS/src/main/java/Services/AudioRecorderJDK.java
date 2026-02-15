package Services;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

public class AudioRecorderJDK {

    private static final int SAMPLE_RATE = 16000;
    private static final int RECORD_TIME = 3000; // 3 secondes

    public static File enregistrer(String outputPath) throws Exception {
        // Format audio : 16kHz, 16 bits, mono
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone non supporté");
        }

        // Ouvrir le microphone
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        System.out.println("🎤 Enregistrement en cours... Parle pendant 3 secondes");

        // Enregistrer dans un buffer mémoire
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        long endTime = System.currentTimeMillis() + RECORD_TIME;

        while (System.currentTimeMillis() < endTime) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            out.write(buffer, 0, bytesRead);
        }

        // Arrêter
        line.stop();
        line.close();

        System.out.println("✅ Enregistrement terminé");

        // Convertir en fichier WAV
        byte[] audioData = out.toByteArray();
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(
                byteArrayInputStream, format, audioData.length / format.getFrameSize());

        File outputFile = new File(outputPath);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

        return outputFile;
    }

    // Test simple
    public static void main(String[] args) {
        try {
            System.out.println("🎤 Test d'enregistrement...");
            File audioFile = enregistrer("test.wav");
            System.out.println("✅ Fichier créé: " + audioFile.getAbsolutePath());
            System.out.println("📊 Taille: " + audioFile.length() + " bytes");
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}