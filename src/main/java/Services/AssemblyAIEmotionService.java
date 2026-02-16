package com.auticare.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import java.util.HashMap;
import java.util.Map;

public class AssemblyAIEmotionService {

    // ✅ Ta clé AssemblyAI
    private static final String ASSEMBLY_API_KEY = "a85ac3dd39fa4ba095177104e778cb91";

    // ✅ Ta clé Gemini
    private static final String GEMINI_API_KEY = "AIzaSyD_97IIE3Ukb9AZXTiu0tqhoZfE7mps5r0";

    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    // ✅ FallbackService pour les conseils et analyse Groq
    private FallbackService fallbackService;

    // ✅ Les 7 émotions possibles
    private static final Map<String, String> EMOTION_PROMPTS = new HashMap<>();
    static {
        EMOTION_PROMPTS.put("happy", "La personne est joyeuse. Donne un conseil court et bienveillant en 2-3 phrases pour profiter de ce moment de bonheur. Réponds en français.");
        EMOTION_PROMPTS.put("sad", "La personne est triste. Donne un conseil réconfortant et doux en 2-3 phrases pour l'aider à traverser cette émotion. Réponds en français.");
        EMOTION_PROMPTS.put("angry", "La personne est en colère. Donne un conseil calme en 2-3 phrases pour l'aider à se détendre. Réponds en français.");
        EMOTION_PROMPTS.put("fearful", "La personne a peur ou est anxieuse. Donne un conseil rassurant en 2-3 phrases pour l'apaiser. Réponds en français.");
        EMOTION_PROMPTS.put("surprised", "La personne est surprise. Donne un conseil en 2-3 phrases pour l'aider à accueillir cette surprise. Réponds en français.");
        EMOTION_PROMPTS.put("disgusted", "La personne est dégoûtée. Donne un conseil en 2-3 phrases pour relativiser. Réponds en français.");
        EMOTION_PROMPTS.put("neutral", "La personne est d'humeur neutre. Donne un conseil en 2-3 phrases pour apporter un peu de positivité dans sa journée. Réponds en français.");
    }

    // ✅ Setter pour FallbackService
    public void setFallbackService(FallbackService service) {
        this.fallbackService = service;
        System.out.println("✅ FallbackService injecté dans AssemblyAIEmotionService");
    }

