package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class GeminiService {

    private static final String API_KEY = "AIzaSyDBMgL7JtQJpzyyQZHFnMqYkvTembMknHk";
    // ✅ Utilisation de l'API v1 avec gemini-2.5-flash
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public String genererRapport(String sujet) {
        try {
            String promptText = "Génère un rapport sur le thème '" + sujet + "' en français avec cette structure exacte:\n\n" +
                    "📌 DÉFINITION\n" +
                    "Une définition simple et claire.\n\n" +
                    "✅ AVANTAGES\n" +
                    "• Avantage 1\n" +
                    "• Avantage 2\n" +
                    "• Avantage 3\n\n" +
                    "❌ INCONVÉNIENTS\n" +
                    "• Inconvénient 1\n" +
                    "• Inconvénient 2\n" +
                    "• Inconvénient 3\n\n" +
                    "💡 CONSEILS\n" +
                    "• Conseil 1\n" +
                    "• Conseil 2\n" +
                    "• Conseil 3";

            // Format correct pour l'API v1 avec generateContent
            String requestBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\n" +
                    "          \"text\": \"" + promptText.replace("\"", "\\\"") + "\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            System.out.println("🔍 Envoi de la requête à Gemini 2.5 Flash...");
            System.out.println("📤 URL: " + API_URL);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(API_URL);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            client.close();

            System.out.println("📥 Réponse reçue");

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            if (jsonResponse.has("error")) {
                String errorMsg = jsonResponse.getAsJsonObject("error").get("message").getAsString();
                System.err.println("❌ Erreur API: " + errorMsg);
                return "Erreur API: " + errorMsg;
            }

            // Extraction du texte de la réponse
            String rapport = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            return rapport;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        GeminiService service = new GeminiService();
        String rapport = service.genererRapport("stress");
        System.out.println("\n📊 RAPPORT :\n");
        System.out.println(rapport);
    }
    public String genererDescription(String prompt) {
        try {
            String requestBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\n" +
                    "          \"text\": \"" + prompt.replace("\"", "\\\"") + "\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(API_URL);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            client.close();

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            if (jsonResponse.has("error")) {
                String errorMsg = jsonResponse.getAsJsonObject("error").get("message").getAsString();
                return "Erreur API: " + errorMsg;
            }

            String resultat = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            // ✅ NETTOYER LA RÉPONSE
            resultat = nettoyerReponse(resultat);

            return resultat;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur: " + e.getMessage();
        }
    }

    // ✅ Nouvelle méthode pour nettoyer la réponse
    private String nettoyerReponse(String texte) {
        // Supprimer les phrases d'introduction courantes
        texte = texte.replaceAll("(?i)voici\\s+une\\s+description\\s*:?\\s*", "");
        texte = texte.replaceAll("(?i)d'accord\\s*,?\\s*voici\\s*:?\\s*", "");
        texte = texte.replaceAll("(?i)je\\s+vous\\s+propose\\s*:?\\s*", "");
        texte = texte.replaceAll("(?i)voici\\s+ce\\s+que\\s+je\\s+peux\\s+vous\\s+proposer\\s*:?\\s*", "");
        texte = texte.replaceAll("(?i)bien sûr\\s*,?\\s*voici\\s*:?\\s*", "");
        texte = texte.replaceAll("(?i)certainement\\s*,?\\s*voici\\s*:?\\s*", "");
        texte = texte.replaceAll("^\\s*:\\s*", ""); // Supprime les deux points au début

        // Supprimer les symboles
        texte = texte.replaceAll("\\*\\*", "");
        texte = texte.replaceAll("\\*", "");
        texte = texte.replaceAll("_", "");
        texte = texte.replaceAll("#", "");
        texte = texte.replaceAll("•", "");
        texte = texte.replaceAll("✓", "");
        texte = texte.replaceAll("✅", "");
        texte = texte.replaceAll("❌", "");
        texte = texte.replaceAll("📌", "");
        texte = texte.replaceAll("💡", "");

        // Nettoyer les espaces
        texte = texte.replaceAll("\\s+", " ").trim();

        return texte;
    }
}