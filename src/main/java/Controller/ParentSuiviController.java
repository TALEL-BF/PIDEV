package Controller;

import Entites.Therapie;
import Services.CompteRenduPdfService;
import Services.GeminiConseilsService;
import Services.SuivieServices;
import Services.TherapieServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.chart.*;
import Entites.Suivie;

public class ParentSuiviController {

    @FXML private TextField tfEmailParent;
    @FXML private ComboBox<String> cbEnfants;
    @FXML private Label lblInfo;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label lblPsy;
    @FXML private Label lblEtat;
    @FXML private Label lblDateHeure;
    @FXML private Label lblScores;
    @FXML private Label lblExercice;
    @FXML private Label lblObs;
    @FXML private Button btnDownloadPdf;
    @FXML private Button btnAi;
    @FXML private TextArea taConseils;

    private Suivie dernierSuivi;

    private final SuivieServices suivieServices = new SuivieServices();
    private final GeminiConseilsService gemini = new GeminiConseilsService();


    @FXML
    public void initialize() {
        lblInfo.setText("Veuillez saisir votre email.");
        cbEnfants.getItems().clear();
        xAxis.setTickLabelRotation(45);
    }

    @FXML
    private void onChercher() {
        String email = tfEmailParent.getText() == null ? "" : tfEmailParent.getText().trim();

        if (email.isEmpty()) {
            lblInfo.setText("❌ Veuillez entrer un email.");
            cbEnfants.getItems().clear();
            return;
        }

        try {
            List<String> enfants = suivieServices.getEnfantsByEmail(email);

            cbEnfants.getItems().setAll(enfants);

            if (enfants.isEmpty()) {
                lblInfo.setText("Aucun enfant trouvé pour cet email.");
            } else {
                cbEnfants.getSelectionModel().selectFirst();
                lblInfo.setText("✅ " + enfants.size() + " enfant(s) trouvé(s).");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("❌ Erreur lors de la recherche.");
        }
    }

    @FXML
    private void onVoirSuivi() {
        String email = tfEmailParent.getText().trim();
        String enfant = cbEnfants.getValue();

        if (enfant == null) {
            lblInfo.setText("Choisissez un enfant.");
            return;
        }

        List<Suivie> stats = suivieServices.getStatsByEmailAndEnfant(email, enfant);

        System.out.println("NB consultations = " + stats.size());
        stats.forEach(x -> System.out.println(x.getIdSuivie() + " | " + x.getDateSuivie()));

        lineChart.getData().clear();

        XYChart.Series<String, Number> humeur = new XYChart.Series<>();
        humeur.setName("Humeur");

        XYChart.Series<String, Number> stress = new XYChart.Series<>();
        stress.setName("Stress");

        XYChart.Series<String, Number> attention = new XYChart.Series<>();
        attention.setName("Attention");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Suivie s : stats) {
            String base = (s.getDateSuivie() == null) ? "Sans date" :
                    s.getDateSuivie().toLocalDateTime().format(fmt);

            // ✅ garantit unique même si même minute
            String label = base + " (#" + s.getIdSuivie() + ")";

            humeur.getData().add(new XYChart.Data<>(label, s.getScoreHumeur()));
            stress.getData().add(new XYChart.Data<>(label, s.getScoreStress()));
            attention.getData().add(new XYChart.Data<>(label, s.getScoreAttention()));
        }

        lineChart.getData().addAll(humeur, stress, attention);

     //   applySeriesColors(humeur, stress, attention);

        xAxis.setTickLabelRotation(45);

        lblInfo.setText("Statistiques chargées pour " + enfant + " (" + stats.size() + ")");
        dernierSuivi = suivieServices.getDernierSuivi(email, enfant);

        if (dernierSuivi == null) {
            lblPsy.setText("-");
            lblEtat.setText("-");
            lblDateHeure.setText("-");
            lblScores.setText("-");
            lblExercice.setText("-");
            lblObs.setText("-");
            btnDownloadPdf.setDisable(true);
            lblInfo.setText("Aucun suivi trouvé pour " + enfant);
            return;
        }

        lblPsy.setText(dernierSuivi.getNomPsy());
        lblEtat.setText(dernierSuivi.getStatut());

        String dt = (dernierSuivi.getDateSuivie() == null) ? "-" :
                dernierSuivi.getDateSuivie().toLocalDateTime().withSecond(0).withNano(0).toString();
        lblDateHeure.setText(dt);

        lblScores.setText(dernierSuivi.getScoreHumeur() + " / " +
                dernierSuivi.getScoreStress() + " / " +
                dernierSuivi.getScoreAttention());

        if (dernierSuivi.getIdTherapieReco() == null) {
            lblExercice.setText("-");
        } else {
            TherapieServices ts = new TherapieServices();
            Therapie t = ts.getTherapieById(dernierSuivi.getIdTherapieReco());

            if (t != null) {
                lblExercice.setText(t.getNomExercice());
            } else {
                lblExercice.setText("-");
            }
        }
        lblObs.setText(dernierSuivi.getObservation() == null ? "-" : dernierSuivi.getObservation());

// ✅ IMPORTANT : le PDF sera généré au clic, donc on n’utilise PLUS CR_PDF_PATH
        btnDownloadPdf.setDisable(false);    }


