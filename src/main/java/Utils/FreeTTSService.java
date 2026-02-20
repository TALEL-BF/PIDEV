package Utils;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class FreeTTSService {

    private static Voice voice;
    private static boolean isInitialized = false;
    private static boolean isSpeaking = false;

    static {
        try {
            System.out.println("🔊 Initialisation de FreeTTS - Voix Kevin...");

            // Configuration FreeTTS
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin16"); // La vraie voix Kevin

            if (voice != null) {
                voice.allocate();

                // ===== RÉGLAGES OPTIMAUX POUR LA VOIX KEVIN =====
                voice.setRate(150);              // Vitesse normale (150)
                voice.setPitch(100);              // Hauteur naturelle de Kevin
                voice.setPitchRange(40);           // Variation naturelle
                voice.setPitchShift(1.0f);         // Pas de décalage (voix naturelle)
                voice.setVolume(5);                 // Volume maximum
                voice.setDurationStretch(1.0f);     // Pas d'étirement (voix naturelle)

                isInitialized = true;
                System.out.println("✅ FreeTTS initialisé avec la voix KEVIN");
                System.out.println("   - Vitesse: 150 (normale)");
                System.out.println("   - Hauteur: 100 (naturelle)");
                System.out.println("   - Volume: 5 (max)");
            } else {
                System.err.println("❌ Voix Kevin non trouvée");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation FreeTTS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;

        if (!isInitialized || voice == null) {
            System.err.println("❌ FreeTTS non initialisé, fallback sur SimpleTTS");
            SimpleTTS.speak(text);
            return;
        }

        if (isSpeaking) {
            System.out.println("⏳ Parole déjà en cours...");
            return;
        }

        new Thread(() -> {
            try {
                isSpeaking = true;
                System.out.println("🔊 [Kevin] " + text);

                // Petite pause avant de parler
                Thread.sleep(50);

                // Parler avec la voix naturelle de Kevin
                voice.speak(text);

                // Petite pause après la parole
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("❌ Erreur FreeTTS: " + e.getMessage());
                SimpleTTS.speak(text); // Fallback
            } finally {
                isSpeaking = false;
            }
        }).start();
    }

    /**
     * Parler plus lentement (pour les enfants)
     */
    public static void speakSlow(String text) {
        if (!isInitialized || voice == null) return;

        new Thread(() -> {
            try {
                float oldRate = voice.getRate();
                voice.setRate(120); // Plus lent
                voice.speak(text);
                voice.setRate(oldRate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Parler plus vite
     */
    public static void speakFast(String text) {
        if (!isInitialized || voice == null) return;

        new Thread(() -> {
            try {
                float oldRate = voice.getRate();
                voice.setRate(180); // Plus vite
                voice.speak(text);
                voice.setRate(oldRate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void speakAsync(String text) {
        speak(text);
    }

    public static void setRate(float rate) {
        if (voice != null) {
            voice.setRate(rate);
            System.out.println("🔊 Vitesse réglée à: " + rate);
        }
    }

    public static void setPitch(int pitch) {
        if (voice != null) {
            voice.setPitch(pitch);
            System.out.println("🔊 Hauteur réglée à: " + pitch);
        }
    }

    public static void setVolume(int volume) {
        if (voice != null) {
            voice.setVolume(volume);
            System.out.println("🔊 Volume réglé à: " + volume);
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Réinitialiser aux réglages par défaut de Kevin
     */
    public static void resetToDefault() {
        if (voice != null) {
            voice.setRate(150);
            voice.setPitch(100);
            voice.setPitchRange(40);
            voice.setPitchShift(1.0f);
            voice.setVolume(5);
            voice.setDurationStretch(1.0f);
            System.out.println("🔊 Réinitialisation aux réglages par défaut de Kevin");
        }
    }
}