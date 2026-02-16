import com.auticare.entities.Event;

import com.auticare.entities.Event;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import java.util.*;

public class EventAIPredictor {

    private RandomForest model;
    private boolean isTrained = false;
    private Instances dataStructure;

    // Train the model with past events
    public void train(List<Event> pastEvents) {
        try {
            if (pastEvents == null || pastEvents.isEmpty()) {
                System.out.println("⚠️ No past events for training");
                return;
            }

            // Create attribute list
            ArrayList<Attribute> attributes = new ArrayList<>();

            // Features we can extract from any event
            attributes.add(new Attribute("title_length"));
            attributes.add(new Attribute("desc_length"));
            attributes.add(new Attribute("day_of_week"));
            attributes.add(new Attribute("month"));
            attributes.add(new Attribute("duration_days"));
            attributes.add(new Attribute("max_participants"));

            // Class attribute - AUTISM FRIENDLINESS
            attributes.add(new Attribute("autism_score"));

            // Create dataset structure
            dataStructure = new Instances("AutismPrediction", attributes, 0);
            dataStructure.setClassIndex(attributes.size() - 1);

            // Add past events to dataset with autism scores
            Instances trainData = new Instances(dataStructure);

            System.out.println("\n🧩 TRAINING DATA (Autism Friendly):");
            System.out.println("=================");

            for (Event e : pastEvents) {
                double[] values = extractFeatures(e);
                double autismScore = calculateAutismScore(e);
                values[values.length - 1] = autismScore;
                trainData.add(new DenseInstance(1.0, values));

                System.out.printf("Event: %-30s | Features: [%3.0f, %3.0f, %.0f, %.0f, %.0f, %3.0f] | Autism: %.1f%%\n",
                        e.getTitre().length() > 25 ? e.getTitre().substring(0, 25) + "..." : e.getTitre(),
                        values[0], values[1], values[2], values[3], values[4], values[5], autismScore);
            }

            // Train model with RandomForest
            model = new RandomForest();
            model.setNumIterations(20);
            model.buildClassifier(trainData);
            isTrained = true;

            System.out.println("\n✅ Autism prediction model trained with " + pastEvents.size() + " events");

        } catch (Exception e) {
            System.out.println("❌ Training error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Extract numerical features from event
    private double[] extractFeatures(Event e) {
        double[] values = new double[7]; // 6 features + autism score

        // 1. Title length
        values[0] = e.getTitre() != null ? e.getTitre().length() : 0;

        // 2. Description length
        values[1] = e.getDescription() != null ? e.getDescription().length() : 0;

        // 3. Day of week
        if (e.getDateDebut() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDateDebut());
            values[2] = cal.get(Calendar.DAY_OF_WEEK); // 1-7
        } else {
            values[2] = 3;
        }

        // 4. Month
        if (e.getDateDebut() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDateDebut());
            values[3] = cal.get(Calendar.MONTH) + 1; // 1-12
        } else {
            values[3] = 6;
        }

        // 5. Duration in days
        if (e.getDateDebut() != null && e.getDateFin() != null) {
            long diff = e.getDateFin().getTime() - e.getDateDebut().getTime();
            values[4] = diff / (1000 * 60 * 60 * 24);
            if (values[4] < 0) values[4] = 1;
        } else {
            values[4] = 1;
        }

        // 6. Max participants
        if (e.getMaxParticipant() > 0) {
            values[5] = e.getMaxParticipant();
        } else {
            values[5] = 50;
        }

        return values;
    }

    // ===== AUTISM SCORE CALCULATION =====
    private double calculateAutismScore(Event e) {
        double score = 50.0;
        String titre = e.getTitre() != null ? e.getTitre().toLowerCase() : "";
        String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
        String combined = titre + " " + desc;

        System.out.println("\n🔍 Analyzing autism suitability for: " + e.getTitre());

        // ✅ POSITIVE keywords (good for autistic people)
        String[] autismPositive = {
                "calme", "silence", "tranquille", "zen", "paisible",
                "accessible", "adapté", "structure", "organisé",
                "petit groupe", "intimiste", "espace calme",
                "sans stress", "bienveillant", "sensoriel",
                "sans bruit", "relaxation", "méditation",
                "yoga", "atelier calme", "routine",
                "prévisible", "sans surprise", "sécurisé"
        };

        // ❌ NEGATIVE keywords (bad for autistic people)
        String[] autismNegative = {
                "foule", "bruyant", "musique forte", "lumières",
                "surprise", "improvisation", "grand public",
                "spectacle", "feux", "fête", "party", "festival",
                "concert", "klaxon", "alarme", "chaotique",
                "imprévisible", "changement", "bousculade",
                "attente longue", "cohue"
        };

        // Count positives
        int positifCount = 0;
        for (String mot : autismPositive) {
            if (combined.contains(mot)) {
                score += 10;
                positifCount++;
                System.out.println("  ✅ Positive: " + mot);
            }
        }

        // Count negatives
        int negatifCount = 0;
        for (String mot : autismNegative) {
            if (combined.contains(mot)) {
                score -= 15;
                negatifCount++;
                System.out.println("  ❌ Negative: " + mot);
            }
        }

        // ===== DAY OF WEEK =====
        if (e.getDateDebut() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDateDebut());
            int day = cal.get(Calendar.DAY_OF_WEEK);

            // Weekdays are calmer (good)
            if (day == Calendar.MONDAY || day == Calendar.TUESDAY ||
                    day == Calendar.WEDNESDAY || day == Calendar.THURSDAY) {
                score += 15;
                System.out.println("  ✅ Weekday (calmer)");
            }
            // Weekends are crowded (bad)
            else if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                score -= 20;
                System.out.println("  ❌ Weekend (crowded)");
            }
        }

