package Services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFGeneratorService {

    private static final String PDF_DIRECTORY = "rapports/";

    // Couleurs iText (BaseColor au lieu de Color)
    private static final BaseColor COLOR_PRIMARY = new BaseColor(139, 92, 246); // Violet
    private static final BaseColor COLOR_SECONDARY = new BaseColor(236, 72, 153); // Rose
    private static final BaseColor COLOR_SUCCESS = new BaseColor(34, 197, 94); // Vert
    private static final BaseColor COLOR_WARNING = new BaseColor(249, 115, 22); // Orange
    private static final BaseColor COLOR_ERROR = new BaseColor(239, 68, 68); // Rouge

    public String genererPDF(String contenu, String sujet) {
        try {
            // Créer le dossier
            new java.io.File(PDF_DIRECTORY).mkdirs();

            // Nom du fichier avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = PDF_DIRECTORY + "rapport_" + sujet + "_" + timestamp + ".pdf";

            // Créer le document
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            // ========== TITRE PRINCIPAL ==========
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.BOLD, COLOR_PRIMARY);
            Paragraph title = new Paragraph("RAPPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.NORMAL, BaseColor.DARK_GRAY);
            Paragraph subTitle = new Paragraph(sujet.toUpperCase(), subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(30);
            document.add(subTitle);

            // ========== DATE ==========
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph date = new Paragraph("Généré le : " + dateStr, dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // ========== TRAITEMENT DU CONTENU ==========
            String[] lignes = contenu.split("\n");

            for (String ligne : lignes) {
                if (ligne.trim().isEmpty()) {
                    document.add(new Paragraph(" "));
                    continue;
                }

                if (ligne.startsWith("📌 DÉFINITION")) {
                    Paragraph p = new Paragraph(ligne,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, COLOR_PRIMARY));
                    p.setSpacingBefore(15);
                    p.setSpacingAfter(5);
                    document.add(p);

                } else if (ligne.startsWith("✅ AVANTAGES")) {
                    Paragraph p = new Paragraph(ligne,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, COLOR_SUCCESS));
                    p.setSpacingBefore(15);
                    p.setSpacingAfter(5);
                    document.add(p);

                } else if (ligne.startsWith("❌ INCONVÉNIENTS")) {
                    Paragraph p = new Paragraph(ligne,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, COLOR_ERROR));
                    p.setSpacingBefore(15);
                    p.setSpacingAfter(5);
                    document.add(p);

                } else if (ligne.startsWith("💡 CONSEILS")) {
                    Paragraph p = new Paragraph(ligne,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, COLOR_WARNING));
                    p.setSpacingBefore(15);
                    p.setSpacingAfter(5);
                    document.add(p);

                } else if (ligne.startsWith("•")) {
                    // Points de liste
                    String text = ligne.substring(1).trim();
                    Paragraph p = new Paragraph("  • " + text,
                            FontFactory.getFont(FontFactory.HELVETICA, 12));
                    p.setIndentationLeft(20);
                    p.setSpacingAfter(3);
                    document.add(p);

                } else {
                    // Texte normal
                    Paragraph p = new Paragraph(ligne,
                            FontFactory.getFont(FontFactory.HELVETICA, 12));
                    p.setSpacingAfter(5);
                    document.add(p);
                }
            }

            // ========== FOOTER ==========
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Généré par AutiCare - Plateforme éducative", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode de compatibilité
    public String sauvegarderRapport(String contenu, String sujet) {
        return genererPDF(contenu, sujet);
    }
}