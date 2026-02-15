package tn.esprit.pidev;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Locale;
import java.util.function.UnaryOperator;

public final class FxValidators {

    private FxValidators() {}

    /** Autorise uniquement un entier (optionnellement vide). */
    public static void applyIntegerFormatter(TextField tf) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            return newText.matches("\\d{0,9}") ? change : null; // jusqu'à 9 chiffres
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    /** Autorise uniquement un double (optionnellement vide), avec point ou virgule. */
    public static void applyDoubleFormatter(TextField tf) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;

            // autorise 123 / 123.4 / 123,4 / .5 (on évite)
            if (newText.matches("\\d{0,9}([\\.,]\\d{0,4})?")) return change;
            return null;
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    public static int requireInt(TextField tf, String fieldName) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException(fieldName + " obligatoire.");
        return Integer.parseInt(s);
    }

    public static double requireDouble(TextField tf, String fieldName) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException(fieldName + " obligatoire.");

        // support virgule
        s = s.replace(',', '.');
        return Double.parseDouble(s);
    }

    public static String requireText(TextField tf, String fieldName) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException(fieldName + " obligatoire.");
        return s;
    }

    public static String normalizeLibelle(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT);
    }
}