        // ===== PARTICIPANTS =====
        if (e.getMaxParticipant() > 0) {
            if (e.getMaxParticipant() <= 15) {
                score += 25; // Very small group = ideal
                System.out.println("  ✅ Very small group: " + e.getMaxParticipant());
            } else if (e.getMaxParticipant() <= 30) {
                score += 15; // Small group
                System.out.println("  ✅ Small group: " + e.getMaxParticipant());
            } else if (e.getMaxParticipant() <= 50) {
                score += 5; // Medium group
                System.out.println("  ⚖️ Medium group: " + e.getMaxParticipant());
            } else if (e.getMaxParticipant() <= 100) {
                score -= 10; // Large group
                System.out.println("  ❌ Large group: " + e.getMaxParticipant());
            } else {
                score -= 25; // Very large group = stressful
                System.out.println("  ❌ Very large group (stressful): " + e.getMaxParticipant());
            }
        }

        // ===== DURATION =====
        if (e.getDateDebut() != null && e.getDateFin() != null) {
            long diff = e.getDateFin().getTime() - e.getDateDebut().getTime();
            int heures = (int) (diff / (1000 * 60 * 60));

            if (heures <= 1) {
                score += 20; // Very short
                System.out.println("  ✅ Very short: " + heures + "h");
            } else if (heures <= 2) {
                score += 15; // Short
                System.out.println("  ✅ Short: " + heures + "h");
            } else if (heures <= 3) {
                score += 5; // Medium
                System.out.println("  ⚖️ Medium: " + heures + "h");
            } else if (heures <= 4) {
                score -= 5; // Long
                System.out.println("  ❌ Long: " + heures + "h");
            } else {
                score -= 20; // Too long
                System.out.println("  ❌ Too long: " + heures + "h");
            }
        }

        // ===== EVENT TYPE =====
        if (e.getTypeEvent() != null) {
            String type = e.getTypeEvent().toLowerCase();

            // Calm types (good)
            if (type.contains("atelier") || type.contains("formation") ||
                    type.contains("yoga") || type.contains("méditation") ||
                    type.contains("thérapie") || type.contains("relaxation") ||
                    type.contains("lecture") || type.contains("bibliothèque")) {
                score += 20;
                System.out.println("  ✅ Calm type: " + type);
            }
            // Noisy types (bad)
            else if (type.contains("festival") || type.contains("concert") ||
                    type.contains("fête") || type.contains("party") ||
                    type.contains("soirée") || type.contains("gala") ||
                    type.contains("spectacle")) {
                score -= 30;
                System.out.println("  ❌ Noisy type: " + type);
            }
            // Social types (can be stressful)
            else if (type.contains("rencontre") || type.contains("réseautage") ||
                    type.contains("conférence") || type.contains("séminaire")) {
                score -= 10;
                System.out.println("  ❌ Social type: " + type);
            }
        }

        // Bonus if mostly positive
        if (positifCount > 2 && negatifCount == 0) {
            score += 15;
            System.out.println("  ✅ Bonus: very good profile");
        }

        // Malus if too many negatives
        if (negatifCount > 2) {
            score -= 20;
            System.out.println("  ❌ Malus: too many negative factors");
        }

        // Ensure between 0-100
        double finalScore = Math.max(0, Math.min(100, score));
        System.out.println("  🧩 FINAL AUTISM SCORE: " + finalScore + "%");

        return finalScore;
    }

    // ===== KEYWORD FALLBACK =====
    public double predictAutismFromKeywords(String title, String description) {
        String combined = (title + " " + description).toLowerCase();
        double score = 50.0;

        // Positive keywords
        String[] autismPositive = {
                "calme", "silence", "tranquille", "zen", "accessible",
                "structure", "petit groupe", "intimiste", "relaxation",
                "méditation", "yoga", "adapté", "paisible", "sécurisé"
        };

        // Negative keywords
        String[] autismNegative = {
                "foule", "bruyant", "fête", "party", "festival",
                "concert", "spectacle", "surprise", "improvisation",
                "lumières", "musique forte", "grand public", "chaotique"
        };

        for (String kw : autismPositive) {
            if (combined.contains(kw)) {
                score += 8;
            }
        }

        for (String kw : autismNegative) {
            if (combined.contains(kw)) {
                score -= 12;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    // ===== PREDICT AUTISM SCORE =====
    public double predictAutismScore(Event e) {
        try {
            if (!isTrained) {
                System.out.println("⚠️ Model not trained, using keywords");
                return predictAutismFromKeywords(e.getTitre(), e.getDescription());
            }

            Instances predictData = new Instances(dataStructure, 0);
            double[] values = extractFeatures(e);

            values[values.length - 1] = Utils.missingValue();

            Instance instance = new DenseInstance(1.0, values);
            instance.setDataset(dataStructure);

            double prediction = model.classifyInstance(instance);
            prediction = Math.max(0, Math.min(100, prediction));

            System.out.println("🧩 Autism prediction for " + e.getTitre() + ": " + prediction + "%");

            return prediction;

        } catch (Exception ex) {
            System.out.println("⚠️ Prediction error, using keywords: " + ex.getMessage());
            return predictAutismFromKeywords(e.getTitre(), e.getDescription());
        }
    }

    public boolean isTrained() {
        return isTrained;
    }
}