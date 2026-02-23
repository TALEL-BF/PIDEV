package Services;

import Entites.Suivie;
import Entites.Therapie;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompteRenduPdfService {

    // ===== PDF Theme (même esprit app.css) =====
    private static final Color PRIMARY = new Color(124, 58, 237);      // #7C3AED (mauve)
    private static final Color BORDER  = new Color(170, 160, 220);
    private static final Color BG_BOX  = new Color(245, 246, 250);
    private static final Color TEXT    = new Color(20, 20, 20);
    private static final Color MUTED   = new Color(90, 90, 90);

    private static final float MARGIN = 50;
    private static final float BOTTOM = 60;
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;

    // Cursor multi-page
    private static class Cursor {
        PDDocument doc;
        PDPage page;
        PDPageContentStream cs;
        float y;
        int pageIndex = 0;
        PDImageXObject logo;
    }

    public File generate(Suivie s, Therapie t) throws IOException {
        // dossier sortie
        File outDir = new File(System.getProperty("user.home"), "PIDEV_CompteRendus");
        if (!outDir.exists()) outDir.mkdirs();

        String fileName = "CR_Suivi_" + s.getIdSuivie() + ".pdf";
        File out = new File(outDir, fileName);

        try (PDDocument doc = new PDDocument()) {
            Cursor cur = new Cursor();
            cur.doc = doc;
            cur.logo = loadLogo(doc);

            // 1ère page
            newPage(cur);

            // ===== Contenu =====
            sectionTitle(cur, "Informations du suivi");
            LocalDateTime dt = s.getDateSuivie() == null ? null : s.getDateSuivie().toLocalDateTime();
            String date = (dt == null) ? "-" : dt.toLocalDate().toString();
            String heure = (dt == null) ? "-" : dt.toLocalTime().withSecond(0).withNano(0).toString();

            kv(cur, "Enfant", safe(s.getNomEnfant()));
            kv(cur, "Psychologue", safe(s.getNomPsy()));
            kv(cur, "Etat", safe(s.getStatut()));
            kv(cur, "Date", date);
            kv(cur, "Heure", heure);
            kv(cur, "Scores (H/S/A)", s.getScoreHumeur() + " / " + s.getScoreStress() + " / " + s.getScoreAttention());
            kv(cur, "Comportement", safe(s.getComportement()));
            kv(cur, "Interaction", safe(s.getInteractionSociale()));
            cur.y -= 6;

            sectionTitle(cur, "Observation");
            boxedParagraph(cur, s.getObservation());

            sectionTitle(cur, "Exercice recommande");
            if (t == null) {
                boxedParagraph(cur, "Aucun exercice recommande.");
            } else {
                String ex =
                        "Nom exercice : " + safe(t.getNomExercice()) + "\n" +
                                "Type : " + safe(t.getTypeExercice()) + "\n" +
                                "Objectif : " + safe(t.getObjectif()) + "\n" +
                                "Duree (min) : " + t.getDureeMin() + "\n" +
                                "Niveau : " + t.getNiveau() + "\n" +
                                "Materiel : " + safe(t.getMateriel()) + "\n" +
                                "Cible : " + safe(t.getCible()) + "\n\n" +
                                "Description:\n" + safe(t.getDescription()) + "\n\n" +
                                "Adaptation TSA:\n" + safe(t.getAdaptationTsa());
                boxedParagraph(cur, ex);
            }

            sectionTitle(cur, "Conseils personnalises (IA)");
            boxedParagraph(cur, s.getCrResume());

            // fermer dernière page
            writeFooter(cur);
            cur.cs.close();

            doc.save(out);
        }

        return out;
    }

    // =========================
    // Page handling
    // =========================
    private void newPage(Cursor cur) throws IOException {
        if (cur.cs != null) {
            writeFooter(cur);
            cur.cs.close();
        }

        cur.page = new PDPage(PDRectangle.A4);
        cur.doc.addPage(cur.page); // addPage officiel PDFBox :contentReference[oaicite:3]{index=3}
        cur.pageIndex++;

        cur.cs = new PDPageContentStream(cur.doc, cur.page, AppendMode.OVERWRITE, true);

        // Header mauve
        cur.cs.setNonStrokingColor(PRIMARY);
        cur.cs.addRect(0, PAGE_H - 90, PAGE_W, 90);
        cur.cs.fill();

        // Logo
        if (cur.logo != null) {
            cur.cs.drawImage(cur.logo, MARGIN, PAGE_H - 78, 40, 40); // drawImage PDFBox :contentReference[oaicite:4]{index=4}
        }

        // Title
        cur.cs.beginText();
        cur.cs.setNonStrokingColor(Color.WHITE);
        cur.cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
        cur.cs.newLineAtOffset(MARGIN + 55, PAGE_H - 62);
        cur.cs.showText("Compte rendu de suivi");
        cur.cs.endText();

        cur.y = PAGE_H - 120;
    }

    private void ensureSpace(Cursor cur, float neededHeight) throws IOException {
        if (cur.y - neededHeight < BOTTOM) {
            newPage(cur);
        }
    }

    private void writeFooter(Cursor cur) throws IOException {
        cur.cs.beginText();
        cur.cs.setNonStrokingColor(new Color(120, 120, 120));
        cur.cs.setFont(PDType1Font.HELVETICA, 9);
        cur.cs.newLineAtOffset(MARGIN, 30);
        cur.cs.showText("Genere par PIDEV - AutiCare   |   Page " + cur.pageIndex);
        cur.cs.endText();
    }

    // =========================
    // Drawing helpers
    // =========================
    private void sectionTitle(Cursor cur, String title) throws IOException {
        ensureSpace(cur, 28);
        cur.cs.beginText();
        cur.cs.setNonStrokingColor(PRIMARY);
        cur.cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cur.cs.newLineAtOffset(MARGIN, cur.y);
        cur.cs.showText(title);
        cur.cs.endText();
        cur.y -= 18;
    }

    private void kv(Cursor cur, String k, String v) throws IOException {
        ensureSpace(cur, 18);

        cur.cs.beginText();
        cur.cs.setNonStrokingColor(MUTED);
        cur.cs.setFont(PDType1Font.HELVETICA, 10);
        cur.cs.newLineAtOffset(MARGIN, cur.y);
        cur.cs.showText(k + " :");
        cur.cs.endText();

        cur.cs.beginText();
        cur.cs.setNonStrokingColor(TEXT);
        cur.cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        cur.cs.newLineAtOffset(MARGIN + 140, cur.y);
        cur.cs.showText(safe(v));
        cur.cs.endText();

        cur.y -= 14;
    }

    private void boxedParagraph(Cursor cur, String text) throws IOException {
        String safeText = (text == null || text.isBlank()) ? "Aucun contenu." : text.trim();

        float padding = 10;
        float fontSize = 10;
        float leading = 1.35f * fontSize;
        PDFont font = PDType1Font.HELVETICA;

        List<String> lines = wrapLines(safeText, font, fontSize, CONTENT_W - 2 * padding);

        // نكتب chunk ب chunk (باش يدعم multi-page)
        int i = 0;
        while (i < lines.size()) {
            // أقصى عدد أسطر تنجم تدخل في الصفحة الحالية
            float availableH = cur.y - BOTTOM - 10;
            int maxLines = (int) ((availableH - (2 * padding + 6)) / leading);
            if (maxLines < 3) {
                newPage(cur);
                continue;
            }

            int end = Math.min(lines.size(), i + maxLines);
            List<String> chunk = lines.subList(i, end);

            float h = Math.max(52, (chunk.size() * leading) + 2 * padding + 6);
            ensureSpace(cur, h + 8);

            // box background
            cur.cs.setNonStrokingColor(BG_BOX);
            cur.cs.addRect(MARGIN, cur.y - h, CONTENT_W, h);
            cur.cs.fill();

            // box border
            cur.cs.setStrokingColor(BORDER);
            cur.cs.addRect(MARGIN, cur.y - h, CONTENT_W, h);
            cur.cs.stroke();

            float textY = cur.y - padding - 6;
            for (String line : chunk) {
                cur.cs.beginText();
                cur.cs.setNonStrokingColor(TEXT);
                // setFont(font,size) حسب Javadoc الرسمي :contentReference[oaicite:5]{index=5}
                cur.cs.setFont(font, fontSize);
                cur.cs.newLineAtOffset(MARGIN + padding, textY);
                cur.cs.showText(line);
                cur.cs.endText();
                textY -= leading;
            }

            cur.y -= (h + 14);
            i = end;

            if (i < lines.size()) {
                ensureSpace(cur, 18);
                cur.cs.beginText();
                cur.cs.setNonStrokingColor(PRIMARY);
                cur.cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                cur.cs.newLineAtOffset(MARGIN, cur.y);
                cur.cs.showText("Suite ...");
                cur.cs.endText();
                cur.y -= 12;
            }
        }
    }

    private List<String> wrapLines(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\\r?\\n");

        for (String para : paragraphs) {
            String p = para.trim();
            if (p.isEmpty()) { lines.add(""); continue; }

            String[] words = p.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String test = line.length() == 0 ? word : line + " " + word;
                float width = font.getStringWidth(test) / 1000f * fontSize;

                if (width <= maxWidth) {
                    line.setLength(0);
                    line.append(test);
                } else {
                    if (line.length() > 0) lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (line.length() > 0) lines.add(line.toString());
        }
        return lines;
    }

    private PDImageXObject loadLogo(PDDocument doc) {
        try (InputStream is = getClass().getResourceAsStream("/assets/logo.png")) {
            if (is == null) return null;
            // createFromByteArray موجود في PDImageXObject. :contentReference[oaicite:6]{index=6}
            return PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo");
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "-" : v.trim();
    }
}