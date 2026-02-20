package Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FalImageService {

    private static final String API_URL = "https://fal.run/fal-ai/flux/dev";
    private final OkHttpClient client;
    private final String apiKey;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public FalImageService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String genererImage(String prompt) {
        try {
            System.out.println("🎨 Fal.ai - Génération pour: \"" + prompt + "\"");

            JsonObject input = new JsonObject();
            input.addProperty("prompt", prompt + ", children's drawing style, cartoon, colorful, simple, white background");
            input.addProperty("image_size", "square_hd");

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(input.toString(), JSON))
                    .addHeader("Authorization", "Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("❌ Erreur API: " + response.code());
                    return null;
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonResponse.has("images")) {
                    String imageUrl = jsonResponse.getAsJsonArray("images")
                            .get(0).getAsJsonObject()
                            .get("url").getAsString();
                    System.out.println("✅ Image générée: " + imageUrl);
                    return imageUrl;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}