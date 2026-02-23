package Controller;

import Entites.Suivie;
import Entites.Therapie;
import Services.CompteRenduPdfService;
import Services.GeminiConseilsService;
import Services.SuivieServices;
import Services.TherapieServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParentSuiviController {

    @FXML private BorderPane root;

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
    @FXML private Button btnUploadParentPdf;

    // Sidebar psychologie
    @FXML private ToggleButton tbPsychologie;
    @FXML private VBox psychologieSubMenu;

    private Suivie dernierSuivi;

    private final SuivieServices suivieServices = new SuivieServices();
    private final GeminiConseilsService gemini = new GeminiConseilsService();

    @FXML
    public void initialize() {
        lblInfo.setText("Veuillez saisir votre email.");
        cbEnfants.getItems().clear();

        xAxis.setTickLabelRotation(45);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);

        // menu psycho ouvert par défaut
        tbPsychologie.setSelected(true);
        psychologieSubMenu.setVisible(true);
        psychologieSubMenu.setManaged(true);
        if (!tbPsychologie.getStyleClass().contains("sideBtnActive")) {
            tbPsychologie.getStyleClass().add("sideBtnActive");
        }
    }

    // =========================
    // Navigation (scene switch)
    // =========================
    @FXML
    private void goArticlesConseils(ActionEvent event) {
        switchScene(event, "/MainArticles.fxml");
    }

    @FXML
    private void goSuivieTherapeutique(ActionEvent event) {
        // Tu es déjà dans ParentSuivi.fxml -> rien à faire
        // (ou reload si tu veux) : switchScene(event, "/ParentSuivi.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(view);

            // Si ton CSS est global, tu peux le remettre (optionnel si déjà dans FXML)
            // scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("❌ Navigation impossible : " + e.getMessage());
        }
    }

    @FXML
    private void togglePsychologie() {
        boolean show = tbPsychologie.isSelected();
        psychologieSubMenu.setVisible(show);
        psychologieSubMenu.setManaged(show);

        if (show) {
            if (!tbPsychologie.getStyleClass().contains("sideBtnActive")) {
                tbPsychologie.getStyleClass().add("sideBtnActive");
            }
        } else {
            tbPsychologie.getStyleClass().remove("sideBtnActive");
        }
    }

    // =========================
    // Chercher enfants
    // =========================
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

    // =========================
    // Voir suivi + Graphe
    // =========================
    @FXML
    private void onVoirSuivi() {
        String email = tfEmailParent.getText() == null ? "" : tfEmailParent.getText().trim();
        String enfant = cbEnfants.getValue();

        if (email.isEmpty()) { lblInfo.setText("❌ Veuillez entrer un email."); return; }
        if (enfant == null) { lblInfo.setText("Choisissez un enfant."); return; }

        List<Suivie> stats = suivieServices.getStatsByEmailAndEnfant(email, enfant);
        lineChart.getData().clear();

        XYChart.Series<String, Number> humeur = new XYChart.Series<>();
        humeur.setName("Humeur");

        XYChart.Series<String, Number> stress = new XYChart.Series<>();
        stress.setName("Stress");

        XYChart.Series<String, Number> attention = new XYChart.Series<>();
        attention.setName("Attention");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Suivie s : stats) {
            String base = (s.getDateSuivie() == null) ? "Sans date"
                    : s.getDateSuivie().toLocalDateTime().format(fmt);
            String label = base + " (#" + s.getIdSuivie() + ")";

            humeur.getData().add(new XYChart.Data<>(label, s.getScoreHumeur()));
            stress.getData().add(new XYChart.Data<>(label, s.getScoreStress()));
            attention.getData().add(new XYChart.Data<>(label, s.getScoreAttention()));
        }

        lineChart.getData().addAll(humeur, stress, attention);

        Platform.runLater(() -> {
            applySeriesColors(humeur, stress, attention);
            lineChart.lookupAll(".chart-line-symbol").forEach(n ->
                    n.setStyle(n.getStyle() + "; -fx-padding: 4px;")
            );
        });

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

        lblPsy.setText(safe(dernierSuivi.getNomPsy()));
        lblEtat.setText(safe(dernierSuivi.getStatut()));

        LocalDateTime dt = (dernierSuivi.getDateSuivie() == null) ? null : dernierSuivi.getDateSuivie().toLocalDateTime();
        lblDateHeure.setText(dt == null ? "-" : dt.withSecond(0).withNano(0).toString());

        lblScores.setText(dernierSuivi.getScoreHumeur() + " / " +
                dernierSuivi.getScoreStress() + " / " +
                dernierSuivi.getScoreAttention());

        if (dernierSuivi.getIdTherapieReco() == null) {
            lblExercice.setText("-");
        } else {
            TherapieServices ts = new TherapieServices();
            Therapie t = ts.getTherapieById(dernierSuivi.getIdTherapieReco());
            lblExercice.setText(t != null ? safe(t.getNomExercice()) : "-");
        }

        lblObs.setText(dernierSuivi.getObservation() == null ? "-" : dernierSuivi.getObservation());
        btnDownloadPdf.setDisable(false);
    }

    private void applySeriesColors(
            XYChart.Series<String, Number> humeur,
            XYChart.Series<String, Number> stress,
            XYChart.Series<String, Number> attention
    ) {
        setSeriesStyle(humeur, "#22C55E");    // vert
        setSeriesStyle(stress, "#EF4444");    // rouge
        setSeriesStyle(attention, "#F97316"); // orange
    }

    private void setSeriesStyle(XYChart.Series<String, Number> series, String color) {
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3px;");
        }
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-background-color: " + color + ", white;");
            }
        }
    }

    // =========================
    // PDF Download
    // =========================
    @FXML
    private void onDownloadPdf() {
        try {
            if (dernierSuivi == null) {
                lblInfo.setText("Aucune consultation disponible.");
                return;
            }

            Therapie t = null;
            if (dernierSuivi.getIdTherapieReco() != null) {
                TherapieServices ts = new TherapieServices();
                t = ts.getTherapieById(dernierSuivi.getIdTherapieReco());
            }

            CompteRenduPdfService pdfService = new CompteRenduPdfService();
            File generated = pdfService.generate(dernierSuivi, t);

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

    // =========================
    // IA Advice
    // =========================
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

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return gemini.genererConseils(prompt);
            }
        };

        task.setOnSucceeded(ev -> {
            String res = task.getValue();
            try {
                suivieServices.updateConseilIA(dernierSuivi.getIdSuivie(), res);
                dernierSuivi.setCrResume(res);
                if (taConseils != null) taConseils.clear();
                lblInfo.setText("✅ Conseils IA enregistrés. Ils seront ajoutés au PDF.");
            } catch (Exception ex) {
                ex.printStackTrace();
                lblInfo.setText("❌ Erreur sauvegarde conseils IA : " + ex.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
            lblInfo.setText("❌ Erreur IA : " + (ex != null ? ex.getMessage() : "inconnue"));
        });

        new Thread(task).start();
    }

    @FXML
    private void onUploadParentPdf() {
        try {
            if (dernierSuivi == null) {
                lblInfo.setText("❌ Aucune consultation trouvée pour envoyer un document.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sujet du document");
            dialog.setHeaderText("Veuillez saisir le sujet du document (obligatoire)");
            dialog.setContentText("Sujet :");

            String sujet = dialog.showAndWait().orElse("").trim();
            if (sujet.isEmpty()) {
                lblInfo.setText("❌ Sujet obligatoire.");
                return;
            }

            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Choisir un document PDF (dessin / écriture)");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf", "*.PDF")
            );

            File src = chooser.showOpenDialog(btnUploadParentPdf.getScene().getWindow());
            if (src == null) return;

            if (!src.getName().toLowerCase().endsWith(".pdf")) {
                lblInfo.setText("❌ Veuillez choisir un fichier PDF.");
                return;
            }

            File outDir = new File(System.getProperty("user.home"), "PIDEV_ParentUploads");
            if (!outDir.exists()) outDir.mkdirs();

            String storedName = "PARENT_" + dernierSuivi.getIdSuivie() + "_" + System.currentTimeMillis() + ".pdf";
            File dest = new File(outDir, storedName);

            java.nio.file.Files.copy(
                    src.toPath(),
                    dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            suivieServices.updateParentUpload(
                    dernierSuivi.getIdSuivie(),
                    src.getName(),
                    dest.getAbsolutePath(),
                    sujet
            );

            lblInfo.setText("✅ Document envoyé au psychologue : " + src.getName());

        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("❌ Erreur upload : " + e.getMessage());
        }
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "-" : v.trim();
    }
}