package com.auticare.services;

import com.auticare.entities.Event;
import com.auticare.services.GeminiService;
import com.auticare.services.GroqService;

import java.util.*;

public class FallbackService {

    private final GeminiService gemini;
    private final GroqService groq;  // ← Renommé de openRouter à groq

    public FallbackService(String groqApiKey) {  // ← Renommé le paramètre
        this.gemini = new GeminiService();
        this.groq = new GroqService(groqApiKey);  // ← Utilise GroqService
        System.out.println("✅ FallbackService initialisé (Gemini + Groq)");
    }

    /**
     * Génère une histoire avec fallback automatique
     */
    /**
     * Génère une histoire avec fallback automatique
     */
    public String genererHistoire(String sujet, int numeroHistoire) {
        System.out.println("📖 FallbackService.genererHistoire pour: " + sujet + " (histoire " + numeroHistoire + ")");
        int random = (int)(Math.random() * 1000);

        // Liste des émotions possibles (4 maintenant)
        String[][] emotions = {
                {"😊", "joie"},
                {"😢", "tristesse"},
                {"😲", "surprise"},
                {"😠", "colère"}
        };

        // Mélanger les émotions pour avoir un ordre aléatoire
        List<String[]> listeEmotions = new ArrayList<>(Arrays.asList(emotions));
        Collections.shuffle(listeEmotions);

        // Prendre les 3 premières émotions après mélange
        String[] emo1 = listeEmotions.get(0);
        String[] emo2 = listeEmotions.get(1);
        String[] emo3 = listeEmotions.get(2);

        // 1. Essayer Gemini avec ordre aléatoire
        String prompt = "Crée UNE SEULE histoire pour enfants sur: '" + sujet +
                "' avec EXACTEMENT 3 phrases.\n\n" +
                "Les émotions doivent apparaître dans CET ORDRE PRÉCIS:\n" +
                "PREMIÈRE PHRASE: " + emo1[0] + " (" + emo1[1] + ")\n" +
                "DEUXIÈME PHRASE: " + emo2[0] + " (" + emo2[1] + ")\n" +
                "TROISIÈME PHRASE: " + emo3[0] + " (" + emo3[1] + ")\n\n" +
                "Exemple de format EXACT à suivre:\n" +
                emo1[0] + " Le petit ours " + getVerbePourEmotion(emo1[1]) + ".\n" +
                emo2[0] + " " + getExemplePourEmotion(emo2[1]) + "\n" +
                emo3[0] + " " + getExemplePourEmotion(emo3[1]) + "\n\n" +
                "ID unique: " + random + "\n" +
                "Réponds UNIQUEMENT avec ces 3 lignes, rien d'autre.";

        String resultat = tryGemini(prompt);
        if (resultat != null) {
            System.out.println("✅ Histoire générée par Gemini");
            return resultat;
        }

        // 2. Essayer Groq avec Llama
        System.out.println("🔄 Fallback vers Groq (Llama)...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) {
            System.out.println("✅ Histoire générée par Groq/Llama");
            return resultat;
        }

        // 3. Histoire par défaut avec les émotions mélangées
        return genererHistoireParDefaut(emo1, emo2, emo3, numeroHistoire);
    }
    private String getVerbePourEmotion(String emotion) {
        switch(emotion) {
            case "joie": return "est content de jouer";
            case "tristesse": return "est triste d'être seul";
            case "surprise": return "est surpris par une étoile";
            case "colère": return "est fâché après le renard";
            default: return "est là";
        }
    }

    private String getExemplePourEmotion(String emotion) {
        switch(emotion) {
            case "joie": return "Il saute de joie en voyant ses amis !";
            case "tristesse": return "La pluie l'empêche de sortir.";
            case "surprise": return "Un cadeau magique apparaît !";
            case "colère": return "Quelqu'un a cassé son jouet préféré.";
            default: return "Que se passe-t-il ?";
        }
    }

