package Services;

import Entites.Suivie;
import Entites.Therapie;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

public class CompteRenduPdfService {

    public File generate(Suivie s, Therapie t) throws IOException {
        // dossier sortie
        File outDir = new File(System.getProperty("user.home"), "PIDEV_CompteRendus");
        if (!outDir.exists()) outDir.mkdirs();

        String fileName = "CR_Suivi_" + s.getIdSuivie() + ".pdf";
        File out = new File(outDir, fileName);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ===== Header band =====
                cs.setNonStrokingColor(new Color(128, 80, 200)); // violet
                cs.addRect(0, y - 55, page.getMediaBox().getWidth(), 55);
                cs.fill();

                // Logo (optionnel) depuis resources/assets/logo.png
                try (InputStream is = getClass().getResourceAsStream("/assets/logo.png")) {
                    if (is != null) {
                        PDImageXObject logo = PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo");
                        cs.drawImage(logo, margin, y - 50, 40, 40);
                    }
                } catch (Exception ignored) {}

                // Title
                cs.beginText();
                cs.setNonStrokingColor(Color.WHITE);
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(margin + 55, y - 38);
                cs.showText("Compte rendu de suivi");
                cs.endText();

                y -= 80;

                // ===== Section: Infos suivi =====
                y = sectionTitle(cs, "Informations du suivi", margin, y);

                LocalDateTime dt = s.getDateSuivie() == null ? null : s.getDateSuivie().toLocalDateTime();
                String date = (dt == null) ? "-" : dt.toLocalDate().toString();
                String heure = (dt == null) ? "-" : dt.toLocalTime().withSecond(0).withNano(0).toString();

                y = kv(cs, "Enfant", safe(s.getNomEnfant()), margin, y);
                y = kv(cs, "Psychologue", safe(s.getNomPsy()), margin, y);
                y = kv(cs, "Etat", safe(s.getStatut()), margin, y);
                y = kv(cs, "Date", date, margin, y);
                y = kv(cs, "Heure", heure, margin, y);
                y = kv(cs, "Scores (H/S/A)",
                        s.getScoreHumeur() + " / " + s.getScoreStress() + " / " + s.getScoreAttention(),
                        margin, y);
                y = kv(cs, "Comportement", safe(s.getComportement()), margin, y);
                y = kv(cs, "Interaction", safe(s.getInteractionSociale()), margin, y);

                y -= 8;

                // ===== Observation box =====
                y = sectionTitle(cs, "Observation", margin, y);
                y = boxedText(cs, safe(s.getObservation()), margin, y, 460);

                y -= 10;

                // ===== Exercice recommandé (complet) =====
                y = sectionTitle(cs, "Exercice recommande", margin, y);

                if (t == null) {
                    y = normalText(cs, "Aucun exercice recommande.", margin, y);
                } else {
                    y = kv(cs, "Nom exercice", safe(t.getNomExercice()), margin, y);
                    y = kv(cs, "Type", safe(t.getTypeExercice()), margin, y);
                    y = kv(cs, "Objectif", safe(t.getObjectif()), margin, y);
                    y = kv(cs, "Duree (min)", String.valueOf(t.getDureeMin()), margin, y);
                    y = kv(cs, "Niveau", String.valueOf(t.getNiveau()), margin, y);
                    y = kv(cs, "Materiel", safe(t.getMateriel()), margin, y);
                    y = kv(cs, "Cible", safe(t.getCible()), margin, y);

                    y -= 6;
                    y = label(cs, "Description", margin, y);
                    y = boxedText(cs, safe(t.getDescription()), margin, y, 460);

                    y -= 6;
                    y = label(cs, "Adaptation TSA", margin, y);
                    y = boxedText(cs, safe(t.getAdaptationTsa()), margin, y, 460);
                }

                // Footer
                cs.beginText();
                cs.setNonStrokingColor(new Color(120, 120, 120));
                cs.setFont(PDType1Font.HELVETICA, 9);
                cs.newLineAtOffset(margin, 30);
                cs.showText("Genere par PIDEV - AutiCare");
                cs.endText();
            }

            doc.save(out);
        }

        return out;
    }

    // ===== helpers =====
    private float sectionTitle(PDPageContentStream cs, String title, float x, float y) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(new Color(60, 60, 60));
        cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 18;
    }

    private float kv(PDPageContentStream cs, String k, String v, float x, float y) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(new Color(90, 90, 90));
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(k + " :");
        cs.endText();

        cs.beginText();
        cs.setNonStrokingColor(new Color(20, 20, 20));
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        cs.newLineAtOffset(x + 140, y);
        cs.showText(v);
        cs.endText();

        return y - 14;
    }

    private float label(PDPageContentStream cs, String t, float x, float y) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(new Color(90, 90, 90));
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(t);
        cs.endText();
        return y - 12;
    }

    private float boxedText(PDPageContentStream cs, String text, float x, float y, float w) throws IOException {
        float h = 52; // simple (tu peux augmenter si tu veux)
        cs.setNonStrokingColor(new Color(245, 246, 250));
        cs.addRect(x, y - h + 10, w, h);
        cs.fill();

        cs.setNonStrokingColor(new Color(170, 160, 220));
        cs.addRect(x, y - h + 10, w, h);
        cs.stroke();

        cs.beginText();
        cs.setNonStrokingColor(new Color(20, 20, 20));
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x + 10, y - 12);
        cs.showText(truncate(text, 120)); // simple, pas de wrap avancé
        cs.endText();

        return y - h;
    }

    private float normalText(PDPageContentStream cs, String t, float x, float y) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(new Color(20, 20, 20));
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(t);
        cs.endText();
        return y - 14;
    }

    private String safe(String v) { return (v == null || v.isBlank()) ? "-" : v.trim(); }
    private String truncate(String v, int max) { return v.length() <= max ? v : v.substring(0, max - 3) + "..."; }
}