    /** Méthode principale pour analyser un fichier audio existant */
    public EmotionResult detecterEmotion(File audioFile) throws Exception {
        try {
            // 1️⃣ Upload du fichier audio
            String audioUrl = uploadAudio(audioFile);
            System.out.println("✅ Audio uploadé: " + audioUrl);

            // 2️⃣ Demande de transcription
            String transcriptId = requestTranscript(audioUrl);
            System.out.println("✅ Transcription ID: " + transcriptId);

            // 3️⃣ Attente et récupération du résultat
            JsonObject result = waitForResult(transcriptId);
            System.out.println("✅ Résultat reçu");

            // 4️⃣ Analyse du texte avec FALLBACK (Gemini → Groq → mots-clés)
            EmotionResult emotionResult = analyserTexteAvecFallback(result);

            // 5️⃣ Générer un conseil avec FALLBACK (Gemini → Groq → défaut)
            if (fallbackService != null) {
                String conseil = fallbackService.genererConseilAvecFallback(emotionResult.emotion);
                emotionResult.setConseil(conseil);
                System.out.println("✅ Conseil généré avec FallbackService");
            } else {
                System.out.println("⚠️ FallbackService non disponible, utilisation Gemini direct");
                String conseil = genererConseilAvecGemini(emotionResult.emotion);
                emotionResult.setConseil(conseil);
            }

            return emotionResult;

        } catch (Exception e) {
            System.err.println("❌ Erreur dans detecterEmotion: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ✅ DEMANDE DE TRANSCRIPTION SIMPLE
    private String requestTranscript(String audioUrl) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(TRANSCRIPT_URL);
            post.setHeader("Authorization", ASSEMBLY_API_KEY);
            post.setHeader("Content-Type", "application/json");

            JsonObject body = new JsonObject();
            body.addProperty("audio_url", audioUrl);

            // Modèle avancé pour meilleure précision
            JsonArray speechModels = new JsonArray();
            speechModels.add("universal-2");
            body.add("speech_models", speechModels);

            post.setEntity(new ByteArrayEntity(body.toString().getBytes("UTF-8")));

            CloseableHttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            response.close();

            System.out.println("📥 Réponse transcript brute: '" + responseBody + "'");

            // Nettoyer avant de parser
            responseBody = nettoyerReponseJSON(responseBody);

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (json.has("error")) {
                throw new IOException("Erreur API: " + json.get("error").getAsString());
            }

            return json.get("id").getAsString();

        } finally {
            client.close();
        }
    }

    // ✅ Analyser le texte avec FALLBACK (Gemini → Groq → mots-clés)
    private EmotionResult analyserTexteAvecFallback(JsonObject result) {
        try {
            // Récupérer le texte transcrit
            if (!result.has("text") || result.get("text").isJsonNull()) {
                return new EmotionResult("neutral", 0.5);
            }

            String texte = result.get("text").getAsString();
            System.out.println("📝 Texte transcrit: " + texte);

            if (texte == null || texte.trim().isEmpty()) {
                return new EmotionResult("neutral", 0.5);
            }

            // 1️⃣ Essayer avec Gemini d'abord
            String prompt = String.format(
                    "Analyse le texte suivant et détermine l'émotion dominante parmi: happy, sad, angry, fearful, surprised, disgusted, neutral.\n" +
                            "Texte: \"%s\"\n" +
                            "Retourne UNIQUEMENT le nom de l'émotion en anglais, rien d'autre.",
                    texte
            );

            String emotion = appelGemini(prompt).trim().toLowerCase();

            // Vérifier si Gemini a réussi
            if (emotion != null && !emotion.contains("erreur") && !emotion.contains("pas pu") && EMOTION_PROMPTS.containsKey(emotion)) {
                System.out.println("🎭 Émotion détectée par Gemini: " + emotion);
                return new EmotionResult(emotion, 0.8);
            }

            // 2️⃣ Si Gemini échoue, essayer avec Groq (si fallbackService disponible)
            if (fallbackService != null) {
                System.out.println("🔄 Fallback vers Groq pour l'analyse...");
                String groqPrompt = String.format(
                        "Analyze the following text and return ONLY the dominant emotion word from this list: happy, sad, angry, fearful, surprised, disgusted, neutral.\n" +
                                "Text: \"%s\"",
                        texte
                );

                String groqEmotion = fallbackService.analyserEmotionAvecGroq(texte).trim().toLowerCase();
                if (groqEmotion != null && EMOTION_PROMPTS.containsKey(groqEmotion)) {
                    System.out.println("🎭 Émotion détectée par Groq: " + groqEmotion);
                    return new EmotionResult(groqEmotion, 0.7);
                }
            }

            // 3️⃣ Si tout échoue, analyse par défaut basée sur mots-clés
            System.out.println("📋 Utilisation analyse par défaut");
            return analyserParMotsCles(texte);

        } catch (Exception e) {
            System.err.println("❌ Erreur analyse: " + e.getMessage());
            return new EmotionResult("neutral", 0.5);
        }
    }

    // ✅ Analyse par mots-clés (fallback final)
    private EmotionResult analyserParMotsCles(String texte) {
        texte = texte.toLowerCase();

        if (texte.contains("content") || texte.contains("heureux") || texte.contains("joie") ||
                texte.contains("happy") || texte.contains("glad") || texte.contains("super")) {
            return new EmotionResult("happy", 0.6);
        }
        if (texte.contains("triste") || texte.contains("pleure") || texte.contains("mal") ||
                texte.contains("sad") || texte.contains("cry") || texte.contains("déçu")) {
            return new EmotionResult("sad", 0.6);
        }
        if (texte.contains("peur") || texte.contains("anxieux") || texte.contains("inquiet") ||
                texte.contains("fear") || texte.contains("scared") || texte.contains("angoisse")) {
            return new EmotionResult("fearful", 0.6);
        }
        if (texte.contains("colère") || texte.contains("fâché") || texte.contains("énervé") ||
                texte.contains("angry") || texte.contains("mad") || texte.contains("furieux")) {
            return new EmotionResult("angry", 0.6);
        }
        if (texte.contains("surprise") || texte.contains("étonné") || texte.contains("wow") ||
                texte.contains("surprised") || texte.contains("incroyable")) {
            return new EmotionResult("surprised", 0.6);
        }
        if (texte.contains("dégout") || texte.contains("beurk") || texte.contains("disgust") ||
                texte.contains("dégoûtant") || texte.contains("beurk")) {
            return new EmotionResult("disgusted", 0.6);
        }

        return new EmotionResult("neutral", 0.5);
    }

    // ✅ Générer un conseil avec Gemini (fallback si FallbackService absent)
    private String genererConseilAvecGemini(String emotion) {
        System.out.println("💡 Génération conseil avec Gemini pour: " + emotion);
        String prompt = EMOTION_PROMPTS.getOrDefault(emotion,
                "La personne ressent de l'émotion. Donne un conseil bienveillant en 2-3 phrases. Réponds en français.");
        return appelGemini(prompt);
    }

    // ✅ Appel à Gemini
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
            try {
                HttpPost post = new HttpPost(GEMINI_URL);
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(requestBody, "UTF-8"));

                CloseableHttpResponse response = client.execute(post);
                String responseBody = EntityUtils.toString(response.getEntity());
                response.close();

                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonResponse.has("error")) {
                    String errorMsg = jsonResponse.getAsJsonObject("error").get("message").getAsString();
                    System.err.println("❌ Erreur API Gemini: " + errorMsg);
                    return "Je n'ai pas pu générer de conseil pour le moment.";
                }

