package Entites;

public class EmploiDuTemps {
    private int idEmploi;
    private String anneeScolaire;
    private String jourSemaine; // lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche
    private String trancheHoraire; // matin, apres_midi, soir, journee
    private Integer idRdv;
    private Integer idSeance;

    // Constructors
    public EmploiDuTemps() {}

    public EmploiDuTemps(int idEmploi, String anneeScolaire, String jourSemaine,
                         String trancheHoraire, Integer idRdv, Integer idSeance) {
        this.idEmploi = idEmploi;
        this.anneeScolaire = anneeScolaire;
        this.jourSemaine = jourSemaine;
        this.trancheHoraire = trancheHoraire;
        this.idRdv = idRdv;
        this.idSeance = idSeance;
    }

    public EmploiDuTemps(String anneeScolaire, String jourSemaine,
                         String trancheHoraire, Integer idRdv, Integer idSeance) {
        this.anneeScolaire = anneeScolaire;
        this.jourSemaine = jourSemaine;
        this.trancheHoraire = trancheHoraire;
        this.idRdv = idRdv;
        this.idSeance = idSeance;
    }

    // Getters and Setters
    public int getIdEmploi() {
        return idEmploi;
    }

    public void setIdEmploi(int idEmploi) {
        this.idEmploi = idEmploi;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public String getTrancheHoraire() {
        return trancheHoraire;
    }

    public void setTrancheHoraire(String trancheHoraire) {
        this.trancheHoraire = trancheHoraire;
    }

    public Integer getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(Integer idRdv) {
        this.idRdv = idRdv;
    }

    public Integer getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(Integer idSeance) {
        this.idSeance = idSeance;
    }

    @Override
    public String toString() {
        return "EmploiDuTemps{" +
                "idEmploi=" + idEmploi +
                ", anneeScolaire='" + anneeScolaire + '\'' +
                ", jourSemaine='" + jourSemaine + '\'' +
                ", trancheHoraire='" + trancheHoraire + '\'' +
                '}';
    }
}

