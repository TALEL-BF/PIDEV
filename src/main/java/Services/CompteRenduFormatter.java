package Services;

import Entites.Suivie;
import Entites.Therapie;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompteRenduFormatter {

    public static String build(Suivie s, Therapie t) {
        LocalDateTime dt = s.getDateSuivie() == null ? null : s.getDateSuivie().toLocalDateTime();
        String date = (dt == null) ? "-" : dt.toLocalDate().toString();
        String heure = (dt == null) ? "-" : dt.toLocalTime().withSecond(0).withNano(0).toString();

        String ex = (t == null) ? "-" : (safe(t.getNomExercice()) + " (" + safe(t.getTypeExercice()) + ")");

        return ""
                + "Bonjour,\n\n"
                + "Voici le compte rendu du suivi de votre enfant :\n\n"
                + "Psychologue : " + safe(s.getNomPsy()) + "\n"
                + "État : " + safe(s.getStatut()) + "\n"
                + "Date : " + date + "\n"
                + "Heure : " + heure + "\n"
                + "Scores (H/S/A) : " + s.getScoreHumeur() + " / " + s.getScoreStress() + " / " + s.getScoreAttention() + "\n"
                + "Exercice recommandé : " + ex + "\n"
                + "Comportement : " + safe(s.getComportement()) + "\n"
                + "Interaction : " + safe(s.getInteractionSociale()) + "\n\n"
                + "Observation :\n" + safe(s.getObservation()) + "\n\n"
                + "Cordialement.\n";
    }

    private static String safe(String v) {
        return (v == null || v.isBlank()) ? "-" : v.trim();
    }
}