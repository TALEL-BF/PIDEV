package Utils;

public class TextToSpeechManager {

    private static boolean isEnabled = true;


    private static final int TTS_METHOD = 1;

    public static void speak(String text) {
        if (!isEnabled || text == null || text.trim().isEmpty()) {
            return;
        }

        String textToSpeak = text.trim();

        switch (TTS_METHOD) {
            case 1:
                FreeTTSService.speak(textToSpeak);
                break;
            case 2:
            default:
                SimpleTTS.speak(textToSpeak);
                break;
        }
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
        System.out.println("🔊 TTS " + (enabled ? "activé" : "désactivé"));
    }

    public static boolean isEnabled() {
        return isEnabled;
    }
}