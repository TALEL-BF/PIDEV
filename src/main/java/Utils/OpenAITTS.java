package Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OpenAITTS {

    private static final String AUDIO_CACHE_DIR = "audio_cache";

    static {
        File cacheDir = new File(AUDIO_CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public static File generateSpeech(String text) throws Exception {
        // Nettoyer le texte pour le nom de fichier
        String safeFileName = text.replaceAll("[^a-zA-Z0-9]", "_");
        if (safeFileName.length() > 50) {
            safeFileName = safeFileName.substring(0, 50);
        }

        File cachedFile = new File(AUDIO_CACHE_DIR, safeFileName + ".mp3");
        if (cachedFile.exists() && cachedFile.length() > 0) {
            System.out.println("🔊 Utilisation du cache audio pour: " + text);
            return cachedFile;
        }

        System.out.println("🔄 Génération audio gratuite pour: " + text);

        // Utiliser l'API Google Translate TTS (gratuite, pas de clé nécessaire)
        String encodedText = URLEncoder.encode(text, "UTF-8");
        String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&q=" + encodedText + "&tl=fr&client=tw-ob";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestMethod("GET");

        try (InputStream in = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(cachedFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("✅ Audio gratuit généré: " + cachedFile.getName());
        return cachedFile;
    }
}