    private String genererHistoireParDefaut(String[] emo1, String[] emo2, String[] emo3, int numeroHistoire) {
        String[] personnages = {"ours", "lapin", "lion", "éléphant", "renard", "pingouin"};
        String personnage = personnages[numeroHistoire % personnages.length];

        return emo1[0] + " Le petit " + personnage + " " + getVerbePourEmotion(emo1[1]) + ".\n" +
                emo2[0] + " " + getExemplePourEmotion(emo2[1]) + "\n" +
                emo3[0] + " " + getExemplePourEmotion(emo3[1]);
    }
    /**
     * Génère une explication d'émotion avec fallback
     */
    public String genererExplicationEmotion(String emotion) {
        System.out.println("📖 Explication pour: " + emotion);

        String prompt = "Explique simplement l'émotion '" + emotion +
                "' pour un enfant autiste. 2-3 phrases.";

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) return resultat;

        // 2. Essayer Groq avec Gemma (léger)
        System.out.println("🔄 Fallback vers Groq (Gemma)...");
        resultat = groq.generateWithGemma(prompt);  // ← Nouvelle méthode
        if (resultat != null) return resultat;

        // 3. Explication par défaut
        return getDefaultExplanation(emotion);
    }

    /**
     * Essaie Gemini et vérifie si la réponse est valide
     */
    private String tryGemini(String prompt) {
        try {
            String resultat = gemini.genererDescription(prompt);

            if (resultat != null && !resultat.isEmpty() &&
                    !resultat.startsWith("Erreur") && !resultat.contains("quota")) {
                System.out.println("✅ Réponse de Gemini");
                return resultat;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Gemini a échoué: " + e.getMessage());
        }
        return null;
    }

    /**
     * Explications par défaut
     */

    /**
     * Génère un rapport avec fallback
     */
    public String genererRapport(String sujet) {
        System.out.println("📊 FallbackService.genererRapport pour: " + sujet);

        // ✅ NOUVEAU PROMPT - Ce que tu vas apprendre
        String prompt = String.format(
                "Crée un rapport éducatif et positif sur l'événement '%s' pour des enfants autistes.\n\n" +
                        "IMPORTANT - Structure le rapport avec ces sections:\n\n" +
                        "🎯 CE QUE TU VAS APPRENDRE\n" +
                        "• 4 à 5 points sur les apprentissages spécifiques (selon le thème de l'événement)\n" +
                        "• Exemples: nouvelles compétences, découvertes, sensations\n\n" +
                        "🎮 CÔTÉ LUDIQUE\n" +
                        "• 3 à 4 points sur les activités amusantes prévues\n" +
                        "• Ce qui rend l'événement joyeux et attractif\n\n" +
                        "🌈 POUR T'AIDER À GRANDIR\n" +
                        "• 3 points sur le développement personnel (confiance, autonomie, partage)\n\n" +
                        "📝 INFORMATIONS PRATIQUES\n" +
                        "• Ce qu'il faut apporter\n" +
                        "• Comment se préparer\n" +
                        "• Ce qui est fourni sur place\n\n" +
                        "Utilise un langage simple, positif et encourageant. Ajoute des émojis au début de chaque section.\n" +
                        "Retourne UNIQUEMENT le rapport, sans introduction.",
                sujet
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) return resultat;

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq (Llama) pour rapport...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) return resultat;

        // 3. Rapport par défaut
        return getDefaultReport(sujet);
    }



    public String genererConseil(String emotion) {
        System.out.println("💡 FallbackService.genererConseil pour: " + emotion);

        String prompt = String.format(
                "Génère un conseil bienveillant et réconfortant pour une personne qui se sent %s. " +
                        "Le conseil doit être court, positif, adapté aux enfants autistes. " +
                        "Utilise un ton doux et encourageant courte .",
                emotion
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) return resultat;

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq (Llama) pour conseil...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) return resultat;

        // 3. Conseil par défaut
        return getDefaultAdvice(emotion);
    }

    /**
     * Génère des questions pour chasse au trésor
     */
    public String[][] genererIndicesChasse(String titre) {
        System.out.println("🗺️ FallbackService.genererIndicesChasse pour: " + titre);

        String prompt = String.format(
                "Crée un jeu de 5 questions-réponses basé sur le nom de l'événement: \"%s\".\n" +
                        "Retourne UNIQUEMENT un objet JSON avec cette structure:\n" +
                        "{\"questions\": [\n" +
                        "  {\"question\": \"texte question 1\", \"reponse\": \"réponse1\"},\n" +
                        "  {\"question\": \"texte question 2\", \"reponse\": \"réponse2\"}\n" +
                        "]}",
                titre
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) {
            return parseQuestions(resultat);
        }

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq (Llama) pour chasse...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) {
            return parseQuestions(resultat);
        }

        // 3. Questions par défaut
        return getDefaultQuestions(titre);
    }

    /**
     * Recommande des événements basés sur l'émotion
     */
    public String recommanderEvenements(String emotion, List<Event> events) {
        System.out.println("🎯 FallbackService.recommanderEvenements pour émotion: " + emotion);

        // Construire la liste des événements
        StringBuilder eventsList = new StringBuilder();
        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            eventsList.append(i).append(". ");
            eventsList.append("Titre: \"").append(e.getTitre()).append("\"\n");
            eventsList.append("   Description: \"").append(e.getDescription()).append("\"\n");
        }

        String prompt = String.format(
                "L'utilisateur se sent %s.\n\n" +
                        "Voici la liste des événements disponibles:\n%s\n" +
                        "Retourne UNIQUEMENT un objet JSON avec cette structure:\n" +
                        "{\"recommendations\": [\n" +
                        "  {\"index\": 0, \"score\": 95, \"raison\": \"...\"},\n" +
                        "  {\"index\": 2, \"score\": 87, \"raison\": \"...\"}\n" +
                        "]}",
                emotion, eventsList.toString()
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) return resultat;

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq (Llama) pour recommandations...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) return resultat;

        // 3. Recommandations par défaut
        return getDefaultRecommendations();
    }

