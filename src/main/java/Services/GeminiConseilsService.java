package Services;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GeminiConseilsService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String model = "gemini-2.5-flash";

    public String genererConseils(String prompt) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY est vide. Configurez la variable d'environnement.");
        }

        String url = "https://generativelanguage.googleapis.com/v1/models/"
                + model + ":generateContent?key=" + apiKey;

        // ✅ CORRECTION : Augmentation drastique de maxOutputTokens pour permettre les plans de 7 jours
        String json = """
        {
          "contents": [{
            "parts": [{"text": %s}]
          }],
          "generationConfig": {
            "temperature": 0.4,
            "maxOutputTokens": 4096
          }
        }
        """.formatted(toJsonString(prompt));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (resp.statusCode() >= 300) {
            throw new RuntimeException("Gemini API error " + resp.statusCode() + " : " + resp.body());
        }

        return extractText(resp.body());
    }

    // ✅ CORRECTION : Une méthode d'extraction beaucoup plus stable et simple
    private static String extractText(String body) {
        int textIndex = body.indexOf("\"text\":");
        if (textIndex < 0) return body; // Retourne le JSON brut en cas d'erreur inattendue

        // Trouve le premier guillemet après "text":
        int startQuote = body.indexOf("\"", textIndex + 7);
        if (startQuote < 0) return body;

        // Cherche le guillemet de fin qui N'EST PAS échappé par un backslash
        int endQuote = startQuote + 1;
        while (endQuote < body.length()) {
            if (body.charAt(endQuote) == '"' && body.charAt(endQuote - 1) != '\\') {
                break;
            }
            endQuote++;
        }

        if (endQuote >= body.length()) return body;

        String rawText = body.substring(startQuote + 1, endQuote);

        // Nettoyage des échappements JSON standards
        return rawText.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\t", "\t")
                .replace("\\r", "")
                .replace("\\\\", "\\");
    }

    private static String toJsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "") + "\"";
    }
}