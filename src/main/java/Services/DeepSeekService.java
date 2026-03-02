package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepSeekService {



    private final HttpClient client;
    private final ObjectMapper mapper;

    public DeepSeekService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }


    public List<GeneratedQuestion> genererQuestions(String titreCours) {
        List<GeneratedQuestion> questions = new ArrayList<>();

        try {
            System.out.println("🤖 Génération IA pour: \"" + titreCours + "\"");

            // Prompt qui force l'IA à ne répondre qu'en JSON
            String prompt = "Tu es un professeur pour enfants. " +
                    "Génère 5 questions à choix multiple sur le thème: '" + titreCours + "'.\n\n" +
                    "RÈGLES IMPÉRATIVES:\n" +
                    "- Réponds UNIQUEMENT avec du JSON valide\n" +
                    "- AUCUN texte avant ou après le JSON\n" +
                    "- Les questions doivent être simples pour des enfants\n" +
                    "- 3 choix par question (A, B, C)\n" +
                    "- La bonne réponse doit être l'un des 3 choix\n" +
                    "- Chaque question vaut 1 point\n\n" +
                    "Format JSON EXACT:\n" +
                    "{\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"question\": \"La question avec ?\",\n" +
                    "      \"choix1\": \"Option 1\",\n" +
                    "      \"choix2\": \"Option 2\",\n" +
                    "      \"choix3\": \"Option 3\",\n" +
                    "      \"bonne_reponse\": \"Option 1\",\n" +
                    "      \"score\": 1\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "Génère maintenant 5 questions pour \"" + titreCours + "\" en JSON UNIQUEMENT.";

            String requestBody = mapper.writeValueAsString(new ApiRequest(
                    MODEL,
                    List.of(new Message("user", prompt)),
                    0.7,
                    2000
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                questions = parserReponse(response.body());
                System.out.println("✅ " + questions.size() + " questions générées par l'IA");
            } else {
                System.err.println("❌ Erreur API: " + response.statusCode());
                System.err.println(response.body());
                // En cas d'erreur, on relance une tentative avec un modèle différent
                questions = retryAvecAutreModele(titreCours);
            }

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            questions = retryAvecAutreModele(titreCours);
        }

        return questions;
    }

    private List<GeneratedQuestion> retryAvecAutreModele(String titreCours) {
        try {
            System.out.println("🔄 Tentative avec modèle Mixtral...");

            String prompt = "Generate 5 multiple choice questions for children about: " + titreCours +
                    ". Return ONLY valid JSON with format: {\"questions\":[{\"question\":\"...?\",\"choix1\":\"...\",\"choix2\":\"...\",\"choix3\":\"...\",\"bonne_reponse\":\"...\",\"score\":1}]}";

            String requestBody = mapper.writeValueAsString(new ApiRequest(
                    "mixtral-8x7b-32768", // Second modèle
                    List.of(new Message("user", prompt)),
                    0.7,
                    2000
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parserReponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Échec seconde tentative: " + e.getMessage());
        }

        // Dernier recours - on laisse l'IA générer mais on extrait le JSON
        return forceGeneration(titreCours);
    }


    private List<GeneratedQuestion> forceGeneration(String titreCours) {
        try {
            String prompt = "Génère 5 questions sur: " + titreCours +
                    ". Réponds en JSON. Ne mets rien d'autre que le JSON.";

            String requestBody = mapper.writeValueAsString(new ApiRequest(
                    "gemma2-9b-it", // Troisième modèle
                    List.of(new Message("user", prompt)),
                    0.7,
                    2000
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parserReponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Échec final: " + e.getMessage());
        }

        // Si tout échoue, on renvoie une liste vide (l'interface gérera)
        return new ArrayList<>();
    }


    private List<GeneratedQuestion> parserReponse(String jsonResponse) {
        List<GeneratedQuestion> questions = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonResponse);
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            System.out.println("📝 Réponse IA reçue");

            // Extraction du JSON
            String jsonStr = extraireJSON(content);

            if (jsonStr != null) {
                JsonNode questionsNode = mapper.readTree(jsonStr).path("questions");

                if (questionsNode.isArray()) {
                    for (JsonNode q : questionsNode) {
                        String question = q.path("question").asText();
                        String choix1 = q.path("choix1").asText();
                        String choix2 = q.path("choix2").asText();
                        String choix3 = q.path("choix3").asText();
                        String bonneReponse = q.path("bonne_reponse").asText();
                        int score = q.path("score").asInt(1);


                        question = question.replaceAll("^\\d+\\.\\s*", "");

                        if (!question.isEmpty() && !choix1.isEmpty() &&
                                !choix2.isEmpty() && !choix3.isEmpty()) {

                            questions.add(new GeneratedQuestion(
                                    question, choix1, choix2, choix3, bonneReponse, score
                            ));
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Extrait le JSON d'une chaîne
     */
    private String extraireJSON(String texte) {
        // Cherche un objet JSON { ... }
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(texte);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }



    public static class GeneratedQuestion {
        private String question;
        private String choix1;
        private String choix2;
        private String choix3;
        private String bonneReponse;
        private int score;

        public GeneratedQuestion(String question, String choix1, String choix2,
                                 String choix3, String bonneReponse, int score) {
            this.question = question;
            this.choix1 = choix1;
            this.choix2 = choix2;
            this.choix3 = choix3;
            this.bonneReponse = bonneReponse;
            this.score = score;
        }

        public String getQuestion() { return question; }
        public String getChoix1() { return choix1; }
        public String getChoix2() { return choix2; }
        public String getChoix3() { return choix3; }
        public String getBonneReponse() { return bonneReponse; }
        public int getScore() { return score; }
    }



    private static class ApiRequest {
        public String model;
        public List<Message> messages;
        public double temperature;
        public int max_tokens;

        public ApiRequest(String model, List<Message> messages,
                          double temperature, int max_tokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = max_tokens;
        }
    }

    private static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}