package com.auticare.services;

import com.auticare.entities.Event;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiService {

    private static final String API_KEY = "AIzaSyD_97IIE3Ukb9AZXTiu0tqhoZfE7mps5r0";
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

    /**
     * Analyse un événement pour déterminer son adaptabilité pour enfants autistes
     * @param titre Le titre de l'événement
     * @param description La description de l'événement
     * @param lieu Le lieu de l'événement (optionnel)
     * @return Un objet JSON avec les caractéristiques analysées
     */
    public String analyserEvenement(String titre, String description, String lieu) {
        String prompt = String.format(
                "Analyse cet événement pour enfants autistes. " +
                        "Titre: \"%s\". Description: \"%s\". Lieu: \"%s\". " +
                        "IMPORTANT: Retourne UNIQUEMENT un objet JSON valide, sans texte avant ni après, avec cette structure exacte: " +
                        "{\"noiseLevel\": \"Calme ou Moyen ou Bruyant\", " +
                        "\"sensoryStimulation\": \"Faible ou Moyenne ou Élevée\", " +
                        "\"recommendedAge\": \"ex: 5-10 ans\", " +
                        "\"activityLevel\": \"Assis ou Modéré ou Actif\", " +
                        "\"recommendation\": \"Excellent ou Bon ou Moyen ou Déconseillé\"}",
                titre, description, lieu != null ? lieu : "Non spécifié"
        );

        String resultat = genererDescription(prompt);
        System.out.println("📥 Gemini brut: " + resultat);

        // Nettoyer la réponse
        resultat = nettoyerReponseJSON(resultat);
        System.out.println("📥 Gemini nettoyé: " + resultat);

        return resultat;
    }

    private String nettoyerReponseJSON(String texte) {
        // Enlever tout ce qui est avant le premier {
        int debut = texte.indexOf("{");
        if (debut != -1) {
            texte = texte.substring(debut);
        }

        // Enlever tout ce qui est après le dernier }
        int fin = texte.lastIndexOf("}");
        if (fin != -1) {
            texte = texte.substring(0, fin + 1);
        }

        return texte;
    }

    /**
     * Version simplifiée pour retourner directement des labels de badges
     */
    public String[] analyserPourBadges(Event event) {
        String resultat = analyserEvenement(
                event.getTitre(),
                event.getDescription(),
                event.getLieu()
        );

        try {
            // Parser le JSON retourné
            JsonObject json = JsonParser.parseString(resultat).getAsJsonObject();

            String niveauSonore = json.get("noiseLevel").getAsString();
            String stimulation = json.get("sensoryStimulation").getAsString();
            String age = json.get("recommendedAge").getAsString();
            String activite = json.get("activityLevel").getAsString();

            // Créer des badges lisibles
            String badgeSonore = getEmojiForNoise(niveauSonore) + " " + niveauSonore;
            String badgeSensoriel = getEmojiForSensory(stimulation) + " " + stimulation;
            String badgeAge = "👥 " + age;
            String badgeActivite = getEmojiForActivity(activite) + " " + activite;

            return new String[]{
                    badgeSonore,
                    badgeSensoriel,
                    badgeAge,
                    badgeActivite
            };

        } catch (Exception e) {
            e.printStackTrace();
            // Valeurs par défaut
            return new String[]{
                    "🔊 Non déterminé",
                    "🧪 Non déterminé",
                    "👥 Âge non spécifié",
                    "🚶 Non déterminé"
            };
        }
    }

    private String getEmojiForNoise(String niveau) {
        switch(niveau.toLowerCase()) {
            case "calme": return "🔇";
            case "moyen": return "🔉";
            case "bruyant": return "🔊";
            default: return "🔊";
        }
    }

    private String getEmojiForSensory(String niveau) {
        switch(niveau.toLowerCase()) {
            case "faible": return "🧘";
            case "moyenne": return "🎨";
            case "élevée": return "⚡";
            default: return "🧪";
        }
    }

    private String getEmojiForActivity(String niveau) {
        switch(niveau.toLowerCase()) {
            case "assis": return "🪑";
            case "modéré": return "🚶";
            case "actif": return "🏃";
            default: return "🚶";
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
    public String genererExplicationEmotion(String emotion) {
        String prompt = String.format(
                "Explique simplement l'émotion '%s' pour un enfant autiste.\n" +
                        "Utilise un langage simple et des exemples concrets.\n" +
                        "Décris comment on reconnaît cette émotion sur le visage.\n" +
                        "3-4 phrases maximum.\n" +
                        "Retourne UNIQUEMENT l'explication, sans introduction.",
                emotion
        );
        return genererDescription(prompt);
    }

    public String genererConseil(String emotion) {
        String prompt = String.format(
                "Génère un conseil bienveillant et réconfortant pour une personne qui se sent %s. " +
                        "Le conseil doit être courte, positif, adapté aux enfants autistes. " +
                        "Utilise un ton doux et encourageant. " +
                        "Retourne UNIQUEMENT le conseil, sans introduction ni guillemets.",
                emotion
        );

        return genererDescription(prompt);
    }

    /**
     * Génère des indices pour une chasse au trésor basée sur un événement
     */
    /**
     * Génère des indices ET des questions pour une chasse au trésor basée sur un événement
     */
    /**
     * Génère des questions pour une chasse au trésor basée sur le NOM de l'événement
     */
    public String[][] genererIndicesChasse(int idEvent, String titre, String description,
                                           String typeEvent, String lieu,
                                           String dateDebut, String dateFin) {
        String prompt = String.format(
                "Crée un jeu de questions-réponses basé UNIQUEMENT sur le nom de l'événement: \"%s\".\n\n" +
                        "Le nom de l'événement est: %s\n\n" +
                        "IMPORTANT: Génère 5 questions ludiques liées à CE NOM précis.\n" +
                        "Les questions doivent être basées sur le thème suggéré par le nom.\n\n" +
                        "Exemples:\n" +
                        "- Si le nom est 'Promenade au zoo': questions sur les animaux\n" +
                        "- Si le nom est 'Atelier cuisine': questions sur la nourriture\n" +
                        "- Si le nom est 'Journée à la plage': questions sur la mer\n" +
                        "- Si le nom est 'Conférence IA': questions sur la technologie\n\n" +
                        "Format de retour (JSON uniquement, sans texte avant/après):\n" +
                        "{\n" +
                        "  \"questions\": [\n" +
                        "    {\"question\": \"texte question 1\", \"reponse\": \"réponse1\"},\n" +
                        "    {\"question\": \"texte question 2\", \"reponse\": \"réponse2\"},\n" +
                        "    {\"question\": \"texte question 3\", \"reponse\": \"réponse3\"},\n" +
                        "    {\"question\": \"texte question 4\", \"reponse\": \"réponse4\"},\n" +
                        "    {\"question\": \"texte question 5\", \"reponse\": \"réponse5\"}\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Les questions doivent être amusantes et simples. " +
                        "Les réponses doivent être en minuscules, sans accents.",
                titre, titre
        );

        String resultat = genererDescription(prompt);
        System.out.println("📥 Questions générées pour '" + titre + "': " + resultat);

        // Nettoyer et parser la réponse
        resultat = nettoyerReponseJSON(resultat);

        try {
            JsonObject json = JsonParser.parseString(resultat).getAsJsonObject();
            JsonArray questionsArray = json.getAsJsonArray("questions");

            String[][] questionsReponses = new String[5][2];

            for (int i = 0; i < questionsArray.size() && i < 5; i++) {
                JsonObject obj = questionsArray.get(i).getAsJsonObject();
                questionsReponses[i][0] = obj.get("question").getAsString();
                questionsReponses[i][1] = obj.get("reponse").getAsString().toLowerCase();
            }

            return questionsReponses;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing questions: " + e.getMessage());
            return getQuestionsParDefaut(titre);
        }
    }

    /**
     * Questions par défaut basées sur le nom
     */
    private String[][] getQuestionsParDefaut(String titre) {
        String[][] defaut = new String[5][2];

        defaut[0][0] = "Combien de mots dans le nom de l'événement ?";
        defaut[0][1] = String.valueOf(titre.split(" ").length);

        defaut[1][0] = "Quelle est la première lettre du nom ?";
        defaut[1][1] = titre.substring(0, 1).toLowerCase();

        defaut[2][0] = "Quel est le mot le plus long dans le nom ?";
        String[] mots = titre.split(" ");
        String plusLong = "";
        for (String m : mots) {
            if (m.length() > plusLong.length()) plusLong = m;
        }
        defaut[2][1] = plusLong.toLowerCase();

        defaut[3][0] = "Combien de lettres dans le nom (sans espaces) ?";
        defaut[3][1] = String.valueOf(titre.replace(" ", "").length());

        defaut[4][0] = "Quel est le thème principal de l'événement ?";
        defaut[4][1] = "theme";

        return defaut;
    }
    /**
     * Questions par défaut si l'API échoue
     */
    private String[][] getQuestionsParDefaut(String titre, String description,
                                             String lieu, String dateDebut, String typeEvent) {
        String[][] defaut = new String[5][2];

        defaut[0][0] = "Quel est le mois de cet événement ?";
        defaut[0][1] = dateDebut != null ?
                dateDebut.split("-")[1] : "mars";

        defaut[1][0] = "Où a lieu cet événement ? (sans les espaces)";
        defaut[1][1] = lieu != null ?
                lieu.toLowerCase().replace(" ", "") : "lieu";

        defaut[2][0] = "Combien de mots dans le titre ?";
        defaut[2][1] = String.valueOf(titre.split(" ").length);

        defaut[3][0] = "Quelle est la première lettre du type d'événement ?";
        defaut[3][1] = typeEvent != null ?
                typeEvent.substring(0, 1).toLowerCase() : "c";

        defaut[4][0] = "Quel est le mot le plus long dans la description ?";
        defaut[4][1] = description != null && description.length() > 0 ?
                description.split(" ")[0].toLowerCase() : "evenement";

        return defaut;
    }

    /**
     * Indices par défaut si l'API échoue
     */
    private String[][] getIndicesParDefaut(String titre, String description,
                                           String lieu, String dateDebut, String typeEvent) {
        String[][] defaut = new String[5][2];

        defaut[0][0] = "Je suis le mois de cet événement";
        defaut[0][1] = dateDebut != null ?
                dateDebut.split("-")[1] : "mars";

        defaut[1][0] = "Je suis le nom du lieu, mais sans les espaces";
        defaut[1][1] = lieu != null ?
                lieu.toLowerCase().replace(" ", "") : "lieu";

        defaut[2][0] = "Combien de mots dans le titre ? (réponse en chiffres)";
        defaut[2][1] = String.valueOf(titre.split(" ").length);

        defaut[3][0] = "La première lettre du type d'événement";
        defaut[3][1] = typeEvent != null ?
                typeEvent.substring(0, 1).toLowerCase() : "c";

        defaut[4][0] = "Trouve un mot clé dans la description (le plus long)";
        defaut[4][1] = description != null && description.length() > 0 ?
                description.split(" ")[0].toLowerCase() : "evenement";

        return defaut;
    }
    /**
     * Recommande des événements basés sur l'émotion de l'utilisateur
     * @param emotion L'émotion détectée (ex: "joie", "tristesse")
     * @param events Liste des événements disponibles
     * @return JSON avec les recommandations
     */
    public String recommanderEvenements(String emotion, List<Event> events) {
        // Construire la liste des événements pour le prompt
        StringBuilder eventsList = new StringBuilder();
        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            eventsList.append(i).append(". ");
            eventsList.append("Titre: \"").append(e.getTitre()).append("\"\n");
            eventsList.append("   Description: \"").append(e.getDescription()).append("\"\n");
            eventsList.append("   Type: \"").append(e.getTypeEvent()).append("\"\n\n");
        }

        String prompt = String.format(
                "L'utilisateur se sent %s.\n\n" +
                        "Voici la liste des événements disponibles:\n%s\n" +
                        "IMPORTANT: Analyse le TITRE et la DESCRIPTION de chaque événement pour déterminer " +
                        "lesquels sont les plus adaptés à quelqu'un qui se sent %s.\n\n" +
                        "Retourne UNIQUEMENT un objet JSON valide avec cette structure EXACTE:\n" +
                        "{\n" +
                        "  \"recommendations\": [\n" +
                        "    {\n" +
                        "      \"index\": 0,\n" +
                        "      \"score\": 95,\n" +
                        "      \"raison\": \"Parfait pour exprimer ta joie à travers l'art\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"index\": 2,\n" +
                        "      \"score\": 87,\n" +
                        "      \"raison\": \"Profite de ton énergie positive en plein air\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Les indices correspondent à la position de l'événement dans la liste (0, 1, 2...).\n" +
                        "Le score doit être entre 0 et 100.\n" +
                        "La raison doit être courte, positive et personnalisée.\n" +
                        "Retourne les 3 à 5 meilleurs événements, triés par score décroissant.",
                emotion, eventsList.toString(), emotion
        );

        String resultat = genererDescription(prompt);
        System.out.println("📥 Recommandations Gemini: " + resultat);

        // Nettoyer la réponse
        resultat = nettoyerReponseJSON(resultat);

        return resultat;
    }
    /**
     * Parse la réponse JSON et retourne une liste d'événements triés par pertinence
     */
    public List<Event> parserRecommandations(String jsonResponse, List<Event> allEvents) {
        List<Event> sortedEvents = new ArrayList<>();

        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray recommendations = json.getAsJsonArray("recommendations");

            // Créer une liste temporaire avec les scores
            Map<Integer, Integer> scores = new HashMap<>();
            Map<Integer, String> raisons = new HashMap<>();

            for (int i = 0; i < recommendations.size(); i++) {
                JsonObject rec = recommendations.get(i).getAsJsonObject();
                int index = rec.get("index").getAsInt();
                int score = rec.get("score").getAsInt();
                String raison = rec.get("raison").getAsString();

                if (index >= 0 && index < allEvents.size()) {
                    scores.put(index, score);
                    raisons.put(index, raison);
                }
            }

            // Trier les indices par score décroissant
            List<Integer> sortedIndices = new ArrayList<>(scores.keySet());
            sortedIndices.sort((a, b) -> scores.get(b) - scores.get(a));

            // Créer la liste triée
            for (int index : sortedIndices) {
                Event e = allEvents.get(index);
                // Tu pourrais stocker le score et la raison dans l'événement si besoin
                sortedEvents.add(e);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing recommandations: " + e.getMessage());
            // En cas d'erreur, retourner la liste originale
            return new ArrayList<>(allEvents);
        }

        return sortedEvents;
    }
    /**
     * Génère une histoire interactive pour enfants basée sur le nom d'un événement
     * @param nomEvent Le titre de l'événement (ex: "Promenade au zoo")
     * @return Une histoire avec des émoticônes à chaque phrase
     */
    public String genererHistoire(String nomEvent) {
        String prompt = "Crée une courte histoire pour enfants (4-5 phrases) basée sur cet événement : '" + nomEvent + "'.\n" +
                "L'histoire doit parler d'un petit animal mignon (lion, ours, lapin, éléphant) qui vit cet événement.\n" +
                "Inclus des moments avec ces 4 émotions :\n" +
                "- 😊 Joie\n" +
                "- 😢 Tristesse\n" +
                "- 😲 Surprise\n" +
                "- 😠 Colère\n\n" +
                "IMPORTANT : Mets l'émoticône au DÉBUT de chaque phrase qui correspond à l'émotion ressentie.\n" +
                "Chaque phrase doit être sur une nouvelle ligne.\n\n" +
                "Exemple de format :\n" +
                "😊 Le petit lion était content d'aller au zoo.\n" +
                "😢 Mais il a perdu son doudou préféré.\n" +
                "😲 Soudain, un perroquet coloré lui a parlé !\n" +
                "😠 Un singe malicieux a volé sa casquette.\n" +
                "😊 À la fin, tout est bien qui finit bien.\n\n" +
                "Retourne UNIQUEMENT l'histoire, sans introduction ni explications.";

        return genererDescription(prompt);
    }

    /**
     * Version avec plus de contrôle sur les émotions
     * @param nomEvent Le titre de l'événement
     * @param personnage Le personnage principal (lion, ours, etc.)
     * @return Une histoire personnalisée
     */
    public String genererHistoirePersonnalisee(String nomEvent, String personnage) {
        String prompt = "Crée une histoire pour enfants avec " + personnage +
                " comme personnage principal, basée sur l'événement : '" + nomEvent + "'.\n" +
                "L'histoire doit avoir 5 phrases, chacune avec une émotion différente parmi :\n" +
                "😊 Joie, 😢 Tristesse, 😲 Surprise, 😠 Colère.\n" +
                "Mets l'émoticône au début de chaque phrase.\n" +
                "Retourne UNIQUEMENT les 5 phrases, une par ligne.";

        return genererDescription(prompt);
    }

    /**
     * Génère une histoire et extrait automatiquement les moments clés
     * @param nomEvent Le titre de l'événement
     * @return Un objet JSON avec l'histoire et les émotions
     */
    public String genererHistoireStructuree(String nomEvent) {
        String prompt = "Crée une histoire pour enfants basée sur : '" + nomEvent + "'.\n" +
                "Retourne un objet JSON avec cette structure exacte :\n" +
                "{\n" +
                "  \"personnage\": \"lion\",\n" +
                "  \"histoire\": [\n" +
                "    {\"texte\": \"Le petit lion était content d'aller au zoo.\", \"emotion\": \"joie\", \"icone\": \"😊\"},\n" +
                "    {\"texte\": \"Il a perdu son doudou.\", \"emotion\": \"tristesse\", \"icone\": \"😢\"},\n" +
                "    {\"texte\": \"Un perroquet lui a parlé !\", \"emotion\": \"surprise\", \"icone\": \"😲\"},\n" +
                "    {\"texte\": \"Un singe a volé sa casquette.\", \"emotion\": \"colère\", \"icone\": \"😠\"},\n" +
                "    {\"texte\": \"Tout est bien qui finit bien.\", \"emotion\": \"joie\", \"icone\": \"😊\"}\n" +
                "  ]\n" +
                "}\n\n" +
                "IMPORTANT : Retourne UNIQUEMENT le JSON, sans texte avant ni après.";

        return genererDescription(prompt);
    }
}