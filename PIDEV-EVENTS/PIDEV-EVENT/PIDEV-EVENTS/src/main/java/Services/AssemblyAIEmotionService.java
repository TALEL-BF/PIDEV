package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AssemblyAIEmotionService {

    private static final String API_KEY = "08fdc46170f84f239441ac505de382a4";
    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    // ✅ Utilise TA clé Gemini existante
    private static final String GEMINI_API_KEY = "AIzaSyDBMgL7JtQJpzyyQZHFnMqYkvTembMknHk";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    /** Méthode principale pour analyser un fichier audio existant */
    public EmotionResult detecterEmotion(File audioFile) throws Exception {
        // 1️⃣ Upload du fichier audio
        String audioUrl = uploadAudio(audioFile);
        System.out.println("✅ Audio uploadé: " + audioUrl);

        // 2️⃣ Demande de transcription + sentiment analysis
        String transcriptId = requestTranscript(audioUrl);
        System.out.println("✅ Transcription ID: " + transcriptId);

        // 3️⃣ Attente et récupération du résultat
        JsonObject result = waitForResult(transcriptId);
        System.out.println("✅ Résultat reçu");

        // 4️⃣ Analyse et retour de l'émotion dominante
        EmotionResult emotionResult = analyserReponse(result);

        // 5️⃣ AJOUT : Générer un conseil avec TON Gemini
        String conseil = genererConseil(emotionResult.emotion);
        emotionResult.setConseil(conseil);

        return emotionResult;
    }

    // ✅ NOUVELLE MÉTHODE : Générer un conseil selon l'émotion
    private String genererConseil(String emotion) {
        String prompt;

        switch(emotion) {
            case "happy":
                prompt = "La personne est joyeuse. Donne un conseil court et bienveillant en 2-3 phrases pour profiter de ce moment de bonheur. Réponds en français.";
                break;
            case "sad":
                prompt = "La personne est triste. Donne un conseil réconfortant et doux en 2-3 phrases pour l'aider à traverser cette émotion. Réponds en français.";
                break;
            case "angry":
                prompt = "La personne est en colère. Donne un conseil calme en 2-3 phrases pour l'aider à se détendre. Réponds en français.";
                break;
            case "neutral":
            default:
                prompt = "La personne est d'humeur neutre. Donne un conseil en 2-3 phrases pour apporter un peu de positivité dans sa journée. Réponds en français.";
                break;
        }

        return appelGemini(prompt);
    }

    // ✅ NOUVELLE MÉTHODE : Appel à TON Gemini
    private String appelGemini(String prompt) {
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
            HttpPost post = new HttpPost(GEMINI_URL);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            client.close();

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            if (jsonResponse.has("error")) {
                String errorMsg = jsonResponse.getAsJsonObject("error").get("message").getAsString();
                System.err.println("❌ Erreur API Gemini: " + errorMsg);
                return "Je n'ai pas pu générer de conseil pour le moment.";
            }

            String conseil = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            return conseil;

        } catch (Exception e) {
            e.printStackTrace();
            return "Je n'ai pas pu générer de conseil pour le moment.";
        }
    }

    private String uploadAudio(File audioFile) throws IOException {
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(UPLOAD_URL);
        post.setHeader("Authorization", API_KEY);
        post.setHeader("Content-Type", "application/octet-stream");
        post.setEntity(new ByteArrayEntity(audioBytes));

        CloseableHttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());
        client.close();

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        return json.get("upload_url").getAsString();
    }

    private String requestTranscript(String audioUrl) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(TRANSCRIPT_URL);
        post.setHeader("Authorization", API_KEY);
        post.setHeader("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("audio_url", audioUrl);
        body.addProperty("sentiment_analysis", true);

        JsonArray speechModels = new JsonArray();
        speechModels.add("universal-2");
        body.add("speech_models", speechModels);

        post.setEntity(new ByteArrayEntity(body.toString().getBytes("UTF-8")));

        CloseableHttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());
        client.close();

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        if (json.has("error")) {
            throw new IOException("Erreur API: " + json.get("error").getAsString());
        }

        return json.get("id").getAsString();
    }

    private JsonObject waitForResult(String transcriptId) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        int attempts = 0;
        while (attempts < 30) {
            HttpGet get = new HttpGet(TRANSCRIPT_URL + "/" + transcriptId);
            get.setHeader("Authorization", API_KEY);

            CloseableHttpResponse response = client.execute(get);
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            String status = json.get("status").getAsString();
            System.out.println("📊 Statut: " + status);

            if (status.equals("completed")) {
                client.close();
                return json;
            } else if (status.equals("error")) {
                client.close();
                throw new IOException("Erreur AssemblyAI: " + json);
            }

            attempts++;
            Thread.sleep(1000);
        }
        client.close();
        throw new IOException("Timeout - Le traitement a pris trop de temps");
    }

    private EmotionResult analyserReponse(JsonObject result) {
        JsonArray sentiments = result.getAsJsonArray("sentiment_analysis_results");
        if (sentiments == null || sentiments.size() == 0) {
            return new EmotionResult("neutral", 0.5);
        }

        int pos = 0, neg = 0, neu = 0;
        for (int i = 0; i < sentiments.size(); i++) {
            String sentiment = sentiments.get(i).getAsJsonObject().get("sentiment").getAsString();
            switch (sentiment) {
                case "POSITIVE": pos++; break;
                case "NEGATIVE": neg++; break;
                case "NEUTRAL": neu++; break;
            }
        }

        int total = sentiments.size();
        if (pos > neg && pos > neu) return new EmotionResult("happy", (double) pos / total);
        if (neg > pos && neg > neu) return new EmotionResult("sad", (double) neg / total);
        return new EmotionResult("neutral", (double) neu / total);
    }

    public static class EmotionResult {
        public final String emotion;
        public final double score;
        private String conseil;

        public EmotionResult(String emotion, double score) {
            this.emotion = emotion;
            this.score = score;
        }

        public void setConseil(String conseil) {
            this.conseil = conseil;
        }

        public String getConseil() {
            return conseil;
        }
    }
}