// ========== MÉTHODES UTILITAIRES ==========

    private String[][] parseQuestions(String jsonResponse) {
        // Implémente le parsing JSON
        return new String[0][0]; // À implémenter
    }

    private String getDefaultReport(String sujet) {
        return "📌 DÉFINITION\n" +
                "Le " + sujet + " est un concept important...\n\n" +
                "✅ AVANTAGES\n" +
                "• Avantage 1\n• Avantage 2\n• Avantage 3\n\n" +
                "❌ INCONVÉNIENTS\n• Inconvénient 1\n• Inconvénient 2\n• Inconvénient 3\n\n" +
                "💡 CONSEILS\n• Conseil 1\n• Conseil 2\n• Conseil 3";
    }

    private String getDefaultAnalysis() {
        return "{\"noiseLevel\": \"Calme\", " +
                "\"sensoryStimulation\": \"Faible\", " +
                "\"recommendedAge\": \"5-10 ans\", " +
                "\"activityLevel\": \"Assis\", " +
                "\"recommendation\": \"Bon\"}";
    }

    private String getDefaultAdvice(String emotion) {
        return "Prends une grande respiration et souviens-toi que cette émotion va passer. " +
                "Tu es fort(e) et capable de la traverser !";
    }

    private String[][] getDefaultQuestions(String titre) {
        String[][] defaut = new String[5][2];
        defaut[0][0] = "Combien de mots dans le titre ?";
        defaut[0][1] = String.valueOf(titre.split(" ").length);
        defaut[1][0] = "Première lettre du titre ?";
        defaut[1][1] = titre.substring(0, 1).toLowerCase();
        return defaut;
    }
    private String getDefaultExplanation(String emotion) {
        switch (emotion.toLowerCase()) {
            case "joie":
                return "😊 La joie c'est quand on est content, quand on sourit et qu'on a envie de sauter partout !";
            case "tristesse":
                return "😢 La tristesse c'est quand on a envie de pleurer, quand quelque chose nous manque. C'est normal d'être triste parfois.";
            case "colère":
                return "😠 La colère c'est quand on est fâché, quand on a envie de crier. Respire profondément et ça ira mieux !";
            case "surprise":
                return "😲 La surprise c'est quand quelque chose d'inattendu arrive, comme un cadeau ou une bonne nouvelle !";
            case "peur":
                return "😨 La peur c'est quand on a un peu peur de quelque chose. C'est normal, prends une grande inspiration !";
            case "dégout":
                return "🤢 Le dégoût c'est quand quelque chose n'est pas bon ou pas agréable. C'est ton corps qui te protège !";
            default:
                return "😐 Chaque émotion est importante et normale. Écoute ton cœur !";
        }
    }

    private String getDefaultRecommendations() {
        return "{\"recommendations\": []}";
    }

    /**
     * Génère une description avec fallback
     */
    public String genererDescription(String prompt) {
        System.out.println("📝 FallbackService.genererDescription");

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) return resultat;

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq pour description...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) return resultat;

        // 3. Description par défaut
        return "Découvrez cet événement spécialement conçu pour les enfants autistes, " +
                "dans un cadre calme et bienveillant. Une expérience unique et adaptée !";
    }

    /**
     * Analyse un événement avec fallback
     */
    public String analyserEvenement(String titre, String description, String lieu) {
        System.out.println("🔍 FallbackService.analyserEvenement pour: " + titre);

        // ✅ PROMPT AMÉLIORÉ - BEAUCOUP PLUS PRÉCIS
        String prompt = String.format(
                "En tant qu'expert en accessibilité pour enfants autistes, analyse cet événement.\n\n" +
                        "Titre: \"%s\"\n" +
                        "Description: \"%s\"\n" +
                        "Lieu: \"%s\"\n\n" +
                        "Analyse ces 5 critères IMPORTANTS pour un enfant autiste:\n\n" +
                        "1. NIVEAU SONORE (noiseLevel):\n" +
                        "   - Calme: Bibliothèque, salle insonorisée, atelier calme\n" +
                        "   - Moyen: Parc, salle de classe, musée\n" +
                        "   - Bruyant: Concert, fête, stade, rue passante\n\n" +
                        "2. STIMULATION SENSORIELLE (sensoryStimulation):\n" +
                        "   - Faible: Lecture, dessin, promenade calme\n" +
                        "   - Moyenne: Peinture, cuisine, petit groupe\n" +
                        "   - Élevée: Feux d'artifice, lumières clignotantes, foules\n\n" +
                        "3. ÂGE RECOMMANDÉ (recommendedAge):\n" +
                        "   Donne une fourchette précise comme \"4-6 ans\", \"7-12 ans\", \"Tout âge\"\n\n" +
                        "4. NIVEAU D'ACTIVITÉ (activityLevel):\n" +
                        "   - Assis: Calme, table, écoute\n" +
                        "   - Modéré: Marche, mouvement léger\n" +
                        "   - Actif: Course, sport, danse\n\n" +
                        "5. RECOMMANDATION GLOBALE (recommendation):\n" +
                        "   - Excellent: Parfaitement adapté\n" +
                        "   - Bon: Adapté avec quelques ajustements\n" +
                        "   - Moyen: Peut convenir à certains enfants\n" +
                        "   - Déconseillé: Risques sensoriels élevés\n\n" +
                        "RETOURNE UNIQUEMENT UN JSON VALIDE avec cette structure EXACTE:\n" +
                        "{\n" +
                        "  \"noiseLevel\": \"Calme\",\n" +
                        "  \"sensoryStimulation\": \"Moyenne\",\n" +
                        "  \"recommendedAge\": \"5-8 ans\",\n" +
                        "  \"activityLevel\": \"Modéré\",\n" +
                        "  \"recommendation\": \"Bon\",\n" +
                        "  \"explication\": \"Courte explication de pourquoi cet événement est adapté\"\n" +
                        "}\n\n" +
                        "IMPORTANT: Le JSON doit être valide, sans texte avant ni après.",
                titre, description, lieu != null ? lieu : "Non spécifié"
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) {
            resultat = nettoyerReponseJSON(resultat);
            System.out.println("✅ Analyse via Gemini réussie");
            return resultat;
        }

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq (Llama) pour analyse...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) {
            resultat = nettoyerReponseJSON(resultat);
            System.out.println("✅ Analyse via Groq réussie");
            return resultat;
        }

        // 3. Analyse par défaut intelligente (basée sur le titre)
        System.out.println("📚 Utilisation analyse par défaut");
        return genererAnalyseParDefaut(titre, description);
    }

    // ✅ NOUVELLE MÉTHODE - Analyse par défaut intelligente
    private String genererAnalyseParDefaut(String titre, String description) {
        String titreLower = titre.toLowerCase();
        String descLower = description != null ? description.toLowerCase() : "";

        String noiseLevel = "Moyen";
        String sensoryStimulation = "Moyenne";
        String activityLevel = "Modéré";
        String recommendation = "Bon";

        // Détection basée sur le titre
        if (titreLower.contains("calme") || titreLower.contains("relaxation") ||
                titreLower.contains("méditation") || titreLower.contains("yoga")) {
            noiseLevel = "Calme";
            sensoryStimulation = "Faible";
            activityLevel = "Assis";
            recommendation = "Excellent";
        } else if (titreLower.contains("sport") || titreLower.contains("course") ||
                titreLower.contains("jeu") || titreLower.contains("actif")) {
            noiseLevel = "Moyen";
            sensoryStimulation = "Élevée";
            activityLevel = "Actif";
            recommendation = "Moyen";
        } else if (titreLower.contains("concert") || titreLower.contains("fête") ||
                titreLower.contains("festival")) {
            noiseLevel = "Bruyant";
            sensoryStimulation = "Élevée";
            activityLevel = "Actif";
            recommendation = "Déconseillé";
        } else if (titreLower.contains("atelier") || titreLower.contains("créatif") ||
                titreLower.contains("peinture") || titreLower.contains("dessin")) {
            noiseLevel = "Calme";
            sensoryStimulation = "Moyenne";
            activityLevel = "Assis";
            recommendation = "Bon";
        }

        // Détection dans la description
        if (descLower.contains("calme") || descLower.contains("silence") ||
                descLower.contains("tranquille")) {
            noiseLevel = "Calme";
        }
        if (descLower.contains("lumière") || descLower.contains("son") ||
                descLower.contains("bruit")) {
            sensoryStimulation = "Élevée";
        }

        // Âge par défaut
        String age = "5-10 ans";
        if (titreLower.contains("bébé") || titreLower.contains("tout-petit")) {
            age = "1-3 ans";
        } else if (titreLower.contains("enfant") || titreLower.contains("junior")) {
            age = "4-8 ans";
        } else if (titreLower.contains("ado") || titreLower.contains("adolescent")) {
            age = "12-17 ans";
        } else if (titreLower.contains("famille") || titreLower.contains("parent")) {
            age = "Tout âge";
        }

        String explication = String.format(
                "Événement %s avec niveau sonore %s, stimulation %s. " +
                        "Activité %s recommandée pour les enfants %s.",
                titre, noiseLevel.toLowerCase(), sensoryStimulation.toLowerCase(),
                activityLevel.toLowerCase(), age
        );

        return String.format(
                "{\n" +
                        "  \"noiseLevel\": \"%s\",\n" +
                        "  \"sensoryStimulation\": \"%s\",\n" +
                        "  \"recommendedAge\": \"%s\",\n" +
                        "  \"activityLevel\": \"%s\",\n" +
                        "  \"recommendation\": \"%s\",\n" +
                        "  \"explication\": \"%s\"\n" +
                        "}",
                noiseLevel, sensoryStimulation, age, activityLevel, recommendation, explication
        );
    }

    // ✅ Méthode de nettoyage améliorée
    private String nettoyerReponseJSON(String texte) {
        if (texte == null || texte.isEmpty()) {
            return null;
        }

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
    // Dans FallbackService.java - Ajoute cette méthode

    /**
     * Génère un planning personnalisé pour un événement
     */
    public String genererPlanning(Event event) {
        System.out.println("📅 FallbackService.genererPlanning pour: " + event.getTitre());

        String prompt = String.format(
                "En tant qu'expert en organisation d'événements pour enfants autistes, " +
                        "crée un planning détaillé pour cet événement:\n\n" +
                        "Titre: %s\n" +
                        "Description: %s\n" +
                        "Lieu: %s\n" +
                        "Date début: %s\n" +
                        "Date fin: %s\n\n" +
                        "Le planning doit inclure:\n" +
                        "🕐 HORAIRE DÉTAILLÉ (avec pauses sensorielles)\n" +
                        "🧘 ACTIVITÉS CALMES\n" +
                        "⚡ GESTION DES STIMULATIONS\n" +
                        "💡 CONSEILS POUR LES ACCOMPAGNANTS\n\n" +
                        "Format avec des émojis et sections claires.",
                event.getTitre(),
                event.getDescription(),
                event.getLieu(),
                event.getDateDebut(),
                event.getDateFin()
        );

        // 1. Essayer Gemini
        String resultat = tryGemini(prompt);
        if (resultat != null) {
            System.out.println("✅ Planning généré par Gemini");
            return resultat;
        }

        // 2. Essayer Groq
        System.out.println("🔄 Fallback vers Groq pour planning...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) {
            System.out.println("✅ Planning généré par Groq");
            return resultat;
        }

        // 3. Planning par défaut
        System.out.println("📋 Utilisation planning par défaut");
        return getDefaultPlanning(event);
    }

    /**
     * Planning par défaut
     */
    private String getDefaultPlanning(Event event) {
        return String.format(
                "📅 PLANNING - %s\n\n" +
                        "🕐 HORAIRE\n" +
                        "• Accueil calme: 30 min avant\n" +
                        "• Activité principale: %s\n" +
                        "• Pause sensorielle: toutes les 45 min\n" +
                        "• Retour au calme: 15 min avant fin\n\n" +
                        "🧘 ESPACE CALME\n" +
                        "• Zone avec éclairage tamisé\n" +
                        "• Casque anti-bruit disponible\n" +
                        "• Tapis et coussins\n\n" +
                        "⚡ GESTION SENSORIELLE\n" +
                        "• Éviter les lumières clignotantes\n" +
                        "• Volume sonore contrôlé\n" +
                        "• Groupes de 5-6 enfants maximum\n\n" +
                        "💡 CONSEILS\n" +
                        "• Prévenir à l'avance des transitions\n" +
                        "• Laisser un espace de retrait\n" +
                        "• Accueillir les objets réconfortants",
                event.getTitre(),
                event.getDescription().length() > 50 ?
                        event.getDescription().substring(0, 50) + "..." :
                        event.getDescription()
        );
    }
    // Dans FallbackService.java

    /**
     * Génère un conseil avec fallback (Gemini → Groq → défaut)
     */
    public String genererConseilAvecFallback(String emotion) {
        System.out.println("💡 FallbackService.genererConseil pour: " + emotion);

        String prompt = String.format(
                "Génère un conseil bienveillant et réconfortant pour une personne qui se sent %s. " +
                        "Le conseil doit être court 2 phrases courte, positif, adapté aux enfants autistes. " +
                        "Utilise un ton doux et encourageant. " +
                        "Réponds en français uniquement.",
                emotion
        );

        // 1. Essayer Gemini d'abord
        String resultat = tryGemini(prompt);
        if (resultat != null) {
            System.out.println("✅ Conseil généré par Gemini");
            return resultat;
        }

        // 2. Essayer Groq ensuite
        System.out.println("🔄 Fallback vers Groq pour conseil...");
        resultat = groq.generateWithLlama(prompt);
        if (resultat != null) {
            System.out.println("✅ Conseil généré par Groq");
            return resultat;
        }

        // 3. Conseil par défaut selon l'émotion
        System.out.println("📋 Utilisation conseil par défaut");
        return getDefaultConseil(emotion);
    }

    /**
     * Conseils par défaut selon l'émotion
     */
    private String getDefaultConseil(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy":
            case "joie":
            case "joyeux":
                return "😊 Profite de ce moment de bonheur ! Respire profondément et savoure cette émotion positive.";

            case "sad":
            case "triste":
            case "tristesse":
                return "😢 C'est normal d'être triste parfois. Prends une grande respiration, tu peux parler à quelqu'un de confiance.";

            case "angry":
            case "colère":
            case "fâché":
                return "😠 Respire calmement. Compte jusqu'à 10, ça va t'aider à te calmer. Tout va bien se passer.";

            case "fearful":
            case "peur":
            case "anxieux":
                return "😨 N'aie pas peur, tu es en sécurité. Prends mon main et respire doucement avec moi.";

            case "surprised":
            case "surprise":
                return "😲 Quelle surprise ! Prends le temps de comprendre ce qui se passe, c'est peut-être une bonne surprise.";

            case "disgusted":
            case "dégoût":
                return "🤢 C'est normal d'être dégoûté parfois. Éloigne-toi de ce qui te dérange et prends l'air.";

            case "neutral":
            default:
                return "😐 Chaque émotion est importante. Écoute ton cœur et prends soin de toi.";
        }
    }
    // Dans FallbackService.java - ajoute cette méthode
    public String analyserEmotionAvecGroq(String texte) {
        String prompt = String.format(
                "Analyze the following text and return ONLY the dominant emotion word from this list: happy, sad, angry, fearful, surprised, disgusted, neutral.\n" +
                        "Text: \"%s\"",
                texte
        );
        return groq.generateWithLlama(prompt);
    }
}