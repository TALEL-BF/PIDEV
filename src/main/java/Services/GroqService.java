package com.auticare.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroqService {

    private final HttpClient client;
    private final String apiKey;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GroqService(String apiKey) {
        this.client = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        System.out.println("✅ GroqService initialisé");
    }

    /**
     * Génère du texte avec le modèle spécifié
     */
    private String generate(String model, String prompt) {
        try {
            System.out.println("📤 Appel Groq - Modèle: " + model);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 500);

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);

            com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
            messages.add(message);
            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String result = json.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                System.out.println("✅ Réponse Groq reçue (" + result.length() + " caractères)");
                return result;
            } else {
                System.err.println("❌ Erreur HTTP Groq " + response.statusCode() + ": " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Groq erreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Modèle principal - Llama 3.3 70B (le plus récent et performant)
     */
    public String generateWithLlama(String prompt) {
        return generate("llama-3.3-70b-versatile", prompt);
    }

    /**
     * Fallback vers Llama (car Mixtral n'existe plus)
     */
    public String generateWithMixtral(String prompt) {
        System.out.println("⚠️ Mixtral déprécié, utilisation de Llama 3.3 à la place");
        return generateWithLlama(prompt);
    }

    /**
     * Fallback vers Llama (car Gemma n'existe plus)
     */
    public String generateWithGemma(String prompt) {
        System.out.println("⚠️ Gemma déprécié, utilisation de Llama 3.3 à la place");
        return generateWithLlama(prompt);
    }

    /**
     * Version simplifiée pour une histoire
     */
    public String genererHistoire(String sujet) {
        String prompt = "Crée une courte histoire pour enfants sur: " + sujet +
                ". 3 phrases avec émoticônes (😊 😢 😲). Réponds UNIQUEMENT les 3 phrases.";
        return generateWithLlama(prompt);
    }

    /**
     * Pour les explications courtes (utilise Llama 3.3 aussi)
     */
    public String genererExplication(String emotion) {
        String prompt = "Explique simplement l'émotion '" + emotion +
                "' pour un enfant autiste. 2-3 phrases maximum.";
        return generateWithLlama(prompt);
    }

    /**
     * Méthode de test
     */
    public void testConnexion() {
        try {
            String test = generateWithLlama("Dis 'OK' en un seul mot");
            System.out.println("🔑 Test Groq: " + test);
        } catch (Exception e) {
            System.err.println("❌ Test échoué: " + e.getMessage());
        }
    }
}