                String resultat = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

                return resultat;

            } finally {
                client.close();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur appel Gemini: " + e.getMessage());
            return "Je n'ai pas pu générer de conseil pour le moment.";
        }
    }

    // ✅ Upload du fichier audio
    private String uploadAudio(File audioFile) throws IOException {
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(UPLOAD_URL);
            post.setHeader("Authorization", ASSEMBLY_API_KEY);
            post.setHeader("Content-Type", "application/octet-stream");
            post.setEntity(new ByteArrayEntity(audioBytes));

            CloseableHttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            response.close();

            System.out.println("📥 Réponse upload brute: '" + responseBody + "'");

            // Nettoyer la réponse avant de parser
            responseBody = nettoyerReponseJSON(responseBody);

            try {
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                if (json.has("upload_url")) {
                    return json.get("upload_url").getAsString();
                } else if (json.has("error")) {
                    throw new IOException("Erreur API: " + json.get("error").getAsString());
                } else {
                    throw new IOException("Réponse inattendue: " + responseBody);
                }
            } catch (JsonSyntaxException e) {
                System.err.println("❌ Erreur parsing JSON: " + e.getMessage());
                System.err.println("📄 Réponse reçue: " + responseBody);
                throw new IOException("Impossible de parser la réponse de l'API: " + responseBody);
            }

        } finally {
            client.close();
        }
    }

    // ✅ Méthode de nettoyage améliorée
    private String nettoyerReponseJSON(String texte) {
        if (texte == null || texte.isEmpty()) {
            return "{}";
        }

        // Enlever les caractères invisibles au début
        texte = texte.trim();

        // Enlever le BOM (Byte Order Mark) si présent
        if (texte.length() > 0 && texte.charAt(0) == '\uFEFF') {
            texte = texte.substring(1);
        }

        // Chercher le premier {
        int debut = texte.indexOf("{");
        if (debut != -1) {
            texte = texte.substring(debut);
        }

        // Remplacer les caractères mal encodés
        texte = texte.replace("Mod�r�", "Modéré")
                .replace("�", "é")
                .replace("è", "è")
                .replace("ê", "ê")
                .replace("à", "à")
                .replace("ç", "ç")
                .replace("œ", "oe");

        return texte;
    }

    // ✅ Attente du résultat
    private JsonObject waitForResult(String transcriptId) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            int attempts = 0;
            while (attempts < 30) {
                HttpGet get = new HttpGet(TRANSCRIPT_URL + "/" + transcriptId);
                get.setHeader("Authorization", ASSEMBLY_API_KEY);

                CloseableHttpResponse response = client.execute(get);
                String responseBody = EntityUtils.toString(response.getEntity());
                response.close();

                // Nettoyer avant de parser
                responseBody = nettoyerReponseJSON(responseBody);

                try {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                    String status = json.get("status").getAsString();
                    System.out.println("📊 Statut: " + status);

                    if (status.equals("completed")) {
                        return json;
                    } else if (status.equals("error")) {
                        throw new IOException("Erreur AssemblyAI: " + json);
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("⚠️ Erreur parsing en attente, tentative " + (attempts + 1));
                }

                attempts++;
                Thread.sleep(1000);
            }
            throw new IOException("Timeout - Le traitement a pris trop de temps");

        } finally {
            client.close();
        }
    }

    // ✅ Classe pour stocker le résultat
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

        @Override
        public String toString() {
            return String.format("Émotion: %s (%.1f%%) - Conseil: %s",
                    emotion, score * 100, conseil != null ? conseil : "Aucun conseil");
        }
    }
}