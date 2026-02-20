package Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class GeminiImageService {



    private final OkHttpClient client;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public GeminiImageService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Génère une image avec Gemini via l'API REST
     */
    public byte[] genererImage(String prompt) {
        try {
            System.out.println("🎨 Génération d'image pour: \"" + prompt + "\"");

            // Utiliser le modèle Gemini 2.0 Flash (disponible gratuitement)
            String model = "gemini-2.0-flash-exp-image-generation";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + API_KEY;

            // Corps de la requête pour la génération d'image
            String jsonBody = String.format(
                    "{\n" +
                            "  \"contents\": [{\n" +
                            "    \"parts\": [{\n" +
                            "      \"text\": \"Generate an image of: %s. Style: simple children's drawing, colorful, cartoon style, white background, educational for kids\"\n" +
                            "    }]\n" +
                            "  }],\n" +
                            "  \"generationConfig\": {\n" +
                            "    \"temperature\": 1,\n" +
                            "    \"topK\": 40,\n" +
                            "    \"topP\": 0.95,\n" +
                            "    \"maxOutputTokens\": 8192,\n" +
                            "    \"responseModalities\": [\"image\", \"text\"]\n" +
                            "  }\n" +
                            "}", prompt);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, JSON))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    System.err.println("❌ Erreur API: " + response.code());
                    System.err.println("Réponse: " + errorBody);
                    return null;
                }

                String responseBody = response.body().string();
                return extractImageFromResponse(responseBody);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrait l'image de la réponse JSON
     */
    private byte[] extractImageFromResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Vérifier si la réponse contient des candidats
            if (jsonObject.has("candidates") && jsonObject.get("candidates").isJsonArray()) {
                var candidates = jsonObject.getAsJsonArray("candidates");

                if (candidates.size() > 0) {
                    var candidate = candidates.get(0).getAsJsonObject();

                    if (candidate.has("content") && candidate.get("content").isJsonObject()) {
                        var content = candidate.getAsJsonObject("content");

                        if (content.has("parts") && content.get("parts").isJsonArray()) {
                            var parts = content.getAsJsonArray("parts");

                            for (int i = 0; i < parts.size(); i++) {
                                var part = parts.get(i).getAsJsonObject();

                                // Chercher l'image dans les parties
                                if (part.has("inlineData") && part.get("inlineData").isJsonObject()) {
                                    var inlineData = part.getAsJsonObject("inlineData");

                                    if (inlineData.has("data") && inlineData.get("data").isJsonPrimitive()) {
                                        String base64Data = inlineData.get("data").getAsString();
                                        System.out.println("✅ Image trouvée dans la réponse");

                                        try {
                                            return Base64.getDecoder().decode(base64Data);
                                        } catch (IllegalArgumentException e) {
                                            System.err.println("❌ Erreur décodage base64: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Si on arrive ici, aucune image n'a été trouvée
            System.err.println("❌ Aucune image trouvée dans la réponse");
            System.err.println("Début de la réponse: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())));

        } catch (Exception e) {
            System.err.println("❌ Erreur extraction: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Test de connexion pour vérifier que la clé API fonctionne
     */
    public String testConnexion() {
        try {
            String model = "gemini-1.5-flash";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + API_KEY;

            String jsonBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\": [{\n" +
                    "      \"text\": \"Dis bonjour en français\"\n" +
                    "    }]\n" +
                    "  }]\n" +
                    "}";

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return "✅ Connexion réussie!";
                } else {
                    return "❌ Erreur: " + response.code();
                }
            }

        } catch (Exception e) {
            return "❌ Exception: " + e.getMessage();
        }
    }
}