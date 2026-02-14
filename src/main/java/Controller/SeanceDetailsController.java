package Controller;

import Entites.Seance;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class SeanceDetailsController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblDate;
    @FXML private Label lblHeure;
    @FXML private Label lblJour;
    @FXML private Label lblDuree;
    @FXML private Label lblStatut;
    @FXML private Label lblId;
    @FXML private Label lblIdAutiste;
    @FXML private Label lblIdProfesseur;
    @FXML private Label lblIdCours;

    private Seance seance;

    public void setSeance(Seance seance) {
        this.seance = seance;
        displaySeanceDetails();
    }

    private void displaySeanceDetails() {
        if (seance == null) return;

        lblTitre.setText(seance.getTitreSeance());
        lblDescription.setText(seance.getDescription());

        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDate.setText(seance.getDateSeance().format(dateFormatter));

        // Format time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        lblHeure.setText(seance.getDateSeance().format(timeFormatter));

        lblJour.setText(capitalizeFirst(seance.getJoursSemaine()));
        lblDuree.setText(seance.getDuree() + " minutes");
        lblId.setText("#" + String.format("%03d", seance.getIdSeance()));

        // Set statut with color
        lblStatut.setText(capitalizeFirst(seance.getStatutSeance()));
        lblStatut.setStyle(getStatutStyle(seance.getStatutSeance()));

        lblIdAutiste.setText(String.valueOf(seance.getIdAutiste()));
        lblIdProfesseur.setText(String.valueOf(seance.getIdProfesseur()));
        lblIdCours.setText(String.valueOf(seance.getIdCours()));
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getStatutStyle(String statut) {
        return switch (statut.toLowerCase()) {
            case "confirme" -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #10b981;";
            case "planifiee" -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #f59e0b;";
            case "termine" -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #6366f1;";
            case "annule" -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #ef4444;";
            case "reporte" -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #8b5cf6;";
            default -> "-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #64748b;";
        };
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}