    private void setSeriesStyle(XYChart.Series<String, Number> series, String color) {
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3px;");
        }
        // couleur des points
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-background-color: " + color + ", white;");
            }
        }
    }
    @FXML
    private void onDownloadPdf() {
        try {
            if (dernierSuivi == null) {
                lblInfo.setText("Aucune consultation disponible.");
                return;
            }

            // 1) Charger la thérapie recommandée (si existe)
            Therapie t = null;
            if (dernierSuivi.getIdTherapieReco() != null) {
                TherapieServices ts = new TherapieServices();
                t = ts.getTherapieById(dernierSuivi.getIdTherapieReco());
            }

            // 2) Générer le PDF (dans user.home/PIDEV_CompteRendus)
            CompteRenduPdfService pdfService = new CompteRenduPdfService();
            File generated = pdfService.generate(dernierSuivi, t);

            // 3) Choisir où enregistrer (télécharger)
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Enregistrer le compte rendu PDF");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf")
            );
            chooser.setInitialFileName(generated.getName());

            File dest = chooser.showSaveDialog(btnDownloadPdf.getScene().getWindow());
            if (dest == null) return;

            java.nio.file.Files.copy(
                    generated.toPath(),
                    dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            lblInfo.setText("✅ PDF généré et enregistré : " + dest.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("❌ Erreur génération PDF : " + e.getMessage());
        }
    }
    private void applySeriesColors(
            XYChart.Series<String, Number> humeur,
            XYChart.Series<String, Number> stress,
            XYChart.Series<String, Number> attention
    ) {

        // Humeur = rouge
        humeur.getNode().setStyle("-fx-stroke: #E53935;");

        // Stress = orange
        stress.getNode().setStyle("-fx-stroke: #FB8C00;");

        // Attention = vert
        attention.getNode().setStyle("-fx-stroke: #43A047;");

        // Couleur des points
        setDataPointsColor(humeur, "#E53935");
        setDataPointsColor(stress, "#FB8C00");
        setDataPointsColor(attention, "#43A047");
    }

    private void setDataPointsColor(XYChart.Series<String, Number> series, String color) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle(
                        "-fx-background-color: " + color + ", white;"
                );
            }
        }
    }


    @FXML
    private void onGenererConseilsIA() {

        if (dernierSuivi == null) {
            lblInfo.setText("Aucune consultation trouvée.");
            return;
        }

        String prompt = """
        Tu es un psychologue spécialisé en accompagnement d’enfants avec TSA.

        Génère UNIQUEMENT des conseils pratiques pour les parents.

        IMPORTANT :
        - Pas d’introduction.
        - Pas de formule de politesse.
        - Pas de lettre.
        - Pas de diagnostic médical.
        - Pas de médicaments.
        - Donne des conseils concrets, simples et applicables à la maison.
        - Format obligatoire :
            1) 6 conseils numérotés
            2) Un mini plan d'action sur 7 jours
            3) 3 signaux d'alerte nécessitant de recontacter le psychologue

        Données :
        Enfant : %s
        Scores (Humeur / Stress / Attention) : %d / %d / %d
        Observation : %s
        Comportement : %s
        Interaction sociale : %s

        Adapte les conseils aux scores.
        Si le stress est bas, propose des activités de stimulation.
        Si l’attention est élevée, propose des défis progressifs.
        Si l’humeur est moyenne, propose des stratégies émotionnelles adaptées.
        """
                .formatted(
                        safe(dernierSuivi.getNomEnfant()),
                        dernierSuivi.getScoreHumeur(),
                        dernierSuivi.getScoreStress(),
                        dernierSuivi.getScoreAttention(),
                        safe(dernierSuivi.getObservation()),
                        safe(dernierSuivi.getComportement()),
                        safe(dernierSuivi.getInteractionSociale())
                );

        // ✅ Appel en background pour ne pas bloquer l'UI
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override protected String call() throws Exception {
                return gemini.genererConseils(prompt);
            }
        };

        task.setOnSucceeded(ev -> {
            String res = task.getValue();
            taConseils.setText(res); // taConseils = TextArea dans ton FXML
            lblInfo.setText("Conseils IA générés ✅");
        });

        task.setOnFailed(ev -> {
            lblInfo.setText("Erreur IA : " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "-" : v.trim();
    }
}