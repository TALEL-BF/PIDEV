package Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class StabilityImageService {

    private static final String API_URL = "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image";
    private final OkHttpClient client;
    private final String apiKey;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public StabilityImageService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }


    public byte[] genererImage(String prompt) {
        try {
            System.out.println("🎨 Stability AI - Génération pour: \"" + prompt + "\"");


            JsonObject input = new JsonObject();
            input.addProperty("text_prompts[0].text", prompt + ", children's drawing style, cartoon, colorful, simple, white background, educational for kids");
            input.addProperty("cfg_scale", 7);
            input.addProperty("height", 1024);
            input.addProperty("width", 1024);
            input.addProperty("samples", 1);
            input.addProperty("steps", 30);


            JsonObject payload = new JsonObject();
            JsonObject textPrompt = new JsonObject();
            textPrompt.addProperty("text", prompt + ", children's drawing style, cartoon, colorful, simple, white background");
            textPrompt.addProperty("weight", 1);

            payload.add("text_prompts", JsonParser.parseString("[{\"text\":\"" + prompt + ", children's drawing style, cartoon, colorful, simple, white background\",\"weight\":1}]").getAsJsonArray());
            payload.addProperty("cfg_scale", 7);
            payload.addProperty("height", 1024);
            payload.addProperty("width", 1024);
            payload.addProperty("samples", 1);
            payload.addProperty("steps", 30);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(payload.toString(), JSON))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            System.out.println("🔑 En-tête Authorization: Bearer " + apiKey.substring(0, Math.min(5, apiKey.length())) + "...");

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    System.err.println("❌ Erreur API: " + response.code());
                    System.err.println("Détails: " + errorBody);
                    return null;
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();


                if (jsonResponse.has("artifacts") && jsonResponse.getAsJsonArray("artifacts").size() > 0) {
                    String base64Image = jsonResponse.getAsJsonArray("artifacts")
                            .get(0).getAsJsonObject()
                            .get("base64").getAsString();

                    System.out.println("✅ Image générée avec succès");
                    return Base64.getDecoder().decode(base64Image);
                } else {
                    System.err.println("❌ Pas d'image dans la réponse");
                    System.err.println("Réponse: " + responseBody);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur réseau: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public byte[] genererImageRapide(String prompt) {
        try {
            String url = "https://api.stability.ai/v1/generation/stable-diffusion-v1-6/text-to-image";

            JsonObject payload = new JsonObject();
            payload.addProperty("text_prompts[0].text", prompt + ", cartoon, colorful");
            payload.addProperty("cfg_scale", 7);
            payload.addProperty("height", 512);
            payload.addProperty("width", 512);
            payload.addProperty("samples", 1);
            payload.addProperty("steps", 20);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(payload.toString(), JSON))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (jsonResponse.has("artifacts") && jsonResponse.getAsJsonArray("artifacts").size() > 0) {
                        String base64Image = jsonResponse.getAsJsonArray("artifacts")
                                .get(0).getAsJsonObject()
                                .get("base64").getAsString();
                        return Base64.getDecoder().decode(base64Image);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return null;
    }
}