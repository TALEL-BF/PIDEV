package Entites;

import java.time.LocalDateTime;

public class Seance {
    private int idSeance;
    private String titreSeance;
    private String description;
    private LocalDateTime dateSeance;
    private String joursSemaine;
    private int duree;
    private String statutSeance; // planifiee, confirme, annule, reporte, termine
    private int idAutiste;
    private int idProfesseur;
    private int idCours;

    // Constructors
    public Seance() {}

    public Seance(int idSeance, String titreSeance, String description, LocalDateTime dateSeance,
                  String joursSemaine, int duree, String statutSeance, int idAutiste, int idProfesseur, int idCours) {
        this.idSeance = idSeance;
        this.titreSeance = titreSeance;
        this.description = description;
        this.dateSeance = dateSeance;
        this.joursSemaine = joursSemaine;
        this.duree = duree;
        this.statutSeance = statutSeance;
        this.idAutiste = idAutiste;
        this.idProfesseur = idProfesseur;
        this.idCours = idCours;
    }

    public Seance(String titreSeance, String description, LocalDateTime dateSeance,
                  String joursSemaine, int duree, String statutSeance, int idAutiste, int idProfesseur, int idCours) {
        this.titreSeance = titreSeance;
        this.description = description;
        this.dateSeance = dateSeance;
        this.joursSemaine = joursSemaine;
        this.duree = duree;
        this.statutSeance = statutSeance;
        this.idAutiste = idAutiste;
        this.idProfesseur = idProfesseur;
        this.idCours = idCours;
    }

    // Getters and Setters
    public int getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(int idSeance) {
        this.idSeance = idSeance;
    }

    public String getTitreSeance() {
        return titreSeance;
    }

    public void setTitreSeance(String titreSeance) {
        this.titreSeance = titreSeance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateSeance() {
        return dateSeance;
    }

    public void setDateSeance(LocalDateTime dateSeance) {
        this.dateSeance = dateSeance;
    }

    public String getJoursSemaine() {
        return joursSemaine;
    }

    public void setJoursSemaine(String joursSemaine) {
        this.joursSemaine = joursSemaine;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getStatutSeance() {
        return statutSeance;
    }

    public void setStatutSeance(String statutSeance) {
        this.statutSeance = statutSeance;
    }

    public int getIdAutiste() {
        return idAutiste;
    }

    public void setIdAutiste(int idAutiste) {
        this.idAutiste = idAutiste;
    }

    public int getIdProfesseur() {
        return idProfesseur;
    }

    public void setIdProfesseur(int idProfesseur) {
        this.idProfesseur = idProfesseur;
    }

    public int getIdCours() {
        return idCours;
    }

    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    @Override
    public String toString() {
        return "Seance{" +
                "idSeance=" + idSeance +
                ", titreSeance='" + titreSeance + '\'' +
                ", dateSeance=" + dateSeance +
                ", statutSeance='" + statutSeance + '\'' +
                '}';
    }
}

