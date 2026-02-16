package com.auticare.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    // 🔑 Remplace par TA clé API
    private static final String API_KEY = "b9d50e1c18c0178289e0d328fa4b6d7";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Récupère la météo pour une ville donnée.
     *
     * @param city Le nom de la ville (ex: "Paris")
     * @return Une chaîne de caractères formatée avec la météo, ou un message d'erreur.
     */
    public String getWeatherForCity(String city) {
        try {
            // 1. Construire l'URL de la requête (en français et en degrés Celsius)
            String urlString = String.format("%s?q=%s&appid=%s&units=metric&lang=fr",
                    BASE_URL, city, API_KEY);

            // 2. Envoyer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Vérifier la réponse
            if (response.statusCode() == 200) {
                // 4. Parser le JSON avec Gson
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return formatWeatherData(json);
            } else if (response.statusCode() == 401) {
                return "Erreur : Clé API invalide. Vérifie ta clé sur openweathermap.org";
            } else if (response.statusCode() == 404) {
                return "Ville '" + city + "' non trouvée.";
            } else {
                return "Erreur météo (" + response.statusCode() + ")";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion à OpenWeatherMap.";
        }
    }

    /**
     * Formate les données JSON en une phrase lisible.
     */
    private String formatWeatherData(JsonObject json) {
        // Extraire les informations principales
        String cityName = json.get("name").getAsString();
        JsonObject main = json.getAsJsonObject("main");
        JsonObject weather = json.getAsJsonArray("weather").get(0).getAsJsonObject();
        JsonObject wind = json.getAsJsonObject("wind");

        // ✅ Garder le format avec une décimale
        double temp = main.get("temp").getAsDouble();
        int humidity = main.get("humidity").getAsInt();
        String description = weather.get("description").getAsString();

        // Extraire la vitesse du vent
        double windSpeed = wind.has("speed") ? wind.get("speed").getAsDouble() : 0;
        double windSpeedKmh = windSpeed * 3.6;

        // ✅ Formater avec une décimale (ex: 15.0 → 15,0)
        return String.format("%s : %.1f°C, %s, %d%% humidité, %.0f km/h",
                cityName, temp, description, humidity, windSpeedKmh);
    }
}